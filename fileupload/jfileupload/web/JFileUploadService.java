package com.hr.common.util.fileupload.jfileupload.web;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Service;

import com.hr.common.dao.Dao;
import com.hr.common.logger.Log;


@Service("JFileUploadService")
public class JFileUploadService{

	@Inject
	@Named("Dao")
	private Dao dao;


	/**
	 * @return String
	 * @throws Exception
	 */
	public String jFileSequence() throws Exception {
		Log.Debug();
		return dao.getMap("jFileSequence", new HashMap() ).get("seq").toString();
	}
	
	public Map<?, ?> jFileCount(Map paramMap) throws Exception {
		Log.DebugStart();
		return (Map<?, ?>) dao.getMap("jFileCount", paramMap);
	}

	/**
	 * 업로드 FILE 조회 Service
	 * 
	 * @param paramMap
	 * @return List
	 * @throws Exception
	 */
	public List jFileList(Map paramMap) throws Exception {
		Log.Debug();
		return (List<?>) dao.getList("jFileList", paramMap);
	}

	public boolean fileStoreSave(List<Map<?, ?>> saveList) throws Exception {
		Log.DebugStart();

		if (saveList != null && saveList.size() > 0) {
			int r = 0;
			for (Map<?, ?> map : saveList) {
				r += dao.create("tsys972save", map);
			}
			if (r == -1) {
				return false;
			}
		}
		return true;
	}

	public boolean fileStoreDelete(List<Map<?, ?>> list) throws Exception {
		Log.DebugStart();

		int r = 0;

		if (list != null && list.size() > 0) {
			for (Map<?, ?> map : list) {
				r += dao.delete("tsys972Delete", map);
			}
			if (r == -1) {
				return false;
			}
			
		}
		return true;
	}
	
	/**
	 * 업로드 FILE 조회 Service
	 * 
	 * @param paramMap
	 * @return List
	 * @throws Exception
	 */
	public Map<?, ?> jFileMap(Map paramMap) throws Exception {
		Log.Debug();
		return (Map<?, ?>) dao.getMap("jFileMap", paramMap);
	}
	
	
	/**
	 * 파일 삭제 Service
	 * 
	 * @param convertMap
	 * @return int
	 * @throws Exception
	 */
	public int jFileDel(Map<?, ?> convertMap) throws Exception {
		Log.Debug();
		int cnt=0;
		if( ((List<?>)convertMap.get("deleteRows")).size() > 0){
			cnt += dao.delete("jFileDelete", convertMap);
		}
		return cnt;
	}

	/**
	 * 저장 Service
	 *
	 * @param convertMap
	 * @return int
	 * @throws Exception
	 */
	public int jFileSave(Map<?, ?> convertMap) throws Exception {
		Log.Debug();

		int cnt=0;
		if( ((List<?>)convertMap.get("mergeRows")).size() > 0){
			cnt += dao.update("jFileUpload", convertMap);
		}

		return cnt;
	}

	
	
	
}