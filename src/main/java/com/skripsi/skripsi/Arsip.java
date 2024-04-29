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

class Header {
    String filePath;
    long fileSize;

    public Header(String filePath, long fileSize) {
        this.filePath = filePath;
        this.fileSize = fileSize;
    }

    public String getFilePath() {
        return filePath;
    }

    public long getFileSize() {
        return fileSize;
    }
}

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

        // Archive headers first
        archiveHeaders(dir, archiveFilePath, basePath, dos);

        // Archive content
        archiveContent(dir, archiveFilePath, basePath, dos);

        dos.close();
        bos.close();
        fos.close();
    }

    private static void archiveHeaders(File dir, String archiveFilePath, String basePath, DataOutputStream dos) throws IOException {
        ArrayList<Header> headers = new ArrayList<>();

        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    archiveHeaders(file, archiveFilePath, basePath + file.getName() + "/", dos);
                } else {
                    Header fileHeader = new Header(basePath + file.getName(), file.length());
                    headers.add(fileHeader);
                }
            }

            for (Header header : headers) {
                dos.writeUTF(header.filePath);
                dos.writeLong(header.fileSize);
            }
        }
    }

    private static void archiveContent(File dir, String archiveFilePath, String basePath, DataOutputStream dos) throws IOException {
        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    archiveContent(file, archiveFilePath, basePath + file.getName() + "/", dos);
                } else {
                    FileInputStream fis = new FileInputStream(file);
                    byte[] buffer = new byte[(int) file.length()];
                    fis.read(buffer);
                    dos.write(buffer);
                    fis.close();
                }
            }
        }
    }

    private static void extractFiles(String archiveFilePath, String extractionDir) throws IOException {
        FileInputStream fis = new FileInputStream(archiveFilePath);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream dis = new DataInputStream(bis);

        ArrayList<Header> headers = new ArrayList<>();

        // Read and store all header information
        while (dis.available() > 0) {
            String entryName = dis.readUTF();
            long fileSize = dis.readLong();
            headers.add(new Header(entryName, fileSize));
        }

        // Close the input streams for headers
        dis.close();
        bis.close();
        fis.close();

        // Re-open the input streams for content extraction
        fis = new FileInputStream(archiveFilePath);
        bis = new BufferedInputStream(fis);
        dis = new DataInputStream(bis);

        // Skip the headers by reading them without processing
        for (Header header : headers) {
            dis.readUTF();
            dis.readLong();
        }

        // Extract file contents using stored headers
        for (Header header : headers) {
            byte[] fileContent = new byte[(int) header.fileSize];
            dis.readFully(fileContent);

            // Write the file content to the extracted directory
            FileOutputStream fos = new FileOutputStream(extractionDir + File.separator + header.filePath);
            fos.write(fileContent);
            fos.close();
            System.out.println("Extracting File: " + header.filePath);
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

            // System.out.println("Folder Names: " + folderNames); //debug
            System.out.print("Enter folder name to explore or '..' to go back (type 'stop' to exit): ");
            userInput = scanner.nextLine();

            if (userInput.equals("stop")) {
                break;
            } else if (userInput.equals("..")) {
                if (currentPath.size() > 1) {
                    currentPath.pop();
                    // System.out.println("Current Path: " + currentPath.peek());
                }
            } else {
                String newPath = currentPath.peek() + userInput + "/";
                if (folderNames.contains(newPath)) {
                    currentPath.push(newPath);
                    // System.out.println("Current Path: " + currentPath.peek());
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
}

