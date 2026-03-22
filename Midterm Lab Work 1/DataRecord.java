import java.time.LocalDate;

public class DataRecord {
    private final LocalDate date;
    private final String customer;
    private final String category;
    private final double amount;

    public DataRecord(LocalDate date, String customer, String category, double amount) {
        this.date = date;
        this.customer = customer;
        this.category = category;
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getCustomer() {
        return customer;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }
}