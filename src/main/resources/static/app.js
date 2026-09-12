const checkButton = document.getElementById("checkButton");
const refreshButton = document.getElementById("refreshButton");

const results = document.getElementById("results");
const status = document.getElementById("status");


async function loadLog() {

    try {

        const response = await fetch("/api/log");

        const text = await response.text();

        if (text.trim() === "") {

            results.textContent =
                "No results available yet.\n\nClick \"Check All Products\" to start.";

        } else {

            results.textContent = text;

        }

    } catch (error) {

        results.textContent =
            "Unable to load results.\n\n" + error;

    }

}


async function checkProducts() {

    checkButton.disabled = true;

    status.className = "status checking";
    status.textContent = "● Checking...";

    results.textContent =
        "Amazon checking started...\n\n" +
        "Please wait while products are checked.";


    try {

        const response = await fetch("/api/check", {
            method: "POST"
        });

        const data = await response.json();

        results.textContent =
            data.message +
            "\n\nThe checker is running.\n" +
            "Results will appear automatically.";

        waitForCompletion();

    } catch (error) {

        results.textContent =
            "Error starting checker:\n\n" + error;

        checkButton.disabled = false;

        status.className = "status ready";
        status.textContent = "● Ready";
    }

}


async function waitForCompletion() {

    const interval = setInterval(async () => {

        try {

            const response = await fetch("/api/status");

            const data = await response.json();

            if (data.status === "ready") {

                clearInterval(interval);

                status.className = "status ready";
                status.textContent = "● Ready";

                checkButton.disabled = false;

                await loadLog();

            }

        } catch (error) {

            console.log(error);

        }

    }, 2000);

}


checkButton.addEventListener("click", checkProducts);

refreshButton.addEventListener("click", loadLog);


loadLog();