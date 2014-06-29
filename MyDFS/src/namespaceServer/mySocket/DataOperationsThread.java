/**
 * 处理数据操作的线程，一次只能接受一个数据请求
 */
package namespaceServer.mySocket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import namespaceServer.model.NamespaceServer;

public class DataOperationsThread extends Thread {
	
	private ServerSocket s = null;
	private Socket socket = null;
	private BufferedReader br = null;  
    private PrintWriter pw = null;
	
	public DataOperationsThread(){
		try{
			start();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		try {
			s = new ServerSocket(6000);
			//服务器端始终等待接收请求，服务器端为6000接口
			while(true){
				socket = s.accept();
				br = new BufferedReader(new InputStreamReader(socket.getInputStream()));  
				pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);  
				//在这里调用处理请求的方法线程
//				new ProcessAccessThread(socket);
				while(true){  
		            String str;  
		            try {  
		                str = br.readLine();  
		                if(str.equals("END")||str==null){  
		                    br.close();  
		                    pw.close();  
		                    socket.close();  
		                    break;  
		                }  
		                //根据该请求处理
//		                System.out.println("Client Socket Message:"+str);  
		                else if(str.equals("Add")){
		                	String path = br.readLine();
		                	String fileName = br.readLine();
		                	int blockSize = Integer.parseInt(br.readLine());
		                	HashMap<UUID,ArrayList<Integer>> blocksAndPorts = NamespaceServer.getNamespaceServer().addFile(path, fileName, blockSize);
		                	ArrayList<UUID> blockIDs = new ArrayList<UUID>(blocksAndPorts.keySet());
		                	//每个uuid返回一行，uuid及端口用:分割
		                	for(UUID blockID:blockIDs) {
		                		ArrayList<Integer> ports = blocksAndPorts.get(blockID);
		                		String out = blockID.toString();
		                		for(int port:ports){
		                			out = out+":"+String.valueOf(port);
		                		}
		                		pw.println(out);
		                	}
		                }
		                else if(str.equals("Get")){
		                	String path = br.readLine();
		                	String fileName = br.readLine();
		                	HashMap<UUID,ArrayList<Integer>> blocksAndPorts = NamespaceServer.getNamespaceServer().getFile(path, fileName);
		                	ArrayList<UUID> blockIDs = new ArrayList<UUID>(blocksAndPorts.keySet());
		                	//每个uuid返回一行，uuid及端口用:分割
		                	for(UUID blockID:blockIDs) {
		                		ArrayList<Integer> ports = blocksAndPorts.get(blockID);
		                		String out = blockID.toString();
		                		for(int port:ports){
		                			out = out+":"+String.valueOf(port);
		                		}
		                		pw.println(out);
		                	}
		                }
		                else if(str.equals("Del")){
		                	String path = br.readLine();
		                	String fileName = br.readLine();
		                	HashMap<Integer,ArrayList<UUID>> portsAndBlocks = NamespaceServer.getNamespaceServer().deleteFile(path, fileName);
		                	if(portsAndBlocks==null) {
		                		pw.println("file is not exists");
		                	}
		                	else {
		                		SocketServer.getSocketServer().deleteRequest(portsAndBlocks);
		                		pw.println("success!");
		                	}
		                }
		                else if(str.equals("Exist")){
		                	String path = br.readLine();
		                	String fileName = br.readLine();
		                	boolean ans = NamespaceServer.getNamespaceServer().existFile(path, fileName);
		                	pw.println(ans);
		                }
		                else if(str.equals("Size")){
		                	String path = br.readLine();
		                	String fileName = br.readLine();
		                	int size = NamespaceServer.getNamespaceServer().sizeFile(path, fileName);
		                	pw.println(size);
		                }
		                else if(str.equals("Mkdir")){
		                	String path = br.readLine();
		                	String dirName = br.readLine();
		                	NamespaceServer.getNamespaceServer().mkdir(path, dirName);
		                	pw.println("finish");
		                }
		                else if(str.equals("Deldir")){
		                	String path = br.readLine();
		                	String dir = br.readLine();
		                	HashMap<Integer,ArrayList<UUID>> portsAndBlocks = NamespaceServer.getNamespaceServer().delDir(path, dir);
		                	if(portsAndBlocks==null) {
		                		pw.println("file or dir is not exists");
		                	}
		                	else {
		                		SocketServer.getSocketServer().deleteRequest(portsAndBlocks);
		                		pw.println("success!");
		                	}
		                }
		                else if(str.equals("List")){
		                	String path = br.readLine();
		                	String dir = br.readLine();
		                	HashMap<String,ArrayList<String>> listMap = NamespaceServer.getNamespaceServer().list(path, dir);
		                	String fileout="file";
		                	//如果该目录下有文件输出格式为“file:filename1:filename2:...”
		                	if(listMap.containsKey("file")&&(!listMap.get("file").isEmpty())) {
		                		ArrayList<String> files = listMap.get("file");
		                		for(String file:files) {
		                			fileout=fileout+":"+file;
		                		}
		                		pw.println(fileout);
		                	}
		                	String dirout = "dir";
		                	//如果该目录下有子目录输出格式为“dir:dirname1:dirname2:...”
		                	if(listMap.containsKey("dir")&&(!listMap.get("dir").isEmpty())) {
		                		ArrayList<String> dirs = listMap.get("dir");
		                		for(String ldir:dirs) {
		                			dirout=dirout+":"+ldir;
		                		}
		                		pw.println(dirout);
		                	}
		                }
		                else if(str.equals("Append")){
		                	String path = br.readLine();
		                	String fileName = br.readLine();
		                	int blockSize = Integer.parseInt(br.readLine());
		                	HashMap<UUID,ArrayList<Integer>> blocksAndPorts = NamespaceServer.getNamespaceServer().append(path, fileName, blockSize);
		                	ArrayList<UUID> blockIDs = new ArrayList<UUID>(blocksAndPorts.keySet());
		                	//每个uuid返回一行，uuid及端口用:分割
		                	for(UUID blockID:blockIDs) {
		                		ArrayList<Integer> ports = blocksAndPorts.get(blockID);
		                		String out = blockID.toString();
		                		for(int port:ports){
		                			out = out+":"+String.valueOf(port);
		                		}
		                		pw.println(out);
		                	}
		                }
		                else{}
		                pw.println("Message Received");  
		                pw.flush();  
		            } catch (Exception e) {  
		                try {  
		                    br.close();  
		                    pw.close();  
		                    socket.close();  
		                } catch (IOException e1) {   
		                    e1.printStackTrace();  
		                }  
		            }  
		        } 
			}
		} catch (Exception e) {
			try {
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}finally{
			try {
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
