
---

# 🛒 Juvarya B2B – Full Backend Project Overview 

---

## 🎯 **What Is Juvarya B2B?**

Juvarya B2B is a backend system similar to **Swiggy or Zomato**, built for restaurant ordering and product management. It supports order placement, real-time tracking, notifications, search, reporting, and payment flow — optimized for B2B operations like restaurants or kitchen outlets.

---

## 🧱 **Tech Stack**

* **Java 17 + Spring Boot** – Core backend services
* **Kafka** – Real-time message communication
* **MySQL / PostgreSQL** – Primary database
* **Docker & Docker Compose** – Local environment
* **Swagger** – API documentation
* **ELK Stack (Elasticsearch, Logstash, Kibana)** – Logging and monitoring
* **Razorpay** – Payment integration
* **Apache POI** – Excel report generation

---

## 🧩 **Modules Explained**

### 1. 👤 **User Management**

* Users register and log in using mobile number + OTP
* OTP is triggered and verified through custom endpoints
* Once logged in, users can place orders, view history, and make payments

---

### 2. 🧾 **Order Management**

* Users can:

    * Create new orders
    * Add multiple items per order
    * View past orders
    * Cancel or reorder previous ones
* Each order stores:

    * Table info (if dine-in)
    * Order time, status, total amount
* Order statuses: `PENDING`, `IN_PROGRESS`, `DELIVERED`, `CANCELLED`, etc.
* Notifications are triggered to restaurants and users

> **Example**: User places an order → Order saved → Restaurant receives KOT (Kitchen Order Ticket) → Restaurant updates status → User gets real-time updates

---

### 3. 🍔 **Product Management**

* Admin or restaurant adds products (veg, non-veg)
* Products can be searched with:

    * Restaurant ID
    * Food type (`veg`, `non-veg`, `all`)
    * Keyword
* Each product includes:

    * Name, price, availability
    * Category (e.g., starters, desserts)

---

### 4. 💳 **Payment Integration**

Handled via **Razorpay**.

#### Key Features:

* User makes payment via UPI, card, or Razorpay checkout
* On successful payment:

    * Order is marked as paid
    * Kafka notification is triggered to user
    * Notification includes payment amount, time, and order ID
* On payment failure:

    * Order stays in `PENDING` or `FAILED` state
    * Retry or cancel options allowed
* If a restaurant **cancels** an already paid order:

    * Refund is initiated using Razorpay Refund API
    * User receives refund status update

> Notifications use templates like:
> “💰 Payment Received: Your payment of ₹149.00 was successful.”

---

### 5. 📢 **Real-Time Notifications via Kafka**

Kafka topics used:

* `order.created` – Notifies restaurant
* `order.status.updated` – Notifies user
* `payment.success` – Notifies user
* `order.cancelled` – Notifies both sides

Each consumer listens and pushes messages to respective users or systems.

---

### 6. 📈 **Excel/PDF Reporting**

* Download sales reports by date range, restaurant, or frequency
* Item-wise and outlet-wise summary
* Fields include: Item Name, Quantity Sold, Tax, Discounts, Total Revenue
* Apache POI is used to generate `.xlsx` files

> Example: Manager downloads **June 2025 item-wise report** with top 10 items sold and total sales amount.

---

### 7. 📊 **Logging & Monitoring (ELK Stack)**

To track all events and logs in real-time:

* **Logstash** collects Spring Boot logs
* **Elasticsearch** indexes logs for search
* **Kibana** lets you search logs like:

    * “All failed payments today”
    * “Top restaurants with delayed orders”
    * “Orders with status not updated after 1hr”

Access:

* Elasticsearch: `http://localhost:9200`
* Kibana: `http://localhost:5601`
* View structured logs and dashboards

---

### 8. 📡 Swagger UI (API Testing)

Every backend module includes a Swagger UI:

* Try APIs like `POST /api/orders/create`
* Trigger OTP login flow
* Test Razorpay payment creation
* View product search filters

Run locally at:

```
http://localhost:{port}/swagger-ui.html
```

---

## 🧾 Example Flow (Swiggy-like User Journey)

1. User logs in using OTP
2. Opens product catalog (veg, non-veg filtering)
3. Adds items to order and places it
4. Chooses to **pay online** → Redirects to Razorpay
5. On success, system:

    * Marks order paid
    * Sends confirmation message
    * Pushes order to restaurant
6. Restaurant receives and accepts order
7. User tracks order until delivery
8. All actions logged in ELK; summary available in reports

---

## ✅ Summary

Juvarya B2B is a **complete backend system** for restaurant and food order operations, offering:

* Clean and fast **order flow**
* Razorpay-based **payment handling with refunds**
* Real-time **status tracking and notifications**
* Restaurant and product-level control
* Powerful **report generation**
* Full observability using the **ELK stack**


