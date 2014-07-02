/**
 * 向存储节点发送删除数据的请求
 */
package namespaceServer.mySocket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;

public class DeleteStorageRequest extends Thread {
	private int port;
	private ArrayList<UUID> blocks;
	public DeleteStorageRequest(int desPort, ArrayList<UUID> desBlocks) {
		port = desPort;
		blocks = desBlocks;
		start();
	}
	public void run(){
		Socket socket = null;  
		BufferedReader br = null;  
		PrintWriter pw = null;  
		try {  
			//客户端socket指定服务器的地址和端口号,这里要获取需要通信的端口  
			socket = new Socket("127.0.0.1", port);  
			//同服务器原理一样  
			br = new BufferedReader(new InputStreamReader(  
					socket.getInputStream()));  
			pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(  
					socket.getOutputStream())));  
			//这里调用方法进行socket交互
			int size = blocks.size();
			pw.println("Del");
			for (int i = 0; i < size; i++) {
				pw.println(blocks.get(i).toString());  
			}  
			pw.println("END");  
			pw.flush();  
		} catch (Exception e) {  
			e.printStackTrace();  
		} finally {  
			try {  
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
