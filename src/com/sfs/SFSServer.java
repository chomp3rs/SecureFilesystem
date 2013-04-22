package com.sfs;
/**
 * SslReverseEchoerRevised.java
 * Copyright (c) 2005 by Dr. Herong Yang
 */
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ServerSocketFactory;
import javax.net.ssl.*;

import org.apache.log4j.Logger;
import org.bouncycastle.openssl.PEMReader;

import com.sfs.util.Properties;
import com.sfs.util.Utilities;
public class SFSServer {
	
	private static Logger logger = Logger.getLogger(SFSServer.class);
   public static void main(String[] args) {
	   
	   if (args.length<2) {
	         System.out.println("Usage:");
	         System.out.println("java SFSServer <serverName> <keystorePassword>");
	         return;
	      }
	   
	   String serverName = args[0];
	   String fileName = Properties.serverCertslocation + serverName + ".cer";
		if(!(Utilities.checkIfFileExists(fileName)))
		{
			// File does not exist, talk to the CA
			logger.info("File does not exist, talk to the CA for " + serverName);

			Socket caServer;
			try {
				caServer = new Socket("localhost",Properties.caPort);
				BufferedReader socketReader =  new BufferedReader(new InputStreamReader(caServer.getInputStream ()));        
				PrintStream caServerSocketWriter = new PrintStream(caServer.getOutputStream());

				caServerSocketWriter.println(Properties.newCertCommand + ":" + serverName);


				KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
				keyPairGenerator.initialize(1024);
				KeyPair KPair = keyPairGenerator.generateKeyPair();

				String privKeyFileName = Properties.serverPrivCertslocation + serverName + ".pem";
				String keyStorePassword = "sfs" + serverName;
				Utilities.pemEncodeToFile(privKeyFileName, KPair.getPrivate(), null);
				PublicKey publicK = KPair.getPublic();

				logger.info("Public key for server " + serverName + "is :" + publicK.toString());

				byte[] publicKByteArr = publicK.getEncoded();
				logger.info("Size :" + publicKByteArr.length);



				DataOutputStream dataOut = new DataOutputStream(caServer.getOutputStream());
				dataOut.write(publicKByteArr);
				caServerSocketWriter.flush();

				
				// Check if certificate was generated successfully
				
				int certGenStatus = Integer.parseInt(socketReader.readLine());
				if(certGenStatus == Properties.certGenSuccess)
				{
					logger.info("Certificate generated successfully");
					int certGenLength = Integer.parseInt(socketReader.readLine());
					logger.info("Certificate length :" + certGenLength);
					
					byte[] certBytes = new byte[certGenLength];
	                DataInputStream clientReader = new DataInputStream(caServer.getInputStream());     
	                int byteCount = clientReader.read(certBytes);
	                byte[] certByte = new byte[byteCount];
	                for(int i=0;i<byteCount;i++)
	                {
	                    certByte[i] = certBytes[i];
	                }
	                File targetFile = new File(fileName);
	                FileOutputStream fos = new FileOutputStream(targetFile);
	                fos.write(certBytes, 0, byteCount);
	                fos.close();
	                
	                // Create the keystore
	                
	                KeyStore ks = KeyStore.getInstance("JKS");
	                ks.load( null, null ); 
	                FileInputStream fis = new FileInputStream(fileName); 
	                BufferedInputStream bis = new BufferedInputStream(fis); 
	                CertificateFactory cf = CertificateFactory.getInstance( "X.509" ); 
	                Certificate cert = null; 
	                cert = cf.generateCertificate( bis );  
	                ks.setCertificateEntry( serverName + "Cert", cert ); 
	                String ksFile = Properties.serverKeyStoreLocation + serverName + ".jks";
	                ks.store( new FileOutputStream( ksFile ), args[1].toCharArray() );  
	                
				}
					
				else
				{
					logger.error("Error in certificate generation");
				}
				caServer.close();


			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
		}

		else
		{
			logger.info("Certificate exists : Server Side");
			int portNumber = 9997;
            try
            {     
            	String serverCert = Properties.serverCertslocation + serverName + ".cer";
                FileInputStream inputStream = new FileInputStream(serverCert);
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                X509Certificate cert = (X509Certificate) cf.generateCertificate(inputStream);
              
                String privKeyFileName = Properties.serverPrivCertslocation + serverName + ".pem";
                PEMReader kr = new PEMReader(new FileReader(privKeyFileName), null);                         
                KeyPair key = (KeyPair) kr.readObject();
                PrivateKey serverPrivateKey = key.getPrivate();
                PublicKey serverPublicKey = key.getPublic();
                KeyStore ksKeys = KeyStore.getInstance("JKS");
                String ksFile = Properties.serverKeyStoreLocation + "server.jks";
                ksKeys.load(new FileInputStream(ksFile),args[1].toCharArray());
                ksKeys.setCertificateEntry("serverCert", cert);                                                
                ksKeys.setKeyEntry("serverKey", key.getPrivate(),args[1].toCharArray(), new Certificate[]{cert});                        
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(ksKeys, args[1].toCharArray());
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(ksKeys);
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
                ServerSocketFactory factory = sslContext.getServerSocketFactory();
                ServerSocket serverSocket = (SSLServerSocket)factory.createServerSocket(9997);  
                while(true);
            }
            catch(Exception ex)
            {
                System.out.println(ex.getMessage());
            }
            
            logger.info("Server started at 9997");
		}

   }
   private static void printSocketInfo(SSLSocket s) {
      System.out.println("Socket class: "+s.getClass());
      System.out.println("   Remote address = "
         +s.getInetAddress().toString());
      System.out.println("   Remote port = "+s.getPort());
      System.out.println("   Local socket address = "
         +s.getLocalSocketAddress().toString());
      System.out.println("   Local address = "
         +s.getLocalAddress().toString());
      System.out.println("   Local port = "+s.getLocalPort());
      System.out.println("   Need client authentication = "
         +s.getNeedClientAuth());
      SSLSession ss = s.getSession();
      try {
         System.out.println("Session class: "+ss.getClass());
         System.out.println("   Cipher suite = "
            +ss.getCipherSuite());
         System.out.println("   Protocol = "+ss.getProtocol());
         System.out.println("   PeerPrincipal = "
            +ss.getPeerPrincipal().getName());
         System.out.println("   LocalPrincipal = "
            +ss.getLocalPrincipal().getName());
      } catch (Exception e) {
         System.err.println(e.toString());
      }
   }
   private static void printServerSocketInfo(SSLServerSocket s) {
      System.out.println("Server socket class: "+s.getClass());
      System.out.println("   Socker address = "
         +s.getInetAddress().toString());
      System.out.println("   Socker port = "
         +s.getLocalPort());
      System.out.println("   Need client authentication = "
         +s.getNeedClientAuth());
      System.out.println("   Want client authentication = "
         +s.getWantClientAuth());
      System.out.println("   Use client mode = "
         +s.getUseClientMode());
   } 
}