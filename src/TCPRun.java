import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


public class TCPRun extends Thread{
	
	int seatsTaken = 0;
	String [] seats;
	BinarySemaphore mutex;
    String com;
    Socket rSocket;
    byte[] pubKey;
	
	public TCPRun(String[] s, BinarySemaphore mt, Socket sock, byte[] key){
        rSocket = sock;
		seats = s;
		mutex = mt;
		pubKey = key;
		try {
	        BufferedReader din = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			com =  din.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Read command: " + com);//debug line
	}

	@Override
	//userid command additional1(filename or username) additional2(file contents as byte string)
	//do NOT have the file extention on the filename
	//filename.txt and filename-meta.txt where meta will contain a list of users that have access
	//both files will be encrypted with server public key 
	public void run() {
		System.out.println("Started thread");//debug line
		String command [];
		command = com.split(" ");
		//start of the parsing for commands
		if(command[1].equals("get")){
			File file = new File(command[2]+".txt");
			File filemeta = new File(command[2] + "-meta.txt");
			if (file.exists() && filemeta.exists()) {
				try{
					String encTextMeta, plainTextMeta, plainText, encText;
					String metaData[];
					BufferedReader br = new BufferedReader(new FileReader(command[2]+ "-meta.txt"));
					encTextMeta = br.readLine();
					br.close();
					plainTextMeta = decrypt(encTextMeta,pubKey);
					metaData = plainTextMeta.split("\n");
					for(int i=1; i<metaData.length-1; ++i){
						if(command[0].equals(metaData[i])){
							BufferedReader br2 = new BufferedReader(new FileReader(command[2]+ ".txt"));
							encText = br2.readLine();
							br2.close();
							plainText = decrypt(encText,metaData[0].getBytes());
							returnPack(plainText);
						}
					}
				} catch (Exception e){
					
				}
			}
		}else if(command[1].equals("put")){
			try{
				String encText;
				String encMeta;
				byte[] hash = hasher(command[3]);

				//encrypt the file and the meta file
				encText = encrypt(command[3], hash);
				encMeta = encrypt(hash.toString() + "\n" + command[0], pubKey);

				File file = new File(command[2]+".txt");
				File filemeta = new File(command[2] + "-meta.txt");
				if (!file.exists()) {
					file.createNewFile();
				}
				if (!filemeta.exists()) {
					filemeta.createNewFile();
				}
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				FileWriter fw2 = new FileWriter(filemeta.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				BufferedWriter bw2 = new BufferedWriter(fw2);
				bw.write(encText);
				bw2.write(encMeta);
				bw.close();
				bw2.close();
				returnPack("Successfully put " + command[2]);
			} catch (Exception e){
				
			}
		}else if(command[0].equals("delegate")){
			mutex.P();
			File filemeta = new File(command[2] + "-meta.txt");
			if (filemeta.exists()) {
				try{
					String encTextMeta, plainTextMeta, encMeta;
					String metaData[];
					BufferedReader br = new BufferedReader(new FileReader(command[2]+ "-meta.txt"));
					encTextMeta = br.readLine();
					br.close();
					plainTextMeta = decrypt(encTextMeta,pubKey);
					plainTextMeta = plainTextMeta + "\n" + command[2];
					encMeta = encrypt(plainTextMeta, pubKey);
					FileWriter fileWritter = new FileWriter(filemeta.getName());
	    	        BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
	    	        bufferWritter.write(encMeta);
	    	        bufferWritter.close();
				} catch (Exception e){
					
				}
			}
			mutex.V();
		}else{
			returnPack("You made a typo!");
		}
	}
	
	public void returnPack(String message){
		try {
			PrintWriter pout = new PrintWriter(rSocket.getOutputStream());
	    	pout.println(message);
            pout.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String encrypt(String data, byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
	{
		    String strCipherText = new String();

			SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
			Cipher aesCipher = Cipher.getInstance("AES");

			aesCipher.init(Cipher.ENCRYPT_MODE,secretKey);


			byte[] byteDataToEncrypt = data.getBytes();
			byte[] byteCipherText = aesCipher.doFinal(byteDataToEncrypt); 
			strCipherText = new BASE64Encoder().encode(byteCipherText);
		
		    return strCipherText;
	}

	public String decrypt(String cipherText , byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException
	{
		    String strDecryptedText= new String();
        	SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
			Cipher aesCipher = Cipher.getInstance("AES");
			aesCipher.init(Cipher.DECRYPT_MODE,secretKey);
			byte[] byteDecryptedText = aesCipher.doFinal(new BASE64Decoder().decodeBuffer(cipherText));
			strDecryptedText = new String(byteDecryptedText);
        	return strDecryptedText;
	}
	
    public static byte[] hasher(String fi) {
    	byte[] hash = {-1};
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			hash = digest.digest(fi.getBytes("UTF-8"));
	        
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hash;
    }
}
