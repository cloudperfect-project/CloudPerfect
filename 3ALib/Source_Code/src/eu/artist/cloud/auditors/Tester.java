package eu.artist.cloud.auditors;


import java.io.*;
import java.util.List;

import org.jclouds.ContextBuilder;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;

public class Tester {

	
	
	public static void main(String[] args) throws IOException {
	
		JCloudsExecutorClient cli=new JCloudsExecutorClient();

		ComputeServiceContext computeServiceContext=cli.AbstractedGetServers("0b6a0cd2-4cfb-4ec8-83c4-4ba8577445fc","vZwSfqIaZ0WgCV79UCJHhcxTCj1CQuu0XGMW85Rbo0w=","azurecompute-arm");
		ComputeService computeService=computeServiceContext.getComputeService();
		computeService.listNodes();

		

		
	}
}
