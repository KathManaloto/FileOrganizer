import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class FileOrganizerGUI extends JFrame {

    private JTextField sourceFolderPathField, destinationFolderPathField;
    private JButton browseSourceButton, browseDestinationButton;

    private JPanel fileTypeCategoryPanel;
    private JRadioButton moveRadioButton;
    private JRadioButton topLevelRadioButton, deepScanRadioButton;
    private JRadioButton allFileTypesRadioButton, organizeByCategoryRadioButton;
    private JPanel imagesExtPanel,documentsExtPanel,audioExtPanel,videoExtPanel, othersExtPanel;
    private JCheckBox imagesCheckBox,documentsCheckBox, audioCheckBox, videosCheckBox, byExtensionCheckBox, othersCheckBox;

    private JTextArea logArea;
    private JButton startOrganizeButton, clearLogsButton;

    private static final SimpleDateFormat LOG_TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

    public FileOrganizerGUI(){

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        setTitle("File Organizer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);                              //terminates the program when close
        setMinimumSize(new Dimension(700, 700));
        setPreferredSize(new Dimension(700, 700));
        setLocationRelativeTo(null);                                          //center window
        setGlobalFont(new Font("SansSerif", Font.BOLD, 12));

        mainPanel.add(createTopPanel());
        mainPanel.add(createCenterPanel());

        moveRadioButton.setSelected(true);                                        // default to move
        topLevelRadioButton.setSelected(true);                                    // default scan level
        allFileTypesRadioButton.setSelected(true);                                // default file type mode

        mainPanel.add(createBottomPanel());

        try {
            java.net.URL iconURL = getClass().getResource("/MyFileOrganizerIcon.png");
            if (iconURL != null) {
                ImageIcon icon = new ImageIcon(iconURL);
                setIconImage(icon.getImage());
            } else {
                log("Icon not found: /MyFileOrganizerIcon.png (using default Java icon)");
            }
        } catch (Exception e) {
            log("Failed to load icon: " + e.getMessage());
        }

        add(mainPanel);
        pack();
        log("Application started");

    }

//=============================================== TOP PANEL ============================================================

    private JPanel createTopPanel(){

        //TOP PANEL
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        setTitledBorder(topPanel,"SELECT FOLDERS",TitledBorder.CENTER);

        Dimension buttonSize = new Dimension(100,25);
        int labelWidth = 130;
        int labelHeight = 25;

        //SOURCE PANEL
        JPanel sourcePanel = new JPanel(new GridBagLayout());

        //Source Label
        JLabel sourceLabel = new JLabel("Source Folder:");
        sourceLabel.setPreferredSize(new Dimension(labelWidth,labelHeight));
        sourcePanel.add(sourceLabel, createGbc(0,0,GridBagConstraints.WEST,GridBagConstraints.NONE,0));

        //Source Folder Path Field
        sourceFolderPathField = new JTextField();
        sourceFolderPathField.setPreferredSize(new Dimension(10, 25));
        sourceFolderPathField.setFont(new Font("SansSerif", Font.PLAIN, 11));
        sourcePanel.add(sourceFolderPathField, createGbc(1,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,1.0));

        //Source Browse Button
        browseSourceButton = new JButton("Browse...");
        browseSourceButton.setPreferredSize(buttonSize);
        browseSourceButton.setFocusable(false);
        browseSourceButton.setToolTipText("Select the folder containing files to organize");
        sourcePanel.add(browseSourceButton,createGbc(2,0,GridBagConstraints.EAST,GridBagConstraints.NONE,0));

        //DESTINATION PANEL
        JPanel destinationPanel = new JPanel(new GridBagLayout());

        //DESTINATION LABEL
        JLabel destinationLabel = new JLabel("Destination Folder:");
        destinationLabel.setPreferredSize(new Dimension(labelWidth,labelHeight));
        destinationPanel.add(destinationLabel,createGbc(0,0,GridBagConstraints.WEST,GridBagConstraints.NONE,0));

        //DESTINATION FOLDER PATH FIELD
        destinationFolderPathField = new JTextField();
        destinationFolderPathField.setPreferredSize(new Dimension(10, 25));
        destinationFolderPathField.setFont(new Font("SansSerif", Font.PLAIN, 11));
        destinationPanel.add(destinationFolderPathField, createGbc(1,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,1.0));

        //DESTINATION BROWSE BUTTON
        browseDestinationButton = new JButton("Browse...");
        browseDestinationButton.setPreferredSize(buttonSize);
        browseDestinationButton.setFocusable(false);
        browseDestinationButton.setToolTipText("Select the folder where organized files will be placed");
        destinationPanel.add(browseDestinationButton,createGbc(2,0,GridBagConstraints.EAST,GridBagConstraints.NONE,0));

        topPanel.add(sourcePanel);
        topPanel.add(destinationPanel);

        return topPanel;
    }

    //============================================== CENTER PANEL ==========================================================
    private JPanel createCenterPanel() {

        //CENTER PANEL
        JPanel centerPanel = new JPanel(new BorderLayout());

        //========= CENTERED SECTION: ACTION, SCAN LEVEL, FILE TYPE =========
        JPanel centerOptionsPanel = new JPanel(new GridBagLayout());
        setTitledBorder(centerOptionsPanel,"SELECT YOUR PREFERRED OPTIONS",TitledBorder.CENTER);

        //ACTION ROW
        JLabel actionLabel = new JLabel("ACTION:");
        centerOptionsPanel.add(actionLabel,createGbc(0,0,GridBagConstraints.WEST,GridBagConstraints.NONE,0));

        moveRadioButton = new JRadioButton("Move");
        moveRadioButton.setToolTipText("Move files from source to destination folder.");
        centerOptionsPanel.add(moveRadioButton, createGbc(1,0,GridBagConstraints.WEST,GridBagConstraints.NONE,0));

        JRadioButton copyRadioButton = new JRadioButton("Copy");
        copyRadioButton.setToolTipText("Copy files from source to destination folder.");
        centerOptionsPanel.add(copyRadioButton, createGbc(2,0,GridBagConstraints.WEST,GridBagConstraints.NONE,0));

        ButtonGroup actionGroup = new ButtonGroup();
        actionGroup.add(moveRadioButton);
        actionGroup.add(copyRadioButton);

        //SCAN LEVEL ROW
        JLabel scanLevelLabel = new JLabel("SCAN LEVEL:");

        centerOptionsPanel.add(scanLevelLabel,createGbc(0,1,GridBagConstraints.WEST,GridBagConstraints.NONE,0));

        topLevelRadioButton = new JRadioButton("Top-Level");
        topLevelRadioButton.setToolTipText("Only scan the top level of the source folder.");
        centerOptionsPanel.add(topLevelRadioButton, createGbc(1,1,GridBagConstraints.WEST,GridBagConstraints.NONE,0));

        deepScanRadioButton = new JRadioButton("Deep Scan");
        deepScanRadioButton.setToolTipText("Includes subfolders when scanning source folder.");
        centerOptionsPanel.add(deepScanRadioButton, createGbc(2,1,GridBagConstraints.WEST,GridBagConstraints.NONE,0));

        ButtonGroup scanLevelGroup = new ButtonGroup();
        scanLevelGroup.add(topLevelRadioButton);
        scanLevelGroup.add(deepScanRadioButton);

        //FILE TYPE ROW
        JLabel fileTypeLabel = new JLabel("FILE TYPE :");

        centerOptionsPanel.add(fileTypeLabel,createGbc(0,2,GridBagConstraints.WEST,GridBagConstraints.NONE,0));

        allFileTypesRadioButton = new JRadioButton("All file types");
        allFileTypesRadioButton.setToolTipText("Organize all files regardless of type.");
        centerOptionsPanel.add(allFileTypesRadioButton, createGbc(1,2,GridBagConstraints.WEST,GridBagConstraints.NONE,0));

        organizeByCategoryRadioButton = new JRadioButton("Organize by Category");
        organizeByCategoryRadioButton.setToolTipText("Choose specific file types to organize.");
        centerOptionsPanel.add(organizeByCategoryRadioButton, createGbc(2,2,GridBagConstraints.WEST,GridBagConstraints.NONE,0));

        ButtonGroup fileTypeGroup = new ButtonGroup();
        fileTypeGroup.add(allFileTypesRadioButton);
        fileTypeGroup.add(organizeByCategoryRadioButton);

        centerPanel.add(centerOptionsPanel, BorderLayout.NORTH);

        //========= FILE TYPE CATEGORY PANEL (left aligned, hidden initially) =========
        fileTypeCategoryPanel = new JPanel(new GridBagLayout());
        setTitledBorder(fileTypeCategoryPanel,"SELECT FILE TYPES",TitledBorder.CENTER);
        fileTypeCategoryPanel.setVisible(false);

        //IMAGES
        imagesCheckBox = new JCheckBox("Images");
        fileTypeCategoryPanel.add(imagesCheckBox,createGbc(0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,1.0));

        imagesExtPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        populateExtensionsForCategory(imagesExtPanel, Collections.emptyList());
        imagesExtPanel.setVisible(false);
        fileTypeCategoryPanel.add(imagesExtPanel,createGbc(1,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,1.0));

        //DOCUMENTS
        documentsCheckBox = new JCheckBox("Documents");
        fileTypeCategoryPanel.add(documentsCheckBox, createGbc(0,1,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,1.0));

        documentsExtPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        populateExtensionsForCategory(documentsExtPanel,Collections.emptyList());
        documentsExtPanel.setVisible(false);
        fileTypeCategoryPanel.add(documentsExtPanel,createGbc(1,1,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,1.0));

        //AUDIO
        audioCheckBox = new JCheckBox("Audios");
        fileTypeCategoryPanel.add(audioCheckBox,createGbc(0,2,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,1.0));

        audioExtPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        populateExtensionsForCategory(audioExtPanel,Collections.emptyList());
        audioExtPanel.setVisible(false);
        fileTypeCategoryPanel.add(audioExtPanel, createGbc(1,2,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,1.0));

        //VIDEOS
        videosCheckBox = new JCheckBox("Videos");
        fileTypeCategoryPanel.add(videosCheckBox, createGbc(0,3,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1.0));

        videoExtPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        populateExtensionsForCategory(videoExtPanel,Collections.emptyList());
        videoExtPanel.setVisible(false);
        fileTypeCategoryPanel.add(videoExtPanel, createGbc(1,3,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,1.0));

        //OTHERS
        othersCheckBox = new JCheckBox("Others");
        fileTypeCategoryPanel.add(othersCheckBox, createGbc(0,4,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1.0));

        othersExtPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        populateExtensionsForCategory(othersExtPanel,Collections.emptyList());
        othersExtPanel.setVisible(false);
        fileTypeCategoryPanel.add(othersExtPanel, createGbc(1,4,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,1.0));

        // BY EXTENSION (aligned beneath all category checkboxes)
        byExtensionCheckBox = new JCheckBox("By Extension");
        byExtensionCheckBox.setToolTipText("Organize selected file types by their extensions.");
        fileTypeCategoryPanel.add(byExtensionCheckBox, createGbc(0,5,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,1.0));

        JPanel fileTypeWrapperPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        fileTypeWrapperPanel.add(fileTypeCategoryPanel);
        centerPanel.add(fileTypeWrapperPanel, BorderLayout.CENTER);

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.add(centerPanel, BorderLayout.CENTER);
        return wrapperPanel;
    }

//============================================== BOTTOM PANEL ==========================================================

    private JPanel createBottomPanel () {

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));

        //LOG PANEL
        logArea = new JTextArea(8, 50);
        logArea.setEditable(false);
        logArea.setBackground(Color.WHITE);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setLineWrap(false);
        logArea.setWrapStyleWord(false);

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setTitledBorder(scrollPane,"Log Output",TitledBorder.LEFT);

        bottomPanel.add(scrollPane, BorderLayout.CENTER);

        //BUTTONS PANEL
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));

        startOrganizeButton = new JButton("Start Organizing");
        startOrganizeButton.setFocusable(false);
        buttonsPanel.add(startOrganizeButton);

        clearLogsButton = new JButton("Clear Logs");
        clearLogsButton.setFocusable(false);
        buttonsPanel.add(clearLogsButton);

        bottomPanel.add(buttonsPanel, BorderLayout.SOUTH);

        return bottomPanel;
    }

    //================================================ HELPERS =============================================================
    private void setTitledBorder(JComponent component, String title, int titleJustification) {
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(Color.GRAY),
                        title,
                        titleJustification,
                        TitledBorder.TOP
                ),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
    }

    private void setGlobalFont(Font font) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, new FontUIResource(font));
            }
        }
    }

    private GridBagConstraints createGbc(int x, int y, int anchor, int fill, double weightX){
        GridBagConstraints gbc =  new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.anchor = anchor;
        gbc.fill = fill;
        gbc.weightx = weightX;
        gbc.insets = new Insets(0,5,0,5);
        return gbc;
    }

    //LOG AREA FORMAT
    public void log(String message) {
        String timestamp = LOG_TIME_FORMAT.format(new Date());
        logArea.append("[" + timestamp + "] " + message + "\n");

        try {
            int end = logArea.getDocument().getLength();
            int line = logArea.getLineOfOffset(end);            // last line index
            int startOfLine = logArea.getLineStartOffset(line); // beginning of last line
            logArea.setCaretPosition(startOfLine);              // caret leftmost
        } catch (BadLocationException ex) {
            logArea.append("[ERROR] " + ex.getMessage() + "\n");
        }
    }

    // Used to determine the file type category selected by the user
    public Set<String> getSelectedCategories() {
        Set<String> categories = new HashSet<>();

        if (imagesCheckBox.isSelected()) categories.add("Images");
        if (documentsCheckBox.isSelected()) categories.add("Documents");
        if (audioCheckBox.isSelected()) categories.add("Audios");
        if (videosCheckBox.isSelected()) categories.add("Videos");
        if (othersCheckBox.isSelected()) categories.add("Others");

        return categories;
    }

    //This builds the checkboxes inside the extensions panel for each category.
    public void populateExtensionsForCategory(JPanel extPanel, List<String> extensions) {
        extPanel.removeAll();

        JCheckBox allCheckBox = new JCheckBox("All");
        extPanel.add(allCheckBox);

        // store all dynamically created extension checkboxes
        List<JCheckBox> extBoxes = new ArrayList<>();

        for (String ext : extensions) {
            JCheckBox extCheckBox = new JCheckBox("." + ext);
            extPanel.add(extCheckBox);
            extBoxes.add(extCheckBox);
        }

        // wire the "All" checkbox to control only this category’s extensions
        allCheckBox.addActionListener(e -> {
            boolean selected = allCheckBox.isSelected();
            for (JCheckBox extBox : extBoxes) {
                extBox.setSelected(selected);
            }
        });

        extPanel.revalidate();
        extPanel.repaint();
    }

    //This collects exactly which extensions were ticked by the user in each selected category
    public Map<String, Set<String>> getSelectedExtensions() {

        Map<String, Set<String>> selected = new HashMap<>();
        addSelectedExtensions("Images", imagesCheckBox, imagesExtPanel, selected);
        addSelectedExtensions("Documents", documentsCheckBox, documentsExtPanel, selected);
        addSelectedExtensions("Audios", audioCheckBox, audioExtPanel, selected);
        addSelectedExtensions("Videos", videosCheckBox, videoExtPanel, selected);
        addSelectedExtensions("Others", othersCheckBox, othersExtPanel, selected);
        return selected;
    }

    //Checks if the main category checkbox is selected -> looks at all extension checkboxes in the category panel
    private void addSelectedExtensions(String categoryName, JCheckBox categoryBox, JPanel panel,
                                       Map<String, Set<String>> selected) {
        if (!categoryBox.isSelected() || !panel.isVisible()) return;

        Set<String> exts = new HashSet<>();
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JCheckBox cb && cb.isSelected()) {
                String label = cb.getText().replaceFirst("^\\.", "");
                exts.add(label);
            }
        }

        if (!exts.isEmpty()) {
            selected.put(categoryName, exts);
        }
    }

    //================================================ GETTERS =============================================================
    //TOP PANEL
    public JFrame getFrame() {return this;}
    public JButton getBrowseSourceButton(){ return browseSourceButton;}
    public JButton getBrowseDestinationButton(){ return browseDestinationButton;}
    public JTextField getSourceFolderPathField(){ return sourceFolderPathField;}
    public JTextField getDestinationFolderPathField(){ return destinationFolderPathField;}

    //CENTER PANEL
    public JPanel getFileTypeCategoryPanel(){ return fileTypeCategoryPanel;}
    public JPanel getImagesExtPanel(){ return imagesExtPanel;}
    public JPanel getAudioExtPanel(){ return audioExtPanel;}
    public JPanel getVideoExtPanel(){ return videoExtPanel;}
    public JPanel getDocumentsExtPanel(){ return documentsExtPanel;}
    public JPanel getOthersExtPanel(){ return othersExtPanel;}
    public JRadioButton getMoveRadioButton(){ return moveRadioButton;}
    public JRadioButton getTopLevelRadioButton(){ return topLevelRadioButton;}
    public JRadioButton getDeepScanRadioButton(){ return deepScanRadioButton;}
    public JRadioButton getOrganizeByCategoryRadioButton(){ return organizeByCategoryRadioButton;}
    public JRadioButton getAllFileTypesRadioButton(){ return allFileTypesRadioButton;}
    public JCheckBox getByExtensionCheckBox(){ return byExtensionCheckBox;}
    public JCheckBox getImagesCheckBox(){ return imagesCheckBox;}
    public JCheckBox getVideosCheckBox(){ return videosCheckBox;}
    public JCheckBox getAudioCheckBox(){ return audioCheckBox;}
    public JCheckBox getDocumentsCheckBox(){ return documentsCheckBox;}
    public JCheckBox getOthersCheckBox(){ return othersCheckBox;}

    //BOTTOM PANEL
    public JTextArea getLogArea(){return logArea;}
    public JButton getStartOrganizeButton(){return startOrganizeButton;}
    public JButton getClearLogsButton(){return clearLogsButton;}

    //============================================== MAIN METHOD ===========================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FileOrganizerGUI gui = new FileOrganizerGUI();
            new FileOrganizerController(gui);
            gui.setVisible(true);
        });
    }
}