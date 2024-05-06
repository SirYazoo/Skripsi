/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
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
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

/**
 *
 * @author rioau
 */
public class Arsip {

    public static void main(String[] args) throws IOException {
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

            archiveFilesToExistingArchive(new File(sourceDir), archiveFilePath, "");
            System.out.println("Files archived successfully.");
        } else {
            System.out.println("Invalid choice.");
            reader.close();
        }
    }

    private static void archiveFiles(File dir, String archiveFilePath, String basePath) throws IOException {
        FileOutputStream fos = new FileOutputStream(archiveFilePath, true);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        DataOutputStream dos = new DataOutputStream(bos);

        if (basePath != "") {
            dos.writeUTF(basePath);
            dos.writeLong(0);
            System.out.println("Created Directory: " + basePath);
        }

        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    System.out.println("Archiving Directory: " + basePath + file.getName() + "/");
                    archiveFiles(file, archiveFilePath, basePath + file.getName() + "/");
                } else {
                    String entryName = basePath + file.getPath().replace(dir.getPath() + File.separator, "");
                    dos.writeUTF(entryName);
                    dos.writeLong(file.length());
                    System.out.println("Archiving File: " + entryName);

                    FileInputStream fis = new FileInputStream(file);
                    byte[] buffer = new byte[(int) file.length()];
                    fis.read(buffer);
                    dos.write(buffer);
                    fis.close();

                }
            }
        }
        dos.close();
        bos.close();
        fos.close();
    }

    private static void archiveFilesToExistingArchive(File dir, String archiveFilePath, String basePath)
            throws IOException {
        List<File> filesToAdd = new ArrayList<>();
        checkExistingArchive(dir, archiveFilePath, basePath, filesToAdd);

        for (File fileToAdd : filesToAdd) {
            String entryName = basePath + fileToAdd.getPath().replace(dir.getPath() + File.separator, "");
            replaceFileInArchive(archiveFilePath, entryName, fileToAdd);
        }
    }

    private static void checkExistingArchive(File dir, String archiveFilePath, String basePath, List<File> filesToAdd)
            throws IOException {
        FileOutputStream fos = new FileOutputStream(archiveFilePath, true);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        DataOutputStream dos = new DataOutputStream(bos);

        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    String directoryName = basePath + file.getName() + "/";
                    if (entryExistsInArchive(archiveFilePath, directoryName)) {
                    System.out.println("Directory already exists in the archive: " + directoryName);
                    }
                    System.out.println("Checking Directory: " + basePath + file.getName() + "/");
                    checkExistingArchive(file, archiveFilePath, basePath + file.getName() + "/", filesToAdd);                                                                                                 // sini
                } else {
                    String entryName = basePath + file.getPath().replace(dir.getPath() + File.separator, "");

                    if (entryExistsInArchive(archiveFilePath, entryName)) {
                        System.out.println("File already exists in the archive: " + entryName);
                        filesToAdd.add(file);
                    } else {
                        dos.writeUTF(entryName);
                        dos.writeLong(file.length());
                        System.out.println("Archiving File: " + entryName);
                        FileInputStream fis = new FileInputStream(file);
                        byte[] buffer = new byte[(int) file.length()];
                        fis.read(buffer);
                        dos.write(buffer);
                        fis.close();
                    }
                }
            }
        }
        dos.close();
        bos.close();
        fos.close();
    }

    private static boolean entryExistsInArchive(String archiveFilePath, String entryName) throws IOException {
        FileInputStream fis = new FileInputStream(archiveFilePath);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream dis = new DataInputStream(bis);

        while (dis.available() > 0) {
            String existingEntry = dis.readUTF();
            dis.readLong();
            dis.skipBytes((int) dis.readLong());

            if (existingEntry.equals(entryName) || (existingEntry.startsWith(entryName))) {
                dis.close();
                bis.close();
                fis.close();
                return true;
            }
        }

        dis.close();
        bis.close();
        fis.close();
        return false;
    }

    private static void replaceFileInArchive(String archiveFilePath, String entryName, File newFile)
            throws IOException {
        File tempFile = new File("temp" + archiveFilePath);

        FileInputStream fis = new FileInputStream(archiveFilePath);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream dis = new DataInputStream(bis);

        FileOutputStream fos = new FileOutputStream(tempFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        DataOutputStream dos = new DataOutputStream(bos);

        while (dis.available() > 0) {
            String existingEntry = dis.readUTF();
            long fileSize = dis.readLong();
            byte[] buffer = new byte[(int) fileSize];
            dis.readFully(buffer);

            if (!existingEntry.equals(entryName)) {
                dos.writeUTF(existingEntry);
                dos.writeLong(fileSize);
                dos.write(buffer);
            }
        }

        // Add the new file to the archive
        dos.writeUTF(entryName);
        dos.writeLong(newFile.length());
        FileInputStream newFis = new FileInputStream(newFile);
        byte[] newBuffer = new byte[(int) newFile.length()];
        newFis.read(newBuffer);
        dos.write(newBuffer);

        newFis.close();
        dos.close();
        bos.close();
        fos.close();
        dis.close();
        bis.close();
        fis.close();

        // Replace the original archive file with the temporary file
        File originalFile = new File(archiveFilePath);
        if (originalFile.delete()) {
            if (!tempFile.renameTo(originalFile)) {
                System.out.println("Failed to rename the temporary archive file.");
            } else {
                System.out.println("File '" + entryName + "' replaced successfully.");
            }
        } else {
            System.out.println("Failed to delete the original archive file.");
        }
    }

    private static void extractFiles(String archiveFilePath, String extractionDir) throws IOException {
        FileInputStream fis = new FileInputStream(archiveFilePath);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream dis = new DataInputStream(bis);

        while (dis.available() > 0) {
            String entryName = dis.readUTF();
            long fileSize = dis.readLong();
            System.out.println("Extracting: " + entryName);
            System.out.println("Size: " + fileSize);

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
            }
        }
        dis.close();
        bis.close();
        fis.close();
    }

    private static void extractSpecificFile(String archiveFilePath, String extractionDir, String entryToExtract)
            throws IOException {
        FileInputStream fis = new FileInputStream(archiveFilePath);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream dis = new DataInputStream(bis);

        while (dis.available() > 0) {
            String entryName = dis.readUTF();
            long fileSize = dis.readLong();

            if (entryName.equals(entryToExtract)) {
                String parentDir = new File(entryToExtract).getParent();
                // System.out.println(parentDir);
                if (parentDir != null) {
                    File parentDirectory = new File(extractionDir + File.separator + parentDir);
                    parentDirectory.mkdirs();
                    // System.out.println(parentDirectory);
                    byte[] fileContent = new byte[(int) fileSize];
                    dis.readFully(fileContent);
                    FileOutputStream fos = new FileOutputStream(extractionDir + File.separator + entryName);
                    fos.write(fileContent);
                    fos.close();
                    System.out.println("Extracting File: " + entryName);
                }
                break;
            } else {
                dis.skipBytes((int) fileSize);
            }
        }

        dis.close();
        bis.close();
        fis.close();
    }

    private static void extractSpecificFolder(String archiveFilePath, String extractionDir, String entryToExtract)
            throws IOException {
        FileInputStream fis = new FileInputStream(archiveFilePath);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream dis = new DataInputStream(bis);

        while (dis.available() > 0) {
            String entryName = dis.readUTF();
            long fileSize = dis.readLong();

            if (entryName.startsWith(entryToExtract)) {
                File directory = new File(extractionDir + File.separator + entryToExtract);
                System.out.println(extractionDir + File.separator + entryToExtract);
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
                }
            } else {
                dis.skipBytes((int) fileSize);
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

        Stack<String> currentPath = new Stack<>();
        currentPath.push(""); // root

        Scanner scanner = new Scanner(System.in);
        String userInput;

        ArrayList<String> folderNames = new ArrayList<>();
        while (true) {
            System.out.println("Current Directory: " + currentPath.peek());
            System.out.println("Contents:");

            while (dis.available() > 0) {
                String entryName = dis.readUTF();
                long fileSize = dis.readLong();
                dis.skipBytes((int) fileSize);

                if (entryName.startsWith(currentPath.peek()) && entryName.length() > currentPath.peek().length()) {
                    String relativePath = entryName.substring(currentPath.peek().length());
                    if (relativePath.indexOf("/") == -1 || relativePath.endsWith("/")) {
                        if (!relativePath.isEmpty()) {
                            if (relativePath.endsWith("/")) {
                                if (!folderNames.contains(relativePath)) {
                                    folderNames.add(relativePath);
                                }
                                System.out.println("Directory: " + relativePath);
                            } else {
                                System.out.println("File: " + relativePath + " (Size: " + fileSize + " bytes)");
                            }
                        }
                    }
                }
            }

            System.out.print("Enter folder name to explore or '..' to go back (type 'stop' to exit): ");
            userInput = scanner.nextLine();

            if (userInput.equals("stop")) {
                break;
            } else if (userInput.equals("..")) {
                if (currentPath.size() > 1) {
                    currentPath.pop();
                }
            } else {
                String newPath = currentPath.peek() + userInput + "/";
                if (folderNames.contains(newPath)) {
                    currentPath.push(newPath);
                } else {
                    System.out.println("Invalid folder name. Please enter a valid folder name.");
                }
            }

            fis.getChannel().position(0);
            bis = new BufferedInputStream(fis);
            dis = new DataInputStream(bis);
        }

        dis.close();
        bis.close();
        fis.close();
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
            String entryName = dis.readUTF();
            long fileSize = dis.readLong();
            byte[] buffer = new byte[(int) fileSize];
            dis.readFully(buffer);

            if (!entryName.equals(entryToDelete) && !(entryName.startsWith(entryToDelete))) {
                dos.writeUTF(entryName);
                dos.writeLong(fileSize);
                dos.write(buffer);
            } else {
                found = true;
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
}