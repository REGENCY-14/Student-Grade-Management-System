# Sample Grade Import Data

This directory contains sample grade data in multiple formats for testing the bulk import functionality.

## Available Files

### 1. CSV Format
**File:** `csv/sample_grades_import.csv`
- Format: `StudentID,SubjectName,SubjectType,Grade`
- Records: 30 grade entries
- Students: 1001-1010
- Usage: Select option 6 (Bulk Import Grades) → Choose format 1 (CSV) → Enter filename: `sample_grades_import`

### 2. JSON Format
**File:** `json/sample_grades_import.json`
- Format: JSON array of grade objects with fields: studentId, subjectName, subjectType, grade, date
- Records: 30 grade entries
- Students: 1001-1010
- Usage: Select option 6 (Bulk Import Grades) → Choose format 2 (JSON) → Enter filename: `sample_grades_import`

### 3. Binary Format
**File:** `binary/sample_grades_import.bin`
- Format: Java serialized GradeData objects
- Records: 30 grade entries
- Students: 1001-1010
- Usage: Select option 6 (Bulk Import Grades) → Choose format 3 (Binary) → Enter filename: `sample_grades_import`

## Sample Data Content

The sample data includes grades for 10 students (IDs 1001-1010) across various subjects:

**Core Subjects:**
- Mathematics
- Physics
- Chemistry
- Biology
- English

**Elective Subjects:**
- History
- Music
- Computer Science
- Art
- Geography

Each student has 3 grade records with scores ranging from 76.5 to 97.5.

## Prerequisites

Before importing grades, ensure the students with IDs 1001-1010 exist in the system. You can:
1. Import students first using `sample_students.csv` (if available)
2. Or manually create students with these IDs

## Notes

- All three files contain identical data in different formats
- Files are stored in their respective format directories: `csv/`, `json/`, `binary/`
- Binary file was generated using the `GenerateSampleBinaryData.java` utility
- Date format in JSON: YYYY-MM-DD (2026-01-05)
