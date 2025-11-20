import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.NumberFormat;
import java.util.Locale;

@WebServlet("/EmployeePayrollServlet")
public class EmployeePayrollServlet extends HttpServlet {
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
        String action = request.getParameter("action");

        if ("view".equals(action)) {
            viewPayrollRecords(request, response, userId);
        } else if ("downloadPayslip".equals(action)) {
            generatePayslip(request, response, userId);
        }
    }

    private void viewPayrollRecords(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException {
        String month = request.getParameter("month");
        String yearStr = request.getParameter("year");

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
            String query = "SELECT month, year, salary, bonuses, deductions, net_salary, paid_status FROM payroll WHERE user_id = ?";
            
            if (month != null && !month.isEmpty()) {
                query += " AND month = ?";
            }
            query += " AND year = ? ORDER BY year DESC, month DESC";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            int paramIndex = 2;
            if (month != null && !month.isEmpty()) {
                stmt.setString(paramIndex++, month);
            }
            stmt.setInt(paramIndex, year);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                JSONObject record = new JSONObject();
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

    private void generatePayslip(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException {
        String month = request.getParameter("month");
        String yearStr = request.getParameter("year");

        if (month == null || month.isEmpty() || yearStr == null || yearStr.isEmpty()) {
            response.setContentType("application/json");
            response.getWriter().print("{\"success\": false, \"message\": \"Month and Year are required.\"}");
            return;
        }

        int year = Integer.parseInt(yearStr);

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
             PrintWriter out = response.getWriter()) {

            String query = "SELECT u.name, u.department, p.salary, p.bonuses, p.deductions, p.net_salary " +
                           "FROM payroll p JOIN users u ON p.user_id = u.id WHERE p.user_id = ? AND p.month = ? AND p.year = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            stmt.setString(2, month);
            stmt.setInt(3, year);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                out.print("<h3>No payroll record found.</h3>");
                return;
            }

            String employeeName = rs.getString("name");
            String department = rs.getString("department");
            double salary = rs.getDouble("salary");
            double bonuses = rs.getDouble("bonuses");
            double deductions = rs.getDouble("deductions");
            double netSalary = rs.getDouble("net_salary");

            // Output HTML Payslip View
            out.println("<html><head><title>Payslip</title>");
            out.println("<link href='https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css' rel='stylesheet'>");
            out.println("</head><body class='container mt-5'>");

            out.println("<div class='card shadow-lg p-4'>");
            out.println("<h2 class='text-center mb-3'>Salary Payslip - " + month + " " + year + "</h2>");
            
            // Employee Details
            out.println("<table class='table table-bordered'>");
            out.println("<tr><th>Employee Name</th><td>" + employeeName + "</td></tr>");
            out.println("<tr><th>Department</th><td>" + department + "</td></tr>");
            out.println("<tr><th>Month</th><td>" + month + "</td></tr>");
            out.println("<tr><th>Year</th><td>" + year + "</td></tr>");
            out.println("</table>");

            // Payroll Table
            out.println("<table class='table table-striped mt-3'>");
            out.println("<thead class='table-dark'><tr><th>Earnings</th><th>Amount</th><th>Deductions</th><th>Amount</th></tr></thead>");
            out.println("<tbody>");
            out.println("<tr><td>Base Salary</td><td>$" + salary + "</td><td>Taxes</td><td>$" + deductions + "</td></tr>");
            out.println("<tr><td>Bonuses</td><td>$" + bonuses + "</td><td></td><td></td></tr>");
            out.println("</tbody>");
            out.println("</table>");

            // Net Salary
            out.println("<h4 class='text-end mt-3'><strong>Net Salary: $" + netSalary + "</strong></h4>");

            out.println("<div class='text-center mt-4'><button class='btn btn-primary' onclick='window.print()'>Print Payslip</button></div>");
            
            out.println("</div>");
            out.println("</body></html>");
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().print("{\"success\": false, \"message\": \"Error generating payslip.\"}");
        }
    }
}
