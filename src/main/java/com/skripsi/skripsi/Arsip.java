package com.skripsi.skripsi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Arsip {

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Choose an option:");
        System.out.println("1. Archive files");
        System.out.println("2. Extract files");
        System.out.println("3. List archive contents");
        System.out.println("4. Extract specific file");
        System.out.println("5. Extract specific folder");
        System.out.println("6. Delete specific file or folder");
        System.out.println("7. Archive to an existing archive");
        System.out.print("Enter your choice (1, 2, 3, 4, 5, 6, or 7): ");
        int choice = Integer.parseInt(reader.readLine());

        if (choice == 1) {
            System.out.print("Enter the path to the source directory: ");
            String sourceDir = reader.readLine();

            System.out.print("Enter the path to the archive file: ");
            String archiveFilePath = reader.readLine();

            archiveFiles(new File(sourceDir), archiveFilePath, "");
            System.out.println("Files archived successfully.");
        } else if (choice == 2) {
            System.out.print("Enter the path to the archive file: ");
            String archiveFilePathExtract = reader.readLine();

            System.out.print("Enter the path to the extraction directory: ");
            String extractionDir = reader.readLine();

            extractFiles(archiveFilePathExtract, extractionDir);
            System.out.println("Files extracted successfully.");
        } else if (choice == 3) {
            System.out.print("Enter the path to the archive file: ");
            String archiveFilePathList = reader.readLine();

            listArchiveContents(archiveFilePathList);
        } else if (choice == 4) {
            System.out.print("Enter the path to the archive file: ");
            String archiveFilePathExtract = reader.readLine();

            System.out.print("Enter the path to the extraction directory: ");
            String extractionDir = reader.readLine();

            System.out.print("Enter the specific file to extract: ");
            String entryToExtract = reader.readLine();

            extractSpecificFile(archiveFilePathExtract, extractionDir, entryToExtract);
            System.out.println("Specific file extracted successfully.");
        } else if (choice == 5) {
            System.out.print("Enter the path to the archive file: ");
            String archiveFilePathExtract = reader.readLine();

            System.out.print("Enter the path to the extraction directory: ");
            String extractionDir = reader.readLine();

            System.out.print("Enter the specific folder to extract: ");
            String entryToExtract = reader.readLine();

            extractSpecificFolder(archiveFilePathExtract, extractionDir, entryToExtract);
            System.out.println("Specific folder extracted successfully.");
        } else if (choice == 6) {
            System.out.print("Enter the path to the archive file: ");
            String archiveFilePathDelete = reader.readLine();

            System.out.print("Enter the specific file or folder to delete: ");
            String entryToDelete = reader.readLine();

            deleteEntry(archiveFilePathDelete, entryToDelete);
            System.out.println("Specific file or folder deleted successfully.");
        } else if (choice == 7) {
            System.out.print("Enter the path to the source directory: ");
            String sourceDir = reader.readLine();

            System.out.print("Enter the path to the archive file: ");
            String archiveFilePath = reader.readLine();

            updateArchiveWithDirectory(new File(sourceDir), archiveFilePath);
            System.out.println("Files archived successfully.");
        } else {
            System.out.println("Invalid choice.");
            reader.close();
        }
    }

    private static void archiveFiles(File dir, String archiveFilePath, String basePath)
            throws IOException, NoSuchAlgorithmException {
        FileOutputStream fos = new FileOutputStream(archiveFilePath, true);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        DataOutputStream dos = new DataOutputStream(bos);

        if (basePath != "") {
            String entryName = basePath;
            String lenghString = String.format("%05d", entryName.length());
            dos.writeBytes(lenghString);
            dos.writeBytes(entryName);
            dos.writeLong(0);
            // System.out.println("Created Directory: " + basePath); // Debug output
        }

        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    System.out.println("Archiving Directory: " + basePath + file.getName() + "/"); // Debug output
                    archiveFiles(file, archiveFilePath, basePath + file.getName() + "/");
                } else {
                    String entryName = basePath + file.getPath().replace(dir.getPath() + File.separator, "");
                    String lenghString = String.format("%05d", entryName.length());
                    dos.writeBytes(lenghString);
                    dos.writeBytes(entryName);
                    dos.writeLong(file.length());
                    System.out.println("Archiving File: " + entryName); // Debug output

                    FileInputStream fis = new FileInputStream(file);
                    String checksum = calculateMD5(file); // Calculate and store MD5 checksum for each file;
                    byte[] buffer = new byte[(int) file.length()];
                    fis.read(buffer);
                    dos.write(buffer);
                    fis.close();
                    dos.writeBytes(checksum);
                }
            }
        }
        dos.close();
        bos.close();
        fos.close();
    }

    private static void extractFiles(String archiveFilePath, String extractionDir)
            throws IOException, NoSuchAlgorithmException {
        FileInputStream fis = new FileInputStream(archiveFilePath);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream dis = new DataInputStream(bis);

        while (dis.available() > 0) {
            byte[] lengthBytes = new byte[5];
            dis.readFully(lengthBytes);
            int entryNameLength = Integer.parseInt(new String(lengthBytes, StandardCharsets.ISO_8859_1).trim());
            // System.out.println("Entry Name Lenght: " + entryNameLength);
            byte[] entryNameBytes = new byte[entryNameLength];
            dis.readFully(entryNameBytes);
            String entryName = new String(entryNameBytes, StandardCharsets.ISO_8859_1);
            long fileSize = dis.readLong();
            // System.out.println("Extracting: " + entryName); //Debug Output
            // System.out.println("Size: " + fileSize); // Debug Output

            if (entryName.endsWith("/")) {
                File directory = new File(extractionDir + File.separator + entryName);
                directory.mkdirs();
                System.out.println("Extracting Directory: " + entryName);
            } else if (!entryName.isEmpty()) {
                byte[] fileContent = new byte[(int) fileSize];
                dis.readFully(fileContent);
                FileOutputStream fos = new FileOutputStream(extractionDir + File.separator + entryName);
                fos.write(fileContent);
                fos.close();
                System.out.println("Extracting File: " + entryName);

                // Validate checksum of extracted files
                byte[] checksum = new byte[32];
                dis.readFully(checksum);
                String storedChecksum = new String(checksum, StandardCharsets.ISO_8859_1);
                // System.out.println(storedChecksum); //Debug Output
                String extractedChecksum = calculateMD5(new File(extractionDir + File.separator + entryName));
                // System.out.println(extractedChecksum); //Debug Output
                if (extractedChecksum.equals(storedChecksum)) {
                    System.out.println("Checksum matched for file: " + entryName);
                } else {
                    System.out.println("Checksum mismatch for file: " + entryName);
                }
            }
        }
        dis.close();
        bis.close();
        fis.close();
    }

    private static void extractSpecificFile(String archiveFilePath, String extractionDir, String entryToExtract)
            throws IOException, NoSuchAlgorithmException {
        FileInputStream fis = new FileInputStream(archiveFilePath);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream dis = new DataInputStream(bis);

        while (dis.available() > 0) {
            byte[] lengthBytes = new byte[5];
            dis.readFully(lengthBytes);
            int entryNameLength = Integer.parseInt(new String(lengthBytes, StandardCharsets.ISO_8859_1).trim());
            // System.out.println("Entry Name Lenght: " + entryNameLength);
            byte[] entryNameBytes = new byte[entryNameLength];
            dis.readFully(entryNameBytes);
            String entryName = new String(entryNameBytes, StandardCharsets.ISO_8859_1);
            // System.out.println(entryName);
            long fileSize = dis.readLong();
            // System.out.println(fileSize);

            if (entryName.equals(entryToExtract)) {
                String fileName = entryName.substring(entryName.lastIndexOf("/"), entryName.length());
                byte[] fileContent = new byte[(int) fileSize];
                dis.readFully(fileContent);
                FileOutputStream fos = new FileOutputStream(extractionDir + File.separator + fileName);
                fos.write(fileContent);
                fos.close();
                System.out.println("Extracting File: " + entryName);

                // Validate checksum of extracted file
                byte[] checksum = new byte[32];
                dis.readFully(checksum);
                String storedChecksum = new String(checksum, StandardCharsets.ISO_8859_1);
                // System.out.println(storedChecksum); //Debug Output
                String extractedChecksum = calculateMD5(new File(extractionDir + File.separator + fileName));
                // System.out.println(extractedChecksum); //Debug Output
                if (extractedChecksum.equals(storedChecksum)) {
                    System.out.println("Checksum matched for file: " + entryName);
                } else {
                    System.out.println("Checksum mismatch for file: " + entryName);
                }
                break;
            } else if (!entryName.equals(entryToExtract) && !entryName.endsWith("/")) {
                int skippedBytes = (int) fileSize + 32;
                dis.skipBytes(skippedBytes);
            }
        }

        dis.close();
        bis.close();
        fis.close();
    }

    private static void extractSpecificFolder(String archiveFilePath, String extractionDir, String entryToExtract)
            throws IOException, NoSuchAlgorithmException {
        FileInputStream fis = new FileInputStream(archiveFilePath);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream dis = new DataInputStream(bis);

        while (dis.available() > 0) {
            byte[] lengthBytes = new byte[5];
            dis.readFully(lengthBytes);
            int entryNameLength = Integer.parseInt(new String(lengthBytes, StandardCharsets.ISO_8859_1).trim());
            // System.out.println("Entry Name Lenght: " + entryNameLength);
            byte[] entryNameBytes = new byte[entryNameLength];
            dis.readFully(entryNameBytes);
            String entryName = new String(entryNameBytes, StandardCharsets.ISO_8859_1);
            // System.out.println(entryName);
            long fileSize = dis.readLong();
            // System.out.println(fileSize);

            if (entryName.startsWith(entryToExtract)) {
                File directory = new File(extractionDir + File.separator + entryToExtract);
                // System.out.println(extractionDir + File.separator + entryToExtract);
                directory.mkdirs();
                if (entryName.endsWith("/")) {
                    File subdirectory = new File(extractionDir + File.separator + entryName);
                    subdirectory.mkdirs();
                    System.out.println("Extracting Directory: " + entryName);
                } else {
                    byte[] fileContent = new byte[(int) fileSize];
                    dis.readFully(fileContent);
                    FileOutputStream fos = new FileOutputStream(extractionDir + File.separator + entryName);
                    fos.write(fileContent);
                    fos.close();
                    System.out.println("Extracting File: " + entryName);

                    // Validate checksum of extracted files
                    byte[] checksum = new byte[32];
                    dis.readFully(checksum);
                    String storedChecksum = new String(checksum, StandardCharsets.ISO_8859_1);
                    // System.out.println(storedChecksum); //Debug Output
                    String extractedChecksum = calculateMD5(new File(extractionDir + File.separator + entryName));
                    // System.out.println(extractedChecksum); //Debug Output
                    if (extractedChecksum.equals(storedChecksum)) {
                        System.out.println("Checksum matched for file: " + entryName);
                    } else {
                        System.out.println("Checksum mismatch for file: " + entryName);
                    }
                }
            } else if (!entryName.startsWith(entryToExtract) && !entryName.endsWith("/")) {
                int skippedBytes = (int) fileSize + 32;
                dis.skipBytes(skippedBytes);
            }
        }
        dis.close();
        bis.close();
        fis.close();
    }

    private static void listArchiveContents(String archiveFilePath) throws IOException {
        FileInputStream fis = new FileInputStream(archiveFilePath);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream dis = new DataInputStream(bis);

        List<String> entries = new ArrayList<>();
        String currentPath = "";
        while (dis.available() > 0) {
            byte[] lengthBytes = new byte[5];
            dis.readFully(lengthBytes);
            int entryNameLength = Integer.parseInt(new String(lengthBytes, StandardCharsets.ISO_8859_1).trim());
            // System.out.println("Entry Name Lenght: " + entryNameLength);
            byte[] entryNameBytes = new byte[entryNameLength];
            dis.readFully(entryNameBytes);
            String entryName = new String(entryNameBytes, StandardCharsets.ISO_8859_1);
            // System.out.println("Entry Name: " + entryName);
            long fileSize = dis.readLong();
            // System.out.println("File Size: " + fileSize);
            if (!entryName.endsWith("/")) {
                dis.skipBytes((int) fileSize + 32);
            }
            entries.add(entryName);
        }
        dis.close();
        bis.close();
        fis.close();
        // System.out.println(entries.toString());

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Current Directory: " + currentPath);
            System.out.println("Contents:");

            for (String entry : entries) {
                if (entry.startsWith(currentPath)) {
                    String relativePath = entry.substring(currentPath.length());
                    if (!relativePath.isEmpty() && !relativePath.contains("/") || relativePath.endsWith("/")) {
                        if (relativePath.indexOf("/") == relativePath.lastIndexOf("/")) {
                            if (relativePath.endsWith("/")) {
                                System.out.println("Directory: " + relativePath);
                            } else {
                                System.out.println("File: " + relativePath);
                            }
                        }
                    }
                }
            }

            System.out.print("Enter folder name to explore or '..' to go back (type 'stop' to exit): ");
            String userInput = scanner.nextLine();

            if (userInput.equals("stop")) {
                break;
            } else if (userInput.equals("..")) {
                if (!currentPath.isEmpty()) {
                    if (currentPath.endsWith("/")) {
                        currentPath = currentPath.substring(0, currentPath.length() - 1);
                        // System.out.println(currentPath);
                    }
                    int lastSlash = currentPath.lastIndexOf("/");
                    if (lastSlash > 0) {
                        currentPath = currentPath.substring(0, lastSlash + 1);
                        // System.out.println(currentPath);
                    } else {
                        currentPath = "";
                    }
                }
            } else {
                String newPath;
                if (currentPath.isEmpty()) {
                    newPath = userInput + "/";
                } else {
                    newPath = currentPath + userInput + "/";
                }
                // System.out.println("New path: " + newPath);
                boolean directoryExists = false;
                for (String entry : entries) {
                    if (entry.startsWith(newPath)) {
                        directoryExists = true;
                        break;
                    }
                }
                if (directoryExists) {
                    currentPath = newPath;
                    // System.out.println("Current path: " + currentPath);
                } else {
                    System.out.println("Invalid folder name. Please enter a valid folder name.");
                }
            }
        }
        scanner.close();
    }

    private static void deleteEntry(String archiveFilePath, String entryToDelete) throws IOException {
        File tempFile = new File("temp" + archiveFilePath);
        FileInputStream fis = new FileInputStream(archiveFilePath);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream dis = new DataInputStream(bis);

        FileOutputStream fos = new FileOutputStream(tempFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        DataOutputStream dos = new DataOutputStream(bos);

        boolean found = false;

        while (dis.available() > 0) {
            byte[] lengthBytes = new byte[5];
            dis.readFully(lengthBytes);
            int entryNameLength = Integer.parseInt(new String(lengthBytes, StandardCharsets.ISO_8859_1).trim());
            // System.out.println("Entry Name Lenght: " + entryNameLength);
            byte[] entryNameBytes = new byte[entryNameLength];
            dis.readFully(entryNameBytes);
            String entryName = new String(entryNameBytes, StandardCharsets.ISO_8859_1);
            // System.out.println(entryName);
            String lenghString = String.format("%05d", entryName.length());
            // System.out.println(lenghString);
            long fileSize = dis.readLong();
            // System.out.println(fileSize);

            if (!entryName.equals(entryToDelete) && !(entryName.startsWith(entryToDelete)) && entryName.endsWith("/")) {
                dos.writeBytes(lenghString);
                dos.writeBytes(entryName);
                dos.writeLong(fileSize);
            } else if (!entryName.equals(entryToDelete) && !(entryName.startsWith(entryToDelete))
                    && !entryName.endsWith("/")) {
                dos.writeBytes(lenghString);
                dos.writeBytes(entryName);
                dos.writeLong(fileSize);
                byte[] buffer = new byte[(int) fileSize];
                byte[] checksum = new byte[32];
                dis.readFully(buffer);
                dis.readFully(checksum);
                String storedChecksum = new String(checksum, StandardCharsets.ISO_8859_1);
                dos.write(buffer);
                dos.writeBytes(storedChecksum);

            } else {
                found = true;
                if (!entryName.endsWith("/")) {
                    int skippedBytes = (int) fileSize + 32;
                    dis.skipBytes(skippedBytes);
                }
            }
        }

        dos.close();
        bos.close();
        fos.close();
        dis.close();
        bis.close();
        fis.close();

        if (found) {
            File originalFile = new File(archiveFilePath);
            if (originalFile.delete()) {
                if (!tempFile.renameTo(originalFile)) {
                    System.out.println("Failed to rename the temporary archive file.");
                } else {
                    System.out.println("Entry '" + entryToDelete + "' deleted successfully.");
                }
            } else {
                System.out.println("Failed to delete the original archive file.");
            }
        } else {
            tempFile.delete(); // Clean up
            System.out.println("No entry found matching '" + entryToDelete + "'.");
        }
    }

    private static void updateArchiveWithDirectory(File dir, String archiveFilePath)
            throws IOException, NoSuchAlgorithmException {
        FileInputStream fis = new FileInputStream(archiveFilePath);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream dis = new DataInputStream(bis);

        List<String> existingEntries = new ArrayList<>();
        while (dis.available() > 0) {
            byte[] lengthBytes = new byte[5];
            dis.readFully(lengthBytes);
            int entryNameLength = Integer.parseInt(new String(lengthBytes, StandardCharsets.ISO_8859_1).trim());
            // System.out.println("Entry Name Lenght: " + entryNameLength);
            byte[] entryNameBytes = new byte[entryNameLength];
            dis.readFully(entryNameBytes);
            String entryName = new String(entryNameBytes, StandardCharsets.ISO_8859_1);
            long fileSize = dis.readLong();
            existingEntries.add(entryName);
            if (!entryName.endsWith("/")) {
                dis.skipBytes((int) fileSize + 32);
            }
        }
        dis.close();
        bis.close();
        fis.close();

        List<String> sourceEntries = new ArrayList<>();
        sourceEntries = checkSourceEntries(dir, "");
        System.out.println("existing" + existingEntries.toString());
        System.out.println("source" + sourceEntries.toString());
        for (String entry : sourceEntries) {
            if (existingEntries.contains(entry) && !entry.endsWith("/")) {
                deleteEntry(archiveFilePath, entry);
                System.out.println("delete");
            }
        }
        archiveFiles(dir, archiveFilePath, "");
    }

    private static List<String> checkSourceEntries(File dir, String basePath)
            throws IOException, NoSuchAlgorithmException {
        File[] files = dir.listFiles();
        List<String> sourceEntries = new ArrayList<>();
        if (basePath != "") {
            String entryName = basePath;
            System.out.println("base entry" + entryName);
            sourceEntries.add(entryName);
        }
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    checkSourceEntries(file, basePath + file.getName() + "/");
                } else {
                    String entryName = basePath + file.getPath().replace(dir.getPath() + File.separator, "");
                    System.out.println("file entry" + entryName);
                    sourceEntries.add(entryName);
                }
            }
        }
        System.out.println(sourceEntries.toString());
        return sourceEntries;
    }

    private static String calculateMD5(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        FileInputStream fis = new FileInputStream(file);
        byte[] dataBytes = new byte[(int) file.length()];

        int bytesRead;
        while ((bytesRead = fis.read(dataBytes)) != -1) {
            md.update(dataBytes, 0, bytesRead);
        }

        fis.close();
        byte[] mdBytes = md.digest();

        StringBuilder sb = new StringBuilder();
        for (byte mdByte : mdBytes) {
            sb.append(String.format("%02X", mdByte));
        }
        return sb.toString();
    }
}
