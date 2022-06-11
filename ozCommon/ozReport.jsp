<%@ page language="java"
    contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"
    session="false"
%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="org.springframework.web.context.WebApplicationContext"%>
<%@ page import="java.util.*,java.io.*,java.net.*"%> 
<% request.setCharacterEncoding("utf-8"); %>
<% response.setContentType("text/html; charset=utf-8"); %>
<%
	String strContextPath = request.getContextPath();
	//WebApp-Context	
	WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext( request.getSession().getServletContext()); 
	Properties systemProp = (Properties)context.getBean("systemProp");
	String strReportUrl =  systemProp.getProperty("report.domain.path") ;	
		
%>
    
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<HTML>
<HEAD>
    <TITLE>Oz Report</TITLE>
    <META http-equiv=Content-Type content="text/html; charset=utf-8"/>
    <meta http-equiv="Cache-Control" content="no-cache"/> 
    <meta http-equiv="Expires" content="0"/> 
    <meta http-equiv="Pragma" content="no-cache"/> 
    <script type="text/javascript">
    	function openOZReport(){
        	
    		
    		var vsUrl = window.location.href;
    		var vaUrl = vsUrl.split("/");
    		vsUrl = vaUrl[0] + "//" + vaUrl[2] + "/";
    		var vsHostUrl = "<%=strReportUrl%>";
    		
    		var voObj = window.parent.returnValue;
    		//레포트 기본 폼 param
    		var poFormParam      = voObj.poFormParam;
    		//조건 param
    		var poParam          = voObj.poParam;
    		//서브 레포트 기본 폼 param
    		var paChildFormParam = voObj.paChildFormParam;
    		//서브 레포트 조건 param
    		var paChildParam     = voObj.paChildParam;
    		
    		var forms = document.getElementById("ozReport");
			
    		var ozParams = [];
    		
    		//기본 설정
    		ozParams.push("viewer.focus_doc_index=0");			//다중 보고서에서 모든 보고서가 바인딩된 후 화면에 표시할 보고서 인덱스
    		ozParams.push("viewer.showtree=false");				//보고서 바인딩 완료 후 보고서 트리창을 화면에 바로 표시할지 여부
    		ozParams.push("viewer.alldocument=true");
    		//ozParams.push("viewer.viewmode=fittowidth"); 		//보고서 화면의 비율을 너비에 맞추기
    		//ozParams.push("global.concatpage=true");	   //다중 보고서를 하나의 보고서처럼 설정
    		ozParams.push("viewer.isframes=false");
    		ozParams.push("information.debug=true");
    		//ozParams.push("export.applyformat=xls,pdf,doc,ppt,csv,txt,hwp,html"); //출력 파일 포맷 지정
    		ozParams.push("toolbar.addmemo=false");			//메모추가 툴바 숨기기
    		ozParams.push("hdm.charset=unicode");	//툴바 데이터저장(HDM 형식으로 저장)시, 데이터인코딩 설정
    		//ozParams.push("hdm.extension=xls");		//툴바 데이터저장(HDM 형식으로 저장)시, 디폴트 파일확장자 설정
    		ozParams.push("excel.savebyhtml=true");	//엑셀저장시, mht 저장 옵션 체크
    		ozParams.push("viewer.linkcommand=true");	// 2014.03.24 - 레포트 link 기능 추가
    		ozParams.push("viewer.printcommand=true");	// 2015.05.13 - 인쇄 커맨드 기능 추가
    		ozParams.push("viewer.exportcommand=true");	// 저장
    		
    		ozParams.push("viewer.fontdpi=auto");	// 저장
    		
    		ozParams.push("hdm.save_description_as_title=true");//2014.03.25 데이터 저장 시 컬럼 제목을 필드 설명으로 설정
    		//ozParams.push("viewer.emptyframe=true"); //빈 프레임 보이기
    		ozParams.push("viewer.showerrormessage=true"); 
    		ozParams.push("toolbar.addmemo=false"); //메모추가 툴바 숨기기
    		//ozParams.push("viewer.progresscommand=true");
    		
    		//파일저장시 디폴트 파일명에 리포트 타이틀명으로 설정
    		var defaultFileNm = "";
    		if(poFormParam["TITLE"] != undefined && poFormParam["TITLE"] != null && poFormParam["TITLE"] != ""){
    			defaultFileNm = poFormParam["TITLE"];
    			if(defaultFileNm.indexOf("/") != -1){
    				defaultFileNm = defaultFileNm.replace(/[\/]/ig, "");
    			}
    			ozParams.push("export.filename="+defaultFileNm);
    		}
    	

    		//리포트 파일명 설정
    		if(poFormParam["ozreportnm"] == undefined || poFormParam["ozreportnm"] == null || poFormParam["ozreportnm"] == ""){
    			alert("Fail to load ozreportnm" + poFormParam["ozreportnm"]);
    			return;
    		}
    		
    		if(poFormParam["odinm"] == undefined || poFormParam["odinm"] == null || poFormParam["odinm"] == ""){
    			alert("Fail to load odinm" + poFormParam["odinm"]);
    			return;
    		}
    		ozParams.push("connection.reportname="+poFormParam["ozreportnm"]);
    		ozParams.push("odi.odinames="+poFormParam["odinm"]);
    		
    		//뷰어 보기 옵션(normal: 한페이지에 한장씩 보기, singlepagecontinuous: 한페이지에 연속보기)
    		//디폴트는 한페이지에 연속보기임
    		if(poFormParam["pagedisplay"] != undefined && poFormParam["pagedisplay"] != null){
    			ozParams.push("viewer.pagedisplay="+poFormParam["pagedisplay"]);
    		}else{
    			ozParams.push("viewer.pagedisplay=singlepagecontinuous"); 
    		}
    		
    		//저장버튼 보이기/숨김 여부
    		if(poFormParam["savevisible"] != undefined && poFormParam["savevisible"] != null){
    			ozParams.push("toolbar.save="+poFormParam["savevisible"]);
    		}
    		//저장버튼 숨기기(저장버튼 및 )
    		if(poFormParam["DOWNLOAD_YN"] != undefined && poFormParam["DOWNLOAD_YN"] != "Y"){
    			ozParams.push("toolbar.save=false");
    			ozParams.push("toolbar.savedm=false");
    		}
    		if(poFormParam["PRT_YN"] != undefined && poFormParam["PRT_YN"] != "Y"){
				ozParams.push("toolbar.print=false");
			}
    		
    		//저장버튼 숨기기(저장버튼 및 )
    		if(poFormParam["VIEW_PER"] != undefined){
    			ozParams.push("viewer.zoom=" + poFormParam["VIEW_PER"]);
    		}
    		
    		//인쇄버튼 활성화 여부
    		if(poFormParam["printenable"] != undefined && poFormParam["printenable"] != null){
    			ozParams.push("toolbar.print="+poFormParam["printenable"]);
    		}
    		
    		//툴바 데이터저장(HDM 형식으로 저장)시, 디폴트 파일확장자 설정
    		if(poFormParam["hdmextension"] != undefined && poFormParam["hdmextension"] != null){
    			ozParams.push("hdm.extension="+poFormParam["hdmextension"]);
    		}else{
    			ozParams.push("hdm.extension=xls");
    		}
    		
    		//저장 파일 포맷
    		if(poFormParam["exportformat"] != undefined && poFormParam["exportformat"] != null){
    			ozParams.push("export.applyformat="+poFormParam["exportformat"]);
    		}else{
    			ozParams.push("export.applyformat=xls,pdf,doc,ppt,csv,txt,hwp,html,ozd");
    		}
    		
    		// 엑셀저장옵션 : 한페이지로 저장
    		ozParams.push("excel.largebundle=true");
    		
    		//인쇄매수
    		if(poFormParam["printcopy"] != undefined && poFormParam["printcopy"] != null){
    			ozParams.push("print.copies="+poFormParam["printcopy"]);
    		}
    		//한글 저장시  라벨고정 여부
    		if(poFormParam["hwp_treataschar"] != undefined && poFormParam["hwp_treataschar"] == "Y"){
    			ozParams.push("hml.treataschar=true");
    			ozParams.push("hml.keeplabelsize=false");
    		}else{
    			ozParams.push("hml.keeplabelsize=true");
    		}
    		
    		//리포트 인쇄시 로그 남길지여부
    		if(poFormParam["loggingprint"] != undefined && poFormParam["loggingprint"] != null){
    			ozParams.push("viewer.printcommand="+poFormParam["loggingprint"]);
    		}
    		
    		//리포트 저장시 로그 남길지여부
    		if(poFormParam["loggingSave"] != undefined && poFormParam["loggingSave"] != null){
    			ozParams.push("viewer.exportcommand="+poFormParam["loggingSave"]);
    		}
    		
    		if(poFormParam["print_adjust"] != undefined && poFormParam["print_adjust"] == "Y"){
    			ozParams.push("print.adjust=true");	
    		}else{
    			ozParams.push("print.adjust=false");
    		}

    		//보고서 화면의 비율을  설정 ex)viewmode : "fittowidth"(보고서 화면의 비율을 너비에 맞추기)
    		if(poFormParam["viewmode"] != undefined && poFormParam["viewmode"]!= null){
    			ozParams.push("viewer.viewmode="+poFormParam["viewmode"]);	
    		}
    		//양면인쇄 기본설정 ex)duplex : "None"(양면인쇄 없음)
    		//None(양면인쇄 = 없음 )
    		//DuplexHorizontal(양면인쇄 = 짧은 쪽으로 넘김)
    		//DuplexVertical(양면인쇄 = 긴 쪽으로 넘김 )
    		if(poFormParam["duplex"] != undefined && poFormParam["duplex"]!= null){
    			ozParams.push("print.duplex="+poFormParam["duplex"]);	
    		}else{
    			ozParams.push("print.duplex=None");
    		}
    		//폼 파라메터 셋팅
    		var index = 1;
    		for(var param in poFormParam)
    		{	//공백처리
    			if(poFormParam[param] == null || poFormParam[param] == undefined) poFormParam[param] = '';
    		
    			if(!isOzFormParamArgs(param)) continue;
    			
    			ozParams.push("connection.args"+index+"="+param+"="+poFormParam[param]);
    			index++;
    		}
    		if(index > 1){
    			ozParams.push("connection.pcount="+(index-1));
    		}
    		
    		//ODI 파라메터 셋팅
    		var index=1;
    		var vsReqInfo="";
    		for(var param in poParam)
    		{
    			if(poParam[param] == null || poParam[param] == undefined) poParam[param] = '';
    			ozParams.push("odi."+ poFormParam["odinm"] +".args"+index+"="+param+"="+poParam[param]);
    			vsReqInfo+=param+":"+poParam[param]+",";
    			index++;
    		}
    		ozParams.push("odi."+ poFormParam["odinm"] +".pcount="+ (index-1));
    		
    		//자식 리포트 설정
    		var child = "";
    		if(paChildFormParam != undefined && paChildFormParam != null){
    			ozParams.push("viewer.childcount="+paChildFormParam.length);
    			
    			for(var i=0, len=paChildFormParam.length; i<len; i++){
    				child = "child"+(i+1)+".";
    				
    				//child 서버 및 리포트 파일명 셋팅
    				ozParams.push(child+"connection.servlet=<%=strReportUrl %>/server");
    				ozParams.push(child+"connection.reportname="+paChildFormParam[i]["ozreportnm"]);
    				ozParams.push(child+"odi.odinames="+paChildFormParam[i]["odinm"]);
    				
    				//폼 파라메터 셋팅
    				var index = 1;
    				for(var param in paChildFormParam[i])
    				{	//공백처리
    					if(paChildFormParam[i][param] == null || paChildFormParam[i][param] == undefined) paChildFormParam[i][param] = '';
    				
    					if(!isOzFormParamArgs(param)) continue;
    					
    					ozParams.push(child+"connection.args"+index+"="+param+"="+paChildFormParam[i][param]);
    					index++;
    				}
    				
    				if(index > 1){
    					ozParams.push(child+"connection.pcount="+(index-1));
    				}
    				
    				//ODI 파라메터 셋팅
    				var index=1;
    				for(var param in paChildParam[i])
    				{
    					if(paChildParam[i][param] == null || paChildParam[i][param] == undefined) paChildParam[i][param] = '';
    					ozParams.push(child+"odi."+ paChildFormParam[i]["odinm"] +".args"+index+"="+param+"="+paChildParam[i][param]);

    					index++;
    				}
    				ozParams.push(child+"odi."+ paChildFormParam[i]["odinm"] +".pcount="+ (index-1));
    			}
    		}
    		
			//출력물 파라메터 넘기기 위한 hidden 엘리먼트 추가
			for ( var j = 0; j< ozParams.length; j++ ){

				var inputField = document.createElement("input");
				inputField.setAttribute("type", "hidden" );
				inputField.setAttribute("name", "viewerParameter" );
				inputField.setAttribute("value", ozParams[j] );
				forms.appendChild(inputField);
		    }
			
    		var frm = document.ozReport;
    		window.parent.returnValue = null;	//초기화
            
          	//증명서등 출력물 인쇄, 저장시... 인쇄 액션에 대한 로그(Log) 저장용 파라메터
			var PRINT_LOG = {
				  url		 : "/cmn/cmnReport/insCmnDownLog.do"
				, strUserId 	: poFormParam["USER_ID"]
				, TEMP_MENU_ID 	: poFormParam["MENU_ID"]
				, TEMP_PGM_ID 	: poFormParam["PGM_ID"]
				, strReqInfo    : vsReqInfo
				, strFileNm     : defaultFileNm
			}
    		
          	
			var poPrintLogParams = PRINT_LOG;
			if(poPrintLogParams != undefined && poPrintLogParams != null){
				for(var param in poPrintLogParams){
					var inputField = document.createElement("input");
					inputField.setAttribute("type", "hidden" );
					inputField.setAttribute("name", "printLogParameter" );
					
					if(param == "url"){
						inputField.setAttribute("value", param+"="+vsUrl+poPrintLogParams[param] );
					}else{
						inputField.setAttribute("value", param+"="+poPrintLogParams[param] );
					}
					
					forms.appendChild(inputField);
				}
			}
			
			
			if(poFormParam["FORM_TYPE"] != undefined && poFormParam["FORM_TYPE"] != null && poFormParam["FORM_TYPE"] != ""){
				if("HTML" == poFormParam["FORM_TYPE"].trim().toUpperCase())
				{
					frm.action = vsHostUrl + "report/ozhviewer/ozhViewer.jsp";
				}
				else if("ACTIVE" == poFormParam["FORM_TYPE"].trim().toUpperCase())
				{
					frm.action = vsHostUrl + "report/ozrviewer/ozrViewer.jsp";
				}
				else if("EXE" == poFormParam["FORM_TYPE"].trim().toUpperCase())
				{
					frm.action = vsHostUrl + "report/ozeviewer/ozeViewer.jsp";
				}
				else
				{
					frm.action = vsHostUrl + "report/ozfviewer/ozfViewer.jsp";
					//맑은고딕 폰트 문제로 html 버젼이 기본으로 지정
					//frm.action = vsHostUrl + "report/ozhviewer/ozhViewer.jsp";					
				}
					
			}else{
				frm.action = vsHostUrl + "report/ozfviewer/ozfViewer.jsp";
				//맑은고딕 폰트 문제로 html 버젼이 기본으로 지정
				//frm.action = vsHostUrl + "report/ozhviewer/ozhViewer.jsp";
			}
	
	
//			if(poFormParam["FORM_TYPE"] != undefined && poFormParam["FORM_TYPE"] != null && poFormParam["FORM_TYPE"] != ""){
//				if("HTML" == poFormParam["FORM_TYPE"].trim().toUpperCase())
//					frm.action = vsHostUrl + "oz70/ozhviewer/ozhViewer.jsp";
//				else if("ACTIVE" == poFormParam["FORM_TYPE"].trim().toUpperCase())
//					frm.action = vsHostUrl + "oz70/ozrviewer/ozrViewer.jsp";
//				else if("EXE" == poFormParam["FORM_TYPE"].trim().toUpperCase())
//					frm.action = vsHostUrl + "oz70/ozeviewer/ozeViewer.jsp";
//				else
//					frm.action = vsHostUrl + "oz70/ozfviewer/ozfViewer.jsp";
//			}else{
//				frm.action = vsHostUrl + "oz70/ozfviewer/ozfViewer.jsp";
//			}
		
    		frm.submit();
    	}
    	
    	function isOzFormParamArgs(args){
    		if(args == null || args == "") return false;
    		
    		if(args == "ozreportnm") return false;
    		else if(args == "odinm") return false;
    		else if(args == "pagedisplay") return false;
    		else if(args == "loggingprint") return false;
    		else if(args == "loggingprint_params") return false;
    		else if(args == "printenable") return false;
    		else if(args == "displayprintwindow") return false;
    		else if(args == "printcopy") return false;
    		else if(args == "savevisible") return false;
    		else if(args == "exportformat") return false;
    		else if(args == "hwp_keeplabelsize") return false;
    		else if(args == "PSN_INFO_ACCESS_LOG") return false;
    		/*else if(args == "viewer.focus_doc_index") return false;
    		else if(args == "viewer.childcount") return false;
    		else if(args == "viewer.showtree") return false;
    		else if(args == "viewer.alldocument") return false;
    		else if(args == "global.concatpage") return false;
    		else if(args == "connection.fetchtype") return false;
    		else if(args == "connection.compresseddatamodule") return false;
    		else if(args == "child") return false;
    		*/
    		
    		return true;
    	}
    </script>
</HEAD>
<BODY hight="100" with ="100" bgColor=white leftMargin=0 topMargin=0 marginwidth="0" marginheight="0" onload="openOZReport();">
	<form name=ozReport id=ozReport method="post">
	</form>
</BODY>
</HTML>