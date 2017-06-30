package de.canitzp.libloader;

import de.canitzp.libloader.remap.ClassMapping;
import de.canitzp.libloader.remap.MappingsParser;
import de.canitzp.libloader.threads.DownloadThread;
import org.apache.commons.lang3.tuple.Pair;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

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
    public JButton chooseJarBtn = new JButton("...");
    public JButton generateObfuscatedMappingsFileBtn = new JButton("Generate obfuscated mappings file");
    public JButton generateMappedMappingsFileBtn = new JButton("Generate mapped mappings file");
    public JButton applyMappingsToJarBtn = new JButton("Apply mappings file to Minecraft jar");

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
        this.chooseJarPath.setEditable(false);
        this.generateObfuscatedMappingsFileBtn.setEnabled(false);
        this.generateMappedMappingsFileBtn.setEnabled(false);
        this.applyMappingsToJarBtn.setEnabled(false);

        JPanel downloadPanel = new JPanel(new BorderLayout());
        downloadPanel.add(this.chooseVersionBtn, BorderLayout.NORTH);
        downloadPanel.add(new JScrollPane(this.logAreaDownload), BorderLayout.CENTER);

        JPanel mappingsPanel = new JPanel(new BorderLayout());
        JPanel mappingsPanelNorth = new JPanel(new BorderLayout());
        mappingsPanelNorth.add(merge(this.chooseJarPath, this.chooseJarBtn, true), BorderLayout.NORTH);
        mappingsPanelNorth.add(mergeGrid(this.generateObfuscatedMappingsFileBtn, this.generateMappedMappingsFileBtn), BorderLayout.CENTER);
        mappingsPanelNorth.add(this.applyMappingsToJarBtn, BorderLayout.SOUTH);

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
            LibLoader.msg("Version button pressed");
            try {
                new VersionFrame(this);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
        this.chooseJarBtn.addActionListener(e -> {
            LibLoader.msg("Choose jar button pressed");
            try {
                JFileChooser chooser = new JFileChooser(DownloadThread.getExecutionPath());
                chooser.setDialogTitle("Choose Minecraft Jar");
                chooser.setDialogType(JFileChooser.OPEN_DIALOG);
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setFileFilter(new FileNameExtensionFilter("Minecraft JAR-File (.jar)", "jar"));
                if(chooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION){
                    MainFrame.this.chooseJarPath.setText(chooser.getSelectedFile().getAbsolutePath());
                    MainFrame.this.generateObfuscatedMappingsFileBtn.setEnabled(true);
                    MainFrame.this.generateMappedMappingsFileBtn.setEnabled(true);
                    MainFrame.this.applyMappingsToJarBtn.setEnabled(true);
                }
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
        });
        this.applyMappingsToJarBtn.addActionListener(e -> {
            LibLoader.msg("Apply mappings to jar button pressed");
            JFileChooser chooser = new JFileChooser(MainFrame.this.chooseJarPath.getText());
            chooser.setDialogTitle("Choose Mappings File");
            chooser.setDialogType(JFileChooser.OPEN_DIALOG);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setFileFilter(new FileNameExtensionFilter("Minecraft Mappings-File (.mappings)", "mappings"));
            if(chooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION){
                LibLoader.msg("Reading mappings file");
                try {
                    for(ClassMapping mapping : new MappingsParser(chooser.getSelectedFile()).read()){
                        System.out.println(mapping);
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        this.generateObfuscatedMappingsFileBtn.addActionListener(e -> {
            LibLoader.msg("Generate obfuscated mappings file button pressed");
            try {
                MainFrame.this.generateObfuscatedMappingsFileBtn.setEnabled(false);
                MainFrame.this.generateMappedMappingsFileBtn.setEnabled(false);
                MainFrame.this.applyMappingsToJarBtn.setEnabled(false);
                LibLoader.msg("Open and analyzing jar file");
                File jarFile = new File(this.chooseJarPath.getText());
                Pair<List<ClassMapping>, Map<String, byte[]>> obfJar = Util.analyzeJarFile(jarFile, Util.readJarFile(jarFile));
                new MappingsParser(new File(".", "obfuscated.mappings")).write(obfJar.getKey());
                MainFrame.this.generateObfuscatedMappingsFileBtn.setEnabled(true);
                MainFrame.this.generateMappedMappingsFileBtn.setEnabled(true);
                MainFrame.this.applyMappingsToJarBtn.setEnabled(true);
            } catch (IOException e1) {
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
