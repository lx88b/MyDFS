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
		//为每个有删除信息需求的节点创建一个线程来处理删除工作
		for(int port:ports) {
			ArrayList<UUID> blocks = portsAndBlocks.get(port);
			new DeleteStorageRequest(port,blocks);
		}
	}
}
