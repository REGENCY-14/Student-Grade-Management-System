public class HonorsStudent extends Student {

    public HonorsStudent(int id, String name, int age, String email, String phone) {
        super(id, name, age, email, phone);
    }

    @Override
    public String getType() {
        return "Honors";
    }

    @Override
    public double getPassingGrade() {
        return 60.0;
    }
}
