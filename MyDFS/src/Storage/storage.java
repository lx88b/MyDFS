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
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    String _block = data;
	    String _msg = storage.blockManager.getBlockData(_block);
	    send(_socket, _msg);
	}
	
	public void recvBlock(){
		Socket _socket = null;
	    try {
			_socket = new Socket(target_IP, target_port);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    String _block = data;
	    String _msg = recv(_socket);
	    storage.blockManager.addBlock(_block, _msg);
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
			_pw.close();
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
			_br.close();
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
	
	/* java storage port rootdir */
	public  static void main(String[] args){
		System.out.println("storage start:"+args[0]);
		{
			mIP = "127.0.0.1";
			mPort = Integer.parseInt(args[0]);
			{
				File _f = new File(args[1]);
				if(! _f.exists()){
					_f.mkdir();
				}
			}
			blockManager = new blockManager(args[1]);
		}
		{
			heartBeat = new storageThread(serverIP, serverPort, OPType.heartBeat, "", mPort+1);
			heartBeat.start();
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
				if(_line.equals("Del")){
					_line = _br.readLine();
					while(! _line.equals("END")){
						storage.blockManager.delBlock(_line);
						_line = _br.readLine();						
					}
					_br.close();
					_pw.close();
					_socket.close();
				}
				else if(_line.equals("SEND")){
					int _port = Integer.parseInt(_br.readLine());
					String block = _br.readLine();
					_br.close();
					_pw.close();
					_socket.close();
					storageThread _st = new storageThread("", _port, OPType.sendBlock, block, -1);
					_st.start();
					_st.join();
				}
				else if(_line.equals("RECV")){
					int _port = Integer.parseInt(_br.readLine());
					String block = _br.readLine();
					_br.close();
					_pw.close();
					_socket.close();
					storageThread _st = new storageThread("", _port, OPType.recvBlock, block, -1);
					_st.start();
					_st.join();
				}
				else if(_line.equals("Add")){
					String block = _br.readLine();
					String _data = "";
					{
						String tmp_line=_br.readLine();
						while(! tmp_line.equals("END")){
							_data += tmp_line + "\n";
							tmp_line = _br.readLine();
						}
						blockManager.addBlock(block, _data);
					}
					_br.close();
					_pw.close();
					_socket.close();
				}
				
			}
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

}
