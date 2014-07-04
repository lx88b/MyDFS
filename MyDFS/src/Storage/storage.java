package Storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;



class blockManager{
	private String rootDir;
	private HashMap<String, Integer> block2size;
	public blockManager(String _root_dir){
		rootDir = _root_dir;
		block2size = new HashMap<String, Integer>();
	}
	/*
	 * public methods
	 */
	public String getBlockData(String _block){
		return loadBlock(_block);
	}
	
	public void addBlock(String _block, String _data){
		block2size.put(_block, _data.length());
		storeBlock(_block, _data);
	}
	public void delBlock(String _block){
		block2size.remove(_block);
		cleanBlock(_block);
	}
	/*
	 * private methods
	 */
	private String getBlockFile(String _block){
		return rootDir+"/b_"+_block;
	}
	private void cleanBlock(String _block){
		String _file = getBlockFile(_block);
		File _f = new File(_file);
		_f.delete();
	}
	private void storeBlock(String _block, String _data){
		String _file = getBlockFile(_block);
		BufferedWriter _bw = null;
		try {
			 _bw = new BufferedWriter(new FileWriter(_file));
			 _bw.write(_data);
			 _bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private String loadBlock(String _block){
		String _file = getBlockFile(_block);
		BufferedReader _br = null;
		StringBuilder _sb = new StringBuilder();
		try {
			_br = new BufferedReader(new FileReader(_file));
			String _line = null;
			while((_line=_br.readLine()) != null){
				_sb.append(_line).append("\n");
			}
			_br.close();
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return _sb.toString();
	}
}

enum OPType{
	sendBlock,
	recvBlock,
	heartBeat
}

class storageThread extends Thread{
	private String target_IP;
	private int target_port;
	private int listen_port;
	private OPType op_type;
	private static int sleep_time = 60*1000;
	/* all relative data is saved in this variable */
	private String data;
	public storageThread(
			String _t_ip, 
			int _t_port, 
			OPType _op_type, 
			String _data,
			int _listen_port
			) {
		// TODO Auto-generated constructor stub
		target_IP = "127.0.0.1";
		target_port = _t_port;
		op_type = _op_type;
		setData(_data);
		listen_port = _listen_port;
	}
	
	@Override
	public void run(){
		switch (op_type) {
		case sendBlock:
		{
			sendBlock();
			break;
		}
		case recvBlock:
		{
			recvBlock();
			break;
		}
		case heartBeat:
		{
			heartBeat();
			break;
		}
		default:
		{
			
			break;
		}
		}
	}
	
	public void sendBlock(){
		Socket _socket = null;
	    try {
			_socket = new Socket(target_IP, target_port);
			String _block = data;
			String _msg = storage.blockManager.getBlockData(_block);
			_msg="BLOCK\n"+_block+"\n"+_msg;
			{
				storage.log("sendmsg:"+_msg);
			}
			send(_socket, _msg);
			_socket.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void recvBlock(){
		Socket _socket = null;
	    try {
			_socket = new Socket(target_IP, target_port);
			String _block = data;
			String _msg = recv(_socket);
			{
				storage.log("recv block:"+_block+" with "+_msg);
			}
			storage.blockManager.addBlock(_block, _msg);
			_socket.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void heartBeat(){
		ServerSocket _server = null;
		Socket _socket = null;
		BufferedReader _br = null;  
	    PrintWriter _pw = null;
		try {			
			_server = new ServerSocket(listen_port);
			while(true){
				_socket = _server.accept();
				_br = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
				_pw = new PrintWriter
						(new BufferedWriter
								(new OutputStreamWriter
										(_socket.getOutputStream())),true);  
				String _line = _br.readLine();

				if(_line.equals("HEARTBEAT")){
					_pw.print("hello");
					_pw.flush();
				}
				_br.close();
				_pw.close();
				_socket.close();
			}
			
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
	/*
	 * private methods:
	 */
	
	private void send(Socket _socket, String _msg){
		PrintWriter _pw = null;
		Scanner _scan = null;
		try {
			_scan = new Scanner(_msg);
			//同服务器原理一样  
			_pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(  
					_socket.getOutputStream())));  
			while(_scan.hasNextLine()){
				String _line = _scan.nextLine();
				_pw.println(_line);
				_pw.flush();
			}
			_pw.println("END");
			_pw.flush();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	private String recv(Socket _socket){
		BufferedReader _br = null;
		String _ret = "";
		try {
			//同服务器原理一样  
			_br = new BufferedReader(new InputStreamReader(  
					_socket.getInputStream()));  
			String _line = null;
			while(true){
				_line = _br.readLine();
				if(_line.equals("END")){
					break;
				}
				if(_ret.equals("")){
					_ret = _line;
				}
				else
				{
					_ret += "\n"+_line;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return _ret;
	}
}

public class storage{
	

	public static  String mIP;
	public static  int mPort;
	public static  blockManager blockManager;
	public static  storageThread heartBeat;
	public static  String serverIP = "127.0.0.1";
	public static  int serverPort = 6001;
	public final static boolean debug_mode = false;
	
	/* java storage port rootdir */
	public  static void main(String[] args){
		System.out.println("storage start:"+args[0]);
		{
			mIP = "127.0.0.1";
			mPort = Integer.parseInt(args[0]);
			{
				File _f = new File("s"+mPort);
				if(! _f.exists()){
					_f.mkdir();
				}
			}
			blockManager = new blockManager("s"+mPort);
		}
		{
			Listen_Thread _lt = new Listen_Thread();
			_lt.start();
			heartBeat = new storageThread(serverIP, serverPort, OPType.heartBeat, "", mPort+1);
			heartBeat.start();
		}
		nodeRegister();
	}
	
	public static void nodeRegister(){
		Socket _socket = null;
		BufferedReader _br = null;  
	    PrintWriter _pw = null;
	    
		try {
			_socket = new Socket(serverIP, serverPort);
			_br = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
			_pw = new PrintWriter
					(new BufferedWriter
							(new OutputStreamWriter
									(_socket.getOutputStream())),true);  
			_pw.println("AddNode\n"+mPort);
			_pw.flush();
			String _line = _br.readLine();
			System.out.print(_line+"\n");
			
			_pw.close();
			_br.close();
			_socket.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			storage.log("register IOException:"+mPort);
			e.printStackTrace();
		}
		
		
	}
	
	public static void log(String _str){
		if(storage.debug_mode){
			System.out.println("log: " + _str);
		}
	}
	
	public static void listen(){
		ServerSocket _server = null;
		Socket _socket = null;
		BufferedReader _br = null;  
	    PrintWriter _pw = null;
	    try 
	    {
			_server = new ServerSocket(mPort);
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
						blockManager.addBlock(block, _data);
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
						blockManager.addBlock(block, _data);
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
					String _data = blockManager.getBlockData(block);
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
