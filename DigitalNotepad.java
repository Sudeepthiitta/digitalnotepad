import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.undo.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class DigitalNotepad extends JFrame {
    private JTextArea textArea;
    private JFileChooser fileChooser;
    private UndoManager undoManager;
    private JLabel statusBar;
    private File currentFile = null;

    public DigitalNotepad() {
        setTitle("Digital Notepad");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Text Area with Scroll
        textArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        // File Chooser
        fileChooser = new JFileChooser();
        fileChooser.setAcceptAllFileFilterUsed(true);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Text Documents (*.txt)", "txt"));

        // Undo Manager
        undoManager = new UndoManager();
        textArea.getDocument().addUndoableEditListener(e -> undoManager.addEdit(e.getEdit()));

        // Menu Bar
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("File");
        addMenuItem(fileMenu, "New", e -> newFile());
        addMenuItem(fileMenu, "Open", e -> openFile());
        addMenuItem(fileMenu, "Save", e -> saveFile());
        addMenuItem(fileMenu, "Save As", e -> saveAsFile());
        fileMenu.addSeparator();
        addMenuItem(fileMenu, "Exit", e -> System.exit(0));

        // Edit Menu (fixed undo/redo)
        JMenu editMenu = new JMenu("Edit");
        JMenuItem undoItem = new JMenuItem("Undo");
        undoItem.addActionListener(e -> {
            if (undoManager.canUndo()) {
                undoManager.undo();
            }
        });
        JMenuItem redoItem = new JMenuItem("Redo");
        redoItem.addActionListener(e -> {
            if (undoManager.canRedo()) {
                undoManager.redo();
            }
        });
        editMenu.add(undoItem);
        editMenu.add(redoItem);
        editMenu.addSeparator();
        addMenuItem(editMenu, "Cut", e -> textArea.cut());
        addMenuItem(editMenu, "Copy", e -> textArea.copy());
        addMenuItem(editMenu, "Paste", e -> textArea.paste());
        addMenuItem(editMenu, "Select All", e -> textArea.selectAll());

        // View Menu
        JMenu viewMenu = new JMenu("View");
        addMenuItem(viewMenu, "Dark Mode", e -> toggleDarkMode());

        // Add Menus
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        setJMenuBar(menuBar);

        // Status Bar
        statusBar = new JLabel("Words: 0 | Characters: 0");
        add(statusBar, BorderLayout.SOUTH);

        // Update Status Bar
        textArea.addCaretListener(e -> updateStatus());

        setVisible(true);
    }

    private void addMenuItem(JMenu menu, String name, ActionListener action) {
        JMenuItem item = new JMenuItem(name);
        item.addActionListener(action);
        menu.add(item);
    }

    // File Operations
    private void newFile() {
        textArea.setText("");
        currentFile = null;
        setTitle("Digital Notepad - New File");
    }

    private void openFile() {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(currentFile))) {
                textArea.read(reader, null);
                setTitle("Digital Notepad - " + currentFile.getName());
                undoManager.discardAllEdits(); // reset undo history after opening
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error opening file!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveFile() {
        if (currentFile == null) {
            saveAsFile();
        } else {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {
                textArea.write(writer);
                setTitle("Digital Notepad - " + currentFile.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving file!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveAsFile() {
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            if (!currentFile.getName().endsWith(".txt")) {
                currentFile = new File(currentFile.getAbsolutePath() + ".txt");
            }
            saveFile();
        }
    }

    // Dark Mode
    private void toggleDarkMode() {
        if (textArea.getBackground().equals(Color.WHITE)) {
            textArea.setBackground(Color.DARK_GRAY);
            textArea.setForeground(Color.WHITE);
            statusBar.setForeground(Color.WHITE);
            statusBar.setBackground(Color.BLACK);
        } else {
            textArea.setBackground(Color.WHITE);
            textArea.setForeground(Color.BLACK);
            statusBar.setForeground(Color.BLACK);
            statusBar.setBackground(Color.LIGHT_GRAY);
        }
    }

    // Status Update
    private void updateStatus() {
        String text = textArea.getText();
        int words = text.trim().isEmpty() ? 0 : text.trim().split("\\s+").length;
        int chars = text.length();
        statusBar.setText("Words: " + words + " | Characters: " + chars);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DigitalNotepad::new);
    }
}