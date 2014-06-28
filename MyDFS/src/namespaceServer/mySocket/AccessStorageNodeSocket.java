/**
 * 心跳监测套接字
 */
package namespaceServer.mySocket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class AccessStorageNodeSocket {
	public void access(){
		Socket socket = null;  
		BufferedReader br = null;  
		PrintWriter pw = null;  
		try {  
			//客户端socket指定服务器的地址和端口号,这里要获取需要通信的端口  
			socket = new Socket("127.0.0.1", 6001);  
			System.out.println("Socket=" + socket);  
			//同服务器原理一样  
			br = new BufferedReader(new InputStreamReader(  
					socket.getInputStream()));  
			pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(  
					socket.getOutputStream())));  
			//这里调用方法进行socket交互
			for (int i = 0; i < 10; i++) {  
				pw.println("howdy " + i);  
				pw.flush();  
				String str = br.readLine();  
				System.out.println(str);  
			}  
			pw.println("END");  
			pw.flush();  
		} catch (Exception e) {  
			e.printStackTrace();  
		} finally {  
			try {  
				System.out.println("close......");  
				br.close();  
				pw.close();  
				socket.close();  
			} catch (IOException e) {  
				// TODO Auto-generated catch block  
				e.printStackTrace();  
			}  
		}
	}
}
