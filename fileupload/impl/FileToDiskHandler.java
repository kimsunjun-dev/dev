package com.hr.common.util.fileupload.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.anyframe.util.DateUtil;
import org.apache.commons.fileupload.util.Streams;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.hr.common.util.StringUtil;
import com.hr.common.util.fileupload.jfileupload.web.JFileUploadService;

public class FileToDiskHandler extends AbsFileHandler {

	private String targetPath = null;
	private String workDir = null;
	private ArrayList<String> files = null;

	public FileToDiskHandler(HttpServletRequest request, HttpServletResponse response, FileUploadConfig config) {
		super(request, response, config);
	}

	@Override
	protected void init() throws Exception {
		
		String realPath = (this.config.getDiskPath().length()==0 ) ? request.getSession().getServletContext().getRealPath("/") : this.config.getDiskPath();
		String strWork  = request.getParameter("work");
		
		if(!"".equals(strWork) && strWork != null) {
			realPath = StringUtil.replaceAll(realPath + strWork + DateUtil.getCurrentDay("yyyyMM") +"/", "//", "/");
		}else {
			realPath = StringUtil.replaceAll(realPath + "/hrfile" + "/" + this.enterCd , "//", "/");
			String tmp = this.config.getProperty(FileUploadConfig.POSTFIX_STORE_PATH);
			tmp = tmp == null ? "" : tmp;

			Pattern p = Pattern.compile("@([\\w]*)@");
			Matcher m = p.matcher(tmp);

			while (m.find()) {
				if (m.groupCount() > 0) {
					String replaceKey = m.group(0);
					String patternKey = m.group(1);

					try {
						tmp = tmp.replace(replaceKey, DateUtil.getCurrentDay(patternKey));
					} catch (Exception e) {
					}
				}
			}
			realPath += tmp;
		}
		this.workDir = StringUtil.replaceAll(realPath, "//", "/");
	}

	@Override
	public void fileupload(InputStream inStrm, String fileNm, Map tsys972Map, int cnt) throws Exception {
		if(this.targetPath == null) {
			init();
		}
		
		if(tsys972Map != null) {
			tsys972Map.put("filePath",StringUtil.replaceAll(this.workDir + "/", "//", "/"));
		}
		
		try {
			String realFileName = null;
			int fileSize = inStrm.available();
			
			String nmPattern = config.getProperty(FileUploadConfig.POSTFIX_NAME_PATTERN);
			if(nmPattern.startsWith("default")) {
				realFileName = fileNm.substring(0, fileNm.lastIndexOf("."));
			} else if(nmPattern.startsWith("random")) {
				realFileName = getTimeStemp()+cnt;
			} else if(nmPattern.startsWith("sabun")) {
				realFileName = request.getParameter("sabun");
				if(realFileName == null || "".equals(realFileName)) {
					realFileName = (String) session.getAttribute("ssnSabun");
				}
			}
			
			if(nmPattern.endsWith("Ext")) {
				realFileName += fileNm.substring(fileNm.lastIndexOf("."));
			}
			
			String path = "";
			if(tsys972Map != null) {
				if(tsys972Map.get("filePath") != null && !"".equals(tsys972Map.get("filePath").toString())) {
					path = StringUtil.replaceAll(tsys972Map.get("filePath").toString() + "/" + realFileName, "//", "/");
				}else {
					path = StringUtil.replaceAll(this.workDir + "/" + realFileName, "//", "/");
				}
			}else {
				path = StringUtil.replaceAll(this.workDir + "/" + realFileName, "//", "/");
			}
			
			File toDir = new File(path);
			File upDir = toDir.getParentFile();

			if (!upDir.exists()) {
				upDir.mkdirs();// 폴더경로 없으면 만들어 놓기.
			}

			FileOutputStream fo = new FileOutputStream(toDir);

			Streams.copy(inStrm, fo, true);
			
			tsys972Map.put("ssnEnterCd", this.enterCd);
			tsys972Map.put("seqNo", cnt);
			tsys972Map.put("sFileNm", realFileName);
			tsys972Map.put("rFileNm", fileNm);
			tsys972Map.put("fileSize", fileSize);
			tsys972Map.put("ssnSabun", session.getAttribute("ssnSabun"));
			tsys972Map.put("filePath", path);
			
			if(this.files == null) {
				this.files = new ArrayList<String>();
			}
			
			this.files.add(realFileName);
		} catch (Exception e) {
			e.printStackTrace();
			
			if(this.files != null && this.files.size() > 0) {
				for(String fileName : this.files) {
					String path = StringUtil.replaceAll(this.targetPath + "/" + this.workDir + "/" + fileName, "//", "/");
					File file = new File(path);
					if(file.isFile()) {
						file.delete();
					}
				}
			}
			
			throw e;
		} finally {
		}
	}

	@Override
	protected InputStream filedownload(Map<?, ?> paramMap) throws Exception {
		if(paramMap == null) {
			return null;
		}
		
		if(this.targetPath == null) {
			init();
		}
		String filePath = String.valueOf(paramMap.get("filePath"));
		String path = StringUtil.replaceAll(filePath, "//", "/");
		
		//File file = new File(path + sname);
		File file = new File(path);
		if (!file.exists()) {
			throw new Exception("<script>alert('The file does not exist.');</script>");
		}

		return new FileInputStream(file);
	}

	@Override
	protected void filedelete(List<Map<?, ?>> deleteList) throws Exception {
		WebApplicationContext webAppCtxt = WebApplicationContextUtils.getWebApplicationContext(session.getServletContext());
		JFileUploadService jFileUploadService = (JFileUploadService) webAppCtxt.getBean("JFileUploadService");

		if (!jFileUploadService.fileStoreDelete(deleteList)) {
			throw new Exception("Error!");
		}

		if (deleteList != null && deleteList.size() > 0) {
			Iterator<Map<?, ?>> it = deleteList.iterator();

			while (it.hasNext()) {
				Map<?, ?> map = it.next();

				String filePath = String.valueOf(map.get("filePath"));
				String path = StringUtil.replaceAll(filePath, "//", "/");
				File file = new File(path);
				if (file.isFile()) {
					file.delete();
				}
			}
		}
	}
}
