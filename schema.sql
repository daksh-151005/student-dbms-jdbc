-- ===================================================================
-- DBMS Class Assignment: Student Database Schema (Oracle SQL)
-- ===================================================================

-- Optional: Drop existing table if you want a clean reset
-- BEGIN
--    EXECUTE IMMEDIATE 'DROP TABLE students';
-- EXCEPTION
--    WHEN OTHERS THEN
--       IF SQLCODE != -942 THEN
--          RAISE;
--       END IF;
-- END;
-- /

-- Create the STUDENTS table
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

-- Insert Initial Sample Records
INSERT INTO students (roll_no, name, email, department, semester, gpa) 
VALUES ('CS101', 'Aarav Sharma', 'aarav.sharma@college.edu', 'Computer Science', 5, 8.85);

INSERT INTO students (roll_no, name, email, department, semester, gpa) 
VALUES ('CS102', 'Priya Patel', 'priya.patel@college.edu', 'Information Technology', 5, 9.20);

INSERT INTO students (roll_no, name, email, department, semester, gpa) 
VALUES ('EC201', 'Rohan Gupta', 'rohan.gupta@college.edu', 'Electronics', 3, 7.90);

INSERT INTO students (roll_no, name, email, department, semester, gpa) 
VALUES ('ME301', 'Ananya Verma', 'ananya.verma@college.edu', 'Mechanical', 7, 8.45);

INSERT INTO students (roll_no, name, email, department, semester, gpa) 
VALUES ('CE401', 'Vikram Malhotra', 'vikram.m@college.edu', 'Civil', 4, 7.60);

-- Commit transaction in Oracle
COMMIT;

-- Verify records
SELECT * FROM students ORDER BY roll_no;
