package Storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Listen_Thread extends Thread{
	
	@Override
	public void run(){
		listen();
	}
	
	public void listen(){
		ServerSocket _server = null;
		Socket _socket = null;
		BufferedReader _br = null;  
	    PrintWriter _pw = null;
	    try 
	    {
			_server = new ServerSocket(storage.mPort);
			while(true){
				_socket = _server.accept();
				_br = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
				_pw = new PrintWriter
						(new BufferedWriter
								(new OutputStreamWriter
										(_socket.getOutputStream())),true);  
				String _line = _br.readLine();
				{
					storage.log("new request :" + _line);
				}
				if(_line.equals("Del")){
					_line = _br.readLine();
					{
						System.out.println("Del block: "+ _line);
					}
					while(! _line.equals("END")){
						storage.blockManager.delBlock(_line);
						{
							storage.log("Delete block"+_line);
						}
						_line = _br.readLine();		
						if(_line==null){
							break;
						}
					}
					_br.close();
					_pw.close();
					_socket.close();
				}
				else if(_line.equals("SEND")){
					int _port = Integer.parseInt(_br.readLine());
					String block = _br.readLine();
					{
						System.out.println("SEND BLOCK["+block+"] to "+ _port);
					}
					_br.close();
					_pw.close();
					_socket.close();
					storageThread _st = new storageThread("", _port, OPType.sendBlock, block, -1);
					_st.start();
					_st.join();
					{
						storage.log("finish send");
					}
				}
				else if(_line.equals("BLOCK")){
					String block = _br.readLine();
					{
						System.out.println("RECV block: ["+block+"]");
					}
					String _data = "";
					{
						String tmp_line=_br.readLine();
						storage.log("line:"+tmp_line);
						while(! tmp_line.equals("END")){
							_data += tmp_line + "\n";
							tmp_line = _br.readLine();
							storage.log("line"+tmp_line);
						}
						storage.blockManager.addBlock(block, _data);
						_pw.println("recv successfully");
						_pw.println("END");
						_pw.flush();
					}
					_br.close();
					_pw.close();
					_socket.close();
					{
						storage.log("recv block["+block+"]");
					}
					{
						storage.log("finish recv");
					}
				}
				else if(_line.equals("Append")){
					String block = _br.readLine();
					String _data = "";
					{
						String tmp_line=_br.readLine();
						while(! tmp_line.equals("END")){
							_data += tmp_line + "\n";
							tmp_line = _br.readLine();
							storage.log(tmp_line);
						}
						storage.blockManager.addBlock(block, _data);
						_pw.println("Append successfully");
						_pw.println("END");
						_pw.flush();
					}
					{
						System.out.println("append "+block+" "+_data);
					}
				}
				else if (_line.equals("Get")) {
					String block = _br.readLine();
					String _data = storage.blockManager.getBlockData(block);
					_pw.println(_data);
					_pw.println("END");
					_pw.flush();
					}
					_br.close();
					_pw.close();
					_socket.close();
				}
				
		} catch (Exception e) {
			// TODO: handle exception
			storage.log("Exception");
		}
	}
	
}
