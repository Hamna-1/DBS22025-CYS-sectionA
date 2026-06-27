🎪 Carnival Management System

A comprehensive desktop application built with **Java Swing** and **MySQL** to manage daily carnival operations, including employee management, ticket booking, show scheduling, venue management, and automated business reporting.

**Course:** Database System Lab 
**Department:** Computer Engineering Department  
**University:** University of Engineering and Technology (UET), Lahore  
**Session:** 2025 | **Instructors:** Ms. Sahar
---

  Features

🖥️ User Interface (Java Swing)
   **Interactive Forms:** Text boxes, password fields, radio buttons, checkboxes, dropdowns, date/time selectors, and text areas.
   **Advanced Tables:** Scrollable tables with **buttons inside table cells** for quick actions.
   **Navigation:** File menu on every screen and a centralized Dashboard.
   **Unified Forms:** Same form used for both "Add" and "Edit" operations.
   **Responsive Design:** Proper layout managers (`BorderLayout`, `GridBagLayout`) for resizing.

⚙️ Backend & Business Logic
   **Domain & Software Classes:** 8+ domain models and 5+ utility/software classes.
   **Data Access:** DAO pattern for clean database interactions.
   **Transactions:** 3 complex transactions (Ticket Booking, Cancellation, Performer Registration).
   **Validation & Exceptions:** Custom validators and exception handling.
   **Logging:** Automatic error and activity logging to the database.
   **PDF Reports:** 10 parameter-based business-level reports generated using Apache PDFBox.

🗄️ Database (MySQL)
  **Tables:** 18 fully normalized tables.
  **Views:** 7 analytical views (e.g., `vw_UpcomingShows`, `vw_RevenueByVenue`).
  **Stored Procedures:** 3 procedures (`sp_BookTicket`, `sp_CancelTicket`, `sp_GetShowRevenue`).
  **Triggers:** 3 automated triggers (Seat availability check, auto seat decrement, audit logging).
  **Constraints:** 40+ constraints (Primary Keys, Foreign Keys, UNIQUE, NOT NULL, CHECK, DEFAULT).

---

🛠️ Tech Stack

 **Frontend:** Java Swing
 **Backend:** Java (JDBC)
 **Database:** MySQL 8.0+
 **PDF Generation:** Apache PDFBox 3.0.7
 **IDE:** IntelliJ IDEA

---

📋 Prerequisites

Before running the project, ensure you have the following installed:
1.  **Java JDK** (Version 11 or higher)
2.  **MySQL Server** (or XAMPP/WAMP)
3.  **IntelliJ IDEA** (Recommended)

---

Installation & Setup

1. Clone the Repository
```bash
git clone https://github.com/your-username/DBS25F001.git
cd DBS22025-CYS-sectionA
```

2. Database Setup
1. Start your MySQL server.
2. Open MySQL Workbench (or command line).
3. Execute the **`Final Project DB.sql`** file located in the root directory. This will create the `carnival_db` database along with all tables, views, triggers, and sample data.

3. Add Required Libraries
Navigate to the `lib/` folder in the project and add the following JAR files to your IntelliJ project libraries (Right-click JAR -> Add as Library):
  `mysql-connector-j-9.7.0.jar`
  `itextpdf-5.5.13.3.jar`

---

🔐 Credentials & Configuration

Database Connection
The application connects to the database using the credentials defined in `util/DBConnection.java`.

*   URL: `jdbc:mysql://localhost:3306/carnival_db`
*   Username: `root`
*   Password: `YourPasword`
    >⚠️ Note: If you are using "XAMPP", the default password is usually blank. If you get a connection error, open `DBConnection.java` and change the password to `""`.

👤 Application Login
The system uses email-based authentication. The password field is a dummy field and accepts any value.

| Role | Employee Name | Login Email | Password |


🚀 How to Run

1. Open the project in **IntelliJ IDEA**.
2. Ensure all libraries are added and MySQL is running.
3. Navigate to `src/Main.java`.
4. Click the **Run** button (or right-click -> Run 'Main.main()').
5. The Login Screen will appear. Use the credentials above to log in.


DBS25F001/
├── src/
│   ├── ui/                        # GUI Forms (Login, Dashboard, etc.) + # Main>java for main entry
│   ├── model/                     # Domain/Entity classes
│   ├── dao/                       # Data Access Objects
│   ├── validator/                 # Input validators
│   ├── exception/                 # Custom exceptions
│   ├── service/                   # Business logic & Transactions
│   └── util/                      # DBConnection, AuditLogger
├── lib/                           # External JAR libraries
├── reports/                       # Generated PDF reports
├── project Sql.txt                # Database schema and seed data
└── README.md                      # Project documentation


🆘 Troubleshooting

| Issue | Solution |

| **Database Connection Failed** | Ensure MySQL is running. Check if `carnival_db` exists. Verify password in `DBConnection.java`. |
| **ClassNotFoundException** | Ensure `mysql-connector-j-9.7.0.jar` is added to project libraries. |
| **PDF Generation Error** | Ensure `pdfbox-app-3.0.7.jar` is added and the `reports/` folder exists in the root directory. |
| **Login Fails** | Ensure the employee email exists in the database and their status is set to `'ACTIVE'`. |

---

👥 Contributors


[Hamna Moazam] - [Hamna-1]
[Hamid Zeeshan] - [Hamid Xee]
[Eman Fatima] - [emanchand]
