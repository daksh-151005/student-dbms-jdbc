package com.student;

import com.student.server.StudentServer;

public class Main {
    public static void main(String[] args) {
        // Use port 5000 (port 8080 is reserved by Oracle Database XDB/APEX service)
        int port = 5000;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        System.out.println("=============================================================");
        System.out.println("   STUDENT DATABASE MANAGEMENT SYSTEM (DBMS ASSIGNMENT)     ");
        System.out.println("   Implementation: Java JDBC + Oracle Database             ");
        System.out.println("=============================================================");

        try {
            StudentServer server = new StudentServer(port);
            server.start();
        } catch (Exception e) {
            System.err.println("Fatal error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
