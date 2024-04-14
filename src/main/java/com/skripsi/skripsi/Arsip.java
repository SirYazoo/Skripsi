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
        System.out.println("3. List archive contents"); // Added option
        System.out.print("Enter your choice (1, 2, or 3): ");
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
        } else if (choice == 3) { // Added condition
            System.out.print("Enter the path to the archive file: ");
            String archiveFilePathList = reader.readLine();

            listArchiveContents(archiveFilePathList); // Added method call
        } else {
            System.out.println("Invalid choice.");
            reader.close();
        }
    }

    private static void archiveFiles(File dir, String archiveFilePath, String basePath) throws IOException {
        FileOutputStream fos = new FileOutputStream(archiveFilePath, true);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        DataOutputStream dos = new DataOutputStream(bos);

        // Write the directory entry
        dos.writeUTF(basePath);
        dos.writeLong(0); // Indicate it as a directory with file size 0
        System.out.println("Archiving Directory: " + basePath);

        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // Recursively archive subdirectories
                    archiveFiles(file, archiveFilePath, basePath + file.getName() + "/");
                    System.out.println("Archiving Directory: " + basePath + file.getName() + "/");
                } else {
                    String entryName = basePath + file.getPath().replace(dir.getPath() + File.separator, "");
                    dos.writeUTF(entryName); // Write the file entry
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
                // Create the directory if it is not the root folder entry
                File directory = new File(extractionDir + File.separator + entryName);
                directory.mkdirs();
                System.out.println("Extracting Directory: " + entryName);
            } else if (!entryName.isEmpty()) {
                byte[] fileContent = new byte[(int) fileSize];
                dis.readFully(fileContent);

                // Write the file content to the extracted directory
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

    private static void listArchiveContents(String archiveFilePath) throws IOException {
        FileInputStream fis = new FileInputStream(archiveFilePath);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream dis = new DataInputStream(bis);

        Stack<String> currentPath = new Stack<>();
        currentPath.push(""); // Start at the root directory

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
                                folderNames.add(relativePath);
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
                    currentPath.pop(); // Go back to the previous directory
                }
            } else {
                String newPath = currentPath.peek() + userInput + "/";
                if (folderNames.contains(newPath)) {
                    currentPath.push(newPath);
                } else {
                    System.out.println("Invalid folder name. Please enter a valid folder name.");
                }
            }

            // Reset the DataInputStream to start from the beginning for the next iteration
            fis.getChannel().position(0);
            bis = new BufferedInputStream(fis);
            dis = new DataInputStream(bis);
        }

        dis.close();
        bis.close();
        fis.close();
        scanner.close();
    }
}
