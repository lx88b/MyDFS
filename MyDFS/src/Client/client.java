package Client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
enum clientOPType{
	ADD,
	APPEND,
	GET,
	DEL_FILE,
	EXIST_FILE,
	SIZEOF,
	CREATE,
	DEL_FOLDER,
	ADDNODE,
	LIST
}

class Address{
	public String IP;
	public int Port;
	public Address(String _ip, int _port){
		IP = _ip;
		Port = _port;
	}
}

class clientOperation extends Thread{
	private clientOPType OPtype;
	private String target_IP;
	private int target_port;
	private String target_file;
	private String Data;
	private String clientDir;
	public clientOperation(
			String _ip, int _port,
			clientOPType _op,
			String _file,
			String _data,
			String _dir
							)
	{
		OPtype = _op;
		target_IP = _ip;
		target_port = _port;
		target_file = _file; 
		Data = _data;
		clientDir = _dir;
		
	}
	@Override
	public void run(){
		{
			client.log(OPtype + "--type\n");
			client.log(target_port + "--port\n");
			client.log(Data + "--data\n");
			client.log(target_file+"--file\n");
		}
		switch (OPtype) {
		case ADD:
		{
			this.add();
			break;
		}
		case APPEND:
		{
			this.Append();
			break;
		}
		case GET:
		{
			this.get();
			break;
		}
		case DEL_FILE:
		{
			this.del();
			break;
		}
		case EXIST_FILE:
		{
			this.exist();
			break;
		}
		case SIZEOF:
		{
			this.Size();
			break;
		}
		case CREATE:
		{
			this.mkDir();
			break;
		}
		case DEL_FOLDER:
		{
			this.delDir();
			break;
		}
		case ADDNODE:
		{
			this.AddNode();
			break;
		}
		case LIST:
		{
			this.List();
			break;
		}
		default:
		{
			break;
		}
		}
	}
	
	public void get(){
		String _path = this.getPathOfFile(target_file);
		/*
		 * What if _path is null ???
		 */
		String _name = this.getFileName(target_file);
		String _msg_send = "Get\n"+_path+"\n"+_name;
		String _msg_recv = null;
		Socket _socket2server = null;
		Socket _socket2storage = null;
		try {
			_socket2server = new Socket(target_IP, target_port);
			this.send(_socket2server, _msg_send);
			_msg_recv = this.recv(_socket2server);
			_socket2server.close();
			if(_msg_recv.indexOf("false") !=-1){
				System.out.print("failed in get file:"+target_file+"\n");
				return;
			}
			
			String[] _sp = _msg_recv.split("\n");
			StringBuffer _sb = new StringBuffer();			
			for(String iStr: _sp)
			{
				String[] tmp_sp = iStr.split(":");
				String _block = tmp_sp[0];
				int _port = Integer.parseInt(tmp_sp[1]);
				_socket2storage = new Socket("127.0.0.1", _port);
				_msg_send = "Get\n" + _block;
				this.send(_socket2storage, _msg_send);
				_msg_recv = this.recv(_socket2storage);
				_sb.append(this.getBlockData(_msg_recv));
				_socket2storage.close();
			}
			this.Data = _sb.toString();
			{
				String _file = clientDir + "/" + _name;
				BufferedWriter _bw = new BufferedWriter(new FileWriter(_file));
				_bw.write(this.Data);
				_bw.close();
				System.out.print("Get "+_name+" successfully!\n");
			}
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void add(){
		String _path = this.getPathOfFile(target_file);
		/*
		 * What if _path is null ???
		 */
		String _name = this.getFileName(target_file);
		String _msg_send = "Add\n"+_path+"\n"+_name+"\n"+ 0;
		String _msg_recv = null;
		Socket _socket2server = null;
		Socket _socket2storage = null;
		try {
			_socket2server = new Socket(target_IP, target_port);
			this.send(_socket2server, _msg_send);
			_msg_recv = this.recv(_socket2server);
			_socket2server.close();
			if(_msg_recv.indexOf("false") !=-1){
				System.out.print("failed in add file:"+target_file+"\n");
				return;
			}
			
			System.out.print("Add " + target_file + " successfully!\n");;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void del(){
		String _path = this.getPathOfFile(target_file);
		/*
		 * What if _path is null ???
		 */
		String _name = this.getFileName(target_file);
		String _msg_send = "Del\n"+_path+"\n"+_name;
		String _msg_recv = null;
		Socket _socket2server = null;
		
		try {
			_socket2server = new Socket(target_IP, target_port);
			this.send(_socket2server, _msg_send);
			_msg_recv = this.recv(_socket2server);
			_socket2server.close();
			if(_msg_recv.indexOf("false") !=-1){
				System.out.print("failed in delete file:"+target_file+"\n");
				return;
			}
			
			System.out.print("Del " + target_file + " successfully!\n");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void exist(){
		String _path = this.getPathOfFile(target_file);
		/*
		 * What if _path is null ???
		 */
		String _name = this.getFileName(target_file);
		String _msg_send = "Exist\n"+_path+"\n"+_name;
		String _msg_recv = null;
		Socket _socket2server = null;
		try {
			_socket2server = new Socket(target_IP, target_port);
			this.send(_socket2server, _msg_send);
			_msg_recv = this.recv(_socket2server);
			_socket2server.close();
			System.out.print(_msg_recv+"\n");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void Size(){
		String _path = this.getPathOfFile(target_file);
		/*
		 * What if _path is null ???
		 */
		String _name = this.getFileName(target_file);
		String _msg_send = "Size\n"+_path+"\n"+_name;
		String _msg_recv = null;
		Socket _socket2server = null;
		try {
			_socket2server = new Socket(target_IP, target_port);
			this.send(_socket2server, _msg_send);
			_msg_recv = this.recv(_socket2server);
			_socket2server.close();
			System.out.print("Size of "+ target_file + " is "+_msg_recv+"\n");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void delDir(){
		String _path = this.getPathOfFile(target_file);
		/*
		 * What if _path is null ???
		 */
		String _name = this.getFileName(target_file);
		String _msg_send = "Deldir\n"+_path+"\n"+_name;
		String _msg_recv = null;
		Socket _socket2server = null;
		try {
			_socket2server = new Socket(target_IP, target_port);
			this.send(_socket2server, _msg_send);
			_msg_recv = this.recv(_socket2server);
			_socket2server.close();
			if(_msg_recv.indexOf("false") !=-1){
				System.out.print("failed in delete dir:"+target_file+"\n");
				return;
			}
			
			System.out.print("delete " + target_file + " successfully!\n");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void mkDir(){
		String _path = this.getPathOfFile(target_file);
		/*
		 * What if _path is null ???
		 */
		String _name = this.getFileName(target_file);
		String _msg_send = "Mkdir\n"+_path+"\n"+_name;
		String _msg_recv = null;
		Socket _socket2server = null;
		try {
			_socket2server = new Socket(target_IP, target_port);
			this.send(_socket2server, _msg_send);
			_msg_recv = this.recv(_socket2server);
			_socket2server.close();
			if(_msg_recv.indexOf("false") !=-1){
				{
					client.log("recv:"+_msg_recv+"\n");
				}
				System.out.print("failed in make dir:"+target_file+"\n");
				return;
			}
			
			System.out.println("Make dir "+target_file+" successfully");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void List(){
		String _path = this.getPathOfFile(target_file);
		if(target_file.equals("."))
			_path = "root/";
		/*
		 * What if _path is null ???
		 */
		String _name = this.getFileName(target_file);
		if(target_file.equals("."))
			_name = this.getFileName("root");
		String _msg_send = "List\n"+_path+"\n"+_name;
		String _msg_recv = null;
		Socket _socket2server = null;
		try {
			_socket2server = new Socket(target_IP, target_port);
			this.send(_socket2server, _msg_send);
			_msg_recv = this.recv(_socket2server);
			_socket2server.close();
			if(_msg_recv.indexOf("false") !=-1){
				System.out.print("failed in list dir:"+target_file+"\n");
				return;
			}
			
			System.out.print(_msg_recv+"\n");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void AddNode(){
		/*
		 * What if _path is null ???
		 */
		String _name = this.getFileName(target_file);
		String _msg_send = "AddNode\n"+Data;//Data shoule be port
		String _msg_recv = null;
		Socket _socket2server = null;
		try {
			_socket2server = new Socket(target_IP, target_port);
			this.send(_socket2server, _msg_send);
			_msg_recv = this.recv(_socket2server);
			_socket2server.close();
			if(_msg_recv.indexOf("false") !=-1){
				System.out.print("failed in list dir:"+target_file+"\n");
				return;
			}
			
			System.out.print(_msg_recv+"\n");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void Append(){
		String _path = this.getPathOfFile(target_file);
		/*
		 * What if _path is null ???
		 */
		String _name = this.getFileName(target_file);
		String _msg_send = "Append\n"+_path+"\n"+_name+"\n"+Data.length();
		String _msg_recv = null;
		Socket _socket2server = null;
		Socket _socket2storage = null;
		try {
			_socket2server = new Socket(target_IP, target_port);
			this.send(_socket2server, _msg_send);
			_msg_recv = this.recv(_socket2server);
			_socket2server.close();
			if(_msg_recv.indexOf("false") !=-1){
				System.out.print("failed in get file:"+target_file+"\n");
				return;
			}
			{
				client.log("append:recv from server: "+ _msg_recv);
			}
			String[] _sp = _msg_recv.split("\n");
			for(String iStr: _sp)
			{
				String[] tmp_sp = iStr.split(":");
				String _block = tmp_sp[0];
				for(int i = 1; i < tmp_sp.length; i ++)
				{
					int _port = Integer.parseInt(tmp_sp[i]);
					_socket2storage = new Socket("127.0.0.1", _port);
					_msg_send = "Append\n" + _block + "\n" + Data;
					this.send(_socket2storage, _msg_send);
					_msg_recv = this.recv(_socket2storage);
					_socket2storage.close();
				}
			}
			{
				System.out.println("Append " + target_file + " successfully!");
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO: handle exception
			System.out.println("Append "+_name+" failed!\n");
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Append "+_name+" failed!\n");
		} 
	}
	
	private String getBlockData(String _recv){
		String _ret = null;
		_ret = _recv;
		return _ret;
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
//			_pw.close();
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
				{
					client.log("recv:["+_line+"]");
				}
				if(_line.equals("END")){
					break;
				}
				else
				if(_line.equals("true")){
					continue;
				}
				else
				if(_ret.equals("")){
					_ret = _line;
				}
				else
				{
					_ret += "\n"+_line;
				}
			}
//			_br.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return _ret;
	}
	
	private String getFileName(String _target_file){
		File _f = new File(_target_file);
		{
			client.log(_target_file + ": has name:"+_f.getName()+"\n");
		}
		return _f.getName();
	}
	
	private String getPathOfFile(String _target_file){
		File _f = new File(_target_file);
		if(_f.getParent()==null){
			return "root/";
		}
		{
			client.log(_target_file + ": has path:"+_f.getParent()+"\n");
		}
		return "root/"+_f.getParent()+"/";
	}
	
}
/*
 * Operation input: 
 * get/add/exist/delete/sizeof -f file_url
 * append -f file_url data
 * create/delete/list -d path_url
 * 
 */
public class client {
	public  String serverIP = "127.0.0.1";
	public  int serverPort = 6000;
	public static String clientDir;
	public final static boolean debug_mode = false;
	public static void log(String _str){
		if(debug_mode)
		{
			System.out.println("log: "+_str);
		}
	}
	
	 public static void main(String[] args){
		 System.out.print("client start\n");
		 {
			 clientDir = "clientRoot";
			 {
				 File _f = new File(clientDir);
				 if(! _f.exists()){
					 _f.mkdir();
				 }
			 }
		 }
		 Scanner _sc = new Scanner(System.in);
		 HashSet<String> opSet = new HashSet<String>();
		 {
			 opSet.add("get");
			 opSet.add("add");
			 opSet.add("exist");
			 opSet.add("delete");
			 opSet.add("sizeof");
			 opSet.add("append");
			 opSet.add("create");
			 opSet.add("list");
			 opSet.add("addnode");
		 }
		 while(true){
			 System.out.print("\n\n******Please input the request in the form as: \n");
			 System.out.print("******get/add/exist/delete/sizeof -f file_url\n");
			 System.out.print("******append -f file_url data\n");
			 System.out.print("******create/delete/list -d path_url\n");
			 String _line = _sc.nextLine();
			 clientOPType _op = null;
			 int _space1 = 0, _space2 = 0;
			 {//parse the optype
				 _space1 = _line.indexOf(" ");
				 {
					 if(_space1 == -1) {
						 System.out.print("error 01:\n");
						 continue;
					 }
				 }
				 String _sop = _line.substring(0, _space1);
				 {
					 if(! opSet.contains(_sop)){
						 System.out.print("error 02\n");
						 continue;
					 }
				 }
				 _space2 = _line.indexOf(" ", _space1+1);
				 {
					 if(_space2 == -1) {
						 System.out.print("error 03\n");
						 continue;
					 }
				 }
				 String _param = _line.substring(_space1+1,_space2);
				 {
					 if(
							 !_param.equals("-f") && 
							 !_param.equals("-d") &&
							 !_param.equals("-p")
						)
					 {
						 System.out.print("error 04\n");
						 continue;
					 }
				 }
				 _op = getOpType(_sop, _param);
			 }//end parse optype
			 String _data = null;
			 String url_or_port = null;
			 if(_op.equals(clientOPType.APPEND)){
				 int _space3 = _line.indexOf(" ", _space2+1);
				 if(_space3 == -1){
					 System.out.print("error 05\n");
					 continue;
				 }
				 url_or_port = _line.substring(_space2+1, _space3);
				 _data = _line.substring(_space3+1);
			 }
			 else {
				 url_or_port = _line.substring(_space2+1);
			}
			 {
				 if(_op.equals(clientOPType.ADDNODE)){
					 _data = url_or_port;
				 }
			 }
			clientOperation _cop = new clientOperation
					("127.0.0.1", 6000, _op, url_or_port, _data, clientDir);
			_cop.start();
			try {
				_cop.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		 }
	 }
	 
	 public static clientOPType getOpType(String _op, String _param){
		 if(_op.equals("get")){
			 return clientOPType.GET;
		 }
		 else
		 if(_op.equals("add")){
			 return clientOPType.ADD;
		 }
		 else
		 if(_op.equals("sizeof")){
			 return clientOPType.SIZEOF;
		 }
		 else
		 if(_op.equals("delete")){
			 if(_param.equals("-f"))
			 {
				 return clientOPType.DEL_FILE;
			 }
			 else
			 {
				 return clientOPType.DEL_FOLDER;
			 }
		 }
		 else
		 if(_op.equals("exist")){
			return clientOPType.EXIST_FILE;
		 }
		 else
		 if(_op.equals("append")){
			 return clientOPType.APPEND;
		 }
		 else
		 if(_op.equals("create")){
			 return clientOPType.CREATE;
		 }
		 else
		 if(_op.equals("list")){
			 return clientOPType.LIST;
		 }
		 return null;
	 }
	 
	 public static void testStorage(){
		 
	 }
}
