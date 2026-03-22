import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/*
 * Student Name: CANTAROS, RICH ANDREI
 * Machine Problem: MP11 - Frequency count for column values
 * Language: Java
 *
 * Description:
 * This program asks the user for the CSV dataset file path first.
 * It then reads and parses the CSV file using BufferedReader and FileReader.
 * After loading the dataset, the program asks the user which column to analyze.
 * It counts how many times each value appears in that column and displays the result.
 */
public class MP11FrequencyCount {

    /*
     * CSVData class:
     * - headers stores the CSV header row
     * - rows stores all valid data rows
     * - sourceLineNumbers stores the original line numbers from the file
     */
    static class CSVData {
        ArrayList<String> headers = new ArrayList<>();
        ArrayList<String[]> rows = new ArrayList<>();
        ArrayList<Integer> sourceLineNumbers = new ArrayList<>();
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Student: CANTAROS, RICH ANDREI");
        System.out.println("MP11 - Frequency Count for Column Values");
        System.out.print("Enter CSV dataset file path: ");
        String filePath = scanner.nextLine();

        try {
            CSVData data = loadCSV(filePath);

            if (data.rows.isEmpty()) {
                System.out.println("No valid data rows were found in the dataset.");
                return;
            }

            System.out.println("\nAvailable columns:");
            for (int i = 0; i < data.headers.size(); i++) {
                String header = data.headers.get(i).trim();
                if (!header.isEmpty()) {
                    System.out.println((i + 1) + " - " + header);
                }
            }

            System.out.print("\nEnter column name or column number for frequency count: ");
            String columnInput = scanner.nextLine();

            int columnIndex = findColumnIndex(data.headers, columnInput);

            if (columnIndex == -1) {
                System.out.println("Invalid column. Please run the program again and enter a valid column name or number.");
                return;
            }

            // frequencyMap stores each unique value and its count
            HashMap<String, Integer> frequencyMap = new HashMap<>();

            // Loop through each row and count values in the selected column
            for (String[] row : data.rows) {
                String value = safeGet(row, columnIndex).trim();

                if (value.isEmpty()) {
                    value = "(EMPTY)";
                }

                frequencyMap.put(value, frequencyMap.getOrDefault(value, 0) + 1);
            }

            // Convert map to list so it can be sorted by frequency descending
            ArrayList<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(frequencyMap.entrySet());
            sortedEntries.sort(new Comparator<Map.Entry<String, Integer>>() {
                @Override
                public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
                    int countCompare = Integer.compare(b.getValue(), a.getValue());
                    if (countCompare != 0) {
                        return countCompare;
                    }
                    return a.getKey().compareToIgnoreCase(b.getKey());
                }
            });

            System.out.println("\n========== FREQUENCY COUNT ==========");
            System.out.println("Selected Column: " + data.headers.get(columnIndex));
            System.out.println("Total Valid Rows: " + data.rows.size());
            System.out.println("Unique Values: " + sortedEntries.size());
            System.out.println("=====================================");

            for (Map.Entry<String, Integer> entry : sortedEntries) {
                System.out.printf("%-45s : %d%n", entry.getKey(), entry.getValue());
            }

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }
    }

    /*
     * loadCSV function:
     * - Reads the CSV file
     * - Finds the actual header row
     * - Skips extra text above the real dataset
     * - Stores valid rows only
     */
    public static CSVData loadCSV(String filePath) throws IOException {
        CSVData data = new CSVData();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        String line;
        boolean headerFound = false;
        int fileLineNumber = 0;
        int headerSize = 0;

        while ((line = reader.readLine()) != null) {
            fileLineNumber++;

            // Remove possible BOM character
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

    /*
     * parseCSVLine function:
     * - Parses one CSV line correctly
     * - Handles commas inside quoted text like "Lastname,Firstname"
     */
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

    /*
     * isRealHeader function:
     * - Checks whether the current line is the real CSV header
     */
    public static boolean isRealHeader(List<String> fields) {
        if (fields.size() < 8) {
            return false;
        }

        return fields.get(0).trim().equalsIgnoreCase("Candidate")
                && fields.get(1).trim().equalsIgnoreCase("Student/ Faculty/ NTE")
                && fields.get(3).trim().equalsIgnoreCase("Exam")
                && fields.get(6).trim().equalsIgnoreCase("Score");
    }

    /*
     * isRowEmpty function:
     * - Returns true if all values in the row are empty
     */
    public static boolean isRowEmpty(List<String> row) {
        for (String field : row) {
            if (!field.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /*
     * safeGet function:
     * - Safely gets a value from a row without causing an index error
     */
    public static String safeGet(String[] row, int index) {
        if (index >= 0 && index < row.length) {
            return row[index];
        }
        return "";
    }

    /*
     * findColumnIndex function:
     * - Accepts either a column number or a column name
     */
    public static int findColumnIndex(ArrayList<String> headers, String input) {
        input = input.trim();

        try {
            int number = Integer.parseInt(input);
            if (number >= 1 && number <= headers.size()) {
                return number - 1;
            }
        } catch (NumberFormatException e) {
            // Not a number, continue checking as text
        }

        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).trim().equalsIgnoreCase(input)) {
                return i;
            }
        }

        return -1;
    }
}