package org.tmt.core.service.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.tmt.core.constants.Alert;
import org.tmt.core.exception.AppException;
import org.tmt.core.mapper.SqlMapper;
import org.tmt.core.service.AbstractBaseService;
import org.tmt.core.service.AppMessageService;
import org.tmt.core.service.LinkLetterService;
import org.tmt.core.util.Util;


@Service
public class LinkLetterServiceImpl extends AbstractBaseService implements LinkLetterService {

//	@Resource(name = "sqlSMSMapper")
//	protected SqlMapper sqlSMSMapper;
	@Resource(name = "sqlMapper")
	protected SqlMapper sqlMapper;
	
	/**
	 * 메시지 폼 로드
	 */
	@Override
	public Map<String,Object> selLoadLetterForm(String letterFormId) throws Exception
	{
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("MSG_ID", letterFormId);

		Map rsMap;
		
		try {
			rsMap = (Map)sqlMapper.selectOneClobToString("cmn-0005", "selLoadLetterForm", map);
		} catch (Exception e) {
			String[] msgValues = {letterFormId};
			
			// 메일/SMS/PUSH에서 사용되는 메시지 본문이 정의 되지 않았습니다. 본문아이디【{0}】
			throw new AppException(Alert.ERROR, "CMN003.CMN@CMN043", msgValues);
		}
		
		if(rsMap == null)
		{
			String[] msgValues = {letterFormId};
			
			// 메일/SMS/PUSH에서 사용되는 메시지 본문이 정의 되지 않았습니다. 본문아이디【{0}】
			throw new AppException(Alert.ERROR, "CMN003.CMN@CMN043", msgValues);
		}
		else
		{
			return rsMap;
		}
	}
	
	
	@Override
	public Map<String,Object> selSenderPhoneCheck(String tempKey) throws Exception
	{
		Map paramMap = new HashMap();
		paramMap.put("AEN", tempKey);
		
		return (Map)sqlMapper.selectOne("cmn-0005", "selSenderPhoneCheck", paramMap);
	}
	
	@Override
	public String getLetterSndNo() throws Exception
	{
		Map rsMap = (Map)sqlMapper.selectOne("cmn-0005", "getLetterSndNo");
		return (String)rsMap.get("SND_NO");
	}
	
	public  Map<String,Object> selSenderYelloIDKey(String tempKey) throws Exception
	{
		Map paramMap = new HashMap();
		paramMap.put("TEMPLATE_CD", tempKey);
		
		return (Map)sqlMapper.selectOne("cmn-0005", "selSenderYelloIDKey", paramMap);
	}	
	
	
	@Override
	public Map<String,Object> selTestInfo(String ip) throws Exception
	{
		Map paramMap = new HashMap();
		paramMap.put("IP", ip);
		
		List infoList = sqlMapper.selectList("cmn-0005", "selTestInfo", paramMap);
		Map data = new HashMap();
		
		if(infoList.size() > 0)
		{
			data = (Map)infoList.get(0);
		}
		
		return data;
	}
	
	@Override
	public Integer insLetterSender(Map<String, Object> sender) throws Exception
	{
		return sqlMapper.insert("cmn-0005", "insLetterSender", sender);
	}
	
	@Override
	public Integer insLetterReceiver(Map<String, Object> receiver) throws Exception
	{
		return sqlMapper.insert("cmn-0005", "insLetterReceiver", receiver);
	}
	
	
	@Override
	public Integer callSendSMS(Map<String, Object> sms) throws Exception
	{
		return sqlMapper.insert("cmn-0005", "insSureMMessageLink", sms);		
	}
	
	@Override
	public Integer callKakaoTalk(Map<String, Object> talk) throws Exception
	{
		return sqlMapper.insert("cmn-0005", "insSureMMessageLink", talk);	
	}	
	
	@Override
	public Integer callSendSMSDbLink(Map<String, Object> sms) throws Exception
	{
		return sqlMapper.insert("cmn-0005", "insSureMMessageLinkDbLink", sms);		
	}
	
	@Override
	public Integer callKakaoTalkDbLink(Map<String, Object> talk) throws Exception
	{
		return sqlMapper.insert("cmn-0005", "insSureMMessageLinkDbLink", talk);	
	}	
	
}
