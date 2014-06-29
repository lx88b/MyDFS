package namespaceServer.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class StorageDir {
	private String dirName;
	private HashMap<String,StorageDir> dirList = new HashMap<String,StorageDir>();
//	private HashMap<String,StorageFileMetadata> fileList = new HashMap<String,StorageFileMetadata>();
	private HashSet<String> fileList = new HashSet<String>();
	
	public StorageDir(String name) {
		dirName = name;
	}
	
	public String getName() {
		return this.dirName;
	}
	
	/**
	 * 测试该路径是否存在
	 * @param paths
	 * @return
	 */
	public boolean testPath(ArrayList<String> paths) {
		if(paths.size()==0)
			return true;
		else {
			if(this.containThisDir(paths.get(0))) {
				String tempName = paths.get(0);
				paths.remove(0);
				return this.dirList.get(tempName).testPath(paths);
			}
			else
				return false;
		}
	}
	
	/**
	 * 添加新目录
	 * @param paths 上级目录地址集合
	 * @param dir 被添加目录
	 * @return
	 */
	public boolean addNewDir(ArrayList<String> paths, StorageDir dir) {
		if(paths.size()==0) {
			if(dirList.containsKey(dir.getName()))
				return false;
			dirList.put(dir.getName(), dir);
		}
		else {
			String dirName = paths.get(0);
			paths.remove(0);
			StorageDir temp = dirList.get(dirName);
			if(temp==null)
				return false;
			temp.addNewDir(paths, dir);
		}
		return true;
	}
	
	public boolean containThisDir(String thisDirName) {
		return this.dirList.containsKey(thisDirName);
	}
	
	/**
	 * 获取目录
	 * @param paths 目录的完整路径
	 * @param dir 目录名称
	 * @return
	 */
	public StorageDir getDir(ArrayList<String> paths, String dir) {
		if(paths.size()==0)
			return this.dirList.get(dir);
		String dirName = paths.get(0);
		paths.remove(0);
		StorageDir temp = dirList.get(dirName);
		if(temp==null)
			return null;
		return temp.getDir(paths, dir);
	}
	
	/**
	 * 删除目录
	 * @param paths 目录的完整名称
	 * @param dir
	 * @return 返回被删除的目录，继续执行删除目录内文件和目录
	 */
	public StorageDir deleteDir(ArrayList<String> paths, String dir) {
		StorageDir temp = null;
		if(paths.size()==0) {
			temp = this.dirList.get(dir);
			this.dirList.remove(dir);
			return temp;
		}
		String dirName = paths.get(0);
		paths.remove(0);
		temp = dirList.get(dirName);
		if(temp==null)
			return null;
		return temp.deleteDir(paths, dir);
	}
	
	/**
	 * 列出目录中的所有内容
	 * @param paths
	 * @param dirN
	 * @return
	 */
	public HashMap<String,ArrayList<String>> list(ArrayList<String> paths, String dirN) {
		if(paths.size()==0) {
			StorageDir dir = this.dirList.get(dirN);
			if(dir==null)
				return null;
			HashMap<String,ArrayList<String>> temp = new HashMap<String,ArrayList<String>>();
			ArrayList<String> files = new ArrayList<String>(dir.fileList);
			temp.put("file", files);
			ArrayList<String> dirs = new ArrayList<String>(dir.dirList.keySet());
			temp.put("dir", dirs);
			return temp;
		}
		String dirName = paths.get(0);
		paths.remove(0);
		StorageDir temp = dirList.get(dirName);
		if(temp==null)
			return null;
		return temp.list(paths, dirN);
	}
	
	/**
	 * 文件是否存在
	 * @param paths
	 * @param fileName
	 * @return
	 */
	public boolean hasFile(ArrayList<String> paths, String fileName) {
		if(paths.size()==0)
			return this.fileList.contains(fileName);
		String dirName = paths.get(0);
		paths.remove(0);
		StorageDir temp = dirList.get(dirName);
		if(temp==null)
			return false;
		return temp.hasFile(paths, fileName);
	}
	
	/**
	 * 删除文件
	 * @param paths
	 * @param fileName
	 * @return
	 */
	public boolean deleteFile(ArrayList<String> paths, String fileName) {
		if(paths.size()==0) {
			if(this.fileList.contains(fileName)) {
				this.fileList.remove(fileName);
				return true;
			}
			return false;
		}
		String dirName = paths.get(0);
		paths.remove(0);
		StorageDir temp = dirList.get(dirName);
		if(temp==null)
			return false;
		return temp.deleteFile(paths, fileName);
	}
	
	/**
	 * 添加文件
	 * @param paths
	 * @param fileName
	 * @return
	 */
	public boolean addFile(ArrayList<String> paths, String fileName) {
		if(paths.size()==0) {
			if(this.fileList.contains(fileName)) {
				return false;
			}
			this.fileList.add(fileName);
			return true;
		}
		String dirName = paths.get(0);
		paths.remove(0);
		StorageDir temp = dirList.get(dirName);
		if(temp==null)
			return false;
		return temp.addFile(paths, fileName);
	}

}
