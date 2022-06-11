package org.tmt.core.service;

import java.util.Map;

public interface LinkLetterService {

	public Map<String,Object> selLoadLetterForm(String letterFormId) throws Exception;
	public String getLetterSndNo() throws Exception;
	public Integer insLetterSender(Map<String, Object> sender) throws Exception;
	public Integer insLetterReceiver(Map<String, Object> receiver) throws Exception;
	public Map<String,Object> selSenderYelloIDKey(String tempKey) throws Exception;
	public Map<String,Object> selSenderPhoneCheck(String tempKey) throws Exception;
	public Map<String,Object> selTestInfo(String ip) throws Exception;
	public Integer callSendSMS(Map<String, Object> sms) throws Exception;
	public Integer callKakaoTalk(Map<String, Object> sms) throws Exception;
	public Integer callSendSMSDbLink(Map<String, Object> sms) throws Exception;
	public Integer callKakaoTalkDbLink(Map<String, Object> sms) throws Exception;	
}
