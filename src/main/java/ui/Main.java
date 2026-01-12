package ui;

import context.ApplicationContext;
import manager.CacheManager;

/**
 * Main entry point for the Student Grade Management System
 * Initializes all components and launches the application menu
 */
public class Main {
    
    public static void main(String[] args) throws Exception {
    System.out.println("╔═══════════════════════════════════════════════════╗");
    System.out.println("║  Student Grade Management System - Initializing   ║");
    System.out.println("╚═══════════════════════════════════════════════════╝\n");
    
    try {
        // Initialize the application
        System.out.println("→ Initializing components...");
        ApplicationContext context = initializeApplication();
        
        System.out.println("✓ All components initialized successfully\n");
        
        // Start the menu system
        System.out.println("→ Launching menu system...\n");
        Menu menu = new Menu(context);
        menu.start();
        
        // Cleanup on exit
        System.out.println("\n→ Performing cleanup...");
        shutdownApplication(context);
        
    } catch (Exception e) {
        System.err.println("\n✗ Fatal error during initialization:");
        System.err.println("  " + e.getMessage());
        e.printStackTrace();
        System.exit(1);
    }
}

/**
 * Initialize all application components
 */
private static ApplicationContext initializeApplication() throws Exception {
    ApplicationContext context = new ApplicationContext();
    
    // Initialize task scheduler
    context.setTaskScheduler(new TaskScheduler(context.getGradeManager(), context.getStudents()));
    context.getTaskScheduler().createDefaultTasks();
    
    // Initialize cache manager and register refreshers
    CacheManager cache = CacheManager.getInstance();
    
    // Refresher for student: keys like "student:123"
    cache.registerRefresher("student:", key -> {
        try {
            int id = Integer.parseInt(key.split(":")[1]);
            for (Student s : context.getStudents()) {
                if (s.getId() == id) return s;
            }
        } catch (Exception e) { }
        return null;
    });
    
    // Refresher for report: recreate lightweight report
    cache.registerRefresher("report:", key -> {
        try {
            int id = Integer.parseInt(key.split(":")[1]);
            return new CacheManager.CacheReport(id, 
                context.getGradeManager().getGradesForStudent(id), 
                context.getGradeManager().calculateOverallAverageSafe(id));
        } catch (Exception e) { }
        return null;
    });
    
    // Warm up cache: first 50 students if present
    cache.warmUpStudents(context.getStudents(), context.getGradeManager(), 50);
    
    // Add shutdown hook for graceful scheduler shutdown
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        if (context.getTaskScheduler() != null) {
            context.getTaskScheduler().shutdown();
        }
    }));
    
    return context;
}

/**
 * Shutdown and cleanup application resources
 */
private static void shutdownApplication(ApplicationContext context) {
    try {
        if (context.getTaskScheduler() != null) {
            context.getTaskScheduler().shutdown();
        }
        System.out.println("✓ Cleanup complete");
    } catch (Exception e) {
        System.err.println("Warning during shutdown: " + e.getMessage());
    }
}
}

