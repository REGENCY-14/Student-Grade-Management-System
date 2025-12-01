public class Student {
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
    }

    public String getType() {
        return "Student";
    }

    public double getPassingGrade() {
        return 0;
    }

    public double getAverageGrade() {
        return averageGrade;
    }

    public void setAverageGrade(double grade) {
        this.averageGrade = grade;
    }
}
