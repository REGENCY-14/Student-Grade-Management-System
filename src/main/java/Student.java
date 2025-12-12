public abstract class Student implements IStudentInfo, IStudentAcademic, IStudentEligibility {
    protected int id;
    protected String name;
    protected int age;
    protected String email;
    protected String phone;
    protected String status = "Active";
    protected double averageGrade;

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
    public int getEnrolledSubjects() { return Menu.gradeManager.getSubjectCountForStudent(this.id); }
    public double computeGPA() {
        int subjectCount = getEnrolledSubjects();
        if (subjectCount == 0) return 0.0;

        double totalGPA = 0.0;
        for (int grade : Menu.gradeManager.getGradesForStudent(this.id)) {
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
