package org.tmt.core.link.external.letter.deliver;

import java.util.HashMap;
import java.util.Map;

import org.tmt.core.link.external.letter.Deliver;
import org.tmt.core.link.external.letter.Letter;
import org.tmt.core.service.LinkLetterService;
import org.tmt.core.util.RandomStringGenerator;


public class DeliverPush implements Deliver {

	private boolean isTestSend = false;			// 본 값이 true면 개발에서도 실제로 발송되며 수신자는 아래 지정한 전화번호/이메일로 한정됨     
	private String testClpNo = "";				// 수신자 핸펀
	private String testEmail = "";				// 수신자 이메일
	
	@Override
	public String deliver(Map sender, Map receiver, LinkLetterService service) {
		
		String deliverResult = "";
		
		String iSendType = "KAKAO";
		String iScheduleStime = "";
		String iScheduleNtime = "";
		String iCategoryNm = "";
		String iResult = "0";
		
		if(!receiver.containsKey("RESEV_SND_DT"))
		{
			receiver.put("RESEV_SND_DT", "");
		}
		
		if(!receiver.containsKey("RESEV_SND_TIME"))
		{
			receiver.put("RESEV_SND_TIME", "");
		}
		
		if(receiver.get("RESEV_SND_DT") != null 
				&& receiver.get("RESEV_SND_TIME") != null
				&& receiver.get("RESEV_SND_DT").toString().length() == 8
				&& receiver.get("RESEV_SND_TIME").toString().length() == 4)
		{
			iScheduleStime = receiver.get("RESEV_SND_DT").toString() + receiver.get("RESEV_SND_TIME").toString() + "00";
			iResult = "R";
			
		}
		else
		{
			iScheduleStime = "00000000000000";
			iResult = "0";
		}

		
		Map sms = new HashMap();

		sms.put("KIND", "T");										//알림톡
		sms.put("USERCODE", "oksms"); 								//슈어엠발송대표계정
		sms.put("DEPTCODE", sender.get("DEPT_CD"));					//발송자소속
		
		sms.put("REQNAME", sender.get("TRNSM_NM"));					//발신자명
		sms.put("REQPHONE", sender.get("TRNSM_CTTP"));				//발신자
		sms.put("TEMPLATECODE", sender.get("TEMPLATE_CODE"));		//알림톡용 템플릿 코드
		sms.put("YELLOWID_KEY", sender.get("SENDER_KEY"));			//알림톡용 템플릿 코드
		
		sms.put("REQTIME", iScheduleStime);	//발신자
		sms.put("RESULT", iResult);	
		
		if(!sender.containsKey("EXHG_SND_YN"))
		{
			sms.put("RESEND", "N");
		}
		else
		{
			if(sender.get("EXHG_SND_YN") != null && sender.get("EXHG_SND_YN").equals("Y"))
			{
				sms.put("RESEND", "Y");
			}
			else
			{
				sms.put("RESEND", "N");
			}
			
		}
		

		if(this.isTestSend)
		{
			sms.put("CALLPHONE", this.testClpNo);	   									//수신자
			sms.put("CALLNAME", receiver.get(Letter.RECEIVER_NAME));			//수신자명
		}
		else
		{
			// 실제발송
			sms.put("CALLPHONE", receiver.get(Letter.RECEIVER).toString());	    //수신자	
			sms.put("CALLNAME", receiver.get(Letter.RECEIVER_NAME));			//수신자명
		}
	
		
		sms.put("SUBJECT", receiver.get(Letter.TITLE));		//제목
		sms.put("MSG", receiver.get(Letter.CONTENTS));		//내용
		sms.put("SMSLINKID", receiver.get("SMSLINKID"));	//연계아이디
				
	    try
		{
	    	
			if(receiver.get(Letter.RECEIVER) != null && !receiver.get(Letter.RECEIVER).equals(""))
			{
				if(this.isTestSend)
				{
					service.callKakaoTalkDbLink(sms);
				}
				else
				{
					service.callKakaoTalk(sms);
				}
				
				
				deliverResult = "연계완료";
			}
			else
			{
				deliverResult = "받는이 전화번호가 없습니다.";
			}
				    	
		}
		catch (Exception e) 
		{
			deliverResult = e.getMessage();
		}
		
		
		return deliverResult;
					  
	}
	
	
	public void initTest(boolean isTestSend, String testClpNo, String testEmail)
	{
		this.isTestSend = isTestSend;
		this.testClpNo = testClpNo;
		this.testEmail = testEmail;
	}

}
