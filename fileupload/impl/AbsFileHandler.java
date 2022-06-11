package com.hr.common.util.fileupload.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.tika.Tika;
import org.apache.tika.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.hr.common.exception.FileUploadException;
import com.hr.common.util.StringUtil;
import com.hr.common.util.fileupload.jfileupload.web.JFileUploadService;

import net.sf.jazzlib.CRC32;
import net.sf.jazzlib.ZipEntry;
import net.sf.jazzlib.ZipOutputStream;

public abstract class AbsFileHandler implements IFileHandler {

	private final int DISK_THRESHOLD_SIZE = 1024 * 1024 * 3; // 3MB
	private Object lockObj = new Object();
	protected HttpSession session = null;
	protected HttpServletRequest request = null;
	protected HttpServletResponse response = null;
	protected FileUploadConfig config = null;
	protected String enterCd = null;
	
	public AbsFileHandler(HttpServletRequest request, HttpServletResponse response, FileUploadConfig config) {
		this.request = request;
		this.session = request.getSession();
		this.response = response;
		this.config = config;
		this.enterCd = request.getParameter("enterCd");
		
		if(this.enterCd == null || "".equals(this.enterCd)) {
			this.enterCd = (String) session.getAttribute("ssnEnterCd");
		}
	}

	protected abstract void init() throws Exception;
	
	public abstract void fileupload(InputStream inStrm, String fileNm, Map tsys972Map, int cnt) throws Exception;

	protected String getTimeStemp() {
		return System.currentTimeMillis()+"";
	}
	
	public JSONArray upload() throws Exception {
		synchronized (lockObj) {
			if (ServletFileUpload.isMultipartContent(request)) {
				init();

				DiskFileItemFactory factory = new DiskFileItemFactory();
				factory.setSizeThreshold(DISK_THRESHOLD_SIZE);
				
				File uploadDir = new File(config.getTempDir());
				if (!uploadDir.exists()) {
					uploadDir.mkdirs();
				}
				
				factory.setRepository(uploadDir);

				ServletFileUpload sUpload = new ServletFileUpload(factory);
				sUpload.setFileSizeMax(Long.valueOf(config.getProperty(FileUploadConfig.POSTFIX_FILE_SIZE)));

				List<FileItem> fList = sUpload.parseRequest(request);
				File toDir = null;
				FileOutputStream fo = null;
				
				try {
					JSONArray jsonArray = new JSONArray();
					

					WebApplicationContext webAppCtxt = WebApplicationContextUtils.getWebApplicationContext(session.getServletContext());
					JFileUploadService jFileUploadService = (JFileUploadService) webAppCtxt.getBean("JFileUploadService");
					Iterator<FileItem> fIt = fList.iterator();

					if (fIt != null) {
						int curFileCnt = fList.size();
						String fileCnt = config.getProperty(FileUploadConfig.POSTFIX_FILE_COUNT);
						int totFileCnt = Integer.valueOf(fileCnt != null && !"".equals(fileCnt) ? fileCnt : "0");
						String fileSeq = request.getParameter("fileSeq");

						int realCnt = 0;
						Map<String, Object> tsys972Map = new HashMap<String, Object>();
						boolean isMaster = false;
						String strFilePath = "";
						if (fileSeq != null && !"".equals(fileSeq)) {
							Map<String, Object> paramMap = new HashMap<String, Object>();
							paramMap.put("ssnEnterCd", this.enterCd);
							paramMap.put("fileSeq", fileSeq);
							Map<?, ?> map = jFileUploadService.jFileCount(paramMap);
							
							if (map != null) {
								isMaster = true;
								String cnt = String.valueOf(map.get("cnt"));
								realCnt = Integer.valueOf(cnt != null && !"".equals(cnt) ? cnt : "1");

								if (totFileCnt > 0 && curFileCnt + realCnt > totFileCnt) {
									throw new FileUploadException("File Count Error!");
								}
								String mcnt = String.valueOf(map.get("mcnt"));
								realCnt = Integer.valueOf(mcnt != null && !"".equals(mcnt) ? mcnt : "1");
								realCnt = realCnt == 0 ? 1 : realCnt;
								//strFilePath = String.valueOf(map.get("filePath"));
							}
						} else {
							fileSeq = jFileUploadService.jFileSequence();
						}

						if (!isMaster) {
							realCnt = 1;
							tsys972Map.put("ssnEnterCd", session.getAttribute("ssnEnterCd"));
							tsys972Map.put("ssnSabun", session.getAttribute("ssnSabun"));
						}

						List<Map<?, ?>> tsys972List = new ArrayList<Map<?, ?>>();
 
						while (fIt.hasNext()) {
							Map<String,Object> tsys972Temp = new HashMap();
							tsys972Temp.put("ssnEnterCd", tsys972Map.get("ssnEnterCd"));
							tsys972Temp.put("ssnSabun", tsys972Map.get("ssnSabun"));
							
							FileItem fItem = fIt.next();

							if (fItem.isFormField()) {
								
							} else {
								String itemName = FilenameUtils.getName(fItem.getName());
								boolean isVaild = true;
								String vaildMsg = null;
								
								String extExtension = config.getProperty(FileUploadConfig.POSTFIX_EXT_EXTENSION);
								if (extExtension != null && !"".equals(extExtension)) {
									String[] arr = itemName.split("\\.");
									
									if (arr.length == 1) {
										isVaild = false;
										vaildMsg = "File Type Error! " + itemName;
									} else {
										String ext = arr[arr.length - 1];
										Pattern p = Pattern.compile(extExtension.replaceAll(",", "|"), Pattern.CASE_INSENSITIVE);
										Matcher m = p.matcher(ext);
										if (!m.matches()) {
											isVaild = false;
											vaildMsg = "File Type Error! " + itemName;
										}
									}
								}
								
								if(isVaild) {
									String mimeExtension = config.getProperty(FileUploadConfig.POSTFIX_MIME_EXTENSION);
									if (mimeExtension != null && !"".equals(mimeExtension)) {
										mimeExtension = mimeExtension.replaceAll("\\*", ".*");

										String path = StringUtil.replaceAll(config.getTempDir() + "/" + itemName, "//", "/");
										toDir = new File(path);
										
										File upDir = toDir.getParentFile();

										if (!upDir.exists()) {
											upDir.mkdirs();// 폴더경로 없으면 만들어 놓기.
										}

										fo = new FileOutputStream(toDir);

										Streams.copy(fItem.getInputStream(), fo, true);
										fo.flush();

										Tika tika = new Tika();
										String mType = tika.detect(toDir);
										toDir.delete();

										Pattern p = Pattern.compile(mimeExtension.replaceAll(",", "|"), Pattern.CASE_INSENSITIVE);
										Matcher m = p.matcher(mType);
										if (!m.matches()) {
											isVaild = false;
											vaildMsg = "File Type Error!  " + itemName + " [" + mType + "]";
										} else {
											isVaild = true;
										}
									}
								}
								
								if(!isVaild) {
									throw new FileUploadException(vaildMsg);
								}

								fileupload(fItem.getInputStream(), itemName, tsys972Temp,  realCnt);
								tsys972Temp.put("fileSeq", fileSeq);
								tsys972List.add(tsys972Temp);
								
								JSONObject jsonObject = new JSONObject();
								jsonObject.put("fileSeq", fileSeq);
								jsonObject.put("seqNo", realCnt);
								jsonObject.put("rFileNm", tsys972Temp.get("rFileNm"));
								jsonObject.put("sFileNm", tsys972Temp.get("sFileNm"));
								jsonObject.put("fileSize", tsys972Temp.get("fileSize"));
								jsonArray.put(jsonObject);
								
								realCnt++;
							}
						}

						boolean result = jFileUploadService.fileStoreSave(tsys972List);

						if (!result) {
							throw new Exception("fileSave falied");
						}
					}
					
					return jsonArray;
				} catch(Exception e) {
					e.printStackTrace();
					throw new Exception("Saved Error!");
				} finally {
					try {
						if(fo != null) {
							fo.close();
							fo = null;
						}
					} catch (Exception ee) {}
					try {
						if(toDir != null) {
							toDir.delete();
						}
					} catch (Exception ee) {}
					
				}
			} else {
				throw new Exception("Error!");
			}
		}
	}
	
	protected abstract InputStream filedownload(Map<?, ?> paramMap) throws Exception;
	
	public void download() throws Exception {
		download(false);
	}
	
	public void download(boolean isDirectView) throws Exception {
		Map<String, String[]> paramMap = request.getParameterMap();
		String[] fileSeqArr = paramMap.get("fileSeq");
		String[] seqNoArr = paramMap.get("seqNo");
		
		download(isDirectView, fileSeqArr, seqNoArr);
	}
	
	public void download(boolean isDirectView, String[] fileSeqArr, String[] seqNoArr) throws Exception {
		synchronized (lockObj) {
			init();
			
			File zipFile = null;
			FileOutputStream fos = null;
			ZipOutputStream zos = null;
			InputStream in = null;
			OutputStream outt = null;
			List<Map<?, ?>> outputList = null;
			
			try {
				if(fileSeqArr != null) {
					outputList = new ArrayList<Map<?,?>>();
					WebApplicationContext webAppCtxt = WebApplicationContextUtils.getWebApplicationContext(session.getServletContext());
					JFileUploadService jFileUploadService = (JFileUploadService) webAppCtxt.getBean("JFileUploadService");
					
					for(int i = 0; i < fileSeqArr.length; i++) {
						String fileSeq = fileSeqArr[i];
						if(fileSeq == null || "".equals(fileSeq)) {
							continue;
						}
						
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("ssnEnterCd", this.enterCd);
						map.put("fileSeq", fileSeq);
						
						if(seqNoArr == null) {
							Collection<?> resultList = jFileUploadService.jFileList(map);
							
							for(Object listItem : resultList) {
								outputList.add((Map<?, ?>) listItem);
							}
						} else {
							String seqNo = seqNoArr[i];
							if(seqNo == null || "".equals(seqNo)) {
								continue;
							}
							
							map.put("seqNo", seqNo);
							outputList.add(jFileUploadService.jFileMap(map));
						}
					}
					
					if(outputList != null && outputList.size() > 0) {
						String downloadName = null;
						
						
						if(outputList.size() == 1) {
							Map<?, ?> resultMap = outputList.get(0);
							//downloadName = String.valueOf(resultMap.get("rFileNm"));
							downloadName = StringUtil.stringValueOf(resultMap.get("rFileNm"));
							in = filedownload(resultMap);
						} else {
							downloadName = getTimeStemp() + ".zip";
							zipFile = new File(config.getTempDir() + "/" + downloadName);
							File upDir = zipFile.getParentFile();
							
							if(!upDir.isDirectory()) {
								upDir.mkdirs();
							}
							
							fos = new FileOutputStream(zipFile);
							zos = new ZipOutputStream(fos);
							
							Iterator<Map<?, ?>> it = outputList.iterator();
							
							while(it.hasNext()) {
								Map<?, ?> resultMap = it.next();
								addEntry(zos, filedownload(resultMap), String.valueOf(resultMap.get("rFileNm")));
							}
							
							zos.close();
							zos = null;
							fos.close();
							fos = null;
							
							in = new FileInputStream(zipFile);
							zipFile.delete();
						}
						
						
						if(in == null) {
							throw new Exception("<script>alert('The file does not exist.');</script>");
						}
						if(!isDirectView) {
							response.setHeader("Content-Type", "application/octet-stream");
							response.setHeader("Content-Disposition", getEncodedFilename(downloadName, getBrowser(request)));
							response.setContentLength((int) in.available());
						}
						
						outt = response.getOutputStream();
						
						byte b[] = new byte[1024];
						int numRead = 0;
						while ((numRead = in.read(b)) != -1) {
							outt.write(b, 0, numRead);
						}
						
						outt.flush();
						outt.close();
						outt = null;
						in.close();
						in = null;
					} else {
						throw new Exception("<script>alert('The file does not exist.');</script>");
					}
					
				} else {
					throw new Exception("<script>alert('The file does not exist.');</script>");
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			} finally {
				try {
					if(outt != null) {
						outt.close();
					}
				} catch (Exception ee) {}
				
				try {
					if(in != null) {
						in.close();
					}
				} catch (Exception ee) {}
				
				try {
					if(fos != null) {
						fos.close();
					}
				} catch (Exception ee) {}
				
				try {
					if(zos != null) {
						zos.close();
					}
				} catch (Exception ee) {}
				
				if(zipFile != null && zipFile.exists()) {
					zipFile.delete();
				}
			}
		}
	}
	
	private void addEntry(ZipOutputStream zos, InputStream is, String realFileName) throws Exception {
		ByteArrayOutputStream baos = null;
		ByteArrayInputStream bais = null;
		BufferedInputStream bis = null;
		
		try {
			byte buffer[] = new byte[102400];
			int bytesRead = 0;

			
			baos = new ByteArrayOutputStream();
			
			while ((bytesRead = is.read(buffer)) > 0) {
				baos.write(buffer, 0, bytesRead);
			}
			
			bais = new ByteArrayInputStream(baos.toByteArray());
			baos.close();
			baos = null;
			
			bis = new BufferedInputStream(bais);
			CRC32 crc = new CRC32();

			bais.mark(0);
			while ((bytesRead = bis.read(buffer)) > 0) {
				crc.update(buffer, 0, bytesRead);
			}
			
			bais.reset();
			bis.close();
			bis = null;
			bis = new BufferedInputStream(bais);
			
			ZipEntry entry = new ZipEntry(realFileName);
			entry.setMethod(ZipEntry.STORED);
			entry.setCompressedSize(bais.available());
			entry.setSize(bais.available());
			entry.setCrc(crc.getValue());
			zos.putNextEntry(entry);
			
			while ((bytesRead = bis.read(buffer)) > 0) {
				zos.write(buffer, 0, bytesRead);
			}
			
			bis.close();
			bis = null;
			bais.close();
			bais = null;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			try { if(bis != null) { bis.close(); bis = null;} } catch(Exception ee){}
			try { if(bais != null) { bais.close(); bais = null;} } catch(Exception ee){}
			try { if(baos != null) { baos.close(); baos = null;} } catch(Exception ee){}
		}
	}
	
	protected abstract void filedelete(List<Map<?, ?>> deleteList) throws Exception;
	
	public void delete() throws Exception {
		synchronized(lockObj) {
			init();
			
			Map<String, String[]> paramMap = request.getParameterMap();
			
			String[] fileSeqArr = paramMap.get("fileSeq");
			String[] seqNoArr = paramMap.get("seqNo");
			
			try {
				if(fileSeqArr != null) {
					WebApplicationContext webAppCtxt = WebApplicationContextUtils.getWebApplicationContext(session.getServletContext());
					JFileUploadService jFileUploadService = (JFileUploadService) webAppCtxt.getBean("JFileUploadService");
					
					List<Map<?, ?>> deleteList = new ArrayList<Map<?,?>>();
					
					if(seqNoArr == null || seqNoArr.length > 1) {
						if(seqNoArr == null) {
							for(String fSeq : fileSeqArr) {
								Map<String, Object> map = new HashMap<String, Object>();
								map.put("ssnEnterCd", this.enterCd);
								map.put("fileSeq", fSeq);
								
								Collection<?> resultList = jFileUploadService.jFileList(map);
								
								for(Object listItem : resultList) {
									deleteList.add((Map<?, ?>) listItem);
								}
							}
						} else {
							for(String sNo : seqNoArr) {
								Map<String, Object> map = new HashMap<String, Object>();
								map.put("ssnEnterCd", this.enterCd);
								map.put("fileSeq", fileSeqArr[0]);
								map.put("seqNo", sNo);
								
								deleteList.add(jFileUploadService.jFileMap(map));
							}
						}
					} else {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("ssnEnterCd", this.enterCd);
						map.put("fileSeq", fileSeqArr[0]);
						map.put("seqNo", seqNoArr[0]);
						
						deleteList.add(jFileUploadService.jFileMap(map));
					}
					
					
					filedelete(deleteList);
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
		}
	}
	
	

	protected String getBrowser(HttpServletRequest request) {
		String header = request.getHeader("User-Agent");
		if (header != null) {
			if (header.indexOf("Trident") > -1) {
				return "MSIE";
			} else if (header.indexOf("Chrome") > -1) {
				return "Chrome";
			} else if (header.indexOf("Opera") > -1) {
				return "Opera";
			} else if (header.indexOf("iPhone") > -1 && header.indexOf("Mobile") > -1) {
				return "iPhone";
			} else if (header.indexOf("Android") > -1 && header.indexOf("Mobile") > -1) {
				return "Android";
			}
		}
		return "Firefox";
	}

	protected String getEncodedFilename(String filename, String browser) throws Exception {
		String dispositionPrefix = "attachment;filename=";
		// String getDecodedFilename = "attachment;filename=";
		String encodedFilename = null;
		if (browser.equals("MSIE")) {
			encodedFilename = URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20");
		} else if (browser.equals("Firefox")) {
			encodedFilename = "\"" + new String(filename.getBytes("UTF-8"), "8859_1") + "\"";
		} else if (browser.equals("Opera")) {
			encodedFilename = "\"" + new String(filename.getBytes("UTF-8"), "8859_1") + "\"";
		} else if (browser.equals("Chrome")) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < filename.length(); i++) {
				char c = filename.charAt(i);
				if (c > '~') {
					sb.append(URLEncoder.encode("" + c, "UTF-8"));
				} else {
					sb.append(c);
				}
			}
			encodedFilename = sb.toString();
		} else {
			throw new RuntimeException("Not supported browser");
		}

		return dispositionPrefix + encodedFilename;
	}
	
	
}
