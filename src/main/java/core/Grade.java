package core;

import java.time.LocalDate;

public class Grade {

    private static int gradeCounter = 5000;

    private int gradeId;
    private int studentId;
    private Subject subject;
    private double grade;
    private LocalDate date;

    public Grade(int studentId, Subject subject, double grade) {
        this.gradeId = gradeCounter++;
        this.studentId = studentId;
        this.subject = subject;
        this.grade = grade;
        this.date = LocalDate.now();
    }

    public int getGradeId() { return gradeId; }

    public int getStudentId() { return studentId; }

    public Subject getSubject() { return subject; }

    public double getGrade() { return grade; }

    public LocalDate getDate() { return date; }

    public String getLetterGrade() {
        if (grade >= 80) return "A";
        if (grade >= 70) return "B";
        if (grade >= 60) return "C";
        if (grade >= 50) return "D";
        return "F";
    }

    public void displayGradeDetails() {
        System.out.println("Grade ID: " + gradeId);
        System.out.println("Student ID: " + studentId);
        System.out.println("Subject: " + subject.getSubjectName());
        System.out.println("Numeric Grade: " + grade);
        System.out.println("Letter Grade: " + getLetterGrade());
        System.out.println("Date: " + date);
    }
}
