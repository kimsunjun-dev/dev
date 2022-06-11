package org.tmt.cmn.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exbuilder.adaptor.egovframework.uiadaptor.UiDTO;
import org.exbuilder.adaptor.exbuilder5.util.AutoCUDDataMapper.TableDataMap;
import org.exbuilder.engine.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.tmt.cmn.service.CmnCodeService;
import org.tmt.cmn.service.CmnCommonService;
import org.tmt.cmn.service.CmnSmsMngService;
import org.tmt.cmn.service.CmnUserDivService;
import org.tmt.core.constants.Alert;
import org.tmt.core.constants.ProcessConstants;
import org.tmt.core.controller.AbstractBaseController;
import org.tmt.core.egovframework.com.cmn.service.EgovFileMngUtil;
import org.tmt.core.exception.AppException;
import org.tmt.core.link.external.letter.LetterCtrl;
import org.tmt.core.service.LinkLetterService;
import org.tmt.core.util.ServerUtil;
import org.tmt.core.util.UtilEncrypt;
import org.tmt.core.vo.UserView;

/**
 * 
 * <pre>
 * 시   스   템  : 
 * 단위시스템  : 
 * 프로그램명  : 알림톡작성(팝업)
 * 설         명  : 알림톡작성
 * </pre>
 *            
 * @author : sunjun 
 *
 * 이력사항
 * 2017. 10. 31. sunjun 최초작성
 */

@Controller
public class CmnPushController extends AbstractBaseController {
	@Autowired
	private EgovFileMngUtil egovFileMngUtil;
	
	@Autowired
	private CmnSmsMngService cmnSmsMngService ;
	
	@Autowired
	private CmnCommonService cmnCommonService;
	
	@Autowired
	private LinkLetterService linkLetterService;
	
	/**
	 * 
	 * <pre>
	 * 메소드명	: onLoad
	 * 설	 명	:
	 * </pre>
	 *
	 * @author	: sunjun
	 *
	 * 이력사항
	 * 2017. 11. 2. sunjun 최초작성
	 *
	 * @param dto
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/cmn/cmnPush/onLoad.do")
	public ModelAndView onLoad(UiDTO dto, HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String, Object> data = new HashMap<String, Object>();
		Map<String, Object> mapParam = new HashMap<String, Object>();

		data.put("strNowDt", cmnCommonService.getSysdate("CUT_DT", "YYYYMMDD"));	//현재날짜
		
		// 메시지그룹코드
		String strMsgGrpRcd = dto.getString("strMsgGrpRcd");
		mapParam.put("MSG_GRP_RCD", strMsgGrpRcd);
		Object objData= cmnSmsMngService.selCmnSndMsgMgtPushList(mapParam);
		data.put("sndMsgList", objData);	//양식구분
		
		return makeModel(data);
	}
	
	/**
	 * 
	 * <pre>
	 * 메소드명	: listSendInfo
	 * 설	 명	:
	 * </pre>
	 *
	 * @author	: sunjun
	 *
	 * 이력사항
	 * 2017. 11. 2. sunjun 최초작성
	 *
	 * @param dto
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/cmn/cmnPush/listSendInfo.do")
	public ModelAndView listSendInfo(UiDTO dto, HttpServletRequest request, 
			HttpServletResponse response) throws Exception {
		
		// 메시지ID
		String strMsgId = dto.getString("strMsgId");
		
		Map mapParam = new HashMap();
		mapParam.put("MSG_ID", strMsgId);
		mapParam.put("USE_YN", "Y");		// 사용여부가 Y
		
		Map mapSendInfo = new HashMap();
		
		// 메시지ID에 해당하는 발송정보 GET
		List listSendInfo = cmnSmsMngService.selCmnSndMsgMgtList(mapParam);
		if(null != listSendInfo && listSendInfo.size() > 0){
			mapSendInfo = (Map)listSendInfo.get(0);
		}
		
		Map<String, Object> data = new HashMap<String, Object>();
		
		data.put("sendInfo", mapSendInfo);
		
		return makeModel(data);
	}
	
	/**
	 * 
	 * <pre>
	 * 메소드명	: saveSend
	 * 설	 명	: 알림톡 발송
	 * </pre>
	 *
	 * @author	: sunjun
	 *
	 * 이력사항
	 * 2017. 11. 2. sunjun 최초작성
	 *
	 * @param dto
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/cmn/cmnPush/saveSend.do")
	public ModelAndView saveSend(UiDTO dto, HttpServletRequest request, 
			HttpServletResponse response) throws Exception {
		
		String strRegPsn = dto.getString("strRegPsn");			// 등록자
		String strSndUse = dto.getString("strSndUse");			// 발송용도
		String strSndTitle = dto.getString("strSndTitle");		// 제목
		String strSndMsg = dto.getString("strSndMsg");			// 내용
		String strReserved = dto.getString("strReserved");		// 예약전송
		String strReserveDt = dto.getString("strReserveDt");	// 예약일자
		String strReserveTime = dto.getString("strReserveTime");// 예약시간
		String strSender = dto.getString("strSender");			// 보내는사람
		String strSndDiv = dto.getString("strSndDiv");			// 발송구분(SMS OR PUSH)
		String strTemplateCode = dto.getString("strTemplateCode"); //알림톡 탬플릿코드
		String strExhgSndYn = dto.getString("strExhgSndYn");       //알림톡 대체발송여부
		
		String strRealSend = dto.getString("strRealSend");			// 메일 첨부 파일
		
		
		// 예약전송일경우 시간 유효성 확인
		if(StringUtil.isNotNullEmpty(strReserved)){
			String strNowDt = cmnCommonService.getSysdate(
					"CUT_DT", "YYYYMMDDHH24MI");
			
			Long lNowDt = Long.parseLong(strNowDt);
			Long lReserve = Long.parseLong(strReserveDt + strReserveTime);
			
			if(lNowDt >= lReserve){
				// 예약발송은 현재시간 이후로 입력되어야 합니다.
				throw new AppException(Alert.WARN, "CMN003.CMN@CMN047");
			}
		}
		
		TableDataMap tableDataMap = dto.getTableData("CMN_SND_MSG_CTNT");
		
		List<Map> listIns = tableDataMap.getInsertData();
		
		// Letter컨트롤 선언
		LetterCtrl letter = null;
		letter = new LetterCtrl(LetterCtrl.PUSH, dto, strSndUse);
		
		
		//개발자 테스트용 코드
		if(strRealSend != null && strRealSend.equals("Y"))
		{
			letter.setRealSend(true);
		}
		//개발자 테스트용 코드
		
		
		letter.setService(linkLetterService);
		
		letter.setSender(strSender);			// 보내는사람
		letter.setSenderName(strRegPsn);		// 등록자
		letter.setTitle(strSndTitle);			// 제목
		letter.setContents(strSndMsg);			// 내용
		letter.setTemplateCode(strTemplateCode);// 탬플릿코드
		letter.setExhgSndYn(strExhgSndYn);      // 대체발송여부
		
		for(Map mapIns : listIns){
			String strRctpCttp = StringUtil.nullToEmpty(mapIns.get("RCTP_CTTP"));	// 전화번호
			String strRctpNm = StringUtil.nullToEmpty(mapIns.get("RCTP_NM"));		// 성명
			String strUserId = StringUtil.nullToEmpty(mapIns.get("USER_ID"));		// 사용자ID
			String strMsgTitle = StringUtil.nullToEmpty(mapIns.get("MSG_TITLE"));	// 제목
			String strMsgCont = StringUtil.nullToEmpty(mapIns.get("MSG_CONT"));		// 내용
			
			if(StringUtil.isNotNullEmpty(strReserved)){
				letter.addReceiver(strRctpCttp, strRctpNm, strReserveDt, strReserveTime, strMsgTitle, strMsgCont, strUserId);
			}else {
				letter.addReceiver(strRctpCttp, strRctpNm, null, null, strMsgTitle, strMsgCont, strUserId);
			}
		}
		
		int iSuccessCnt = letter.deliver();
		
		Map<String, Object> data = new HashMap<String, Object>();
		
		data.put("iSuccessCnt", iSuccessCnt);
		
		return makeModel(data);
	}
	
}
