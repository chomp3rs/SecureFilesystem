package com.sfs.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.Enumeration;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import com.sfs.SFSClient;

public class Utilities {
	
	private static Logger logger = Logger.getLogger(SFSClient.class);

	public static boolean checkIfFileExists(String location)
	{
		File testFile = new File(location);

		if(testFile.exists()){
			return true;
		}

		return false;
	}
	
	   public static PublicKey getPublicKey(byte[] publicKeyBytes) {
	        PublicKey retPubkey = null;
	        try {
	        	retPubkey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyBytes));
	            
	        } catch (Exception e) {
	            logger.error(" Problem occured while creating PublicKey from provided public key bytes,Exception:" + e.getMessage());
	        }
	        return retPubkey;
	    }
	   
	   
	   public static void pemEncodeToFile(String filename, Object obj, char[] password) throws Exception{
		    PEMWriter pw = new PEMWriter(new FileWriter(filename));
		       if (password != null && password.length > 0) {
		           pw.writeObject(obj, "DESEDE", password, new SecureRandom());
		       } else {
		           pw.writeObject(obj);
		       }
		       pw.flush();
		       pw.close();
		    }
	   
	   
	   public static X509Certificate generateCertificate(PublicKey pubKey , String subjectDN)
	   {
	        X509Certificate genCertficate = null;
			try {
				
				X509V3CertificateGenerator v3CertGen = new X509V3CertificateGenerator(); 
				v3CertGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
		        v3CertGen.setIssuerDN(new X509Principal(Properties.issuerDN));
		        v3CertGen.setNotBefore(new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30));
		        v3CertGen.setNotAfter(new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 365*10)));
		        v3CertGen.setSubjectDN(new X509Principal(subjectDN));

		        v3CertGen.setPublicKey(pubKey);
		        
		        KeyStore ks = KeyStore.getInstance("PKCS12");
		        FileInputStream fis = new FileInputStream(Properties.caKeystore);
		        ks.load(fis, Properties.caKeystorePwd.toCharArray());
                Key key = ks.getKey("caCert",Properties.caKeystorePwd.toCharArray());
		        v3CertGen.setSignatureAlgorithm("MD5withRSAEncryption");
		        genCertficate = v3CertGen.generateX509Certificate((PrivateKey) key);

			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SignatureException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CertificateEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (KeyStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnrecoverableKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return genCertficate;
	   }


}
