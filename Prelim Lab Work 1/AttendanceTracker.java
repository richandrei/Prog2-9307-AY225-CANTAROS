import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Attendance Tracker Application
 * LAB Work 1
 */
public class AttendanceTracker {

    public static void main(String[] args) {

        // Create JFrame window
        JFrame frame = new JFrame("Attendance Tracker");
        frame.setSize(400, 250);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); // Center the window

        // Create a panel with GridLayout for proper alignment
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Labels
        JLabel nameLabel = new JLabel("Attendance Name:");
        JLabel courseLabel = new JLabel("Course / Year:");
        JLabel timeLabel = new JLabel("Time In:");
        JLabel signatureLabel = new JLabel("E-Signature:");

        // Text fields
        JTextField nameField = new JTextField();
        JTextField courseField = new JTextField();
        JTextField timeField = new JTextField();
        JTextField signatureField = new JTextField();

        // Obtain system date and time
        String timeIn = LocalDateTime.now().toString();
        timeField.setText(timeIn);
        timeField.setEditable(false); // Prevent editing

        // Generate E-Signature using UUID
        String eSignature = UUID.randomUUID().toString();
        signatureField.setText(eSignature);
        signatureField.setEditable(false); // Prevent editing

        // Add components to panel
        panel.add(nameLabel);
        panel.add(nameField);
        panel.add(courseLabel);
        panel.add(courseField);
        panel.add(timeLabel);
        panel.add(timeField);
        panel.add(signatureLabel);
        panel.add(signatureField);

        // Add panel to frame
        frame.add(panel);

        // Make frame visible
        frame.setVisible(true);
    }
}
