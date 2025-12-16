import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * Thread-safe generic cache with LRU eviction and background refresh.
 */
public class CacheManager {

    private static final CacheManager INSTANCE = new CacheManager();

    public static CacheManager getInstance() { return INSTANCE; }

    public static final int DEFAULT_MAX_ENTRIES = 150;

    private final ConcurrentHashMap<String, CacheEntry> map = new ConcurrentHashMap<>();
    private final ConcurrentLinkedDeque<String> accessOrder = new ConcurrentLinkedDeque<>();
    private final int maxEntries;

    // Stats
    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong misses = new AtomicLong();
    private final AtomicLong totalHitTimeNs = new AtomicLong();
    private final AtomicLong totalMissTimeNs = new AtomicLong();
    private final AtomicLong evictionCount = new AtomicLong();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "cache-refresher");
        t.setDaemon(true);
        return t;
    });

    // registered refreshers by key prefix
    private final ConcurrentHashMap<String, Function<String, Object>> refreshers = new ConcurrentHashMap<>();

    private CacheManager() { this(DEFAULT_MAX_ENTRIES); }

    private CacheManager(int maxEntries) {
        this.maxEntries = maxEntries;
        // refresh stale entries every 5 minutes
        scheduler.scheduleAtFixedRate(this::refreshStaleEntries, 5, 5, TimeUnit.MINUTES);
    }

    private static class CacheEntry implements Serializable {
        Object value;
        long createdAt;
        long lastAccessAt;
        long accessCount;

        CacheEntry(Object value) {
            this.value = value;
            long now = Instant.now().toEpochMilli();
            this.createdAt = now;
            this.lastAccessAt = now;
            this.accessCount = 0;
        }
    }

    public Object get(String key) {
        long start = System.nanoTime();
        CacheEntry e = map.get(key);
        if (e == null) {
            misses.incrementAndGet();
            totalMissTimeNs.addAndGet(System.nanoTime() - start);
            return null;
        }
        // update access metadata
        e.lastAccessAt = Instant.now().toEpochMilli();
        e.accessCount++;
        // move to tail
        accessOrder.remove(key);
        accessOrder.addLast(key);
        hits.incrementAndGet();
        totalHitTimeNs.addAndGet(System.nanoTime() - start);
        return e.value;
    }

    public void put(String key, Object value) {
        CacheEntry entry = new CacheEntry(value);
        map.put(key, entry);
        accessOrder.remove(key);
        accessOrder.addLast(key);
        // evict if needed
        while (map.size() > maxEntries) {
            String oldest = accessOrder.pollFirst();
            if (oldest == null) break;
            map.remove(oldest);
            evictionCount.incrementAndGet();
        }
    }

    public Object getOrLoad(String key, Function<String, Object> loader) {
        Object v = get(key);
        if (v != null) return v;
        long start = System.nanoTime();
        Object loaded = loader.apply(key);
        totalMissTimeNs.addAndGet(System.nanoTime() - start);
        if (loaded != null) put(key, loaded);
        return loaded;
    }

    public void invalidate(String key) {
        map.remove(key);
        accessOrder.remove(key);
    }

    public void invalidateByPrefix(String prefix) {
        Set<String> keys = map.keySet();
        for (String k : keys) {
            if (k.startsWith(prefix)) {
                invalidate(k);
            }
        }
    }

    public void clear() {
        map.clear();
        accessOrder.clear();
    }

    public void registerRefresher(String keyPrefix, Function<String, Object> refresher) {
        refreshers.put(keyPrefix, refresher);
    }

    private void refreshStaleEntries() {
        long now = Instant.now().toEpochMilli();
        long staleThreshold = TimeUnit.MINUTES.toMillis(10); // entries older than 10 minutes
        try {
            for (Map.Entry<String, CacheEntry> e : map.entrySet()) {
                String key = e.getKey();
                CacheEntry entry = e.getValue();
                if (now - entry.lastAccessAt > staleThreshold) {
                    // find a refresher by prefix
                    for (Map.Entry<String, Function<String, Object>> r : refreshers.entrySet()) {
                        String prefix = r.getKey();
                        if (key.startsWith(prefix)) {
                            try {
                                Object refreshed = r.getValue().apply(key);
                                if (refreshed != null) {
                                    put(key, refreshed);
                                }
                            } catch (Exception ex) {
                                // ignore individual refresh failures
                            }
                            break;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            // swallow to keep scheduler alive
        }
    }

    // Stats display
    public void displayStats() {
        long h = hits.get();
        long m = misses.get();
        long total = h + m;
        double hitRate = total == 0 ? 0.0 : (h * 100.0 / total);
        double missRate = total == 0 ? 0.0 : (m * 100.0 / total);
        double avgHitNs = h == 0 ? 0.0 : (totalHitTimeNs.doubleValue() / h);
        double avgMissNs = m == 0 ? 0.0 : (totalMissTimeNs.doubleValue() / m);

        System.out.println("\n--- CACHE STATISTICS ---");
        System.out.println("Hit Rate: " + String.format("%.2f%%", hitRate));
        System.out.println("Miss Rate: " + String.format("%.2f%%", missRate));
        System.out.println("Average Hit Time: " + String.format("%.2f ms", avgHitNs / 1_000_000.0));
        System.out.println("Average Miss Time: " + String.format("%.2f ms", avgMissNs / 1_000_000.0));
        System.out.println("Total Entries: " + map.size());
        System.out.println("Eviction Count: " + evictionCount.get());
        System.out.println("Approx Memory Usage: " + approximateMemoryUsage() + " bytes");
    }

    private long approximateMemoryUsage() {
        long total = 0;
        for (Map.Entry<String, CacheEntry> e : map.entrySet()) {
            Object v = e.getValue().value;
            if (v instanceof Serializable) {
                try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                    oos.writeObject(v);
                    oos.flush();
                    total += bos.size();
                } catch (Exception ex) {
                    total += 1024; // fallback 1KB
                }
            } else {
                total += 1024; // fallback
            }
        }
        return total;
    }

    public void displayContents() {
        System.out.println("\n--- CACHE CONTENTS ---");
        for (Map.Entry<String, CacheEntry> e : map.entrySet()) {
            String key = e.getKey();
            CacheEntry ce = e.getValue();
            System.out.println(String.format("Key: %s | LastAccess: %d | Accesses: %d", key, ce.lastAccessAt, ce.accessCount));
        }
    }

    /**
     * Warm the cache with provided student ids and their generated reports.
     */
    public void warmUpStudents(List<Student> students, GradeManager gradeManager, int limit) {
        int count = 0;
        for (Student s : students) {
            if (count++ >= limit) break;
            String sk = "student:" + s.getId();
            put(sk, s);
            String rk = "report:" + s.getId();
            // create a lightweight report object
            CacheReport rep = new CacheReport(s.getId(), gradeManager.getGradesForStudent(s.getId()), gradeManager.calculateOverallAverageSafe(s.getId()));
            put(rk, rep);
        }
    }

    // Simple report POJO stored in cache
    public static class CacheReport implements Serializable {
        public int studentId;
        public List<Integer> grades;
        public double overallAvg;

        public CacheReport(int studentId, List<Integer> grades, double overallAvg) {
            this.studentId = studentId;
            this.grades = new ArrayList<>(grades);
            this.overallAvg = overallAvg;
        }
    }
}
