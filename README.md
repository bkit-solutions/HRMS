# 🗳 HRMS – Human Resource Management System (J2EE)

<p align="center">
A complete organizational HRMS solution built using <b>JSP</b>, <b>Servlets</b>, and <b>JDBC</b>.  
Designed for employee & HR operations including attendance, payroll, and leave management.
</p>

<p align="center">
<img src="https://cdn-icons-png.flaticon.com/512/3135/3135715.png" width="130">
</p>

---

## 📌 Overview  
The **HRMS (Human Resource Management System)** is a full-stack **Java (J2EE)** project that provides role‑based access for both **Employees** and **HR/Admin**.

It contains modules for:

- Attendance  
- Payroll  
- Leave Management  
- Employee Records  
- HR Controls  

This system is ideal for academic, internship, and enterprise‑level HR automation projects.

---

## ⚡ Core Features

### 👨‍💼 Employee Panel
- Submit attendance  
- Apply for leaves  
- View payroll and salary slips  
- Check approval status  
- Edit basic profile  

### 🧑‍💼 HR/Admin Panel
- Manage employees  
- Approve / reject leave requests  
- Manage attendance  
- Generate payroll  
- View dashboards & reports  

---

## 🏗 Tech Stack

### **Backend**
- Java (J2EE)
- Servlets
- JDBC
- MySQL Database  

### **Frontend**
- JSP  
- HTML / CSS  
- Basic JavaScript  

---

## 📁 Project Structure

```
HRMS/
│
├── .project
├── .classpath
├── hr_sql.sql               <-- Database initialization script
│
├── src/main/java            <-- Java, Servlets, JDBC classes
├── src/main/webapp          <-- JSP, CSS, images
│
└── build/classes            <-- Compiled files
```

---

## 🗃 Database Setup

The SQL script:

```
hr_sql.sql
```

must be imported before running the project.

### **Steps:**
1. Create a new database:
```sql
CREATE DATABASE hrms;
```

2. Import the SQL file using:
- MySQL Workbench  
- phpMyAdmin  
- or terminal  

This will auto‑create tables for:
- employees  
- hr  
- attendance  
- payroll  
- leaves  

---

## 🔌 JDBC Configuration (IMPORTANT)

Inside:

```
src/main/java/
```

update your MySQL username & password:

```java
String url = "jdbc:mysql://localhost:3306/hrms";
String username = "your_mysql_username";
String password = "your_mysql_password";  // Set your real password
Class.forName("com.mysql.cj.jdbc.Driver");
Connection conn = DriverManager.getConnection(url, username, password);
```

⚠️ The project will NOT run unless the correct credentials are set.

---

## 🚀 Setup & Execution Guide

### 1️⃣ Install Requirements
- JDK 8+  
- Apache Tomcat (8.5 / 9 / 10)  
- MySQL Server  
- MySQL Connector/J  

---

### 2️⃣ Import into Eclipse / IntelliJ
- Open **Eclipse**  
- Go to `File > Import > Existing Projects into Workspace`  
- Select the HRMS project folder  
- Finish  

---

### 3️⃣ Add MySQL Connector JAR
Right‑click project →  
`Build Path > Configure Build Path` →  
`Add External JAR` → choose **mysql‑connector.jar**

---

### 4️⃣ Configure Tomcat Server
- Add project to Tomcat  
- Ensure no errors  
- Start server  

---

### 5️⃣ Run the Application
Open:

```
http://localhost:8080/HRMS/
```

---

## 👥 Project Maintainers
<h3 align="center">BKIT</h3>

<p align="center">
Developing efficient, scalable HR management applications using full‑stack Java.
</p>
