import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/HRAttendanceServlet")
public class HRAttendanceServlet extends HttpServlet {
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

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JSONArray records = new JSONArray();

        int page = 1;
        int recordsPerPage = 5;
        if (request.getParameter("page") != null) {
            page = Integer.parseInt(request.getParameter("page"));
        }
        int start = (page - 1) * recordsPerPage;

        String dateFilter = request.getParameter("date");

        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS)) {
            String query = "SELECT a.user_id, u.name, a.date, a.check_in, a.check_out, a.status " +
                           "FROM attendance a JOIN users u ON a.user_id = u.id ";
            
            if (dateFilter != null && !dateFilter.isEmpty()) {
                query += "WHERE a.date = ? ";
            }

            query += "ORDER BY a.date DESC LIMIT ?, ?";
            PreparedStatement stmt = conn.prepareStatement(query);

            int paramIndex = 1;
            if (dateFilter != null && !dateFilter.isEmpty()) {
                stmt.setString(paramIndex++, dateFilter);
            }
            stmt.setInt(paramIndex++, start);
            stmt.setInt(paramIndex, recordsPerPage);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                JSONObject record = new JSONObject();
                record.put("user_id", rs.getInt("user_id"));
                record.put("employeeName", rs.getString("name"));
                record.put("date", rs.getString("date"));
                record.put("check_in", rs.getString("check_in") != null ? rs.getString("check_in") : "");
                record.put("check_out", rs.getString("check_out") != null ? rs.getString("check_out") : "");
                record.put("status", rs.getString("status"));
                records.put(record);
            }

            // Get total count for pagination
            String countQuery = "SELECT COUNT(*) FROM attendance";
            if (dateFilter != null && !dateFilter.isEmpty()) {
                countQuery += " WHERE date = ?";
            }
            PreparedStatement countStmt = conn.prepareStatement(countQuery);
            if (dateFilter != null && !dateFilter.isEmpty()) {
                countStmt.setString(1, dateFilter);
            }
            ResultSet countRs = countStmt.executeQuery();
            int totalRecords = 0;
            if (countRs.next()) {
                totalRecords = countRs.getInt(1);
            }
            int totalPages = (int) Math.ceil((double) totalRecords / recordsPerPage);

            JSONObject result = new JSONObject();
            result.put("records", records);
            result.put("totalPages", totalPages);
            out.print(result);
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
        int userId = Integer.parseInt(request.getParameter("userId"));
        String date = request.getParameter("date");

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS)) {
            if ("update".equals(action)) {
                String checkIn = request.getParameter("checkIn");
                String checkOut = request.getParameter("checkOut");
                String status = request.getParameter("status");

                String query = "UPDATE attendance SET check_in = ?, check_out = ?, status = ? WHERE user_id = ? AND date = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, checkIn);
                stmt.setString(2, checkOut);
                stmt.setString(3, status);
                stmt.setInt(4, userId);
                stmt.setString(5, date);
                stmt.executeUpdate();

                out.print("{\"success\": true, \"message\": \"Attendance updated successfully.\"}");
            } else if ("delete".equals(action)) {
                String query = "DELETE FROM attendance WHERE user_id = ? AND date = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, userId);
                stmt.setString(2, date);
                stmt.executeUpdate();

                out.print("{\"success\": true, \"message\": \"Attendance record deleted.\"}");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            out.print("{\"success\": false, \"message\": \"Database error.\"}");
        }
    }
}
