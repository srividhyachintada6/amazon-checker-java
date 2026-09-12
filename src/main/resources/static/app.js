// DOM Elements
const checkButton = document.getElementById("checkButton");
const checkButtonText = document.getElementById("checkButtonText");
const refreshButton = document.getElementById("refreshButton");
const runCheckNowButton = document.getElementById("runCheckNowButton");
const runCheckNowText = document.getElementById("runCheckNowText");
const autoMonitoringBadge = document.getElementById("autoMonitoringBadge");
const autoIntervalText = document.getElementById("autoIntervalText");
const nextCheckCountdown = document.getElementById("nextCheckCountdown");
const nextCheckDetail = document.getElementById("nextCheckDetail");

const results = document.getElementById("results");
const statusElement = document.getElementById("status");
const statusText = document.getElementById("statusText");
const checkingNotice = document.getElementById("checkingNotice");
const errorBanner = document.getElementById("errorBanner");
const errorMessage = document.getElementById("errorMessage");

// Toolbar Elements (Search, Filter, Sort)
const searchInput = document.getElementById("searchInput");
const clearSearchButton = document.getElementById("clearSearchButton");
const filterPills = document.querySelectorAll(".filter-pill");
const countFilterAll = document.getElementById("countFilterAll");
const countFilterInStock = document.getElementById("countFilterInStock");
const countFilterOutOfStock = document.getElementById("countFilterOutOfStock");
const countFilterErrors = document.getElementById("countFilterErrors");
const sortSelect = document.getElementById("sortSelect");
const resetFiltersButton = document.getElementById("resetFiltersButton");
const noSearchMatch = document.getElementById("noSearchMatch");
const productSkeletons = document.getElementById("productSkeletons");

// KPI Elements
const statTotal = document.getElementById("statTotal");
const statInStock = document.getElementById("statInStock");
const statOutOfStock = document.getElementById("statOutOfStock");
const statErrors = document.getElementById("statErrors");
const statPriceDrops = document.getElementById("statPriceDrops");
const statLastCheck = document.getElementById("statLastCheck");

// Product Management (Add/Edit) Modal Elements
const addProductButton = document.getElementById("addProductButton");
const productModal = document.getElementById("productModal");
const productModalBackdrop = document.getElementById("productModalBackdrop");
const productModalTitle = document.getElementById("productModalTitle");
const closeProductModalBtn = document.getElementById("closeProductModalBtn");
const cancelProductModalBtn = document.getElementById("cancelProductModalBtn");
const productForm = document.getElementById("productForm");
const editProductId = document.getElementById("editProductId");
const productNameInput = document.getElementById("productNameInput");
const productUrlInput = document.getElementById("productUrlInput");
const productModalError = document.getElementById("productModalError");
const productModalErrorMsg = document.getElementById("productModalErrorMsg");
const saveProductModalBtn = document.getElementById("saveProductModalBtn");

// Delete Modal Elements
const deleteModal = document.getElementById("deleteModal");
const deleteModalBackdrop = document.getElementById("deleteModalBackdrop");
const closeDeleteModalBtn = document.getElementById("closeDeleteModalBtn");
const cancelDeleteModalBtn = document.getElementById("cancelDeleteModalBtn");
const confirmDeleteModalBtn = document.getElementById("confirmDeleteModalBtn");
const deleteProductName = document.getElementById("deleteProductName");

// Global History Elements
const globalHistoryTableBody = document.getElementById("globalHistoryTableBody");
const globalHistoryEmpty = document.getElementById("globalHistoryEmpty");
const refreshHistoryBtn = document.getElementById("refreshHistoryBtn");

// State
let allProducts = [];
let currentFilter = "all";
let searchQuery = "";
let currentSort = "time-desc";
let productPendingDelete = null;

const productGrid = document.getElementById("productGrid");
const emptyState = document.getElementById("emptyState");
const toggleLogButton = document.getElementById("toggleLogButton");
const logContainer = document.getElementById("logContainer");

// Modal Elements
const historyModal = document.getElementById("historyModal");
const modalBackdrop = document.getElementById("modalBackdrop");
const modalTitle = document.getElementById("modalTitle");
const closeModalButton = document.getElementById("closeModalButton");
const closeModalFooterButton = document.getElementById("closeModalFooterButton");
const historyLoading = document.getElementById("historyLoading");
const historyTableContainer = document.getElementById("historyTableContainer");
const historyTableBody = document.getElementById("historyTableBody");
const historyEmpty = document.getElementById("historyEmpty");

// Toggle raw activity log drawer
if (toggleLogButton && logContainer) {
    toggleLogButton.addEventListener("click", () => {
        const isHidden = logContainer.classList.toggle("hidden");
        toggleLogButton.textContent = isHidden ? "Show Activity Log" : "Hide Activity Log";
        if (!isHidden && results && results.textContent.trim() === "No results loaded yet.") {
            loadRawLog();
        }
    });
}

// Modal Close Listeners
if (closeModalButton) closeModalButton.addEventListener("click", closeHistoryModal);
if (closeModalFooterButton) closeModalFooterButton.addEventListener("click", closeHistoryModal);
if (modalBackdrop) modalBackdrop.addEventListener("click", closeHistoryModal);

window.addEventListener("keydown", (e) => {
    if (e.key === "Escape" && historyModal && !historyModal.classList.contains("hidden")) {
        closeHistoryModal();
    }
});

function closeHistoryModal() {
    if (historyModal) {
        historyModal.classList.add("hidden");
    }
}

/**
 * Format ISO timestamp into user-friendly date and time
 */
function formatDateTime(isoString) {
    if (!isoString) return "Never";
    try {
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
 * Format time only (e.g., 09:37 AM)
 */
function formatTimeOnly(isoString) {
    if (!isoString) return "Not checked";
    try {
        const cleanIso = isoString.split(".")[0];
        const date = new Date(cleanIso);
        if (isNaN(date.getTime())) {
            return isoString.replace("T", " ").substring(11, 16);
        }
        return date.toLocaleTimeString(undefined, {
            hour: "2-digit",
            minute: "2-digit"
        });
    } catch (e) {
        return isoString;
    }
}

/**
 * Clean & format price numbers
 */
function formatPrice(price) {
    if (price === null || price === undefined || isNaN(price)) {
        return "Not available";
    }
    return "₹" + Number(price).toLocaleString("en-IN", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

/**
 * Return CSS badge class matching status
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
 * Return emoji indicator for availability status
 */
function getStatusEmoji(status) {
    const s = (status || "").toUpperCase();
    if (s.includes("IN STOCK")) return "🟢";
    if (s.includes("OUT OF STOCK") || s.includes("UNAVAILABLE")) return "🔴";
    if (s.includes("CHECKING")) return "🟡";
    return "⚪";
}

/**
 * Display friendly error banner
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
 * Clear error banner
 */
function hideError() {
    if (errorBanner) {
        errorBanner.classList.add("hidden");
    }
}

/**
 * HTML sanitization helper
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
 * Fetch and render products from GET /api/products
 * and summary metrics from GET /api/summary
 */
async function loadDashboardData() {
    if (allProducts.length === 0 && productSkeletons) {
        productSkeletons.classList.remove("hidden");
        if (productGrid) productGrid.classList.add("hidden");
    }

    try {
        const [productsRes, summaryRes] = await Promise.all([
            fetch("/api/products"),
            fetch("/api/summary")
        ]);

        if (!productsRes.ok) {
            throw new Error(`Products API returned HTTP ${productsRes.status}`);
        }
        if (!summaryRes.ok) {
            throw new Error(`Summary API returned HTTP ${summaryRes.status}`);
        }

        const products = await productsRes.json();
        const summary = await summaryRes.json();

        hideError();
        allProducts = Array.isArray(products) ? products : [];

        // Update 6 KPI Summary Cards
        renderSummary(summary);

        // Apply Search, Filter, Sort and Render Products
        applyFiltersAndRender();

        // Load Global Monitoring History
        await loadGlobalHistory();

    } catch (error) {
        console.error("Failed to load dashboard data:", error);
        showError("Unable to load product data from the server. Please ensure the backend is running.");
    } finally {
        if (productSkeletons) productSkeletons.classList.add("hidden");
        if (productGrid) productGrid.classList.remove("hidden");
    }
}

/**
 * Render 6 KPI summary statistics
 */
function renderSummary(summary) {
    if (!summary) return;

    if (statTotal) statTotal.textContent = summary.totalProducts ?? 0;
    if (statInStock) statInStock.textContent = summary.inStock ?? 0;
    if (statOutOfStock) statOutOfStock.textContent = summary.outOfStock ?? 0;
    if (statErrors) statErrors.textContent = summary.errors ?? 0;
    if (statPriceDrops) statPriceDrops.textContent = summary.priceDrops ?? 0;
    if (statLastCheck) statLastCheck.textContent = formatDateTime(summary.lastChecked);
}

/**
 * Apply Search, Filter, and Sort then render
 */
function applyFiltersAndRender() {
    // 1. Calculate counts for filter pills
    const inStockCount = allProducts.filter(p => (p.status || "").toUpperCase().includes("IN STOCK")).length;
    const outOfStockCount = allProducts.filter(p => {
        const s = (p.status || "").toUpperCase();
        return s.includes("OUT OF STOCK");
    }).length;
    const errorsCount = allProducts.filter(p => {
        const s = (p.status || "").toUpperCase();
        return s.includes("ERROR") || s.includes("UNAVAILABLE") || s.includes("NOT CHECKED") || s.includes("404") || s.includes("NOT FOUND") || !p.status;
    }).length;

    if (countFilterAll) countFilterAll.textContent = allProducts.length;
    if (countFilterInStock) countFilterInStock.textContent = inStockCount;
    if (countFilterOutOfStock) countFilterOutOfStock.textContent = outOfStockCount;
    if (countFilterErrors) countFilterErrors.textContent = errorsCount;

    // 2. Filter by category
    let filtered = [...allProducts];
    if (currentFilter === "instock") {
        filtered = filtered.filter(p => (p.status || "").toUpperCase().includes("IN STOCK"));
    } else if (currentFilter === "outofstock") {
        filtered = filtered.filter(p => (p.status || "").toUpperCase().includes("OUT OF STOCK"));
    } else if (currentFilter === "errors") {
        filtered = filtered.filter(p => {
            const s = (p.status || "").toUpperCase();
            return s.includes("ERROR") || s.includes("UNAVAILABLE") || s.includes("NOT CHECKED") || s.includes("404") || s.includes("NOT FOUND") || !p.status;
        });
    }

    // 3. Filter by search query
    const q = (searchQuery || "").trim().toLowerCase();
    if (q) {
        filtered = filtered.filter(p => (p.name || "").toLowerCase().includes(q));
    }

    // 4. Sort
    filtered.sort((a, b) => {
        if (currentSort === "name-asc") {
            return (a.name || "").localeCompare(b.name || "");
        } else if (currentSort === "name-desc") {
            return (b.name || "").localeCompare(a.name || "");
        } else if (currentSort === "price-asc") {
            const pa = a.price !== null && a.price !== undefined ? a.price : 99999999;
            const pb = b.price !== null && b.price !== undefined ? b.price : 99999999;
            return pa - pb;
        } else if (currentSort === "price-desc") {
            const pa = a.price !== null && a.price !== undefined ? a.price : -1;
            const pb = b.price !== null && b.price !== undefined ? b.price : -1;
            return pb - pa;
        } else {
            // time-desc (Newest last checked first)
            const ta = a.lastChecked || "";
            const tb = b.lastChecked || "";
            return tb.localeCompare(ta);
        }
    });

    // 5. Check empty states
    if (allProducts.length === 0) {
        if (productGrid) productGrid.innerHTML = "";
        if (emptyState) emptyState.classList.remove("hidden");
        if (noSearchMatch) noSearchMatch.classList.add("hidden");
        return;
    }

    if (emptyState) emptyState.classList.add("hidden");

    if (filtered.length === 0) {
        if (productGrid) productGrid.innerHTML = "";
        if (noSearchMatch) noSearchMatch.classList.remove("hidden");
        return;
    }

    if (noSearchMatch) noSearchMatch.classList.add("hidden");
    renderProducts(filtered);
}

/**
 * Render structured product cards with Edit/Remove actions and Price Change
 */
function renderProducts(products) {
    if (!productGrid) return;
    productGrid.innerHTML = "";

    products.forEach(product => {
        const card = document.createElement("article");
        card.className = "product-card";

        const badgeClass = getBadgeClass(product.status);
        const statusEmoji = getStatusEmoji(product.status);
        const formattedPrice = formatPrice(product.price);
        const formattedTime = product.lastChecked ? formatTimeOnly(product.lastChecked) : "Not yet checked";
        const hasScreenshot = Boolean(product.screenshot);
        const screenshotSrc = hasScreenshot ? product.screenshot : null;
        const hasUrl = Boolean(product.productUrl && product.productUrl.trim() !== "");

        // Price Change logic
        let priceDiffHtml = "";
        if (product.previousPrice !== null && product.previousPrice !== undefined && product.priceChange !== null) {
            const prevFormatted = formatPrice(product.previousPrice);
            if (product.priceChange < 0) {
                const dropAmount = Math.abs(product.priceChange).toFixed(2);
                priceDiffHtml = `
                    <span class="price-prev-label">Previous: ${escapeHtml(prevFormatted)}</span>
                    <span class="price-diff-badge price-diff-drop">↓ ₹${escapeHtml(dropAmount)}</span>
                `;
            } else if (product.priceChange > 0) {
                const riseAmount = product.priceChange.toFixed(2);
                priceDiffHtml = `
                    <span class="price-prev-label">Previous: ${escapeHtml(prevFormatted)}</span>
                    <span class="price-diff-badge price-diff-rise">↑ ₹${escapeHtml(riseAmount)}</span>
                `;
            } else {
                priceDiffHtml = `
                    <span class="price-prev-label">Previous: ${escapeHtml(prevFormatted)}</span>
                    <span class="price-diff-badge price-diff-same">Unchanged</span>
                `;
            }
        } else {
            priceDiffHtml = `<span class="price-diff-none">No previous price</span>`;
        }

        card.innerHTML = `
            <div class="product-card-media">
                <div class="product-card-top-actions">
                    <button class="btn-card-action" onclick="openEditProductModal('${escapeHtml(product.id)}')" title="Edit product" aria-label="Edit ${escapeHtml(product.name)}">
                        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M12 20h9"></path>
                            <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"></path>
                        </svg>
                    </button>
                    <button class="btn-card-action btn-card-action-delete" onclick="openDeleteModal('${escapeHtml(product.id)}')" title="Remove product" aria-label="Remove ${escapeHtml(product.name)}">
                        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <polyline points="3 6 5 6 21 6"></polyline>
                            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                        </svg>
                    </button>
                </div>

                ${hasScreenshot ? `
                    <img src="${escapeHtml(screenshotSrc)}" alt="${escapeHtml(product.name)}" loading="lazy"
                         onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';" />
                    <div class="media-placeholder" style="display: none;">
                        <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                            <circle cx="8.5" cy="8.5" r="1.5"></circle>
                            <polyline points="21 15 16 10 5 21"></polyline>
                        </svg>
                        <span>Preview Unavailable</span>
                    </div>
                ` : `
                    <div class="media-placeholder">
                        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path>
                            <polyline points="3.27 6.96 12 12.01 20.73 6.96"></polyline>
                            <line x1="12" y1="22.08" x2="12" y2="12"></line>
                        </svg>
                        <span>No Screenshot Yet</span>
                    </div>
                `}
                <span class="badge ${badgeClass} media-badge">${statusEmoji} ${escapeHtml(product.status || "UNKNOWN")}</span>
            </div>

            <div class="product-card-body">
                <div class="product-card-header">
                    <h3 class="product-title" title="${escapeHtml(product.name)}">${escapeHtml(product.name)}</h3>
                </div>

                <div class="product-metrics">
                    <div class="price-main-row">
                        <span class="price-value">${escapeHtml(formattedPrice)}</span>
                        <div class="metric-group">
                            <span class="metric-label">Last checked</span>
                            <span class="time-value">${escapeHtml(formattedTime)}</span>
                        </div>
                    </div>

                    <div class="price-meta">
                        ${priceDiffHtml}
                    </div>
                </div>

                <div class="product-card-footer">
                    ${hasUrl ? `
                        <a href="${escapeHtml(product.productUrl)}" target="_blank" rel="noopener noreferrer" class="btn btn-outline btn-sm">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"></path>
                                <polyline points="15 3 21 3 21 9"></polyline>
                                <line x1="10" y1="14" x2="21" y2="3"></line>
                            </svg>
                            <span>View Product</span>
                        </a>
                    ` : `
                        <button class="btn btn-outline btn-sm" disabled title="No direct URL configured">
                            <span>No Product URL</span>
                        </button>
                    `}

                    ${hasScreenshot ? `
                        <a href="${escapeHtml(screenshotSrc)}" target="_blank" rel="noopener noreferrer" class="btn btn-secondary btn-sm" title="View captured screenshot">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                                <circle cx="8.5" cy="8.5" r="1.5"></circle>
                                <polyline points="21 15 16 10 5 21"></polyline>
                            </svg>
                            <span>Screenshot</span>
                        </a>
                    ` : `
                        <button class="btn btn-secondary btn-sm" disabled title="No screenshot captured yet">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                                <line x1="3" y1="3" x2="21" y2="21"></line>
                            </svg>
                            <span>Screenshot</span>
                        </button>
                    `}

                    <button class="btn btn-ghost btn-sm btn-full" onclick="openPriceHistory('${escapeHtml(product.id)}', '${escapeHtml(product.name)}')">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <circle cx="12" cy="12" r="10"></circle>
                            <polyline points="12 6 12 12 16 14"></polyline>
                        </svg>
                        <span>Price & Availability History</span>
                    </button>
                </div>
            </div>
        `;

        productGrid.appendChild(card);
    });
}

/**
 * Open Price History Modal for a product
 */
async function openPriceHistory(productId, productName) {
    if (!historyModal) return;

    if (modalTitle) modalTitle.textContent = productName;
    historyModal.classList.remove("hidden");

    if (historyLoading) historyLoading.classList.remove("hidden");
    if (historyTableContainer) historyTableContainer.classList.add("hidden");
    if (historyEmpty) historyEmpty.classList.add("hidden");
    if (historyTableBody) historyTableBody.innerHTML = "";

    try {
        const res = await fetch(`/api/products/${encodeURIComponent(productId)}/history`);
        if (!res.ok) {
            throw new Error(`HTTP ${res.status}`);
        }

        const historyPoints = await res.json();
        if (historyLoading) historyLoading.classList.add("hidden");

        if (!Array.isArray(historyPoints) || historyPoints.length === 0) {
            if (historyEmpty) historyEmpty.classList.remove("hidden");
            return;
        }

        if (historyTableContainer) historyTableContainer.classList.remove("hidden");

        const reversed = [...historyPoints].reverse();

        reversed.forEach((point, idx) => {
            const tr = document.createElement("tr");

            const formattedTime = formatDateTime(point.timestamp);
            const formattedPrice = formatPrice(point.price);
            const badgeClass = getBadgeClass(point.status);
            const emoji = getStatusEmoji(point.status);

            let changeHtml = '<span class="price-diff-none">—</span>';
            const originalIndex = historyPoints.length - 1 - idx;
            if (originalIndex > 0) {
                const prevPoint = historyPoints[originalIndex - 1];
                if (point.price !== null && point.price !== undefined && prevPoint.price !== null && prevPoint.price !== undefined) {
                    const diff = Math.round((point.price - prevPoint.price) * 100) / 100;
                    if (diff < 0) {
                        changeHtml = `<span class="price-diff-badge price-diff-drop">↓ ₹${Math.abs(diff).toFixed(2)}</span>`;
                    } else if (diff > 0) {
                        changeHtml = `<span class="price-diff-badge price-diff-rise">↑ ₹${diff.toFixed(2)}</span>`;
                    } else {
                        changeHtml = `<span class="price-diff-badge price-diff-same">Unchanged</span>`;
                    }
                }
            }

            tr.innerHTML = `
                <td><strong>${escapeHtml(formattedTime)}</strong></td>
                <td><span class="badge ${badgeClass}">${emoji} ${escapeHtml(point.status)}</span></td>
                <td><strong>${escapeHtml(formattedPrice)}</strong></td>
                <td>${changeHtml}</td>
            `;

            historyTableBody.appendChild(tr);
        });

    } catch (error) {
        console.error("Error loading price history:", error);
        if (historyLoading) historyLoading.classList.add("hidden");
        if (historyEmpty) {
            historyEmpty.innerHTML = `<p style="color: var(--danger);">Failed to load history: ${escapeHtml(error.message)}</p>`;
            historyEmpty.classList.remove("hidden");
        }
    }
}
window.openPriceHistory = openPriceHistory;

/**
 * Load global monitoring history (Feature 6)
 */
async function loadGlobalHistory() {
    if (!globalHistoryTableBody) return;
    try {
        const res = await fetch("/api/history?limit=25");
        if (!res.ok) return;
        const historyList = await res.json();

        globalHistoryTableBody.innerHTML = "";
        if (!Array.isArray(historyList) || historyList.length === 0) {
            if (globalHistoryEmpty) globalHistoryEmpty.classList.remove("hidden");
            return;
        }

        if (globalHistoryEmpty) globalHistoryEmpty.classList.add("hidden");

        historyList.forEach(entry => {
            const tr = document.createElement("tr");
            const badgeClass = getBadgeClass(entry.status);
            const emoji = getStatusEmoji(entry.status);
            const timeDisplay = formatDateTime(entry.timestamp);
            const priceDisplay = entry.formattedPrice ? entry.formattedPrice : formatPrice(entry.price);

            tr.innerHTML = `
                <td><strong>${escapeHtml(entry.productName || "Unknown Product")}</strong></td>
                <td><span class="badge ${badgeClass}">${emoji} ${escapeHtml(entry.status || "UNKNOWN")}</span></td>
                <td><strong>${escapeHtml(priceDisplay)}</strong></td>
                <td>${escapeHtml(timeDisplay)}</td>
            `;
            globalHistoryTableBody.appendChild(tr);
        });
    } catch (e) {
        console.error("Failed to load global history:", e);
    }
}

/**
 * Open Add Product Modal (Feature 1)
 */
function openAddProductModal() {
    if (!productModal) return;
    if (editProductId) editProductId.value = "";
    if (productNameInput) productNameInput.value = "";
    if (productUrlInput) productUrlInput.value = "";
    if (productModalTitle) productModalTitle.textContent = "Add Monitored Product";
    if (productModalError) productModalError.classList.add("hidden");
    productModal.classList.remove("hidden");
    if (productNameInput) productNameInput.focus();
}
window.openAddProductModal = openAddProductModal;

/**
 * Open Edit Product Modal (Feature 1)
 */
function openEditProductModal(id) {
    if (!productModal) return;
    const product = allProducts.find(p => p.id === id);
    if (!product) return;

    if (editProductId) editProductId.value = id;
    if (productNameInput) productNameInput.value = product.name || "";
    if (productUrlInput) productUrlInput.value = product.productUrl || "";
    if (productModalTitle) productModalTitle.textContent = "Edit Product";
    if (productModalError) productModalError.classList.add("hidden");
    productModal.classList.remove("hidden");
    if (productNameInput) productNameInput.focus();
}
window.openEditProductModal = openEditProductModal;

/**
 * Close Product Modal
 */
function closeProductModal() {
    if (productModal) productModal.classList.add("hidden");
    if (productModalError) productModalError.classList.add("hidden");
    if (productForm) productForm.reset();
}

/**
 * Handle Add/Edit Product form submit
 */
async function handleProductFormSubmit(e) {
    e.preventDefault();

    const id = (editProductId ? editProductId.value : "").trim();
    const name = (productNameInput ? productNameInput.value : "").trim();
    const url = (productUrlInput ? productUrlInput.value : "").trim();

    if (!name) {
        showModalError("Product name is required.");
        return;
    }

    if (!url) {
        showModalError("Product URL is required.");
        return;
    }

    if (!url.startsWith("http://") && !url.startsWith("https://")) {
        showModalError("URL must start with http:// or https://");
        return;
    }

    if (saveProductModalBtn) {
        saveProductModalBtn.disabled = true;
        saveProductModalBtn.textContent = "Saving...";
    }

    try {
        const isEdit = Boolean(id);
        const endpoint = isEdit ? `/api/products/${encodeURIComponent(id)}` : "/api/products";
        const method = isEdit ? "PUT" : "POST";

        const res = await fetch(endpoint, {
            method: method,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ name: name, url: url })
        });

        if (!res.ok) {
            let errorMsg = `Server error (${res.status})`;
            try {
                const errData = await res.json();
                if (errData.message) errorMsg = errData.message;
                else if (errData.error) errorMsg = errData.error;
            } catch (_) {}
            showModalError(errorMsg);
            return;
        }

        closeProductModal();
        await loadDashboardData();

    } catch (err) {
        showModalError("Network error: " + err.message);
    } finally {
        if (saveProductModalBtn) {
            saveProductModalBtn.disabled = false;
            saveProductModalBtn.textContent = "Save Product";
        }
    }
}

function showModalError(msg) {
    if (productModalError && productModalErrorMsg) {
        productModalErrorMsg.textContent = msg;
        productModalError.classList.remove("hidden");
    }
}

/**
 * Open Delete Product Confirmation Modal
 */
function openDeleteModal(id) {
    if (!deleteModal) return;
    const product = allProducts.find(p => p.id === id);
    productPendingDelete = id;
    if (deleteProductName) {
        deleteProductName.textContent = product ? product.name : "this product";
    }
    deleteModal.classList.remove("hidden");
}
window.openDeleteModal = openDeleteModal;

/**
 * Close Delete Modal
 */
function closeDeleteModal() {
    if (deleteModal) deleteModal.classList.add("hidden");
    productPendingDelete = null;
}

/**
 * Handle Delete Confirmation
 */
async function handleConfirmDelete() {
    if (!productPendingDelete) return;

    if (confirmDeleteModalBtn) {
        confirmDeleteModalBtn.disabled = true;
        confirmDeleteModalBtn.textContent = "Removing...";
    }

    try {
        const res = await fetch(`/api/products/${encodeURIComponent(productPendingDelete)}`, {
            method: "DELETE"
        });

        if (!res.ok) {
            let errorMsg = `Failed to remove product (${res.status})`;
            try {
                const errData = await res.json();
                if (errData.message) errorMsg = errData.message;
                else if (errData.error) errorMsg = errData.error;
            } catch (_) {}
            showError(errorMsg);
        } else {
            closeDeleteModal();
            await loadDashboardData();
        }
    } catch (err) {
        showError("Network error while removing product: " + err.message);
    } finally {
        if (confirmDeleteModalBtn) {
            confirmDeleteModalBtn.disabled = false;
            confirmDeleteModalBtn.textContent = "Remove Product";
        }
        closeDeleteModal();
    }
}

/**
 * Optional fetch for raw activity log
 */
async function loadRawLog() {
    try {
        const response = await fetch("/api/log");
        if (!response.ok) return;
        const text = await response.text();
        if (results) {
            results.textContent = text.trim() === "" ? "No logs recorded yet." : text;
        }
    } catch (e) {
        console.error("Could not fetch raw log:", e);
    }
}

// State variables for monitoring and scheduler
let targetNextCheckTime = null;
let isCurrentlyChecking = false;
let pollingInterval = null;

/**
 * Update UI based on GET /api/status response
 */
function updateStatusAndScheduler(data) {
    if (!data) return;

    const status = (data.status || "").toLowerCase();
    const checking = Boolean(data.checking || status === "checking");
    isCurrentlyChecking = checking;

    if (checking) {
        if (statusElement && statusText) {
            statusElement.className = "status checking";
            statusText.textContent = "Checking Amazon products...";
        }
        if (checkingNotice) {
            checkingNotice.classList.remove("hidden");
        }
        if (checkButton) checkButton.disabled = true;
        if (checkButtonText) checkButtonText.textContent = "Checking...";
        if (runCheckNowButton) runCheckNowButton.disabled = true;
        if (runCheckNowText) runCheckNowText.textContent = "Checking...";
    } else if (status.includes("automatic monitoring")) {
        if (statusElement && statusText) {
            statusElement.className = "status auto-monitoring";
            statusText.textContent = "● Automatic Monitoring";
        }
        if (checkingNotice) {
            checkingNotice.classList.add("hidden");
        }
        if (checkButton) checkButton.disabled = false;
        if (checkButtonText) checkButtonText.textContent = "Check All Products";
        if (runCheckNowButton) runCheckNowButton.disabled = false;
        if (runCheckNowText) runCheckNowText.textContent = "Run Check Now";
    } else {
        if (statusElement && statusText) {
            statusElement.className = "status ready";
            statusText.textContent = "Ready";
        }
        if (checkingNotice) {
            checkingNotice.classList.add("hidden");
        }
        if (checkButton) checkButton.disabled = false;
        if (checkButtonText) checkButtonText.textContent = "Check All Products";
        if (runCheckNowButton) runCheckNowButton.disabled = false;
        if (runCheckNowText) runCheckNowText.textContent = "Run Check Now";
    }

    const scheduler = data.scheduler || {};
    const enabled = scheduler.enabled !== undefined ? scheduler.enabled : (data.schedulerEnabled ?? true);
    const intervalMinutes = scheduler.intervalMinutes || data.scheduleIntervalMinutes || 30;

    if (autoMonitoringBadge) {
        if (enabled) {
            autoMonitoringBadge.className = "badge badge-auto-enabled";
            autoMonitoringBadge.textContent = "● Enabled";
        } else {
            autoMonitoringBadge.className = "badge badge-auto-disabled";
            autoMonitoringBadge.textContent = "● Disabled";
        }
    }

    if (autoIntervalText) {
        autoIntervalText.textContent = `${intervalMinutes} minutes`;
    }

    if (scheduler.nextCheck) {
        targetNextCheckTime = new Date(scheduler.nextCheck.split(".")[0]);
        if (nextCheckDetail) {
            nextCheckDetail.textContent = `at ${formatTimeOnly(scheduler.nextCheck)}`;
        }
    } else {
        targetNextCheckTime = null;
        if (nextCheckDetail) {
            nextCheckDetail.textContent = "";
        }
    }

    renderNextCheckCountdown();
}

/**
 * 1-second countdown ticker for the "Next check:" display
 */
function renderNextCheckCountdown() {
    if (!nextCheckCountdown) return;

    if (isCurrentlyChecking) {
        nextCheckCountdown.textContent = "Checking...";
        return;
    }

    if (!targetNextCheckTime || isNaN(targetNextCheckTime.getTime())) {
        nextCheckCountdown.textContent = "Calculating...";
        return;
    }

    const now = new Date();
    const diffMs = targetNextCheckTime.getTime() - now.getTime();
    const diffSec = Math.floor(diffMs / 1000);

    if (diffSec <= 0) {
        nextCheckCountdown.textContent = "Due now";
    } else if (diffSec < 60) {
        nextCheckCountdown.textContent = "< 1 minute";
    } else {
        const mins = Math.floor(diffSec / 60);
        nextCheckCountdown.textContent = `${mins} minute${mins !== 1 ? 's' : ''}`;
    }
}

/**
 * Fetch status once from /api/status
 */
async function fetchStatus() {
    try {
        const res = await fetch("/api/status");
        if (!res.ok) return null;
        const data = await res.json();
        updateStatusAndScheduler(data);
        return data;
    } catch (e) {
        console.warn("Could not fetch status:", e);
        return null;
    }
}

/**
 * Trigger Check All Products via POST /api/check
 */
async function checkProducts() {
    if (checkButton) checkButton.disabled = true;
    if (checkButtonText) checkButtonText.textContent = "Checking...";
    if (runCheckNowButton) runCheckNowButton.disabled = true;
    if (runCheckNowText) runCheckNowText.textContent = "Checking...";

    if (statusElement && statusText) {
        statusElement.className = "status checking";
        statusText.textContent = "Checking Amazon products...";
    }

    if (checkingNotice) {
        checkingNotice.classList.remove("hidden");
    }

    try {
        const response = await fetch("/api/check", {
            method: "POST"
        });

        if (!response.ok && response.status !== 409) {
            throw new Error(`HTTP ${response.status}`);
        }

        hideError();
        waitForCompletion();

    } catch (error) {
        console.error("Error triggering check:", error);
        showError("Failed to trigger product check: " + error.message);

        // Restore actual status
        await fetchStatus();
    }
}

/**
 * Poll GET /api/status until checking completes, then refresh data
 */
function waitForCompletion() {
    if (pollingInterval) clearInterval(pollingInterval);

    pollingInterval = setInterval(async () => {
        try {
            const data = await fetchStatus();
            if (!data) return;

            const isChecking = Boolean(data.checking || data.status === "checking");
            if (!isChecking) {
                clearInterval(pollingInterval);
                pollingInterval = null;

                // Automatically reload structured product cards and summary
                await loadDashboardData();

                // If log is expanded, update it too
                if (logContainer && !logContainer.classList.contains("hidden")) {
                    await loadRawLog();
                }
            }
        } catch (error) {
            console.error("Error polling status:", error);
        }
    }, 2000);
}

// Background idle poller: checks status every 5 seconds to detect automated scheduler runs
setInterval(async () => {
    if (pollingInterval) return; // Already polling actively

    const wasChecking = isCurrentlyChecking;
    const data = await fetchStatus();
    if (data) {
        const isChecking = Boolean(data.checking || data.status === "checking");
        if (wasChecking && !isChecking) {
            // An automatic check just finished! Reload fresh product data
            await loadDashboardData();
            if (logContainer && !logContainer.classList.contains("hidden")) {
                await loadRawLog();
            }
        }
    }
}, 5000);

// 1-second countdown ticker for smooth UI updates
setInterval(renderNextCheckCountdown, 1000);

// Event Listeners
if (checkButton) {
    checkButton.addEventListener("click", checkProducts);
}

if (runCheckNowButton) {
    runCheckNowButton.addEventListener("click", checkProducts);
}

if (refreshButton) {
    refreshButton.addEventListener("click", () => {
        loadDashboardData();
        fetchStatus();
    });
}

// Search (Feature 2)
if (searchInput) {
    searchInput.addEventListener("input", (e) => {
        searchQuery = e.target.value;
        if (clearSearchButton) {
            clearSearchButton.classList.toggle("hidden", !searchQuery.trim());
        }
        applyFiltersAndRender();
    });
}

if (clearSearchButton) {
    clearSearchButton.addEventListener("click", () => {
        if (searchInput) searchInput.value = "";
        searchQuery = "";
        clearSearchButton.classList.add("hidden");
        applyFiltersAndRender();
        if (searchInput) searchInput.focus();
    });
}

// Filter Pills (Feature 3)
filterPills.forEach(pill => {
    pill.addEventListener("click", () => {
        filterPills.forEach(p => {
            p.classList.remove("active");
            p.setAttribute("aria-selected", "false");
        });
        pill.classList.add("active");
        pill.setAttribute("aria-selected", "true");
        currentFilter = pill.getAttribute("data-filter") || "all";
        applyFiltersAndRender();
    });
});

// Sort Dropdown (Feature 4)
if (sortSelect) {
    sortSelect.addEventListener("change", (e) => {
        currentSort = e.target.value;
        applyFiltersAndRender();
    });
}

// Reset Filters Button
if (resetFiltersButton) {
    resetFiltersButton.addEventListener("click", () => {
        if (searchInput) searchInput.value = "";
        if (clearSearchButton) clearSearchButton.classList.add("hidden");
        searchQuery = "";
        currentFilter = "all";
        filterPills.forEach(p => {
            if (p.getAttribute("data-filter") === "all") {
                p.classList.add("active");
                p.setAttribute("aria-selected", "true");
            } else {
                p.classList.remove("active");
                p.setAttribute("aria-selected", "false");
            }
        });
        applyFiltersAndRender();
    });
}

// Product Management (Feature 1)
if (addProductButton) {
    addProductButton.addEventListener("click", openAddProductModal);
}

if (productForm) {
    productForm.addEventListener("submit", handleProductFormSubmit);
}

if (closeProductModalBtn) {
    closeProductModalBtn.addEventListener("click", closeProductModal);
}
if (cancelProductModalBtn) {
    cancelProductModalBtn.addEventListener("click", closeProductModal);
}
if (productModalBackdrop) {
    productModalBackdrop.addEventListener("click", closeProductModal);
}

// Delete Confirmation Modal
if (closeDeleteModalBtn) {
    closeDeleteModalBtn.addEventListener("click", closeDeleteModal);
}
if (cancelDeleteModalBtn) {
    cancelDeleteModalBtn.addEventListener("click", closeDeleteModal);
}
if (deleteModalBackdrop) {
    deleteModalBackdrop.addEventListener("click", closeDeleteModal);
}
if (confirmDeleteModalBtn) {
    confirmDeleteModalBtn.addEventListener("click", handleConfirmDelete);
}

// Refresh Global History (Feature 6)
if (refreshHistoryBtn) {
    refreshHistoryBtn.addEventListener("click", loadGlobalHistory);
}

// Global Escape Key to close any open modal
window.addEventListener("keydown", (e) => {
    if (e.key === "Escape") {
        if (productModal && !productModal.classList.contains("hidden")) {
            closeProductModal();
        }
        if (deleteModal && !deleteModal.classList.contains("hidden")) {
            closeDeleteModal();
        }
        if (historyModal && !historyModal.classList.contains("hidden")) {
            closeHistoryModal();
        }
    }
});

// Initial dashboard load
loadDashboardData();
fetchStatus();