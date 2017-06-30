package de.canitzp.libloader;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * @author canitzp
 */
public class MainFrame extends JFrame{

    public JTabbedPane tabs = new JTabbedPane();
    // Download Panel
    public JTextArea logAreaDownload = new JTextArea();
    public JButton chooseVersionBtn = new JButton("Choose Minecraft Version");
    // Mappings Panel
    public JTextArea logAreaMappings = new JTextArea();
    public JTextField chooseJarPath = new JTextField();
    public JButton choseJarBtn = new JButton("...");
    public JButton generateObfuscatedMappingsFileBtn = new JButton("Generate obfuscated mappings file");
    public JButton generateMappedMappingsFileBtn = new JButton("Generate mapped mappings file");

    public MainFrame(){
        super("LibLoader " + LibLoader.VERSION);
        this.setLayout(new BorderLayout());
        this.setMinimumSize(new Dimension(854, 480));

        DocumentListener documentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                logAreaDownload.setCaretPosition(logAreaDownload.getDocument().getLength());
                logAreaMappings.setCaretPosition(logAreaMappings.getDocument().getLength());
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                logAreaDownload.setCaretPosition(logAreaDownload.getDocument().getLength());
                logAreaMappings.setCaretPosition(logAreaMappings.getDocument().getLength());
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                logAreaDownload.setCaretPosition(logAreaDownload.getDocument().getLength());
                logAreaMappings.setCaretPosition(logAreaMappings.getDocument().getLength());
            }
        };

        this.logAreaDownload.setEditable(false);
        this.logAreaDownload.getDocument().addDocumentListener(documentListener);
        this.logAreaMappings.setEditable(false);
        this.logAreaMappings.getDocument().addDocumentListener(documentListener);

        JPanel downloadPanel = new JPanel(new BorderLayout());
        downloadPanel.add(this.chooseVersionBtn, BorderLayout.NORTH);
        downloadPanel.add(new JScrollPane(this.logAreaDownload), BorderLayout.CENTER);

        JPanel mappingsPanel = new JPanel(new BorderLayout());
        JPanel mappingsPanelNorth = new JPanel(new BorderLayout());
        mappingsPanelNorth.add(merge(this.chooseJarPath, this.choseJarBtn, true), BorderLayout.NORTH);
        mappingsPanelNorth.add(mergeGrid(this.generateObfuscatedMappingsFileBtn, this.generateMappedMappingsFileBtn), BorderLayout.CENTER);

        mappingsPanel.add(mappingsPanelNorth, BorderLayout.NORTH);
        mappingsPanel.add(new JScrollPane(this.logAreaMappings), BorderLayout.CENTER);

        this.tabs.addTab("Download", downloadPanel);
        this.tabs.addTab("Mapping", mappingsPanel);
        this.add(tabs, BorderLayout.CENTER);

        this.pack();
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setVisible(true);

        this.chooseVersionBtn.addActionListener(e -> {
            try {
                new VersionFrame(this);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
    }

    public void clearLog(){
        this.logAreaDownload.setText(null);
        this.logAreaMappings.setText(null);
    }

    public static JComponent merge(JComponent first, JComponent second, boolean leftAndRight){
        JPanel comp = new JPanel(new BorderLayout());
        comp.add(first, leftAndRight ? BorderLayout.CENTER : BorderLayout.NORTH);
        comp.add(second, leftAndRight ? BorderLayout.EAST : BorderLayout.SOUTH);
        return comp;
    }

    public static JComponent mergeGrid(JComponent first, JComponent second){
        JPanel comp = new JPanel(new GridLayout(1, 2));
        comp.add(first);
        comp.add(second);
        return comp;
    }

}
