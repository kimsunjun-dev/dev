package org.tmt.core.link.external.letter.deliver;

import java.util.Map;

import org.tmt.core.link.external.letter.Deliver;
import org.tmt.core.link.external.letter.Letter;
import org.tmt.core.link.mail.SenderSMTP;
import org.tmt.core.service.LinkLetterService;

public class DeliverEmail implements Deliver {

	
	private boolean isTestSend = false;			// 본 값이 true면 개발에서도 실제로 발송되며 수신자는 아래 지정한 전화번호/이메일로 한정됨     
	private String testClpNo = "";				// 수신자 핸펀
	private String testEmail = "";				// 수신자 이메일
	
	@Override
	public String deliver(Map sender, Map receiver, LinkLetterService service) {
		// TODO Auto-generated method stub
		String deliverResult = "";
		
		SenderSMTP smtp = new SenderSMTP();
		
		if(sender.get(Letter.SENDER) != null && !sender.get(Letter.SENDER).equals(""))
		{
			smtp.setSender(sender.get(Letter.SENDER).toString());	
		}
		
		if(sender.get(Letter.SENDER_NAME) != null)
		{
			smtp.setSenderName(sender.get(Letter.SENDER_NAME).toString());	
		}

		if(this.isTestSend)
		{
			smtp.setRecipient(this.testEmail);
		}
		else
		{
			if(receiver.get(Letter.RECEIVER) != null)
			{
				smtp.setRecipient(receiver.get(Letter.RECEIVER).toString());
			}
		}
		


		if(receiver.get(Letter.RECEIVER_NAME) != null)
		{
			smtp.setRecipientName(receiver.get(Letter.RECEIVER_NAME).toString());	
		}
		
		smtp.setSubject(receiver.get(Letter.TITLE)+"");
		smtp.setBody(receiver.get(Letter.CONTENTS)+"");

		if(sender.get(Letter.TEMP_FILE_NM) != null && !sender.get(Letter.TEMP_FILE_NM).equals(""))
		{
			smtp.setAttachFileFullPath(sender.get(Letter.TEMP_FILE_NM).toString());
			smtp.setAttachFileViewName(sender.get(Letter.VIEW_FILE_NM).toString());
		}
		
	    try
		{
	    	
			if(receiver.get(Letter.RECEIVER) != null && !receiver.get(Letter.RECEIVER).equals(""))
			{
				smtp.send();
				deliverResult = "연계완료";
			}
			else
			{
				deliverResult = "받는이 주소가 없습니다.";
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
