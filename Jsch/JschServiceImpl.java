package com.isu.rem.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.transaction.Transactional;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.isu.auth.config.AuthConfigProvider;
import com.isu.auth.config.data.AuthConfig;
import com.isu.option.service.TenantConfigManagerService;
import com.isu.option.util.ImageUtil;
import com.isu.option.util.Sha256;
import com.isu.option.vo.ReturnParam;
import com.isu.rec.entity.CommCollection;
import com.isu.rec.entity.CommFiles;
import com.isu.rec.repository.CommCollectionRepository;
import com.isu.rec.repository.RecCommFilesRepository;
import com.isu.rem.DBEncryptDecryptModule;
import com.isu.rem.StringUtil;
import com.isu.rem.entity.RemAnnouncement;
import com.isu.rem.entity.RemApplicantByAnnounce;
import com.isu.rem.entity.RemEvaluationResultByStep;
import com.isu.rem.entity.RemResumeAbroad;
import com.isu.rem.entity.RemResumeBasic;
import com.isu.rem.entity.RemResumeBasicEtc;
import com.isu.rem.entity.RemResumeCareer;
import com.isu.rem.entity.RemResumeDesc;
import com.isu.rem.entity.RemResumeEducation;
import com.isu.rem.entity.RemResumeEtc;
import com.isu.rem.entity.RemResumeHobby;
import com.isu.rem.entity.RemResumeLanguage;
import com.isu.rem.entity.RemResumeLicense;
import com.isu.rem.entity.RemResumeMilitary;
import com.isu.rem.entity.RemResumeParttimejob;
import com.isu.rem.entity.RemResumeSchool;
import com.isu.rem.entity.RemStep;
import com.isu.rem.entity.RemUserMgr;
import com.isu.rem.repository.RemAnnouncementDao;
import com.isu.rem.repository.RemAnnouncementRepository;
import com.isu.rem.repository.RemApplicantByAnnounceDao;
import com.isu.rem.repository.RemEvaluationResultByStepDao;
import com.isu.rem.repository.RemEvaluationResultByStepRepository;
import com.isu.rem.repository.RemEvaluationResultDetailByStepDao;
import com.isu.rem.repository.RemEvaluationResultInterviewerByStepDao;
import com.isu.rem.repository.RemResumeAbroadRepository;
import com.isu.rem.repository.RemResumeBasicDao;
import com.isu.rem.repository.RemResumeBasicEtcRepository;
import com.isu.rem.repository.RemResumeBasicRepository;
import com.isu.rem.repository.RemResumeCareerRepository;
import com.isu.rem.repository.RemResumeDescRepository;
import com.isu.rem.repository.RemResumeEducationRepository;
import com.isu.rem.repository.RemResumeEtcRepository;
import com.isu.rem.repository.RemResumeFamilyRepository;
import com.isu.rem.repository.RemResumeHobbyRepository;
import com.isu.rem.repository.RemResumeLanguageRepository;
import com.isu.rem.repository.RemResumeLicenseRepository;
import com.isu.rem.repository.RemResumeMilitaryRepository;
import com.isu.rem.repository.RemResumeParttimejobRepository;
import com.isu.rem.repository.RemResumeSchoolRepository;
import com.isu.rem.repository.RemStepDao;
import com.isu.rem.repository.RemUserMgrDao;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

@Service
public class JschServiceImpl implements SamyangService {

	@Value("${use.encrypt.key}")
	private String encryptKey;

	@Resource
	private RemEvaluationResultByStepRepository evaluationResultByStepRepo;

	@Resource
	private RemResumeBasicRepository basicRepo;

	@Resource
	private RemResumeBasicDao basicDao;

	@Resource
	private RemResumeBasicEtcRepository basicEtcRepo;

	@Resource
	private RemResumeCareerRepository careerRepo;

	@Resource
	private RemResumeDescRepository descRepo;

	@Resource
	private RemResumeEtcRepository etcRepo;

	@Resource
	private RemResumeFamilyRepository familyRepo;

	@Resource
	private RemResumeLanguageRepository languageRepo;

	@Resource
	private RemResumeLicenseRepository licenseRepo;

	@Resource
	private RemResumeMilitaryRepository militaryRepo;

	@Resource
	private RemResumeSchoolRepository schoolRepo;
	
	@Resource
	private RemResumeParttimejobRepository parttimejobRepo;
	
	@Resource
	private RemResumeEducationRepository educationRepo;
	
	@Resource
	private RemResumeAbroadRepository abroadRepo;
	
	@Resource
	private RemResumeHobbyRepository hobbyRepo;
	
	@Resource 
	private RemAnnouncementRepository announcementRepo;
	
	@Resource
	private RemApplicantByAnnounceDao remApplicantByAnnounceDao;
	
	@Resource
	private RecCommFilesRepository recCommFilesRepo;

	@Resource
	private RemStepDao stepDao;

	@Resource
	private RemAnnouncementDao remAnnouncementDao;
	
	@Resource
	private RemEvaluationResultDetailByStepDao evaluationResultDetailByStepDao;

	@Autowired
	private UserMgrService userMgrService;

	@Resource
	private RemEvaluationResultInterviewerByStepDao evaluationResultInterviewerByStepDao; 
	
	@Resource
	private RemEvaluationResultByStepDao evaluationResultByStepDao;

	@Resource
	CommCollectionRepository commCollectionRepo;
	
	@Resource
	private RemUserMgrDao remUserMgrDao;
	@Autowired
	AuthConfigProvider authConfigProvider;
	@Autowired
	TenantConfigManagerService tcms;

	public ReturnParam sendErp(Long tenantId, Map<String, Object> applicants, Map<String,Object> mapSessionData) throws Exception {
		
		String userKey = StringUtil.isNullToString(mapSessionData.get("userKey"));
		ReturnParam rp = new ReturnParam();
		rp.setSuccess("");
		
		if (applicants == null || applicants.get("stepInfo") == null ||	applicants.get("applicants") == null) {
			rp.setFail("대상자가 없습니다."); 
			return rp;
		}
		
		//해당 공고를 찾음 
		Long stepId = Long.parseLong(((Map<String, Object>)	applicants.get("stepInfo")).get("stepId").toString()); 
		RemStep remStep =stepDao.findById(stepId); 
		Long annoId = remStep.getRemAnnouncement().getAnnoId(); 
		int stepSeq = remStep.getStepSeq(); //진행차수
		
		//선택한 지원자
		List<Map<String, Object>> appls = (List<Map<String, Object>>) applicants.get("applicants"); 
		List<Long> applIds = new ArrayList<Long>();
		if (appls != null && appls.size() > 0) { 
			for (Map<String, Object> appl : appls) {
				applIds.add(Long.parseLong(appl.get("applId").toString()));
			} 
		}else { //선택값이 없을 경우 전체
			List<RemEvaluationResultByStep> targets = evaluationResultByStepRepo.findByStepId(stepId);
		    if (targets != null && targets.size() > 0) {
		    	for (RemEvaluationResultByStep target : targets) {
		    		applIds.add(target.getRemApplicantByAnnounce().getApplId()); 
		    	} 
		    } 
		}
		
		Map<String,Object> mapResult = new HashMap<String,Object>();
		List<RemAnnouncement> listAnnounce = announcementRepo.findByAnnoIdReturnByAnnounce(annoId);
		RemAnnouncement announcement = listAnnounce.get(0);
		mapResult.put("annoId"      , StringUtil.isNullToString(announcement.getAnnoId()));      //채용공고ID
		mapResult.put("companyNm"   , StringUtil.isNullToString(announcement.getNote()));                                   //회사명
		mapResult.put("annoTitle"   , StringUtil.isNullToString(announcement.getAnnoTitle()));   //채용공고제목
		mapResult.put("annoType"    , StringUtil.isNullToString(announcement.getAnnoType()));    //채용구분
		mapResult.put("annoCategory", StringUtil.isNullToString(announcement.getAnnoCategory()));//채용공고카테고리
		mapResult.put("applicantCnt", evaluationResultByStepDao.findByStepIdAndPassYn(stepId, "Y").size());// 합격자 수
		
		String apiKey = tcms.getConfigValue(tenantId, "REC.AUTH.API_KEY", true, "");
		String secret = tcms.getConfigValue(tenantId, "REC.AUTH.SECRET", true, "");
		
		
		mapResult.put("secret", secret);
		mapResult.put("apiKey", apiKey);
		String strLoginId = StringUtil.isNullToString(mapSessionData.get("login_id"));
		strLoginId = DBEncryptDecryptModule.decrypt(strLoginId);
		mapResult.put("userKey", mapSessionData.get("cd_company") + "@" + strLoginId);
		
		CommCollection collection =  commCollectionRepo.findByTenantIdAndCollectionNm(tenantId, "majors");
		List<Map<String,Object>> collectionList = new ArrayList<>();
		if(collection != null) {
			ObjectMapper mapperCollenction = new ObjectMapper();
			collectionList = mapperCollenction.readValue(collection.getCollectionValue(), new ArrayList<Map<String, Object>>().getClass());
		}
				
		
		
		List<Map<String,Object>> listapplicant = new ArrayList<Map<String,Object>>();
		for(Long applId : applIds) {
			ObjectMapper mapper = new ObjectMapper();
			
			List<Map<String,Object>> listEvalInfo     = new ArrayList<Map<String,Object>>();
			List<Map<String,Object>> listEvalInfoTemp = new ArrayList<Map<String,Object>>();
			List<Map<String,Object>> listDetail       = new ArrayList<Map<String,Object>>();
			List<Map<String,Object>> listDetailGroup  = new ArrayList<Map<String,Object>>();
			List<Map<String,Object>> listSchool       = new ArrayList<Map<String,Object>>();
			List<Map<String,Object>> listCareer       = new ArrayList<Map<String,Object>>();
			List<Map<String,Object>> listPart         = new ArrayList<Map<String,Object>>();
			List<Map<String,Object>> listEdu          = new ArrayList<Map<String,Object>>();
			List<Map<String,Object>> listlicense      = new ArrayList<Map<String,Object>>();
			List<Map<String,Object>> listLanguage     = new ArrayList<Map<String,Object>>();
			List<Map<String,Object>> listAbroad       = new ArrayList<Map<String,Object>>();
			List<Map<String,Object>> listDesc         = new ArrayList<Map<String,Object>>();
			List<Long> listIdTemp                    = new ArrayList<Long>();
			
			Map<String,Object> mapEvalInfo = new HashMap<String,Object>();
			Map<String,Object> mapResume   = new HashMap<String,Object>();
			Map<String,Object> mapAppli    = new HashMap<String,Object>();
			
			RemApplicantByAnnounce remApplicantDto = new RemApplicantByAnnounce();
		
			listIdTemp.add(applId);
			remApplicantDto = remApplicantByAnnounceDao.findById(applId);
			mapAppli.put("applId"      , StringUtil.isNullToString(remApplicantDto.getApplId())); //ID
			mapAppli.put("applNo"      , StringUtil.isNullToString(remApplicantDto.getApplNo())); //지원자번호
			mapAppli.put("applName"    , StringUtil.isNullToString(remApplicantDto.getApplicantName())); //지원자이름
			mapAppli.put("applCategory", StringUtil.isNullToString(remApplicantDto.getApplyCategory())); //지원자선택카테고리
			
			//평가정보조회
			listEvalInfo = evaluationResultByStepDao.findByAnnoId(stepId, applId);
			for(Map<String,Object> data: listEvalInfo) {
				mapEvalInfo.clear();
				mapEvalInfo.put("stepId"    , data.get("stepId"));
				mapEvalInfo.put("stepName"  , data.get("stepNm"));
				mapEvalInfo.put("evalId"    , data.get("evalId"));
				mapEvalInfo.put("evalName"  , data.get("evalName"));
				mapEvalInfo.put("evalresult", data.get("evalResult"));
				mapEvalInfo.put("evalRn"    , data.get("evalRn"));
				mapEvalInfo.put("evalSum"   , data.get("evalSum"));
				mapEvalInfo.put("evalSumRn" , data.get("evalSumRn"));
				
				List<Map<String,Object>> listDetailTemp = new ArrayList<Map<String,Object>>();
				listDetailGroup = evaluationResultDetailByStepDao.findByAnnoIdGroupbyInterviewerKey(stepId, applId);
				
				for(Map<String,Object> detailGroup : listDetailGroup) {
					
					Map<String,Object> mapDetailGroupTemp = new HashMap<String,Object>();
					String strInterVuewerKey = StringUtil.isNullToString(detailGroup.get("interviewerKey"));
					
					List<Map<String,Object>> listComments = evaluationResultInterviewerByStepDao.findCommentsbyStepInterviewer(stepId, applId, strInterVuewerKey);
					if(listComments.size() > 0) {
						mapDetailGroupTemp.put("comments", listComments.get(0).get("comments"));
					}else {
						mapDetailGroupTemp.put("comments", "");
					}
					mapDetailGroupTemp.put("interviewerKey", strInterVuewerKey);
					
					List<Map<String,Object>> listDetailInterviewerTemp = new ArrayList<Map<String,Object>>();
					listDetailInterviewerTemp = evaluationResultDetailByStepDao.findByAnnoIdInterviewerKey(stepId, applId, strInterVuewerKey);
					List<Map<String,Object>> listInterviewerTemp = new ArrayList<Map<String,Object>>();
					for(Map<String,Object> detail : listDetailInterviewerTemp) {
						Map<String,Object> mapDetailTemp = new HashMap<String,Object>();
						mapDetailTemp.put("itemName" , StringUtil.isNullToString(detail.get("itemName")));
						mapDetailTemp.put("itemgrade", StringUtil.isNullToString(detail.get("itemgrade")));
						mapDetailTemp.put("itemscore", StringUtil.isNullToString(detail.get("itemscore")));
						mapDetailTemp.put("itemcalc" , StringUtil.isNullToString(detail.get("itemcalc")));
						listInterviewerTemp.add(mapDetailTemp);
					}
					mapDetailGroupTemp.put("items", listInterviewerTemp);
					
					if(!"".equals(strInterVuewerKey)) {
						String iKey = strInterVuewerKey;
						List<String> targetUserKey = new ArrayList<String>();
						targetUserKey.add(iKey);
						
						Map<String, Object> param = new HashMap<String, Object>();
						param.put("targetUserKey", mapper.writeValueAsString(targetUserKey));
						param.put("searchWord", "");

						Map<String, Object> empList = userMgrService.empSearch(tenantId, param, userKey);
						
						if(empList.get("message") != null && "success".equals(empList.get("message"))) {
							if(empList.get("data") != null) {
								List<Map<String, Object>> emps = (List<Map<String, Object>>)empList.get("data");
								Map<String,Object> mapInterView = new HashMap<String,Object>();
								for(Map<String, Object> emp : emps) {
									mapInterView.put("name"    , StringUtil.isNullToString(emp.get("empNm")));
									mapInterView.put("position", StringUtil.isNullToString(emp.get("position")));
								}
								mapDetailGroupTemp.put("interviewer", mapInterView);
							}
						}
					}else {
						Map<String,Object> mapInterView = new HashMap<String,Object>();
						mapInterView.put("name"    , "");
						mapInterView.put("position", "");
						mapDetailGroupTemp.put("interviewer", mapInterView);	
					}
					listDetailTemp.add(mapDetailGroupTemp);
				}
						
				if(listDetailGroup.size() > 0) {
					mapEvalInfo.put("detail", listDetailTemp);
				}
				listEvalInfoTemp.add(mapEvalInfo);
			}
			
			mapAppli.put("evalInfo", listEvalInfoTemp);
			
			mapResume.clear();
			//이력서생성일자
			mapResume.put("creatDate", StringUtil.isNullToString(remApplicantDto.getCreateDate()));
			
			// 전송할 데이터 조회
			List<RemResumeBasic>       basic        = basicRepo.findByApplIds(listIdTemp);   
			List<RemResumeBasicEtc>    basicEtc     = basicEtcRepo.findByApplIds(listIdTemp); 
			List<RemResumeCareer>      career       = careerRepo.findByApplIds(listIdTemp); 
			List<RemResumeDesc>        desc         = descRepo.findByApplIds(listIdTemp); 
			List<RemResumeEtc>         etc          = etcRepo.findByApplIds(listIdTemp); 
			List<RemResumeLanguage>    language     = languageRepo.findByApplIds(listIdTemp);
			List<RemResumeLicense>     license      = licenseRepo.findByApplIds(listIdTemp);
			List<RemResumeMilitary>    military     = militaryRepo.findByApplIds(listIdTemp); 
			List<RemResumeSchool>      school       = schoolRepo.findByApplIds(listIdTemp);
			List<RemResumeParttimejob> parttimejob  = parttimejobRepo.findByApplIds(listIdTemp);
			List<RemResumeEducation>   education    = educationRepo.findByApplIds(listIdTemp);
			List<RemResumeAbroad>      abroad       = abroadRepo.findByApplIds(listIdTemp);
			List<RemResumeHobby>       hobby        = hobbyRepo.findByApplIds(listIdTemp);
			
			//기초정보
			Map<String,Object> mapBasic    = new HashMap<String,Object>();
			String strPhotoUrl = "";
			for (RemResumeBasic m : basic) {
				Map<String, Object> mapDecrypt   = new HashMap<String, Object>();
				Map<String, Object> mapPhtoData  = new HashMap<String, Object>();
				mapDecrypt                       = basicDao.getDecryptStr(encryptKey, m.getEmail());
				strPhotoUrl = StringUtil.isEmpty(m.getPhoto())?"":"https://jobadmin.samyangfoods.com/rem/api/sy/resume/uploadfile?url="+m.getPhoto();
				
				try {
					mapPhtoData = getPhtoInfo(strPhotoUrl, m.getPhoto(), tenantId);
				}catch(Exception e) {
					rp.setFail("사진파일 복사중 오류가 발생하였습니다.");
					return rp;
				}
				
				mapBasic.put("photoConts" , mapPhtoData.get("base64"));     //(url or path or base64 협의)
				mapBasic.put("photo" , annoId + "" +  mapAppli.get("applNo") + "" + "." + mapPhtoData.get("type")); //"photo": "채용공고ID + 지원번호 + 확장자명    
				mapBasic.put("name"       , StringUtil.isNullToString(m.getName()));      //성명
				mapBasic.put("gender"     , StringUtil.isNullToString(m.getGender()));    //성별
				mapBasic.put("cName"      , StringUtil.isNullToString(m.getCname()));     //성명(한자)
				mapBasic.put("eName"      , StringUtil.isNullToString(m.getEname()));     //성명(영문)
				mapBasic.put("birthDate"  , StringUtil.isNullToString(m.getBirthDate())); //생년월일
				mapBasic.put("nationality", StringUtil.isNullToString(m.getCol2()));      //내외국인
				mapBasic.put("recruitPath", StringUtil.isNullToString(m.getCol1()));      //채용경로
				mapBasic.put("visaInfo"   , StringUtil.isNullToString(m.getCol3()));      //비자정보
				mapBasic.put("email"      , StringUtil.isNullToString(mapDecrypt.get("decryptStr").toString()));         //email
			}
			mapResume.put("basic", mapBasic);
			
			//연락처
			Map<String,Object> mapBasicEtc    = new HashMap<String,Object>();
			for (RemResumeBasicEtc m : basicEtc) {
				mapBasicEtc.clear();
				Map<String, Object> mapDecryptTel   = new HashMap<String, Object>();
				Map<String, Object> mapDecryptPhone = new HashMap<String, Object>();
				mapDecryptTel   = basicDao.getDecryptStr(encryptKey, m.getTel());
				mapDecryptPhone = basicDao.getDecryptStr(encryptKey, m.getPhone());
				mapBasicEtc.put("address"    , StringUtil.isNullToString(m.getAddress())); //주소
				mapBasicEtc.put("tel"        , StringUtil.isNullToString(mapDecryptTel.get("decryptStr"))); //집전화
				mapBasicEtc.put("mobilePhone", StringUtil.isNullToString(mapDecryptPhone.get("decryptStr"))); // 모바일
			}
			mapResume.put("basicEtc", mapBasicEtc);
			
			//장애보훈
			Map<String,Object> mapEtc    = new HashMap<String,Object>();
			for (RemResumeEtc m : etc) {
				mapEtc.clear();
				mapEtc.put("isLesion"    , StringUtil.isNullToString(m.getCol3())); //장애여부
				mapEtc.put("lesionType"  , StringUtil.isNullToString(m.getCol4())); //장애구분
				mapEtc.put("lesionGrade" , StringUtil.isNullToString(m.getCol5())); //장애등급
				mapEtc.put("isHandicap"  , StringUtil.isNullToString(m.getCol1())); //국가보훈여부
				mapEtc.put("handicapType", StringUtil.isNullToString(m.getCol2())); //국가보훈구분
				mapEtc.put("isWeak"      , StringUtil.isNullToString(m.getCol6())); // 취약계층여부
				mapEtc.put("weakType"    , StringUtil.isNullToString(m.getCol7())); //취약계층구분
			}
			mapResume.put("etc", mapEtc);
			
			//군경력
			Map<String,Object> mapMilitary = new HashMap<String,Object>();
			for(RemResumeMilitary m : military) {
				mapMilitary.clear();
				mapMilitary.put("militaryKind"  , StringUtil.isNullToString(m.getMilitaryKind())); //복무여부
				mapMilitary.put("militaryType"  , StringUtil.isNullToString(m.getMilitaryType())); //군별
				mapMilitary.put("militaryLevel" , StringUtil.isNullToString(m.getCol3()));         //계급
				mapMilitary.put("militarySdate" , StringUtil.isNullToString(m.getStartDate()));    //복무기간(시작일자)
				mapMilitary.put("militaryEdate" , StringUtil.isNullToString(m.getEndDate()));      //복무기간(종료일자)
				mapMilitary.put("militaryClass" , StringUtil.isNullToString(m.getCol2()));         //병과
				mapMilitary.put("militaryReason", StringUtil.isNullToString(m.getCol1()));         //면제사유
			}
			mapResume.put("military", mapMilitary);
			
			//학력
			for(RemResumeSchool m : school) {
				Map<String,Object> mapSchool = new HashMap<String,Object>();
				mapSchool.put("type"        , StringUtil.isNullToString(m.getType()));        //고등학교/대학교 구분 H or U
				mapSchool.put("lastSchool"  , StringUtil.isNullToString(m.getLastSchool()));  //최종학력 여부
				mapSchool.put("startDate"   , StringUtil.isNullToString(m.getStartDate()));   //재학기간
				mapSchool.put("endDate"     , StringUtil.isNullToString(m.getEndDate()));     //재학기간
				mapSchool.put("graduated"   , StringUtil.isNullToString(m.getCol3()));        //졸업구분
				mapSchool.put("admission"   , StringUtil.isNullToString(m.getCol4()));        //입학구분
				mapSchool.put("schoolName"  , StringUtil.isNullToString(m.getSchoolName()));  //학교
				mapSchool.put("campusName"  , StringUtil.isNullToString(m.getCampusName()));  //캠퍼스
				mapSchool.put("major"       , StringUtil.isNullToString(m.getMajor()));       //전공
				if(!"".equals(StringUtil.isNullToString(m.getMajor()))) {
					for(Map<String,Object> mapCollection : collectionList) {
						if(StringUtil.isNullToString(m.getMajor()).equals(StringUtil.isNullToString(mapCollection.get("name")))) {
							mapSchool.put("majorClass"       , StringUtil.isNullToString(mapCollection.get("class")));       //전공계열
							break;
						}
					}
				}
				mapSchool.put("subMajor"    , StringUtil.isNullToString(m.getSubMajor()));    //부/복수전공명 
				if(!"".equals(StringUtil.isNullToString(m.getSubMajor()))) {
					for(Map<String,Object> mapCollection : collectionList) {
						if(StringUtil.isNullToString(m.getSubMajor()).equals(StringUtil.isNullToString(mapCollection.get("name")))) {
							mapSchool.put("subMajorClass"       , StringUtil.isNullToString(mapCollection.get("class")));       //부/복수전공계열
							break;
						}
					}
				}
				mapSchool.put("subMajorType", StringUtil.isNullToString(m.getSubMajorType()));//부/복수전공구분
				mapSchool.put("schoolTime"  , StringUtil.isNullToString(m.getCol1()));        //주/야
				mapSchool.put("score"       , StringUtil.isNullToString(m.getScore()));       //총학점
				mapSchool.put("subScore"    , StringUtil.isNullToString(m.getCol2()));        //전공학점
				mapSchool.put("schoolLevel" , StringUtil.isNullToString(m.getSchoolLevel())); //계열 or 학력
				listSchool.add(mapSchool);
			}
			mapResume.put("school", listSchool);
			
			//경력
			for(RemResumeCareer m : career) {
				Map<String,Object> mapCareer = new HashMap<String,Object>();
				mapCareer.put("startDate"   , StringUtil.isNullToString(m.getStartDate()));  //기간
				mapCareer.put("endDate"     , StringUtil.isNullToString(m.getEndDate()));    //기간
				mapCareer.put("companyName" , StringUtil.isNullToString(m.getCompanyName()));//회사명
				mapCareer.put("isCompany"   , StringUtil.isNullToString(m.getCol10()));      //재직여부
				mapCareer.put("task"        , StringUtil.isNullToString(m.getCol4()));       //업종
				mapCareer.put("deptName"    , StringUtil.isNullToString(m.getCol5()));       //부서
				mapCareer.put("taskContent" , StringUtil.isNullToString(m.getCol2()));       //담당업무
				mapCareer.put("taskLevel"   , StringUtil.isNullToString(m.getCompanyJob())); //직급/직책
				mapCareer.put("annualSalary", StringUtil.isNullToString(m.getCol1()));       //급여
				mapCareer.put("bonus"       , StringUtil.isNullToString(m.getCol8()));       //성과급
				mapCareer.put("bonusType"   , StringUtil.isNullToString(m.getCol9()));       //고정/변동여부
				mapCareer.put("leaveReason" , StringUtil.isNullToString(m.getCol6()));       //퇴사사유
				mapCareer.put("taskDetail"  , StringUtil.isNullToString(m.getCol7()));       //상세업무내용
				listCareer.add(mapCareer);
			}
			mapResume.put("career", listCareer);
			//아르바이트
			for(RemResumeParttimejob m : parttimejob) {
				Map<String,Object> mapPart = new HashMap<String,Object>();
				mapPart.put("startDate"  , StringUtil.isNullToString(m.getStartDate()));  //기간
				mapPart.put("endDate"    , StringUtil.isNullToString(m.getEndDate()));    //기간
				mapPart.put("companyName", StringUtil.isNullToString(m.getCompanyName()));//회사명
				mapPart.put("taskDetail" , StringUtil.isNullToString(m.getCol1()));       //상세담당업무
				listPart.add(mapPart);
			}
			mapResume.put("parttimejob", listPart);
			
			//교육
			for(RemResumeEducation m : education) {
				Map<String,Object> mapEdu = new HashMap<String,Object>();
				mapEdu.put("startDate"       , StringUtil.isNullToString(m.getStartDate()));       //기간
				mapEdu.put("endDate"         , StringUtil.isNullToString(m.getEndDate()));         //기간
				mapEdu.put("educationPlace"  , StringUtil.isNullToString(m.getEducationPlace()));  //장소
				mapEdu.put("educationName"   , StringUtil.isNullToString(m.getEducationName()));   //교육명
				mapEdu.put("educationContent", StringUtil.isNullToString(m.getEducationContent())); //내용
				listEdu.add(mapEdu);
			}
			mapResume.put("education", listEdu);
			
			//자격증
			for(RemResumeLicense m : license) {
				Map<String,Object> maplicense = new HashMap<String,Object>();
				maplicense.put("licenseName", StringUtil.isNullToString(m.getLicenseName())); //자격증
				maplicense.put("issueOrg"   , StringUtil.isNullToString(m.getIssueOrg()));    //발행처발행기관
				maplicense.put("issueDate"  , StringUtil.isNullToString(m.getIssueDate()));   //취득일
				listlicense.add(maplicense);
			}
			mapResume.put("license", listlicense);
			
			//외국어
			for(RemResumeLanguage m : language) {
				Map<String,Object> mapLanguage = new HashMap<String,Object>();
				mapLanguage.put("languageName" , StringUtil.isNullToString(m.getLanguageName()));
				mapLanguage.put("languagePlace", StringUtil.isNullToString(m.getCol1()));
				mapLanguage.put("lastScore"    , StringUtil.isNullToString(m.getLastScore()));
				mapLanguage.put("issueDate"    , StringUtil.isNullToString(m.getIssueDate()));
				listLanguage.add(mapLanguage);
			}
			mapResume.put("language", listLanguage);
			
			//해외경험
			for(RemResumeAbroad m : abroad) {
				Map<String,Object> mapAbroad = new HashMap<String,Object>();
				mapAbroad.put("startDate"      , StringUtil.isNullToString(m.getStartDate()));     //기간
				mapAbroad.put("endDate"        , StringUtil.isNullToString(m.getEndDate()));       //기간
				mapAbroad.put("sabroadCountry" , StringUtil.isNullToString(m.getAbroadCountry())); //체류국가
				mapAbroad.put("sabroadType"    , StringUtil.isNullToString(m.getAbroadType()));    //체류형태
				mapAbroad.put("sabroadLanguage", StringUtil.isNullToString(m.getCol1()));          //구사언어
				mapAbroad.put("sabroadContent" , StringUtil.isNullToString(m.getCol2()));          //상세내용
				listAbroad.add(mapAbroad);
			}
			mapResume.put("abroad", listAbroad);
			
			//취미
			Map<String, Object> mapHobby = new HashMap<String,Object>();
			for(RemResumeHobby m : hobby) {
				mapHobby.clear();
				mapHobby.put("specialty"      , StringUtil.isNullToString(m.getSpecialty())); //특기
				mapHobby.put("hobby"          , StringUtil.isNullToString(m.getHobby()));       //취미
				mapHobby.put("joinDate"       , StringUtil.isNullToString(m.getCol1()));        //입사가능일
				mapHobby.put("hopeSalary"     , StringUtil.isNullToString(m.getCol2()));        //희망연봉
			}
			mapResume.put("hobby", mapHobby);
			
			//자기소개서 
			Map<String,Object> mapDesc1 = new HashMap<String,Object>();
			Map<String,Object> mapDesc2 = new HashMap<String,Object>();
			Map<String,Object> mapDesc3 = new HashMap<String,Object>();
			Map<String,Object> mapDesc4 = new HashMap<String,Object>();
			Map<String,Object> mapDesc5 = new HashMap<String,Object>();
			for(RemResumeDesc m : desc) {
				mapDesc1.put("label", StringUtil.isNullToString(m.getLabel1()));   //항목
				mapDesc1.put("content", StringUtil.isNullToString(m.getDesc1())); //내용
				listDesc.add(mapDesc1);
				mapDesc2.put("label", StringUtil.isNullToString(m.getLabel2()));   //항목
				mapDesc2.put("content", StringUtil.isNullToString(m.getDesc2())); //내용
				listDesc.add(mapDesc2);
				mapDesc3.put("label", StringUtil.isNullToString(m.getLabel3()));   //항목
				mapDesc3.put("content", StringUtil.isNullToString(m.getDesc3())); //내용
				listDesc.add(mapDesc3);
				mapDesc4.put("label", StringUtil.isNullToString(m.getLabel4()));   //항목
				mapDesc4.put("content", StringUtil.isNullToString(m.getCol1())); //내용
				listDesc.add(mapDesc4);
				mapDesc5.put("label", StringUtil.isNullToString(m.getLabel5()));   //항목
				mapDesc5.put("content", StringUtil.isNullToString(m.getCol2())); //내용
				listDesc.add(mapDesc5);
			}
			mapResume.put("descpart", listDesc);
			mapAppli.put("resumeInfo", mapResume);
			listapplicant.add(mapAppli);
			try {
				this.fileDataConvert(remApplicantDto.getResumeValue(), tenantId, annoId, StringUtil.isNullToString(applId));
			}catch(Exception e) {
				rp.setFail("첨부파일 복사중 오류가 발생하였습니다.");
				return rp;
			}
			
		}
		mapResult.put("applicantList", listapplicant);
		
		ObjectMapper mapperResult = new ObjectMapper(); 
		try { 
			rp.put("returnData", mapperResult.writeValueAsString(mapResult));

			RestTemplate restTemplate = new RestTemplate();
			HttpComponentsClientHttpRequestFactory crf = new HttpComponentsClientHttpRequestFactory();
	          crf.setReadTimeout(600000); //읽기시간초과 타임아웃
	          crf.setConnectTimeout(600000); //연결시간초과 타임아웃
	          // @formatter:off
	          HttpClient httpClient = HttpClientBuilder.create() 
	               .evictIdleConnections(2000L, TimeUnit.MILLISECONDS) //서버에서 keepalive시간동안 미 사용한 커넥션을 죽이는 등의 케이스 방어로 idle커넥션을 주기적으로 지움
	               .build();
	          crf.setHttpClient(httpClient);
	          // @formatter:on
	          restTemplate.setRequestFactory(crf);
	        
			Map<String,Object> mapRtn = new HashMap();
			mapRtn.put("resumeValues", mapResult);
			String strUrl = tcms.getConfigValue(tenantId, "REM.HR.URL", true, "");
			try {
				ResponseEntity<Map> resRtn = restTemplate.postForEntity(strUrl, mapRtn, Map.class);
				if(resRtn.getBody() != null) {
					if("FAIL".equals(resRtn.getBody().get("status"))) {
						rp.setFail(StringUtil.isNullToString(resRtn.getBody().get("message")));
					}
				}
			}catch(Exception e) {
				rp.setFail("HR 시스템 연동중 오류가 발생하였습니다.");
			}
		} catch (JsonProcessingException e1) {
			
		}
		
		return rp;
	}

	protected String urlToBase64Str(String url) {

		if (url != null && !"".equals(url)) {
			String fileId = url.substring(url.lastIndexOf("/") + 1, url.length());
			CommFiles file = recCommFilesRepo.findById(Long.valueOf(fileId)).get();
			if (file != null)
				return ImageUtil.getBase64String(file.getFileData());
		}
		return null;
	}

	public Map<String,Object> getPhtoInfo(String strPhotoUrl, String strUrlParam, Long tenantId) throws Exception {
	    
		if("".equals(StringUtil.isNullToString(strPhotoUrl))) {
			return null;
		}else {
			String imageStr = "";
	        InputStream finput = null;
	        Map<String,Object> mapRtn = new HashMap();
	        try {
	        	
	        	String host = tcms.getConfigValue(tenantId, "REC.FILE_SERVER.HOST", true, ""); // 필드 파일 업로드 경로
	    		String apiKey = tcms.getConfigValue(tenantId, "REC.AUTH.API_KEY", true, "");
	    		String secret = tcms.getConfigValue(tenantId, "REC.AUTH.SECRET", true, "");
	    		
	    		String downloadUrl = host + strUrlParam + "?apiKey="+apiKey+"&secret="+secret;
	        	URL fileURL = new URL(downloadUrl);
	        	HttpURLConnection httpConn = (HttpURLConnection) fileURL.openConnection();
	        	
	        	//String contentType = httpConn.getContentType();
	        	String fileName = "downloaded.jpg";
	        	String strTemp = httpConn.getHeaderField("Content-Disposition");
	        	strTemp = URLDecoder.decode(new String(strTemp.getBytes("ISO-8859-1"), "UTF-8"), "UTF-8");
	        	if(strTemp != null && strTemp.indexOf("=") != -1) {
	        	   fileName = strTemp.split("=")[1]; //getting value after '='
	        	}
	        	
	        	// URL을 통해 File 생성
	            File file = new File(fileName);
	            String strExt = "jpg";
	            if(fileName.indexOf(";") != -1) {
	            	fileName = fileName.replaceAll(";", "");
	            }
	            
	            if(fileName.split("\\.").length > 1) {
	            	strExt = fileName.split("\\.")[fileName.split("\\.").length - 1];
	            	if(strExt == "jfif") strExt = "jpg";
	            }
	            
        		URL url = new URL(strPhotoUrl);
	            BufferedImage img = ImageIO.read(url);    
				ImageIO.write(img, strExt, file);
		        finput = new FileInputStream(file);
		        byte[] imageBytes = new byte[(int)file.length()];
		        finput.read(imageBytes, 0, imageBytes.length);
		        imageStr = ImageUtil.getBase64String(imageBytes);
        	
	        	mapRtn.put("type", strExt);
	        	mapRtn.put("base64", imageStr);
	        	
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw e;
			} catch(Exception e){
				throw e;
			}finally {
		        try {
					finput.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	        return mapRtn;
		}
	}
	
	private void fileDataConvert(String strValue , Long tenantId, Long annoId, String applId) throws Exception {
		
		if("".equals(StringUtil.isNullToString(strValue))) {
			return;
		}
		
		ObjectMapper mapper = new ObjectMapper(); 
		//Map<Object, Object> map = new HashMap<Object, Object>(); // convert JSON string to Map
		ArrayList<Map<Object, Object>> listData = new ArrayList<Map<Object, Object>>();
		try {
			listData = mapper.readValue(strValue, new TypeReference<ArrayList<Map<Object, Object>>>(){});
			for(Map<Object,Object> m : listData) {
				//첨부파일
				if("uploadfilepart".equals(m.get("sectionKey"))) {
					ArrayList<Map<String,Object>> listSection = (ArrayList<Map<String, Object>>) m.get("sectionData");
					for(Map<String,Object> mSection : listSection) {
						Iterator<String> keys = mSection.keySet().iterator();
						while (keys.hasNext()){ 
							String key = StringUtil.isNullToString(keys.next()); 
							if(key.indexOf("file") != -1) {
								ArrayList<Map<String,Object>> listFile = new ArrayList<Map<String,Object>>();
								if(!"".equals(StringUtil.isNullToString(mSection.get(key)))) {
									listFile = (ArrayList<Map<String, Object>>) mSection.get(key);
									for(Map<String,Object> mFile : listFile) {
										String strDownUrl = StringUtil.isNullToString(mFile.get("remoteDownurl"));
										String strFileName = StringUtil.isNullToString(mFile.get("name"));
										this.copyToFile(strDownUrl, tenantId, annoId, applId, strFileName);
									}
								}
							}
						
						}						
					}
					
				}
			}
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} 
		
		

	/*	Map<String,Object> mapResult = new HashMap<String,Object>();
		mapResult.put("data", strValue);
		ObjectMapper mapperResult = new ObjectMapper(); 
		try { 
			mapperResult.writeValueAsString(mapResult);
		} catch (JsonProcessingException e1) {
			
		}*/
		
	}
	
	
	private void copyToFile(String strFileUrl, Long tenantId, Long annoId, String applId, String fileName) throws Exception {
			 
		String host = tcms.getConfigValue(tenantId, "REC.FILE_SERVER.HOST", true, ""); // 필드 파일 업로드 경로
		String apiKey = tcms.getConfigValue(tenantId, "REC.AUTH.API_KEY", true, "");
		String secret = tcms.getConfigValue(tenantId, "REC.AUTH.SECRET", true, "");
		String downloadUrl = host + strFileUrl + "?apiKey="+apiKey+"&secret="+secret;
		
		String REMOTE_ADDR = tcms.getConfigValue(tenantId, "REM.FTP_FILE_IP", true, "");
		int PORT = Integer.parseInt(tcms.getConfigValue(tenantId, "REM.FTP_FILE_PORT", true, ""));
		String USERNAME = tcms.getConfigValue(tenantId, "REM.FTP_FILE_USER", true, "");
		String PASSWORD = tcms.getConfigValue(tenantId, "REM.FTP_FILE_PASSWORD", true, "");
		
		Session session = null;
		Channel channel = null;
		ChannelSftp channelSftp = null;
		
		JSch jsch = new JSch();
		InputStream inputStream = null;
		try {
            URL fileURL = new URL(downloadUrl);
            HttpURLConnection httpConn = (HttpURLConnection) fileURL.openConnection();
            int responseCode = httpConn.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK) {
            	inputStream = httpConn.getInputStream();

            	session  = jsch.getSession(USERNAME, REMOTE_ADDR, PORT);
    			session.setPassword(PASSWORD);
    			java.util.Properties config = new java.util.Properties();
    			config.put("StrictHostKeyChecking", "no");
    			session.setConfig(config);
    			session.connect();
    			
    			channel = session.openChannel("sftp");
    			channel.connect();
    			channelSftp = (ChannelSftp) channel;
    			String strPath = this.mkdirDir(annoId + "/" + applId, channelSftp);
    			channelSftp.put(inputStream, strPath + fileName);
    			channelSftp.get(strPath + fileName);
    			try {
    				channelSftp.chmod(Integer.parseInt("775", 8), strPath + fileName);
    			}catch(Exception e) {
    				
    			}
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
        	try {
				if(channelSftp != null) channelSftp.disconnect();
				if(channel != null) channel.disconnect();
				if(session != null) session.disconnect();
				inputStream.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
		
	}
	
	private String mkdirDir(String path, ChannelSftp channelSftp) throws SftpException {

    	String[] pathArray = path.split("/");
    	String currentDirectory = "/aaa/bbb/files";
    	String totPathArray = "";
    	for(int i =0; i< pathArray.length; i++) {
    		totPathArray += pathArray[i] + "/";
			String currentPath = currentDirectory+ "/" + totPathArray;
    		try {
				channelSftp.mkdir(currentPath);
    			try {
    				channelSftp.chmod(Integer.parseInt("775", 8), currentPath);
    			}catch(Exception e) {
    				
    			}
				channelSftp.cd(currentPath);
			} catch (Exception e) {
				channelSftp.cd(currentPath);
			}
    	}
    	return currentDirectory+ "/" + totPathArray;

	}
	
	
}
