package com.hr.tim.awaytime.cardInOutUpload;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Service;

import com.hr.common.dao.Dao;
import com.hr.common.excelTxtFilePas.ExcelRead;
import com.hr.common.excelTxtFilePas.ExcelReadOption;
import com.hr.common.logger.Log;

/**
 * 연차휴가계획관리 Service
 *
 * @author bckim
 *
 */
@Service("CardInOutUploadService")
public class CardInOutUploadService{

	@Inject
	@Named("Dao")
	private Dao dao;

	/**
	 * 조회 Service
	 *
	 * @param paramMap
	 * @return List
	 * @throws Exception
	 */
	public List<?> getCardInOutUploadServiceList(Map<?, ?> paramMap) throws Exception {
		Log.Debug();
		return (List<?>) dao.getList("getCardInOutUploadServiceList", paramMap);
	}

	/**
	 * 저장 Service
	 *
	 * @param convertMap
	 * @return int
	 * @throws Exception
	 */
	public int saveCardInOutUploadService(Map convertMap) throws Exception {
		Log.Debug();
		int cnt=0;
		if( ((List<?>)convertMap.get("deleteRows")).size() > 0){
			cnt += dao.delete("deleteCardInOutUpload", convertMap);
		}
		
		if( ((List<?>)convertMap.get("insertRows")).size() > 0){
			List<Map<String, Object>> insertList = (List<Map<String,Object>>)convertMap.get("insertRows");
			for(int i=0; i<insertList.size(); i++) {
				insertList.get(i).put("ssnEnterCd", convertMap.get("ssnEnterCd"));
				insertList.get(i).put("ssnSabun", convertMap.get("ssnSabun"));
				cnt += dao.update("insertCardInOutUpload", insertList.get(i));
			}
		}

		if( ((List<?>)convertMap.get("updateRows")).size() > 0){
			List<Map<String, Object>> updateList = (List<Map<String,Object>>)convertMap.get("updateRows");
			for(int i=0; i<updateList.size(); i++) {
				updateList.get(i).put("ssnEnterCd", convertMap.get("ssnEnterCd"));
				updateList.get(i).put("ssnSabun", convertMap.get("ssnSabun"));
				cnt += dao.update("updateCardInOutUpload", updateList.get(i));
			}
			
		}
		return cnt;
	}
	
	/**
	 * 일괄삭제 Service
	 *
	 * @param convertMap
	 * @return int
	 * @throws Exception
	 */
	public int deleteCardInOutUploadConfirm(Map convertMap) throws Exception {
		Log.Debug();
		int cnt = dao.delete("deleteCardInOutUploadConfirm", convertMap);
		return cnt;
	}
	
	public int excelUpload(Map<String, Object> convertMap) throws Exception {
		Log.Debug();
		int cnt=0;
		String excelFile = (String) convertMap.get("rPath");
		ExcelReadOption ro = new ExcelReadOption();
		
		ro.setFilePath(excelFile);
		ro.setOutputColumns("A", "B", "C", "D", "E", "F");
		ro.setStartRow(2);

		String ymd = "";
		String hms = "";

		int baseDataCnt = 100;
		int excpCnt = result.size() % baseDataCnt;

		List<Map<String, String>> result = ExcelRead.read(ro);
		List<Map<String,String>> insertRows = new ArrayList<Map<String,String>>();
		
		int loopCnt = 1;
		for(int i =0; i < result.size(); i++) {
			Map<String,String> map = result.get(i);
			map.put("ssnEnterCd",convertMap.get("ssnEnterCd").toString());
			map.put("ssnSabun",convertMap.get("ssnSabun").toString());
			ymd = map.get("A"); 
			if(ymd == null || "".equals(ymd)) {
				continue;
			}
			ymd = ymd.replaceAll("[^0-9]","");
			map.put("ymd",ymd);
			hms = map.get("B"); 
			hms = hms.replaceAll("[^0-9]","");
			map.put("hms",hms);
			map.put("sabun",map.get("C").toString().trim());
			map.put("acuCd",map.get("E").toString().trim());
			map.put("inoutNm",map.get("F").toString().trim());
			insertRows.add(map);
			
			//baseDataCnt건 마다 저장한다.
			if((i+1) == (loopCnt * baseDataCnt )) {
				Map<String,Object> mapResult = new HashMap<String,Object>();
				mapResult.put("ssnEnterCd", convertMap.get("ssnEnterCd"));
				mapResult.put("ssnSabun", convertMap.get("ssnSabun"));
				mapResult.put("insertRows", insertRows);
				cnt += dao.update("isnertCardInOutUploadExcelAll", mapResult);
				insertRows.clear();
				loopCnt++;
			}
			//baseDataCnt건 만큼 저장 후 나머지 건수 저장
			if(excpCnt != 0 && result.size() == (i+1)) {
				Map<String,Object> mapResult = new HashMap<String,Object>();
				mapResult.put("ssnEnterCd", convertMap.get("ssnEnterCd"));
				mapResult.put("ssnSabun", convertMap.get("ssnSabun"));
				mapResult.put("insertRows", insertRows);
				cnt += dao.update("isnertCardInOutUploadExcelAll", mapResult);
			}
		}
		return cnt;
	}
}