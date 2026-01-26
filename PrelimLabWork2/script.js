// Hardcoded credentials (as required)
const VALID_USERNAME = "admin";
const VALID_PASSWORD = "1234";

// Beep sound
const beep = new Audio("beep.mp3");

document.getElementById("loginForm").addEventListener("submit", function (e) {
    e.preventDefault();

    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;

    const message = document.getElementById("message");
    const timestampDisplay = document.getElementById("timestamp");

    if (username === VALID_USERNAME && password === VALID_PASSWORD) {
        // Successful login
        message.textContent = "Welcome, " + username + "!";
        message.style.color = "green";

        const now = new Date();
        const timestamp = formatDate(now);

        timestampDisplay.textContent = "Login Time: " + timestamp;

        generateAttendanceFile(username, timestamp);

    } else {
        // Failed login
        message.textContent = "Incorrect username or password.";
        message.style.color = "red";
        timestampDisplay.textContent = "";

        beep.play();
    }
});

// Format date as MM/DD/YYYY HH:MM:SS
function formatDate(date) {
    const pad = num => num.toString().padStart(2, "0");

    return (
        pad(date.getMonth() + 1) + "/" +
        pad(date.getDate()) + "/" +
        date.getFullYear() + " " +
        pad(date.getHours()) + ":" +
        pad(date.getMinutes()) + ":" +
        pad(date.getSeconds())
    );
}

// Generate attendance file
function generateAttendanceFile(username, timestamp) {
    const attendanceData =
        "Attendance Summary\n\n" +
        "Username: " + username + "\n" +
        "Timestamp: " + timestamp + "\n";

    const blob = new Blob([attendanceData], { type: "text/plain" });
    const link = document.createElement("a");

    link.href = URL.createObjectURL(blob);
    link.download = "attendance_summary.txt";

    link.click();
}
