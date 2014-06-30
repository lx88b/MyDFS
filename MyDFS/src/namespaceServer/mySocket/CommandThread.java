package namespaceServer.mySocket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class CommandThread extends Thread{
	private String target_IP = "127.0.0.1";
	private int target_port;
	private String command;
	private String data;
	public CommandThread(
			int node_port, 
			int transPort, 
			String transType,
			String _block
			) 
	{
		// TODO Auto-generated constructor stub
		target_port = node_port;
		command = transType + "\n" +transPort+"\n"+_block;
	}
	
	public void setTransferToComm(
			int node_port, 
			int transToPort, 
			String _block)
	{
		target_port = node_port;
		command = "SEND\n"+transToPort+"\n"+_block;
	}
	
	public void setFormTransComm(
			int node_port, 
			int FromtransPort, 
			String _block)
	{
		target_port = node_port;
		command = "RECV\n"+FromtransPort+"\n"+_block;
	}
	
	@Override
	public void run(){
		Socket _socket = null;
		PrintWriter _pw = null;
		try {
			 _socket = new Socket(target_IP, target_port);
			 this.send(_socket, command);	
			 _socket.close();
			 
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
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
