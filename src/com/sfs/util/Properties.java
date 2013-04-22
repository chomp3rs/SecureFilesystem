package com.sfs.util;

public class Properties {

	public static final String clientCertslocation = "/home/gayathri/workspace-scsp2/SFSProject/client/certs/";
	public static final String clientPrivCertslocation = "/home/gayathri/workspace-scsp2/SFSProject/client/certs/private/";
	public static final String serverCertslocation = "/home/gayathri/workspace-scsp2/SFSProject/server/certs/";
	public static final String serverPrivCertslocation = "/home/gayathri/workspace-scsp2/SFSProject/server/certs/private/";
	public static final String clientKeyStoreLocation = "/home/gayathri/workspace-scsp2/SFSProject/client/ks/";
	public static final String serverKeyStoreLocation = "/home/gayathri/workspace-scsp2/SFSProject/server/ks/";
	public static final String newCertCommand = "NEWCERT";
	public static final int publicKEncodedSize = 162;
	public static final String caKeystorePwd = "ca@8903";
	public static final String caKeystore = "/home/gayathri/workspace-scsp2/SFSProject/ca/ca.p12";
	public static final String issuerDN = "C=US, ST=GA, L=Atlanta, O=SFS, CN=SFS CA/emailAddress=gayathri6@gatech.edu";
	public static final String subjectDN = "CN=%s, ST=GA, C=US/emailAddress=gayathri.rad@gmail.com, O=SFS";
	public static final int certGenSuccess = 101;
	public static final int certGenFailure = 501;
	public static final int caPort = 9999;
	
	
}
