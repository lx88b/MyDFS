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
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;



class FS{
		public String rootDir;
		
		FS(String _dir){
			rootDir = _dir;
		}
		
		public String getFile(String _file){
			if(! existFile(_file))
			{
				return _file + " doesn't exist\n";
			}
			else
			{
				return fileCont(_file);
			}
		}
		
		public String deleteFile(String _file){
			if(! existFile(_file))
			{
				return _file + " doesn't exist\n";
			}
			else
			{
				delFile(_file);
				return _file + " deleted successfully\n";
			}
		}
		
		public String existFileOrNot(String _file){
			if(! existFile(_file))
			{
				return _file + " doesn't exist\n";
			}
			else
			{
				return _file + " exists\n";
			}
		}
		
		public String sizeofFile(String _file){
			if(! existFile(_file))
			{
				return _file + " doesn't exist\n";
			}
			else
			{
				return _file + " has size : " + fileSZ(_file);
			}
		}
		
		public String addData2File(String _file, String _data){
			if(! existFile(_file))
			{
				return _file + " doesn't exist\n";
			}
			else
			{
				appendFile(_file, _data);
				return "added successfully\n";
			}
		}
		
		public void appendFile(String _file, String _data){
	        try {
	            // 打开一个随机访问文件流，按读写方式
	            RandomAccessFile randomFile = new RandomAccessFile(rootDir + "/" + _file, "rw");
	            // 文件长度，字节数
	            long fileLength = randomFile.length();
	            //将写文件指针移到文件尾。
	            randomFile.seek(fileLength);
	            randomFile.writeBytes(_data);
	            randomFile.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
		
		private void delFile(String _file){
			File _f = new File(rootDir + "/" +  _file);
			_f.delete();
		}
		
		private String fileCont(String _file){
			StringBuffer _sb = null;
			try {
			BufferedReader _br = new BufferedReader(new FileReader(_file));
			_sb = new StringBuffer("");
			String _line = null;
				while((_line = _br.readLine()) != null){
					_sb.append(_line+"\n");
				}
				_br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return _sb.toString();
		}
		
		private long fileSZ(String _file){
			File _f = new File(rootDir + "/" + _file);
			return _f.length();
		}
		
		public boolean existFile(String _file){
			File _f = new File(rootDir + "/" + _file);
			return _f.isFile();
		}
		
		/*
		 * Dir Operations: list delete create
		 */
		public String listDir(String _path){
			if(! existDir(_path))
			{
				return _path + " doesn't exist\n";
			}
			else
			{
				return lsDir(_path);
			}
		}
		
		public String deleteDir(String _path){
			if(! existDir(_path))
			{
				return _path + " doesn't exist\n";
			}
			else
			{
				delDir(_path);
				return _path + " deleted successfully\n";
			}
		}
		
		public String createDir(String _path){
			if(existDir(_path))
			{
				return _path + " already existed\n";
			}
			else
			{
				mkDir(_path);
				return _path + " created successfully\n";
			}
		}
		
		public boolean existDir(String _path){
			File _f = new File(rootDir + "/" + _path);
			return _f.isDirectory();
		}
		
		public String lsDir(String _path){
			StringBuffer _sb = new StringBuffer();
			File _f = new File(rootDir + "/" + _path);
			File[] _files = _f.listFiles();
			_sb.append("There are ")
			   .append(_files.length)
			   .append(" files\n");
			for(File iFile : _files){
				_sb.append("\t");
				if(iFile.isFile())
				{
					_sb.append("file : ").append(iFile.getName()).append("\n");
				}
				else
				{
					_sb.append("folder : ").append(iFile.getName()).append("\n");
				}
			}
			return _sb.toString();
		}
		
		public void delDir(String _path){
			File _f = new File(rootDir + "/" + _path);
			_f.delete();
		}
		
		public void mkDir(String _path){
			File _f = new File(rootDir + "/" + _path);
			_f.mkdir();
		}
}

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
	/*
	 * private methods
	 */
	private String getBlockFile(String _block){
		return rootDir+"/b_"+_block;
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
	private OPType op_type;
	private static int sleep_time = 60*1000;
	/* all relative data is saved in this variable */
	private String data;
	public storageThread(String _t_ip, int _t_port, OPType _op_type, String _data) {
		// TODO Auto-generated constructor stub
		target_IP = _t_ip;
		target_port = _t_port;
		op_type = _op_type;
		setData(_data);
	}
	
	@Override
	public void run(){
		switch (op_type) {
		case sendBlock:
		{
			
			break;
		}
		case recvBlock:
		{
			
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
	
	public void heartBeat(){
		while(true){
			Socket _socket = null;
			try {
				
				_socket = new Socket(target_IP, target_port);
				
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			this.send(_socket, "HEARTBEAT");
			
			try {
				
				Thread.sleep(sleep_time);
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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

public class storage extends Thread{
	

	public  String mIP;
	public  int mPort;
	public  blockManager blockManager;
	public  storageThread heartBeat;
	public  String serverIP;
	public  int serverPort = 6001;
	public storage(String _ip, int _port, String _server_ip, int _server_port, String _root_dir){
		mIP = _ip;
		mPort = _port;
		serverIP = _server_ip;
		serverPort = _server_port;
		blockManager = new blockManager(_root_dir);
	}
	
	@Override
	public  void run(){
		{
			heartBeat = new storageThread(serverIP, serverPort, OPType.heartBeat, "");
			heartBeat.start();
		}
		{
			
		}
	}

}
