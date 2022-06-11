package com.hr.tim.awaytime.cardInOutUpload;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.tika.Tika;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.hr.common.code.CommonCodeService;
import com.hr.common.logger.Log;
import com.hr.common.util.ParamUtils;
import com.oreilly.servlet.MultipartRequest;
import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;

@Controller
@RequestMapping("/CardInOutUpload.do")
public class CardInOutUploadController {

	@Inject
	@Named("CardInOutUploadService")
	private CardInOutUploadService cardInOutUploadService;

	/**
	 * 사원증기록업로드 View
	 *
	 * @return String
	 * @throws Exception
	 */
	@RequestMapping(params = "cmd=viewCardInOutUpload")
	public String viewCardInOutUpload() throws Exception {
		return "tim/awayTime/cardInOutUpload/cardInOutUpload";
	}

	/**
	 * 사원증기록업로드 파일업로드 View
	 *
	 * @return String
	 * @throws Exception
	 */
	@RequestMapping(params = "cmd=viewCardInOutUploadPop")
	public String viewCardInOutUploadPop() throws Exception {
		return "tim/awayTime/cardInOutUpload/cardInOutUploadPop";
	}
	
	/**
	 * 사원증기록업로드 조회
	 *
	 * @param session
	 * @param request
	 * @param paramMap
	 * @return ModelAndView
	 * @throws Exception
	 */
	@RequestMapping(params = "cmd=getCardInOutUploadList")
	public ModelAndView getCardInOutUploadList(
			HttpSession session,  HttpServletRequest request,
			@RequestParam Map<String, Object> paramMap ) throws Exception {
		Log.DebugStart();

		paramMap.put("ssnEnterCd", session.getAttribute("ssnEnterCd"));

		List<?> list  = new ArrayList<Object>();
		String Message = "";
		try{
			list = cardInOutUploadService.getCardInOutUploadServiceList(paramMap);

		}catch(Exception e){
			Message="조회에 실패 하였습니다.";
		}
		ModelAndView mv = new ModelAndView();
		mv.setViewName("jsonView");
		mv.addObject("DATA", list);
		mv.addObject("Message", Message);
		Log.DebugEnd();
		return mv;
	}
	

	/**
	 * 
	 *
	 * @param session
	 * @param request
	 * @param paramMap
	 * @return ModelAndView
	 * @throws Exception
	 */
	@RequestMapping(params = "cmd=saveCardInOutUpload")
	public ModelAndView saveCardInOutUpload(
			HttpSession session,  HttpServletRequest request,
			@RequestParam Map<String, Object> paramMap ) throws Exception {
		Log.DebugStart();

		Map<String, Object> convertMap = ParamUtils.requestInParamsMultiDML(request,paramMap.get("s_SAVENAME").toString(),"");
		convertMap.put("ssnSabun", 	session.getAttribute("ssnSabun"));
		convertMap.put("ssnEnterCd",session.getAttribute("ssnEnterCd"));

		String message = "";
		int resultCnt = -1;
		try{
			resultCnt = cardInOutUploadService.saveCardInOutUploadService(convertMap);
			if(resultCnt > 0){ message="저장 되었습니다."; } else{ message="저장된 내용이 없습니다."; }
		}catch(Exception e){
			resultCnt = -1; message="저장에 실패 하였습니다.";
		}
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("Code", resultCnt);
		resultMap.put("Message", message);
		ModelAndView mv = new ModelAndView();
		mv.setViewName("jsonView");
		mv.addObject("Result", resultMap);
		Log.DebugEnd();
		return mv;
	}
	
	
	/**
	 * 삭제처리
	 *
	 * @param session
	 * @param request
	 * @param paramMap
	 * @return ModelAndView
	 * @throws Exception
	 */
	@RequestMapping(params = "cmd=deleteCardInOutUpload")
	public ModelAndView deleteCardInOutUpload(
			HttpSession session,  HttpServletRequest request,
			@RequestParam Map<String, Object> paramMap ) throws Exception {
		Log.DebugStart();
		
		paramMap.put("ssnSabun", 	session.getAttribute("ssnSabun"));
		paramMap.put("ssnEnterCd",session.getAttribute("ssnEnterCd"));
		
		String message = "";
		int cnt = 0;
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try{
			String ymd = paramMap.get("baseDate").toString();
			ymd = ymd.replaceAll("[^0-9]","");
			paramMap.put("baseDateTemp", ymd);
			cnt = cardInOutUploadService.deleteCardInOutUploadConfirm(paramMap);
			if(cnt == 0){ 
				resultMap.put("Code", -1);
				message="삭제된 내용이 없습니다."; 
			} 
			else{ 
				message="삭제 처리 되었습니다.";
				resultMap.put("Code", 0);
			}
		}catch(Exception e){
			resultMap.put("Code", -1);
			message = "삭제 처리도중 에러가 발생하였습니다.";
		}
		resultMap.put("Message", message);
		ModelAndView mv = new ModelAndView();
		mv.setViewName("jsonView");
		mv.addObject("Result", resultMap);
		Log.DebugEnd();
		return mv;
	}
	
	@RequestMapping(params = "cmd=cardInOutUploadFileSave")
	public ModelAndView cardInOutUploadFile(
			HttpSession session,  HttpServletRequest request, HttpServletResponse response,
			@RequestParam Map<String, Object> paramMap ) throws Exception {
		paramMap.put("ssnEnterCd", session.getAttribute("ssnEnterCd"));
		paramMap.put("ssnSabun", session.getAttribute("ssnSabun"));

		String ssnEnterCd = (String) session.getAttribute("ssnEnterCd");
		String ssnSabun = (String) session.getAttribute("ssnSabun");

		String excelFile = (String) paramMap.get("file");
		String rPath  =  request.getSession().getServletContext().getRealPath("/")+"/hrfile/"+ssnEnterCd +"/tim/cardInOut/" +ssnSabun + "/"+excelFile;
		paramMap.put("rPath", rPath);
		File destFile = new File(rPath);
		Tika tika = new Tika();
		String fileType = tika.detect(destFile);
		Log.Debug("fileType : " + fileType);
		String ext =excelFile.substring(excelFile.lastIndexOf(".")+1).toUpperCase();

		boolean isExcel = false;
		String[] mtList = {"application/vnd.ms-excel"};

		int resultCnt = -1;
		String message = "";

		try{

			if ( "XLS".equals(ext) || "XLSX".equals(ext)){
				isExcel = true;
			}

			if ( !isExcel ){
				Map<String, Object> resultMap = new HashMap<String, Object>();
				resultMap.put("Code", resultCnt);
				resultMap.put("Message", "올바른 파일 형식이 아닙니다.");
				ModelAndView mv = new ModelAndView();
				mv.setViewName("jsonView");
				mv.addObject("Result", resultMap);
				return mv;
			}

			if(excelFile==null || excelFile.isEmpty()){
				Map<String, Object> resultMap = new HashMap<String, Object>();
				resultMap.put("Code", resultCnt);
				resultMap.put("Message", "엑셀파일을 선택 해 주세요.");
				ModelAndView mv = new ModelAndView();
				mv.setViewName("jsonView");
				mv.addObject("Result", resultMap);
				return mv;
			}

			if(!destFile.exists()) {
				Map<String, Object> resultMap = new HashMap<String, Object>();
				resultMap.put("Code", resultCnt);
				resultMap.put("Message", "파일이 존재하지 않습니다.");
				ModelAndView mv = new ModelAndView();
				mv.setViewName("jsonView");
				mv.addObject("Result", resultMap);
				return mv;
			}
			resultCnt = cardInOutUploadService.excelUpload(paramMap);
			Log.Debug("resultCnt : " + resultCnt);
			if(resultCnt > 0){ message=resultCnt + "건 업로드 되었습니다."; } else{ message="업로드된 내용이 없습니다."; }
		}catch(IOException e){
			resultCnt = -1; message="업로드에 실패하였습니다.";
		}
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("Code", resultCnt);
		resultMap.put("Message", message);
		ModelAndView mv = new ModelAndView();
		mv.setViewName("jsonView");
		mv.addObject("Result", resultMap);
		Log.DebugEnd();
		return mv;
	}
	
	@RequestMapping(params = "cmd=upload")
	public ModelAndView upload(HttpSession session, HttpServletRequest request, HttpServletResponse response,
			@RequestParam Map<String, Object> paramMap)  throws Exception{

		String ssnEnterCd = (String) session.getAttribute("ssnEnterCd");
		String ssnSabun = (String) session.getAttribute("ssnSabun");

		String rPath  =  request.getSession().getServletContext().getRealPath("/")+"/hrfile/"+ssnEnterCd +"/tim/cardInOut/" +ssnSabun + "/";

		Log.Debug("File Upload Path : "+rPath);

		File df = new File(rPath);

		if(!df.isDirectory()) {
			if( df.mkdirs() ){
				Log.Debug("Directory Create Success");
			}
		}

		try{
			File[] destroy = df.listFiles();
			for(File des : destroy){
				Log.Debug("delete file list : "+des.getPath());
				des.delete();
			}
		}catch(Exception e){
			Log.Debug(e.getMessage());
		}

		int sizeLimit = 50 * 1024 * 1024 ;

		int resultCnt = -1;
		String message = "";

		try{

			MultipartRequest multi = new MultipartRequest(request, rPath, sizeLimit, "UTF-8" , new DefaultFileRenamePolicy());

			String fileName = multi.getOriginalFileName("inputFile");

			File destFile = new File(rPath+"/"+fileName);

			Log.Debug ("create file : " + (rPath+"/"+fileName));

		}catch(IOException e){
			resultCnt = -1; message="업로드에 실패하였습니다.";
		}

		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("Code", resultCnt);
		resultMap.put("Message", message);
		ModelAndView mv = new ModelAndView();
		mv.setViewName("jsonView");
		mv.addObject("Result", resultMap);
		Log.DebugEnd();
		return null;
    }
	
}
