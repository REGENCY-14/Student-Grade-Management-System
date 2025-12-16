# Student Grade Management System

## Overview

This is a **console-based Student Grade Management System** built using Java 25. The system allows you to:

* Add and manage students (Regular & Honors).
* Record grades for different subjects (Core & Elective).
* View all students with details including average grades and status.
* View detailed grade reports for individual students.
* Calculate averages and display performance summaries.
* **Generate concurrent batch reports with progress tracking**
* **Monitor real-time statistics with auto-refresh dashboard**
* **Schedule automated tasks for system maintenance**
* **Search students using advanced regex patterns**
* Automatically generate unique IDs for students and grades.

---

## Advanced Features

### 1. Real-Time Statistics Dashboard (Option 11)
- **Live calculation** of student statistics every 5 seconds
- **Auto-refreshing display** every 1 second
- **Grade distribution** visualized with ASCII charts
- **Top 10 performers** leaderboard with rankings
- **Performance metrics**: Cache hit rates, calculation time, throughput
- **Interactive controls**: Pause/Resume, Refresh, Clear, Help
- Background daemon thread for non-blocking operation

### 2. Concurrent Batch Report Generation (Option 10)
- **Multi-threaded execution** with configurable thread pool (2-8 threads)
- **Live progress tracking** with visual progress bars
- **Individual report timing** for performance analysis
- **Performance comparison** between concurrent vs sequential generation
- **Thread-safe file writing** with unique timestamped filenames
- **Top 5 fastest reports** display
- **CSV export** of student details to ./reports/ directory

### 3. Scheduled Automated Tasks (Option 12)
- **ScheduledExecutorService** with 4-thread pool
- **Four default tasks**:
  1. Daily GPA Recalculation at 2:00 AM
  2. Hourly Statistics Cache Refresh
  3. Weekly Batch Reports (Monday 3:00 AM)
  4. Daily Database Backup at 1:00 AM
- **Execution logging** with timestamps and duration tracking
- **Task management**: Enable/Disable/Remove/Status
- **Simulated email notifications** on completion
- **Countdown display** to next execution
- Background execution without blocking main thread

### 4. Advanced Pattern Based Search (Option 13)
- **5 different search types**:
  1. **Email Domain Pattern** - Regex search on email domains
  2. **Phone Area Code Pattern** - Extract and match area codes
  3. **Student ID Pattern** - Wildcard/regex on student IDs
  4. **Name Pattern** - Full regex matching on names
  5. **Custom Regex Pattern** - Search any field with custom regex
- **Match highlighting** with ► and ◄ indicators
- **Search statistics** including pattern complexity hints
- **Bulk operations**: View full details, CSV export, aggregate stats
- **Error handling** with helpful regex error messages
- Linear O(n) search performance

---

## Features

1. **Add Student**

   * Add Regular or Honors students.
   * Automatically assigns a unique student ID.
   * Sets initial status to Active.

2. **View Students**

   * Displays all students in a table format.
   * Shows student ID, name, type, average grade, and status.

3. **Record Grade**

   * Validate student ID before recording a grade.
   * Select subject category: Core (Mathematics, English, Science) or Elective (Music, Art, Physical Education).
   * Enter a grade between 0-100.
   * Automatically generates unique grade ID and records date.
   * Displays confirmation with grade details.

4. **View Grade Report**

   * Displays all grades for a student in reverse chronological order.
   * Shows grade ID, date, subject, type, grade, and PASS/FAIL status.
   * Calculates and displays averages for core subjects, elective subjects, and overall.

5. **Search Students**

   * Comprehensive search by student ID or name.
   * Display matching results with full details.
   * Quick access to student records.

6. **Export Student Data**

   * Export student information to file.
   * Export grade reports in detailed format.
   * CSV export support.

7. **Bulk Import**

   * Import grades from CSV file.
   * Batch process multiple grades efficiently.
   * Validation and error reporting.

8. **Real-Time Dashboard**

   * Live statistics monitoring.
   * Auto-refresh with interactive controls.
   * Grade distribution visualization.

9. **Concurrent Reports**

   * Multi-threaded batch report generation.
   * Progress tracking and performance metrics.
   * Configurable thread pool.

10. **Scheduled Tasks**

    * Automated background task execution.
    * Task scheduling and management.
    * Execution logging and notifications.

11. **Pattern Search**

    * Advanced regex-based student search.
    * Multiple search pattern types.
    * Match highlighting and statistics.
    * Bulk operations on results.

12. **Menu Navigation**

    * Hierarchical menu system with 15 options.
    * Clear section organization.
    * Interactive prompts and guidance.

---

## Project Structure

### Classes and Interfaces

* **Student (Abstract Class)**: Base class for students with fields like ID, name, email, phone, type, status, and average grade.
* **RegularStudent & HonorsStudent**: Extend `Student`, represent different student types with different passing grade thresholds.
* **Subject (Abstract Class)**: Base class for subjects with `subjectName` and `subjectCode`. Abstract methods: `displaySubjectDetails()`, `getSubjectType()`.
* **CoreSubject & ElectiveSubject**: Extend `Subject`, define mandatory and optional subjects.
* **Grade**: Represents a student's grade for a subject, with unique grade ID, student ID, subject, grade value, and date.
* **GradeManager**: Manages all grades, calculates averages, determines PASS/FAIL, and displays grade reports.
* **StudentManagement**: Manages student array, adding, searching, viewing all students, and calculating class average.
* **Menu**: Handles the console-based user interface and navigation with 15 menu options.
* **StatisticsDashboard**: Real-time statistics display with auto-refresh (1 second), color-coded output, and interactive controls.
* **StatsCalculator**: Background daemon thread calculating statistics every 5 seconds with ConcurrentHashMap for thread safety.
* **ConcurrentReportGenerator**: Multi-threaded batch report generation with progress tracking, configurable thread pool (2-8 threads).
* **ScheduledTask**: Task configuration with type/schedule enums, execution time calculations, and statistics tracking.
* **TaskScheduler**: Manages scheduled task execution with ScheduledExecutorService, 4 default tasks, logging, and notifications.
* **RegexSearchEngine**: Advanced pattern-based search engine with match highlighting, statistics, and bulk operations.
* **FileFormatManager** (Optional): Handles CSV import/export and file operations.

---## Object-Oriented Principles Used

* **Abstraction**: `Student` and `Subject` abstract classes.
* **Inheritance**: `RegularStudent` & `HonorsStudent` extend `Student`; `CoreSubject` & `ElectiveSubject` extend `Subject`.
* **Polymorphism**: Treating `CoreSubject` and `ElectiveSubject` as `Subject`; using `Student` references for both Regular and Honors students.
* **Encapsulation**: Private fields with getters and setters in `Grade` and `Student`.
* **Separation of Concerns**: `Menu` handles UI, `StudentManagement` manages students, `GradeManager` manages grades.

---

## Usage

1. Run the `Menu` class.

2. Follow the on-screen menu with options:

   **STUDENT MANAGEMENT**
   * Enter 1 to add a student.
   * Enter 2 to view all students.
   * Enter 3 to record a grade.

   **FILE OPERATIONS**
   * Enter 5 to export student data.
   * Enter 7 to bulk import grades.
   * Enter 10 to generate concurrent batch reports.

   **ANALYTICS AND REPORTING**
   * Enter 4 to view a grade report.
   * Enter 6 to calculate student GPA.
   * Enter 8 to view grades statistics.
   * Enter 11 to launch real-time statistics dashboard.

   **SEARCH AND QUERY**
   * Enter 9 to search students.

   **ADVANCED FEATURES**
   * Enter 12 to manage scheduled automated tasks.
   * Enter 13 to use advanced pattern-based search.
   * Enter 15 to exit.

3. Follow prompts to enter student or grade information.

4. For advanced features:
   - Real-time Dashboard: Auto-refreshes every 1 second with live stats
   - Concurrent Reports: Configure thread count (2-8) for batch generation
   - Scheduled Tasks: View countdown to next execution, manage task status
   - Pattern Search: Use regex patterns to find students by email, phone, ID, or name

---

## Requirements

* Java JDK 25 or above.
* Maven 3.6+ for building.
* Console/terminal to run the program.
* 50+ MB available memory for concurrent operations.

---

## Building and Running

### Build
```bash
mvn clean compile
mvn package -DskipTests
```

### Run
```bash
java -cp target/student-management-system-1.0-SNAPSHOT.jar Main
```

Or run directly from IDE by executing the Main.java class.

---

## Notes

* The system supports up to 50 students and 200 grades.
* Students have unique IDs starting from 1000.
* Grades are validated to be within 0-100.
* Grade reports calculate and display PASS/FAIL status based on thresholds:

  * Regular: ≥50 to pass.
  * Honors: ≥60 to pass.

* **Concurrent Features**:
  - Real-time Dashboard runs in background daemon thread
  - Batch reports use ExecutorService thread pool
  - Scheduled tasks execute on ScheduledExecutorService
  - All background operations are non-blocking

* **Thread Safety**:
  - ConcurrentHashMap for statistics caching
  - Synchronized blocks for file operations
  - Atomic variables for counters
  - Proper resource cleanup and shutdown hooks

---

This system demonstrates a full application of Java OOP principles with a practical student and grade management workflow.
