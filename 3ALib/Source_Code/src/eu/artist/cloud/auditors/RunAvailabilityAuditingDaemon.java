/*
 * Copyright (c) 2014 ICCS
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0
 * which
accompanies this distribution, and is available at
 *
http://www.apache.org/licenses/LICENSE-2.0.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *
George Kousiouris

 * Initially developed in the context of ARTIST EU project
www.artist-project.eu
 *//**
 * 
 */
package eu.artist.cloud.auditors;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Set;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jclouds.ContextBuilder;
//import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Closeables;
import com.google.inject.Module;
import org.jclouds.ContextBuilder;
//import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;


import java.io.Closeable;
import java.io.IOException;
import java.util.Set;
/**
 * @author geo
 *
 */
public class RunAvailabilityAuditingDaemon {

	/**
	 * @param args
	 */
	   
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	 
	    
		
		
		
		int quantumOfTime=1; //dependent on provider-check to see how to abstract better

		InputStream input = null;
		Properties prop2 = new Properties();
		try {




			input = new FileInputStream("/run/secrets/"+args[0]+"_"+args[1]+"_"+args[2]+"_"+args[3]);
		//	input =new FileInputStream("3alibconfig.properties");
			// load a properties file
			prop2.load(input);
			//abstracted part-differences are hidden inside the implementation of each Auditor
			AvailabilityAuditor thisauditor=new AvailabilityAuditor();
			thisauditor.setProviderName(prop2.getProperty("ProviderName"));
			thisauditor.setServiceName(prop2.getProperty("ServiceName"));
			thisauditor.setUser(prop2.getProperty("user"));
			thisauditor.setApikey(prop2.getProperty("APIkey"));
			thisauditor.setDbhost("147.102.19.184");
			//thisauditor.setDbhost(prop2.getProperty("databaseIP"));
			thisauditor.setPath(prop2.getProperty("Path"));
			thisauditor.setLibuser(args[0]);  //<---------args[0]
			thisauditor.run();
             
		
			
			//calculate Availability
			/*
			AbstractedAvailabilityCalculator calc=new AbstractedAvailabilityCalculator();
			calc.calculateAvailability(prop2.getProperty("databaseIP"), quantumOfTime);
			 */

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}


	/*
		AvailabilityAuditor googleauditor=new AvailabilityAuditor();
		thisauditor.setProviderName("google");
		thisauditor.setServiceName("-compute-engine");
		thisauditor.setUser("");
		thisauditor.setApikey("");
		thisauditor.run();
	 */



	public static void createPropertiesFile(String[] args){

		//properties



	}
	/*
	public static void readPropertiesFile(){
		InputStream input = null;
		Properties prop2 = new Properties();
		try {

			input = new FileInputStream("3alibconfig.properties");

			// load a properties file
			prop2.load(input);

			// get the property value and print it out
			System.out.println(prop2.getProperty("databaseIP"));
			System.out.println(prop2.getProperty("user"));
			System.out.println(prop2.getProperty("APIkey"));

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}*/

}

