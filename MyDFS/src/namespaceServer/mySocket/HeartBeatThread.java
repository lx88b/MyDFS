package namespaceServer.mySocket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import namespaceServer.model.NamespaceServer;
import namespaceServer.model.StorageNode;

public class HeartBeatThread extends Thread{
	public static long sleep_time = 10*1000;
	
	@Override
	public void run(){
	    NamespaceServer _ns = NamespaceServer.getNamespaceServer();
		while(true){
			_ns.heartBeat();
			try {
				HeartBeatThread.sleep(sleep_time);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
