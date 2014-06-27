package namespaceServer.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.UUID;

public class NamespaceServer {
	//�ڵ�˿ڣ��ڵ����
	HashMap<Integer,StorageNode> nodeList = new HashMap<Integer,StorageNode>();
	//�ļ������ļ�����
	HashMap<String,StorageFileMetadata> fileMetadataList = new HashMap<String,StorageFileMetadata>();
	//Ŀ¼��:<���ͣ�Ŀ¼�ļ�����>
	private Object sysNodeObject = new Object();
	private Object sysfileObject = new Object();
	private static NamespaceServer ns = null;
	public static NamespaceServer getNamespaceServer() {
		if(ns==null)
			ns = new NamespaceServer();
		return ns;
	}
	
	/**
	 * 
	 * @param fileName
	 * @param blockSize
	 * @return ��ǰ���id�Լ������õĽڵ㼯��
	 */
	public synchronized HashMap<UUID,ArrayList<Integer>> addFile(String fileName, int blockSize) {
		if(this.fileMetadataList.containsKey(fileName))
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
		StorageFileBlockMetadata newBlock = new StorageFileBlockMetadata();
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
		//������ļ���Ϣ
		this.fileMetadataList.put(fileName, newFile);
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
	 * 
	 * @param fileName
	 * @return ���ļ������п����Լ���Ӧ�Ľڵ���
	 */
	public synchronized HashMap<UUID,ArrayList<Integer>> getFile(String fileName) {
		if(!fileMetadataList.containsKey(fileName))
			return null;
		StorageFileMetadata sfm = this.fileMetadataList.get(fileName);
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
	 * 
	 * @param fileName
	 * @return ��ɾ�����ļ������п���Լ����ڽڵ�
	 */
	public synchronized HashMap<UUID,ArrayList<Integer>> deleteFile(String fileName) {
		if(!fileMetadataList.containsKey(fileName))
			return null;
		StorageFileMetadata sfm = this.fileMetadataList.get(fileName);
		HashMap<UUID,StorageFileBlockMetadata> blocks = sfm.getBlocks();
		//��ȡ�˸��ļ�������blockԪ����
		ArrayList<StorageFileBlockMetadata> bs = (ArrayList<StorageFileBlockMetadata>) blocks.values();
		HashMap<UUID,ArrayList<Integer>> temp = new HashMap<UUID,ArrayList<Integer>>();
		int size = bs.size();
		for(int i=0; i<size;i++){
			StorageFileBlockMetadata tempBlock = bs.get(i);
			//���ÿһ��block��id
			UUID blockID = tempBlock.getBlockID();
			//��block���ڵĴ洢�ڵ�Ķ˿�
			ArrayList<Integer> tempNodePorts = tempBlock.getNodePort();
			//��¼��ɾ�����ݵĿ�ż����ڽڵ�˿�
			temp.put(blockID, tempNodePorts);
			//ɾ����¼�ÿ�����нڵ�����Ԫ����
			for(int tempPort : tempNodePorts){
				StorageNode tempNode = this.nodeList.get(tempPort);
				tempNode.deleteBlock(blockID);
				this.nodeList.put(tempPort, tempNode);
			}
		}
		//ɾ�����ļ���Ԫ����
		this.fileMetadataList.remove(fileName);
		return temp;
	}

	public synchronized boolean existFile(String fileName) {
		return this.fileMetadataList.containsKey(fileName);
	}

	public synchronized int sizeFile(String fileName) {
		StorageFileMetadata tempFile = this.fileMetadataList.get(fileName);
		if(tempFile==null)
			return 0;
		return tempFile.getFileSize();
	}

	public void mkdir() {

	}

	public void delDir() {

	}

	public void list() {

	}

	public synchronized HashMap<UUID,ArrayList<Integer>> append(String fileName,int blockSize) {
		if(!this.fileMetadataList.containsKey(fileName))
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
		StorageFileMetadata newFile = this.fileMetadataList.get(fileName);
		StorageFileBlockMetadata newBlock = new StorageFileBlockMetadata();
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
		this.fileMetadataList.put(fileName, newFile);
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
}
