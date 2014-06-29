package namespaceServer.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore.Entry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import javax.swing.SpringLayout.Constraints;

public class NamespaceServer { 
	//节点端口：节点对象
	HashMap<Integer,StorageNode> nodeList = new HashMap<Integer,StorageNode>();
	//文件名：文件对象
	HashMap<String,StorageFileMetadata> fileMetadataList = new HashMap<String,StorageFileMetadata>();
	//目录
	StorageDir root = new StorageDir("root");
	private static NamespaceServer ns = null;
	public static NamespaceServer getNamespaceServer() {
		if(ns==null)
			ns = new NamespaceServer();
		return ns;
	}
	
	/**
	 * 添加一个文件
	 * @param path
	 * @param fileName
	 * @param blockSize
	 * @return 当前块的id以及被放置的节点集合
	 */
	public synchronized HashMap<UUID,ArrayList<Integer>> addFile(String path, String fileName, int blockSize) {
		String fullFileName = path+fileName;
		if(this.fileMetadataList.containsKey(fullFileName))
			return null;
		if(nodeList.isEmpty())
			return null;
		HashMap<UUID,ArrayList<Integer>> temp = new HashMap<UUID,ArrayList<Integer>>();
		ArrayList<Integer> tempNodes = new ArrayList<Integer>();
		//生成该块编号
		UUID blockID = UUID.randomUUID();
		//获得所有存储节点并按存储数据大小排序
		ArrayList<StorageNode> nodes = (ArrayList<StorageNode>)nodeList.values();
		Collections.sort(nodes, new Comparator<StorageNode>(){
			@Override
			public int compare(StorageNode arg0, StorageNode arg1) {
				// TODO Auto-generated method stub
				return arg0.getNodeSize()-arg1.getNodeSize();
			}
		});
		//获得最小的节点，
		StorageNode node1 = nodes.get(0);
		//创建新文件和新块
		StorageFileMetadata newFile = new StorageFileMetadata();
		StorageFileBlockMetadata newBlock = new StorageFileBlockMetadata(fullFileName);
		//给该新块添加第一个存储节点的信息
		newBlock.addNodePort(node1.getPort());
		newBlock.setBlockID(blockID);
		newBlock.setBlockSize(blockSize);
		//如果存在多个存储节点，给该块添加第二个存储节点的信息
		if(nodes.size()>1) {
			StorageNode node2 = nodes.get(1);
			newBlock.addNodePort(node2.getPort());
		}
		//给新文件添加块和文件名
		newFile.addBlock(blockID, newBlock);
		newFile.setFileName(fileName);
		newFile.setFullFileName(fullFileName);
		//添加新文件信息
		this.fileMetadataList.put(fullFileName, newFile);
		//在最小节点中添加该块并更新节点信息
		node1.addBlock(blockID, newBlock);
		this.nodeList.put(node1.getPort(), node1);
		tempNodes.add(node1.getPort());
		//如果存在，在次小的节点中添加该块信息并更新节点信息
		if(nodes.size()>1) {
			StorageNode node2 = nodes.get(1);
			node2.addBlock(blockID, newBlock);
			this.nodeList.put(node2.getPort(), node2);
			tempNodes.add(node2.getPort());
		}
		temp.put(blockID, tempNodes);
		//在目录记录中添加该文件,用文件的纯名称
		root.addFile(this.getPathsByString(path), fileName);
		return temp;
	}

	/**
	 * 获取该文件
	 * @param path
	 * @param fileName
	 * @return 该文件的所有块编号以及对应的节点编号
	 */
	public synchronized HashMap<UUID,ArrayList<Integer>> getFile(String path, String fileName) {
		String fullFileName = path+fileName;
		if(!fileMetadataList.containsKey(fullFileName))
			return null;
		StorageFileMetadata sfm = this.fileMetadataList.get(fullFileName);
		HashMap<UUID,StorageFileBlockMetadata> blocks = sfm.getBlocks();
		ArrayList<StorageFileBlockMetadata> bs = (ArrayList<StorageFileBlockMetadata>) blocks.values();
		HashMap<UUID,ArrayList<Integer>> temp = new HashMap<UUID,ArrayList<Integer>>();
		int size = bs.size();
		for(int i=0; i<size;i++){
			StorageFileBlockMetadata tempBlock = bs.get(i);
			temp.put(tempBlock.getBlockID(), tempBlock.getNodePort());
		}
		return temp;
	}

	/**
	 * 删除该文件
	 * @param path
	 * @param fileName
	 * @return 节点及其所要删除的块号
	 */
	public synchronized HashMap<Integer,ArrayList<UUID>> deleteFile(String path, String fileName) {
		return myDeleteFile(path, fileName);
	}
	
	private HashMap<Integer,ArrayList<UUID>> myDeleteFile(String path, String fileName) {
		String fullFileName = path+fileName;
		if(!fileMetadataList.containsKey(fullFileName))
			return null;
		StorageFileMetadata sfm = this.fileMetadataList.get(fullFileName);
		HashMap<UUID,StorageFileBlockMetadata> blocks = sfm.getBlocks();
		//获取了该文件的所有block元数据
		ArrayList<StorageFileBlockMetadata> bs = (ArrayList<StorageFileBlockMetadata>) blocks.values();
		HashMap<Integer,ArrayList<UUID>> temp = new HashMap<Integer,ArrayList<UUID>>();
		int size = bs.size();
		for(int i=0; i<size;i++){
			StorageFileBlockMetadata tempBlock = bs.get(i);
			//获得每一个block的id
			UUID blockID = tempBlock.getBlockID();
			//该block所在的存储节点的端口
			ArrayList<Integer> tempNodePorts = tempBlock.getNodePort();
			for(int tempPort:tempNodePorts) {
				//记录有删除需要的节点号及其上被删除的块号
				if(temp.containsKey(tempPort)){
					temp.get(tempPort).add(blockID);
				}
				else {
					ArrayList<UUID> blockIDs = new ArrayList<UUID>();
					blockIDs.add(blockID);
					temp.put(tempPort, blockIDs);
				}
				//删除记录该块的所有节点的相关元数据
				StorageNode tempNode = this.nodeList.get(tempPort);
				tempNode.deleteBlock(blockID);
				this.nodeList.put(tempPort, tempNode);
			}
		}
		//删除该文件的元数据
		this.fileMetadataList.remove(fullFileName);
		root.deleteFile(this.getPathsByString(path), fileName);
		return temp;
	}

	public synchronized boolean existFile(String path, String fileName) {
		String fullFileName = path+fileName;
		return this.fileMetadataList.containsKey(fullFileName);
	}

	public synchronized int sizeFile(String path, String fileName) {
		String fullFileName = path+fileName;
		StorageFileMetadata tempFile = this.fileMetadataList.get(fullFileName);
		if(tempFile==null)
			return 0;
		return tempFile.getFileSize();
	}

	public synchronized void mkdir(String path, String dir) {
		root.addNewDir(this.getPathsByString(path), new StorageDir(dir));
	}

	public synchronized HashMap<Integer,ArrayList<UUID>> delDir(String path, String dir) {
		HashMap<Integer,ArrayList<UUID>> deleteNodeAndBlocks = new HashMap<Integer,ArrayList<UUID>>();
		myDelDir(path,dir,deleteNodeAndBlocks);
		return deleteNodeAndBlocks;
	}
	private void myDelDir(String path, String dir,HashMap<Integer,ArrayList<UUID>> deleteNodeAndBlocks) {
		ArrayList<String> paths = this.getPathsByString(path);
		//获得要被删除的目录对象
		StorageDir deletedDir = root.getDir(paths, dir);
		//获得目录中的所有内容
		HashMap<String,ArrayList<String>> allContent = deletedDir.list(paths, dir);
		//获得当前目录的路径
		path = path+dir+"/";
		//获得当前目录的所有文件
		ArrayList<String> files = allContent.get("file");
		//删除这些文件
		for(String fileName:files) {
			HashMap<Integer,ArrayList<UUID>> tempHash = this.myDeleteFile(path, fileName);
			//被删除的文件所在的节点集合
			ArrayList<Integer> ports = new ArrayList<Integer>(tempHash.keySet());
			for(int port:ports) {
				//该节点上被删除的块集合
				ArrayList<UUID> blocks = tempHash.get(port);
				//已有该节点的记录，继续添加
				if(deleteNodeAndBlocks.containsKey(port)) {
					for(UUID blockID:blocks) {
						deleteNodeAndBlocks.get(port).add(blockID);
					}
				}
				//没有记录，新添加
				else {
					deleteNodeAndBlocks.put(port, blocks);
				}
			}
		}
		//获取当前目录的所有子目录
		ArrayList<String> dirs = allContent.get("dir");
		//删除这些子目录
		for(String dirName:dirs) {
			this.myDelDir(path, dirName, deleteNodeAndBlocks);
		}
		//删除该目录
		root.deleteDir(paths, dir);
	}

	public HashMap<String,ArrayList<String>> list(String path, String dir) {
		return root.list(this.getPathsByString(path), dir);
	}

	public synchronized HashMap<UUID,ArrayList<Integer>> append(String path, String fileName,int blockSize) {
		String fullFileName = path+fileName;
		if(!this.fileMetadataList.containsKey(fullFileName))
			return null;
		if(nodeList.isEmpty())
			return null;
		HashMap<UUID,ArrayList<Integer>> temp = new HashMap<UUID,ArrayList<Integer>>();
		ArrayList<Integer> tempNodes = new ArrayList<Integer>();
		//生成该块编号
		UUID blockID = UUID.randomUUID();
		//获得所有存储节点并按存储数据大小排序
		ArrayList<StorageNode> nodes = (ArrayList<StorageNode>)nodeList.values();
		Collections.sort(nodes, new Comparator<StorageNode>(){
			@Override
			public int compare(StorageNode arg0, StorageNode arg1) {
				// TODO Auto-generated method stub
				return arg0.getNodeSize()-arg1.getNodeSize();
			}
		});
		//获得最小的节点，
		StorageNode node1 = nodes.get(0);
		//获得文件和创建新块
		StorageFileMetadata newFile = this.fileMetadataList.get(fullFileName);
		StorageFileBlockMetadata newBlock = new StorageFileBlockMetadata(fullFileName);
		//给该新块添加第一个存储节点的信息
		newBlock.addNodePort(node1.getPort());
		newBlock.setBlockID(blockID);
		newBlock.setBlockSize(blockSize);
		//如果存在多个存储节点，给该块添加第二个存储节点的信息
		if(nodes.size()>1) {
			StorageNode node2 = nodes.get(1);
			newBlock.addNodePort(node2.getPort());
		}
		//给新文件添加块
		newFile.addBlock(blockID, newBlock);
		//更新文件信息
//		this.fileMetadataList.put(fullFileName, newFile);
		//在最小节点中添加该块并更新节点信息
		node1.addBlock(blockID, newBlock);
		this.nodeList.put(node1.getPort(), node1);
		tempNodes.add(node1.getPort());
		//如果存在，在次小的节点中添加该块信息并更新节点信息
		if(nodes.size()>1) {
			StorageNode node2 = nodes.get(1);
			node2.addBlock(blockID, newBlock);
			this.nodeList.put(node2.getPort(), node2);
			tempNodes.add(node2.getPort());
		}
		temp.put(blockID, tempNodes);
		return temp;
	}
	
	/**
	 * 把路径拆解为目录数组，目录名不允许为root或者空
	 * @param pathS
	 * @return
	 */
	private ArrayList<String> getPathsByString(String pathS) {
		String[] s = pathS.split("/");
		ArrayList<String> paths = new ArrayList<String>();
		int size = s.length;
		for(int i=0;i<size;i++) {
			if(!(s[i].equals("")||s[i].equals("root")))
				paths.add(s[i]);
		}
		return paths;
	}
	
	public synchronized void removeNode(int nodePort) {
		StorageNode deletedNode = nodeList.get(nodePort);
		//移除该节点
		nodeList.remove(nodePort);
		if(nodeList.size()<=1)
			return;
		//获得所有要移动的块
		HashMap<UUID,StorageFileBlockMetadata> blocks = deletedNode.getBlocks();
		{//修改元数据  问题?
			for(UUID uid : blocks.keySet()){
				StorageFileBlockMetadata _sfbm = blocks.get(uid);
				_sfbm.deleteNodePort(nodePort);
			}
		}
		//获得所有要移动的块id
		ArrayList<UUID> blockIDs = new ArrayList<UUID>(blocks.keySet());
		ArrayList<StorageNode> snList = new ArrayList<StorageNode>();
		{
			for(Integer iI:nodeList.keySet()){
				snList.add(nodeList.get(iI));
			}
		}
		for(UUID uid : blockIDs){
			int _port = blocks.get(uid).getOnePort();
			StorageNode _from = nodeList.get(_port);
			
			Collections.sort(snList);
			for(int i = 0; i < snList.size(); i ++){
				if(snList.get(i).existBlock(uid)){
					continue;
				}
				this.transerBlock(_from, snList.get(i), uid);
				break;
			}
		}
	}
	
	public synchronized void addNode(int nodePort) {
		//add the nodes first
		StorageNode _new_node = null;
		{
			_new_node = new StorageNode();
			_new_node.setPort(nodePort);
			nodeList.put(nodePort, _new_node);
		}
		//第一个节点 以及 节点存在的情况
		//判断是不是第二个节点
		{
			if(nodeList.size() == 2){
				StorageNode _first_node = null;
				for(Integer iI:nodeList.keySet()){
					_first_node = nodeList.get(iI);
				}
				final HashMap<UUID, StorageFileBlockMetadata> blocks
					= _first_node.getBlocks();
				transerBlocks(_first_node, _new_node, blocks);
			}
		}
	}
	
	public synchronized void heartBeat(){
		HashSet<Integer> lostSet = new HashSet<Integer>();
		for(java.util.Map.Entry<Integer, StorageNode> iE: nodeList.entrySet()){
			StorageNode _node = iE.getValue();
			int _heart_beat_port = _node.getPort() + 1;
			Socket _socket = null;
			BufferedReader _br = null;  
		    PrintWriter _pw = null;
		    String _line = null;
			try {
				_socket = new Socket("127.0.0.1", _heart_beat_port);
				_socket.setSoTimeout(100);
				_br = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
				_pw = new PrintWriter
						(new BufferedWriter
								(new OutputStreamWriter
										(_socket.getOutputStream())),true);  
				_pw.println("HEARTBEAT");
				_pw.flush();
				_line = _br.readLine();
				if(_line == null){
					lostSet.add(_heart_beat_port);
				}
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.print("\n[node of port "+_heart_beat_port+" is lost]\n");
				lostSet.add(_heart_beat_port);
			}
			
		}
	  for(Integer iI:lostSet){
		 this.removeNode(iI.intValue());
		  /* Assume that atmost one node crash once */
		  break;
	  }
	}
	
	
	
	private synchronized void transerBlocks(
			StorageNode _from, StorageNode _to, 
			final HashMap<UUID, StorageFileBlockMetadata> _blocks
			)
	{
		for(UUID uid : _blocks.keySet()){
			transerBlock(_from, _to, uid);
		}
	}
	
	private synchronized void transerBlock(
			StorageNode _from, StorageNode _to, 
			UUID _blocks
			){
		
	}
}
