// ===================================================================
// Phase 8: Cloud Deployment & Configurable Backend API
// ===================================================================
const apiConfigBtn = document.getElementById("apiConfigBtn");
const apiEndpointBadge = document.getElementById("apiEndpointBadge");
const apiSettingsModal = document.getElementById("apiSettingsModal");
const apiSettingsModalBackdrop = document.getElementById("apiSettingsModalBackdrop");
const closeApiSettingsModalBtn = document.getElementById("closeApiSettingsModalBtn");
const apiBaseUrlInput = document.getElementById("apiBaseUrlInput");
const apiTestResult = document.getElementById("apiTestResult");
const apiTestStatusIcon = document.getElementById("apiTestStatusIcon");
const apiTestStatusMsg = document.getElementById("apiTestStatusMsg");
const testApiConnectionBtn = document.getElementById("testApiConnectionBtn");
const testApiBtnText = document.getElementById("testApiBtnText");
const saveApiSettingsBtn = document.getElementById("saveApiSettingsBtn");

function getApiBaseUrl() {
    if (typeof window !== "undefined") {
        if (window.API_BASE_URL) return String(window.API_BASE_URL).replace(/\/+$/, "");
        const stored = localStorage.getItem("API_BASE_URL");
        if (stored && stored.trim()) return stored.trim().replace(/\/+$/, "");
    }
    return "";
}

function setApiBaseUrl(url) {
    if (url && url.trim()) {
        localStorage.setItem("API_BASE_URL", url.trim().replace(/\/+$/, ""));
    } else {
        localStorage.removeItem("API_BASE_URL");
    }
    updateApiEndpointBadge();
}

function apiUrl(path) {
    const base = getApiBaseUrl();
    const cleanPath = path.startsWith("/") ? path : `/${path}`;
    return base ? `${base}${cleanPath}` : cleanPath;
}

function getAssetUrl(url) {
    if (!url) return null;
    if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("data:")) {
        return url;
    }
    const base = getApiBaseUrl();
    const cleanPath = url.startsWith("/") ? url : `/${url}`;
    return base ? `${base}${cleanPath}` : cleanPath;
}

function updateApiEndpointBadge() {
    if (!apiEndpointBadge) return;
    const base = getApiBaseUrl();
    if (!base) {
        apiEndpointBadge.textContent = "API: Default";
    } else {
        try {
            const parsed = new URL(base);
            apiEndpointBadge.textContent = `API: ${parsed.hostname}`;
        } catch {
            apiEndpointBadge.textContent = "API: Custom";
        }
    }
}

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

// Smart Add Product (Phase 6) Elements
const modalModeTabs = document.getElementById("modalModeTabs");
const tabModeLink = document.getElementById("tabModeLink");
const tabModeName = document.getElementById("tabModeName");
const tabModeImage = document.getElementById("tabModeImage");
const modeLinkSection = document.getElementById("modeLinkSection");
const modeNameSection = document.getElementById("modeNameSection");
const modeImageSection = document.getElementById("modeImageSection");
const productModalSubtitle = document.getElementById("productModalSubtitle");

const searchByNameInput = document.getElementById("searchByNameInput");
const findProductByNameBtn = document.getElementById("findProductByNameBtn");
const findProductByNameText = document.getElementById("findProductByNameText");

const imageDropzone = document.getElementById("imageDropzone");
const productImageFileInput = document.getElementById("productImageFileInput");
const imagePreviewBox = document.getElementById("imagePreviewBox");
const imagePreviewImg = document.getElementById("imagePreviewImg");
const imageFileName = document.getElementById("imageFileName");
const imageFileSize = document.getElementById("imageFileSize");
const removeImageBtn = document.getElementById("removeImageBtn");
const imageDetectedSection = document.getElementById("imageDetectedSection");
const imageDetectedNameInput = document.getElementById("imageDetectedNameInput");
const findProductByImageBtn = document.getElementById("findProductByImageBtn");
const findProductByImageText = document.getElementById("findProductByImageText");

const searchResultsArea = document.getElementById("searchResultsArea");
const searchResultsCount = document.getElementById("searchResultsCount");
const searchSpinner = document.getElementById("searchSpinner");
const searchEmptyState = document.getElementById("searchEmptyState");
const searchResultsList = document.getElementById("searchResultsList");
const productModalSuccess = document.getElementById("productModalSuccess");
const productModalSuccessMsg = document.getElementById("productModalSuccessMsg");

let currentAddMode = "link";
let currentSearchResults = [];

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

// Phase 9 Multi-Store & Price Comparison Elements
const storePills = document.querySelectorAll(".store-pill");
const comparePricesBtn = document.getElementById("comparePricesBtn");
const productStoreSelect = document.getElementById("productStoreSelect");
const searchStoreFilter = document.getElementById("searchStoreFilter");
const comparisonModal = document.getElementById("comparisonModal");
const comparisonModalBackdrop = document.getElementById("comparisonModalBackdrop");
const closeComparisonModalBtn = document.getElementById("closeComparisonModalBtn");
const closeComparisonFooterBtn = document.getElementById("closeComparisonFooterBtn");
const comparisonQueryInput = document.getElementById("comparisonQueryInput");
const executeComparisonBtn = document.getElementById("executeComparisonBtn");
const executeComparisonText = document.getElementById("executeComparisonText");
const comparisonLoading = document.getElementById("comparisonLoading");
const monitoredComparisonsSection = document.getElementById("monitoredComparisonsSection");
const monitoredComparisonsList = document.getElementById("monitoredComparisonsList");
const monitoredComparisonsEmpty = document.getElementById("monitoredComparisonsEmpty");
const liveComparisonSection = document.getElementById("liveComparisonSection");
const liveComparisonResult = document.getElementById("liveComparisonResult");

// State
let allProducts = [];
let currentFilter = "all";
let currentStoreFilter = "all";
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
            fetch(apiUrl("/api/products")),
            fetch(apiUrl("/api/summary"))
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

    // 2b. Filter by store (Phase 9)
    if (currentStoreFilter && currentStoreFilter !== "all") {
        filtered = filtered.filter(p => (p.store || "AMAZON").toUpperCase() === currentStoreFilter.toUpperCase());
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
        const screenshotSrc = hasScreenshot ? getAssetUrl(product.screenshot) : null;
        const hasUrl = Boolean(product.productUrl && product.productUrl.trim() !== "");
        const store = product.store || "AMAZON";
        const isFlipkart = store.toUpperCase() === "FLIPKART";
        const storeClass = isFlipkart ? "store-badge-flipkart" : "store-badge-amazon";
        const storeLabel = isFlipkart ? "Flipkart" : "Amazon";

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

                <div class="card-store-indicator">
                    <span class="store-badge ${storeClass}">${escapeHtml(storeLabel)}</span>
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
                    <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 8px; width: 100%;">
                        <h3 class="product-title" title="${escapeHtml(product.name)}">${escapeHtml(product.name)}</h3>
                    </div>
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
        const res = await fetch(apiUrl(`/api/products/${encodeURIComponent(productId)}/history`));
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
        const res = await fetch(apiUrl("/api/history?limit=25"));
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

            const entryStore = (entry.store || "AMAZON").toUpperCase();
            const storeBadgeClass = entryStore === "FLIPKART" ? "store-badge-flipkart" : "store-badge-amazon";
            const storeName = entryStore === "FLIPKART" ? "Flipkart" : "Amazon";

            tr.innerHTML = `
                <td>
                    <div style="display: flex; align-items: center; gap: 8px;">
                        <span class="store-badge ${storeBadgeClass}">${escapeHtml(storeName)}</span>
                        <strong>${escapeHtml(entry.productName || "Unknown Product")}</strong>
                    </div>
                </td>
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
/**
 * Modal Alert Helpers
 */
function showModalError(msg) {
    if (productModalError && productModalErrorMsg) {
        productModalErrorMsg.textContent = msg;
        productModalError.classList.remove("hidden");
    }
    if (productModalSuccess) productModalSuccess.classList.add("hidden");
}

function showModalSuccess(msg) {
    if (productModalSuccess && productModalSuccessMsg) {
        productModalSuccessMsg.textContent = msg;
        productModalSuccess.classList.remove("hidden");
    }
    if (productModalError) productModalError.classList.add("hidden");
}

function clearModalAlerts() {
    if (productModalError) productModalError.classList.add("hidden");
    if (productModalSuccess) productModalSuccess.classList.add("hidden");
}

/**
 * Switch Smart Add Product Mode (Link, Name, Image)
 */
function switchAddProductMode(mode) {
    currentAddMode = mode;
    clearModalAlerts();

    if (searchResultsArea) searchResultsArea.classList.add("hidden");
    if (searchResultsList) searchResultsList.innerHTML = "";

    const tabs = [
        { btn: tabModeLink, section: modeLinkSection, name: "link" },
        { btn: tabModeName, section: modeNameSection, name: "name" },
        { btn: tabModeImage, section: modeImageSection, name: "image" }
    ];

    tabs.forEach(t => {
        if (t.btn && t.section) {
            const isActive = t.name === mode;
            t.btn.classList.toggle("active", isActive);
            t.btn.setAttribute("aria-selected", isActive ? "true" : "false");
            t.section.classList.toggle("hidden", !isActive);
        }
    });

    if (mode === "link" && productNameInput) {
        productNameInput.focus();
    } else if (mode === "name" && searchByNameInput) {
        searchByNameInput.focus();
    }
}

/**
 * Open Smart Add Product Modal (Feature 1 & Phase 6)
 */
function openAddProductModal() {
    if (!productModal) return;
    if (editProductId) editProductId.value = "";
    if (productNameInput) productNameInput.value = "";
    if (productUrlInput) productUrlInput.value = "";
    if (searchByNameInput) searchByNameInput.value = "";
    if (imageDetectedNameInput) imageDetectedNameInput.value = "";

    // Reset Image Dropzone & Preview
    if (productImageFileInput) productImageFileInput.value = "";
    if (imagePreviewBox) imagePreviewBox.classList.add("hidden");
    if (imageDropzone) imageDropzone.classList.remove("hidden");
    if (imageDetectedSection) imageDetectedSection.classList.add("hidden");
    if (productStoreSelect) productStoreSelect.value = "AUTO";

    // Hide search results
    if (searchResultsArea) searchResultsArea.classList.add("hidden");
    if (searchResultsList) searchResultsList.innerHTML = "";

    clearModalAlerts();

    if (productModalTitle) productModalTitle.textContent = "Add Monitored Product";
    if (productModalSubtitle) productModalSubtitle.textContent = "Choose how to add your product: by link, search, or image";
    if (modalModeTabs) modalModeTabs.classList.remove("hidden");
    if (saveProductModalBtn) saveProductModalBtn.textContent = "Save Product";

    switchAddProductMode("link");

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

    clearModalAlerts();

    if (editProductId) editProductId.value = id;
    if (productNameInput) productNameInput.value = product.name || "";
    if (productUrlInput) productUrlInput.value = product.productUrl || "";
    if (productStoreSelect) productStoreSelect.value = product.store || "AUTO";

    if (productModalTitle) productModalTitle.textContent = "Edit Product";
    if (productModalSubtitle) productModalSubtitle.textContent = "Update product name or product URL";
    if (modalModeTabs) modalModeTabs.classList.add("hidden");

    // Show only link form
    if (modeLinkSection) modeLinkSection.classList.remove("hidden");
    if (modeNameSection) modeNameSection.classList.add("hidden");
    if (modeImageSection) modeImageSection.classList.add("hidden");
    if (searchResultsArea) searchResultsArea.classList.add("hidden");

    if (saveProductModalBtn) saveProductModalBtn.textContent = "Update Product";

    productModal.classList.remove("hidden");
    if (productNameInput) productNameInput.focus();
}
window.openEditProductModal = openEditProductModal;

/**
 * Close Product Modal
 */
function closeProductModal() {
    if (productModal) productModal.classList.add("hidden");
    clearModalAlerts();
    if (productForm) productForm.reset();
}

/**
 * Execute real Amazon product search via backend
 */
async function executeAmazonSearch(query, triggerBtn, triggerTextEl) {
    const q = (query || "").trim();
    if (!q) {
        showModalError("Please enter a product name or keyword to search.");
        return;
    }

    clearModalAlerts();
    if (triggerBtn) triggerBtn.disabled = true;
    if (triggerTextEl) triggerTextEl.textContent = "Searching...";

    if (searchResultsArea) searchResultsArea.classList.remove("hidden");
    if (searchSpinner) searchSpinner.classList.remove("hidden");
    if (searchEmptyState) searchEmptyState.classList.add("hidden");
    if (searchResultsList) searchResultsList.innerHTML = "";
    if (searchResultsCount) searchResultsCount.textContent = "Searching...";

    try {
        const storeFilter = searchStoreFilter ? searchStoreFilter.value : "all";
        const res = await fetch(apiUrl(`/api/products/search?query=${encodeURIComponent(q)}&store=${encodeURIComponent(storeFilter)}`));
        if (!res.ok) {
            let errorMsg = `Search failed (HTTP ${res.status})`;
            try {
                const errData = await res.json();
                if (errData.message) errorMsg = errData.message;
            } catch (_) {}
            throw new Error(errorMsg);
        }

        const items = await res.json();
        currentSearchResults = Array.isArray(items) ? items : [];

        if (currentSearchResults.length === 0) {
            if (searchEmptyState) searchEmptyState.classList.remove("hidden");
            if (searchResultsCount) searchResultsCount.textContent = "0 results";
        } else {
            if (searchEmptyState) searchEmptyState.classList.add("hidden");
            if (searchResultsCount) searchResultsCount.textContent = `${currentSearchResults.length} result${currentSearchResults.length !== 1 ? 's' : ''}`;
            renderSearchResults(currentSearchResults);
        }

    } catch (err) {
        console.error("Product search error:", err);
        showModalError(err.message || "Failed to search products");
        if (searchResultsArea) searchResultsArea.classList.add("hidden");
    } finally {
        if (searchSpinner) searchSpinner.classList.add("hidden");
        if (triggerBtn) triggerBtn.disabled = false;
        if (triggerTextEl) triggerTextEl.textContent = "Find Product";
    }
}

/**
 * Render real multi-store search result items
 */
function renderSearchResults(items) {
    if (!searchResultsList) return;
    searchResultsList.innerHTML = "";

    items.forEach((item, index) => {
        const card = document.createElement("div");
        card.className = "search-result-card";

        const badgeClass = getBadgeClass(item.availability);
        const priceDisplay = item.formattedPrice ? item.formattedPrice : (item.price ? '₹' + Number(item.price).toFixed(2) : 'Price unavailable');
        const hasImg = Boolean(item.imageUrl && item.imageUrl.trim() !== "");
        const itemStore = (item.store || "AMAZON").toUpperCase();
        const isFlipkart = itemStore === "FLIPKART";
        const storeBadgeClass = isFlipkart ? "store-badge-flipkart" : "store-badge-amazon";
        const storeLabel = isFlipkart ? "Flipkart" : "Amazon";

        card.innerHTML = `
            <div class="search-result-img-wrapper">
                ${hasImg ? `
                    <img src="${escapeHtml(item.imageUrl)}" alt="${escapeHtml(item.name)}" loading="lazy"
                         onerror="this.style.display='none'; this.nextElementSibling.style.display='block';" />
                    <svg class="media-placeholder" style="display:none; width:28px; height:28px; color:var(--text-muted);" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                        <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                        <circle cx="8.5" cy="8.5" r="1.5"></circle>
                        <polyline points="21 15 16 10 5 21"></polyline>
                    </svg>
                ` : `
                    <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" style="color:var(--text-muted);">
                        <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                        <circle cx="8.5" cy="8.5" r="1.5"></circle>
                        <polyline points="21 15 16 10 5 21"></polyline>
                    </svg>
                `}
            </div>
            <div class="search-result-info">
                <h4 class="search-result-title" title="${escapeHtml(item.name)}">${escapeHtml(item.name)}</h4>
                <div class="search-result-meta">
                    <span class="store-badge ${storeBadgeClass}">${escapeHtml(storeLabel)}</span>
                    <span class="search-result-price">${escapeHtml(priceDisplay)}</span>
                    <span class="badge ${badgeClass}" style="font-size: 0.72rem; padding: 2px 8px;">${escapeHtml(item.availability || 'IN STOCK')}</span>
                </div>
                ${item.url ? `
                    <a href="${escapeHtml(item.url)}" target="_blank" rel="noopener noreferrer" class="search-result-link">
                        <span>View on ${escapeHtml(storeLabel)}</span>
                        <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"></path><polyline points="15 3 21 3 21 9"></polyline><line x1="10" y1="14" x2="21" y2="3"></line></svg>
                    </a>
                ` : ''}
            </div>
            <div class="search-result-action">
                <button type="button" class="btn btn-primary btn-sm btn-select-product" onclick="selectSearchResult(${index})">
                    Select Product
                </button>
            </div>
        `;

        searchResultsList.appendChild(card);
    });
}

/**
 * Handle user selecting a search result to monitor
 */
async function selectSearchResult(index) {
    const item = currentSearchResults[index];
    if (!item) return;

    clearModalAlerts();

    const selectButtons = document.querySelectorAll(".btn-select-product");
    const targetBtn = selectButtons[index];
    if (targetBtn) {
        targetBtn.disabled = true;
        targetBtn.textContent = "Adding...";
    }

    try {
        const res = await fetch(apiUrl("/api/products"), {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                name: item.name,
                url: item.url,
                store: item.store || "AMAZON"
            })
        });

        if (!res.ok) {
            let errorMsg = `Failed to add product (${res.status})`;
            try {
                const errData = await res.json();
                if (errData.message) errorMsg = errData.message;
                else if (errData.error) errorMsg = errData.error;
            } catch (_) {}
            showModalError(errorMsg);
            if (targetBtn) {
                targetBtn.disabled = false;
                targetBtn.textContent = "Select Product";
            }
            return;
        }

        showModalSuccess(`"${item.name}" added successfully to monitoring!`);
        setTimeout(() => {
            closeProductModal();
            loadDashboardData();
        }, 700);

    } catch (err) {
        showModalError("Network error: " + err.message);
        if (targetBtn) {
            targetBtn.disabled = false;
            targetBtn.textContent = "Select Product";
        }
    }
}
window.selectSearchResult = selectSearchResult;

/**
 * Handle Product Image File (validation, preview, and smart title extraction)
 */
function handleImageFile(file) {
    if (!file) return;

    clearModalAlerts();

    // 1. Format validation
    const validTypes = ["image/png", "image/jpeg", "image/jpg", "image/webp"];
    if (!validTypes.includes(file.type.toLowerCase()) && !file.name.match(/\.(png|jpe?g|webp)$/i)) {
        showModalError("Unsupported image format. Please upload a PNG, JPG, or WEBP image.");
        return;
    }

    // 2. Size validation (max 5MB)
    const maxSize = 5 * 1024 * 1024;
    if (file.size > maxSize) {
        showModalError("Image too large. Maximum allowed size is 5MB.");
        return;
    }

    // 3. Image preview via FileReader
    const reader = new FileReader();
    reader.onload = (e) => {
        if (imagePreviewImg) imagePreviewImg.src = e.target.result;
        if (imagePreviewBox) imagePreviewBox.classList.remove("hidden");
        if (imageDropzone) imageDropzone.classList.add("hidden");

        const sizeKb = Math.round(file.size / 1024);
        const sizeStr = sizeKb >= 1024 ? (sizeKb / 1024).toFixed(1) + " MB" : sizeKb + " KB";
        if (imageFileName) imageFileName.textContent = file.name;
        if (imageFileSize) imageFileSize.textContent = sizeStr;

        // 4. Intelligent product name extraction from filename
        let cleanName = file.name
            .replace(/\.[^/.]+$/, "")             // strip extension
            .replace(/[_\-]+/g, " ")             // replace underscores and hyphens
            .replace(/\b\d{8,}\b/g, "")           // strip timestamp numbers like 20260912
            .replace(/\s+/g, " ")                // collapse multiple spaces
            .trim();

        // Capitalize first letter of words
        if (cleanName.length > 2) {
            cleanName = cleanName.split(" ")
                .map(w => w.charAt(0).toUpperCase() + w.slice(1))
                .join(" ");
        }

        if (imageDetectedNameInput) {
            imageDetectedNameInput.value = cleanName;
        }
        if (imageDetectedSection) {
            imageDetectedSection.classList.remove("hidden");
            if (imageDetectedNameInput) imageDetectedNameInput.focus();
        }
    };
    reader.readAsDataURL(file);
}

/**
 * Handle Add/Edit Product form submit (Link mode)
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

        const payload = { name: name, url: url };
        if (productStoreSelect && productStoreSelect.value && productStoreSelect.value !== "AUTO") {
            payload.store = productStoreSelect.value;
        }

        const res = await fetch(apiUrl(endpoint), {
            method: method,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
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

        showModalSuccess(isEdit ? "Product updated successfully!" : "Product added successfully!");
        setTimeout(() => {
            closeProductModal();
            loadDashboardData();
        }, 600);

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
        const res = await fetch(apiUrl(`/api/products/${encodeURIComponent(productPendingDelete)}`), {
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
        const response = await fetch(apiUrl("/api/log"));
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
        const res = await fetch(apiUrl("/api/status"));
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
        const response = await fetch(apiUrl("/api/check"), {
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

// Phase 6: Mode Tabs
if (tabModeLink) {
    tabModeLink.addEventListener("click", () => switchAddProductMode("link"));
}
if (tabModeName) {
    tabModeName.addEventListener("click", () => switchAddProductMode("name"));
}
if (tabModeImage) {
    tabModeImage.addEventListener("click", () => switchAddProductMode("image"));
}

// Phase 6: Option 2 - Search by Name
if (findProductByNameBtn) {
    findProductByNameBtn.addEventListener("click", () => {
        const query = searchByNameInput ? searchByNameInput.value : "";
        executeAmazonSearch(query, findProductByNameBtn, findProductByNameText);
    });
}
if (searchByNameInput) {
    searchByNameInput.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
            e.preventDefault();
            executeAmazonSearch(searchByNameInput.value, findProductByNameBtn, findProductByNameText);
        }
    });
}

// Phase 6: Option 3 - Image Upload & Dropzone
if (imageDropzone && productImageFileInput) {
    imageDropzone.addEventListener("click", () => {
        productImageFileInput.click();
    });

    imageDropzone.addEventListener("dragover", (e) => {
        e.preventDefault();
        imageDropzone.classList.add("dragover");
    });

    imageDropzone.addEventListener("dragenter", (e) => {
        e.preventDefault();
        imageDropzone.classList.add("dragover");
    });

    imageDropzone.addEventListener("dragleave", (e) => {
        e.preventDefault();
        imageDropzone.classList.remove("dragover");
    });

    imageDropzone.addEventListener("drop", (e) => {
        e.preventDefault();
        imageDropzone.classList.remove("dragover");
        if (e.dataTransfer && e.dataTransfer.files && e.dataTransfer.files.length > 0) {
            handleImageFile(e.dataTransfer.files[0]);
        }
    });
}

if (productImageFileInput) {
    productImageFileInput.addEventListener("change", (e) => {
        if (e.target.files && e.target.files.length > 0) {
            handleImageFile(e.target.files[0]);
        }
    });
}

if (removeImageBtn) {
    removeImageBtn.addEventListener("click", () => {
        if (productImageFileInput) productImageFileInput.value = "";
        if (imagePreviewBox) imagePreviewBox.classList.add("hidden");
        if (imagePreviewImg) imagePreviewImg.src = "";
        if (imageDropzone) imageDropzone.classList.remove("hidden");
        if (imageDetectedSection) imageDetectedSection.classList.add("hidden");
        if (imageDetectedNameInput) imageDetectedNameInput.value = "";
        if (searchResultsArea) searchResultsArea.classList.add("hidden");
        if (searchResultsList) searchResultsList.innerHTML = "";
        currentSearchResults = [];
    });
}

if (findProductByImageBtn) {
    findProductByImageBtn.addEventListener("click", () => {
        const query = imageDetectedNameInput ? imageDetectedNameInput.value : "";
        executeAmazonSearch(query, findProductByImageBtn, findProductByImageText);
    });
}
if (imageDetectedNameInput) {
    imageDetectedNameInput.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
            e.preventDefault();
            executeAmazonSearch(imageDetectedNameInput.value, findProductByImageBtn, findProductByImageText);
        }
    });
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

// Cloud API Settings Modal (Phase 8)
function openApiSettingsModal() {
    if (!apiSettingsModal) return;
    if (apiBaseUrlInput) {
        apiBaseUrlInput.value = getApiBaseUrl();
    }
    if (apiTestResult) {
        apiTestResult.className = "api-test-result hidden";
        if (apiTestStatusMsg) apiTestStatusMsg.textContent = "";
    }
    apiSettingsModal.classList.remove("hidden");
    if (apiBaseUrlInput) apiBaseUrlInput.focus();
}

function closeApiSettingsModal() {
    if (apiSettingsModal) {
        apiSettingsModal.classList.add("hidden");
    }
}

async function testApiConnection() {
    const rawUrl = apiBaseUrlInput ? apiBaseUrlInput.value.trim().replace(/\/+$/, "") : "";
    const testEndpoint = rawUrl ? `${rawUrl}/api/health` : "/api/health";

    if (testApiBtnText) testApiBtnText.textContent = "Testing...";
    if (testApiConnectionBtn) testApiConnectionBtn.disabled = true;

    if (apiTestResult) {
        apiTestResult.className = "api-test-result testing";
        if (apiTestStatusIcon) apiTestStatusIcon.textContent = "⏳";
        if (apiTestStatusMsg) apiTestStatusMsg.textContent = `Connecting to ${testEndpoint}...`;
        apiTestResult.classList.remove("hidden");
    }

    try {
        const res = await fetch(testEndpoint, { method: "GET" });
        if (!res.ok) {
            throw new Error(`HTTP ${res.status} ${res.statusText}`);
        }
        const data = await res.json();
        if (apiTestResult) {
            apiTestResult.className = "api-test-result success";
            if (apiTestStatusIcon) apiTestStatusIcon.textContent = "✔";
            const dbInfo = data.database ? ` (DB: ${data.database})` : "";
            if (apiTestStatusMsg) apiTestStatusMsg.textContent = `Connected successfully! Status: ${data.status || "UP"}${dbInfo}`;
        }
    } catch (err) {
        if (apiTestResult) {
            apiTestResult.className = "api-test-result error";
            if (apiTestStatusIcon) apiTestStatusIcon.textContent = "✖";
            if (apiTestStatusMsg) apiTestStatusMsg.textContent = `Connection failed: ${err.message}. Verify that the backend is running and CORS allows this origin.`;
        }
    } finally {
        if (testApiBtnText) testApiBtnText.textContent = "Test Connection";
        if (testApiConnectionBtn) testApiConnectionBtn.disabled = false;
    }
}

function handleSaveApiSettings() {
    const url = apiBaseUrlInput ? apiBaseUrlInput.value.trim() : "";
    setApiBaseUrl(url);
    closeApiSettingsModal();
    // Reload data using new base url
    loadDashboardData();
    fetchStatus();
}

if (apiConfigBtn) {
    apiConfigBtn.addEventListener("click", openApiSettingsModal);
}
if (closeApiSettingsModalBtn) {
    closeApiSettingsModalBtn.addEventListener("click", closeApiSettingsModal);
}
if (apiSettingsModalBackdrop) {
    apiSettingsModalBackdrop.addEventListener("click", closeApiSettingsModal);
}
if (testApiConnectionBtn) {
    testApiConnectionBtn.addEventListener("click", testApiConnection);
}
if (saveApiSettingsBtn) {
    saveApiSettingsBtn.addEventListener("click", handleSaveApiSettings);
}

// Phase 9: Store Filter Pills
if (storePills && storePills.length > 0) {
    storePills.forEach(pill => {
        pill.addEventListener("click", () => {
            storePills.forEach(p => {
                p.classList.remove("active");
                p.setAttribute("aria-selected", "false");
            });
            pill.classList.add("active");
            pill.setAttribute("aria-selected", "true");
            currentStoreFilter = pill.getAttribute("data-store") || "all";
            applyFiltersAndRender();
        });
    });
}

// Phase 9: Cross-Store Price Comparison Functions
function createComparisonCard(comp) {
    const card = document.createElement("div");
    card.className = "comparison-card";

    let headerHtml = `
        <div class="comparison-card-header">
            <h4 class="comparison-product-title">${escapeHtml(comp.productName)}</h4>
            ${comp.priceDifference && comp.priceDifference > 0 ? `
                <span class="savings-badge">
                    Save ₹${escapeHtml(comp.priceDifference.toFixed(2))}
                </span>
            ` : ""}
        </div>
    `;

    let storesHtml = `<div class="comparison-stores-row">`;
    if (Array.isArray(comp.stores)) {
        comp.stores.forEach(item => {
            const isLowest = item.lowest || (comp.lowestPriceStore && item.store === comp.lowestPriceStore && comp.lowestPrice === item.price);
            const storeName = item.store === "FLIPKART" ? "Flipkart" : "Amazon";
            const badgeClass = item.store === "FLIPKART" ? "store-badge-flipkart" : "store-badge-amazon";
            const statusClass = getBadgeClass(item.status);
            const formattedPrice = item.formattedPrice || (item.price !== null && item.price !== undefined ? formatPrice(item.price) : "Price not found");

            storesHtml += `
                <div class="comparison-store-item ${isLowest ? 'is-lowest' : ''}">
                    <div class="comparison-store-header">
                        <span class="store-badge ${badgeClass}">${escapeHtml(storeName)}</span>
                        ${isLowest ? '<span class="lowest-tag">Lowest Price</span>' : ''}
                    </div>
                    <div class="comparison-store-price">${escapeHtml(formattedPrice)}</div>
                    <div class="comparison-store-status">
                        <span class="badge ${statusClass}">${escapeHtml(item.status || 'UNKNOWN')}</span>
                    </div>
                    ${item.productUrl ? `
                        <a href="${escapeHtml(item.productUrl)}" target="_blank" rel="noopener noreferrer" class="btn btn-outline btn-sm" style="margin-top: 8px;">
                            View on ${escapeHtml(storeName)}
                        </a>
                    ` : ''}
                </div>
            `;
        });
    }
    storesHtml += `</div>`;

    let diffSummary = "";
    if (comp.priceDifference && comp.priceDifference > 0 && comp.lowestPriceStore && comp.highestPriceStore) {
        const lowestName = comp.lowestPriceStore === "FLIPKART" ? "Flipkart" : "Amazon";
        const highestName = comp.highestPriceStore === "FLIPKART" ? "Flipkart" : "Amazon";
        diffSummary = `
            <div class="comparison-difference-summary" style="margin-top: 10px; font-size: 0.85rem; color: var(--text-muted);">
                <strong>${escapeHtml(lowestName)}</strong> is cheaper by <strong style="color: var(--success);">₹${escapeHtml(comp.priceDifference.toFixed(2))}</strong> compared to ${escapeHtml(highestName)}.
            </div>
        `;
    }

    card.innerHTML = headerHtml + storesHtml + diffSummary;
    return card;
}

async function openPriceComparisonModal() {
    if (!comparisonModal) return;
    comparisonModal.classList.remove("hidden");

    if (comparisonQueryInput) comparisonQueryInput.value = "";
    if (liveComparisonSection) liveComparisonSection.classList.add("hidden");
    if (liveComparisonResult) liveComparisonResult.innerHTML = "";

    await loadMonitoredComparisons();
}

function closePriceComparisonModal() {
    if (comparisonModal) comparisonModal.classList.add("hidden");
}

async function loadMonitoredComparisons() {
    if (!monitoredComparisonsList) return;
    monitoredComparisonsList.innerHTML = "";
    if (comparisonLoading) comparisonLoading.classList.remove("hidden");
    if (monitoredComparisonsEmpty) monitoredComparisonsEmpty.classList.add("hidden");

    try {
        const res = await fetch(apiUrl("/api/comparison"));
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const comps = await res.json();

        if (!Array.isArray(comps) || comps.length === 0) {
            if (monitoredComparisonsEmpty) monitoredComparisonsEmpty.classList.remove("hidden");
            return;
        }

        comps.forEach(c => {
            monitoredComparisonsList.appendChild(createComparisonCard(c));
        });
    } catch (e) {
        console.error("Failed to load comparisons:", e);
        if (monitoredComparisonsEmpty) {
            monitoredComparisonsEmpty.innerHTML = `<p style="color: var(--danger);">Failed to load comparisons: ${escapeHtml(e.message)}</p>`;
            monitoredComparisonsEmpty.classList.remove("hidden");
        }
    } finally {
        if (comparisonLoading) comparisonLoading.classList.add("hidden");
    }
}

async function executeLiveComparison() {
    const q = comparisonQueryInput ? comparisonQueryInput.value.trim() : "";
    if (!q) return;

    if (executeComparisonBtn) executeComparisonBtn.disabled = true;
    if (executeComparisonText) executeComparisonText.textContent = "Comparing...";
    if (comparisonLoading) comparisonLoading.classList.remove("hidden");
    if (liveComparisonSection) liveComparisonSection.classList.add("hidden");
    if (liveComparisonResult) liveComparisonResult.innerHTML = "";

    try {
        const res = await fetch(apiUrl(`/api/comparison/live?query=${encodeURIComponent(q)}`));
        if (!res.ok) {
            throw new Error(`Comparison returned HTTP ${res.status}`);
        }
        const comp = await res.json();
        if (comp && Array.isArray(comp.stores) && comp.stores.length > 0) {
            liveComparisonResult.appendChild(createComparisonCard(comp));
            liveComparisonSection.classList.remove("hidden");
        } else {
            liveComparisonResult.innerHTML = `<p class="empty-state-text" style="padding: 16px;">No comparable items found across stores for "${escapeHtml(q)}".</p>`;
            liveComparisonSection.classList.remove("hidden");
        }
    } catch (e) {
        console.error("Live comparison error:", e);
        liveComparisonResult.innerHTML = `<p style="color: var(--danger); padding: 16px;">Failed to perform live comparison: ${escapeHtml(e.message)}</p>`;
        liveComparisonSection.classList.remove("hidden");
    } finally {
        if (comparisonLoading) comparisonLoading.classList.add("hidden");
        if (executeComparisonBtn) executeComparisonBtn.disabled = false;
        if (executeComparisonText) executeComparisonText.textContent = "Compare";
    }
}

// Comparison Modal Listeners
if (comparePricesBtn) {
    comparePricesBtn.addEventListener("click", openPriceComparisonModal);
}
if (closeComparisonModalBtn) {
    closeComparisonModalBtn.addEventListener("click", closePriceComparisonModal);
}
if (closeComparisonFooterBtn) {
    closeComparisonFooterBtn.addEventListener("click", closePriceComparisonModal);
}
if (comparisonModalBackdrop) {
    comparisonModalBackdrop.addEventListener("click", closePriceComparisonModal);
}
if (executeComparisonBtn) {
    executeComparisonBtn.addEventListener("click", executeLiveComparison);
}
if (comparisonQueryInput) {
    comparisonQueryInput.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
            e.preventDefault();
            executeLiveComparison();
        }
    });
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
        if (apiSettingsModal && !apiSettingsModal.classList.contains("hidden")) {
            closeApiSettingsModal();
        }
        if (comparisonModal && !comparisonModal.classList.contains("hidden")) {
            closePriceComparisonModal();
        }
    }
});

// Initialize UI badge
updateApiEndpointBadge();

// Initial dashboard load
loadDashboardData();
fetchStatus();