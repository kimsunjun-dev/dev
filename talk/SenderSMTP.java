package org.tmt.core.link.mail;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;


public class SenderSMTP {

    private String body;
    private String cc;
    private String host;
    private String recipient;
    private String sender;
    private String subject;
    private String senderName; 
    private String recipientName;
    private String attachFileFullPath;
    private String attachFileViewName;
    
    /**
     * host attribute setter method.
     */
    public void setHost(String host) {
    	this.host = host;
    }
    
    /**
     * recipient attribute setter method.
     */
    public void setRecipient(String recipient) {
    	this.recipient = recipient;
    }
    
    public void setRecipientName(String recipientName) {
    	this.recipientName = recipientName;
    }

    /**
     * sender attribute setter method.
     */
    public void setSender(String sender) {
    	this.sender = sender;
    }
    
    /**
     * sender attribute setter method.
     */
    public void setSenderName(String senderName) {
    	this.senderName = senderName;
    }
    
    public void setAttachFileFullPath(String attachFileFullPath)
    {
    	this.attachFileFullPath = attachFileFullPath;
    }
    public void setAttachFileViewName(String attachFileViewName)
    {
    	this.attachFileViewName = attachFileViewName;
    }
    
    
    
    /**
     * cc attribute setter method.
     */
    public void setCc(String cc) {
		this.cc = cc;
    }

    /**
     * subject attribute setter method.
     */
    public void setSubject(String subject) {
    	this.subject = subject;
    }
    
    /**
     * body attribute setter method.
     */
    public void setBody(String body) {
    	this.body = body;
    }
    
	public void send() throws Exception
	{
	    ByteArrayOutputStream os = new ByteArrayOutputStream();
	    PrintStream ps = new PrintStream(os);
	    
	    try {
	    	
			//final String username = "cic";
			//final String password = "2401";
			
			Properties props = System.getProperties();
		    props.put("mail.transport.protocol", "smtp");
		    props.put("mail.smtp.host", "mail.ok.ac.kr");
		    props.put("mail.smtp.port", "25");
		    props.put("mail.debug", "true");
		    
		    
		    Session session = Session.getDefaultInstance(props, null);
		    
//		    Session session = Session.getDefaultInstance(props, 		  
//		    	new javax.mail.Authenticator() {
//				protected PasswordAuthentication getPasswordAuthentication() {
//					return new PasswordAuthentication(username, password);
//				}
//			  });
		    
		    //session.setDebug(true);
		    //session.setDebugOut(ps);
		    
		    Message msg = new MimeMessage(session);
	
		    InternetAddress fromAddr = new  InternetAddress(this.sender, MimeUtility.encodeText(this.senderName,"UTF-8","B"), "UTF-8");
		    InternetAddress toAddr = new  InternetAddress(this.recipient, MimeUtility.encodeText(this.recipientName,"UTF-8","B"), "UTF-8");
	
		    msg.setHeader("content-type", "text/plain;charset=utf-8");
	
		    msg.setFrom(fromAddr);
		    msg.setRecipient(Message.RecipientType.TO, toAddr);
	
		    if (this.subject != null)
		    {
			    msg.setSubject(MimeUtility.encodeText(this.subject,"utf-8","B"));
		    }
	
		    Multipart mp = new MimeMultipart();

		    
		    if (body != null)
		    {	
			    MimeBodyPart contentsPart = new MimeBodyPart();
			    contentsPart.setContent(this.getContent(body),"text/html; charset=utf-8");
			    mp.addBodyPart(contentsPart);
		    }
		    
		    if(attachFileFullPath != null)
		    {
		    	if(new File(attachFileFullPath).exists())
		    	{
		    		MimeBodyPart filePart = new MimeBodyPart();
		    		FileDataSource fds = new FileDataSource(attachFileFullPath);
		    		filePart.setDataHandler(new DataHandler(fds));
		    		filePart.setFileName(MimeUtility.encodeText(attachFileViewName, "utf-8", "B"));
		    		mp.addBodyPart(filePart);
		    	}
		    }
		    
		    msg.setContent(mp, "text/html;charset=utf-8");
		    msg.setSentDate(new java.util.Date());
		    Transport.send(msg);
	    }
	    finally {
	      ps.close();
	      os.close();
	    }
	    
	}
	
	private String getContent(String body)
	{
		// 메일 내용
		String content = "<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Transitional//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd'>";
		content += "<html>";
		content += "<head>";
		content += "<meta http-equiv='X-UA-Compatible' content='IE=edge'/>";
		content += "<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'/>";
		content += "<title>대학교</title>";
		content += "<style type='text/css'>";
		content += "a:hover {";
		content += "	text-decoration: underline !important";
		content += "}";
		content += "</style>";
		content += "</head>";
		content += "<body style='background:#fff;margin:0 0 0 0;font-size:12px;font-family:Dotum;color:#333333'>";
		content += "<table width='700' cellspacing='0' cellpadding='0' border='0' summary='전체레이아웃'>";
		content += "<tr>";
		content += " <td width='100%' bgcolor='#dddddd' style='padding:1px'>"; 
		content += "  <!-- 메일 테이블 영역 시작 -->";
		content += "  <table width='100%' cellspacing='0' cellpadding='0' border='0' summary='이메일본테이블시작'>";
		content += "    <tr>";
		content += "      <td bgcolor='#ffffff'> ";
		content += "        <!-- 상단 시작 -->";
		content += "        <table width='100%' cellspacing='0' cellpadding='0' border='0' bgcolor='#ffffff' summary='상단테이블'>";
		content += "          <tr>";
		content += "           <td bgcolor='#2f62ad' height='6'></td>";
		content += "         </tr>";
		content += "          <tr>";
		content += "            <td bgcolor='#ffffff'>";
		content += "             <table width='100%' cellspacing='0' cellpadding='0' border='0' summary='하단테이블'>";
		content += "                <tr>";
		content += "                 <td style='padding:5px 5px 5px 5px;'><a href='https://socuri.ok.ac.kr' target='_blank'><img src='https://cwuis.ok.ac.kr/images/mail/ccuis_logo.png' width='157' height='29' alt='대학교' border='0' /></a></td>";
		content += "               </tr>";
		content += "             </table>";
		content += "           </td>";
		content += "         </tr>";
		content += "         <tr>";
		content += "            <td bgcolor='#adadad' height='1'></td>";
		content += "          </tr>";
		content += "        </table>";
		content += "        <!-- 상단 끝 --> ";
		content += "      </td>";
		content += "   </tr>";
		content += "   <tr>";
		
		content += "	<td bgcolor='#ffffff' align='center'>";
		content += "	<!-- 메일 본문 컨텐츠 시작 -->";
		content += "	<table width='100%' cellspacing='0' cellpadding='0' border='0' summary='컨텐츠' style='margin:10px 40px 0 10px'>";
		content += "  	<tr>";
		content += "    	<td>";
		content += "     	 <table width='100%' cellspacing='0' cellpadding='0' border='0' summary='안내메일' style='padding:10px 0 20px 10px'>";
		content += "        	<tr>";
		content += "          	<td width='100%' align='left'>";
		content += "            <table width='100%' cellspacing='0' cellpadding='0' border='0' style='font-size:13px;line-height:22px;font-family:Dotum'>";
		content += "                     <tr>";
		content += "                     </tr>";
		content += "                     <tr>";
		content += "                       <td style='padding-bottom:16px'>";
		content += "                       <pre>";
		content += body;
		content += "                       </pre>";
		content += "                        </td>";
		content += "                      </tr>";
		content += "                    </table>";
		content += "                  </td>";
		content += "               </tr>";
		content += "             </table>";
		content += "           </td>";
		content += "         </tr>";
		content += "        <tr>";
		content += "          <td height='100'></td>";
		content += "        </tr>";
		content += "      </table>";
		content += "      <!-- 메일 본문 컨텐츠 끝 --> ";
		content += "    </td>";
		content += "  </tr>";
		content += "   <tr>";
		content += "     <td bgcolor='#d4d2d2' height='1'></td>";
		content += "  </tr>";
		content += "  <tr>";
		content += "    <td> ";
		content += "       <!-- 하단 시작 -->";
		content += "      <table width='100%' height='60' cellspacing='0' cellpadding='0' border='0' summary='하단테이블'>";
		content += "        <tr>";
		content += "         <td bgcolor='#f8f8f8' style='padding:20px;line-height:18px;color:#777777;font-size:11px;font-family:Dotum'>";
		content += "          Copyright(c) 2018 Chung Cheong University. All Rights Reserved. </td>";
		content += "       </tr>";
		content += "        <tr>";
		content += "          <td bgcolor='#dddddd' height='1'></td>";
		content += "        </tr>";
		content += "      </table>";
		content += "    <!-- 하단 끝 --> ";
		content += "    </td>";
		content += "    </tr>";
		content += "  </table>";
		content += "  <!-- 메일 테이블 영역 끝 --> ";
		content += "</td>";
		content += "</tr>";
		content += "</table>";
		content += "</body>";
		content += "</html>";
			
		return content;
	}
}
