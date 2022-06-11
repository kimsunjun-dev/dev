package com.hr.common.excelTxtFilePas;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelFileType {

	public static Workbook getWorkbook(String fileString){

		FileInputStream fis = null;

		try{
			fis= new FileInputStream(fileString);
		}catch(FileNotFoundException e){
			throw new RuntimeException(e.getMessage(),e);
		}

		Workbook wb = null;

		if ( fileString.toUpperCase().endsWith(".XLS")){
			try{
				wb = new HSSFWorkbook(fis);
			}catch(IOException e){
				throw new RuntimeException(e.getMessage(),e);
			}
		}else if ( fileString.toUpperCase().endsWith(".XLSX")){
			try{
				wb = new XSSFWorkbook(fis);
			}catch(IOException e){
				throw new RuntimeException(e.getMessage(),e);
			}
		}

		return wb;
	}
}
