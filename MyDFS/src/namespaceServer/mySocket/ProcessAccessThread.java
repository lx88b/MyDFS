package namespaceServer.mySocket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class ProcessAccessThread extends Thread {
	private Socket socket = null;  
    private BufferedReader br = null;  
    private PrintWriter pw = null;
    
	public ProcessAccessThread(Socket s){
		socket = s;  
		try {  
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));  
			pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);  
			start();  
		} catch (Exception e) {  

			e.printStackTrace();  
		} 
	}
	
	public void run() {  
        while(true){  
            String str;  
            try {  
                str = br.readLine();  
                if(str.equals("END")){  
                    br.close();  
                    pw.close();  
                    socket.close();  
                    break;  
                }  
                //根据该请求处理
//                System.out.println("Client Socket Message:"+str);  
                pw.println("Message Received");  
                pw.flush();  
            } catch (Exception e) {  
                try {  
                    br.close();  
                    pw.close();  
                    socket.close();  
                } catch (IOException e1) {   
                    e1.printStackTrace();  
                }  
            }  
        }  
    }  
}
