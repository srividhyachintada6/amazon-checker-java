# Amazon Availability Checker (Java version)

A Java port of the Python `amazon_availability_checker` project. It reads a list of
products from `data/products.csv`, fetches each Amazon product page, extracts the
stock status and price, logs the result, and saves a screenshot of the page.

## Project layout

```
amazon-checker-java/
├── pom.xml
├── data/
│   └── products.csv              # your product list (name, url)
├── offline_pages/
│   └── sample_product.html       # sample page for offline/demo mode
└── src/main/java/com/amazonchecker/
    ├── Main.java                 # entry point (was main.py)
    ├── config/
    │   ├── Headers.java          # was config/headers.py
    │   └── Settings.java         # was config/settings.py
    ├── scraper/
    │   ├── ProductScraper.java   # was scraper/product_scraper.py
    │   └── SearchScraper.java    # was scraper/search_scraper.py
    ├── tracker/
    │   ├── AvailabilityTracker.java  # was tracker/availability_tracker.py
    │   └── PriceTracker.java         # was tracker/price_tracker.py
    ├── utils/
    │   ├── CsvHandler.java       # was utils/csv_handler.py
    │   ├── LogWriter.java        # was utils/logger.py
    │   ├── MockParser.java       # was utils/mock_parser.py
    │   ├── Screenshot.java       # was utils/screenshot.py
    │   └── Product.java          # simple data holder (dict replacement)
    └── scheduler/
        └── SchedulerRunner.java  # was scheduler/scheduler.py
```

**Not ported:** the Python project's PyQt desktop GUI (`gui/app.py`) and the Flask
web dashboard (`web/app.py`) are Python-only UI frameworks with no direct Java
equivalent, so this port covers the core CLI logic (scraping, tracking, logging,
scheduling). If you want a Java UI on top of this, a Swing/JavaFX desktop app or a
Spring Boot web dashboard would be the natural equivalents — ask if you'd like one
built out.

## Prerequisites

1. **Java 17+** — check with `java -version`.
2. **Maven 3.8+** — check with `mvn -version`.
3. **Google Chrome** installed (for the screenshot feature — Selenium drives real Chrome in headless mode).

## Build

From the project root:

```bash
mvn clean package
```

This downloads dependencies (Jsoup for HTML parsing/fetching, Selenium + WebDriverManager
for screenshots) and produces a runnable "fat jar" at:

```
target/amazon-availability-checker.jar
```

## Run

```bash
java -jar target/amazon-availability-checker.jar
```

It will:
1. Read `data/products.csv`.
2. For each row, use the URL if given, or search Amazon by product name if the URL column is blank.
3. Fetch the page, extract availability + price.
4. Print results to the console.
5. Append a line to `data/availability_log.txt`.
6. Save a screenshot into `screenshots/`.

### products.csv format

```csv
product_name,product_url
Amazon Basics HDMI Cable,https://www.amazon.in/dp/B014I8SSD0
SanDisk 32GB Pendrive,https://www.amazon.in/dp/B07F6JCK5W
```

Leave `product_url` blank to have the tool search Amazon by name instead.

### Offline/demo mode

Set `USE_OFFLINE_DEMO = true` in `config/Settings.java` to parse
`offline_pages/sample_product.html` instead of hitting the network — useful for
testing the parsing logic without live requests.

### Running on a schedule

`scheduler/SchedulerRunner.java` re-runs a given task every `CHECK_INTERVAL_HOURS`
(set in `Settings.java`). To use it, call it from `Main` instead of a single run:

```java
SchedulerRunner.runScheduler(Main::run);
```

## Notes on Amazon scraping

Amazon's page structure (element IDs/classes for price and stock status) changes
periodically and scraping is against Amazon's Terms of Service, so:
- Selectors may need updating over time if Amazon changes its HTML.
- For serious/production use, Amazon's official Product Advertising API is the
  supported, ToS-compliant way to get this data.
- Aggressive automated requests can get your IP rate-limited or blocked, same as
  in the original Python version — this Java version has the same headers-based
  approach and no additional evasion techniques.
