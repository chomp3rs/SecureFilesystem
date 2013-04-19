import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;


public class TCPServer {

	static String [] seats = {"","","","","","","",""};
	static int seatsTaken = 0;
	static BinarySemaphore mutex = new BinarySemaphore(true);
	static final byte key[] = "hello".getBytes();
	public TCPServer(int numSeats){
		seats = new String[numSeats];
		for (int i = 0; i < numSeats; i++)
		{
			seats[i]="";
		}
		seatsTaken = 0;
	}
	
	public static void main(String[] args) {
        Socket rSocket;
        int port = 2018;
        try {
        	ServerSocket datasocket = new ServerSocket(port);
            while (true) {
                rSocket = datasocket.accept();
                System.out.println("Accepted socket");//debug line
        		try {
        	        BufferedReader din = new BufferedReader(new InputStreamReader(rSocket.getInputStream()));
                    System.out.println(din.readLine());//debug  line;
        		} catch (IOException e) {
        			e.printStackTrace();
        		}
                TCPRun t = new TCPRun(seats, mutex, rSocket, key);
                System.out.println("created new TCPRun");//debug line
                t.start();
				t.join();
            
            }
        } catch (SocketException se) {
            System.err.println(se);
        }catch (IOException e) {
            System.err.println(e);
        }catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}