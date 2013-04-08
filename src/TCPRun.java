import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class TCPRun extends Thread{
	
	int seatsTaken = 0;
	String [] seats;
	BinarySemaphore mutex;
    String com;
    Socket rSocket;
	
	public TCPRun(String[] s, BinarySemaphore mt, Socket sock){
        rSocket = sock;
		seats = s;
		mutex = mt;
		try {
	        BufferedReader din = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			com =  din.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Read command: " + com);//debug line
	}

	@Override
	public void run() {
		System.out.println("Started thread");//debug line
		String command [];
		command = com.split(" ");
		//start of the parsing for commands
		if(command[0].equals("reserve")){
			boolean sTaken = false;
			mutex.P();
			for (int i = 0; i < seats.length;i++){
				if (!seats[i].equals(""))
					seatsTaken++;
			}
			if(seatsTaken == seats.length){
				returnPack("Sold out - No seat available.");
				sTaken = true;
			}else{
				for(int i=0; i<seats.length; ++i){
					if(!seats[i].equals("") && seats[i].equals(command[1].trim())){
						returnPack("Seat alraedy booked against the name provided.");
						sTaken = true;
					}
				}
				if(!sTaken){
					int i;
					for(i=0; i<seats.length; ++i){
						if(seats[i].equals("")){
							seats[i] = command[1].trim();
							seatsTaken++;
							break;
						}
					}
					returnPack("Seat assigned to you is " + i);
				}
			}mutex.V();
		}else if(command[0].equals("bookSeat")){
			int seatNum = Integer.parseInt(command[2].trim());
			boolean sTaken = false;
			mutex.P();
			for(int i=0; i<seats.length; ++i){
				if(!seats[i].equals("") && seats[i].equals(command[1].trim())){
					returnPack("Seat alraedy booked against the name provided.");
					sTaken = true;
				}
			}
			if(seats[seatNum].equals("") && !sTaken){
				seatsTaken++;
				seats[seatNum] = command[1];
				returnPack(seatNum + " is available.");
			}else if(!sTaken){
				returnPack(seatNum + " is not available.");
			}
			mutex.V();
		}else if(command[0].equals("search")){
			mutex.P();
			for(int i=0; i<seats.length; ++i){
				if(seats[i].equals(command[1].trim())){
					returnPack(Integer.toString(i));
					break;
				}
				else if(i == seats.length-1)
				{
					returnPack("Not found");
				}
			}
//			for (int i = 0; i < seats.length; i++)
//			{
//				System.out.print(seats[i]+ ", ");
//			}
//			System.out.println();
			mutex.V();
		}else if(command[0].equals("delete")){
			mutex.P();
			for(int i=0; i<seats.length; ++i){
				if((seats[i] != null || seats[i].length() == 0) && seats[i].equals(command[1].trim())){
					seats[i] = "";
					seatsTaken--;
					returnPack(command[1].trim() + " was deleted");
					break;
				}
				else if (i == seats.length-1)
				{
					returnPack("No reservation found for " + command[1].trim());
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
}
