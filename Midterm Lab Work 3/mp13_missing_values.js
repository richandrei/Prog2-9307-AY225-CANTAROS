/*
 * Student Name: CANTAROS, RICH ANDREI
 * Machine Problem: MP13 - Detect rows with missing values
 * Language: JavaScript (Node.js)
 *
 * Description:
 * This program asks the user for the CSV dataset file path first.
 * It reads and parses the CSV file using Node.js.
 * It then checks each valid row for missing values in meaningful columns only.
 * Blank unnamed columns and the placeholder Column1 are ignored to avoid false results.
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

// meaningful columns are the ones checked for missing values
function getMeaningfulColumns(headers) {
    const meaningfulColumns = [];

    headers.forEach((header, index) => {
        const cleanHeader = header.trim();
        if (cleanHeader !== "" && cleanHeader.toLowerCase() !== "column1") {
            meaningfulColumns.push(index);
        }
    });

    return meaningfulColumns;
}

async function main() {
    try {
        console.log("Student: CANTAROS, RICH ANDREI");
        console.log("MP13 - Detect Rows with Missing Values");

        const filePath = await askQuestion("Enter CSV dataset file path: ");
        const data = loadCSV(filePath);

        if (data.rows.length === 0) {
            console.log("No valid data rows were found in the dataset.");
            rl.close();
            return;
        }

        const meaningfulColumns = getMeaningfulColumns(data.headers);
        let missingRowCount = 0;

        console.log("\n========== MISSING VALUE REPORT ==========");
        console.log("Meaningful columns checked only");
        console.log("(Blank unnamed columns and Column1 are ignored)");
        console.log("==========================================");

        data.rows.forEach((row, rowIndex) => {
            const missingColumns = [];

            meaningfulColumns.forEach(colIndex => {
                const value = safeGet(row, colIndex).trim();
                if (value === "") {
                    missingColumns.push(data.headers[colIndex]);
                }
            });

            if (missingColumns.length > 0) {
                missingRowCount++;

                let candidateName = safeGet(row, 0).trim();
                if (candidateName === "") {
                    candidateName = "(NO CANDIDATE NAME)";
                }

                console.log(`Data Row #${rowIndex + 1} | File Line #${data.sourceLineNumbers[rowIndex]} | Candidate: ${candidateName}`);
                console.log(`Missing Columns: ${missingColumns.join(", ")}`);
                console.log("------------------------------------------");
            }
        });

        if (missingRowCount === 0) {
            console.log("No rows with missing values were found in the meaningful columns.");
        } else {
            console.log(`Total Rows With Missing Values: ${missingRowCount}`);
        }

    } catch (error) {
        console.log("Error:", error.message);
    } finally {
        rl.close();
    }
}

main();