Here’s a tailored `README.md` for your current setup:

---

# 🏥 Charaka Hospital Management System

A secure, multi-tenant hospital management system with full audit tracking and encrypted data storage. Built with Java Spring Boot using microservices architecture.

## 📦 Microservices Currently Implemented

### 1. **User Management Service**

Handles:

* Authentication (JWT-based)
* Role-based authorization
* Onboarding:

  * Super Admins
  * Hospital Admins (1 per hospital)
  * Telecallers (up to 2 hospitals max)
  * Receptionists
  * Doctors
* Telecaller-to-Hospital assignments with active/inactive status
* Admin-level user management and permissions

### 2. **Healthcare Service**

Handles:

* Encrypted patient data storage (name, phone, etc.)
* Patient enquiry creation and updates
* Appointment creation and mapping to hospital + doctor
* Hospital-specific patient and doctor access
* Auditing for all updates (`@CreatedBy`, `@LastModifiedBy`)

---

## 🔒 Security

* Uses JWT token for authentication.
* Role-based authorization using Spring Security.
* Patient data encrypted in DB using JPA `@AttributeConverter`.

---

## 🧠 Roles and Permissions

| Role           | Capabilities                                                                 |
| -------------- | ---------------------------------------------------------------------------- |
| Super Admin    | Onboard hospitals, assign Hospital Admins                                    |
| Hospital Admin | Manage users in a specific hospital, assign telecallers, enable/disable them |
| Telecaller     | Manage patient data for up to 2 hospitals                                    |
| Doctor         | View and update clinical information for assigned appointments               |
| Receptionist   | Manage front-desk and schedule appointments                                  |

---


## 🛠️ Tech Stack

* Java 17
* Spring Boot 3.x
* Spring Data JPA
* Spring Security
* MySQL
* Lombok
* Feign Client for inter-service communication

---

## 🔄 Workflows

### 🔹 Hospital Onboarding

1. Super Admin creates a new hospital.
2. Assigns a Hospital Admin.
3. Hospital Admin can assign telecallers and set status.

### 🔹 Patient Management

1. Telecaller adds patient enquiry (encrypted).
2. Appointment created and assigned to a doctor.
3. Doctor updates appointment info post-consultation.

---

## 📝 How to Run

1. Set up MySQL DB and configure `application.yml` in each service.
2. Build and run each service independently:

```bash
./mvnw clean spring-boot:run
```

3. Use Postman or frontend app to interact with services.

---

## 📌 Notes


* Database encryption is handled via `@AttributeConverter`.
* Auditing enabled via Spring Data JPA.

---
