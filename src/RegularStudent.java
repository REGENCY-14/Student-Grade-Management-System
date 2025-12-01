public class RegularStudent extends Student {

    public RegularStudent(int id, String name, int age, String email, String phone) {
        super(id, name, age, email, phone);
    }

    @Override
    public String getType() {
        return "Regular";
    }

    @Override
    public double getPassingGrade() {
        return 50.0;
    }
}
