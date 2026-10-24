package com.student.model;

/**
 * Model class representing a Student entity in the database.
 * Matches the STUDENTS table in Oracle Database.
 */
public class Student {
    private String rollNo;
    private String name;
    private String email;
    private String department;
    private int semester;
    private double gpa;

    public Student() {
    }

    public Student(String rollNo, String name, String email, String department, int semester, double gpa) {
        this.rollNo = rollNo;
        this.name = name;
        this.email = email;
        this.department = department;
        this.semester = semester;
        this.gpa = gpa;
    }

    // Getters and Setters
    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public double getGpa() {
        return gpa;
    }

    public void setGpa(double gpa) {
        this.gpa = gpa;
    }

    /**
     * Converts this Student object into a valid JSON string without external libraries.
     */
    public String toJson() {
        return "{"
                + "\"rollNo\":\"" + escapeJson(rollNo) + "\","
                + "\"name\":\"" + escapeJson(name) + "\","
                + "\"email\":\"" + escapeJson(email) + "\","
                + "\"department\":\"" + escapeJson(department) + "\","
                + "\"semester\":" + semester + ","
                + "\"gpa\":" + String.format(java.util.Locale.US, "%.2f", gpa)
                + "}";
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    @Override
    public String toString() {
        return "Student{" +
                "rollNo='" + rollNo + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", department='" + department + '\'' +
                ", semester=" + semester +
                ", gpa=" + gpa +
                '}';
    }
}
