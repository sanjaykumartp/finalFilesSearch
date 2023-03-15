package com.assessment;

public class SearchResult {
	private String fileName;
	private String name;
	private String email;
	private String mobileNumber;
	private String Search_criteria;
	public SearchResult(String fileName, String name, String email, String mobileNumber, String search_criteria) {
		super();
		this.fileName = fileName;
		this.name = name;
		this.email = email;
		this.mobileNumber = mobileNumber;
		Search_criteria = search_criteria;
	}
	public SearchResult() {
		super();
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getMobileNumber() {
		return mobileNumber;
	}
	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
	public String getSearch_criteria() {
		return Search_criteria;
	}
	public void setSearch_criteria(String search_criteria) {
		Search_criteria = search_criteria;
	}
	

	
}

