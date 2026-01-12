package models;

import core.Student;
import exception.InvalidStudentDataException;

public class StudentFactory {

    public static Student createStudent(int type, int id, String name, int age, String email, String phone)
            throws InvalidStudentDataException {

        return switch (type) {
            case 1 -> new RegularStudent(id, name, age, email, phone);
            case 2 -> new HonorsStudent(id, name, age, email, phone);

            default -> throw new InvalidStudentDataException("Invalid student type choice: " + type);
        };
    }
}
