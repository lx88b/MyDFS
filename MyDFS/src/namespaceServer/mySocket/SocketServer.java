package namespaceServer.mySocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class SocketServer {
	private static SocketServer socketServer=null;
	public static SocketServer getSocketServer() {
		if(socketServer==null) {
			socketServer = new SocketServer();
		}
		return socketServer;
	}
	
	public void deleteRequest(HashMap<Integer,ArrayList<UUID>> portsAndBlocks) {
		ArrayList<Integer> ports = new ArrayList<Integer>(portsAndBlocks.keySet());
		//Ϊÿ����ɾ����Ϣ����Ľڵ㴴��һ���߳�������ɾ������
		for(int port:ports) {
			ArrayList<UUID> blocks = portsAndBlocks.get(port);
			new DeleteStorageRequest(port,blocks);
		}
	}
}
