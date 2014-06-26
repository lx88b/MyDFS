/**
 * 处理增加节点的线程，一次只能处理一个节点
 */
package namespaceServer.mySocket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ProcessAccessThread extends Thread {
	private ServerSocket s = null;
	private Socket socket = null;
	private BufferedReader br = null;  
    private PrintWriter pw = null;
    
	public ProcessAccessThread(){
		try{
			start();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		try {
			s = new ServerSocket(6001);
			//服务器端始终等待接收请求，服务器端为6000接口
			while(true){
				socket = s.accept();
				br = new BufferedReader(new InputStreamReader(socket.getInputStream()));  
				pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);  
				while(true){  
					String str;  
					try {  
						str = br.readLine();  
						if(str.equals("END")||str==null){  
							br.close();  
							pw.close();  
							socket.close();  
							break;  
						}  
						//根据该请求处理
						//                System.out.println("Client Socket Message:"+str);  
						else if(str.equals("AddNode")){
							this.addNode();
						}
						else{}
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
		} catch (Exception e) {
			try {
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}finally{
			try {
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    }
	

	
	private void addNode() {
		
	}
}
