/*
 * Student Name: CANTAROS, RICH ANDREI
 * Machine Problem: MP11 - Frequency count for column values
 * Language: JavaScript (Node.js)
 *
 * Description:
 * This program asks the user for the CSV dataset file path first.
 * It reads and parses the CSV file using the Node.js File System module.
 * After loading the dataset, it asks which column to analyze.
 * It then counts the frequency of values in that column and displays the result.
 */

const fs = require("fs");
const readline = require("readline");

// readline interface for user input
const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});

// askQuestion function:
// used to prompt the user and return the answer
function askQuestion(question) {
    return new Promise(resolve => rl.question(question, resolve));
}

// parseCSVLine function:
// parses one CSV line while handling commas inside quotes
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

// checks if the current row is the real CSV header
function isRealHeader(fields) {
    if (fields.length < 8) return false;

    return fields[0].trim().toLowerCase() === "candidate" &&
           fields[1].trim().toLowerCase() === "student/ faculty/ nte" &&
           fields[3].trim().toLowerCase() === "exam" &&
           fields[6].trim().toLowerCase() === "score";
}

// checks if the row is fully empty
function isRowEmpty(row) {
    return row.every(field => field.trim() === "");
}

// safely gets a value from a row
function safeGet(row, index) {
    return index >= 0 && index < row.length ? row[index] : "";
}

// loadCSV function:
// reads the file, finds the actual header row, and stores valid data rows
function loadCSV(filePath) {
    const content = fs.readFileSync(filePath, "utf8").replace(/\uFEFF/g, "");
    const lines = content.split(/\r?\n/);

    const data = {
        headers: [],
        rows: [],
        sourceLineNumbers: []
    };

    let headerFound = false;
    let headerSize = 0;

    lines.forEach((line, lineIndex) => {
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
        data.sourceLineNumbers.push(lineIndex + 1);
    });

    if (!headerFound) {
        throw new Error("CSV header row was not found.");
    }

    return data;
}

// finds the column index by number or name
function findColumnIndex(headers, input) {
    const trimmedInput = input.trim();

    const number = Number(trimmedInput);
    if (!Number.isNaN(number) && Number.isInteger(number)) {
        if (number >= 1 && number <= headers.length) {
            return number - 1;
        }
    }

    for (let i = 0; i < headers.length; i++) {
        if (headers[i].trim().toLowerCase() === trimmedInput.toLowerCase()) {
            return i;
        }
    }

    return -1;
}

async function main() {
    try {
        console.log("Student: CANTAROS, RICH ANDREI");
        console.log("MP11 - Frequency Count for Column Values");

        const filePath = await askQuestion("Enter CSV dataset file path: ");
        const data = loadCSV(filePath);

        if (data.rows.length === 0) {
            console.log("No valid data rows were found in the dataset.");
            rl.close();
            return;
        }

        console.log("\nAvailable columns:");
        data.headers.forEach((header, index) => {
            if (header.trim() !== "") {
                console.log(`${index + 1} - ${header}`);
            }
        });

        const columnInput = await askQuestion("\nEnter column name or column number for frequency count: ");
        const columnIndex = findColumnIndex(data.headers, columnInput);

        if (columnIndex === -1) {
            console.log("Invalid column. Please run the program again and enter a valid column name or number.");
            rl.close();
            return;
        }

        // frequencyMap stores the count of every unique value
        const frequencyMap = {};

        data.rows.forEach(row => {
            let value = safeGet(row, columnIndex).trim();
            if (value === "") value = "(EMPTY)";

            frequencyMap[value] = (frequencyMap[value] || 0) + 1;
        });

        // sort by frequency descending, then alphabetically
        const sortedEntries = Object.entries(frequencyMap).sort((a, b) => {
            if (b[1] !== a[1]) return b[1] - a[1];
            return a[0].localeCompare(b[0]);
        });

        console.log("\n========== FREQUENCY COUNT ==========");
        console.log(`Selected Column: ${data.headers[columnIndex]}`);
        console.log(`Total Valid Rows: ${data.rows.length}`);
        console.log(`Unique Values: ${sortedEntries.length}`);
        console.log("=====================================");

        sortedEntries.forEach(([value, count]) => {
            console.log(`${value.padEnd(45)} : ${count}`);
        });

    } catch (error) {
        console.log("Error:", error.message);
    } finally {
        rl.close();
    }
}

main();