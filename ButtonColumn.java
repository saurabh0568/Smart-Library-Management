import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class ButtonColumn extends AbstractCellEditor
        implements TableCellRenderer, TableCellEditor, ActionListener {
    JTable table;
    Action action;
    JButton renderButton, editButton;
    int column;

    public ButtonColumn(JTable table, Action action, int column) {
        this.table = table;
        this.action = action;
        this.column = column;

        renderButton = new JButton();
        editButton = new JButton();
        editButton.setFocusPainted(false);
        editButton.addActionListener(this);

        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(column).setCellRenderer(this);
        columnModel.getColumn(column).setCellEditor(this);
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        renderButton.setText((value == null) ? "" : value.toString());
        return renderButton;
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        editButton.setText((value == null) ? "" : value.toString());
        return editButton;
    }

    public Object getCellEditorValue() {
        return editButton.getText();
    }

    public void actionPerformed(ActionEvent e) {
        fireEditingStopped();
        action.actionPerformed(new ActionEvent(table, ActionEvent.ACTION_PERFORMED, "" + table.getSelectedRow()));
    }
}
