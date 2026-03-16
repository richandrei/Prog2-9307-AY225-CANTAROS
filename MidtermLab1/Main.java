import java.io.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class Main {
    private static final Scanner input = new Scanner(System.in);
    private static final DecimalFormat MONEY = new DecimalFormat("#,##0.00");

    private static final List<String> DATE_HEADERS = Arrays.asList(
            "date", "order_date", "sale_date", "transaction_date", "purchase_date"
    );

    private static final List<String> CUSTOMER_HEADERS = Arrays.asList(
            "customer", "customer_name", "customername", "client", "buyer", "name"
    );

    private static final List<String> CATEGORY_HEADERS = Arrays.asList(
            "category", "product_category", "item_category", "department", "product_type"
    );

    private static final List<String> AMOUNT_HEADERS = Arrays.asList(
            "amount", "sale", "sales", "total", "total_sales", "revenue",
            "price", "sales_amount", "total_amount"
    );

    public static void main(String[] args) {
        System.out.println("=== Mini Data Analytics Console Dashboard ===");

        File file = askValidFilePath();
        List<DataRecord> records;

        try {
            records = loadDataset(file);
        } catch (Exception e) {
            System.out.println("Fatal error: " + e.getMessage());
            return;
        }

        if (records.isEmpty()) {
            System.out.println("No valid records found in the CSV file.");
            return;
        }

        menuLoop(records);
        input.close();
    }

    private static File askValidFilePath() {
        while (true) {
            System.out.print("Enter dataset file path: ");
            String path = input.nextLine().trim();
            File file = new File(path);

            try {
                validateFile(file);
                return file;
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                System.out.println("Please try again.\n");
            }
        }
    }

    private static void validateFile(File file) throws Exception {
        if (!file.exists()) {
            throw new Exception("File does not exist.");
        }

        if (!file.isFile()) {
            throw new Exception("Path is not a file.");
        }

        if (!file.canRead()) {
            throw new Exception("File is not readable.");
        }

        if (!file.getName().toLowerCase().endsWith(".csv")) {
            throw new Exception("File is not in CSV format.");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String headerLine = reader.readLine();

            if (headerLine == null || headerLine.trim().isEmpty()) {
                throw new Exception("CSV file is empty.");
            }

            Map<String, Integer> headerMap = createHeaderMap(parseCsvLine(headerLine));

            if (findHeader(headerMap, DATE_HEADERS) == null) {
                throw new Exception("CSV is missing a date column.");
            }

            if (findHeader(headerMap, CUSTOMER_HEADERS) == null) {
                throw new Exception("CSV is missing a customer column.");
            }

            if (findHeader(headerMap, CATEGORY_HEADERS) == null) {
                throw new Exception("CSV is missing a category column.");
            }

            if (findHeader(headerMap, AMOUNT_HEADERS) == null) {
                throw new Exception("CSV is missing an amount/sales column.");
            }
        } catch (IOException e) {
            throw new Exception("Unable to read the file.");
        }
    }

    private static List<DataRecord> loadDataset(File file) throws Exception {
        List<DataRecord> records = new ArrayList<>();
        int skippedRows = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String headerLine = reader.readLine();

            if (headerLine == null) {
                throw new Exception("CSV file is empty.");
            }

            Map<String, Integer> headerMap = createHeaderMap(parseCsvLine(headerLine));

            int dateIndex = getRequiredHeaderIndex(headerMap, DATE_HEADERS, "date");
            int customerIndex = getRequiredHeaderIndex(headerMap, CUSTOMER_HEADERS, "customer");
            int categoryIndex = getRequiredHeaderIndex(headerMap, CATEGORY_HEADERS, "category");
            int amountIndex = getRequiredHeaderIndex(headerMap, AMOUNT_HEADERS, "amount/sales");

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    String[] values = parseCsvLine(line);

                    String dateText = safeGet(values, dateIndex);
                    String customerText = safeGet(values, customerIndex);
                    String categoryText = safeGet(values, categoryIndex);
                    String amountText = safeGet(values, amountIndex);

                    if (dateText.isEmpty() || customerText.isEmpty() || categoryText.isEmpty() || amountText.isEmpty()) {
                        throw new Exception("Missing required value.");
                    }

                    LocalDate date = parseDate(dateText);
                    double amount = parseAmount(amountText);

                    records.add(new DataRecord(date, customerText, categoryText, amount));
                } catch (Exception e) {
                    skippedRows++;
                }
            }
        } catch (IOException e) {
            throw new Exception("Unable to load dataset.");
        }

        System.out.println("File found. Processing...");
        System.out.println("Valid records loaded: " + records.size());
        System.out.println("Skipped invalid rows: " + skippedRows);

        return records;
    }

    private static void menuLoop(List<DataRecord> records) {
        while (true) {
            System.out.println("\n========== MENU ==========");
            System.out.println("1 - View Dataset Summary");
            System.out.println("2 - Monthly Sales");
            System.out.println("3 - Top Customers");
            System.out.println("4 - Category Analysis");
            System.out.println("5 - Exit");
            System.out.print("Choose an option: ");

            String choice = input.nextLine().trim();

            switch (choice) {
                case "1":
                    viewDatasetSummary(records);
                    break;
                case "2":
                    viewMonthlySales(records);
                    break;
                case "3":
                    viewTopCustomers(records);
                    break;
                case "4":
                    viewCategoryAnalysis(records);
                    break;
                case "5":
                    System.out.println("Exiting program...");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void viewDatasetSummary(List<DataRecord> records) {
        double totalSales = 0.0;
        Set<String> uniqueCustomers = new HashSet<>();
        Set<String> uniqueCategories = new HashSet<>();
        LocalDate earliest = records.get(0).getDate();
        LocalDate latest = records.get(0).getDate();

        for (DataRecord record : records) {
            totalSales += record.getAmount();
            uniqueCustomers.add(record.getCustomer().toLowerCase());
            uniqueCategories.add(record.getCategory().toLowerCase());

            if (record.getDate().isBefore(earliest)) {
                earliest = record.getDate();
            }

            if (record.getDate().isAfter(latest)) {
                latest = record.getDate();
            }
        }

        double averageSale = totalSales / records.size();

        System.out.println("\n===== DATASET SUMMARY =====");
        System.out.printf("%-25s %s%n", "Total Records:", records.size());
        System.out.printf("%-25s %s%n", "Total Sales:", MONEY.format(totalSales));
        System.out.printf("%-25s %s%n", "Average Sale:", MONEY.format(averageSale));
        System.out.printf("%-25s %s%n", "Unique Customers:", uniqueCustomers.size());
        System.out.printf("%-25s %s%n", "Unique Categories:", uniqueCategories.size());
        System.out.printf("%-25s %s%n", "Earliest Date:", earliest);
        System.out.printf("%-25s %s%n", "Latest Date:", latest);
    }

    private static void viewMonthlySales(List<DataRecord> records) {
        Map<YearMonth, Double> monthlySales = new TreeMap<>();

        for (DataRecord record : records) {
            YearMonth month = YearMonth.from(record.getDate());
            monthlySales.put(month, monthlySales.getOrDefault(month, 0.0) + record.getAmount());
        }

        System.out.println("\n===== MONTHLY SALES =====");
        System.out.printf("%-15s %15s%n", "Month", "Total Sales");
        System.out.println("--------------------------------");

        for (Map.Entry<YearMonth, Double> entry : monthlySales.entrySet()) {
            System.out.printf("%-15s %15s%n", entry.getKey(), MONEY.format(entry.getValue()));
        }
    }

    private static void viewTopCustomers(List<DataRecord> records) {
        Map<String, Double> customerTotals = new HashMap<>();

        for (DataRecord record : records) {
            customerTotals.put(
                    record.getCustomer(),
                    customerTotals.getOrDefault(record.getCustomer(), 0.0) + record.getAmount()
            );
        }

        List<Map.Entry<String, Double>> sortedCustomers = new ArrayList<>(customerTotals.entrySet());
        sortedCustomers.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        System.out.println("\n===== TOP CUSTOMERS =====");
        System.out.printf("%-5s %-25s %15s%n", "Rank", "Customer", "Total Sales");
        System.out.println("---------------------------------------------------");

        int limit = Math.min(5, sortedCustomers.size());
        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Double> entry = sortedCustomers.get(i);
            System.out.printf("%-5d %-25s %15s%n", i + 1, entry.getKey(), MONEY.format(entry.getValue()));
        }
    }

    private static void viewCategoryAnalysis(List<DataRecord> records) {
        Map<String, Double> categoryTotals = new HashMap<>();
        Map<String, Integer> categoryCounts = new HashMap<>();

        for (DataRecord record : records) {
            categoryTotals.put(
                    record.getCategory(),
                    categoryTotals.getOrDefault(record.getCategory(), 0.0) + record.getAmount()
            );

            categoryCounts.put(
                    record.getCategory(),
                    categoryCounts.getOrDefault(record.getCategory(), 0) + 1
            );
        }

        List<String> categories = new ArrayList<>(categoryTotals.keySet());
        categories.sort((a, b) -> Double.compare(categoryTotals.get(b), categoryTotals.get(a)));

        System.out.println("\n===== CATEGORY ANALYSIS =====");
        System.out.printf("%-20s %12s %15s %15s%n", "Category", "Records", "Total Sales", "Average Sale");
        System.out.println("--------------------------------------------------------------------");

        for (String category : categories) {
            int count = categoryCounts.get(category);
            double total = categoryTotals.get(category);
            double average = total / count;

            System.out.printf(
                    "%-20s %12d %15s %15s%n",
                    category,
                    count,
                    MONEY.format(total),
                    MONEY.format(average)
            );
        }
    }

    private static Map<String, Integer> createHeaderMap(String[] headers) {
        Map<String, Integer> headerMap = new HashMap<>();

        for (int i = 0; i < headers.length; i++) {
            headerMap.put(normalizeHeader(headers[i]), i);
        }

        return headerMap;
    }

    private static String findHeader(Map<String, Integer> headerMap, List<String> aliases) {
        for (String alias : aliases) {
            if (headerMap.containsKey(alias)) {
                return alias;
            }
        }
        return null;
    }

    private static int getRequiredHeaderIndex(Map<String, Integer> headerMap, List<String> aliases, String name) throws Exception {
        String found = findHeader(headerMap, aliases);

        if (found == null) {
            throw new Exception("Missing required column: " + name);
        }

        return headerMap.get(found);
    }

    private static String normalizeHeader(String header) {
        return header
                .replace("\uFEFF", "")
                .trim()
                .toLowerCase()
                .replaceAll("[\\s-]+", "_");
    }

    private static String safeGet(String[] values, int index) {
        if (index < 0 || index >= values.length) {
            return "";
        }
        return values[index].trim();
    }

    private static LocalDate parseDate(String value) throws Exception {
        List<DateTimeFormatter> formats = Arrays.asList(
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("yyyy/M/d"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd"),
                DateTimeFormatter.ofPattern("M/d/yyyy"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                DateTimeFormatter.ofPattern("d/M/yyyy"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy")
        );

        for (DateTimeFormatter format : formats) {
            try {
                return LocalDate.parse(value.trim(), format);
            } catch (DateTimeParseException ignored) {
            }
        }

        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw new Exception("Invalid date: " + value);
        }
    }

    private static double parseAmount(String value) throws Exception {
        String cleaned = value.replaceAll("[^0-9.\\-]", "");

        if (cleaned.isEmpty()) {
            throw new Exception("Invalid amount: " + value);
        }

        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            throw new Exception("Invalid amount: " + value);
        }
    }

    private static String[] parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                values.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }

        values.add(current.toString().trim());
        return values.toArray(new String[0]);
    }
}