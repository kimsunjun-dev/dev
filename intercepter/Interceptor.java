package com.hr.common.interceptor;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.hr.common.exception.IBSheetException;
import com.hr.common.logger.Log;
import com.hr.common.loginSessionListener.LoginSessionListener;
import com.hr.common.security.SecurityMgrService;
import com.hr.common.util.ParamUtils;
import com.hr.common.util.SessionUtil;
import com.hr.common.util.StringUtil;

/**
 * HandlerMapping 된 Controller로 가기전후의 추가 적인작업
 *
 */
public class Interceptor extends HandlerInterceptorAdapter {


	@Inject
	@Named("SecurityMgrService")
	private SecurityMgrService securityMgrService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		// form에서 넘어오는 데이터가 get인지 post인지 확인
		if(request.getMethod().toUpperCase().equals("GET")){
			Log.Debug("GET");
		}
		else{
			Log.Debug("POST");
		}

		Log.Debug("■■■■■■■■■■■■■■■■■■■■■■■ Servlet Start ■■■■■■■■■■■■■■■■■■■■■■■");
		Log.Debug("■■■■■■■■■■ sessionId : " +request.getRequestedSessionId());
		Enumeration<?> enumsHeader = request.getHeaderNames();
		String header = "";
		while (enumsHeader.hasMoreElements()) {
			header = (String) enumsHeader.nextElement();
			Log.Debug("[HEADER]" + header + " : " + request.getHeader(header));
		}
		String baseUrl = StringUtil.getBaseUrl(request)+StringUtil.getRelativeUrl(request);
		Log.Debug("[REQUEST]relativeUrl:" 		+ StringUtil.getRelativeUrl(request));
		Log.Debug("[REQUEST]baseUrl:" 		+ baseUrl);
		Log.Debug("[REQUEST]ContentLength:" + request.getContentLength());
		Log.Debug("[REQUEST]LocalName:" 	+ request.getLocalName().toString());
		Log.Debug("[REQUEST]Method:" 		+ request.getMethod().toString());
		Log.Debug("[REQUEST]Protocol:" 		+ request.getProtocol());
		Log.Debug("[REQUEST]Class:" 		+ request.getClass());
		Log.Debug("[REQUEST]Scheme:" 		+ request.getScheme());
		Log.Debug("[REQUEST]RemoteHost:" 	+ request.getRemoteHost() + ":" + request.getRemotePort());
		Log.Debug("[REQUEST]RequestURL:" 	+ request.getRequestURL() + (request.getQueryString() != null  ? "?" + request.getQueryString(): "")) ;
		Log.Debug("[REQUEST]Session:" 		+ request.getSession(false));
		Log.Debug("[REQUEST]Locales:" 		+ request.getLocale());
		Log.Debug("[MAPPING]Controller:"	+ handler.getClass().getCanonicalName());
		Method m[] = handler.getClass().getDeclaredMethods();
		for (int i = 0; i < m.length; i++) {
			Log.Debug("[MAPPING]Method:"+m[i].toGenericString());
		}
		Log.Debug("[MAPPING]IBUserAgent:"	+ request.getHeader("IBUserAgent"));
		String controllerName = handler.getClass().getSimpleName();
		HttpSession session	= request.getSession(false);

		//로그인컨트롤러가 아닌경우
		String ssnSabun = "";
		String passKey  = "N";
		if(!controllerName.equals( "LoginController")
		&& !controllerName.equals( "PrivacyAgreementController") // 개인정보보호법동의
		&& !(controllerName.equals( "OtherController") && "/Popg.do".equals(StringUtil.getRelativeUrl(request))) // 비밀번호찾기
		&& !(controllerName.equals( "SecurityMgrController") && !"/getDecryptUrl.do".equals(StringUtil.getRelativeUrl(request))) // 보안 체크
		){

			Log.Debug("■ session : " +request.getRequestedSessionId());
			Log.Debug("■ ssnSabun : " +session.getAttribute("ssnSabun"));
			if (session == null || session.getAttribute("ssnSabun") == null ) {
				Log.Debug("■ 중복로그인으로 세션이 끊어졌는지 체크 --->");
				Log.Debug("■ getRequestedSessionId:" + request.getRequestedSessionId());
				Map<String, Object> paramMap = new HashMap<String, Object>();
				paramMap.put("jsessionid", request.getRequestedSessionId());
				paramMap.put("ip", StringUtil.getClientIP(request));
				Map<String, Object> rsMap = (Map<String, Object>)securityMgrService.getLogoutDup(paramMap); // 중복로그인 여부 조회
				Log.Debug("■■■■■ rsMap:" + rsMap);
				if( rsMap != null && "Y".equals((String)rsMap.get("logoutDup"))){
					response.sendRedirect("/Info.do?code=992");
					return false;
				}
			}

			// session검사
			if (session == null) {
				response.sendRedirect("/Info.do?code=905");
				return false;
			}
			ssnSabun 	= (String)session.getAttribute("ssnSabun");
			if (ssnSabun == null) {
				response.sendRedirect("/Info.do?code=905");
				return false;
			}

			//실제로 세션이 존재 하는지 체크
			LoginSessionListener lsl = new LoginSessionListener();
			lsl.printSessionList();

			if( lsl.getSession( session.getId() ) == null && !"/Main.do".equals(StringUtil.getRelativeUrl(request)) ){
				response.sendRedirect("/Info.do?code=905");
				return false;
			}

			/*■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■*/
			// 시스템 보안체크 여부에 따라 보안체크
			/*■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■*/
			String ssnSecurityYn = (String)session.getAttribute("ssnSecurityYn");
			String ssnSecurityDetail = (String)session.getAttribute("ssnSecurityDetail");
			Log.Debug("■ SSN_SECURITY_YN:" + ssnSecurityYn);
			Log.Debug("■ SSN_SECURITY_DETAIL:" + ssnSecurityDetail);
			if( ssnSecurityYn != null && ssnSecurityYn.equals("Y")
			  && ( "|CommonCodeController|HelpPopupController|").indexOf(controllerName) < 0   //공통코드 제외, 도움말 공통 팝업  제외
              ){
				Log.Debug("■■■■■■■■■■■■■■■■■ [ 보안 체크 시작 ]■■■■■■■■■■■■■■■■■■■■■■■■■■");
				String chkCode = securityCheck(request);
				Log.Debug("■ [ code: "+chkCode+" ] ");
				if( !chkCode.equals("") ){

					securityError(request, response, isIBSheet, chkCode);

					return false;
				}
				Log.Debug("■■■■■■■■■■■■■■■■■[ 보안 체크 종료 ]■■■■■■■■■■■■■■■■■■■■■■■■■");
			}
			/*■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■*/

		}else{
			//로그인컨트롤러 인경우에 선처리
			passKey = "Y";
		}
		//requestParam View
		ParamUtils.viewParam(request);
		//세션이 생성된 상태에서 로그용 데이터 생성
		Log.Debug("[ 확인0[session] =]" + session);

		if (session != null){
			ssnSabun 	= (String)session.getAttribute("ssnSabun");
			if(ssnSabun != null){
				SessionUtil.setRequestAttribute("logIp", request.getRemoteAddr());
				SessionUtil.setRequestAttribute("logRequestUrl", request.getRequestURI()  + (request.getQueryString() != null  ? "?" + request.getQueryString(): ""));
				SessionUtil.setRequestAttribute("logController", controllerName);
			}


		}else{
			request.setAttribute("logIp", 			InetAddress.getLocalHost().getHostAddress());
			request.setAttribute("logRequestUrl", 	request.getRequestURI() + (request.getQueryString() != null  ? "?" + request.getQueryString(): ""));
			request.setAttribute("logController", 	controllerName);
		}


		/// Referer
		String baseRep = request.getScheme() + "://" + request.getServerName()+ ":" + request.getServerPort() ;
		Log.Debug("[ 확인logPage[baseRep] =]" + baseRep);

		String referrer = request.getHeader("Referer");
		if(referrer!=null){
			referrer = referrer.replaceAll(baseRep, "") ;
		}

		if (session != null){
			request.getSession(false).setAttribute("logReferer", referrer);
			Log.Debug("[ 확인logPage[logPage] =]" + (String) session.getAttribute("logReferer"));
		}

		//처리 시간 계산
		long startTime = System.currentTimeMillis();
		request.setAttribute("startTime", startTime);
		return super.preHandle(request, response, handler);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.springframework.web.servlet.handler.HandlerInterceptorAdapter#postHandle
	 * (javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse, java.lang.Object,
	 * org.springframework.web.servlet.ModelAndView)
	 */
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		// TODO Auto-generated method stub
		long startTime = (Long)request.getAttribute("startTime");
		long endTime = System.currentTimeMillis();
		long executeTime = endTime - startTime;

		Log.Debug("[" + handler + "] executeTime : " + executeTime + "ms");
		super.postHandle(request, response, handler, modelAndView);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.web.servlet.handler.HandlerInterceptorAdapter#
	 * afterCompletion(javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse, java.lang.Object,
	 * java.lang.Exception)
	 */
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		Log.Debug("■■■■■■■■■■■■■■■■■■■■■■■ Servlet End ■■■■■■■■■■■■■■■■■■■■■■■");
	}



	/**
	 * 보안에러 저장
	 * @param request
	 * @param errCode
	 */
	public void securityError(HttpServletRequest request, HttpServletResponse response, boolean isIBSheet, String code) throws Exception {

		String msg = "Error";

		HttpSession session = request.getSession(false);
		String ssnEnterCd = (String)session.getAttribute("ssnEnterCd");
		String errorUrl = (String)session.getAttribute("errorUrl");
		String ssnGrpCd = (String)session.getAttribute("ssnGrpCd");
		String ssnSabun = (String)session.getAttribute("ssnSabun");

		/**  관리자가 사용자변경으로 로그인 했을 때
		 **/
		String ssnAdminSabun = (String)session.getAttribute("ssnAdminSabun");
		String msg0 = "";
		if( ssnAdminSabun != null && ssnSabun != null && !ssnSabun.equals(ssnAdminSabun)){
			msg0 = "[A:"+ssnAdminSabun+"], ";
		}
		Log.Debug("■ ssnAdminSabun : " +ssnAdminSabun );
		Log.Debug("■ msg0 : " +msg0 );


		/*
		 * 990 : 로그인 정보가 DB에 존재하지 않음
		 * 991 : 세션 변조
		 * 992 : 중복 로그인
		 * 993 : 파라미터 변조
		 * 994 : 권한 없는 화면 접근
		 * 995 : URL 직접 접근
		 */
		if( code.equals("990")){
			msg = "로그인 정보가 DB에 존재하지 않음";
		}else if( code.equals("991")){
			msg = "세션 변조 => 권한["+ssnGrpCd+"], 사번["+ssnSabun+"] 로 시도함";

			String oriSessionId = (String)session.getAttribute("errorSessionId"); //원래 세션 아이디

			if( oriSessionId != null  && !oriSessionId.equals("") ){
				LoginSessionListener lsl = new LoginSessionListener();

				if( lsl.getSession( oriSessionId )  != null ){
					session = lsl.getSession( oriSessionId ); //원래 세션 정보
					ssnGrpCd = (String)session.getAttribute("ssnGrpCd");
					ssnSabun = (String)session.getAttribute("ssnSabun");
				}
			}else{
				msg = "세션 변조 => 원래 세션이 존재 하지 않음. 접속IP로 체크.";
			}


		}else if( code.equals("993")){
			msg = "파라미터 변조 => " + session.getAttribute("errorMsg");
		}else if( code.equals("994")){
			msg = "권한 없는 화면 접근";
		}else if( code.equals("995")){
			msg = "URL 직접 접근";
		}

		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("ssnEnterCd",	ssnEnterCd);
		paramMap.put("job",			"view");
		paramMap.put("ip",			StringUtil.getClientIP(request));
		paramMap.put("refererUrl",	"");
		paramMap.put("requestUrl",	errorUrl);
		paramMap.put("controller",	"");
		paramMap.put("queryId",		code);
		paramMap.put("menuId",		"");
		paramMap.put("ssnGrpCd",	ssnGrpCd);
		paramMap.put("memo",		msg0+msg);
		paramMap.put("ssnSabun",	ssnSabun);

		securityMgrService.PrcCall_P_COM_SET_OBSERVER(paramMap);

		session.invalidate();

		if( isIBSheet ){
			throw new IBSheetException(code);
		}else{
			response.sendRedirect("/Info.do?code="+code);
		}

	}

	/**
	 * 보안체크
	 * 1. 로그인 체크 - 세션변조
	 * 2. 파라미터 변조 체크
	 * 3. 권한 없는 화면 접근 체크
	 *
	 * Result Code
	 * 990 : 로그인 정보가 DB에 존재하지 않음
	 * 991 : 세션 변조
	 * 993 : 파라미터 변조
	 * 994 : 권한 없는 화면 접근
	 *
	 * @param request
	 * @return
	 */
	public String securityCheck(HttpServletRequest request){

		try{

			HttpSession session	= request.getSession(false);


			String ssnSecurityDetail = (String)session.getAttribute("ssnSecurityDetail"); //보안체크 개별 체크 여부
			String ssnEnterCd   = (String)session.getAttribute("ssnEnterCd");
			String ssnAdmin     = (String)session.getAttribute("ssnAdmin"); //관리자 여부
			String ssnSabun 	= (String)session.getAttribute("ssnSabun");
			String ssnGrpCd 	= (String)session.getAttribute("ssnGrpCd");
			String cmd 			= (String)request.getParameter("cmd");
			
			String searchApplSeq = String.valueOf(request.getParameter("searchApplSeq") == null ? "" : String.valueOf(request.getParameter("searchApplSeq"))); //신청서

			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("ssnSecurityDetail", 	ssnSecurityDetail);
			paramMap.put("ssnEnterCd", 			ssnEnterCd);
			paramMap.put("ssnSabun", 			ssnSabun);
			paramMap.put("ssnGrpCd", 			ssnGrpCd);
			paramMap.put("cmd", 				cmd);
			paramMap.put("ssnAdmin", 			ssnAdmin);
			paramMap.put("clientIp", 			StringUtil.getClientIP(request) );
			paramMap.put("sessionId", 			session.getId());
			paramMap.put("relUrl", 				StringUtil.getRelativeUrl(request));
			paramMap.put("searchApplSeq", 		searchApplSeq);


			Log.Debug("★★★paramMap : " + paramMap );
			String mrd = (String)request.getParameter("Mrd");
			if( mrd != null ) mrd = mrd.substring(mrd.lastIndexOf("/")+1);
			paramMap.put("mrd", 				mrd);
			paramMap.put("rdParam", 			(String)request.getParameter("Param"));

			session.setAttribute("errorUrl", StringUtil.getRelativeUrl(request));

			//체크 프로시저 호출
			Map<?, ?> rs = securityMgrService.PrcCall_F_SEC_GET_AUTH_CHK(paramMap);
			Log.Debug("PrcCall_F_SEC_GET_AUTH_CHK : " + rs );


			//체크 결과
			String result = (String)rs.get("result");
			JSONObject jObject  = new JSONObject(result); //CODE, SECURITY_KEY
			String code = jObject.getString("CODE");

			
			String applSabun = "";
			if( searchApplSeq != null && !searchApplSeq.equals("") ){
				applSabun = jObject.getString("APPL_SABUN"); //신청순번이 있을 때 신청자 사번
			}else{
				applSabun = ssnSabun; //로그인사번
			}
			Log.Debug("★★★ applSabun : " + applSabun );

			if( code.equals("0")){

				//RD에서 체크 하는 키 값
				String securityKey = jObject.getString("SECURITY_KEY");
				if(  securityKey != null && !securityKey.equals("")){ //securityKey
					request.setAttribute("securityKey", securityKey);
				}
			}else{

				if( code.equals("991")){ //세션변조 일 때
					session.setAttribute("errorSessionId", jObject.getString("SECURITY_KEY"));
				}
				return code; //에러
			}
			
			if( ssnSecurityDetail.indexOf("P") > -1 ){ //파라미터 변조 체크 여부

				//조회권한이 자신만 조회일 때만 사번 변조 체크
				String ssnSearchType = (String)session.getAttribute("ssnSearchType"); //조회권한 A:전체 , P:자신만조회
				Log.Debug("ssnSearchType    :" + ssnSearchType);
				Log.Debug("cmd    :" + cmd);
				if( cmd != null && !cmd.equals("") && ssnSearchType != null && ssnSearchType.equals("P") ){

					if(cmd.indexOf("get") == 0
					   && !cmd.equals("getEmpProfile") //사원검색-사원프로파일 화면 조회 - 예외
					   && !cmd.equals("baseEmployeeDetail") //사원검색 상세 - 예외
					   && !cmd.equals("viewEmpProfile") //사원검색 상세 - 예외
					){

						String ssnCheckSabun = (String)session.getAttribute("ssnCheckSabun"); //체크 파라미터 명
						Log.Debug("ssnCheckSabun    :" + ssnCheckSabun);
						String arr[] = ssnCheckSabun.split(",");
						for(int i = 0; i < arr.length; i++) {
							String str = (String)request.getParameter(arr[i]);
							if( str != null && !str.equals("") && !ssnSabun.equals(str) && !applSabun.equals(str)){
								Log.Debug("■■■■■■■■■■■■■■■■■■■ 3. 파라미터 변조 체크 ■■■■■■■■■■■■■■■■■■■■■■■■");
								Log.Debug("name    :" + arr[i]);
								Log.Debug("param   :" + request.getParameter(arr[i]));
								Log.Debug("session :" + ssnSabun);
								Log.Debug("■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■");
								session.setAttribute("errorMsg", "[sabun="+str+"]");
								return "993";
							}
						}
					}
				}//if(cmd != null )
			}

			//에러일 때만 사용
			session.removeAttribute("errorUrl");
			session.removeAttribute("errorMsg");

		}catch(Exception e){

			Log.Debug("□□□□□□□□□□□□□□□□□□□□□ Exception 체크 실패! " + e.getMessage() );
			e.printStackTrace();
		}
		return "";
	}

}
