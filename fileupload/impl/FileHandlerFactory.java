package com.hr.common.util.fileupload.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FileHandlerFactory {

	public static IFileHandler getFileHandler(HttpServletRequest request, HttpServletResponse response) {
		String uploadType = request.getParameter("uploadType");
		if(uploadType == null || "".equals(uploadType)) {
			uploadType = String.valueOf(request.getAttribute("uploadType"));
		}
		
		return FileHandlerFactory.getFileHandler(uploadType, request, response);
	}
	
	public static IFileHandler getFileHandler(String uploadType, HttpServletRequest request, HttpServletResponse response) {
		IFileHandler fileHandler = null;
		
		FileUploadConfig config = new FileUploadConfig(uploadType);
		String storeType = config.getProperty(FileUploadConfig.POSTFIX_STORE_TYPE);

		if("FILE".equals(storeType)) {
			fileHandler = new FileToDiskHandler(request, response, config);
		}
		
		return fileHandler;
	}
}
