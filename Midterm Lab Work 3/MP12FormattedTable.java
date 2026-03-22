import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * Student Name: CANTAROS, RICH ANDREI
 * Machine Problem: MP12 - Display dataset in formatted table output
 * Language: Java
 *
 * Description:
 * This program asks the user for the CSV dataset file path first.
 * It reads and parses the CSV file, then stores valid rows in an ArrayList.
 * After loading the dataset, it displays the records in a clean formatted table.
 * Blank unnamed columns and the placeholder Column1 are ignored for readability.
 */
public class MP12FormattedTable {

    static class CSVData {
        ArrayList<String> headers = new ArrayList<>();
        ArrayList<String[]> rows = new ArrayList<>();
    }

    public static void main(String[] args) {
        java.util.Scanner scanner = new java.util.Scanner(System.in);

        System.out.println("Student: CANTAROS, RICH ANDREI");
        System.out.println("MP12 - Display Dataset in Formatted Table Output");
        System.out.print("Enter CSV dataset file path: ");
        String filePath = scanner.nextLine();

        try {
            CSVData data = loadCSV(filePath);

            if (data.rows.isEmpty()) {
                System.out.println("No valid data rows were found in the dataset.");
                return;
            }

            ArrayList<Integer> displayColumns = getDisplayColumns(data.headers);

            if (displayColumns.isEmpty()) {
                System.out.println("No displayable columns were found.");
                return;
            }

            int[] widths = computeColumnWidths(data, displayColumns);
            int rowNumberWidth = Math.max(4, String.valueOf(data.rows.size()).length() + 2);

            System.out.println("\n========== FORMATTED DATASET TABLE ==========");
            System.out.println("Total Valid Rows: " + data.rows.size());
            System.out.println("Displayed Columns: meaningful columns only");
            System.out.println("=============================================\n");

            printSeparator(displayColumns, widths, rowNumberWidth, data.headers);
            System.out.print(formatCell("No.", rowNumberWidth) + " | ");
            for (int i = 0; i < displayColumns.size(); i++) {
                int colIndex = displayColumns.get(i);
                System.out.print(formatCell(data.headers.get(colIndex), widths[i]));
                if (i < displayColumns.size() - 1) {
                    System.out.print(" | ");
                }
            }
            System.out.println();
            printSeparator(displayColumns, widths, rowNumberWidth, data.headers);

            for (int i = 0; i < data.rows.size(); i++) {
                String[] row = data.rows.get(i);

                System.out.print(formatCell(String.valueOf(i + 1), rowNumberWidth) + " | ");

                for (int j = 0; j < displayColumns.size(); j++) {
                    int colIndex = displayColumns.get(j);
                    String value = safeGet(row, colIndex);
                    System.out.print(formatCell(value, widths[j]));

                    if (j < displayColumns.size() - 1) {
                        System.out.print(" | ");
                    }
                }
                System.out.println();
            }

            printSeparator(displayColumns, widths, rowNumberWidth, data.headers);

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
        int headerSize = 0;

        while ((line = reader.readLine()) != null) {
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
     * getDisplayColumns function:
     * - Keeps only meaningful headers
     * - Ignores blank unnamed columns and the placeholder Column1
     */
    public static ArrayList<Integer> getDisplayColumns(ArrayList<String> headers) {
        ArrayList<Integer> displayColumns = new ArrayList<>();

        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i).trim();
            if (!header.isEmpty() && !header.equalsIgnoreCase("Column1")) {
                displayColumns.add(i);
            }
        }

        return displayColumns;
    }

    /*
     * computeColumnWidths function:
     * - Computes the table width of every displayed column
     * - Width is limited to keep the table readable
     */
    public static int[] computeColumnWidths(CSVData data, ArrayList<Integer> displayColumns) {
        int[] widths = new int[displayColumns.size()];

        for (int i = 0; i < displayColumns.size(); i++) {
            int colIndex = displayColumns.get(i);
            int maxWidth = data.headers.get(colIndex).length();

            for (String[] row : data.rows) {
                String value = safeGet(row, colIndex);
                if (value.length() > maxWidth) {
                    maxWidth = value.length();
                }
            }

            widths[i] = Math.min(maxWidth, 35);
        }

        return widths;
    }

    /*
     * formatCell function:
     * - Truncates long text if needed
     * - Pads text so columns align properly
     */
    public static String formatCell(String text, int width) {
        if (text == null) {
            text = "";
        }

        if (text.length() > width) {
            if (width > 3) {
                text = text.substring(0, width - 3) + "...";
            } else {
                text = text.substring(0, width);
            }
        }

        return String.format("%-" + width + "s", text);
    }

    public static void printSeparator(ArrayList<Integer> displayColumns, int[] widths, int rowNumberWidth, ArrayList<String> headers) {
        System.out.print(repeat("-", rowNumberWidth) + "-+-");
        for (int i = 0; i < displayColumns.size(); i++) {
            System.out.print(repeat("-", widths[i]));
            if (i < displayColumns.size() - 1) {
                System.out.print("-+-");
            }
        }
        System.out.println();
    }

    public static String repeat(String text, int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(text);
        }
        return builder.toString();
    }
}