package ants.core.ui;

import ants.Ant;
import ants.core.AntsGameModel;
import ants.core.Serialization;
import java.awt.Color;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.TransferHandler;
import javax.swing.filechooser.FileFilter;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import net.miginfocom.swing.MigLayout;

public class OptionsUI extends JPanel {
    private static OptionsUI instance;
    private JTextField fileField;
    private JSpinner widthSpinner;
    private JSpinner heightSpinner;
    private JSpinner numStartingAntsSpinner;
    private JSpinner numTurnsPerNewAntSpinner;
    private JButton browseButton;
    private JButton startButton;
    private JFileChooser fileChooser;
    private final ActionListener actionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            if (src == OptionsUI.this.browseButton)
                OptionsUI.this.showFileChooser();
            else if (src == OptionsUI.this.startButton)
                try {
                    OptionsUI.this.start();
                } catch (Exception ee) {
                    try {
                        ee.printStackTrace(new PrintStream("ERR.txt"));
                    } catch (FileNotFoundException localFileNotFoundException) {
                    }
                    throw new RuntimeException(ee);
                }
        }
    };

    private OptionsUI() {
        initUI();

        initializeListeners();
    }

    public static OptionsUI get() {
        if (instance == null) {
            instance = new OptionsUI();
        }
        return instance;
    }

    private void initUI() {
        setLayout(new MigLayout("insets 10, gap 10"));

        int minSize = 1;
        int size = 20;
        int maxSize = 128;

        this.fileField = new JTextField(32);
        this.browseButton = new JButton("Browse");
        this.widthSpinner = new JSpinner(new SpinnerNumberModel(size, minSize, maxSize, 1));
        this.heightSpinner = new JSpinner(new SpinnerNumberModel(size, minSize, maxSize, 1));
        this.numStartingAntsSpinner = new JSpinner(new SpinnerNumberModel(3, 0, 999, 1));
        this.numTurnsPerNewAntSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 99, 1));
        this.startButton = new JButton("Go!");

        this.browseButton.setFocusPainted(false);
        this.startButton.setFocusPainted(false);
        this.startButton.setBackground(Color.green);
        this.startButton.setFont(new Font("Arial", 1, 30));
        this.fileField.setTransferHandler(new TransferHandler() {
            public boolean canImport(TransferHandler.TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            public boolean importData(TransferHandler.TransferSupport support) {
                try {
                    List files = (List) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    OptionsUI.this.fileField.setText(((File) files.get(0)).toString());
                    return true;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        add(new JLabel("Ant Implementation:"), "");
        add(this.fileField, "");
        add(this.browseButton, "wrap");

        add(new JLabel("You can drag and drop your .java or .class file into the implementation field."), "span, wrap");
        add(this.startButton, "wrap");
    }

    private void showFileChooser() {
        if (this.fileChooser == null) {
            this.fileChooser = new JFileChooser();
            this.fileChooser.setFileSelectionMode(0);
            this.fileChooser.setFileFilter(new FileFilter() {
                public boolean accept(File f) {
                    return (f.isDirectory()) || (f.getName().endsWith(".java")) || (f.getName().endsWith(".class"));
                }

                public String getDescription() {
                    return "JAVA or CLASS file";
                }
            });
        }
        int i = this.fileChooser.showOpenDialog(this);
        if (i != 0) {
            return;
        }

        this.fileField.setText(this.fileChooser.getSelectedFile().toString());
    }

    private void start() {
        String filePath = this.fileField.getText();

        if (filePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "You must select your Ant implementation!");
            return;
        }
        if ((!filePath.endsWith(".java")) && (!filePath.endsWith(".class"))) {
            JOptionPane.showMessageDialog(this, "You must choose a JAVA or CLASS file as your Ant implementation!");
            return;
        }

        if (filePath.endsWith(".java")) {
            System.out.println("Compiling java file....");
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

            if (compiler == null) {
                JOptionPane.showMessageDialog(null, "No compiler was found -- try using a .class file instead.");
                return;
            }

            int i = compiler.run(null, System.out, System.err, new String[]{filePath});
            if (i != 0) {
                JOptionPane.showMessageDialog(this, "Problem Compiling");
                return;
            }

            filePath = filePath.substring(0, filePath.length() - 4) + "class";
        }

        File file = new File(this.fileField.getText());
        if (!file.exists()) {
            throw new RuntimeException("Could not find the class file -- " + file);
        }

        Class c = Serialization.loadClass(file);

        if (!Ant.class.isAssignableFrom(c)) {
            JOptionPane.showMessageDialog(this, "Your implementation must implement Ant.java!");
            return;
        }

        AntsGameModel model = new AntsGameModel(c, ((Integer) this.widthSpinner.getValue()).intValue(),
                ((Integer) this.heightSpinner
                        .getValue()).intValue(), ((Integer) this.numStartingAntsSpinner.getValue()).intValue(), ((Integer) this.numTurnsPerNewAntSpinner.getValue()).intValue());

        Ants.get().setContentPane(new AntsView(model));
    }

    private void initializeListeners() {
        this.browseButton.addActionListener(this.actionListener);
        this.startButton.addActionListener(this.actionListener);
    }
}
