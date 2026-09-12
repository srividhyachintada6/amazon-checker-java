// DOM Elements
const checkButton = document.getElementById("checkButton");
const checkButtonText = document.getElementById("checkButtonText");
const refreshButton = document.getElementById("refreshButton");
const results = document.getElementById("results");
const statusElement = document.getElementById("status");
const statusText = document.getElementById("statusText");
const checkingNotice = document.getElementById("checkingNotice");
const errorBanner = document.getElementById("errorBanner");
const errorMessage = document.getElementById("errorMessage");

const statTotal = document.getElementById("statTotal");
const statInStock = document.getElementById("statInStock");
const statOutOfStock = document.getElementById("statOutOfStock");
const statLastCheck = document.getElementById("statLastCheck");

const productGrid = document.getElementById("productGrid");
const emptyState = document.getElementById("emptyState");
const toggleLogButton = document.getElementById("toggleLogButton");
const logContainer = document.getElementById("logContainer");

// Toggle raw log section
if (toggleLogButton && logContainer) {
    toggleLogButton.addEventListener("click", () => {
        const isHidden = logContainer.classList.toggle("hidden");
        toggleLogButton.textContent = isHidden ? "Show Log" : "Hide Log";
    });
}

/**
 * Format timestamp into human-readable date & time
 */
function formatDateTime(isoString) {
    if (!isoString) return "Never";
    try {
        // Strip nanoseconds if present for standard JS Date compatibility
        const cleanIso = isoString.split(".")[0];
        const date = new Date(cleanIso);
        if (isNaN(date.getTime())) {
            return isoString.replace("T", " ").substring(0, 19);
        }
        return date.toLocaleDateString(undefined, {
            month: "short",
            day: "numeric"
        }) + ", " + date.toLocaleTimeString(undefined, {
            hour: "2-digit",
            minute: "2-digit"
        });
    } catch (e) {
        return isoString;
    }
}

/**
 * Clean up and format price string
 */
function formatPrice(priceStr) {
    if (!priceStr || priceStr.trim() === "" || priceStr.includes("NOT FOUND")) {
        return "Not available";
    }
    const clean = priceStr.replace("₹", "").trim();
    return "₹" + clean;
}

/**
 * Map status text to CSS badge class
 */
function getBadgeClass(status) {
    const s = (status || "").toUpperCase();
    if (s.includes("IN STOCK")) return "badge-instock";
    if (s.includes("OUT OF STOCK")) return "badge-outofstock";
    if (s.includes("UNAVAILABLE")) return "badge-unavailable";
    if (s.includes("CHECKING")) return "badge-checking";
    return "badge-ready";
}

/**
 * Show error banner
 */
function showError(message) {
    if (errorBanner && errorMessage) {
        errorMessage.textContent = message;
        errorBanner.classList.remove("hidden");
    }
    if (statusElement && statusText) {
        statusElement.className = "status error";
        statusText.textContent = "Offline";
    }
}

/**
 * Hide error banner
 */
function hideError() {
    if (errorBanner) {
        errorBanner.classList.add("hidden");
    }
}

/**
 * Parse availability_log.txt and update dashboard UI
 */
function parseAndRenderResults(logText) {
    if (!logText || logText.trim() === "") {
        if (results) {
            results.textContent = "No availability results yet. Run your first product check.";
        }
        if (productGrid) productGrid.innerHTML = "";
        if (emptyState) emptyState.classList.remove("hidden");

        if (statTotal) statTotal.textContent = "0";
        if (statInStock) statInStock.textContent = "0";
        if (statOutOfStock) statOutOfStock.textContent = "0";
        if (statLastCheck) statLastCheck.textContent = "Never";
        return;
    }

    if (results) {
        results.textContent = logText;
    }

    const lines = logText.split(/\r?\n/);
    const productMap = new Map();
    let mostRecentTime = null;

    for (const line of lines) {
        if (!line || !line.includes("|")) continue;

        const parts = line.split("|").map(p => p.trim());
        if (parts.length >= 3) {
            const timestamp = parts[0];
            const name = parts[1];
            const status = parts[2];
            const price = parts[3] || "";

            // Later entries in log represent the latest check for each product
            productMap.set(name, {
                name: name,
                status: status,
                price: price,
                timestamp: timestamp
            });

            mostRecentTime = timestamp;
        }
    }

    const products = Array.from(productMap.values());

    if (products.length === 0) {
        if (productGrid) productGrid.innerHTML = "";
        if (emptyState) emptyState.classList.remove("hidden");
        return;
    }

    if (emptyState) emptyState.classList.add("hidden");

    // Calculate Summary Statistics
    let inStockCount = 0;
    let outOfStockCount = 0;

    for (const prod of products) {
        const s = prod.status.toUpperCase();
        if (s.includes("IN STOCK")) {
            inStockCount++;
        } else if (s.includes("OUT OF STOCK") || s.includes("UNAVAILABLE")) {
            outOfStockCount++;
        }
    }

    if (statTotal) statTotal.textContent = products.length;
    if (statInStock) statInStock.textContent = inStockCount;
    if (statOutOfStock) statOutOfStock.textContent = outOfStockCount;
    if (statLastCheck) statLastCheck.textContent = formatDateTime(mostRecentTime);

    // Render Product Cards
    if (productGrid) {
        productGrid.innerHTML = "";

        products.forEach(product => {
            const card = document.createElement("article");
            card.className = "product-card";

            const badgeClass = getBadgeClass(product.status);
            const formattedPrice = formatPrice(product.price);
            const formattedTime = formatDateTime(product.timestamp);

            card.innerHTML = `
                <div class="product-card-media">
                    <div class="media-placeholder">
                        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path>
                            <polyline points="3.27 6.96 12 12.01 20.73 6.96"></polyline>
                            <line x1="12" y1="22.08" x2="12" y2="12"></line>
                        </svg>
                        <span>Amazon Product</span>
                    </div>
                    <span class="badge ${badgeClass} media-badge">${escapeHtml(product.status)}</span>
                </div>

                <div class="product-card-body">
                    <div class="product-card-header">
                        <h3 class="product-title" title="${escapeHtml(product.name)}">${escapeHtml(product.name)}</h3>
                    </div>

                    <div class="product-metrics">
                        <div class="metric-group">
                            <span class="metric-label">Current Price</span>
                            <span class="price-value">${escapeHtml(formattedPrice)}</span>
                        </div>
                        <div class="metric-group">
                            <span class="metric-label">Last Checked</span>
                            <span class="time-value">${escapeHtml(formattedTime)}</span>
                        </div>
                    </div>

                    <div class="product-card-footer">
                        <a href="https://www.amazon.in/s?k=${encodeURIComponent(product.name)}" target="_blank" rel="noopener noreferrer" class="btn btn-outline btn-sm">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"></path>
                                <polyline points="15 3 21 3 21 9"></polyline>
                                <line x1="10" y1="14" x2="21" y2="3"></line>
                            </svg>
                            <span>View on Amazon</span>
                        </a>
                        <button class="btn btn-secondary btn-sm" onclick="alert('Screenshots are stored in the screenshots/ directory. In Phase 3, direct screenshot preview will be enabled.')">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                                <circle cx="8.5" cy="8.5" r="1.5"></circle>
                                <polyline points="21 15 16 10 5 21"></polyline>
                            </svg>
                            <span>Screenshot</span>
                        </button>
                    </div>
                </div>
            `;
            productGrid.appendChild(card);
        });
    }
}

/**
 * HTML Escape Helper
 */
function escapeHtml(str) {
    if (!str) return "";
    return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

/**
 * Load log from /api/log
 */
async function loadLog() {
    try {
        const response = await fetch("/api/log");
        if (!response.ok) {
            throw new Error(`HTTP error ${response.status}`);
        }
        const text = await response.text();
        hideError();
        parseAndRenderResults(text);
    } catch (error) {
        console.error("Unable to load log:", error);
        showError("Unable to connect to the backend service. Please ensure the server is running.");
        if (results) {
            results.textContent = "Unable to load results.\n\n" + error;
        }
    }
}

/**
 * Check products via /api/check
 */
async function checkProducts() {
    if (checkButton) {
        checkButton.disabled = true;
    }
    if (checkButtonText) {
        checkButtonText.textContent = "Checking...";
    }

    if (statusElement && statusText) {
        statusElement.className = "status checking";
        statusText.textContent = "Checking Amazon products...";
    }

    if (checkingNotice) {
        checkingNotice.classList.remove("hidden");
    }

    if (results) {
        results.textContent =
            "Amazon checking started...\n\n" +
            "Please wait while products are checked.\n" +
            "Results will appear automatically upon completion.";
    }

    try {
        const response = await fetch("/api/check", {
            method: "POST"
        });

        if (!response.ok) {
            throw new Error(`HTTP error ${response.status}`);
        }

        const data = await response.json();

        if (results) {
            results.textContent =
                data.message +
                "\n\nThe checker is running in background.\n" +
                "Results will update automatically.";
        }

        hideError();
        waitForCompletion();

    } catch (error) {
        console.error("Error starting checker:", error);
        showError("Failed to trigger check: " + error.message);

        if (checkButton) checkButton.disabled = false;
        if (checkButtonText) checkButtonText.textContent = "Check All Products";

        if (statusElement && statusText) {
            statusElement.className = "status ready";
            statusText.textContent = "Ready";
        }

        if (checkingNotice) {
            checkingNotice.classList.add("hidden");
        }
    }
}

/**
 * Poll /api/status until checking is completed
 */
async function waitForCompletion() {
    const interval = setInterval(async () => {
        try {
            const response = await fetch("/api/status");
            if (!response.ok) {
                throw new Error(`HTTP error ${response.status}`);
            }

            const data = await response.json();

            if (data.status === "ready") {
                clearInterval(interval);

                if (statusElement && statusText) {
                    statusElement.className = "status ready";
                    statusText.textContent = "Ready";
                }

                if (checkButton) checkButton.disabled = false;
                if (checkButtonText) checkButtonText.textContent = "Check All Products";

                if (checkingNotice) {
                    checkingNotice.classList.add("hidden");
                }

                await loadLog();
            }
        } catch (error) {
            console.error("Error polling status:", error);
        }
    }, 2000);
}

// Event Listeners
if (checkButton) {
    checkButton.addEventListener("click", checkProducts);
}

if (refreshButton) {
    refreshButton.addEventListener("click", () => {
        loadLog();
    });
}

// Initial fetch on page load
loadLog();