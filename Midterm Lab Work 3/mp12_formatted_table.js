/*
 * Student Name: CANTAROS, RICH ANDREI
 * Machine Problem: MP12 - Display dataset in formatted table output
 * Language: JavaScript (Node.js)
 *
 * Description:
 * This program asks the user for the CSV dataset file path first.
 * It reads the file using Node.js, parses the CSV data, and stores valid rows.
 * It then displays the dataset in a clean formatted table.
 * Blank unnamed columns and the placeholder Column1 are ignored for readability.
 */

const fs = require("fs");
const readline = require("readline");

const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});

function askQuestion(question) {
    return new Promise(resolve => rl.question(question, resolve));
}

function parseCSVLine(line) {
    const fields = [];
    let currentField = "";
    let inQuotes = false;

    for (let i = 0; i < line.length; i++) {
        const ch = line[i];

        if (ch === '"') {
            if (inQuotes && i + 1 < line.length && line[i + 1] === '"') {
                currentField += '"';
                i++;
            } else {
                inQuotes = !inQuotes;
            }
        } else if (ch === "," && !inQuotes) {
            fields.push(currentField);
            currentField = "";
        } else {
            currentField += ch;
        }
    }

    fields.push(currentField);
    return fields;
}

function isRealHeader(fields) {
    if (fields.length < 8) return false;

    return fields[0].trim().toLowerCase() === "candidate" &&
           fields[1].trim().toLowerCase() === "student/ faculty/ nte" &&
           fields[3].trim().toLowerCase() === "exam" &&
           fields[6].trim().toLowerCase() === "score";
}

function isRowEmpty(row) {
    return row.every(field => field.trim() === "");
}

function safeGet(row, index) {
    return index >= 0 && index < row.length ? row[index] : "";
}

function loadCSV(filePath) {
    const content = fs.readFileSync(filePath, "utf8").replace(/\uFEFF/g, "");
    const lines = content.split(/\r?\n/);

    const data = {
        headers: [],
        rows: []
    };

    let headerFound = false;
    let headerSize = 0;

    lines.forEach(line => {
        const parsedLine = parseCSVLine(line);

        if (!headerFound) {
            if (isRealHeader(parsedLine)) {
                data.headers = parsedLine;
                headerFound = true;
                headerSize = parsedLine.length;
            }
            return;
        }

        while (parsedLine.length < headerSize) {
            parsedLine.push("");
        }

        if (isRowEmpty(parsedLine)) {
            return;
        }

        data.rows.push(parsedLine);
    });

    if (!headerFound) {
        throw new Error("CSV header row was not found.");
    }

    return data;
}

// chooses only meaningful columns for display
function getDisplayColumns(headers) {
    const displayColumns = [];

    headers.forEach((header, index) => {
        const cleanHeader = header.trim();
        if (cleanHeader !== "" && cleanHeader.toLowerCase() !== "column1") {
            displayColumns.push(index);
        }
    });

    return displayColumns;
}

// computes proper width for each displayed column
function computeColumnWidths(data, displayColumns) {
    return displayColumns.map(colIndex => {
        let maxWidth = data.headers[colIndex].length;

        data.rows.forEach(row => {
            const value = safeGet(row, colIndex);
            if (value.length > maxWidth) {
                maxWidth = value.length;
            }
        });

        return Math.min(maxWidth, 35);
    });
}

// formats each cell for aligned output
function formatCell(text, width) {
    text = text || "";

    if (text.length > width) {
        text = width > 3 ? text.substring(0, width - 3) + "..." : text.substring(0, width);
    }

    return text.padEnd(width, " ");
}

function printSeparator(widths, rowNumberWidth) {
    let line = "-".repeat(rowNumberWidth) + "-+-";
    line += widths.map(width => "-".repeat(width)).join("-+-");
    console.log(line);
}

async function main() {
    try {
        console.log("Student: CANTAROS, RICH ANDREI");
        console.log("MP12 - Display Dataset in Formatted Table Output");

        const filePath = await askQuestion("Enter CSV dataset file path: ");
        const data = loadCSV(filePath);

        if (data.rows.length === 0) {
            console.log("No valid data rows were found in the dataset.");
            rl.close();
            return;
        }

        const displayColumns = getDisplayColumns(data.headers);

        if (displayColumns.length === 0) {
            console.log("No displayable columns were found.");
            rl.close();
            return;
        }

        const widths = computeColumnWidths(data, displayColumns);
        const rowNumberWidth = Math.max(4, String(data.rows.length).length + 2);

        console.log("\n========== FORMATTED DATASET TABLE ==========");
        console.log(`Total Valid Rows: ${data.rows.length}`);
        console.log("Displayed Columns: meaningful columns only");
        console.log("=============================================\n");

        printSeparator(widths, rowNumberWidth);

        let headerLine = formatCell("No.", rowNumberWidth) + " | ";
        headerLine += displayColumns
            .map((colIndex, i) => formatCell(data.headers[colIndex], widths[i]))
            .join(" | ");
        console.log(headerLine);

        printSeparator(widths, rowNumberWidth);

        data.rows.forEach((row, rowIndex) => {
            let rowLine = formatCell(String(rowIndex + 1), rowNumberWidth) + " | ";
            rowLine += displayColumns
                .map((colIndex, i) => formatCell(safeGet(row, colIndex), widths[i]))
                .join(" | ");
            console.log(rowLine);
        });

        printSeparator(widths, rowNumberWidth);

    } catch (error) {
        console.log("Error:", error.message);
    } finally {
        rl.close();
    }
}

main();