/*
 * Copyright 2014 ICCS/NTUA
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Initially developed in the context of ARTIST EU project www.artist-project.eu
 *
 */

package eu.artist.migration.pt.executor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Arrays;



public abstract class UnixProcessUtils{
		
	public static int getUnixPID(Process process) throws Exception
	{
		Class<? extends Process> cl = process.getClass();
	    if (cl.getName().equals("java.lang.UNIXProcess"))
		{
	    	
	        Field field = cl.getDeclaredField("pid");
	        field.setAccessible(true);
	        Object pidObject = field.get(process);
	        return (Integer) pidObject;
	    } else{
	        throw new IllegalArgumentException("Needs to be a UNIXProcess");
	    }
	}
		
		
	public static void killUnixProcess(Process process, String pass, String processName) throws Exception
	{
	    //int pid = getUnixPIDByName(process);
        //SystemCommandExecutor commandExecutor = new SystemCommandExecutor(Arrays.asList("sudo", "-S", "kill", "-SIGINT", Integer.toString(pid)), pass);
	    //commandExecutor.start();
        
        
	  //fot code
	    //Process p = Runtime.getRuntime().exec("echo "+pass+" | "+"sudo -S kill -SIGKILL "+Integer.toString(pid) );
	    //System.out.println("@@Sending command: "+"echo "+pass+" | "+"sudo -S kill -SIGKILL "+Integer.toString(pid) );
			String [] StrPid = getUnixPIDByName(processName);
			for (int i=0; i<StrPid.length; i++) {
				if (StrPid.equals(""))
					continue;
				System.out.println("******killing process:"+StrPid[i]+"....");
				SystemCommandExecutor commandExecutor = new SystemCommandExecutor(Arrays.asList("sudo", "-S", "kill", "-SIGINT", StrPid[i]), pass);
				commandExecutor.start();
			}
	}
	
	//fotis method, the existing one does not return PIDSTAT/tshark ids, but a wrong sudo process ID!!
	static String [] getUnixPIDByName (String ProcessName) {
		
		String [] result;
	    Process p=null;
		try {
			p = Runtime.getRuntime().exec("ps -C "+ProcessName+" -o pid");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    
	    BufferedReader reader = 
                new BufferedReader(new InputStreamReader(p.getInputStream()));
		StringBuilder builder = new StringBuilder();
		String line = null;
		try {
			while ( (line = reader.readLine()) != null) {
			builder.append(line);
				builder.append(System.getProperty("line.separator"));
				}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String processIDs = builder.toString();
		
		processIDs = processIDs.replace("PID", "");
		processIDs = processIDs.replaceFirst("\n", "");
		if (ProcessName.equals("pidstat")) {
			result = new String[1];
			result[0] = processIDs.replace("\n", "");

		}	
		else {
			result = processIDs.split("\n");

		}

		return result;
	}
	
		
}
