import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.omg.PortableServer.POA;


public class test_client2storage {
	public static int curPort = 7002;
	public static void main(String[] args){
		{
			System.out.print("test begin\n");
		}
		ProcessBuilder _pb_client = new ProcessBuilder("java", "Client.client", "7000", "fclient");
		ProcessBuilder _pb_storage =new ProcessBuilder("java", "Storage.storage", ""+curPort, "s"+curPort);
		try {
			Process _client = _pb_client.start();
			{
				BufferedReader _c_br = 
						new BufferedReader(
								new InputStreamReader(
										new BufferedInputStream(_client.getInputStream())));
				printBR(_c_br);
			}
			Process[] _storage = new Process[5];
			for(int i = 0; i < 5; i ++){
				int _port = getNewPort();
				_storage[i] = getProcess("Storage.storage", _port);
				{
					BufferedReader _s_br = 
							new BufferedReader(
									new InputStreamReader(
											new BufferedInputStream(_storage[i].getInputStream())));
					printBR(_s_br);
				}
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
	
	public static void printBR(BufferedReader _br) throws IOException{
		String _line = null;
		while((_line = _br.readLine()) != null){
			System.out.print(_line+"\n");
		}
	}
	
	public static Process getProcess(String _program, int _port) throws IOException{
		ProcessBuilder _pb_storage = new ProcessBuilder("java", "Storage.storage", ""+_port, "s"+_port);
		return _pb_storage.start();
	}
}
