# Student Records Management Using JDBC
**Name:** ____________________ &nbsp;&nbsp;&nbsp;&nbsp; **Roll Number:** ____________________  
**Date:** ____________________ &nbsp;&nbsp;&nbsp;&nbsp; **Branch / Section:** _________________

---

## Aim and Essential Theory

### Aim
To add, update, delete (drop), and view student records using an HTML/CSS frontend, a Java backend, and JDBC connectivity to an Oracle Database.

### Essential Theory
The system demonstrates the classic 3-tier architecture for database-backed web applications:
1. **Frontend Tier (Client):** HTML5, CSS3, and JavaScript provide a responsive interface. A user submits student data via form controls or clicks action buttons (**Add**, **Update**, **Drop**, **Refresh**).
2. **Application Tier (Java Backend):** Built using Java SE's built-in `com.sun.net.httpserver.HttpServer`. It parses incoming HTTP requests, processes parameters, and interacts with the database via standard JDBC APIs.
3. **Database Tier (Oracle Database):** Stores the relational table `STUDENTS` containing fields `roll_no` (Primary Key), `name`, `email` (Unique), `department`, `semester`, and `gpa`.

#### JDBC Workflow and Core Components
* **`Class.forName("oracle.jdbc.OracleDriver")`**: Dynamically loads and registers the Oracle JDBC Type 4 Thin Driver.
* **`DriverManager.getConnection(url, user, password)`**: Establishes a TCP connection to the Oracle TNS listener (`jdbc:oracle:thin:@localhost:1521:xe`).
* **`PreparedStatement`**: Pre-compiled SQL statements containing parameter placeholders (`?`). This prevents SQL Injection vulnerabilities and optimizes execution performance:
  * **INSERT (Add):** `INSERT INTO students (roll_no, name, email, department, semester, gpa) VALUES (?, ?, ?, ?, ?, ?)`
  * **UPDATE (Modify):** `UPDATE students SET name = ?, email = ?, department = ?, semester = ?, gpa = ? WHERE roll_no = ?`
  * **DELETE (Drop):** `DELETE FROM students WHERE roll_no = ?`
  * **SELECT (Query):** `SELECT roll_no, name, email, department, semester, gpa FROM students ORDER BY roll_no ASC`
* **`executeUpdate()`**: Executes DML statements (`INSERT`, `UPDATE`, `DELETE`) and returns an integer specifying the number of rows affected.
* **`executeQuery()`**: Executes DQL statements (`SELECT`) and returns a `ResultSet` cursor that traverses matching records row by row.
* **`try-with-resources`**: Ensures database connections, prepared statements, and result sets are deterministically closed, avoiding connection leaks.

**Connection String:** `jdbc:oracle:thin:@localhost:1521:xe` | **User:** `system` | **Port:** `5000` (Web Server), `1521` (Oracle TNS)

---

## Run

1. **Install Prerequisites:** JDK 17 or newer, and Oracle Database (XE 11g / 19c / 21c).
2. **Execute Launcher:**
   * On **Windows:** Run `run.bat` (automatically compiles Java source, starts the server, and launches default browser).
   * On **Linux / macOS:** Run `sh run.sh`.
   * Manually:
     ```bash
     javac -cp "lib/ojdbc6.jar" -d bin src/com/student/model/*.java src/com/student/dao/*.java src/com/student/util/*.java src/com/student/server/*.java src/com/student/*.java
     java -cp "bin;lib/ojdbc6.jar" com.student.Main
     ```
3. Open `http://localhost:5000` in your web browser. Stop the server with `Ctrl + C`.

---

## Step-by-Step Test Execution and Verification

### Initial Database State
Fresh `SELECT` query executed through JDBC before user operations.

```
Database: jdbc:oracle:thin:@localhost:1521:xe
SELECT * FROM students ORDER BY roll_no;
```

| Roll No | Name | Email | Department | Semester | GPA |
| :--- | :--- | :--- | :--- | :--- | :--- |
| *(Table is currently empty)* | | | | | |

```
Total students: 0
```
*Figure 1. No students yet in the database.*

---

### Step 1: Add the First Student
Enter Roll No `CS101`, Name `Aarav Sharma`, Email `aarav.sharma@college.edu`, Department `Computer Science`, Semester `5`, and GPA `8.85`, then click **Add Student**. The student count changes from 0 to 1.

#### Operation Result:
```
ADD: Saved successfully.
SQL: INSERT INTO students (roll_no, name, email, department, semester, gpa) VALUES (?, ?, ?, ?, ?, ?)
Student Roll No: CS101
Rows affected: 1
```
*Figure 2. Add operation result and SQL executed through JDBC PreparedStatement.*

#### Fresh SELECT Result:
```
SELECT * FROM students ORDER BY roll_no;
```

| Roll No | Name | Email | Department | Semester | GPA |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **CS101** | Aarav Sharma | aarav.sharma@college.edu | Computer Science | 5 | 8.85 |

```
Total students: 1
```
*Figure 3. Fresh SELECT result. Student CS101 is stored in the Oracle database.*

---

### Step 2: Add a Second Student
Enter Roll No `IT102`, Name `Meera Patel`, Email `meera.patel@college.edu`, Department `Information Technology`, Semester `3`, and GPA `9.20`, then click **Add Student**. The student count changes from 1 to 2; student CS101 remains.

#### Operation Result:
```
ADD: Saved successfully.
SQL: INSERT INTO students (roll_no, name, email, department, semester, gpa) VALUES (?, ?, ?, ?, ?, ?)
Student Roll No: IT102
Rows affected: 1
```
*Figure 4. Add operation result for second student through JDBC.*

#### Fresh SELECT Result:
```
SELECT * FROM students ORDER BY roll_no;
```

| Roll No | Name | Email | Department | Semester | GPA |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **CS101** | Aarav Sharma | aarav.sharma@college.edu | Computer Science | 5 | 8.85 |
| **IT102** | Meera Patel | meera.patel@college.edu | Information Technology | 3 | 9.20 |

```
Total students: 2
```
*Figure 5. Fresh SELECT result. Both students are present in the database.*

---

### Step 3: Update an Existing Student
Select student `CS101`, enter updated Name `Aarav Sharma (Honors)`, Department `Computer Science`, Semester `6`, and GPA `9.10`, then click **Update**. Exactly one row changes and the student count remains 2.

#### Operation Result:
```
UPDATE: Saved successfully.
SQL: UPDATE students SET name = ?, email = ?, department = ?, semester = ?, gpa = ? WHERE roll_no = ?
Student Roll No: CS101
Rows affected: 1
```
*Figure 6. Update operation result and SQL executed through JDBC.*

#### Fresh SELECT Result:
```
SELECT * FROM students ORDER BY roll_no;
```

| Roll No | Name | Email | Department | Semester | GPA |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **CS101** | Aarav Sharma (Honors) | aarav.sharma@college.edu | Computer Science | 6 | 9.10 |
| **IT102** | Meera Patel | meera.patel@college.edu | Information Technology | 3 | 9.20 |

```
Total students: 2
```
*Figure 7. Fresh SELECT result. Roll No CS101 now has updated semester and GPA.*

---

### Step 4: Delete / Drop an Existing Student
Select student `IT102` and click **Drop Student**. Confirmation dialog verifies the Primary Key. One row is removed and the total count changes from 2 to 1.

#### Operation Result:
```
DELETE / DROP: Saved successfully.
SQL: DELETE FROM students WHERE roll_no = ?
Student Roll No: IT102
Rows affected: 1
```
*Figure 8. Drop operation result and SQL executed through JDBC.*

#### Fresh SELECT Result:
```
SELECT * FROM students ORDER BY roll_no;
```

| Roll No | Name | Email | Department | Semester | GPA |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **CS101** | Aarav Sharma (Honors) | aarav.sharma@college.edu | Computer Science | 6 | 9.10 |

```
Total students: 1
```
*Figure 9. Fresh SELECT result. IT102 is absent; CS101 remains unchanged.*

---

### Step 5: Refresh
Click **Refresh**. The saved student records remain unchanged because this control performs a read-only `SELECT` query.

#### Operation Result:
```
Ready. Data refreshed from database. No DML modification performed.
```
*Figure 10. Refresh reads database state without modifying data.*

---

### Step 6: Saved Database After Restart
After stopping Java (`Ctrl + C`) and restarting `run.bat`, student `CS101` still holds the updated information, and `IT102` remains absent. This verifies that all JDBC commits permanently persisted in Oracle Database storage.

```
SELECT * FROM students ORDER BY roll_no;
```

| Roll No | Name | Email | Department | Semester | GPA |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **CS101** | Aarav Sharma (Honors) | aarav.sharma@college.edu | Computer Science | 6 | 9.10 |

```
Total students: 1
```
*Figure 11. Verification confirms all modifications persisted after server restart.*

**Result:** Add, Update, and Delete/Drop operations successfully modified and persisted data in the database through JDBC PreparedStatement.

---

## Complete Source Code Listing

### 1. Database Connection Manager (`DBConnection.java`)
```java
package com.student.util;

import java.io.*;
import java.sql.*;
import java.util.Properties;

public class DBConnection {
    private static String driver = "oracle.jdbc.OracleDriver";
    private static String url = "jdbc:oracle:thin:@localhost:1521:xe";
    private static String user = "system";
    private static String password = "oracle";
    private static boolean driverLoaded = false;
    private static String lastError = null;

    static {
        loadProperties();
        loadDriver();
    }

    private static void loadProperties() {
        Properties props = new Properties();
        File propFile = new File("db.properties");
        try {
            if (propFile.exists()) {
                try (InputStream in = new FileInputStream(propFile)) {
                    props.load(in);
                }
            }
            if (props.getProperty("db.driver") != null) driver = props.getProperty("db.driver").trim();
            if (props.getProperty("db.url") != null) url = props.getProperty("db.url").trim();
            if (props.getProperty("db.user") != null) user = props.getProperty("db.user").trim();
            if (props.getProperty("db.password") != null) password = props.getProperty("db.password").trim();
        } catch (Exception e) {
            System.err.println("[DBConnection] Using defaults: " + e.getMessage());
        }
    }

    private static void loadDriver() {
        try {
            Class.forName(driver);
            driverLoaded = true;
        } catch (ClassNotFoundException e) {
            driverLoaded = false;
            lastError = "Oracle JDBC Driver not found in classpath.";
        }
    }

    public static Connection getConnection() throws SQLException {
        if (!driverLoaded) loadDriver();
        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            lastError = null;
            return conn;
        } catch (SQLException e) {
            lastError = e.getMessage();
            throw e;
        }
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            lastError = e.getMessage();
            return false;
        }
    }

    public static String getUrl() { return url; }
    public static String getLastError() { return lastError; }
}
```

---

### 2. Data Access Object (`StudentDAO.java`)
```java
package com.student.dao;

import com.student.model.Student;
import com.student.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    public void initTable() {
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

            if (rs.next() && rs.getInt(1) == 0) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(createTableSql);
                }
            }
        } catch (SQLException e) {
            System.err.println("[StudentDAO] initTable: " + e.getMessage());
        }
    }

    // READ ALL: SELECT
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

    // CREATE: INSERT
    public boolean addStudent(Student s) throws SQLException {
        String sql = "INSERT INTO students (roll_no, name, email, department, semester, gpa) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, s.getRollNo().trim());
            pstmt.setString(2, s.getName().trim());
            pstmt.setString(3, s.getEmail().trim());
            pstmt.setString(4, s.getDepartment().trim());
            pstmt.setInt(5, s.getSemester());
            pstmt.setDouble(6, s.getGpa());
            return pstmt.executeUpdate() > 0;
        }
    }

    // UPDATE: UPDATE
    public boolean updateStudent(Student s) throws SQLException {
        String sql = "UPDATE students SET name = ?, email = ?, department = ?, semester = ?, gpa = ? WHERE roll_no = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, s.getName().trim());
            pstmt.setString(2, s.getEmail().trim());
            pstmt.setString(3, s.getDepartment().trim());
            pstmt.setInt(4, s.getSemester());
            pstmt.setDouble(5, s.getGpa());
            pstmt.setString(6, s.getRollNo().trim());
            return pstmt.executeUpdate() > 0;
        }
    }

    // DELETE: DELETE / DROP
    public boolean dropStudent(String rollNo) throws SQLException {
        String sql = "DELETE FROM students WHERE roll_no = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, rollNo.trim());
            return pstmt.executeUpdate() > 0;
        }
    }
}
```

---

### 3. Application Main & Server Entry (`Main.java`)
```java
package com.student;

import com.student.server.StudentServer;

public class Main {
    public static void main(String[] args) {
        // Port 5000 avoids port 8080 Oracle XDB conflict
        int port = 5000;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        try {
            StudentServer server = new StudentServer(port);
            server.start();
        } catch (Exception e) {
            System.err.println("Fatal error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

---

### 4. Database Schema Script (`schema.sql`)
```sql
CREATE TABLE students (
    roll_no     VARCHAR2(20)    NOT NULL,
    name        VARCHAR2(100)   NOT NULL,
    email       VARCHAR2(100)   NOT NULL,
    department  VARCHAR2(50)    NOT NULL,
    semester    NUMBER(2)       DEFAULT 1,
    gpa         NUMBER(3,2)     DEFAULT 0.00,
    CONSTRAINT pk_students PRIMARY KEY (roll_no),
    CONSTRAINT uq_students_email UNIQUE (email),
    CONSTRAINT chk_students_gpa CHECK (gpa >= 0.00 AND gpa <= 10.00),
    CONSTRAINT chk_students_sem CHECK (semester >= 1 AND semester <= 8)
);

COMMIT;
```

---

### 5. Launchers

#### Windows Launcher (`run.bat`)
```bat
@echo off
setlocal
cd /d "%~dp0"

set JAVA_DIR=C:\Program Files\Java\jdk-26.0.1
if exist "%JAVA_DIR%\bin\java.exe" (
    set "JAVA_CMD=%JAVA_DIR%\bin\java.exe"
    set "JAVAC_CMD=%JAVA_DIR%\bin\javac.exe"
) else (
    set "JAVA_CMD=java"
    set "JAVAC_CMD=javac"
)

if not exist "bin\com\student\Main.class" (
    if not exist bin mkdir bin
    "%JAVAC_CMD%" -cp "lib/ojdbc6.jar" -d bin src/com/student/model/*.java src/com/student/dao/*.java src/com/student/util/*.java src/com/student/server/*.java src/com/student/*.java
    if %ERRORLEVEL% neq 0 exit /b 1
)

start "" http://localhost:5000
"%JAVA_CMD%" -cp "bin;lib/ojdbc6.jar" com.student.Main
pause
```

#### Linux & macOS Launcher (`run.sh`)
```bash
#!/bin/sh
set -eu
cd "$(dirname "$0")"
mkdir -p bin
javac -cp "lib/ojdbc6.jar" -d bin src/com/student/model/*.java src/com/student/dao/*.java src/com/student/util/*.java src/com/student/server/*.java src/com/student/*.java
java -cp "bin:lib/ojdbc6.jar" com.student.Main
```
