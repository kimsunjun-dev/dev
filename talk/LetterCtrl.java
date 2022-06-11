package org.tmt.core.link.external.letter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exbuilder.adaptor.egovframework.uiadaptor.UiDTO;
import org.exbuilder.adaptor.exbuilder5.constants.AppWorksConstants;
import org.exbuilder.engine.util.StringUtil;
import org.tmt.core.constants.Alert;
import org.tmt.core.constants.ProcessConstants;
import org.tmt.core.exception.AppException;
import org.tmt.core.form.FormLoader;
import org.tmt.core.form.FormMap;
import org.tmt.core.form.FormRepository;
import org.tmt.core.form.doc.LetterForm;
import org.tmt.core.service.LinkLetterService;
import org.tmt.core.vo.UserView;

public class LetterCtrl {
	
	private Log logger = LogFactory.getLog(this.getClass());
	
	public static String PUSH = "DeliverPush";
	public static String SMS = "DeliverSms";
	public static String EMAIL = "DeliverEmail";
	
	private Deliver deliver;
	private Map sender;
	private List letterStack;
	
	private UiDTO dto;
	private String title;
	private String contents;
	private String remark;
	private String templateCode;
	private String exhgSndYn;
	private String extServiceUserId;
	private String extServiceDeptCd;
	
	// SMS / MAIL을 실제 발송여부 
	// SUREDATA에 INSERT
	// 개발 DB의 경우 SUREDATA 가 가상으로 만들어져 있어 데이터만 인서트 되고 
	// 발송은 되지 않음.
	private boolean isRealSend = true;			
	
	// SMS / MAIL 발송 테스트 를 위한 환경 <로컬 개발에서 동작> - START 
	// 본 값이 true면 개발에서도 실제로 발송되며 수신자는 아래 지정한 번호로만 발송됨 
	// 발송되는 SMS, 알림톡, 이메일 모두 적용
	private boolean isTestSend = false;										 
	private String testClpNo = "";								// 수신자 핸펀 (본인핸펀)
	private String testEmail = "";					// 수신자 이메일 (본인이메일)
	// SMS / MAIL 발송 테스트 를 위한 환경 <로컬 개발에서 동작> - END 
	
	private LinkLetterService linkLetterService;
	
	public LetterCtrl(String deliverType, UiDTO dto, String remark) throws AppException {

		super();
		
		try {
			
			this.dto = dto;
			this.deliver = (Deliver) Class.forName("org.tmt.core.link.external.letter.deliver."+deliverType).newInstance();
			
			this.sender = new HashMap();
			
			this.letterStack = new ArrayList();
			
			this.sender.put("TRNSM_MASS_RCD", deliverType);
			this.sender.put("REMARK", remark);
			this.sender.put("SND_DIV_RCD", "SYSTEM");
			
			this.deliver.initTest(this.isTestSend, this.testClpNo, this.testEmail);
			
			//UserView userView = (UserView) this.dto.getObject(ProcessConstants.USER_VIEW);
			//this.sender.put("DEPT_CD", userView.getUserInfo("ASGN_DEPT_CD"));
						
		} catch (Exception e) {
			// e.printStackTrace();
			// 메일/SMS/PUSH 발송 초기화에 실패하였습니다.
			throw new AppException(Alert.ERROR, "CMN003.CMN@CMN045");			
		}
	}
	
	public void setRealSend(boolean realSend)
	{
		this.isRealSend = realSend;
	}
	
	public void setTemplateCode(String templateCode)
	{
		this.templateCode = templateCode;
	}
	
	public void setExhgSndYn(String strExhgSndYn)
	{
		this.sender.put("EXHG_SND_YN", strExhgSndYn);
	}	
	
	public void setUserPopSnd()
	{
		this.sender.put("SND_DIV_RCD", "USER_POP");
	}
	
	public void setService(LinkLetterService service)
	{
		this.linkLetterService = service;
	}
	
	public void setSender(String sender)
	{
		this.sender.put(Letter.SENDER, sender);
	}
	
	public void setSenderName(String senderName)
	{
		this.sender.put(Letter.SENDER_NAME, senderName);
	}
	
	public void setTitle(String title)
	{
		this.title = title;
	}	
	
	public void setContents(String contents)
	{
		this.contents = contents;
	}	
	
	public void setRemark(String remark)
	{
		this.remark = remark;
	}
	
	
	public void setAttcFile(String fullPath, String viewFileName)
	{
		this.sender.put(Letter.TEMP_FILE_NM, fullPath);
		this.sender.put(Letter.VIEW_FILE_NM, viewFileName);
	}
	
	public void setContentsXMLForm(String formMapId, String formId) throws Exception
	{
		LetterForm form = loadXMLForm(formMapId, formId);
		this.title = form.getTitle();
		this.contents = form.getContents(); 
	}
	
	public void setContentsDBForm(String letterId) throws Exception
	{
		Map letterMap =  linkLetterService.selLoadLetterForm(letterId);
		this.title = (String) letterMap.get("SND_TITLE");
		this.contents = (String) letterMap.get("SND_MSG");
		this.templateCode = (String) letterMap.get("TEMPLATE_CODE");
	}
	
	
	public Map getContentsDBForm(String letterId) throws Exception
	{
		Map letterMap =  linkLetterService.selLoadLetterForm(letterId);
		this.templateCode = (String) letterMap.get("TEMPLATE_CODE");
		
		return letterMap;
	}
	
	
	/**
	 * 
	 * @param recevier
	 * @param name
	 * @param reservSendDate  20110101
	 * @param reservSendTime  1111
	 */
	public void addReceiver(String recevier, String name, String userId,
			String reservSendDate, String reservSendTime, Map mappingData)
	{
		Map letter = new HashMap();
		
		letter.put(Letter.RECEIVER, recevier);
		letter.put(Letter.RECEIVER_NAME, name);
		letter.put(Letter.USER_ID, userId);
		letter.put(Letter.CONTENTS, mappingContents(mappingData));
		letter.put(Letter.TITLE, mappingTitle(mappingData));
        
		this.letterStack.add(letter);
	}
	
	
	/**
	 * 
	 * @param recevier
	 * @param name
	 * @param reservSendDate  20110101
	 * @param reservSendTime  1111
	 */
	public void addReceiver(String recevier, String name, String reservSendDate, String reservSendTime, String title, String contents, String userId)
	{
		Map letter = new HashMap();
		
		letter.put(Letter.RECEIVER, recevier);
		letter.put(Letter.RECEIVER_NAME, name);
		letter.put(Letter.CONTENTS, contents);
		letter.put(Letter.TITLE, title);
		letter.put(Letter.RESEV_SND_DT, reservSendDate);
		letter.put(Letter.RESEV_SND_TIME, reservSendTime);
		letter.put(Letter.USER_ID, userId);
        
		this.letterStack.add(letter);
	}	
	
	/**
	 * 2401
	 * @param recevier
	 * @param name
	 * @param reservSendDate  20110101
	 * @param reservSendTime  1111
	 */
	public void addReceiver(String recevier, String name, String title, String contents, String userId)
	{
		Map letter = new HashMap();
		
		letter.put(Letter.RECEIVER, recevier);
		letter.put(Letter.RECEIVER_NAME, name);
		letter.put(Letter.CONTENTS, contents);
		letter.put(Letter.TITLE, title);
		letter.put(Letter.USER_ID, userId);
        
		this.letterStack.add(letter);
	}		
	
	public void extServiceSender(String userId, String deptCd)
	{
		this.extServiceDeptCd = deptCd;
		this.extServiceUserId = userId;
	}
	
	public int deliver() throws Exception
	{		
		int deliverCnt = 0;
		String strSndNo = linkLetterService.getLetterSndNo();
		
		UserView userView   = (UserView) dto.getObject(ProcessConstants.USER_VIEW);
		
		String strUserId = "";
		String strDeptCd = "";
		
		if(userView != null)
		{
			Map mapUserInfo     = userView.getUserInfo();
			strUserId = StringUtil.nullToEmpty(mapUserInfo.get("USER_ID"));
			strDeptCd = StringUtil.nullToEmpty(mapUserInfo.get("ASGN_DEPT_CD"));	
		}
		else
		{
			if(this.extServiceDeptCd == null || this.extServiceUserId == null)
			{
				//외부에서 발송시 외부발송자 부서가 지정되야 합니다.
				throw new AppException(Alert.ERROR, "CMN003.CMN@CMN061");
			}
		}
		
		// 발송 로그 생성 마스터 테이블
		sender.put("SND_NO", strSndNo);
		sender = this.dto.getDefaultData(sender, AppWorksConstants.DATA_INSERT);
		
		sender.put("USER_ID", strUserId);								// dto를 이용한 로그인세션참조
		sender.put("DEPT_CD", strDeptCd);								// dto를 이용한 로그인세션참조
		
		if(this.extServiceDeptCd != null && this.extServiceUserId != null)
		{			
			sender.put("USER_ID", this.extServiceUserId);
			sender.put("DEPT_CD", this.extServiceDeptCd);
			
			if(sender.get("CRT_USER_ID") == null
					|| sender.get("CRT_PGM_ID") == null)
			{
				sender.put("CRT_USER_ID", this.extServiceUserId);
				sender.put("CRT_PGM_ID", this.extServiceUserId);
				sender.put("CRT_IP_MAC", this.extServiceUserId);
			}
		}
		
		sender.put("TRNSM_NM", sender.get(Letter.SENDER_NAME));
		sender.put("TRNSM_CTTP", sender.get(Letter.SENDER));
		sender.put("TRNSM_NUM", this.letterStack.size());			// dto를 이용한 로그인세션참조
		sender.put("TEMPLATE_CODE", this.templateCode);				// 알림톡용 템플릿 코드

		
		if(!sender.containsKey("EXHG_SND_YN"))
		{
			sender.put("EXHG_SND_YN", "N");
		}

		
//발신번호 체크 전용
//		if(this.sender.get("TRNSM_MASS_RCD").equals(LetterCtrl.SMS))
//		{
//			Map phMap = linkLetterService.selSenderPhoneCheck(sender.get(Letter.SENDER).toString());
//			
//			
//			if(phMap == null)
//			{
//				throw new AppException(Alert.ERROR, "CMN003.CMN@CMN060");
//			}
//			else
//			{
//				
//				int cnt = Integer.parseInt(phMap.get("CNT").toString());
//				
//				if(cnt == 0)
//				{
//					throw new AppException(Alert.ERROR, "CMN003.CMN@CMN060");
//				}
//			}
//		}
		
		if(this.templateCode != null && !this.templateCode.equals(""))
		{
			Map kakaMap = linkLetterService.selSenderYelloIDKey(this.templateCode);
						
			if(kakaMap == null || kakaMap.isEmpty()
			   || kakaMap.get("SENDER_KEY") == null 
			   || kakaMap.get("SENDER_KEY").equals("")
			   || kakaMap.get("SENDER_CAL") == null
			   || kakaMap.get("SENDER_CAL").equals("")
			   )
			{
				throw new AppException(Alert.ERROR, "CMN003.CMN@CMN059");
			}
			else
			{
				sender.put("SENDER_KEY", kakaMap.get("SENDER_KEY") );				// 알림톡용 템플릿 코드	
				sender.put("SENDER_CAL", kakaMap.get("SENDER_CAL") );				// 알림톡용 콜백번호				
			}
		}
				
		linkLetterService.insLetterSender(sender);
		
		for(int i=0; i<this.letterStack.size(); i++)
		{
			Map receiver = (Map) this.letterStack.get(i);
			
			receiver.put("SND_NO", strSndNo );
			receiver.put("SND_SERIAL_NO", (i+1));
						
			if(receiver.get("RESEV_SND_DT") == null)
			{
				receiver.put("RESEV_SND_DT", "");
			}
			
			if(receiver.get("RESEV_SND_TIME") == null)
			{
				receiver.put("RESEV_SND_TIME", "");
			}
			
			String deliverResult = "";
			
			receiver.put("SMSLINKID", linkLetterService.getLetterSndNo());
			
			if(this.isRealSend)
			{
				deliverResult = this.deliver.deliver(this.sender, receiver, linkLetterService);
			}
			else
			{
				deliverResult = "연계완료";
			}
			
			if(deliverResult.equals("연계완료"))
			{
				deliverCnt++;
			}
			
			receiver.put("DELIVER_RESULT", deliverResult);
			
			receiver = this.dto.getDefaultData(receiver, AppWorksConstants.DATA_INSERT);
			
			if(receiver.get("CRT_USER_ID") == null
					|| receiver.get("CRT_PGM_ID") == null)
			{
				receiver.put("CRT_USER_ID", this.extServiceUserId);
				receiver.put("CRT_PGM_ID", this.extServiceUserId);
				receiver.put("CRT_IP_MAC", this.extServiceUserId);
			}
			
			linkLetterService.insLetterReceiver(receiver);
		}
		
		return deliverCnt;
	}
	
	private String mappingTitle(Map mappingData)
	{
        String repTitle = new String(this.title);
		
        Iterator<String> keys = mappingData.keySet().iterator();
        while( keys.hasNext() ){
           String key = keys.next();
           repTitle = repTitle.replaceAll("@"+key+"", (String) mappingData.get(key));
        }

        return repTitle;		
	}
	
	private String mappingContents(Map mappingData)
	{
        String repContents = new String(this.contents);
		
        Iterator<String> keys = mappingData.keySet().iterator();
        while( keys.hasNext() ){
           String key = keys.next();
           repContents =  repContents.replaceAll("@"+key+"", (String) mappingData.get(key));
        }

        return repContents;		
	}
	
	
	private LetterForm loadXMLForm(String formMapId, String formId) throws Exception
	{
		FormRepository repository = FormRepository.getInstance();	
		FormMap formMap = repository.getFormMap(formMapId);
		
		if (formMap == null) {
			//해당하는 연계 폼을 찾을 수 없습니다. FORM-MAP-ID【{0}】 FORM ID【{1}】
			String[] msgValues = {formMapId, formId};
			throw new AppException(Alert.ERROR, "CMN003.CMN@CMN045", msgValues);		
		}

		if (repository.isReloadable()) {
			File file = repository.getFile(formMapId);
			if ((file != null) && (file.exists())
					&& (file.lastModified() > formMap.getLoadTime())) {
				try {
					FormLoader loader = new FormLoader();
					loader.reload(file);
				} catch (Exception e) {
					logger.debug(e.getMessage());
				}
			}
		}

		LetterForm form = (LetterForm)formMap.getForm(formId);
		if (form == null) {
			//해당하는 연계 폼을 찾을 수 없습니다. FORM-MAP-ID【{0}】 FORM ID【{1}】
			String[] msgValues = {formMapId, formId};
			throw new AppException(Alert.ERROR, "CMN003.CMN@CMN045", msgValues);
		}
		
		return form;
	}
	
}
