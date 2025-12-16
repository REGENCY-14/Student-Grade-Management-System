import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Thread-safe asynchronous audit logger with file rotation.
 */
public class AuditLogger {

    private static final AuditLogger INSTANCE = new AuditLogger();
    public static AuditLogger getInstance() { return INSTANCE; }

    private final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>(200_000);
    private final Thread writerThread;

    private volatile BufferedWriter writer;
    private volatile LocalDate currentDate = LocalDate.now();
    private volatile long bytesWritten = 0;

    private static final long MAX_BYTES = 10L * 1024L * 1024L; // 10 MB
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ISO_INSTANT;
    private final Path logDir = Path.of("logs");

    private AuditLogger() {
        try { Files.createDirectories(logDir); } catch (Exception e) { }
        openWriter();

        writerThread = new Thread(this::runWriter, "audit-writer");
        writerThread.setDaemon(true);
        writerThread.start();

        // ensure graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            shutdown();
        }));
    }

    private synchronized void openWriter() {
        try {
            currentDate = LocalDate.now();
            String baseName = "audit_" + DATE_FMT.format(currentDate);
            int seq = 0;
            File f;
            do {
                String name = baseName + (seq == 0 ? "" : ("_" + seq)) + ".log";
                f = logDir.resolve(name).toFile();
                seq++;
            } while (f.exists() && f.length() >= MAX_BYTES);

            writer = new BufferedWriter(new FileWriter(f, true));
            bytesWritten = f.exists() ? f.length() : 0;
        } catch (IOException e) {
            writer = null;
        }
    }

    private void rotateIfNeeded() {
        try {
            LocalDate today = LocalDate.now();
            if (!today.equals(currentDate) || bytesWritten >= MAX_BYTES) {
                try { if (writer != null) writer.close(); } catch (Exception ex) { }
                openWriter();
            }
        } catch (Exception ex) { }
    }

    private void runWriter() {
        try {
            while (true) {
                String line = queue.poll(1, TimeUnit.SECONDS);
                if (line == null) {
                    rotateIfNeeded();
                    continue;
                }
                try {
                    if (writer == null) openWriter();
                    if (writer != null) {
                        writer.write(line);
                        writer.newLine();
                        writer.flush();
                        bytesWritten += line.getBytes().length + System.lineSeparator().getBytes().length;
                    }
                } catch (IOException e) {
                    // best-effort: drop if cannot write
                }
                rotateIfNeeded();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void log(String operationType, String userAction, long executionTimeMs, boolean success, String message) {
        String ts = TS_FMT.format(Instant.now());
        String threadId = String.valueOf(Thread.currentThread().getId());
        String safeMsg = message == null ? "" : message.replaceAll("[\n\r]", " ");
        String entry = String.join("|", ts, threadId, operationType, userAction, String.valueOf(executionTimeMs), success ? "SUCCESS" : "FAILURE", safeMsg);
        // Use blocking put to minimize risk of losing entries even under heavy concurrent load
        try {
            queue.put(entry);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Read recent entries (from all log files, most recent first)
    public List<AuditEntry> readRecentEntries(int limit) {
        List<File> files = listLogFiles();
        List<AuditEntry> all = new ArrayList<>();
        for (File f : files) {
            try {
                List<String> lines = Files.readAllLines(f.toPath());
                for (String l : lines) {
                    AuditEntry e = parseLine(l);
                    if (e != null) all.add(e);
                }
            } catch (Exception ex) { }
        }
        Collections.reverse(all); // newest first
        return all.subList(0, Math.min(limit, all.size()));
    }

    public List<AuditEntry> searchEntries(Instant from, Instant to, String operationType, Long threadId) {
        List<File> files = listLogFiles();
        List<AuditEntry> result = new ArrayList<>();
        for (File f : files) {
            try {
                List<String> lines = Files.readAllLines(f.toPath());
                for (String l : lines) {
                    AuditEntry e = parseLine(l);
                    if (e == null) continue;
                    if (from != null && e.timestamp.isBefore(from)) continue;
                    if (to != null && e.timestamp.isAfter(to)) continue;
                    if (operationType != null && !operationType.isEmpty() && !e.operationType.equalsIgnoreCase(operationType)) continue;
                    if (threadId != null && e.threadId != threadId) continue;
                    result.add(e);
                }
            } catch (Exception ex) { }
        }
        return result;
    }

    public AuditStats computeStats(Instant from, Instant to) {
        List<AuditEntry> entries = searchEntries(from, to, null, null);
        AuditStats stats = new AuditStats();
        for (AuditEntry e : entries) {
            long hour = e.timestamp.atZone(ZoneId.systemDefault()).withMinute(0).withSecond(0).withNano(0).toEpochSecond();
            stats.opsPerHour.merge(hour, 1, Integer::sum);
            if (e.executionTimeMs >= 0) {
                stats.totalExecTime += e.executionTimeMs;
                stats.countExec++;
            }
        }
        stats.avgExecTime = stats.countExec == 0 ? 0 : (stats.totalExecTime / (double) stats.countExec);
        return stats;
    }

    private List<File> listLogFiles() {
        File dir = logDir.toFile();
        File[] files = dir.listFiles((d, name) -> name.startsWith("audit_") && name.endsWith(".log"));
        List<File> list = new ArrayList<>();
        if (files != null) for (File f : files) list.add(f);
        list.sort((a, b) -> Long.compare(a.lastModified(), b.lastModified()));
        return list;
    }

    private AuditEntry parseLine(String line) {
        try {
            String[] parts = line.split("\\|", 7);
            if (parts.length < 6) return null;
            Instant ts = Instant.parse(parts[0]);
            long tid = Long.parseLong(parts[1]);
            String op = parts[2];
            String action = parts[3];
            long exec = Long.parseLong(parts[4]);
            boolean success = "SUCCESS".equalsIgnoreCase(parts[5]);
            String msg = parts.length >= 7 ? parts[6] : "";
            return new AuditEntry(ts, tid, op, action, exec, success, msg);
        } catch (Exception ex) {
            return null;
        }
    }

    public void shutdown() {
        try {
            // flush queue
            while (!queue.isEmpty()) {
                String line = queue.poll();
                if (line == null) break;
                if (writer != null) {
                    try { writer.write(line); writer.newLine(); } catch (Exception ex) { }
                }
            }
            if (writer != null) try { writer.flush(); writer.close(); } catch (Exception ex) { }
        } catch (Exception ex) { }
    }

    public static class AuditEntry {
        public final Instant timestamp;
        public final long threadId;
        public final String operationType;
        public final String userAction;
        public final long executionTimeMs;
        public final boolean success;
        public final String message;

        public AuditEntry(Instant timestamp, long threadId, String operationType, String userAction, long executionTimeMs, boolean success, String message) {
            this.timestamp = timestamp; this.threadId = threadId; this.operationType = operationType; this.userAction = userAction; this.executionTimeMs = executionTimeMs; this.success = success; this.message = message;
        }
    }

    public static class AuditStats {
        public final java.util.Map<Long, Integer> opsPerHour = new java.util.HashMap<>();
        public long totalExecTime = 0;
        public int countExec = 0;
        public double avgExecTime = 0;
    }
}
