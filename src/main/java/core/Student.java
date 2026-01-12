package core;

import context.IStudentInfo;
import context.IStudentAcademic;
import context.IStudentEligibility;
import context.ApplicationContext;

public abstract class Student implements IStudentInfo, IStudentAcademic, IStudentEligibility {
    public int id;
    public String name;
    public int age;
    public String email;
    public String phone;
    public String status = "Active";
    public double averageGrade;

    public Student(int id, String name, int age, String email, String phone) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.email = email;
        this.phone = phone;
        this.averageGrade = 0;
    }

    // IStudentInfo
    public int getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }

    // IStudentAcademic
    public double getAverageGrade() { return averageGrade; }
    public void setAverageGrade(double grade) { this.averageGrade = grade; }
    public int getEnrolledSubjects() { return ApplicationContext.getInstance().getGradeManager().getSubjectCountForStudent(this.id); }
    public double computeGPA() {
        int subjectCount = getEnrolledSubjects();
        if (subjectCount == 0) return 0.0;

        double totalGPA = 0.0;
        for (int grade : ApplicationContext.getInstance().getGradeManager().getGradesForStudent(this.id)) {
            totalGPA += gradeToGPA(grade);
        }
        return totalGPA / subjectCount;
    }
    public void updateAverageGPA() { this.averageGrade = computeGPA(); }
    double gradeToGPA(int grade) {
        if (grade >= 80) return 4.0;
        if (grade >= 70) return 3.0;
        if (grade >= 60) return 2.0;
        if (grade >= 50) return 1.0;
        return 0.0;
    }

    // IStudentEligibility
    public abstract String getType();
    public abstract int getPassingGrade();
    public boolean isEligibleForHonors() { return getAverageGrade() >= getPassingGrade(); }

    public abstract String getStatus();
}
