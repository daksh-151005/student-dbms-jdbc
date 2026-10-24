# Student Database Management System (DBMS Assignment)
### Java JDBC Implementation with Oracle Database

A clean, responsive, and easy-to-understand web application for managing student database records. Built as an assignment submission for **Database Management Systems (DBMS)** class, demonstrating **JDBC (Java Database Connectivity)** fundamentals using Oracle Database.

---

## 📋 Features Required & Implemented

1. **Adding Entries (INSERT)**:
   - Insert new student records (Roll Number, Full Name, Email, Department, Semester, and GPA).
   - Validates required fields, semester bounds (1-8), and GPA (0.00 - 10.00).
   - Executes parameterized `INSERT` query via `PreparedStatement`.

2. **Dropping Entries (DELETE)**:
   - Remove/drop student records from database by Primary Key (`roll_no`).
   - Includes safety confirmation modal showing the exact SQL statement (`DELETE FROM students WHERE roll_no = ?`).

3. **Updating Entries (UPDATE)**:
   - Edit existing student information (Name, Email, Department, Semester, GPA) while keeping the Primary Key fixed.
   - Executes parameterized `UPDATE` query via `PreparedStatement`.

4. **Viewing / Filtering (SELECT)**:
   - Display enrolled students in a structured, clean data table.
   - Instant live search by Roll Number, Name, or Email.
   - Department filter dropdown.
   - Summary statistics cards: Total Students, Average GPA, and Department Count.

---

## 🛠️ Technology Stack

| Layer | Technology | Purpose |
| :--- | :--- | :--- |
| **Database** | Oracle Database (XE 11g / 19c / 21c) | Relational Database storage & constraints |
| **Driver** | Oracle JDBC Thin Driver (`ojdbc6.jar`) | Type 4 Pure Java network protocol driver |
| **Backend** | Java SE (JDK 8 / 17 / 21 / 26) | Business logic & JDBC query execution |
| **Web Server** | Java Built-in `HttpServer` | Lightweight, zero-dependency HTTP server (port 5000) |
| **Frontend** | HTML5, CSS3, Modern JavaScript | Clean, responsive student management dashboard |

> **Zero Complex Frameworks**: Built using pure Java SE standard libraries + Oracle JDBC driver. No Tomcat, Maven, or heavy Spring setup required.

---

## 📁 Project Directory Structure

```
student-management-jdbc/
├── bin/                          # Compiled Java .class files
├── lib/
│   └── ojdbc6.jar                # Oracle JDBC Driver JAR (Type 4)
├── src/
│   └── com/
│       └── student/
│           ├── Main.java         # Application entry point
│           ├── model/
│           │   └── Student.java  # Entity model class (POJO)
│           ├── dao/
│           │   └── StudentDAO.java # Core JDBC CRUD implementation
│           ├── util/
│           │   └── DBConnection.java # Database connection manager
│           └── server/
│               └── StudentServer.java # Built-in web server & REST handlers
├── web/
│   ├── index.html                # Frontend dashboard & modals
│   ├── style.css                 # Clean academic styling
│   └── app.js                    # AJAX calls to JDBC backend
├── db.properties                 # Oracle connection configuration
├── schema.sql                    # SQL script to create table & seed data
├── compile.bat                   # 1-Click build script (Windows)
├── run.bat                       # 1-Click start script (opens browser)
└── README.md                     # Documentation & Viva Q&A
```

---

## 🚀 Quick Start Guide (How to Run)

### Option 1: 1-Click Runner (Easiest)
Simply double-click:
```bat
run.bat
```
This script automatically compiles the source code, starts the Java web server, and opens `http://localhost:5000/` in your default browser.

### Option 2: Running via Command Prompt / PowerShell

1. Open PowerShell or Command Prompt in the project folder:
   ```powershell
   cd "C:\Users\Daksh Pratap Singh\.gemini\antigravity\scratch\student-management-jdbc"
   ```

2. Compile:
   ```powershell
   javac -cp "lib/ojdbc6.jar" -d bin src/com/student/model/*.java src/com/student/dao/*.java src/com/student/util/*.java src/com/student/server/*.java src/com/student/*.java
   ```

3. Run:
   ```powershell
   java -cp "bin;lib/ojdbc6.jar" com.student.Main
   ```

4. Open your browser and navigate to:
   ```
   http://localhost:5000
   ```

---

## 🗄️ Database Configuration & Oracle Setup

The file `db.properties` in the project root holds your connection settings:

```properties
db.driver=oracle.jdbc.OracleDriver
db.url=jdbc:oracle:thin:@localhost:1521:xe
db.user=system
db.password=oracle
```

### Starting Oracle Service (If not already running):
If the web portal shows **"Oracle DB Offline"**:
1. Open Command Prompt as **Administrator**.
2. Run:
   ```cmd
   net start OracleServiceXE
   net start OracleXETNSListener
   ```
3. Refresh the website or click the **Refresh** button.

### Running the SQL Schema Manually (Optional):
The application automatically creates the `STUDENTS` table if it does not exist. However, if your professor asks you to run the SQL script in SQL*Plus:

```bash
sqlplus system/oracle@xe
SQL> @schema.sql
```

---

## 🔍 How JDBC is Implemented (Code Walkthrough)

### 1. Database Connection (`DBConnection.java`)
Uses `DriverManager.getConnection()` with Type 4 Thin URL:
```java
Class.forName("oracle.jdbc.OracleDriver");
Connection conn = DriverManager.getConnection(
    "jdbc:oracle:thin:@localhost:1521:xe", "system", "oracle"
);
```

### 2. Adding a Student (`INSERT`)
Uses `PreparedStatement` with parameterized placeholders `?`:
```java
String sql = "INSERT INTO students (roll_no, name, email, department, semester, gpa) VALUES (?, ?, ?, ?, ?, ?)";
try (Connection conn = DBConnection.getConnection();
     PreparedStatement pstmt = conn.prepareStatement(sql)) {
    pstmt.setString(1, student.getRollNo());
    pstmt.setString(2, student.getName());
    pstmt.setString(3, student.getEmail());
    pstmt.setString(4, student.getDepartment());
    pstmt.setInt(5, student.getSemester());
    pstmt.setDouble(6, student.getGpa());
    int rows = pstmt.executeUpdate();
}
```

### 3. Dropping a Student (`DELETE`)
```java
String sql = "DELETE FROM students WHERE roll_no = ?";
try (Connection conn = DBConnection.getConnection();
     PreparedStatement pstmt = conn.prepareStatement(sql)) {
    pstmt.setString(1, rollNo);
    int rows = pstmt.executeUpdate();
}
```

### 4. Updating a Student (`UPDATE`)
```java
String sql = "UPDATE students SET name = ?, email = ?, department = ?, semester = ?, gpa = ? WHERE roll_no = ?";
try (Connection conn = DBConnection.getConnection();
     PreparedStatement pstmt = conn.prepareStatement(sql)) {
    pstmt.setString(1, student.getName());
    pstmt.setString(2, student.getEmail());
    pstmt.setString(3, student.getDepartment());
    pstmt.setInt(4, student.getSemester());
    pstmt.setDouble(5, student.getGpa());
    pstmt.setString(6, student.getRollNo());
    int rows = pstmt.executeUpdate();
}
```

### 5. Fetching All Students (`SELECT`)
```java
String sql = "SELECT roll_no, name, email, department, semester, gpa FROM students ORDER BY roll_no ASC";
try (Connection conn = DBConnection.getConnection();
     PreparedStatement pstmt = conn.prepareStatement(sql);
     ResultSet rs = pstmt.executeQuery()) {
    while (rs.next()) {
        String rollNo = rs.getString("roll_no");
        String name = rs.getString("name");
        // ... construct Student object and add to list
    }
}
```

---

## 🎓 DBMS Lab / Viva Exam Q&A Guide

Be prepared to answer these common questions during your lab evaluation:

**Q1: What is JDBC and what role does it play?**  
> *Answer:* JDBC (Java Database Connectivity) is a standard Java API that provides independent connectivity between Java applications and relational databases. It enables sending SQL statements and retrieving query results.

**Q2: What type of JDBC Driver is used here?**  
> *Answer:* We are using **Type 4 (Direct-to-Database Pure Java Driver)**, specifically the Oracle Thin Driver (`oracle.jdbc.OracleDriver`). It converts JDBC calls directly into vendor-specific network protocol (TNS for Oracle) without needing client-side Oracle software installations or native code.

**Q3: What is the difference between `Statement` and `PreparedStatement`?**  
> *Answer:* 
> 1. `PreparedStatement` is pre-compiled by the database engine, offering faster performance when executed repeatedly.
> 2. `PreparedStatement` accepts parameterized inputs (`?`), preventing **SQL Injection attacks**.
> 3. Standard `Statement` executes plain SQL text strings and has to be compiled every time.

**Q4: What is the difference between `executeQuery()` and `executeUpdate()`?**  
> *Answer:*
> - `executeQuery()` is used for `SELECT` statements and returns a `ResultSet` object containing rows of data.
> - `executeUpdate()` is used for DDL/DML statements (`INSERT`, `UPDATE`, `DELETE`) and returns an `int` representing the number of rows affected.

**Q5: Why is `try-with-resources` used in our JDBC code?**  
> *Answer:* `try-with-resources` automatically closes `Connection`, `PreparedStatement`, and `ResultSet` objects once execution finishes (even if an exception occurs), preventing database connection leaks and socket exhaustion.
