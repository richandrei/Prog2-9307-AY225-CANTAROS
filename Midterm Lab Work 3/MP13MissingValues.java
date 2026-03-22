import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * Student Name: CANTAROS, RICH ANDREI
 * Machine Problem: MP13 - Detect rows with missing values
 * Language: Java
 *
 * Description:
 * This program asks the user for the CSV dataset file path first.
 * It reads and parses the CSV data, then checks each valid row for missing values.
 * For accurate checking, blank unnamed columns and the placeholder Column1 are ignored.
 * It displays rows with missing values and lists which columns are incomplete.
 */
public class MP13MissingValues {

    static class CSVData {
        ArrayList<String> headers = new ArrayList<>();
        ArrayList<String[]> rows = new ArrayList<>();
        ArrayList<Integer> sourceLineNumbers = new ArrayList<>();
    }

    public static void main(String[] args) {
        java.util.Scanner scanner = new java.util.Scanner(System.in);

        System.out.println("Student: CANTAROS, RICH ANDREI");
        System.out.println("MP13 - Detect Rows with Missing Values");
        System.out.print("Enter CSV dataset file path: ");
        String filePath = scanner.nextLine();

        try {
            CSVData data = loadCSV(filePath);

            if (data.rows.isEmpty()) {
                System.out.println("No valid data rows were found in the dataset.");
                return;
            }

            ArrayList<Integer> meaningfulColumns = getMeaningfulColumns(data.headers);
            int missingRowCount = 0;

            System.out.println("\n========== MISSING VALUE REPORT ==========");
            System.out.println("Meaningful columns checked only");
            System.out.println("(Blank unnamed columns and Column1 are ignored)");
            System.out.println("==========================================");

            for (int i = 0; i < data.rows.size(); i++) {
                String[] row = data.rows.get(i);
                ArrayList<String> missingColumns = new ArrayList<>();

                for (int colIndex : meaningfulColumns) {
                    String value = safeGet(row, colIndex).trim();
                    if (value.isEmpty()) {
                        missingColumns.add(data.headers.get(colIndex));
                    }
                }

                if (!missingColumns.isEmpty()) {
                    missingRowCount++;
                    String candidateName = safeGet(row, 0).trim();
                    if (candidateName.isEmpty()) {
                        candidateName = "(NO CANDIDATE NAME)";
                    }

                    System.out.println("Data Row #" + (i + 1)
                            + " | File Line #" + data.sourceLineNumbers.get(i)
                            + " | Candidate: " + candidateName);
                    System.out.println("Missing Columns: " + String.join(", ", missingColumns));
                    System.out.println("------------------------------------------");
                }
            }

            if (missingRowCount == 0) {
                System.out.println("No rows with missing values were found in the meaningful columns.");
            } else {
                System.out.println("Total Rows With Missing Values: " + missingRowCount);
            }

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }
    }

    public static CSVData loadCSV(String filePath) throws IOException {
        CSVData data = new CSVData();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        String line;
        boolean headerFound = false;
        int fileLineNumber = 0;
        int headerSize = 0;

        while ((line = reader.readLine()) != null) {
            fileLineNumber++;
            line = line.replace("\uFEFF", "");
            List<String> parsedLine = parseCSVLine(line);

            if (!headerFound) {
                if (isRealHeader(parsedLine)) {
                    data.headers.addAll(parsedLine);
                    headerFound = true;
                    headerSize = parsedLine.size();
                }
                continue;
            }

            while (parsedLine.size() < headerSize) {
                parsedLine.add("");
            }

            if (isRowEmpty(parsedLine)) {
                continue;
            }

            data.rows.add(parsedLine.toArray(new String[0]));
            data.sourceLineNumbers.add(fileLineNumber);
        }

        reader.close();

        if (!headerFound) {
            throw new IOException("CSV header row was not found.");
        }

        return data;
    }

    public static List<String> parseCSVLine(String line) {
        ArrayList<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    currentField.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField.setLength(0);
            } else {
                currentField.append(ch);
            }
        }

        fields.add(currentField.toString());
        return fields;
    }

    public static boolean isRealHeader(List<String> fields) {
        if (fields.size() < 8) {
            return false;
        }

        return fields.get(0).trim().equalsIgnoreCase("Candidate")
                && fields.get(1).trim().equalsIgnoreCase("Student/ Faculty/ NTE")
                && fields.get(3).trim().equalsIgnoreCase("Exam")
                && fields.get(6).trim().equalsIgnoreCase("Score");
    }

    public static boolean isRowEmpty(List<String> row) {
        for (String field : row) {
            if (!field.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static String safeGet(String[] row, int index) {
        if (index >= 0 && index < row.length) {
            return row[index];
        }
        return "";
    }

    /*
     * getMeaningfulColumns function:
     * - Returns only useful columns for missing-value checking
     * - Blank unnamed columns and Column1 are skipped
     */
    public static ArrayList<Integer> getMeaningfulColumns(ArrayList<String> headers) {
        ArrayList<Integer> meaningfulColumns = new ArrayList<>();

        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i).trim();
            if (!header.isEmpty() && !header.equalsIgnoreCase("Column1")) {
                meaningfulColumns.add(i);
            }
        }

        return meaningfulColumns;
    }
}