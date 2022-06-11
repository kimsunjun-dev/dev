<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import = "com.hr.common.util.StringUtil" %>
<%@ include file="/WEB-INF/jsp/common/include/taglibs.jsp"%>   
<!DOCTYPE html> <html class="hidden"><head>

<spring:eval var="rdUrl" expression="@opti.getProperty('rd.url')"/>
<spring:eval var="rdRsn" expression="@opti.getProperty('rd.servicename')"/>
<spring:eval var="rdMrd" expression="@opti.getProperty('rd.mrdUrl')"/>

<%
String baseRdPath 		= "/html/report/" ;
%>
<script src="${rdUrl}/ReportingServer/html5/js/jquery-1.11.0.min.js"></script>
<script src="${rdUrl}/ReportingServer/html5/js/crownix-viewer.min.js"></script>
<link rel="stylesheet" type="text/css" href="${rdUrl}/ReportingServer/html5//css/crownix-viewer.min.css">

</head>


<body style="margin:0;height:100%">
<div id="crownix-viewer" style="position:absolute;width:100%;height:100%"></div>
<script type="text/javascript">


 window.onload = function(){
	
	var rdUrl = "${rdUrl}" ;
	var rsn = "${rdRsn}" ;

	var rdAgent = "/DataServer/rdagent.jsp" ;
	
	if( !checkParam() ) return ;
	
	var	param  = "/rfn ["+rdUrl+rdAgent+"] /rsn ["+rsn+"] /rreportopt [256] /rmmlopt [1] ";
	param  = param +"/" + $("#ParamGubun", parent.document).val() + " ";//rp또는 rv로 넘어온다.

	reportFileNm= "${rdMrd}<%=baseRdPath%>" + $("#Mrd", parent.document).val();//루트를 제외한 RD경로 및 파일명 매칭
	param		= param + $("#Param", parent.document).val(); //파라매터 넘김
	
	setRdSessionInfo() ;
	
	/* 파라미터 변조 체크를 위한 securityKey 를 파라미터로 전송 함 */
	if( $("#ParamGubun", parent.document).val() == "rp" ){
		param =  param + "[${securityKey}] /rv securityKey[${securityKey}]";
	}else{ //rv
		param =  param + " securityKey[${securityKey}]";
	}

	mrd			= reportFileNm;

	var viewer = new m2soft.crownix.Viewer('${rdUrl}/ReportingServer/service', 'crownix-viewer');
	
	//툴바 item hide 처리
	if($("#ToolbarYn", parent.document).val() != "Y") {
		viewer.hideToolbar();//툴바 전체 비활성화 - 대소문자 주의!
    }
	
	var hideItem = toolbarItem();
	for (var i = 0, len = hideItem.length; i < len; i++) {
		viewer.hideToolbarItem (["ratio",hideItem[i]]);
		if(hideItem[i] == "print"){
			viewer.hideToolbarItem (["ratio","print_pdf"]);	
		}
	}

	viewer.openFile(mrd, param, {timeout:3000});
	
}

 window.onbeforeunload = function(e) {
		if( parent.returnResult != null ) {
			parent.returnResult() ;
		}
};

function toolbarItem() {
	// 툴바 hide 
	var hideItem 	= new Array();
	var itemNum     = 0;
	
	var array=[
		{name:'SaveYn',     type:'save',		defaultShowYn:'Y'},	//기능컨트롤_저장
		{name:'PrintYn',    type:'print',		defaultShowYn:'Y'},	//기능컨트롤_인쇄
		{name:'PrintPdfYn', type:'print_pdf',	defaultShowYn:'Y'},	//기능컨트롤_PDF인쇄
		{name:'ExcelYn',    type:'xls',			defaultShowYn:'Y'},	//기능컨트롤_엑셀
		{name:'WordYn',     type:'doc',			defaultShowYn:'Y'},	//기능컨트롤_워드
		{name:'PptYn',      type:'ppt',			defaultShowYn:'Y'},	//기능컨트롤_파워포인트
		{name:'HwpYn',      type:'hwp',			defaultShowYn:'Y'},	//기능컨트롤_한글
		{name:'PdfYn',      type:'pdf',         defaultShowYn:'Y'}	//기능컨트롤_PDF
	];

	for(var i=0;i<array.length;i++){
		if ( ( $("#"+array[i].name, parent.document).length > 0 && $("#"+array[i].name, parent.document).val() == "N" ) || 
			 ( $("#"+array[i].name, parent.document).length > 0 && $("#"+array[i].name, parent.document).val() != "Y" && array[i].defaultShowYn == "N") || 
			 ( $("#"+array[i].name, parent.document).length < 1 && array[i].defaultShowYn == "N") ){
			hideItem[itemNum] = array[i].type;
			itemNum ++;
		}
	}
	return hideItem;
}

function checkParam() {
	if( !( $("#ParamGubun", parent.document).val() == "rp" || $("#ParamGubun", parent.document).val() == "rv" || $("#ParamGubun", parent.document).val() == "" ) ) {
		alert("개발오류입니다.\n파라매터 중 rdParamGubun값은 rp또는rv이어야 합니다.") ;
		return false ;
	} else if( $("#ParamGubun", parent.document).val() == "" ) { $("#ParamGubun", parent.document).val("rp") ; }//값이 넘어오지 않으면 기본 rp

	return true ;
}

function setRdSessionInfo() {
    /*
	사진을 보려면 Plugin Viewer에서는 세션유지를 못하는 버그가 있어
	해당사항을 우리 소스에서 해결하는 코드 : sessionParam
	*/
	var sessionParam = "" ;
	$("#ParamGubun", parent.document).val() == "rv" ? sessionParam = " NgmSsoName[JSESSIONID] NgmSsoData["+"<%=(String)session.getId()%>"+"]" : sessionParam = "/rv NgmSsoName[JSESSIONID] NgmSsoData["+"<%=(String)session.getId()%>"+"]" ;
	
	/* 파라미터 변조 체크를 위한 securityKey 를 파라미터로 전송 함*/
	sessionParam =  sessionParam + " securityKey[${securityKey}]";
	
	$("#Param", parent.document).val( $("#Param", parent.document).val() + sessionParam ) ;
}
	
function setRdMenu() {
    if($("#ToolbarYn", parent.document).val() != "Y") {
    	rdViewer.HideToolBar();//툴바 전체 비활성화 - 대소문자 주의!
    	rdViewer.HidePopupMenu(0);//컨텍스트 메뉴 전체 비활성화
    }

    if($("#SaveYn", parent.document).val() != "Y") {
        rdViewer.DisableToolbar(0);
        rdViewer.HidePopupMenu(4) ;
        rdViewer.HidePopupMenu(9) ;
        rdViewer.HidePopupMenu(8) ;
    }
    
    if($("#PrintYn", parent.document).val() != "Y") {
        rdViewer.DisableToolbar(1);
        rdViewer.HidePopupMenu(3) ;
    }
    
    if($("#ExcelYn", parent.document).val() != "Y") { rdViewer.DisableToolbar(13); }
    if($("#WordYn", parent.document).val() != "Y") { rdViewer.DisableToolbar(17); }
    if($("#PptYn", parent.document).val() != "Y") { rdViewer.DisableToolbar(16); }
    if($("#HwpYn", parent.document).val() != "Y") { rdViewer.DisableToolbar(14); }
    if($("#PdfYn", parent.document).val() != "Y") { rdViewer.DisableToolbar(15); }
    
	rdViewer.AutoAdjust = false;
	if($("#ZoomRatio", parent.document).val() != "") { rdViewer.ZoomRatio = Number( $("#ZoomRatio", parent.document).val() ) ; } 
	else {	rdViewer.ZoomRatio = 100 ; }
	rdViewer.SetBackgroundColor(255,255,255);
}
//IE외 브라우져 최종 인쇄버튼 누를 시 발생
function PrintFinished() {
	$("#printResultYn", parent.document).val("Y") ;
	if( parent.returnResult != null ) {
		parent.returnResult() ;
	}
}
function FileOpenFinished(){
	if ( $("#AutoPrintYn", parent.document).val() == "Y" ) {
        rdViewer.CMPrint();
    }
}
</script>
</body>

</HTML>




