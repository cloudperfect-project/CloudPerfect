package eu.artist.migration.ct.common;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;

//FOT: new class, in order to read VM-id from profiler application-info.txt file 

public class ProfilerConfigurationReader {
		
	public static String getVMid(String confFile){
		
		try{
			BufferedReader bufferedReader;
			bufferedReader = new BufferedReader(new FileReader(confFile));
			
			String line;
			ArrayList<String> valuesP = new ArrayList<String>();
			
			if ((line=bufferedReader.readLine())==null){
				bufferedReader.close();
				throw new Exception("Wrong number of arguments in file");
			}
			
			if ((line=bufferedReader.readLine())!=null){
				valuesP=new ArrayList<String>(Arrays.asList(line.split(",")));
				int i=valuesP.size();
				if (i!=2){
					bufferedReader.close();
					throw new Exception("Wrong number of arguments for Pidstat");

				}
				String vm_id=valuesP.get(0);
				bufferedReader.close();
				return vm_id;
			} 
			else {
				bufferedReader.close();
				throw new Exception("Wrong number of arguments in file");
			}	
		} catch(Exception e){
				System.out.println(e);
				return "";

		}
	}

}
