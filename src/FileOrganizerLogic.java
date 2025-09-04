import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;

public class FileOrganizerLogic {

    // ---- Category sets defined once ----
    public static final Set<String> IMAGES = Set.of("jpg", "jpeg", "png", "gif", "bmp", "ico", "tiff", "svg");
    public static final Set<String> DOCS   = Set.of("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv", "rtf");
    public static final Set<String> AUDIOS = Set.of("mp3", "wav", "flac", "aac", "ogg", "m4a");
    public static final Set<String> VIDEO  = Set.of("mp4", "mkv", "mov", "avi", "wmv");

    // All file types (top-level or deep)
    public static void moveOrCopyAllFileTypes(
            String sourceFolder, String destinationFolder, String action, boolean deepScan, Consumer<String> logCallBack,
            BiFunction<File, boolean[], FileOrganizerController.OverwriteDecision> overwriteHandler){

        File sourceDir = new File(sourceFolder);
        File destinationDir = new File(destinationFolder);

        if (!destinationDir.exists()) {
            if (destinationDir.mkdirs()) logCallBack.accept("[INFO] Created destination folder: " + destinationDir.getAbsolutePath());
            else logCallBack.accept("[ERROR] Could not create destination folder: " + destinationDir.getAbsolutePath());
        }

        Map<String, Object> overwriteState = new HashMap<>();
        processDirectory(sourceDir, destinationDir, action, deepScan, logCallBack, overwriteHandler,
                null,null,true, overwriteState);
    }

    // === By category (top-level or deep) ===
    public static void moveOrCopyByCategory(
            String sourceFolder, String destinationFolder, String action, boolean deepScan,
            Set<String> categoriesFilter, Map<String, Set<String>> extensionsFilter, Consumer<String> logCallBack,
            BiFunction<File, boolean[], FileOrganizerController.OverwriteDecision> overwriteHandler) {

        if (categoriesFilter == null || categoriesFilter.isEmpty()) {
            logCallBack.accept("[WARNING] No categories selected.");
            return;
        }

        File sourceDir = new File(sourceFolder);
        File destinationDir = new File(destinationFolder);

        if (!destinationDir.exists()) {
            if (destinationDir.mkdirs()) logCallBack.accept("[INFO] Created destination folder: " + destinationDir.getAbsolutePath());
            else logCallBack.accept("[ERROR] Could not create destination folder: " + destinationDir.getAbsolutePath());
        }

        Map<String, Object> overwriteState = new HashMap<>();
        processDirectory(sourceDir, destinationDir, action, deepScan, logCallBack, overwriteHandler,
                categoriesFilter,extensionsFilter,true, overwriteState);
    }

    // Handles top-level vs deep-scan, with optional category filtering
    private static void processDirectory(
            File dir, File destinationDir, String action, boolean deepScan, Consumer<String> log,
            BiFunction<File, boolean[], FileOrganizerController.OverwriteDecision> overwriteHandler,
            Set<String> categoriesFilter,  Map<String, Set<String>> extensionsFilter,
            boolean isRoot, Map<String, Object> overwriteState) {

        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            log.accept("[WARNING] Source folder does not exist or is not a directory: " + (dir == null ? "null" : dir.getAbsolutePath()));
            return;
        }

        File[] childFiles = dir.listFiles();
        if (childFiles == null || childFiles.length == 0) {
            log.accept("[INFO] No files found in subfolder: " + dir.getAbsolutePath());
            return;
        }

        // ✅ Top-level check: only when isRoot == true
        if (isRoot) {
            boolean hasFile = Arrays.stream(childFiles).anyMatch(File::isFile);   //used to scan top-level files
            if (!hasFile) {
                log.accept("[INFO] No top-level files found in: " + dir.getAbsolutePath());
                // don't return → still allow scanning subdirectories
            }
        }

        for (File child : childFiles) {
            // 🚨 Add cancel check here
            if (overwriteState.containsKey("cancelled")) {
                return; // stop processing immediately
            }

            if (child.isDirectory()) {
                if (deepScan) {
                    processDirectory(child, destinationDir, action, true, log, overwriteHandler, categoriesFilter,extensionsFilter,false, overwriteState);
                }
                continue;
            }

            String name = child.getName();
            if (name.startsWith(".")) {
                log.accept("[INFO] Skipped hidden file: " + name);
                continue;
            }

            String ext = extOf(name);
            String category = categoryForExt(ext); // always returns non-null

            // ✅ Category filter
            if (categoriesFilter != null && categoriesFilter.stream().noneMatch(c -> c.equalsIgnoreCase(category))) {
                log.accept("[INFO] Skipped (not in selected categories): " + name);
                continue;
            }

            // ✅ Extension filter (only if provided)
            if (extensionsFilter != null && !extensionsFilter.isEmpty()) {
                Set<String> allowedExts = extensionsFilter.get(category); // get selected extensions for this category
                if (allowedExts != null && !allowedExts.isEmpty() && !allowedExts.contains(ext)) {
                    log.accept("[INFO] Skipped (extension not selected): " + name);
                    continue;
                }
            }

            // ✅ Ensure category folder exists (only if not cancelled)
            if (overwriteState.containsKey("cancelled")) {
                return;
            }

            // ✅ Ensure category folder exists
            File categoryDir = new File(destinationDir, category);
            if (!categoryDir.exists()) {
                if (categoryDir.mkdirs()) log.accept("[INFO] Created category folder: " + category);
                else log.accept("[WARNING] Could not create category folder: " + category);
            }

            // ✅ Perform move/copy
            File targetFile = new File(categoryDir, name);
            moveOrCopy(child, targetFile, action, log, overwriteHandler, overwriteState);
        }
    }

    // === Copy or move with overwrite handling ===
    private static void moveOrCopy(
            File sourceFile, File targetFile, String action, Consumer<String> log,
            BiFunction<File, boolean[], FileOrganizerController.OverwriteDecision> overwriteHandler,
            Map<String, Object> overwriteState) {

        if (overwriteState.containsKey("cancelled")) { return;} // stop immediately if cancelled earlier

        try {
            if (targetFile.exists()) {

                FileOrganizerController.OverwriteDecision decision;

                // If user already selected "Apply to All", reuse that decision
                if(overwriteState.containsKey("decision")) decision = (FileOrganizerController.OverwriteDecision) overwriteState.get("decision");
                else{
                    //Ask user
                    boolean[] applyToAll = {false};
                    decision = overwriteHandler.apply(targetFile, applyToAll);

                    //applyToAll[0] = true if checkbox is ticked
                    if(decision != FileOrganizerController.OverwriteDecision.CANCEL && applyToAll[0]){
                        overwriteState.put("decision", decision);
                        log.accept("[INFO] Applied decision to all remaining files: " + decision);
                    }
                }

                // Handle cancel
                if (decision == FileOrganizerController.OverwriteDecision.CANCEL) {
                    log.accept("[INFO] Operation cancelled by the user. Stopping further processing");
                    overwriteState.put("cancelled", true);
                    return;
                }

                // Apply user’s choice
                switch (decision) {
                    case SKIP -> {
                        log.accept("[INFO] Skipped: " + sourceFile.getName());
                        return;
                    }
                    case KEEP_BOTH -> targetFile = nextAvailableName(targetFile);
                    case OVERWRITE -> {
                        // Replace existing
                    }
                    default -> {
                        // no-op, for future extensions
                    }
                }
            }

            if ("move".equalsIgnoreCase(action)) {
                Files.move(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                log.accept("[INFO] Moved: " + sourceFile.getName() + " → " + targetFile.getAbsolutePath());
            } else {
                Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                log.accept("[INFO] Copied: " + sourceFile.getName() + " → " + targetFile.getAbsolutePath());
            }

        } catch (IOException e) {
            log.accept("[Error]: " + sourceFile.getName() + " → " + e.getMessage());
        }
    }


    // === Helpers ===
    public static String extOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot == -1 || dot == fileName.length() - 1) return "";
        return fileName.substring(dot + 1).toLowerCase();
    }

    private static String categoryForExt(String ext) {
        if (ext.isEmpty())        return "Others";                 // no extension → directly "Others"
        if (IMAGES.contains(ext)) return "Images";
        if (DOCS.contains(ext))   return "Documents";
        if (AUDIOS.contains(ext)) return "Audios";
        if (VIDEO.contains(ext))  return "Videos";

        return "Others";
    }

    private static File nextAvailableName(File target){
        if(!target.exists()) return target;

        String name = target.getName();
        String base = name;
        String ext = "";
        int dot = name.lastIndexOf('.');

        if(dot > 0){
            base = name.substring(0, dot);
            ext = name.substring(dot);
        }

        int counter = 1;
        File dupFile;

        do {
            dupFile = new File(target.getParentFile(), base + " (" + counter + ")" + ext);
            counter++;
        } while(dupFile.exists());
        return dupFile;
    }

    public static Map<String, Set<String>> scanExtensionsByCategory(String folderPath, boolean deepScan) {

        Map<String, Set<String>> categoryMap = new HashMap<>();
        categoryMap.put("Images", new HashSet<>());
        categoryMap.put("Documents", new HashSet<>());
        categoryMap.put("Audios", new HashSet<>());
        categoryMap.put("Videos", new HashSet<>());
        categoryMap.put("Others", new HashSet<>());

        File sourceDir = new File(folderPath);
        scanDirectoryForExtensions(sourceDir, deepScan, categoryMap);

        return categoryMap;
    }

    private static void scanDirectoryForExtensions(File dir, boolean deepScan, Map<String, Set<String>> categoryMap) {

        if (dir == null || !dir.isDirectory()) return;

        File[] files = dir.listFiles();

        if (files == null) return;

        for (File f : files) {
            if (f.isFile()) {
                String ext = extOf(f.getName());
                String category = categoryForExt(ext);
                categoryMap.get(category).add(ext);
            } else if (deepScan) scanDirectoryForExtensions(f, true, categoryMap);
        }
    }

    // Delete empty folders recursively (for deep scan)
    public static void deleteEmptyFolders(File dir, Consumer<String> log) {
        if (dir == null || !dir.isDirectory()) return;

        File[] children = dir.listFiles();

        if (children == null) return;

        for (File child : children) {
            if (child.isDirectory()) {
                // Recurse first
                deleteEmptyFolders(child, log);

                // Delete if now empty
                File[] subFiles = child.listFiles();
                if (subFiles != null && subFiles.length == 0) {
                    if (child.delete()) log.accept("[INFO] Deleted empty folder: " + child.getAbsolutePath());
                    else log.accept("[WARNING] Could not delete: " + child.getAbsolutePath());
                }
            }
        }
    }

    // Delete only immediate empty subfolders (for top-level scan)
    public static void deleteEmptyFoldersTopLevel(File sourceDir, Consumer<String> log) {
        if (sourceDir == null || !sourceDir.isDirectory()) return;

        File[] children = sourceDir.listFiles(File::isDirectory);
        if (children == null) return;

        for (File child : children) {
            File[] subFiles = child.listFiles();
            if (subFiles != null && subFiles.length == 0) {
                if (child.delete()) log.accept("[INFO] Deleted empty folder: " + child.getAbsolutePath());
                else log.accept("[WARNING] Could not delete: " + child.getAbsolutePath());
            }
        }
    }
}
