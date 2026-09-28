# TransitAssist — Accessible Transit Assistance Booking

> **Empowering inclusive, dignified, and reliable campus mobility for persons with disabilities.**

---

## 📌 Problem Statement

Persons with disabilities on campus frequently require dedicated transit assistance to navigate educational, residential, and transit facilities safely and on time. Common assistance needs include:
- **Wheelchair assistance** across campus terrains and stairs
- **Escort assistance** for visually impaired or neurodivergent campus members
- **Shuttle transit assistance** connecting hostel blocks and transit gates
- **Boarding assistance** for accessible buses and campus shuttles

Historically, users have had to rely on informal, last-minute requests or uncertain verbal coordination, leading to delays, missed lectures, and safety concerns. **TransitAssist** provides a modern, centralized, web-based platform where users can schedule transit assistance in advance, and staff coordinators can assign verified helpers while strictly preventing schedule overlaps and workload bottlenecks.

---

## 🚀 Core Features

1. **Advance Assistance Booking**: Users book assistance trips by specifying pickup location, travel date, travel time, assistance type, and special trip notes.
2. **Intelligent Helper Assignment**: Staff coordinators review requests and assign available campus helpers with automatic conflict and overlap checks.
3. **Real-Time Request Lifecycle Tracking**: Track trip progress transparently through `REQUESTED` ➔ `ASSIGNED` ➔ `COMPLETED` (or `CANCELLED`).
4. **Controlled Request Cancellation**: Requesters can cancel their pending or assigned trips anytime before completion. Completed trips are strictly protected from cancellation or reassignment.
5. **Daily Helper Workload Management**: Staff and administrators monitor real-time helper schedules, today's trip counts, and availability statuses (`AVAILABLE`, `BUSY`, `OFFLINE`).
6. **Multi-Tier Role Security**: Role-based access control for `USER`, `STAFF`, and `ADMIN` users with BCrypt password hashing.
7. **Accessibility & Responsive Design**: Keyboard accessible, high contrast, mobile-friendly (320px–1440px), with persistent **Light / Dark Mode** support.
8. **RESTful API & Postman Suite**: Complete REST APIs with standard HTTP status codes, structured JSON responses, and a preconfigured Postman collection.

---

## ⚙️ Technology Stack

- **Backend**: Java 17+, Spring Boot 3.4.3, Spring MVC, Spring Data JPA, Spring Security, Spring Validation, Maven
- **Database**: MySQL 8.0+ (with MySQL Connector/J) & Hibernate ORM, H2 In-Memory DB (profile for test/demo)
- **Frontend**: Thymeleaf, HTML5, CSS3 Custom Properties (Design System), Vanilla JavaScript
- **API Testing**: Postman Collection (v2.1)

---

## 🏛️ System Architecture

TransitAssist strictly adheres to a layered software architecture:

```
src/main/java/com/madhan/TransitAssist/
├── controller/         # Web/Thymeleaf UI Controllers (Home, Auth, User, Request, Staff, Helper, Admin)
├── restcontroller/     # REST API Controllers (Auth, Requests, Staff, Helpers, Admin)
├── model/              # JPA Entities (User, Helper, AssistanceRequest) & Enums (Role, RequestStatus, etc.)
├── repository/         # Spring Data JPA Repositories
├── service/            # Core Business Logic & Rule Enforcement Services
├── dto/                # Data Transfer Objects & Request/Response Payloads
├── security/           # Spring Security Config, CustomUserDetailsService, Handlers
├── config/             # DataInitializer (Seeds Admin, Staff, Helpers, and Test User)
└── exception/          # Global Exception Handlers & Custom Business Exceptions
```

---

## 🔒 Business Rules (Enforced in Service Layer)

The following domain rules are enforced in the **Service Layer** (`AssistanceRequestService.java`), independent of UI validations:

* **RULE 1 (Overlap Prevention)**: A helper cannot be assigned to two overlapping-time requests. Before assigning, the service validates existing assignments on the same travel date. If another assignment exists within a 60-minute window, the assignment is rejected with:
  > *"Helper cannot be assigned because they already have another request during this time."*
* **RULE 2 (Completed Request Immutability)**: A `COMPLETED` request cannot be cancelled or reassigned. Any attempt immediately aborts without database modification:
  > *"Completed requests cannot be cancelled."* / *"Completed requests cannot be reassigned."*
* **RULE 3 (Ownership Verification)**: A normal user can cancel **only their own** requests. Unauthorized cancellation attempts return HTTP 403 / access exception.
* **RULE 4 (Cancellation Lifecycle)**: Users cannot cancel completed or already cancelled requests.
* **RULE 5 (Availability Check)**: Only helpers with `AVAILABLE` status can be assigned. Helpers marked `BUSY` or `OFFLINE` are rejected. Upon assignment, the helper status is set to `BUSY`.
* **RULE 6 (Role-Based Authorization)**: Only `STAFF` and `ADMIN` users can assign helpers, mark trips completed, and manage helper profiles.
* **RULE 7 (Data Integrity)**: Required trip information (pickup location, future or present date, travel time, assistance type) must be valid prior to persistence.

---

## 🗄️ Database Setup & MySQL Configuration

### 1. Create MySQL Database
Log in to your local MySQL server (via MySQL Workbench, phpMyAdmin, or terminal) and execute:

```sql
CREATE DATABASE transitassist;
```

### 2. Configure Database Credentials
Open `src/main/resources/application.properties` and enter your MySQL root password:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/transitassist?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
# Enter your MySQL password below:
spring.datasource.password=YOUR_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

> **Note**: For zero-dependency testing without a local MySQL service running, you can activate the built-in H2 in-memory profile by passing `--spring.profiles.active=h2`.

---

## 🔑 Default Credentials for Testing

The system automatically initializes default accounts via `DataInitializer.java` on initial startup:

| Role | Email | Password | Permissions |
| :--- | :--- | :--- | :--- |
| **ADMIN** | `admin@transitassist.com` | `Admin@123` | Full system access, user governance, staff creation, all requests & helpers |
| **STAFF** | `staff@transitassist.com` | `Staff@123` | Request management, helper assignment, workload tracking, complete trips |
| **USER** | `madhan@example.com` | `Password@123` | Book transit assistance, view dashboard, track & cancel own trips |

> **Interactive Login**: The login page (`/login`) includes **1-click quick-fill buttons** for each test persona for instant evaluation.

---

## 🏃 How to Run the Application

From the project root directory (`TransitAssist/TransitAssist/TransitAssist`):

### On Windows:
```cmd
mvnw.cmd spring-boot:run
```
*(Or with the H2 profile if MySQL is not currently running)*:
```cmd
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=h2
```

### On macOS / Linux:
```bash
./mvnw spring-boot:run
```

Once the application starts, navigate to:
👉 **`http://localhost:8080`**

---

## 📡 REST API Endpoints

All APIs return standardized JSON responses formatted as:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2026-09-28T18:30:00"
}
```

### Authentication Endpoints
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Public | Register new user account |
| `POST` | `/api/auth/login` | Public | Authenticate user and initiate session |
| `POST` | `/api/auth/logout` | Authenticated | Terminate session |

### Assistance Requests Endpoints
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/requests` | Authenticated | Create a new assistance booking |
| `GET` | `/api/requests` | Authenticated | Get all requests (user's own or all if staff) |
| `GET` | `/api/requests/{id}` | Authenticated | Get assistance request details by ID |
| `PUT` | `/api/requests/{id}/cancel` | Authenticated | Cancel request (Rule 2, 3 & 4 enforced) |
| `PUT` | `/api/requests/{id}/complete`| STAFF / ADMIN | Mark assigned trip as completed |

### Staff Operations Endpoints
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/staff/requests` | STAFF / ADMIN | List all requests (supports `?status=` and `?date=`) |
| `PUT` | `/api/staff/requests/{requestId}/assign/{helperId}` | STAFF / ADMIN | Assign helper to request (Rule 1 & 5 enforced) |
| `GET` | `/api/staff/workload` | STAFF / ADMIN | View helper workload schedule (supports `?date=`) |

### Helper Management Endpoints
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/helpers` | STAFF / ADMIN | Register a new campus assistant |
| `GET` | `/api/helpers` | Authenticated | List all helpers (supports `?availableOnly=true`) |
| `GET` | `/api/helpers/{id}` | Authenticated | Get helper details by ID |
| `PUT` | `/api/helpers/{id}` | STAFF / ADMIN | Update helper info / availability |
| `DELETE` | `/api/helpers/{id}` | STAFF / ADMIN | Delete helper (rejects if active tasks exist) |

### Admin Endpoints
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/admin/users` | ADMIN | List all registered users and staff |
| `PUT` | `/api/admin/users/{id}/status` | ADMIN | Enable or disable user account |
| `POST` | `/api/admin/staff` | ADMIN | Create a new staff coordinator account |
| `GET` | `/api/admin/stats` | ADMIN | Global system metrics and analytics |

---

## 🧪 Postman API Testing

A complete Postman collection is included in the project root:
📄 **`TransitAssist_API.postman_collection.json`**

### Steps to Test in Postman:
1. Open Postman and click **Import**.
2. Select `TransitAssist_API.postman_collection.json` from the project directory.
3. The collection includes the environment variable `{{baseUrl}} = http://localhost:8080`.
4. Run requests in sequence across the organized folders:
   - **1. Authentication**: Register, Login (User, Staff, Admin), Logout
   - **2. Assistance Requests**: Create, Get by ID, Cancel, Complete
   - **3. Staff**: View all requests, Assign helper
   - **4. Helpers**: Create, View, Update, Delete
   - **5. Workload**: Daily helper workload breakdown
   - **6. Admin**: User governance, Create staff, System stats
   - **7. Edge Cases & Business Rules**:
     - Overlapping assignment test (Rule 1 ➔ 400 Bad Request)
     - Cancel completed request test (Rule 2 ➔ 400 Bad Request)
     - Reassign completed request test (Rule 2 ➔ 400 Bad Request)
     - Normal user accessing staff endpoint (Rule 6 ➔ 403 Forbidden)
     - Duplicate registration check (➔ 400 Bad Request)

---

## 🎨 UI Pages & Design Walkthrough

1. **Landing Page (`/`)**: Hero section, key accessibility features, and direct role-based action routes.
2. **Login Page (`/login`)**: Secure sign-in with 1-click test credential auto-fill buttons.
3. **Registration Page (`/register`)**: Student onboarding with full validation feedback.
4. **User Dashboard (`/dashboard`)**: Metric cards (`Total`, `Requested`, `Assigned`, `Completed`), recent requests table with status badges and quick actions.
5. **Request Assistance (`/requests/new`)**: Accessible booking form with assistance type selection, location picker, date/time pickers, and description notes.
6. **My Requests (`/requests/my`)**: Filterable requests view with status tabs and cancellation triggers.
7. **Request Details (`/requests/{id}`)**: Step-by-step visual timeline tracker (`Requested` ➔ `Assigned` ➔ `Completed`), helper card, and contextual actions.
8. **Staff Dashboard (`/staff/dashboard`)**: Operational metrics, helper status counters, today's workload table, and recent bookings.
9. **Staff Request Management (`/staff/requests`)**: Filter by status/date, assign helper modal, and mark completed triggers.
10. **Helper Management (`/staff/helpers`)**: Add/edit helpers, toggle availability (`AVAILABLE`, `BUSY`, `OFFLINE`), and safe deletion.
11. **Helper Workload (`/staff/helpers/workload`)**: Schedule view displaying daily trip counts and time slots for each assistant.
12. **Admin Dashboard (`/admin/dashboard`)**: High-level platform statistics, user count, request distribution, and quick links.
13. **User Governance (`/admin/users`)**: User listing, account disable/enable toggle, and Add Staff modal.
14. **User Profile (`/profile`)**: Account information and verification status.
15. **Access Denied (`/access-denied`)**: Dedicated 403 error page with recovery navigation.

---

## 🔮 Future Enhancements

- **Real-Time GPS Tracking**: Live location tracking for campus shuttles and helpers.
- **SMS & Push Notifications**: Automated WhatsApp / SMS alerts when a helper is assigned.
- **Feedback & Rating System**: Ratings and comments from requesters to maintain helper service quality.
- **Multi-language Support**: Screen-reader optimized voice navigation and regional language prompts.
