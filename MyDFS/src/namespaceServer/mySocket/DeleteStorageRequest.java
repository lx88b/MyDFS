/**
 * ��洢�ڵ㷢��ɾ�����ݵ�����
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
			//�ͻ���socketָ���������ĵ�ַ�Ͷ˿ں�,����Ҫ��ȡ��Ҫͨ�ŵĶ˿�  
			socket = new Socket("127.0.0.1", port);  
			//ͬ������ԭ��һ��  
			br = new BufferedReader(new InputStreamReader(  
					socket.getInputStream()));  
			pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(  
					socket.getOutputStream())));  
			//������÷�������socket����
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
