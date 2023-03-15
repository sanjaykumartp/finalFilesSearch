package com.assessment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

public class FilesSearchController {

	public static String searchDirectory(File directory, String searchString, ExcelWriter excelWriter) throws IOException {
		StringBuilder resultBuilder = new StringBuilder();
		boolean searchStringFound = false; // added variable to check if search string is found in any file

		// check if directory exists
		if (!directory.exists()) {
			resultBuilder.append("Invalid directory path: " + directory.getPath());
			throw new IllegalArgumentException(resultBuilder.toString());
		}

		// check if directory is empty
		File[] files = directory.listFiles();
		if (files == null || files.length == 0) {
			resultBuilder.append("No text, doc, docx or pdf files found in : " + directory.getPath());
			throw new IllegalArgumentException(resultBuilder.toString());
		}

		// check if search string is empty
		if (searchString.trim().isEmpty()) {
			resultBuilder.append("Please enter a search string.");
			throw new IllegalArgumentException(resultBuilder.toString());
		}

		for (File file : files) {
			if (file.isDirectory()) {
				resultBuilder.append(searchDirectory(file, searchString, excelWriter));
			} else {
				boolean foundInFile = searchFile(file, searchString, excelWriter); // added variable to check if search string is found in current file
				if (foundInFile) {
					searchStringFound = true;
				}


			}

		}
		if (!searchStringFound) { // throw exception if search string is not found in any file
			resultBuilder.append(searchString + " this keyword is not present in any file");
			throw new IllegalArgumentException(resultBuilder.toString());
		}
		resultBuilder.append("Search Completed ");
		return resultBuilder.toString();
	}


	private static boolean searchFile(File file, String searchString, ExcelWriter excelWriter) throws IOException {
		String fileExtension = FilenameUtils.getExtension(file.getName());
		boolean fileContainsKeywords = false;
		switch (fileExtension) {
		case "txt":
		    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
		        StringBuilder fileContent = new StringBuilder(); // store the file content
		        String firstLine = br.readLine(); // read the first line of the file
		        String name = ""; // initialize the name variable
		        if (firstLine != null) {
		            String[] words = firstLine.trim().split("\\s+"); // split the first line by spaces
		            if (words.length > 0) {
		                name = words[0]; // set the name to the first word of the first line
		            }
		        }
		        while ((firstLine = br.readLine()) != null) {
		            fileContent.append(firstLine).append("\n");
		        }
		        String[] keywordsAnd = searchString.split("\\+"); // split search string by ++
		        String[] keywordsOr = searchString.split("\\|\\|"); // split search string by ||
		        boolean containsAllKeywords = true;
		        for (String keyword : keywordsAnd) {
		            keyword = keyword.trim().toLowerCase(); // trim and convert to lowercase
		            if (!fileContent.toString().toLowerCase().contains(keyword)) {
		                containsAllKeywords = false;
		                break;
		            }
		        }
		        if (containsAllKeywords) {
		            SearchResult result = new SearchResult(file.getName(), "", "", "", searchString);
		            result.setName(name); // set the name in the search result

		            // Extract emails and mobile numbers from the file content
		            Pattern emailPattern = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");
		            Pattern mobilePattern = Pattern.compile("\\b\\d{10}\\b");

		            Matcher emailMatcher = emailPattern.matcher(fileContent.toString());
		            while (emailMatcher.find()) {
		                result.setEmail(emailMatcher.group());
		            }

		            Matcher mobileMatcher = mobilePattern.matcher(fileContent.toString());
		            while (mobileMatcher.find()) {
		                result.setMobileNumber(mobileMatcher.group());
		            }

		            result.setFileName(file.getName());
		            result.setSearch_criteria(searchString);
		            excelWriter.addResult(result);
		            fileContainsKeywords = true;
		        } else {
		            boolean containsAnyKeyword = false;
		            for (String keyword : keywordsOr) {
		                keyword = keyword.trim().toLowerCase(); // trim and convert to lowercase
		                if (fileContent.toString().toLowerCase().contains(keyword)) {
		                    containsAnyKeyword = true;
		                    break;
		                }
		            }
		            if (containsAnyKeyword) {
		                SearchResult result = new SearchResult(file.getName(), "", "", "", searchString);
		                result.setName(name); // set the name in the search result

		                // Extract emails and mobile numbers from the file content
		                Pattern emailPattern = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");
		                Pattern mobilePattern = Pattern.compile("\\b\\d{10}\\b");

		                Matcher emailMatcher = emailPattern.matcher(fileContent.toString());
		                while (emailMatcher.find()) {
		                    result.setEmail(emailMatcher.group());
		                }

		                Matcher mobileMatcher = mobilePattern.matcher(fileContent.toString());
		                while (mobileMatcher.find()) {
		                    result.setMobileNumber(mobileMatcher.group());
		                }

		                result.setFileName(file.getName());
		                result.setSearch_criteria(searchString);
		                excelWriter.addResult(result);
		                fileContainsKeywords = true;
		            }
		        }
		    }
		    break;

		case "doc":
		case "docx":
			try (FileInputStream fis = new FileInputStream(file);
					XWPFDocument document = new XWPFDocument(fis)) {
				XWPFWordExtractor extractor = new XWPFWordExtractor(document);
				String text = extractor.getText();
				boolean containsAllKeywords = true;
				String[] keywordsAnd = searchString.split("\\+"); // split search string by ++
				String[] keywordsOr = searchString.split("\\|\\|"); // split search string by ||
				for (String keyword : keywordsAnd) {
					keyword = keyword.trim().toLowerCase(); // trim and convert to lowercase
					if (!text.toLowerCase().contains(keyword)) {
						containsAllKeywords = false;
						break;
					}
				}
				if (containsAllKeywords) {
					SearchResult result = new SearchResult(file.getName(), "", "", "", searchString);
					result.setFileName(file.getName());
					result.setSearch_criteria(searchString);
					extractNameEmailMobile(text, result);
					excelWriter.addResult(result);
					fileContainsKeywords = true;
				} else {
					boolean containsAnyKeyword = false;
					for (String keyword : keywordsOr) {
						keyword = keyword.trim().toLowerCase(); // trim and convert to lowercase
						if (text.toLowerCase().contains(keyword)) {
							containsAnyKeyword = true;
							break;
						}
					}
					if (containsAnyKeyword) {
						SearchResult result = new SearchResult(file.getName(), "", "", "", searchString);
						result.setFileName(file.getName());
						result.setSearch_criteria(searchString);
						extractNameEmailMobile(text, result);
						excelWriter.addResult(result);
						fileContainsKeywords = true;
					}
				}
			}
			break;
		case "pdf":
			try (PDDocument document = PDDocument.load(file)) {
				PDFTextStripper stripper = new PDFTextStripper();
				String text = stripper.getText(document);
				boolean containsAllKeywords = true;
				String[] keywordsAnd = searchString.split("\\+"); // split search string by ++
				String[] keywordsOr = searchString.split("\\|\\|"); // split search string by ||
				for (String keyword : keywordsAnd) {
					keyword = keyword.trim().toLowerCase(); // trim and convert to lowercase
					if (!text.toLowerCase().contains(keyword)) {
						containsAllKeywords = false;
						break;
					}
				}
				if (containsAllKeywords) {
					SearchResult result = new SearchResult(file.getName(), "", "", "", searchString);
					result.setFileName(file.getName());
					result.setSearch_criteria(searchString);
					extractNameEmailMobile(text, result);
					excelWriter.addResult(result);
					fileContainsKeywords = true;
				} else {
					boolean containsAnyKeyword = false;
					for (String keyword : keywordsOr) {
						keyword = keyword.trim().toLowerCase(); // trim and convert to lowercase
						if (text.toLowerCase().contains(keyword)) {
							containsAnyKeyword = true;
							break;
						}
					}
					if (containsAnyKeyword) {
						SearchResult result = new SearchResult(file.getName(), "", "", "", searchString);
						result.setFileName(file.getName());
						result.setSearch_criteria(searchString);
						extractNameEmailMobile(text, result);
						excelWriter.addResult(result);
						fileContainsKeywords = true;
					}
				}
			}
			break;
		default:
			System.out.println("Unsupported file type: " + fileExtension);
			break;
		}
		return fileContainsKeywords;
	}


	private static void extractNameEmailMobile(String text, SearchResult result) {
		String[] lines = text.split("\\r?\\n");//split using regular expression
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			if (line.toLowerCase().contains("@") && line.toLowerCase().contains(".com")) {
				String[] parts = line.split("\\s+");
				for (String part : parts) {
					if (part.contains("@") && part.contains(".com")) {
						String email = part.replaceAll("[^a-zA-Z0-9@.]+", "");
						if (email.toLowerCase().startsWith("email:-")) {
							email = email.substring(7);
						}
						result.setEmail(email);
					}
				}
			}else {
				Pattern pattern = Pattern.compile("(\\d[\\s-]?){10}");
				Matcher matcher = pattern.matcher(line);
				while (matcher.find()) {
					String mobileNumber = matcher.group(0).replaceAll("\\s|-", "");
					result.setMobileNumber(mobileNumber);
				}
			}
			if (i == 0) {
				String firstLine = line.trim();
				if (firstLine.matches("(?i)^\\w+\\s*Name\\s*:(.*)$")) {
					String[] parts = firstLine.split(":\\s*")[1].trim().split("\\s+");
					StringBuilder fullName = new StringBuilder();
					for (String part : parts) {
						if (part.matches("[a-zA-Z]+")) {
							fullName.append(part).append(" ");
						}
					}
					result.setName(fullName.toString().trim());
				} else {
					String[] parts = firstLine.split("\\s+");
					StringBuilder fullName = new StringBuilder();
					for (String part : parts) {
						if (part.matches("[a-zA-Z]+")) {
							fullName.append(part).append(" ");
						}
					}
					result.setName(fullName.toString().trim());
				}
			}
		}
	}
}