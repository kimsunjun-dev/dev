package com.hr.common.util.fileupload.jfileupload.web;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.hr.common.exception.FileUploadException;
import com.hr.common.logger.Log;
import com.hr.common.util.fileupload.impl.FileHandlerFactory;
import com.hr.common.util.fileupload.impl.IFileHandler;

/**
 *
 * 파일 업로드가 처리되기 위한 기본적인 환경정보를 처리하는 클래스

 */
@Controller("fileuploadJFileUploadController")
@RequestMapping("/fileuploadJFileUpload.do")
public class JFileUploadController {

	@Inject
	@Named("JFileUploadService")
	private JFileUploadService jFileUploadService;

	@Resource(name="fileupload")
	private Properties fp;
	
	/**
	 * file UPLOAD 관리
	 *
	 * @return String
	 * @throws Exception
	 */
	@RequestMapping(params = "cmd=fileMgrPopup")
	public String fileMgrPopup() throws Exception {
		return "common/popup/filePopup";
	}
	
	@RequestMapping(params = "cmd=upload")
	public void upload(HttpServletRequest request, HttpServletResponse response) throws FileUploadException, JSONException, IOException {
		Log.DebugStart();
		
		JSONObject jsonObject = new JSONObject();
		response.setContentType("text/plain");
		response.setCharacterEncoding("utf-8");

		Log.Debug("■■■■■■■■■■■■■■■ parameter start ■■■■■■■■■■■■■■■");
		for( Enumeration enumeration = request.getParameterNames(); enumeration.hasMoreElements();) {
	         Object obj = enumeration.nextElement();
	         String s = request.getParameterValues((String)obj)[0];
	         Log.Debug( "Parameter name ="+ obj.toString() + ", Parameter value =["+s+"]" );
		}
		Log.Debug("■■■■■■■■■■■■■■■ parameter end ■■■■■■■■■■■■■■■");
		
		try {
			IFileHandler fileHandler = FileHandlerFactory.getFileHandler(request, response);
			JSONArray jsonArray = fileHandler.upload();
			jsonObject.put("code", "success");
			jsonObject.put("data", jsonArray);
			
			response.getWriter().write(jsonObject.toString());
		} catch(FileUploadException fue) {
			fue.printStackTrace();
			jsonObject.put("code", "error");
			jsonObject.put("msg", fue.getMessage());
			response.getWriter().write(jsonObject.toString());
		} catch(Exception e) {
			e.printStackTrace();
			jsonObject.put("code", "error");
			jsonObject.put("msg", e.getMessage());
			response.getWriter().write(jsonObject.toString());
//			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, jsonObject.toString());
		}
		
		Log.DebugEnd();
	}
	
	@RequestMapping(params = "cmd=delete")
	public ModelAndView delete(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// comment 시작
		Log.DebugStart();

		String message = "";
		String code = "success";
		
		try{
			IFileHandler fileHandler = FileHandlerFactory.getFileHandler(request, response);
			fileHandler.delete();
		}catch(Exception e){
			e.printStackTrace();
			message="저장에 실패하였습니다.";
			code = "error";
		}

		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("code", code);
		resultMap.put("message", message);
		ModelAndView mv = new ModelAndView();
		mv.setViewName("jsonView");
		mv.addObject("result", resultMap);
		Log.DebugEnd();
		return mv;
	}
	
	@RequestMapping(params = "cmd=download")
	public void download( HttpServletRequest request,HttpServletResponse response) throws Exception {
		Log.DebugStart();
		
		IFileHandler fileHandler = FileHandlerFactory.getFileHandler(request, response);
		fileHandler.download();
		
		Log.DebugEnd();
	}
	
	@RequestMapping(params = "cmd=jFileList")
	public ModelAndView jFileList(HttpSession session,  HttpServletRequest request, @RequestParam Map paramMap) throws Exception {
		Log.DebugStart();
		paramMap.put("ssnSabun", 	session.getAttribute("ssnSabun"));
		paramMap.put("ssnEnterCd",session.getAttribute("ssnEnterCd"));
		paramMap.put("ssnLocaleCd", "kr");

		List list =  jFileUploadService.jFileList(paramMap);

		ModelAndView mv = new ModelAndView();
		mv.setViewName("jsonView");
		mv.addObject("data", list);
		Log.DebugEnd();
		return mv;
	}
	
}
