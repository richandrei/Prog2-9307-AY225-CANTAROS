import javax.swing.*;
import java.awt.*;

public class PrelimGradeCalculatorGUI {

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        JFrame frame = new JFrame("Prelim Grade Calculator");
        frame.setSize(420, 420);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        panel.setLayout(new GridLayout(0,1,8,8));

        JTextField attendance = new JTextField();
        JTextField lab1 = new JTextField();
        JTextField lab2 = new JTextField();
        JTextField lab3 = new JTextField();

        JTextArea output = new JTextArea(6, 20);
        output.setEditable(false);
        output.setFont(new Font("Monospaced", Font.PLAIN, 13));
        output.setBackground(new Color(245,245,245));

        JButton compute = new JButton("Compute");

        panel.add(new JLabel("Attendance"));
        panel.add(attendance);
        panel.add(new JLabel("Lab Work 1"));
        panel.add(lab1);
        panel.add(new JLabel("Lab Work 2"));
        panel.add(lab2);
        panel.add(new JLabel("Lab Work 3"));
        panel.add(lab3);
        panel.add(compute);
        panel.add(new JScrollPane(output));

        compute.addActionListener(e -> {
            double a = Double.parseDouble(attendance.getText());
            double l1 = Double.parseDouble(lab1.getText());
            double l2 = Double.parseDouble(lab2.getText());
            double l3 = Double.parseDouble(lab3.getText());

            double labAvg = (l1 + l2 + l3) / 3;
            double cs = (a * 0.40) + (labAvg * 0.60);

            double pass = (75 - (cs * 0.30)) / 0.70;
            double excel = (100 - (cs * 0.30)) / 0.70;

            output.setText(
                String.format(
                    "Lab Work Average: %.2f%n" +
                    "Class Standing: %.2f%n%n" +
                    "Exam to PASS: %.2f%n" +
                    "Exam for EXCELLENT: %.2f",
                    labAvg, cs, pass, excel
                )
            );
        });

        frame.add(panel);
        frame.setVisible(true);
    }
}
