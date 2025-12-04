# Student Grade Management System

## Overview

This is a **console-based Student Grade Management System** built using Java. The system allows you to:

* Add and manage students (Regular & Honors).
* Record grades for different subjects (Core & Elective).
* View all students with details including average grades and status.
* View detailed grade reports for individual students.
* Calculate averages and display performance summaries.
* Automatically generate unique IDs for students and grades.

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

5. **Menu Navigation**

   * Simple console menu to navigate all features.
   * Options: Add Student, View Students, Record Grade, View Grade Report, Exit.

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
* **Menu**: Handles the console-based user interface and navigation.

---

## Object-Oriented Principles Used

* **Abstraction**: `Student` and `Subject` abstract classes.
* **Inheritance**: `RegularStudent` & `HonorsStudent` extend `Student`; `CoreSubject` & `ElectiveSubject` extend `Subject`.
* **Polymorphism**: Treating `CoreSubject` and `ElectiveSubject` as `Subject`; using `Student` references for both Regular and Honors students.
* **Encapsulation**: Private fields with getters and setters in `Grade` and `Student`.
* **Separation of Concerns**: `Menu` handles UI, `StudentManagement` manages students, `GradeManager` manages grades.

---

## Usage

1. Run the `Menu` class.

2. Follow the on-screen menu:

   * Enter 1 to add a student.
   * Enter 2 to view all students.
   * Enter 3 to record a grade.
   * Enter 4 to view a grade report.
   * Enter 5 to exit.

3. Follow prompts to enter student or grade information.

4. View grades and averages as needed.

---

## Requirements

* Java JDK 8 or above.
* Console/terminal to run the program.

---

## Notes

* The system supports up to 50 students and 200 grades.
* Students have unique IDs starting from 1000.
* Grades are validated to be within 0-100.
* Grade reports calculate and display PASS/FAIL status based on thresholds:

  * Regular: ≥50 to pass.
  * Honors: ≥60 to pass.

---

This system demonstrates a full application of Java OOP principles with a practical student and grade management workflow.
