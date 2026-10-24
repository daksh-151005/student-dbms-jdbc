package com.student.dao;

import com.student.model.Student;
import com.student.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Student entities.
 * Implements pure JDBC CRUD operations (Create, Read, Update, Delete/Drop)
 * against Oracle Database using PreparedStatement and Connection.
 */
public class StudentDAO {

    /**
     * Initializes the database: creates the STUDENTS table if it does not already exist
     * and seeds initial sample records for demonstration.
     */
    public void initTable() {
        // Query to check if STUDENTS table exists in Oracle user schema
        String checkTableSql = "SELECT COUNT(*) FROM user_tables WHERE UPPER(table_name) = 'STUDENTS'";

        String createTableSql = "CREATE TABLE students ("
                + "  roll_no     VARCHAR2(20) NOT NULL PRIMARY KEY,"
                + "  name        VARCHAR2(100) NOT NULL,"
                + "  email       VARCHAR2(100) NOT NULL UNIQUE,"
                + "  department  VARCHAR2(50) NOT NULL,"
                + "  semester    NUMBER(2) DEFAULT 1,"
                + "  gpa         NUMBER(3,2) DEFAULT 0.00"
                + ")";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkTableSql);
             ResultSet rs = checkStmt.executeQuery()) {

            boolean tableExists = false;
            if (rs.next()) {
                tableExists = rs.getInt(1) > 0;
            }

            if (!tableExists) {
                System.out.println("[StudentDAO] Table STUDENTS does not exist. Creating table...");
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(createTableSql);
                    System.out.println("[StudentDAO] Table STUDENTS created successfully in Oracle Database.");
                }

                // Insert sample students
                addStudent(new Student("CS101", "Aarav Sharma", "aarav.sharma@college.edu", "Computer Science", 5, 8.85));
                addStudent(new Student("CS102", "Priya Patel", "priya.patel@college.edu", "Information Technology", 5, 9.20));
                addStudent(new Student("EC201", "Rohan Gupta", "rohan.gupta@college.edu", "Electronics", 3, 7.90));
                addStudent(new Student("ME301", "Ananya Verma", "ananya.verma@college.edu", "Mechanical", 7, 8.45));
                addStudent(new Student("CE401", "Vikram Malhotra", "vikram.m@college.edu", "Civil", 4, 7.60));
                System.out.println("[StudentDAO] Sample student records seeded successfully.");
            } else {
                System.out.println("[StudentDAO] Table STUDENTS already exists in Oracle Database.");
            }

        } catch (SQLException e) {
            System.err.println("[StudentDAO] Note on initTable: " + e.getMessage());
        }
    }

    /**
     * READ: Retrieves all student records from the database.
     * Uses: SELECT roll_no, name, email, department, semester, gpa FROM students ORDER BY roll_no
     */
    public List<Student> getAllStudents() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT roll_no, name, email, department, semester, gpa FROM students ORDER BY roll_no ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Student s = new Student();
                s.setRollNo(rs.getString("roll_no"));
                s.setName(rs.getString("name"));
                s.setEmail(rs.getString("email"));
                s.setDepartment(rs.getString("department"));
                s.setSemester(rs.getInt("semester"));
                s.setGpa(rs.getDouble("gpa"));
                students.add(s);
            }
        }
        return students;
    }

    /**
     * READ SINGLE: Retrieves a specific student by Roll Number.
     */
    public Student getStudentByRollNo(String rollNo) throws SQLException {
        String sql = "SELECT roll_no, name, email, department, semester, gpa FROM students WHERE roll_no = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, rollNo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Student s = new Student();
                    s.setRollNo(rs.getString("roll_no"));
                    s.setName(rs.getString("name"));
                    s.setEmail(rs.getString("email"));
                    s.setDepartment(rs.getString("department"));
                    s.setSemester(rs.getInt("semester"));
                    s.setGpa(rs.getDouble("gpa"));
                    return s;
                }
            }
        }
        return null;
    }

    /**
     * CREATE / ADD: Inserts a new student record into the database.
     * Uses: INSERT INTO students (roll_no, name, email, department, semester, gpa) VALUES (?, ?, ?, ?, ?, ?)
     */
    public boolean addStudent(Student student) throws SQLException {
        String sql = "INSERT INTO students (roll_no, name, email, department, semester, gpa) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, student.getRollNo().trim());
            pstmt.setString(2, student.getName().trim());
            pstmt.setString(3, student.getEmail().trim());
            pstmt.setString(4, student.getDepartment().trim());
            pstmt.setInt(5, student.getSemester());
            pstmt.setDouble(6, student.getGpa());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * UPDATE: Modifies existing student details based on Roll Number.
     * Uses: UPDATE students SET name = ?, email = ?, department = ?, semester = ?, gpa = ? WHERE roll_no = ?
     */
    public boolean updateStudent(Student student) throws SQLException {
        String sql = "UPDATE students SET name = ?, email = ?, department = ?, semester = ?, gpa = ? WHERE roll_no = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, student.getName().trim());
            pstmt.setString(2, student.getEmail().trim());
            pstmt.setString(3, student.getDepartment().trim());
            pstmt.setInt(4, student.getSemester());
            pstmt.setDouble(5, student.getGpa());
            pstmt.setString(6, student.getRollNo().trim());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * DELETE / DROP: Drops/deletes a student entry from the database by Roll Number.
     * Uses: DELETE FROM students WHERE roll_no = ?
     */
    public boolean dropStudent(String rollNo) throws SQLException {
        String sql = "DELETE FROM students WHERE roll_no = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, rollNo.trim());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
}
