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

package eu.artist.migration.pt.cotroller;


public class ProfilingTool {

	public static void main(String[] args){
		if (args.length==1){
			System.out.println("Please enter argument: application/benchmarks");
		}else if (args.length==2){
			if (args[1].equals("application")){
				new ControllerUserApp();
			}
			else if (args[1].equals(("benchmarks"))){
				new ControllerBench();
			}
			else {
				System.out.println("Mispelled input parameter... Must choose the profiling type by providing the argument: \"application\" or \"benchmarks\"");
			}
		}else if (args.length==3){
			if (args[1].equals("application")){
				new ControllerUserApp(args[0], args[2]);
			}
			else if (args[1].equals(("benchmarks"))){
				new ControllerBench(args[0], args[2]);
			}
			else {
				System.out.println("Mispelled input parameter... Must choose the profiling type by providing the argument: \"application\" or \"benchmarks\"");
			}
		}
		else System.out.println("Wrong number of "
				+ "arguments!");
	}
	
	public ProfilingTool(){
		
	}
}
