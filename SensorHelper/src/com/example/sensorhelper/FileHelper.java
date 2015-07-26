package com.example.sensorhelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileHelper {
	
	public void fileBuilder(String filePath){
		File file=new File(filePath);
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void fileWriter(StringBuffer sb,String filePath){
		File file=new File(filePath);
		try {
			RandomAccessFile raf=new RandomAccessFile(file,"rw");
			raf.seek(file.length());
			raf.write(sb.toString().getBytes());
			raf.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void fileDeleter(String filePath){
		File file=new File(filePath);
		if(file.exists()){
			file.delete();
		}
	}

}
