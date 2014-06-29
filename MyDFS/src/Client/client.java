package Client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
	public clientOperation(
			String _ip, int _port,
			clientOPType _op,
			String _file,
			String _data
							)
	{
		OPtype = _op;
		target_IP = _ip;
		target_port = _port;
		target_file = _file; 
		Data = _data;
	}
	@Override
	public void run(){
		System.out.print(OPtype + "\n");
		System.out.print(target_IP + "\n");
		System.out.print(OPtype + "\n");
		System.out.print(Data + "\n");
	}
	
	public void get(){
		String _msg_send = "GET\t"+target_file;
		String _msg_recv = null;
		Socket _socket2server = null;
		Socket _socket2storage = null;
		try {
			_socket2server = new Socket(target_IP, target_port);
			this.send(_socket2server, _msg_send);
			_msg_recv = this.recv(_socket2server);
			ArrayList<Address> _store_list = this.getIP_Port(_msg_recv);
			StringBuffer _sb = new StringBuffer();
			for(Address iAddr:_store_list)
			{
				_socket2storage = new Socket(iAddr.IP, iAddr.Port);
				_msg_send = "GET\t" + "TARGET_BLOCK";
				this.send(_socket2storage, _msg_send);
				_msg_recv = this.recv(_socket2storage);
				_sb.append(this.getBlockData(_msg_recv));
				_socket2storage.close();
			}
			_socket2server.close();
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void add(){
		String _msg_send = "ADD\t"+target_file+"\t"+Data;
		String _msg_recv = null;
		Socket _socket2server = null;
		Socket _socket2storage = null;
		try {
			_socket2server = new Socket(target_IP, target_port);
			this.send(_socket2server, _msg_send);
			_msg_recv = this.recv(_socket2server);
			ArrayList<Address> _store_list = this.getIP_Port(_msg_recv);
			for(Address iAddr:_store_list)
			{
				_socket2storage = new Socket(iAddr.IP, iAddr.Port);
				_msg_send = "ADD\t" + "TARGET_BLOCK" + "\t" + Data;
				this.send(_socket2storage, _msg_send);
				_msg_recv = this.recv(_socket2storage);
				System.out.println(_msg_recv);
				_socket2storage.close();
			}
			_socket2server.close();
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private ArrayList<Address> getIP_Port(String _recv){
		ArrayList<Address> _addr_list = null;
		{
			
		}
		return _addr_list;
	}
	private String getBlockData(String _recv){
		String _ret = null;
		
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
	
}

public class client {
		 public static void main(String[] args){
			 System.out.print("finish\n");
		 }
}
