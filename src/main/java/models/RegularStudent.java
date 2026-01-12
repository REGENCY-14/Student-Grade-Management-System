package models;

import core.Student;

public class RegularStudent extends Student {

    public RegularStudent(int id, String name, int age, String email, String phone) {
        super(id, name, age, email, phone);
    }

    @Override
    public String getType() {
        return "Regular";
    }

    @Override
    public int getPassingGrade() {
        return (int) 50.0;
    }

    @Override
    public String getStatus() {
        return (averageGrade >= 50) ? "Passing" : "Failing";
    }




}
