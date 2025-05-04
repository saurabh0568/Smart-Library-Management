import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class AutoFineCalculatorNoLibrary extends JFrame {
    private JComboBox<String> expectedDay, expectedMonth, expectedYear;
    private JComboBox<String> actualDay, actualMonth, actualYear;
    private JTextField fineField, numBooksField;
    private JButton calculateButton;
    private JLabel fineRateLabel;
    private long finePerDay = 0;

    public AutoFineCalculatorNoLibrary() {
        setTitle("Auto Fine Calculator");
        setSize(500, 370);
        setLayout(new GridLayout(7, 2, 10, 10));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Initialize components
        expectedDay = createDayBox();
        expectedMonth = createMonthBox();
        expectedYear = createYearBox();

        actualDay = createDayBox();
        actualMonth = createMonthBox();
        actualYear = createYearBox();

        fineField = new JTextField();
        fineField.setEditable(false);

        numBooksField = new JTextField();

        calculateButton = new JButton("Calculate Fine");

        fineRateLabel = new JLabel("Fine per day: ₹ --");
        fineRateLabel.setFont(new Font("Arial", Font.BOLD, 14));

        // Layout
        add(new JLabel("Expected Return Date:"));
        add(createDatePanel(expectedDay, expectedMonth, expectedYear));

        add(new JLabel("Actual Return Date:"));
        add(createDatePanel(actualDay, actualMonth, actualYear));

        add(new JLabel("Number of Books:"));
        add(numBooksField);

        add(new JLabel("Fine (₹):"));
        add(fineField);

        add(calculateButton);
        add(new JLabel());

        add(fineRateLabel);  // bottom row
        add(new JLabel());

        calculateButton.addActionListener(e -> calculateFine());

        // Load fine per day from DB
        loadFineFromDatabase();

        setVisible(true);
    }

    private void loadFineFromDatabase() {
        try {
            // Adjust DB credentials accordingly
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "odd*357.");
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT fine_per_day FROM fine WHERE id = 1");

            if (rs.next()) {
                finePerDay = rs.getLong("fine_per_day");
                fineRateLabel.setText("Fine per day: ₹ " + finePerDay);
            } else {
                JOptionPane.showMessageDialog(this, "No fine record found.");
            }

            rs.close();
            stmt.close();
            con.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
    }

    private JPanel createDatePanel(JComboBox<String> day, JComboBox<String> month, JComboBox<String> year) {
        JPanel panel = new JPanel();
        panel.add(day);
        panel.add(month);
        panel.add(year);
        return panel;
    }

    private JComboBox<String> createDayBox() {
        String[] days = new String[31];
        for (int i = 1; i <= 31; i++) days[i - 1] = String.format("%02d", i);
        return new JComboBox<>(days);
    }

    private JComboBox<String> createMonthBox() {
        String[] months = new String[12];
        for (int i = 1; i <= 12; i++) months[i - 1] = String.format("%02d", i);
        return new JComboBox<>(months);
    }

    private JComboBox<String> createYearBox() {
        String[] years = new String[10];
        int currentYear = LocalDate.now().getYear();
        for (int i = 0; i < 10; i++) years[i] = String.valueOf(currentYear - 5 + i);
        return new JComboBox<>(years);
    }

    private void calculateFine() {
        try {
            int eDay = Integer.parseInt((String) expectedDay.getSelectedItem());
            int eMonth = Integer.parseInt((String) expectedMonth.getSelectedItem());
            int eYear = Integer.parseInt((String) expectedYear.getSelectedItem());

            int aDay = Integer.parseInt((String) actualDay.getSelectedItem());
            int aMonth = Integer.parseInt((String) actualMonth.getSelectedItem());
            int aYear = Integer.parseInt((String) actualYear.getSelectedItem());

            int numBooks = Integer.parseInt(numBooksField.getText());

            LocalDate expected = LocalDate.of(eYear, eMonth, eDay);
            LocalDate actual = LocalDate.of(aYear, aMonth, aDay);

            long daysLate = ChronoUnit.DAYS.between(expected, actual);
            long fine = (daysLate > 0) ? daysLate * finePerDay * numBooks : 0;

            fineField.setText(String.valueOf(fine));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input. Please check all fields.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AutoFineCalculatorNoLibrary());
    }
}
