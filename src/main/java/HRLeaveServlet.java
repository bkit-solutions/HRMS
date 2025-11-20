import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/HRLeaveServlet")
public class HRLeaveServlet extends HttpServlet {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/EmployeeManagementSystem";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASS = "ijustDh53@";

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !"HR".equals(session.getAttribute("role"))) {
            response.sendRedirect("index.html");
            return;
        }

        String action = request.getParameter("action");
        if ("viewLeaveRequests".equals(action)) {
            viewLeaveRequests(request, response);
        } else if ("getEmployees".equals(action)) {
            getEmployees(request, response);
        }
    }

    private void viewLeaveRequests(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String employeeId = request.getParameter("employeeId");
        String status = request.getParameter("status");
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JSONArray leaveRecords = new JSONArray();

        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS)) {
            String query = "SELECT l.id, u.name AS employee_name, l.leave_type, l.start_date, l.end_date, l.reason, l.status " +
                           "FROM leave_requests l JOIN users u ON l.user_id = u.id WHERE 1=1";
            if (employeeId != null && !employeeId.isEmpty()) {
                query += " AND l.user_id = " + employeeId;
            }
            if (status != null && !status.isEmpty()) {
                query += " AND l.status = '" + status + "'";
            }

            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                JSONObject leave = new JSONObject();
                leave.put("id", rs.getInt("id"));
                leave.put("employee_name", rs.getString("employee_name"));
                leave.put("leave_type", rs.getString("leave_type"));
                leave.put("start_date", rs.getString("start_date"));
                leave.put("end_date", rs.getString("end_date"));
                leave.put("reason", rs.getString("reason"));
                leave.put("status", rs.getString("status"));
                leaveRecords.put(leave);
            }
            out.print("{\"records\": " + leaveRecords.toString() + "}");
        } catch (SQLException e) {
            e.printStackTrace();
            out.print("{\"success\": false, \"message\": \"Database error.\"}");
        }
    }

    private void getEmployees(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JSONArray employees = new JSONArray();

        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS)) {
            String query = "SELECT id, name FROM users WHERE role = 'Employee'";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                JSONObject employee = new JSONObject();
                employee.put("id", rs.getInt("id"));
                employee.put("name", rs.getString("name"));
                employees.put(employee);
            }
            out.print(employees);
        } catch (SQLException e) {
            e.printStackTrace();
            out.print("{\"success\": false, \"message\": \"Database error.\"}");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !"HR".equals(session.getAttribute("role"))) {
            response.sendRedirect("index.html");
            return;
        }

        String action = request.getParameter("action");

        if ("updateLeaveStatus".equals(action)) {
            updateLeaveStatus(request, response);
        }
    }

    private void updateLeaveStatus(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int leaveId = Integer.parseInt(request.getParameter("id"));
        String status = request.getParameter("status");

        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS)) {
            String query = "UPDATE leave_requests SET status = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, status);
            stmt.setInt(2, leaveId);
            stmt.executeUpdate();

            response.getWriter().print("{\"success\": true, \"message\": \"Leave request updated successfully.\"}");
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().print("{\"success\": false, \"message\": \"Database error.\"}");
        }
    }
}
