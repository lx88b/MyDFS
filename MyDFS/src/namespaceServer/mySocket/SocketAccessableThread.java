/**
 * �������ݲ������̣߳�һ��ֻ�ܽ���һ����������
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

public class SocketAccessableThread extends Thread {
	
	private ServerSocket s = null;
	private Socket socket = null;
	private BufferedReader br = null;  
    private PrintWriter pw = null;
	
	public SocketAccessableThread(){
		try{
			start();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		try {
			s = new ServerSocket(6000);
			//��������ʼ�յȴ��������󣬷�������Ϊ6000�ӿ�
			while(true){
				socket = s.accept();
				br = new BufferedReader(new InputStreamReader(socket.getInputStream()));  
				pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);  
				//��������ô�������ķ����߳�
//				new ProcessAccessThread(socket);
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
		                //���ݸ�������
//		                System.out.println("Client Socket Message:"+str);  
		                else if(str.equals("Add")){
		                	this.addFile();
		                }
		                else if(str.equals("Get")){
		                	this.getFile();
		                }
		                else if(str.equals("Del")){
		                	this.deleteFile();
		                }
		                else if(str.equals("Exist")){
		                	this.existFile();
		                }
		                else if(str.equals("Size")){
		                	this.sizeFile();
		                }
		                else if(str.equals("Mkdir")){
		                	this.mkdir();
		                }
		                else if(str.equals("Deldir")){
		                	this.delDir();
		                }
		                else if(str.equals("List")){
		                	this.list();
		                }
		                else if(str.equals("Append")){
		                	this.append();
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
	
	private void addFile() {
		
	}
	
	private void getFile() {
		
	}
	
	private void deleteFile() {
		
	}
	
	private void existFile() {
		
	}
	
	private void sizeFile() {
		
	}
	
	private void mkdir() {
		
	}
	
	private void delDir() {
		
	}
	
	private void list() {
		
	}
	
	private void append() {
		
	}

}
