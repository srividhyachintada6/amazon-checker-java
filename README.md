# Amazon Availability & Price Monitoring System

An automated, end-to-end e-commerce product monitoring system built with **Java**, **Spring Boot 3**, **Jsoup**, **Selenium WebDriver**, and modern web technologies. The application periodically tracks Amazon product availability, detects price fluctuations, captures proof screenshots, and presents actionable analytics through a responsive dashboard.

---

## 📌 Problem Statement

E-commerce prices and stock availability on Amazon fluctuate constantly due to algorithmic repricing, flash sales, and inventory shifts. Manually checking product pages is tedious, inefficient, and often leads to missing limited-time discounts or stock replenishments. 

This project delivers an automated solution that monitors targeted products in the background, logs historical price changes, captures visual evidence via automated browser screenshots, and provides an interactive web interface for real-time tracking and product management.

---

## 🎯 Key Objectives

* **Automated Product Tracking**: Periodically scrape Amazon product pages to extract real-time stock status and pricing.
* **Price Fluctuation Detection**: Compute price deltas (drops, increases, unchanged) relative to recorded history.
* **Visual Audit Trail**: Capture headless Chrome screenshots of monitored product pages as verifiable proof.
* **Persistent Local Storage**: Maintain configuration and audit history in lightweight, portable CSV and log files without requiring external database setup.
* **Modern Web Dashboard**: Provide a responsive, real-time UI with KPI analytics, instant search, status filtering, multi-criteria sorting, and modal-based product management.
* **Robust Error Handling**: Handle Amazon rate-limiting, CAPTCHA, missing selectors, and network anomalies gracefully without crashing or leaking Java stack traces to the frontend.

---

## 🚀 Key Features

### 1. Product Management (CRUD)
* **Add Products**: Add products directly from the UI by providing a descriptive name and valid Amazon URL.
* **Edit & Update**: Modify existing product names and URLs seamlessly.
* **Remove Products**: Delete products from active monitoring with confirmation protection.
* **Validation & Uniqueness**: Enforces non-empty names, valid HTTP/HTTPS URLs, and prevents duplicate URLs from being registered.

### 2. Search, Filter & Multi-Criteria Sort
* **Live Search**: Instant client-side search filtering by product name with a single-click clear control.
* **Status Filter Tabs**: Filter items by *All*, *In Stock*, *Out of Stock*, or *Errors* with real-time count badges.
* **Sorting Options**: Sort products by Name (A–Z / Z–A), Price (Low to High / High to Low), or Last Checked timestamp.

### 3. Background Scheduler & On-Demand Checking
* **Automated Background Scheduler**: Built on Java's `ScheduledExecutorService` running periodic checks at configurable intervals (default: 30 minutes via `application.properties`).
* **Manual Trigger**: "Check All Products" and "Run Check Now" buttons allow immediate checks on demand.
* **Live Status Polling**: Displays current scheduler status, countdown to the next scheduled check, and animated checking indicators.

### 4. Price & Availability Tracking
* **Availability Analysis**: Detects stock statuses including "In Stock", "Out of Stock", "Currently Unavailable", and error states.
* **Price Difference Computation**: Calculates previous price and price change amount (e.g. `↓ ₹30.00` or `↑ ₹15.00`) directly from verified historical data.
* **Product Price History Modal**: View chronological price and availability points for each individual item.

### 5. Automated Screenshots & Security
* **Selenium Headless Chrome**: Uses WebDriverManager to automatically configure and drive headless Chrome, capturing page screenshots upon each check.
* **Secure Asset Serving**: Dedicated API endpoint restricts access strictly to the `screenshots/` directory, enforces `.png`/`.jpg` extensions, and prevents directory traversal attacks.

### 6. Dashboard Analytics & Global Audit History
* **6 Key Performance Indicators (KPIs)**:
  * Total Products Monitored
  * In Stock Count
  * Out of Stock Count
  * Error / Unavailable Count
  * Active Price Drops
  * Last Check Timestamp
* **Recent Monitoring Activity Table**: Chronological audit trail showing recent checks, product names, availability badges, recorded prices, and timestamps.
* **Raw Activity Log Drawer**: Collapsible raw log viewer reading directly from `data/availability_log.txt`.

---

## 🛠️ Technology Stack

| Layer | Technology |
|---|---|
| **Core Language** | Java 17+ (LTS) |
| **Build & Packaging** | Apache Maven, `maven-shade-plugin` (Uber JAR) |
| **Backend Framework** | Spring Boot 3.3.0 (`spring-boot-starter-web`) |
| **Scraping & HTML Parsing** | Jsoup 1.17.2 |
| **Browser Automation** | Selenium WebDriver 4.21.0, WebDriverManager 5.8.0 |
| **Frontend** | Semantic HTML5, Modern CSS3 (CSS Variables, Flexbox, CSS Grid), Vanilla JavaScript (ES6+) |
| **Data Persistence** | Flat-file CSV (`data/products.csv`) and text audit log (`data/availability_log.txt`) |

---

## 🏗️ Architecture & High-Level Flow

```
+-------------------------------------------------------------------------------+
|                             WEB DASHBOARD (Browser)                           |
|  - 6 KPI Analytics Cards        - Search, Filter & Sort Toolbar               |
|  - Product Cards with Actions   - Modals: Add, Edit, Delete, Price History    |
|  - Real-Time Status & Countdown - Global Monitoring Activity Audit Table      |
+---------------------------------------+---------------------------------------+
                                        | (REST JSON & Static Assets)
                                        v
+-------------------------------------------------------------------------------+
|                       SPRING BOOT REST CONTROLLER & SERVICE                   |
|                   (/api/products, /api/summary, /api/status, ...)              |
+-------------------+---------------------------------------+-------------------+
                    |                                       |
                    v                                       v
+---------------------------------------+   +-----------------------------------+
|            SCHEDULER RUNNER           |   |       DATA PERSISTENCE LAYER      |
|  - Background periodic checking       |   |  - data/products.csv              |
|  - Configurable interval (properties) |   |  - data/availability_log.txt      |
|  - On-demand manual trigger           |   |  - screenshots/                   |
+-------------------+-------------------+   +-----------------------------------+
                    |
                    v
+-------------------------------------------------------------------------------+
|                           SCRAPING & TRACKING ENGINE                          |
|  - ProductScraper (Jsoup HTTP GET + Desktop Headers + Fallback Selectors)    |
|  - PriceTracker & AvailabilityTracker (Stock & Price Regex Normalization)     |
|  - Screenshot Service (Selenium WebDriver + Headless Chrome)                 |
+-------------------------------------------------------------------------------+
                                        |
                                        v
                            [ Amazon.in Product Pages ]
```

---

## 📂 Project Structure

```
amazon-checker-java/
├── data/
│   ├── products.csv                  # Monitored product definitions (name, url)
│   └── availability_log.txt          # Chronological audit log of all checks
├── screenshots/                      # Captured page screenshots from headless Chrome
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── amazonchecker/
│       │           ├── Main.java                 # CLI runner entry point
│       │           ├── config/
│       │           │   ├── Headers.java          # Anti-bot HTTP request headers
│       │           │   └── Settings.java         # Project settings & path constants
│       │           ├── scraper/
│       │           │   ├── ProductScraper.java   # Jsoup-based Amazon page scraper
│       │           │   └── SearchScraper.java    # Fallback search scraper
│       │           ├── tracker/
│       │           │   ├── AvailabilityTracker.java  # Stock status detector
│       │           │   └── PriceTracker.java         # Price parser & delta tracker
│       │           ├── utils/
│       │           │   ├── CsvHandler.java       # Thread-safe CSV read/write
│       │           │   ├── LogWriter.java        # Audit logging utility
│       │           │   ├── MockParser.java       # Offline testing parser
│       │           │   ├── Product.java          # Product model
│       │           │   └── Screenshot.java       # Selenium headless screenshot taker
│       │           ├── scheduler/
│       │           │   └── SchedulerRunner.java  # ScheduledExecutor background runner
│       │           └── web/
│       │               ├── WebApplication.java   # Spring Boot application entry point
│       │               ├── WebMvcConfig.java     # Static resource & security configuration
│       │               ├── ProductService.java   # Business logic, validation & history parser
│       │               ├── DashboardController.java # REST API controller & exception handlers
│       │               └── dto/
│       │                   ├── ProductRequest.java       # Add/edit request body
│       │                   ├── ProductResponse.java      # Product detail DTO
│       │                   ├── SummaryResponse.java      # 6 KPI metrics DTO
│       │                   ├── HistoryEntryResponse.java # Audit log entry DTO
│       │                   └── PriceHistoryPoint.java    # Product price point DTO
│       └── resources/
│           ├── application.properties            # Scheduler and server configuration
│           └── static/
│               ├── index.html                    # Dashboard UI markup
│               ├── style.css                     # Modern CSS styles & responsive grid
│               └── app.js                        # Dashboard application logic & API clients
├── pom.xml                                       # Maven build configuration
└── README.md                                     # Project documentation
```

---

## ⚙️ Configuration

Application settings can be configured via `src/main/resources/application.properties`:

```properties
# Server Port
server.port=8080

# Automatic Product Monitoring Schedule (in minutes)
checker.schedule.minutes=30

# Spring Web Configuration
spring.application.name=amazon-availability-checker
```

* **`checker.schedule.minutes`**: Defines how often background checks run automatically (default: 30 minutes).
* **`server.port`**: Defines the HTTP port for the web dashboard (default: 8080).

---

## 📡 REST API Endpoints

| Method | Endpoint | Description | Sample Response / Status |
|---|---|---|---|
| `GET` | `/api/summary` | Returns 6 KPI summary metrics | `{ "totalProducts": 3, "inStock": 2, "outOfStock": 0, "errors": 1, "priceDrops": 1, ... }` |
| `GET` | `/api/products` | Returns all active monitored products with status & prices | `200 OK` (JSON array of products) |
| `POST` | `/api/products` | Adds a new product to monitoring and `products.csv` | `201 Created` / `400 Bad Request` / `409 Conflict` |
| `PUT` | `/api/products/{id}` | Updates an existing product's name or URL | `200 OK` / `400 Bad Request` / `404 Not Found` |
| `DELETE` | `/api/products/{id}` | Removes a product from active monitoring | `200 OK` (`{ "status": "success", ... }`) |
| `GET` | `/api/history?limit=25` | Returns chronological global monitoring audit entries | `200 OK` (JSON array of recent checks) |
| `GET` | `/api/products/{id}/history` | Returns price and availability history points for a product | `200 OK` (JSON array of price checkpoints) |
| `GET` | `/api/status` | Returns scheduler health and countdown timers | `{ "status": "automatic monitoring", "checking": false, ... }` |
| `POST` | `/api/check` | Triggers an immediate manual product check | `200 OK` (`{ "status": "started", ... }`) |
| `GET` | `/api/screenshots/{filename}` | Securely streams a captured screenshot image | `200 OK` (`image/png` or `image/jpeg`) |
| `GET` | `/api/log` | Returns the raw text from `data/availability_log.txt` | `200 OK` (`text/plain`) |

---

## 💻 How to Run

### Prerequisites
1. **Java 17 or higher** (`java -version`).
2. **Apache Maven 3.8 or higher** (`mvn -version`).
3. **Google Chrome** installed (required for headless screenshot generation).

### 1. Build the Application
Clone the repository and build the standalone runnable JAR using Maven:

```bash
mvn clean package -DskipTests
```

This compiles the project, bundles all dependencies, and creates the fat JAR at:
```
target/amazon-availability-checker.jar
```

### 2. Start the Web Dashboard
Run the packaged JAR:

```bash
java -jar target/amazon-availability-checker.jar
```

The Spring Boot server will start, launch the background scheduler, and serve the application on:
```
http://localhost:8080
```

Open `http://localhost:8080` in any modern web browser to access the dashboard.

### 3. Run in CLI-Only Mode (Optional)
To execute a one-time console check without starting the web dashboard:

```bash
java -cp target/amazon-availability-checker.jar com.amazonchecker.Main
```

---

## 🛡️ Error Handling & Security

* **No Stack Trace Leaks**: Custom `@ExceptionHandler` methods intercept all exceptions, returning standardized, user-friendly JSON payloads (`{ "status": "error", "message": "..." }`).
* **Path Traversal Protection**: The `/api/screenshots/{filename}` endpoint enforces strict whitelist validation, rejects URL-encoded traversal (`..`, `%2f`), and prevents reading files outside the designated `screenshots/` directory.
* **XSS Sanitization**: Frontend renders user and scraping data via an HTML entity sanitizer before DOM insertion.
* **Amazon Rate Limiting & Headless Detection**: Uses browser-like request headers and User-Agents to minimize bot detection; network timeouts and missing elements are converted into descriptive error statuses rather than uncaught exceptions.

---

## 🔮 Future Improvements

* **Email & Webhook Alerts**: Send notifications via SMTP or Discord/Slack webhooks when a monitored product drops below a target threshold.
* **Exporting**: One-click export of monitoring history to CSV or Excel.
* **Multi-Region Support**: Support checking Amazon domains across different regions (Amazon.com, Amazon.co.uk, Amazon.de).
* **Proxy Rotation**: Configurable HTTP/SOCKS5 proxy rotation to support high-frequency enterprise monitoring.

---

## 📄 License

This project is developed for educational and portfolio demonstration purposes. All product names, logos, and brands are property of their respective owners.
