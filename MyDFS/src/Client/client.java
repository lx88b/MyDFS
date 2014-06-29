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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
enum clientOPType{
	ADD,
	GET,
	DEL_FILE,
	EXIST_FILE,
	SIZEOF,
	CREATE,
	DEL_FOLDER,
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
	private String Response;
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
		System.out.print(OPtype + "\n");
		System.out.print(target_IP + "\n");
		System.out.print(OPtype + "\n");
		System.out.print(Data + "\n");
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
		String _msg_send = "Add\n"+_path+"\n"+_name+"\n"+Data.length();
		String _msg_recv = null;
		Socket _socket2server = null;
		Socket _socket2storage = null;
		try {
			_socket2server = new Socket(target_IP, target_port);
			this.send(_socket2server, _msg_send);
			_msg_recv = this.recv(_socket2server);
			_socket2server.close();
			
			String[] _sp = _msg_recv.split("\n");
			for(String iStr: _sp)
			{
				String[] tmp_sp = iStr.split(":");
				String _block = tmp_sp[0];
				for(int i = 1; i < tmp_sp.length; i ++)
				{
					int _port = Integer.parseInt(tmp_sp[i]);
					_socket2storage = new Socket("127.0.0.1", _port);
					_msg_send = "Add\n" + _block + "\n" + Data;
					this.send(_socket2storage, _msg_send);
					_msg_recv = this.recv(_socket2storage);
					_socket2storage.close();
				}
			}
			System.out.print("Add successfully\n");
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
			System.out.print(_msg_recv+"\n");
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
			System.out.print(_msg_recv+"\n");
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
			System.out.print(_msg_recv+"\n");
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
			System.out.print(_msg_recv+"\n");
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
		/*
		 * What if _path is null ???
		 */
		String _name = this.getFileName(target_file);
		String _msg_send = "List\n"+_path+"\n"+_name;
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
	
	private String getFileName(String _target_file){
		File _f = new File(_target_file);
		return _f.getName();
	}
	
	private String getPathOfFile(String _target_file){
		File _f = new File(_target_file);
		return _f.getParent();
	}
	
}

public class client {
	public  String serverIP = "127.0.0.1";
	public  int serverPort = 6001;
	
	 public static void main(String[] args){
		 System.out.print("finish\n");
	 }
}
