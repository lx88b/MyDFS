package namespaceServer.mySocket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketAccessableThread extends Thread {
	
	private ServerSocket s = null;
	private Socket socket = null;
	
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
				//��������ô�������ķ����߳�
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

}
