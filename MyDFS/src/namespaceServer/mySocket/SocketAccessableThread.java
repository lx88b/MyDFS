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
			//服务器端始终等待接收请求，服务器端为6000接口
			while(true){
				socket = s.accept();
				//在这里调用处理请求的方法线程
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
