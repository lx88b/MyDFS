import java.io.IOException;

import org.omg.PortableServer.POA;


public class test_client2storage {
	public static int curPort = 7002;
	public static void main(String[] args){
		ProcessBuilder _pb_client = new ProcessBuilder("java Client.client 7000 fclient");
		ProcessBuilder _pb_storage = new ProcessBuilder("java Storage.storage "+curPort+ " s"+curPort);
		try {
			Process _client = _pb_client.start();
			Process[] _storage = new Process[5];
			for(int i = 0; i < 5; i ++){
				int _port = getNewPort();
				_storage[i] = getProcess("Storage.storage", _port);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static int getNewPort(){
		int _port = curPort;
		curPort += 2;
		return _port;
	}
	
	public static Process getProcess(String _program, int _port) throws IOException{
		ProcessBuilder _pb_storage = new ProcessBuilder("java "+_program+" "+_port+ " s"+_port);
		return _pb_storage.start();
	}
}
