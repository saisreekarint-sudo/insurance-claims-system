# 🛡️ Insurance Claims Processing System

A robust, enterprise-grade Spring Boot backend for managing the entire lifecycle of insurance claims—from policy validation to final settlement.

# Assumptions Made for the assignemt
`Policy Table: I have made a dummy policy table containing number of policies for the claims to exist`

`IT Guy Table: To record the audit of who made the changes and to also make it real life application, I made a table called "IT Guys" where once the claim is raised, the ticket gets asigned to a single IT person who is `AVAILABLE` at the instant.`

This project features an ***Automated Resource Allocation System*** that intelligently assigns incoming claims to available adjusters ("IT Guys") using a First-Come-First-Served (FCFS) scheduling algorithm, ensuring efficient workload management.

## 🚀 Key Features

* **Smart Claim Routing:** Automatically detects available staff and assigns claims upon registration.
* **Workflow State Machine:** Enforces strict status transitions (e.g., `REGISTERED` → `IN_REVIEW` → `APPROVED` → `SETTLED`).
* **Role-Based Access Control (RBAC):** Prevents unauthorized access (e.g., "Bob" cannot settle "Alice's" assigned claim).
* **Automated Fraud Detection:** Flags claims exceeding 70% of the policy limit for manual `FRAUD_REVIEW`.
* **Fraud case handling:** The claims that are marked fraud can be made into review and then be approved/rejected manually by the particular IT Guy.
* **Full Audit Trail:** Logs every action, status change, timestamp for compliance and also the details of the IT guy handled .
* **Dynamic Settlement:** Supports full or partial settlements with automatic resource release upon completion.
* **Workload Balance for IT Guys** One IT Guy is only assigned one claim at a time. By FCFS bases we check if the guy is available to work and assign him the ticket.

## 🛠️ Tech Stack

* **Core:** Java 21, Spring Boot 3.x
* **Data:** Spring Data JPA (Hibernate), MySQL/H2 Database
* **Validation:** Jakarta Bean Validation
* **Tools:** Lombok, Maven
* **Testing:** Postman

---

## 🏗️ Architecture & Design

### Class Diagram
The system follows a layered architecture (Controller -> Service -> Repository).

<img width="8192" height="3042" alt="Untitled diagram-2026-01-14-045419" src="https://github.com/user-attachments/assets/a998599f-3a37-42c5-ad8b-a73523767228" />

### ER-Diagram

The system maintains data integrity using Foreign Keys and Unique Constraints.

<img width="4439" height="5263" alt="Untitled diagram-2026-01-14-045519" src="https://github.com/user-attachments/assets/5f71f735-2d1a-4c74-bca7-981dfcb04186" />

---

## 🏃‍♂️ Getting Started

### Prerequisites
* Java 17+
* Maven
* Postman (for testing)

### Installation
1.  **Clone the repository**
    ```bash
    git clone [https://github.com/yourusername/insurance-claims-system.git](https://github.com/yourusername/insurance-claims-system.git)
    ```
2.  **Build the project**
    ```bash
    mvn clean install
    ```
3.  **Run the application**
    ```bash
    mvn spring-boot:run
    ```
    *The app will start on `http://localhost:8080`*

---

## 🧪 API Demo Script (The "Alice & Bob" Story)

Use this flow to demonstrate the **Auto-Scheduling** and **Security** features.

### 1. View Workforce Status
Check who is available to work.
* **GET** `/api/it-guys`
* *Result:* Alice and Bob are `AVAILABLE`.

### 2. The Auto-Assignment (Scheduling Algorithm)
Register a new claim. The system will instantly assign it to the first free worker.
* **POST** `/api/claims`
    ```json
    {
      "policyNumber": "POL-12345",
      "claimType": "ACCIDENT",
      "claimAmount": 5000.0,
      "description": "Fender bender"
    }
    ```
* *Result:* Response shows `"assignedITGuyName": "Alice (Senior)"`. Alice is now **BUSY**.

### 3. Load Balancing
Register a **second** claim immediately.
* **POST** `/api/claims`
* *Result:* Since Alice is busy, the system assigns this to `"assignedITGuyName": "Bob (Junior)"`.

### 4. Security Check (Ownership Validation)
Try to make Bob update Alice's claim.
* **PATCH** `/api/claims/{ALICE_CLAIM_ID}/status?status=APPROVED&requestorName=Bob (Junior)`
* *Result:* **400 Bad Request** - *"Access Denied. This claim is assigned to Alice."*

### 5. Settlement & Release
Alice finishes her job.
* **PUT** `/api/claims/{ALICE_CLAIM_ID}/settle?requestorName=Alice (Senior)`
* *Result:* Claim is `SETTLED`. Alice is automatically marked `AVAILABLE` again to take new tasks.

---

## 📚 API Reference

### Claims
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/claims` | Register a new claim (Auto-assigns IT Guy) |
| `PATCH` | `/api/claims/{id}/status` | Update status (Requires owner name) |
| `PUT` | `/api/claims/{id}/settle` | Settle claim (Amount optional) |
| `GET` | `/api/claims/{id}/audit` | View full history log |

### Workforce (IT Guys)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/it-guys` | List all staff and their status |
| `POST` | `/api/it-guys/batch` | Bulk hire new staff |
| `PUT` | `/api/it-guys/reset` | **Emergency:** Reset everyone to AVAILABLE |
| `DELETE` | `/api/it-guys/{id}` | Fire an IT Guy (If not busy) |

### Policies
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/policies` | View active policies |
| `POST` | `/api/policies/batch` | Bulk upload policies |

---

## Postman Apis for clearer understanding

[Postman Apis](https://sai-sreekar-int-297043.postman.co/workspace/insurance-claim~7589b6dc-df4a-4f96-913c-5a7bf64f7d90/collection/51303689-a478e7d3-65c1-44b1-bd1a-cfc0750048e6?action=share&creator=51303689&active-environment=51303689-a42bee5b-d470-45ff-ad82-593e0ea23b8c)

**Please note:** Environment variables :- 

Endpoint: `localhost:8080/api/claims`

Policies: `localhost:8080/api/policies`





