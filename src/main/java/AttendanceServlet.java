import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/AttendanceServlet")
public class AttendanceServlet extends HttpServlet {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/EmployeeManagementSystem";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASS = "ijustDh53@";

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("index.html");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String action = request.getParameter("action");
        String today = java.time.LocalDate.now().toString();
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");

        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS)) {
            if ("checkin".equals(action)) {
                String query = "INSERT INTO attendance (user_id, date, check_in, status) VALUES (?, ?, NOW(), 'Present') ON DUPLICATE KEY UPDATE check_in = NOW(), status = 'Present'";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, userId);
                stmt.setString(2, today);
                stmt.executeUpdate();
                out.print("{\"success\": true, \"message\": \"Checked in successfully.\"}");
            } else if ("checkout".equals(action)) {
                String query = "UPDATE attendance SET check_out = NOW() WHERE user_id = ? AND date = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, userId);
                stmt.setString(2, today);
                stmt.executeUpdate();
                out.print("{\"success\": true, \"message\": \"Checked out successfully.\"}");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            out.print("{\"success\": false, \"message\": \"Database error.\"}");
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("index.html");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String selectedDate = request.getParameter("date");
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        JSONArray records = new JSONArray();

        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS)) {
            String query = "SELECT date, check_in, check_out, status FROM attendance WHERE user_id = ?";
            if (selectedDate != null && !selectedDate.isEmpty()) {
                query += " AND date = ?";
            }

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            if (selectedDate != null && !selectedDate.isEmpty()) {
                stmt.setString(2, selectedDate);
            }
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                JSONObject record = new JSONObject();
                record.put("date", rs.getString("date"));
                record.put("check_in", rs.getString("check_in"));
                record.put("check_out", rs.getString("check_out"));
                record.put("status", rs.getString("status"));
                records.put(record);
            }

            out.print("{\"records\": " + records.toString() + "}");
        } catch (SQLException e) {
            e.printStackTrace();
            out.print("{\"error\": \"Database error occurred.\"}");
        }
    }
}
