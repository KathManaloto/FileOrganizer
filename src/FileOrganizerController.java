import javax.swing.*;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.*;
import java.awt.BorderLayout;
import java.awt.Component;

public class FileOrganizerController {

    //FIELDS
    private final FileOrganizerGUI gui;
    private File lastSourceDir = null;
    private File lastDestinationDir = null;
    public enum OverwriteDecision {
        OVERWRITE, KEEP_BOTH, SKIP, CANCEL
    }

    //CONSTRUCTOR
    public FileOrganizerController(FileOrganizerGUI gui) {
        this.gui = gui;
        attachListeners();
        validateByExtensionState();
    }

    //============================================ ACTION LISTENERS ========================================================

    private void attachListeners() {

        //TOP
        gui.getBrowseSourceButton().addActionListener(e -> onBrowseSource());
        gui.getBrowseDestinationButton().addActionListener(e -> onBrowseDestination());

        //CENTER
        gui.getTopLevelRadioButton().addActionListener(e -> updateExtensionPanelsVisibility());
        gui.getDeepScanRadioButton().addActionListener(e -> updateExtensionPanelsVisibility());
        gui.getAllFileTypesRadioButton().addActionListener(e -> onAllFileTypesSelected());
        gui.getOrganizeByCategoryRadioButton().addActionListener(e -> onOrganizeByCategory());
        gui.getImagesCheckBox().addActionListener(e -> onImageCheckBox());
        gui.getDocumentsCheckBox().addActionListener(e -> onDocumentCheckBox());
        gui.getAudioCheckBox().addActionListener(e -> onAudioCheckBox());
        gui.getVideosCheckBox().addActionListener(e -> onVideoCheckBox());
        gui.getOthersCheckBox().addActionListener(e -> onOthersCheckBox());

        //BOTTOM
        gui.getClearLogsButton().addActionListener(e -> onClearLogs());
        gui.getStartOrganizeButton().addActionListener(e -> {
            String sourceFolder = gui.getSourceFolderPathField().getText().trim();
            String destinationFolder = gui.getDestinationFolderPathField().getText().trim();

            //extra check if the source and destination folders were selected
            if (sourceFolder.isEmpty() || destinationFolder.isEmpty()) {
                showError("Please select both source and destination folders.");
                return;
            }

            //Wrap the folders in File objects
            File sourceDir = new File(sourceFolder);
            File destinationDir = new File(destinationFolder);

            if (!validateFolders(sourceDir, destinationDir)) {
                return;
            }
            onStartOrganize(sourceFolder, destinationFolder);
        });

        ItemListener validateByExtensionListener = e -> validateByExtensionState();
        gui.getImagesCheckBox().addItemListener(validateByExtensionListener);
        gui.getDocumentsCheckBox().addItemListener(validateByExtensionListener);
        gui.getAudioCheckBox().addItemListener(validateByExtensionListener);
        gui.getVideosCheckBox().addItemListener(validateByExtensionListener);
        gui.getOthersCheckBox().addItemListener(validateByExtensionListener);
        gui.getByExtensionCheckBox().addItemListener(e -> updateExtensionPanelsVisibility());

    }

    //=============================================== TOP PANEL ============================================================

    private void onBrowseSource() {
        File startDir = lastSourceDir;

        if (startDir == null && lastDestinationDir != null) {
            startDir = lastDestinationDir.getParentFile();
        }

        JFileChooser chooser = new JFileChooser(startDir);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select Source Folder Path");

        int result = chooser.showOpenDialog(gui);
        if (result == JFileChooser.APPROVE_OPTION) {
            lastSourceDir = chooser.getSelectedFile();
            gui.getSourceFolderPathField().setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void onBrowseDestination() {
        File startDir = lastDestinationDir;

        if (startDir == null && lastSourceDir != null) {
            startDir = lastSourceDir.getParentFile();
        }

        JFileChooser chooser = new JFileChooser(startDir);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select Destination Folder Path");

        int result = chooser.showOpenDialog(gui);
        if (result == JFileChooser.APPROVE_OPTION) {
            lastDestinationDir = chooser.getSelectedFile();
            gui.getDestinationFolderPathField().setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private boolean validateFolders(File sourceDir, File destinationDir) {
        if (sourceDir == null || destinationDir == null) {
            showError("Please select both source and destination folders.");
            return false;
        }

        boolean invalidSource = !sourceDir.exists() || !sourceDir.isDirectory();
        boolean invalidDestination = !destinationDir.exists() || !destinationDir.isDirectory();

        if (invalidSource && invalidDestination) {
            showError("The selected source and destination folders are invalid!");
            return false;
        }

        if (invalidSource) {
            showError("The selected source folder does not exist or is not a directory.");
            return false;
        }

        if (invalidDestination) {
            showError("The selected destination folder does not exist or is not a directory.");
            return false;
        }

        if (sourceDir.equals(destinationDir)) {
            showError("The source and destination folders cannot be the same.");
            return false;
        }

        if (destinationDir.getAbsolutePath().startsWith(sourceDir.getAbsolutePath())) {
            showError("The destination folder cannot be inside the source folder.");
            return false;
        }

        return true;
    }

    //============================================= CENTER PANEL ===========================================================

    private void onAllFileTypesSelected() {
        setFileTypeVisibility(false);
        setFileTypeExtensionVisibility(false);
    }

    private void onOrganizeByCategory() {
        setFileTypeVisibility(true);
        setFileTypeControlSelection(false);
        gui.getFileTypeCategoryPanel().revalidate();
    }

    private void onImageCheckBox() {
        if (!gui.getImagesCheckBox().isSelected()) {
            resetExtensionCheckboxes(gui.getImagesExtPanel());
        }
        updateExtensionPanelsVisibility();
    }

    private void onDocumentCheckBox() {
        if (!gui.getDocumentsCheckBox().isSelected()) {
            resetExtensionCheckboxes(gui.getDocumentsExtPanel());
        }
        updateExtensionPanelsVisibility();
    }

    private void onAudioCheckBox() {
        if (!gui.getAudioCheckBox().isSelected()) {
            resetExtensionCheckboxes(gui.getAudioExtPanel());
        }
        updateExtensionPanelsVisibility();
    }

    private void onVideoCheckBox() {
        if (!gui.getVideosCheckBox().isSelected()) {
            resetExtensionCheckboxes(gui.getVideoExtPanel());
        }
        updateExtensionPanelsVisibility();
    }

    private void onOthersCheckBox() {
        if (!gui.getOthersCheckBox().isSelected()) {
            resetExtensionCheckboxes(gui.getOthersExtPanel());
        }
        updateExtensionPanelsVisibility();
    }
    //============================================= HELPERS ===========================================================

    private void setFileTypeVisibility(boolean visible) {
        gui.getFileTypeCategoryPanel().setVisible(visible);
        gui.getByExtensionCheckBox().setVisible(visible);
        gui.getImagesCheckBox().setVisible(visible);
        gui.getDocumentsCheckBox().setVisible(visible);
        gui.getAudioCheckBox().setVisible(visible);
        gui.getVideosCheckBox().setVisible(visible);
        gui.getOthersCheckBox().setVisible(visible);
    }

    private void setFileTypeExtensionVisibility(boolean visible) {
        gui.getImagesExtPanel().setVisible(visible);
        gui.getDocumentsExtPanel().setVisible(visible);
        gui.getAudioExtPanel().setVisible(visible);
        gui.getVideoExtPanel().setVisible(visible);
        gui.getOthersExtPanel().setVisible(visible);
    }

    private void setFileTypeControlSelection(boolean selected) {
        gui.getByExtensionCheckBox().setSelected(selected);
        gui.getImagesCheckBox().setSelected(selected);
        gui.getDocumentsCheckBox().setSelected(selected);
        gui.getAudioCheckBox().setSelected(selected);
        gui.getVideosCheckBox().setSelected(selected);
        gui.getOthersCheckBox().setSelected(selected);
    }

    private void resetExtensionCheckboxes(JPanel extPanel) {
        for (Component comp : extPanel.getComponents()) {
            if (comp instanceof JCheckBox) {
                ((JCheckBox) comp).setSelected(false);
            }
        }
    }

    private void validateByExtensionState() {
        boolean anyTypeSelected =
                gui.getImagesCheckBox().isSelected()    ||
                gui.getDocumentsCheckBox().isSelected() ||
                gui.getAudioCheckBox().isSelected()     ||
                gui.getVideosCheckBox().isSelected()    ||
                gui.getOthersCheckBox().isSelected();

        if (!anyTypeSelected) {
            gui.getByExtensionCheckBox().setSelected(false);
        }
        gui.getByExtensionCheckBox().setEnabled(anyTypeSelected);
        updateExtensionPanelsVisibility();
    }

    private void updateExtensionPanelsVisibility() {
        boolean showExtensions = gui.getByExtensionCheckBox().isSelected();
        boolean deepScan = gui.getDeepScanRadioButton().isSelected();
        String sourceFolder = gui.getSourceFolderPathField().getText();

        if (sourceFolder == null || sourceFolder.isBlank()) {
            refreshCategoryExtensions(false, gui.getImagesCheckBox(), gui.getImagesExtPanel(), Collections.emptySet(),"Images");
            refreshCategoryExtensions(false, gui.getDocumentsCheckBox(), gui.getDocumentsExtPanel(), Collections.emptySet(),"Documents");
            refreshCategoryExtensions(false, gui.getAudioCheckBox(), gui.getAudioExtPanel(), Collections.emptySet(),"Audios");
            refreshCategoryExtensions(false, gui.getVideosCheckBox(), gui.getVideoExtPanel(), Collections.emptySet(),"Videos");
            refreshCategoryExtensions(false, gui.getOthersCheckBox(), gui.getOthersExtPanel(), Collections.emptySet(),"Others");
            return;
        }

        Map<String, Set<String>> extMap = FileOrganizerLogic.scanExtensionsByCategory(sourceFolder, deepScan);

        refreshCategoryExtensions(showExtensions, gui.getImagesCheckBox(), gui.getImagesExtPanel(), extMap.get("Images"), "Images");
        refreshCategoryExtensions(showExtensions, gui.getDocumentsCheckBox(), gui.getDocumentsExtPanel(), extMap.get("Documents"), "Documents");
        refreshCategoryExtensions(showExtensions, gui.getAudioCheckBox(), gui.getAudioExtPanel(), extMap.get("Audios"), "Audios");
        refreshCategoryExtensions(showExtensions, gui.getVideosCheckBox(), gui.getVideoExtPanel(), extMap.get("Videos"), "Videos");
        refreshCategoryExtensions(showExtensions, gui.getOthersCheckBox(), gui.getOthersExtPanel(), extMap.get("Others"), "Others");
    }

    private void refreshCategoryExtensions(
            boolean showExtensions,
            JCheckBox categoryCheckBox,
            JPanel categoryPanel,
            Set<String> exts,
            String categoryName
    ) {
        // Get selections saved so far
        Map<String, Set<String>> currentlySelected = gui.getSelectedExtensions();
        Set<String> selectedForThisCategory = currentlySelected.getOrDefault(categoryName, new HashSet<>());

        boolean shouldShow = showExtensions && categoryCheckBox.isSelected();
        categoryPanel.setVisible(shouldShow);

        if (shouldShow && exts != null && !exts.isEmpty()) {
            // ✅ Use GUI method so "All" checkbox is created
            gui.populateExtensionsForCategory(categoryPanel, new ArrayList<>(exts));

            // ✅ Restore previous selections, including "All"
            for (Component comp : categoryPanel.getComponents()) {
                if (comp instanceof JCheckBox cb) {
                    String label = cb.getText().replaceFirst("^\\.", ""); // remove dot if present

                    if (label.equalsIgnoreCase("All")) {
                        if (selectedForThisCategory.contains("All")) {
                            cb.setSelected(true);

                            // Also select all extension checkboxes for consistency
                            for (Component extComp : categoryPanel.getComponents()) {
                                if (extComp instanceof JCheckBox extCb &&
                                        !extCb.getText().equalsIgnoreCase("All")) {
                                    extCb.setSelected(true);
                                }
                            }
                        }
                    } else if (selectedForThisCategory.contains(label) ||
                            selectedForThisCategory.contains(cb.getText())) {
                        cb.setSelected(true);
                    }
                }
            }

        } else {
            categoryPanel.removeAll(); // ensure it's empty when hidden
        }

        categoryPanel.revalidate();
        categoryPanel.repaint();
    }


    //============================================= START ORGANIZE ===========================================================

    private void onStartOrganize(String sourceFolder, String destinationFolder) {

        String action = gui.getMoveRadioButton().isSelected() ? "move" : "copy";
        boolean deepScan = gui.getDeepScanRadioButton().isSelected();

        logInfo("===== PROCESS START =====");
        logInfo("Source Folder: " + sourceFolder);
        logInfo("Destination Folder: " + destinationFolder);

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            private boolean fatalError = false;

            @Override
            protected Void doInBackground() {
                if (gui.getAllFileTypesRadioButton().isSelected()) {
                    FileOrganizerLogic.moveOrCopyAllFileTypes(
                            sourceFolder, destinationFolder, action, deepScan,
                            this::publish,   // ✅ publish logs
                            FileOrganizerController.this::handleOverwrite
                    );

                } else if (gui.getOrganizeByCategoryRadioButton().isSelected()) {
                    Set<String> categoriesFilter = gui.getSelectedCategories();

                    // 🚨 1. Validate categories
                    if (categoriesFilter == null || categoriesFilter.isEmpty()) {
                        String msg = "No categories selected. Please select your preferred categories.";

                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                                gui.getFrame(),
                                msg,
                                "No Categories Selected",
                                JOptionPane.WARNING_MESSAGE
                        ));
                        publish("[WARNING] " + msg);

                        fatalError = true; // mark fatal stop
                        return null;
                    }

                    boolean byExtension = gui.getByExtensionCheckBox().isSelected();
                    Map<String, Set<String>> extensionsFilter = byExtension ? gui.getSelectedExtensions() : null;

                    // 🚨 2. Validate extensions if "By Extension" is enabled
                    if (byExtension) {
                        boolean noneSelected = extensionsFilter.values().stream().allMatch(Set::isEmpty);
                        if (noneSelected) {
                            String msg = "By Extension is enabled, but no extensions are selected. Please select at least one.";

                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                                    gui.getFrame(),
                                    msg,
                                    "No Extensions Selected",
                                    JOptionPane.WARNING_MESSAGE
                            ));
                            publish("[WARNING] " + msg);

                            fatalError = true; // mark fatal stop
                            return null;
                        }
                    }

                    // ✅ Proceed only if validations pass
                    FileOrganizerLogic.moveOrCopyByCategory(
                            sourceFolder, destinationFolder, action,
                            deepScan, categoriesFilter, extensionsFilter,
                            this::publish,   // publish logs
                            FileOrganizerController.this::handleOverwrite
                    );
                }
                return null;
            }

            @Override
            protected void process(java.util.List<String> logs) {
                for (String log : logs) {
                    gui.log(log);  // ✅ updates GUI in real-time
                }
            }

            @Override
            protected void done() {
                if (fatalError) {
                    // 🚨 Skip all finalization (like delete-empty-folder prompt)
                    return;
                }

                String msg = deepScan
                        ? "Do you want to delete empty folders (including subfolders) in the source directory?"
                        : "Do you want to delete empty folders directly under the source directory?";

                int choice = JOptionPane.showConfirmDialog(
                        gui.getFrame(),
                        msg,
                        "Delete Empty Folders",
                        JOptionPane.YES_NO_OPTION
                );

                if (choice == JOptionPane.YES_OPTION) {
                    File sourceDir = new File(sourceFolder);
                    if (deepScan) {
                        FileOrganizerLogic.deleteEmptyFolders(sourceDir, gui::log);
                    } else {
                        FileOrganizerLogic.deleteEmptyFoldersTopLevel(sourceDir, gui::log);
                    }
                }

                gui.log("[INFO] ===== PROCESS COMPLETE =====\n");
            }
        };

        worker.execute();
    }

    //============================================= OVERWRITE DIALOG ===========================================================

    public OverwriteDecision handleOverwrite(File targetFile, boolean[] applyToAll) {
        JCheckBox applyToAllCheck = new JCheckBox("Apply to all files");

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JLabel label = new JLabel("<html>File already exists:<br>"
                + targetFile.getAbsolutePath()
                + "<br><br>What do you want to do?</html>");

        panel.add(label, BorderLayout.NORTH);
        panel.add(applyToAllCheck, BorderLayout.SOUTH);

        String[] options = {"Overwrite", "Keep Both", "Skip", "Cancel"};

        int choice = JOptionPane.showOptionDialog(
                gui.getFrame(),
                panel,
                "Duplicate File",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        OverwriteDecision decision = switch (choice) {
            case 0 -> OverwriteDecision.OVERWRITE;
            case 1 -> OverwriteDecision.KEEP_BOTH;
            case 2 -> OverwriteDecision.SKIP;
            default -> OverwriteDecision.CANCEL;
        };

        applyToAll[0] = (decision != OverwriteDecision.CANCEL) && applyToAllCheck.isSelected();

        gui.log("[INFO] Actions for duplicate file/s: " + decision
                + (applyToAll[0] ? " (applied to all)" : ""));

        return decision;
    }

    // --- Logging helpers ---
    private void logInfo(String message) { logCallback("[INFO] " + message);}
    private void logWarning(String message) { logCallback("[WARNING] " + message);}
    private void logError(String message) { logCallback("[ERROR] " + message);}

    private void logCallback(String logMessage) {
        // ✅ Instead of writing directly, let SwingWorker publish it if running
        gui.log(logMessage);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(gui.getFrame(), message, "Validation error", JOptionPane.ERROR_MESSAGE);
        logError(message);
    }

    private void onClearLogs() {
        gui.getLogArea().setText("");
        logInfo("Logs cleared");
    }
}