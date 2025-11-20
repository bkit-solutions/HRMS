import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/HRPayrollServlet")
public class HRPayrollServlet extends HttpServlet {
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

        if ("view".equals(action)) {
            viewPayrollRecords(request, response);
        } else if ("getEmployees".equals(action)) {
            getEmployees(request, response);
        }
    }

    private void viewPayrollRecords(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String month = request.getParameter("month");
        String yearStr = request.getParameter("year");
        String userIdStr = request.getParameter("userId");

        if (yearStr == null || yearStr.isEmpty()) {
            response.setContentType("application/json");
            response.getWriter().print("{\"success\": false, \"message\": \"Year parameter is required.\"}");
            return;
        }

        int year = Integer.parseInt(yearStr);
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JSONArray records = new JSONArray();

        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS)) {
            String query = "SELECT p.id, u.name AS employeeName, p.month, p.year, p.salary, p.bonuses, p.deductions, p.net_salary, p.paid_status " +
                           "FROM payroll p JOIN users u ON p.user_id = u.id WHERE p.year = ?";

            if (month != null && !month.isEmpty()) {
                query += " AND p.month = ?";
            }
            if (userIdStr != null && !userIdStr.isEmpty()) {
                query += " AND p.user_id = ?";
            }
            query += " ORDER BY p.year DESC, p.month DESC";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, year);
            int paramIndex = 2;
            if (month != null && !month.isEmpty()) {
                stmt.setString(paramIndex++, month);
            }
            if (userIdStr != null && !userIdStr.isEmpty()) {
                stmt.setInt(paramIndex, Integer.parseInt(userIdStr));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                JSONObject record = new JSONObject();
                record.put("id", rs.getInt("id"));
                record.put("employeeName", rs.getString("employeeName"));
                record.put("month", rs.getString("month"));
                record.put("year", rs.getInt("year"));
                record.put("salary", rs.getDouble("salary"));
                record.put("bonuses", rs.getDouble("bonuses"));
                record.put("deductions", rs.getDouble("deductions"));
                record.put("net_salary", rs.getDouble("net_salary"));
                record.put("paid_status", rs.getString("paid_status"));
                records.put(record);
            }

            JSONObject result = new JSONObject();
            result.put("records", records);
            out.print(result);
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

        if ("addPayroll".equals(action)) {  
            addPayroll(request, response);
        } else if ("updatePayroll".equals(action)) {  // ✅ Fixed: updatePayroll Method Added
            updatePayroll(request, response);
        } else if ("deletePayroll".equals(action)) {  // ✅ Fixed: deletePayroll Method Added
            deletePayroll(request, response);
        } else if ("markAsPaid".equals(action)) {  
            markAsPaid(request, response);
        }
    }
    
    private void markAsPaid(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int payrollId = Integer.parseInt(request.getParameter("id"));

        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS)) {
            String query = "UPDATE payroll SET paid_status = 'Paid' WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, payrollId);
            stmt.executeUpdate();

            response.getWriter().print("{\"success\": true, \"message\": \"Payroll marked as Paid.\"}");
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().print("{\"success\": false, \"message\": \"Database error.\"}");
        }
    }


    private void addPayroll(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int userId = Integer.parseInt(request.getParameter("userId"));
        String month = request.getParameter("month");
        int year = Integer.parseInt(request.getParameter("year"));
        double salary = Double.parseDouble(request.getParameter("salary"));
        double bonuses = Double.parseDouble(request.getParameter("bonuses"));
        double deductions = Double.parseDouble(request.getParameter("deductions"));

        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS)) {
            String query = "INSERT INTO payroll (user_id, month, year, salary, bonuses, deductions, paid_status) VALUES (?, ?, ?, ?, ?, ?, 'Pending')";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            stmt.setString(2, month);
            stmt.setInt(3, year);
            stmt.setDouble(4, salary);
            stmt.setDouble(5, bonuses);
            stmt.setDouble(6, deductions);
            stmt.executeUpdate();

            response.getWriter().print("{\"success\": true, \"message\": \"Payroll added successfully.\"}");
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().print("{\"success\": false, \"message\": \"Database error.\"}");
        }
    }

    private void updatePayroll(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int payrollId = Integer.parseInt(request.getParameter("id"));
        double salary = Double.parseDouble(request.getParameter("salary"));
        double bonuses = Double.parseDouble(request.getParameter("bonuses"));
        double deductions = Double.parseDouble(request.getParameter("deductions"));

        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS)) {
            String query = "UPDATE payroll SET salary = ?, bonuses = ?, deductions = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setDouble(1, salary);
            stmt.setDouble(2, bonuses);
            stmt.setDouble(3, deductions);
            stmt.setInt(4, payrollId);
            stmt.executeUpdate();

            response.getWriter().print("{\"success\": true, \"message\": \"Payroll updated successfully.\"}");
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().print("{\"success\": false, \"message\": \"Database error.\"}");
        }
    }

    private void deletePayroll(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int payrollId = Integer.parseInt(request.getParameter("id"));

        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS)) {
            String query = "DELETE FROM payroll WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, payrollId);
            stmt.executeUpdate();

            response.getWriter().print("{\"success\": true, \"message\": \"Payroll deleted successfully.\"}");
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().print("{\"success\": false, \"message\": \"Database error.\"}");
        }
    }
}
