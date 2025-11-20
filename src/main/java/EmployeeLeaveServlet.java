import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/EmployeeLeaveServlet")
public class EmployeeLeaveServlet extends HttpServlet {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/EmployeeManagementSystem";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASS = "ijustDh53@";

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("index.html");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        viewLeaveHistory(request, response, userId);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("index.html");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String action = request.getParameter("action");

        if ("applyLeave".equals(action)) {
            applyForLeave(request, response, userId);
        } else if ("cancelLeave".equals(action)) {
            cancelLeave(request, response, userId);
        }
    }

    // ✅ Fetch Leave History
    private void viewLeaveHistory(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JSONArray leaveRecords = new JSONArray();

        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS)) {
            String query = "SELECT id, leave_type, start_date, end_date, reason, status FROM leave_requests WHERE user_id = ? ORDER BY start_date DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                JSONObject leave = new JSONObject();
                leave.put("id", rs.getInt("id"));
                leave.put("leave_type", rs.getString("leave_type"));
                leave.put("start_date", rs.getString("start_date"));
                leave.put("end_date", rs.getString("end_date"));
                leave.put("reason", rs.getString("reason"));
                leave.put("status", rs.getString("status"));
                leaveRecords.put(leave);
            }

            JSONObject result = new JSONObject();
            result.put("records", leaveRecords);
            out.print(result);
        } catch (SQLException e) {
            e.printStackTrace();
            out.print("{\"success\": false, \"message\": \"Database error while fetching leave history.\"}");
        }
    }

    // ✅ Apply for Leave with Overlapping Check
    private void applyForLeave(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException {
        String leaveType = request.getParameter("leaveType");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        String reason = request.getParameter("reason");

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        if (startDate.compareTo(endDate) > 0) {
            out.print("{\"success\": false, \"message\": \"End date cannot be before start date.\"}");
            return;
        }

        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS)) {
            // Check for overlapping leave requests
            String checkQuery = "SELECT COUNT(*) FROM leave_requests WHERE user_id = ? AND (start_date BETWEEN ? AND ? OR end_date BETWEEN ? AND ?)";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setInt(1, userId);
            checkStmt.setString(2, startDate);
            checkStmt.setString(3, endDate);
            checkStmt.setString(4, startDate);
            checkStmt.setString(5, endDate);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                out.print("{\"success\": false, \"message\": \"You already have a leave request for these dates.\"}");
                return;
            }

            // Insert leave request
            String query = "INSERT INTO leave_requests (user_id, leave_type, start_date, end_date, reason, status) VALUES (?, ?, ?, ?, ?, 'Pending')";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            stmt.setString(2, leaveType);
            stmt.setString(3, startDate);
            stmt.setString(4, endDate);
            stmt.setString(5, reason);
            stmt.executeUpdate();

            out.print("{\"success\": true, \"message\": \"Leave request submitted successfully.\"}");
        } catch (SQLException e) {
            e.printStackTrace();
            out.print("{\"success\": false, \"message\": \"Database error.\"}");
        }
    }

    // ✅ Cancel Leave Request (Only if Pending)
    private void cancelLeave(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException {
        int leaveId = Integer.parseInt(request.getParameter("id"));

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS)) {
            // Check if leave is still pending
            String checkQuery = "SELECT status FROM leave_requests WHERE id = ? AND user_id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setInt(1, leaveId);
            checkStmt.setInt(2, userId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                if (!"Pending".equals(rs.getString("status"))) {
                    out.print("{\"success\": false, \"message\": \"Only pending leave requests can be canceled.\"}");
                    return;
                }
            } else {
                out.print("{\"success\": false, \"message\": \"Leave request not found.\"}");
                return;
            }

            // Delete leave request
            String query = "DELETE FROM leave_requests WHERE id = ? AND user_id = ? AND status = 'Pending'";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, leaveId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();

            out.print("{\"success\": true, \"message\": \"Leave request canceled successfully.\"}");
        } catch (SQLException e) {
            e.printStackTrace();
            out.print("{\"success\": false, \"message\": \"Database error.\"}");
        }
    }
}
