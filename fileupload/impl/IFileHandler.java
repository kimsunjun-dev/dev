package com.hr.common.util.fileupload.impl;

import java.io.InputStream;
import java.util.Map;

import org.json.JSONArray;

public interface IFileHandler {

	JSONArray upload() throws Exception;
	void fileupload(InputStream inStrm, String fileNm, Map tsys972Map,  int cnt) throws Exception;
	
	void download() throws Exception;
	void download(boolean isDirectView) throws Exception;
	void download(boolean isDirectView, String[] fileSeqArr, String[] seqNoArr) throws Exception;
	
	void delete() throws Exception;
	
}
