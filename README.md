# 🏦 SecureBank CRM – Bank Customer Relationship Program
### Object-Oriented Programming (OOP) – Java Swing GUI Application

---

## OOP Principles Demonstrated

| Principle       | Where Applied |
|----------------|---------------|
| **Abstraction**   | `User` (abstract class) with abstract methods `getRole()` and `getDisplayInfo()` |
| **Encapsulation** | All model fields are `private` with controlled getters/setters; `DataStore` singleton |
| **Inheritance**   | `Customer → User`, `Staff → User`, `Manager → Staff → User` (multi-level) |
| **Polymorphism**  | `getRole()` and `getDisplayInfo()` overridden in each subclass; `User` references hold any subtype |

---

## Project Structure

```
BankCRM/
├── src/
│   └── bankcrm/
│       ├── Main.java                         ← Entry point
│       ├── model/
│       │   ├── User.java                     ← Abstract base (Abstraction)
│       │   ├── Customer.java                 ← Extends User (Inheritance)
│       │   ├── Staff.java                    ← Extends User (Inheritance)
│       │   ├── Manager.java                  ← Extends Staff (Multi-level Inheritance)
│       │   ├── Ticket.java                   ← Encapsulated ticket entity + Enums
│       │   ├── TicketHistory.java            ← Immutable audit trail entry
│       │   └── Notification.java             ← In-app notification
│       ├── service/
│       │   ├── DataStore.java                ← Singleton in-memory repository
│       │   ├── AuthService.java              ← Login / password management
│       │   ├── TicketService.java            ← Full ticket lifecycle logic
│       │   ├── UserService.java              ← User registration / staff CRUD
│       │   └── NotificationService.java      ← Notification dispatch
│       ├── util/
│       │   ├── Validator.java                ← Centralised input validation
│       │   └── UITheme.java                  ← Colour palette, fonts, component factory
│       └── gui/
│           ├── MainFrame.java                ← Top-level window (CardLayout)
│           ├── LoginPanel.java               ← Login screen
│           ├── RegisterPanel.java            ← Customer self-registration
│           ├── customer/
│           │   └── CustomerDashboard.java    ← 4-tab customer portal
│           ├── staff/
│           │   └── StaffDashboard.java       ← 2-tab staff portal
│           └── manager/
│               └── ManagerDashboard.java     ← 4-tab manager portal
├── nbproject/
│   ├── project.xml
│   └── project.properties
├── build.xml                                 ← Ant build script
├── manifest.mf
└── README.md
```

---

## How to Open in Apache NetBeans

1. Launch **Apache NetBeans** (17 or later recommended)
2. Go to **File → Open Project**
3. Browse to and select the **BankCRM** folder
4. NetBeans will detect the Ant project automatically
5. Right-click the project → **Run** (or press **F6**)

> **Java version:** Requires Java 11 or later (project targets Java 11).

---

## Demo Accounts (Pre-loaded)

| Role     | Username   | Password          |
|----------|------------|-------------------|
| Manager  | `manager`  | `BankManager@324` |
| Staff    | `staff1`   | `Staff1@324`      |
| Staff    | `staff2`   | `Staff2@324`      |
| Customer | `customer1`| `Customer1@324`   |
| Customer | `customer2`| `Customer2@324`   |

---

## Functional Features

### Customer
- ✅ Register new account (with full validation)
- ✅ Login / Logout
- ✅ Submit support ticket (category, description, priority)
- ✅ View all personal tickets with status + response
- ✅ Search tickets by ID
- ✅ Close resolved ticket with star rating & feedback
- ✅ Update profile (name, email, phone, address)
- ✅ Change password (validates current, strength rules)
- ✅ Receive in-app notifications on status changes
- ✅ Mark notifications as read / Mark all read

### Staff
- ✅ Login / Logout
- ✅ View all tickets with filters (status, priority, category)
- ✅ Search tickets by keyword / customer name / ID
- ✅ Sort tickets (date, priority)
- ✅ Respond to ticket + update status in one action
- ✅ Update status independently
- ✅ Add / update internal remarks (hidden from customers)
- ✅ View complete ticket history audit trail
- ✅ Reopen closed/resolved tickets
- ✅ Performance counter (tickets handled)

### Manager
- ✅ Login / Logout
- ✅ Assign tickets to staff members
- ✅ View dashboard with full stats
- ✅ Monitor staff performance (handled ticket count)
- ✅ Add / Edit / Delete / Toggle-active staff accounts
- ✅ Reset staff passwords
- ✅ View all tickets with search, filter, sort
- ✅ Generate reports: by status, by priority, monthly summary
- ✅ Average response time calculation
- ✅ View customer feedback and ratings

### System
- ✅ Auto-generated unique ticket IDs (TKT-YYYY-NNNNN)
- ✅ Auto-generated user IDs
- ✅ Timestamp on all ticket events
- ✅ Full ticket history / audit trail
- ✅ Input validation everywhere (username, email, phone, password strength, description length)
- ✅ Pre-loaded sample data for instant demonstration

---

## Password Requirements
- Minimum 8 characters
- At least one **uppercase** letter
- At least one **lowercase** letter
- At least one **digit**
- At least one **special character** (e.g. `!@#$%^&*`)
