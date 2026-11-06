# MediBook – Patient Appointment Booking Management System

A Java-based desktop application designed to simplify patient appointment booking and management using Java Swing, JDBC, and MySQL.

## About the Project

MediBook is a Patient Appointment Booking Management System developed as part of a B.Tech Computer Science and Engineering academic project. It provides a centralized platform where patients can book appointments, doctors can manage their schedules, and administrators can manage the overall system.

The application uses a graphical user interface built with Java Swing and stores information in a MySQL database through JDBC connectivity.

## Key Features

### 1. Patient Module
- Patient registration and login
- View available doctors
- Book appointments by selecting a doctor, date, and time slot
- View appointment details and status
- Cancel appointments
- Manage profile information
- View notifications

### 2. Doctor Module
- View scheduled appointments
- View patient and appointment details
- Update appointment status
- Report appointment delays
- Reschedule appointments

### 3. Admin Module
- Manage doctor details
- Manage doctor schedules
- View registered patients
- View all appointments
- Monitor appointment statistics through the dashboard

### 4. Notification Module
- Display appointment-related updates
- Maintain notification records
- Help patients stay informed about appointment changes

## Technologies Used

| Technology | Purpose |
|---|---|
| Java | Application development |
| Java Swing | Graphical User Interface |
| MySQL | Database management |
| JDBC | Database connectivity |
| Apache NetBeans IDE | Development environment |
| Maven | Project and dependency management |

## Project Structure

```text
MediBookNew/
│
├── src/
│   ├── main/
│   └── test/
│
├── pom.xml
├── .gitignore
└── README.md
```

## Database Configuration

- Database Name: `medibook_new_db`
- Database: MySQL
- Connectivity: JDBC
- MySQL Connector/J: 8.4.0

Update the database connection settings according to your local MySQL configuration before running the application. Do not publish database passwords or private credentials.

## Screenshots

Application screenshots will be added here.

- Patient Dashboard
- Doctor Dashboard
- Admin Dashboard

## How to Run the Project

1. Install Java JDK and Apache NetBeans IDE.
2. Install MySQL Server.
3. Create the database named `medibook_new_db`.
4. Configure the database connection in the project.
5. Open the project in Apache NetBeans.
6. Allow Maven to download the required dependencies.
7. Run the application from NetBeans.

## Future Enhancements

- Email and SMS appointment reminders
- Online payment integration
- Enhanced security and authentication
- Web and mobile application support
- Advanced appointment analytics

## Learning Outcomes

This project provided practical experience in:
- Object-Oriented Programming using Java
- Java Swing GUI development
- MySQL database design and management
- JDBC connectivity
- Event handling and application development
- Modular software design

## Project Information

**Project Name:** MediBook – Patient Appointment Booking Management System

**Project Type:** Academic Project

**Course:** B.Tech Computer Science and Engineering

**Subject:** Object Oriented Programming (PBCST304)

**Development Environment:** Apache NetBeans IDE

---

*Developed as an academic project to demonstrate Java-based desktop application development and database management.*
