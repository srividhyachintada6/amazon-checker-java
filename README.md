# Multi-Store E-Commerce Availability & Price Monitoring System

An automated, end-to-end e-commerce product monitoring system built with **Java 17**, **Spring Boot 3**, **Jsoup**, **Selenium WebDriver**, and modern web technologies. The application periodically tracks product availability and prices across **Amazon** and **Flipkart**, detects price fluctuations, captures verifiable visual screenshots via headless Chrome, renders interactive SVG price trend charts, and provides real-time analytics through a responsive dashboard.

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

### 1. Multi-Store Monitoring & Price Comparison
* **Unified Multi-Store Engine**: Extensible `ProductStoreScraper` interface supporting both **Amazon** and **Flipkart**, with automatic store detection from URLs.
* **Side-by-Side Store Comparison**: Live cross-store comparison drawer querying Amazon and Flipkart simultaneously to find best prices and deals.

### 2. Smart Product Management & On-Demand Checking
* **3-in-1 Smart Add**: Add products via Direct Link, Live Store Search (by product title/keywords), or Drag-and-Drop Image upload (with automatic name extraction).
* **On-Demand Single Product Checking**: Check any individual product with a single click (`POST /api/products/{id}/check`), displaying animated states (`Checking...` → `✔ Success` or `✖ Failed`).
* **Complete CRUD Lifecycle**: Create, edit, and safely delete products with confirmation modals.

### 3. Price History & Pure SVG Price Trend Chart
* **Price Difference Engine**: Accurately detects price drops (`↓ ₹30.00`), price increases (`↑ ₹15.00`), and unchanged prices against previous checks without inventing mock numbers.
* **Interactive SVG Trend Chart**: A lightweight, zero-dependency inline SVG area and line chart rendered dynamically in vanilla JS, complete with min/max price boundaries and hover tooltips.
* **Audit History**: Complete chronological log of check timestamps, availability states, and recorded prices.

### 4. Background Monitoring & Control
* **Scheduled Background Execution**: Periodic checks run at configurable intervals (default: 30 minutes) via Java's `ScheduledExecutorService`.
* **Dynamic Scheduler Toggle**: Enable or disable automated checks on the fly from the UI or REST API (`POST /api/scheduler/toggle`).
* **Live Countdown Ticker**: Real-time 1-second countdown ticker displaying time remaining until the next automated check.
* **Tab-Visibility Aware**: Background polling automatically pauses when the browser tab is hidden and resumes immediately when focused.

### 5. 6 Live KPI Analytics Cards
* **Total Products**: Active monitored product count.
* **In Stock**: Number of items currently available for purchase.
* **Out of Stock**: Number of items out of stock or unavailable.
* **Stores Monitored**: Active store channels (Amazon, Flipkart).
* **Last Check**: Exact timestamp of the most recent check.
* **Next Check**: Scheduled time for the upcoming automated scan.

### 6. Full Search, Filter & Multi-Criteria Sort
* **Live Search**: Instant case-insensitive filtering by product title.
* **Store Filtering**: Filter by All Stores, Amazon, or Flipkart.
* **Availability Filtering**: Filter by All, In Stock, Out of Stock, or Errors.
* **Comprehensive Sorting**: Sort by Last Checked (Newest / Oldest), Newest Added, Price (Low to High / High to Low), or Product Name (A–Z / Z–A).

### 7. Responsive & Accessible UI
* **Zero Horizontal Scroll**: Strictly verified across 320px, 375px, 425px, 768px, 1024px, and 1440px viewport widths.
* **Mobile-First Touch Targets**: 44px minimum target sizes for buttons, inputs, and tabs.
* **Accessibility (a11y)**: Focus rings (`:focus-visible`), ARIA attributes, semantic headings, and high-contrast color palette.

---

## 🛠️ Technology Stack

| Layer | Technology |
|---|---|
| **Core Language** | Java 17+ (LTS) |
| **Build & Packaging** | Apache Maven, `maven-shade-plugin` (Uber JAR) |
| **Backend Framework** | Spring Boot 3.3.0 (`spring-boot-starter-web`, `spring-boot-starter-data-jpa`) |
| **Database & Persistence**| PostgreSQL (Production) / H2 (Local file fallback), Hibernate 6 |
| **Scraping & HTML Parsing** | Jsoup 1.17.2 |
| **Browser Automation** | Selenium WebDriver 4.21.0, WebDriverManager 5.8.0 |
| **Frontend** | Semantic HTML5, Modern CSS3 (CSS Variables, Flexbox, CSS Grid), Vanilla JavaScript (ES6+) |
| **Frontend Cloud Hosting** | Vercel (`vercel.json` static CDN with optional proxy rewrites) |
| **Backend Cloud Hosting** | Render, Railway, AWS ECS, or Docker containers |

---

## 🏗️ Architecture & High-Level Flow

```
+-------------------------------------------------------------------------------+
|                             WEB DASHBOARD (Browser)                           |
|  - 6 KPI Analytics Cards        - Search, Filter & Sort Toolbar               |
|  - Product Cards with Actions   - Modals: Add, Edit, Delete, Price History    |
|  - Real-Time Status & Countdown - Global Monitoring Activity Audit Table      |
|  - Cloud Backend Config Modal   - Automatic API URL Switcher                  |
+---------------------------------------+---------------------------------------+
                                        | (Static Assets: Vercel CDN)
                                        | (REST JSON via HTTPS)
                                        v
+-------------------------------------------------------------------------------+
|                       SPRING BOOT REST CONTROLLER & SERVICE                   |
|       (/api/products, /api/summary, /api/status, /api/health, ...)            |
+-------------------+---------------------------------------+-------------------+
                    |                                       |
                    v                                       v
+---------------------------------------+   +-----------------------------------+
|            SCHEDULER RUNNER           |   |       PERSISTENCE LAYER (JPA)     |
|  - Background periodic checking       |   |  - PostgreSQL (Production)        |
|  - Configurable interval (env vars)   |   |  - H2 Database (Local Dev)        |
|  - On-demand manual trigger           |   |  - Dual-write CSV backup log      |
+-------------------+-------------------+   +-----------------------------------+
                    |
                    v
+-------------------------------------------------------------------------------+
|                           SCRAPING & TRACKING ENGINE                          |
|  - ProductScraper (Jsoup HTTP GET + Desktop Headers + Fallback Selectors)    |
|  - PriceTracker & AvailabilityTracker (Stock & Price Regex Normalization)     |
|  - Screenshot Service (Selenium WebDriver + Headless Chrome --no-sandbox)     |
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
│       │                   ├── SearchResultResponse.java # Amazon search result item DTO
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

## ⚙️ Configuration & Environment Variables

Application settings can be configured via environment variables or `src/main/resources/application.properties`. See `.env.example` for all configurable keys.

| Variable | Default | Purpose |
|---|---|---|
| `PORT` | `8080` | HTTP port used by the Spring Boot server (dynamic on cloud platforms) |
| `DATABASE_URL` | `jdbc:h2:file:./data/amazon_checker_db...` | JDBC URL for PostgreSQL (Production) or H2 (Local file) |
| `DATABASE_USERNAME` | `sa` | Database username |
| `DATABASE_PASSWORD` | *(empty)* | Database password |
| `FRONTEND_URL` | `http://localhost:3000,http://localhost:8080,https://*.vercel.app` | Comma-separated allowed CORS origins |
| `CHECKER_SCHEDULE_MINUTES` | `30` | Background monitoring interval in minutes |

---

## 📡 REST API Endpoints

| Method | Endpoint | Description | Sample Response / Status |
|---|---|---|---|
| `GET` | `/api/summary` | Returns 6 KPI summary metrics (including stores & next check) | `{ "totalProducts": 3, "inStock": 2, "outOfStock": 1, "storesMonitored": 2, "nextCheck": "..." }` |
| `GET` | `/api/products` | Returns all active monitored products with prices & deltas | `200 OK` (JSON array of products) |
| `POST` | `/api/products/{id}/check` | On-demand check of an individual product | `200 OK` (Updated ProductResponse) |
| `GET` | `/api/products/{id}/history` | Returns price and availability checkpoints for a product | `200 OK` (JSON array of price checkpoints) |
| `POST` | `/api/products` | Adds a new product to monitoring and persistent database | `201 Created` / `400 Bad Request` / `409 Conflict` |
| `PUT` | `/api/products/{id}` | Updates an existing product's name, URL, or store | `200 OK` / `400 Bad Request` / `404 Not Found` |
| `DELETE` | `/api/products/{id}` | Removes a product from active monitoring | `200 OK` (`{ "status": "success", ... }`) |
| `GET` | `/api/products/search` | Live search across Amazon or Flipkart (`?query=...&store=...`) | `200 OK` (JSON array of search items) |
| `GET` | `/api/products/compare` | Compares product prices across stores (`?query=...`) | `200 OK` (PriceComparisonResponse) |
| `GET` | `/api/status` | Returns scheduler health, running state, and countdown | `{ "status": "automatic monitoring", "checking": false, ... }` |
| `POST` | `/api/check` | Triggers an immediate manual check of all products | `200 OK` (`{ "status": "started", ... }`) |
| `POST` | `/api/scheduler/toggle` | Toggles automatic background monitoring on or off | `200 OK` (`{ "enabled": false, ... }`) |
| `GET` | `/api/history?limit=25` | Returns chronological global monitoring audit entries | `200 OK` (JSON array of recent checks) |
| `GET` | `/api/screenshots/{filename}` | Securely streams a captured screenshot image | `200 OK` (`image/png` or `image/jpeg`) |
| `GET` | `/api/health` | Service health, database status, and scheduler metrics | `200 OK` (`{ "status": "UP", "database": "CONNECTED", ... }`) |
| `GET` | `/api/log` | Returns the raw text audit log | `200 OK` (`text/plain`) |

---

## 🗄️ Database Schema

The persistent storage layer utilizes PostgreSQL managed via Spring Data JPA with automatic DDL updates (`spring.jpa.hibernate.ddl-auto=update`), falling back to local H2 in development.

### 1. `monitored_products` Table
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `VARCHAR(64)` | `PRIMARY KEY` | Product unique identifier / slug |
| `name` | `VARCHAR(255)` | `NOT NULL` | Product display name |
| `product_url` | `VARCHAR(1000)` | `NOT NULL, UNIQUE` | E-commerce target URL |
| `store` | `VARCHAR(32)` | `NOT NULL` | Store identifier (`AMAZON`, `FLIPKART`) |
| `created_at` | `TIMESTAMP` | `NOT NULL` | Registration timestamp |

### 2. `audit_logs` Table
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGSERIAL` | `PRIMARY KEY` | Auto-increment audit record ID |
| `product_name` | `VARCHAR(255)` | `NOT NULL` | Monitored product name |
| `status` | `VARCHAR(64)` | `NOT NULL` | Availability status (`IN STOCK`, `OUT OF STOCK`, etc.) |
| `price` | `NUMERIC(10,2)` | `NULLABLE` | Scraped price at check time |
| `timestamp` | `TIMESTAMP` | `NOT NULL` | Execution timestamp |
| `screenshot_path` | `VARCHAR(255)` | `NULLABLE` | Relative path to screenshot artifact |

---

## 🌐 Cloud Deployment Guide

The application separates the frontend and backend architectures:
* **Frontend (Vercel)**: Static CDN hosting for instant global loading and zero server cost.
* **Backend (Render / Railway / AWS / Docker)**: Full Spring Boot runtime with Java 17+, Jsoup, Selenium WebDriver, and Chrome headless.
* **Database (PostgreSQL / Neon / Supabase)**: Scalable relational database for persistent products and audit logs.

### 1. Deploy Frontend to Vercel

The repository includes `vercel.json` preconfigured to serve `src/main/resources/static`.

#### Option A: Vercel CLI
```bash
# Install Vercel CLI (if needed)
npm install -g vercel

# Deploy directly from repository root
vercel
```

#### Option B: Vercel Dashboard (Git Integration)
1. Push your code to GitHub / GitLab / Bitbucket.
2. In Vercel, click **Add New Project** and import the repository.
3. Configure the build settings:
   * **Framework Preset**: Other
   * **Root Directory**: `./` (leave default)
   * **Output Directory**: `src/main/resources/static`
4. Click **Deploy**.
5. Once deployed, open your Vercel URL, click the **⚙ API Settings** button in the header, enter your deployed Spring Boot backend URL (e.g. `https://your-backend.onrender.com`), and click **Test Connection** & **Save**.

### 2. Deploy Backend to Cloud (Render / Railway / Docker)

Because Selenium WebDriver requires Google Chrome and background threads, the backend should be deployed to a container or VM host (e.g., Render Web Service, Railway, or AWS ECS):

#### Deploying on Render:
1. Create a **New Web Service** connected to your repository.
2. Configure build & runtime:
   * **Environment**: Java / Docker
   * **Build Command**: `mvn clean package -DskipTests`
   * **Start Command**: `java -jar target/amazon-availability-checker.jar`
3. Add Environment Variables in the Render dashboard:
   * `PORT`: `8080` (or leave default, Render sets this automatically)
   * `DATABASE_URL`: `jdbc:postgresql://<host>:<port>/<dbname>?sslmode=require`
   * `DATABASE_USERNAME`: `<db_user>`
   * `DATABASE_PASSWORD`: `<db_password>`
   * `FRONTEND_URL`: `https://your-app.vercel.app`
   * `CHECKER_SCHEDULE_MINUTES`: `30`
4. Verify health at `https://your-backend.onrender.com/api/health`.

### 3. Provisioning Managed PostgreSQL (Free Tiers Available)
You can use any PostgreSQL provider such as **Neon.tech**, **Supabase**, or **Render PostgreSQL**:
1. Create a new PostgreSQL database.
2. Copy the JDBC connection URL.
3. Supply `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD` to your backend environment variables. Hibernate will automatically create the tables on startup (`spring.jpa.hibernate.ddl-auto=update`).

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

## 🎓 Interview Preparation Guide

This section is written from the authentic perspective of a **B.Tech Computer Science student** explaining this project during software engineering and backend interviews.

---

### 1. Why did you build this project?
> "I built this project to solve a genuine, everyday problem: e-commerce prices and stock availability change dynamically, and checking pages manually is tedious. I wanted to design an automated, production-style backend system from scratch that connects web scraping, background scheduling, concurrency control, relational persistence, and a modern responsive dashboard—rather than building another standard CRUD app."

---

### 2. What were the key technical challenges you faced?
> "The three most significant challenges were:
> 1. **Anti-Scraping Resilience & DOM Volatility**: Amazon and Flipkart frequently change their HTML structure and block basic automated requests. I implemented desktop User-Agent headers, a cascading hierarchy of fallback CSS selectors, and defensive regex parsing so the application never crashes on missing elements or varied currency formats.
> 2. **Scheduler Concurrency & Race Condition Prevention**: If a scraping cycle takes 40 seconds due to slow responses while another check is triggered, multiple threads could write duplicate logs or compete for database locks. I solved this by implementing a `ReentrantLock` with `tryLock()` in `SchedulerRunner`, ensuring only one check runs at any time.
> 3. **Dual Persistence & Cloud Readiness**: Transitioning from local CSV files to PostgreSQL without breaking the local developer experience. I implemented Spring Data JPA repositories with a fallback to local H2 file storage, while maintaining CSV dual-writing so local audit backups are never lost."

---

### 3. How does the scraping engine work?
> "When a check is initiated:
> 1. The `ScraperFactory` inspects the product URL and resolves the appropriate `ProductStoreScraper` implementation (`AmazonScraper` or `FlipkartScraper`).
> 2. The scraper sends an HTTP GET request via Jsoup configured with browser-like headers (`User-Agent`, `Accept-Language`, `Accept-Encoding`).
> 3. It queries a prioritized list of CSS selectors to locate the price element (e.g. `.a-price .a-offscreen`, `#priceblock_ourprice`, `div._30jeq3`).
> 4. The raw string is cleaned using regular expressions to strip symbols like `₹` and commas, and parsed into a clean `Double`.
> 5. An `AvailabilityTracker` analyzes keywords such as 'In stock', 'Currently unavailable', or 'Sold Out'.
> 6. Selenium WebDriver with headless Chrome is invoked to capture a timestamped PNG screenshot for visual audit verification."

---

### 4. Why did you use both Jsoup and Selenium? Why not just one?
> "Jsoup and Selenium serve two fundamentally different purposes in this architecture:
> * **Jsoup** is an ultra-fast HTTP client and HTML parser. It fetches and parses HTML in under 200 milliseconds with minimal memory usage. Performing all price checks through Jsoup keeps the system fast and lightweight.
> * **Selenium WebDriver** is a full browser automation tool. It requires significant CPU and memory to spin up a browser process. Using Selenium for every single HTTP check would be inefficient and unscalable.
> 
> Therefore, I use **Jsoup as the primary scraping engine** for speed, and **Selenium headless Chrome exclusively for rendering and capturing visual screenshot proof**. This hybrid approach gives the speed of Jsoup with the visual verification of Selenium."

---

### 5. How does the automatic monitoring scheduler work?
> "The scheduler uses Java's `ScheduledExecutorService` initialized in `SchedulerRunner`.
> * It schedules a periodic task at fixed delay (`scheduleWithFixedDelay`) using a configurable interval from `application.properties` (e.g., 30 minutes).
> * Before running, it checks an `AtomicBoolean` enabled flag (which can be toggled on/off via the dashboard).
> * It acquires a `ReentrantLock` so that on-demand manual checks and automated background checks never collide or corrupt the audit history.
> * The frontend tracks the `nextCheck` time from `/api/status` and displays a live 1-second countdown ticker."

---

### 6. How does price history and price change calculation work?
> "Every time a product is checked, the new price is compared against the most recent previous check for that product.
> * If `currentPrice < previousPrice`, the system flags a **Price Drop** and computes the exact difference (e.g., `-₹30.00`).
> * If `currentPrice > previousPrice`, it flags a **Price Increase**.
> * These checkpoints are saved into the `audit_logs` table.
> * When a user opens the price history modal, the frontend queries `/api/products/{id}/history`, renders an interactive table, and dynamically generates a lightweight SVG polyline chart illustrating price movement over time without downloading any bulky third-party libraries."

---

### 7. How does the multi-store architecture work?
> "I designed the multi-store architecture around the **Strategy Pattern** and **Factory Pattern**:
> 1. An interface called `ProductStoreScraper` defines common methods: `scrapeProduct(url)`, `searchProducts(keyword)`, and `getStoreName()`.
> 2. `AmazonScraper` and `FlipkartScraper` implement this interface, containing platform-specific DOM selectors and extraction logic.
> 3. `ScraperFactory` inspects the domain name or store tag and provides the correct implementation.
> 4. `ProductService` and `DashboardController` only interact with the `ProductStoreScraper` interface.
> 
> This means adding a new store (like Croma or Reliance Digital) only requires creating a new scraper class implementing the interface, without changing the dashboard, database models, or scheduler."

---

### 8. What is the deployment architecture?
> "The frontend and backend are completely decoupled:
> * **Frontend**: Pure HTML5, CSS3, and JavaScript hosted on **Vercel CDN** for fast edge delivery worldwide. The frontend contains an API configuration modal so it can point to any backend URL.
> * **Backend**: A containerized Spring Boot service hosted on **Render** running Java 17 and headless Chrome. It handles scheduling, scraping, and REST endpoints.
> * **Database**: Managed PostgreSQL hosted on **Neon.tech** connected via JDBC over SSL.
> * **Local Fallback**: For local development, the app automatically falls back to an embedded H2 file database and local CSV storage, ensuring it runs out-of-the-box on any developer's machine."

---

## 🔮 Limitations & Future Improvements

* **Email & Push Notifications**: Integrating JavaMailSender or Discord webhooks to alert users the moment a price drops below a threshold.
* **Proxy Pool Integration**: Adding rotating residential proxies for enterprise-grade crawling resilience.
* **User Authentication**: Adding multi-tenant JWT-based authentication so multiple users can maintain separate product watchlists.

---

## 📄 License

This project is open-source and intended for academic, educational, and portfolio demonstration purposes. All brand names (Amazon, Flipkart) are property of their respective owners.
