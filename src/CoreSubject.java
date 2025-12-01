public class CoreSubject extends Subject {

    private final boolean mandatory = true;

    public CoreSubject(String name, String code) {
        super(name, code);
    }

    @Override
    public void displaySubjectDetails() {
        System.out.println("Core Subject: " + getSubjectName() +
                " (" + getSubjectCode() + ")");
    }

    @Override
    public String getSubjectType() {
        return "Core";
    }

    @Override
    public boolean isMandatory() {
        return mandatory;
    }
}
