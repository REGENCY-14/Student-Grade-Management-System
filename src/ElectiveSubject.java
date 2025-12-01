public class ElectiveSubject extends Subject {

    private final boolean mandatory = false;

    public ElectiveSubject(String name, String code) {
        super(name, code);
    }

    @Override
    public void displaySubjectDetails() {
        System.out.println("Elective Subject: " + getSubjectName() +
                " (" + getSubjectCode() + ")");
    }

    @Override
    public String getSubjectType() {
        return "Elective";
    }

    @Override
    public boolean isMandatory() {
        return mandatory;
    }
}
