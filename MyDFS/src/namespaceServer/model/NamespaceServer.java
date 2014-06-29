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
	//�ڵ�˿ڣ��ڵ����
	HashMap<Integer,StorageNode> nodeList = new HashMap<Integer,StorageNode>();
	//�ļ������ļ�����
	HashMap<String,StorageFileMetadata> fileMetadataList = new HashMap<String,StorageFileMetadata>();
	//Ŀ¼
	StorageDir root = new StorageDir("root");
	private static NamespaceServer ns = null;
	public static NamespaceServer getNamespaceServer() {
		if(ns==null)
			ns = new NamespaceServer();
		return ns;
	}
	
	/**
	 * ���һ���ļ�
	 * @param path
	 * @param fileName
	 * @param blockSize
	 * @return ��ǰ���id�Լ������õĽڵ㼯��
	 */
	public synchronized HashMap<UUID,ArrayList<Integer>> addFile(String path, String fileName, int blockSize) {
		String fullFileName = path+fileName;
		if(this.fileMetadataList.containsKey(fullFileName))
			return null;
		if(nodeList.isEmpty())
			return null;
		HashMap<UUID,ArrayList<Integer>> temp = new HashMap<UUID,ArrayList<Integer>>();
		ArrayList<Integer> tempNodes = new ArrayList<Integer>();
		//���ɸÿ���
		UUID blockID = UUID.randomUUID();
		//������д洢�ڵ㲢���洢���ݴ�С����
		ArrayList<StorageNode> nodes = (ArrayList<StorageNode>)nodeList.values();
		Collections.sort(nodes, new Comparator<StorageNode>(){
			@Override
			public int compare(StorageNode arg0, StorageNode arg1) {
				// TODO Auto-generated method stub
				return arg0.getNodeSize()-arg1.getNodeSize();
			}
		});
		//�����С�Ľڵ㣬
		StorageNode node1 = nodes.get(0);
		//�������ļ����¿�
		StorageFileMetadata newFile = new StorageFileMetadata();
		StorageFileBlockMetadata newBlock = new StorageFileBlockMetadata(fullFileName);
		//�����¿���ӵ�һ���洢�ڵ����Ϣ
		newBlock.addNodePort(node1.getPort());
		newBlock.setBlockID(blockID);
		newBlock.setBlockSize(blockSize);
		//������ڶ���洢�ڵ㣬���ÿ���ӵڶ����洢�ڵ����Ϣ
		if(nodes.size()>1) {
			StorageNode node2 = nodes.get(1);
			newBlock.addNodePort(node2.getPort());
		}
		//�����ļ���ӿ���ļ���
		newFile.addBlock(blockID, newBlock);
		newFile.setFileName(fileName);
		newFile.setFullFileName(fullFileName);
		//������ļ���Ϣ
		this.fileMetadataList.put(fullFileName, newFile);
		//����С�ڵ�����Ӹÿ鲢���½ڵ���Ϣ
		node1.addBlock(blockID, newBlock);
		this.nodeList.put(node1.getPort(), node1);
		tempNodes.add(node1.getPort());
		//������ڣ��ڴ�С�Ľڵ�����Ӹÿ���Ϣ�����½ڵ���Ϣ
		if(nodes.size()>1) {
			StorageNode node2 = nodes.get(1);
			node2.addBlock(blockID, newBlock);
			this.nodeList.put(node2.getPort(), node2);
			tempNodes.add(node2.getPort());
		}
		temp.put(blockID, tempNodes);
		//��Ŀ¼��¼����Ӹ��ļ�,���ļ��Ĵ�����
		root.addFile(this.getPathsByString(path), fileName);
		return temp;
	}

	/**
	 * ��ȡ���ļ�
	 * @param path
	 * @param fileName
	 * @return ���ļ������п����Լ���Ӧ�Ľڵ���
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
	 * ɾ�����ļ�
	 * @param path
	 * @param fileName
	 * @return �ڵ㼰����Ҫɾ���Ŀ��
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
		//��ȡ�˸��ļ�������blockԪ����
		ArrayList<StorageFileBlockMetadata> bs = (ArrayList<StorageFileBlockMetadata>) blocks.values();
		HashMap<Integer,ArrayList<UUID>> temp = new HashMap<Integer,ArrayList<UUID>>();
		int size = bs.size();
		for(int i=0; i<size;i++){
			StorageFileBlockMetadata tempBlock = bs.get(i);
			//���ÿһ��block��id
			UUID blockID = tempBlock.getBlockID();
			//��block���ڵĴ洢�ڵ�Ķ˿�
			ArrayList<Integer> tempNodePorts = tempBlock.getNodePort();
			for(int tempPort:tempNodePorts) {
				//��¼��ɾ����Ҫ�Ľڵ�ż����ϱ�ɾ���Ŀ��
				if(temp.containsKey(tempPort)){
					temp.get(tempPort).add(blockID);
				}
				else {
					ArrayList<UUID> blockIDs = new ArrayList<UUID>();
					blockIDs.add(blockID);
					temp.put(tempPort, blockIDs);
				}
				//ɾ����¼�ÿ�����нڵ�����Ԫ����
				StorageNode tempNode = this.nodeList.get(tempPort);
				tempNode.deleteBlock(blockID);
				this.nodeList.put(tempPort, tempNode);
			}
		}
		//ɾ�����ļ���Ԫ����
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
		//���Ҫ��ɾ����Ŀ¼����
		StorageDir deletedDir = root.getDir(paths, dir);
		//���Ŀ¼�е���������
		HashMap<String,ArrayList<String>> allContent = deletedDir.list(paths, dir);
		//��õ�ǰĿ¼��·��
		path = path+dir+"/";
		//��õ�ǰĿ¼�������ļ�
		ArrayList<String> files = allContent.get("file");
		//ɾ����Щ�ļ�
		for(String fileName:files) {
			HashMap<Integer,ArrayList<UUID>> tempHash = this.myDeleteFile(path, fileName);
			//��ɾ�����ļ����ڵĽڵ㼯��
			ArrayList<Integer> ports = new ArrayList<Integer>(tempHash.keySet());
			for(int port:ports) {
				//�ýڵ��ϱ�ɾ���Ŀ鼯��
				ArrayList<UUID> blocks = tempHash.get(port);
				//���иýڵ�ļ�¼���������
				if(deleteNodeAndBlocks.containsKey(port)) {
					for(UUID blockID:blocks) {
						deleteNodeAndBlocks.get(port).add(blockID);
					}
				}
				//û�м�¼�������
				else {
					deleteNodeAndBlocks.put(port, blocks);
				}
			}
		}
		//��ȡ��ǰĿ¼��������Ŀ¼
		ArrayList<String> dirs = allContent.get("dir");
		//ɾ����Щ��Ŀ¼
		for(String dirName:dirs) {
			this.myDelDir(path, dirName, deleteNodeAndBlocks);
		}
		//ɾ����Ŀ¼
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
		//���ɸÿ���
		UUID blockID = UUID.randomUUID();
		//������д洢�ڵ㲢���洢���ݴ�С����
		ArrayList<StorageNode> nodes = (ArrayList<StorageNode>)nodeList.values();
		Collections.sort(nodes, new Comparator<StorageNode>(){
			@Override
			public int compare(StorageNode arg0, StorageNode arg1) {
				// TODO Auto-generated method stub
				return arg0.getNodeSize()-arg1.getNodeSize();
			}
		});
		//�����С�Ľڵ㣬
		StorageNode node1 = nodes.get(0);
		//����ļ��ʹ����¿�
		StorageFileMetadata newFile = this.fileMetadataList.get(fullFileName);
		StorageFileBlockMetadata newBlock = new StorageFileBlockMetadata(fullFileName);
		//�����¿���ӵ�һ���洢�ڵ����Ϣ
		newBlock.addNodePort(node1.getPort());
		newBlock.setBlockID(blockID);
		newBlock.setBlockSize(blockSize);
		//������ڶ���洢�ڵ㣬���ÿ���ӵڶ����洢�ڵ����Ϣ
		if(nodes.size()>1) {
			StorageNode node2 = nodes.get(1);
			newBlock.addNodePort(node2.getPort());
		}
		//�����ļ���ӿ�
		newFile.addBlock(blockID, newBlock);
		//�����ļ���Ϣ
//		this.fileMetadataList.put(fullFileName, newFile);
		//����С�ڵ�����Ӹÿ鲢���½ڵ���Ϣ
		node1.addBlock(blockID, newBlock);
		this.nodeList.put(node1.getPort(), node1);
		tempNodes.add(node1.getPort());
		//������ڣ��ڴ�С�Ľڵ�����Ӹÿ���Ϣ�����½ڵ���Ϣ
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
	 * ��·�����ΪĿ¼���飬Ŀ¼��������Ϊroot���߿�
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
		//�Ƴ��ýڵ�
		nodeList.remove(nodePort);
		if(nodeList.size()<=1)
			return;
		//�������Ҫ�ƶ��Ŀ�
		HashMap<UUID,StorageFileBlockMetadata> blocks = deletedNode.getBlocks();
		{//�޸�Ԫ����  ����?
			for(UUID uid : blocks.keySet()){
				StorageFileBlockMetadata _sfbm = blocks.get(uid);
				_sfbm.deleteNodePort(nodePort);
			}
		}
		//�������Ҫ�ƶ��Ŀ�id
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
		//��һ���ڵ� �Լ� �ڵ���ڵ����
		//�ж��ǲ��ǵڶ����ڵ�
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
