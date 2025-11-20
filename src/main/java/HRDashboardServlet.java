import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/HRDashboardServlet")
public class HRDashboardServlet extends HttpServlet {
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

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS)) {
            if ("attendance".equals(action)) {
                out.print(getAttendanceReport(conn));
            } else if ("payroll".equals(action)) {
                out.print(getPayrollReport(conn));
            } else if ("leave".equals(action)) {
                out.print(getLeaveReport(conn));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            out.print("{\"success\": false, \"message\": \"Database error.\"}");
        }
    }

    // Generate Attendance Report
    private String getAttendanceReport(Connection conn) throws SQLException {
        String query = "SELECT date, COUNT(*) AS total FROM attendance GROUP BY date ORDER BY date";
        PreparedStatement stmt = conn.prepareStatement(query);
        ResultSet rs = stmt.executeQuery();

        JSONObject result = new JSONObject();
        JSONArray labels = new JSONArray();
        JSONArray values = new JSONArray();

        while (rs.next()) {
            labels.put(rs.getString("date"));
            values.put(rs.getInt("total"));
        }

        result.put("labels", labels);
        result.put("values", values);
        return result.toString();
    }

    // Generate Payroll Report
    private String getPayrollReport(Connection conn) throws SQLException {
        String query = "SELECT department, SUM(net_salary) AS total_salary FROM users u JOIN payroll p ON u.id = p.user_id GROUP BY department";
        PreparedStatement stmt = conn.prepareStatement(query);
        ResultSet rs = stmt.executeQuery();

        JSONObject result = new JSONObject();
        JSONArray labels = new JSONArray();
        JSONArray values = new JSONArray();

        while (rs.next()) {
            labels.put(rs.getString("department"));
            values.put(rs.getDouble("total_salary"));
        }

        result.put("labels", labels);
        result.put("values", values);
        return result.toString();
    }

    // Generate Leave Report
    private String getLeaveReport(Connection conn) throws SQLException {
        String query = "SELECT leave_type, COUNT(*) AS total FROM leave_requests GROUP BY leave_type";
        PreparedStatement stmt = conn.prepareStatement(query);
        ResultSet rs = stmt.executeQuery();

        JSONObject result = new JSONObject();
        JSONArray labels = new JSONArray();
        JSONArray values = new JSONArray();

        while (rs.next()) {
            labels.put(rs.getString("leave_type"));
            values.put(rs.getInt("total"));
        }

        result.put("labels", labels);
        result.put("values", values);
        return result.toString();
    }
}
