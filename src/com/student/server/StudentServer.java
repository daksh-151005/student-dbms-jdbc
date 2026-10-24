package com.student.server;

import com.student.dao.StudentDAO;
import com.student.model.Student;
import com.student.util.DBConnection;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lightweight HTTP Web Server using Java built-in HttpServer.
 * Serves the frontend web interface and provides REST API endpoints
 * for Student CRUD operations backed by JDBC.
 */
public class StudentServer {

    private int port;
    private final StudentDAO dao;
    private final File webDir;
    
    // In-memory fallback repository used if Oracle Database is temporarily offline
    private final Map<String, Student> fallbackStudents = new ConcurrentHashMap<>();
    private boolean isOracleAvailable = false;

    public StudentServer(int port) {
        this.port = port;
        this.dao = new StudentDAO();
        File dir = new File("web");
        if (!dir.exists() || !dir.isDirectory()) {
            dir = new File("docs");
        }
        this.webDir = dir;
        initFallbackData();
    }

    private void initFallbackData() {
        fallbackStudents.put("CS101", new Student("CS101", "Aarav Sharma", "aarav.sharma@college.edu", "Computer Science", 5, 8.85));
        fallbackStudents.put("CS102", new Student("CS102", "Priya Patel", "priya.patel@college.edu", "Information Technology", 5, 9.20));
        fallbackStudents.put("EC201", new Student("EC201", "Rohan Gupta", "rohan.gupta@college.edu", "Electronics", 3, 7.90));
        fallbackStudents.put("ME301", new Student("ME301", "Ananya Verma", "ananya.verma@college.edu", "Mechanical", 7, 8.45));
        fallbackStudents.put("CE401", new Student("CE401", "Vikram Malhotra", "vikram.m@college.edu", "Civil", 4, 7.60));
    }

    public void start() throws IOException {
        // Test Oracle connection and initialize table if connected
        isOracleAvailable = DBConnection.testConnection();
        if (isOracleAvailable) {
            System.out.println("[StudentServer] Connected to Oracle Database successfully.");
            dao.initTable();
        } else {
            System.out.println("[StudentServer] Notice: Oracle Database connection could not be established.");
            System.out.println("[StudentServer] Details: " + DBConnection.getLastError());
            System.out.println("[StudentServer] Running with interactive preview mode enabled.");
        }

        HttpServer server = null;
        int targetPort = port;
        for (int p = targetPort; p <= targetPort + 20; p++) {
            try {
                server = HttpServer.create(new InetSocketAddress(p), 0);
                this.port = p;
                break;
            } catch (java.net.BindException be) {
                System.out.println("[StudentServer] Port " + p + " is occupied, trying port " + (p + 1) + "...");
            }
        }
        if (server == null) {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        }

        // API Contexts
        server.createContext("/api/status", new StatusHandler());
        server.createContext("/api/students", new StudentsHandler());
        server.createContext("/api/students/add", new AddStudentHandler());
        server.createContext("/api/students/update", new UpdateStudentHandler());
        server.createContext("/api/students/drop", new DropStudentHandler());
        server.createContext("/api/students/delete", new DropStudentHandler()); // alias

        // Static Files Context (Frontend)
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null); // default executor
        server.start();

        System.out.println("=============================================================");
        System.out.println(" Student DBMS Web Server is RUNNING at:");
        System.out.println(" http://localhost:" + port + "/");
        System.out.println("=============================================================");
    }

    /**
     * Handler for checking Oracle DB connection status.
     */
    private class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            isOracleAvailable = DBConnection.testConnection();
            String json = "{"
                    + "\"connected\":" + isOracleAvailable + ","
                    + "\"url\":\"" + escapeJson(DBConnection.getUrl()) + "\","
                    + "\"lastError\":\"" + escapeJson(DBConnection.getLastError() != null ? DBConnection.getLastError() : "") + "\""
                    + "}";
            sendJsonResponse(exchange, 200, json);
        }
    }

    /**
     * Handler for GET /api/students (Retrieve all students)
     */
    private class StudentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCors(exchange);
                return;
            }

            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            List<Student> list;
            boolean fromOracle = false;
            try {
                list = dao.getAllStudents();
                isOracleAvailable = true;
                fromOracle = true;
            } catch (SQLException e) {
                isOracleAvailable = false;
                list = new ArrayList<>(fallbackStudents.values());
                list.sort(Comparator.comparing(Student::getRollNo));
            }

            StringBuilder sb = new StringBuilder();
            sb.append("{\"fromDatabase\":").append(fromOracle).append(",\"students\":[");
            for (int i = 0; i < list.size(); i++) {
                sb.append(list.get(i).toJson());
                if (i < list.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("]}");

            sendJsonResponse(exchange, 200, sb.toString());
        }
    }

    /**
     * Handler for POST /api/students/add (Add student via JDBC)
     */
    private class AddStudentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCors(exchange);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> params = parseParams(body);

            String rollNo = params.getOrDefault("rollNo", "").trim();
            String name = params.getOrDefault("name", "").trim();
            String email = params.getOrDefault("email", "").trim();
            String department = params.getOrDefault("department", "").trim();
            int semester = parseInt(params.get("semester"), 1);
            double gpa = parseDouble(params.get("gpa"), 0.0);

            if (rollNo.isEmpty() || name.isEmpty() || email.isEmpty() || department.isEmpty()) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"Roll No, Name, Email, and Department are required.\"}");
                return;
            }

            Student student = new Student(rollNo, name, email, department, semester, gpa);

            try {
                boolean ok = dao.addStudent(student);
                isOracleAvailable = true;
                sendJsonResponse(exchange, 200, "{\"success\":" + ok + ",\"message\":\"Student " + escapeJson(rollNo) + " added successfully via Oracle JDBC!\"}");
            } catch (SQLException e) {
                // If Oracle is offline or table does not exist, update fallback
                if (DBConnection.getLastError() != null) {
                    fallbackStudents.put(rollNo, student);
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Student " + escapeJson(rollNo) + " added (Preview mode - Oracle DB offline: " + escapeJson(e.getMessage()) + ")\"}");
                } else {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"Oracle JDBC Error: " + escapeJson(e.getMessage()) + "\"}");
                }
            }
        }
    }

    /**
     * Handler for POST /api/students/update (Update student via JDBC)
     */
    private class UpdateStudentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCors(exchange);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> params = parseParams(body);

            String rollNo = params.getOrDefault("rollNo", "").trim();
            String name = params.getOrDefault("name", "").trim();
            String email = params.getOrDefault("email", "").trim();
            String department = params.getOrDefault("department", "").trim();
            int semester = parseInt(params.get("semester"), 1);
            double gpa = parseDouble(params.get("gpa"), 0.0);

            if (rollNo.isEmpty()) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"Roll Number is required to update student.\"}");
                return;
            }

            Student student = new Student(rollNo, name, email, department, semester, gpa);

            try {
                boolean ok = dao.updateStudent(student);
                isOracleAvailable = true;
                if (ok) {
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Student " + escapeJson(rollNo) + " updated successfully via Oracle JDBC!\"}");
                } else {
                    sendJsonResponse(exchange, 404, "{\"success\":false,\"error\":\"Student with Roll No " + escapeJson(rollNo) + " not found in database.\"}");
                }
            } catch (SQLException e) {
                if (fallbackStudents.containsKey(rollNo)) {
                    fallbackStudents.put(rollNo, student);
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Student " + escapeJson(rollNo) + " updated (Preview mode - Oracle DB offline: " + escapeJson(e.getMessage()) + ")\"}");
                } else {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"Oracle JDBC Error: " + escapeJson(e.getMessage()) + "\"}");
                }
            }
        }
    }

    /**
     * Handler for POST /api/students/drop (Drop/Delete student via JDBC)
     */
    private class DropStudentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCors(exchange);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> params = parseParams(body);

            String rollNo = params.getOrDefault("rollNo", "").trim();
            if (rollNo.isEmpty()) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"Roll Number is required to drop student.\"}");
                return;
            }

            try {
                boolean ok = dao.dropStudent(rollNo);
                isOracleAvailable = true;
                if (ok) {
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Student " + escapeJson(rollNo) + " dropped successfully via Oracle JDBC!\"}");
                } else {
                    sendJsonResponse(exchange, 404, "{\"success\":false,\"error\":\"Student with Roll No " + escapeJson(rollNo) + " not found in database.\"}");
                }
            } catch (SQLException e) {
                if (fallbackStudents.containsKey(rollNo)) {
                    fallbackStudents.remove(rollNo);
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Student " + escapeJson(rollNo) + " dropped (Preview mode - Oracle DB offline: " + escapeJson(e.getMessage()) + ")\"}");
                } else {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"Oracle JDBC Error: " + escapeJson(e.getMessage()) + "\"}");
                }
            }
        }
    }

    /**
     * Serves static HTML/CSS/JS frontend files from the web/ folder.
     */
    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if ("/".equals(path) || path.isEmpty()) {
                path = "/index.html";
            }

            File file = new File(webDir, path.substring(1));
            if (!file.exists() || file.isDirectory()) {
                String notFound = "<h1>404 Not Found</h1><p>File not found: " + path + "</p>";
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(404, notFound.getBytes(StandardCharsets.UTF_8).length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFound.getBytes(StandardCharsets.UTF_8));
                }
                return;
            }

            String contentType = "text/plain";
            if (path.endsWith(".html")) contentType = "text/html; charset=UTF-8";
            else if (path.endsWith(".css")) contentType = "text/css; charset=UTF-8";
            else if (path.endsWith(".js")) contentType = "application/javascript; charset=UTF-8";
            else if (path.endsWith(".svg")) contentType = "image/svg+xml";

            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, file.length());

            try (InputStream in = new FileInputStream(file);
                 OutputStream out = exchange.getResponseBody()) {
                byte[] buf = new byte[4096];
                int count;
                while ((count = in.read(buf)) >= 0) {
                    out.write(buf, 0, count);
                }
            }
        }
    }

    // Helper utilities
    private void sendCors(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.sendResponseHeaders(204, -1);
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int read;
            while ((read = in.read(buf)) != -1) {
                baos.write(buf, 0, read);
            }
            return baos.toString(StandardCharsets.UTF_8);
        }
    }

    private Map<String, String> parseParams(String body) {
        Map<String, String> map = new HashMap<>();
        if (body == null || body.trim().isEmpty()) return map;
        body = body.trim();

        if (body.startsWith("{") && body.endsWith("}")) {
            Pattern p = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(\"[^\"]*\"|[^,}]+)");
            Matcher m = p.matcher(body);
            while (m.find()) {
                String k = m.group(1);
                String v = m.group(2).trim();
                if (v.startsWith("\"") && v.endsWith("\"")) {
                    v = v.substring(1, v.length() - 1);
                }
                map.put(k, v);
            }
        } else {
            for (String pair : body.split("&")) {
                int idx = pair.indexOf('=');
                if (idx > 0) {
                    try {
                        String k = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                        String v = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                        map.put(k, v);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        return map;
    }

    private int parseInt(String s, int def) {
        if (s == null) return def;
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    private double parseDouble(String s, double def) {
        if (s == null) return def;
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return def; }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", "");
    }
}
