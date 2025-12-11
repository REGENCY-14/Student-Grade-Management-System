public class HonorsStudent extends Student {

    public HonorsStudent(int id, String name, int age, String email, String phone) {
        super(id, name, age, email, phone);
    }

    @Override
    public String getType() {
        return "Honors";
    }

    @Override
    public int getPassingGrade() {
        return (int) 60.0;
    }

    @Override
    public String getStatus() {
        return (averageGrade >= 60) ? "Passing  " : "Failing";
    }
}
