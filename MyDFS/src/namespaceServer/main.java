package namespaceServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import namespaceServer.model.NamespaceServer;
import namespaceServer.mySocket.DataOperationsThread;
import namespaceServer.mySocket.HeartBeatThread;
import namespaceServer.mySocket.ProcessAccessThread;

public class main {
	public final static boolean debug_mode = true;
	public static void main(String[] args) {

		{
			DataOperationsThread _dpt = new DataOperationsThread();
			_dpt.start();
			HeartBeatThread _hbt = new HeartBeatThread();
			_hbt.start();
			ProcessAccessThread _pat = new ProcessAccessThread();
			_pat.start();
		}
/*		Thread dataOperationThread = new DataOperationsThread();
		Thread nodeOperationThread = new ProcessAccessThread();
		try {
			dataOperationThread.join();
			nodeOperationThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		NamespaceServer ns = NamespaceServer.getNamespaceServer();
		ns.test();
		boolean mk;
		mk=ns.mkdir("/root/", "test");
		mk=ns.mkdir("/root/", "test2");
		mk=ns.mkdir("/root/", "test3");
		ns.addFile("/root/test/", "test");
		ns.append("/root/test/", "test", 10);
		ns.addFile("/root/test2/", "test2");
		ns.append("/root/test2/", "test2", 20);
		mk = ns.addFile("/root/test3/", "test3");
		System.out.println("add "+mk);
		HashMap<UUID,ArrayList<Integer>> append = ns.append("/root/test3/", "test3", 10);
		System.out.println("append1 "+append);
		append = ns.append("/root/test3/", "test4", 10);
		System.out.println("append2 "+append);
		HashMap<String,ArrayList<String>> list2 = ns.list("", "root");
		System.out.println("list2 "+list2);
		HashMap<UUID,ArrayList<Integer>> tempget = ns.getFile("/root/test3/", "test3");
		System.out.println("get "+tempget);
		HashMap<Integer,ArrayList<UUID>> tempdelf = ns.deleteFile("/root/test3/", "test3");
		System.out.println("del "+tempdelf);
		boolean exist = ns.existFile("/root/test3/", "test3");
		System.out.println("exist1 "+exist);
		exist = ns.existFile("/root/test2/", "test2");
		System.out.println("exist2 "+exist);
		exist = ns.existFile("/root/", "test1");
		System.out.println("exist3 "+exist);
		int size = ns.sizeFile("/root/test3/", "test3");
		System.out.println("size1 "+size);
		size = ns.sizeFile("/root/test2/", "test2");
		System.out.println("size2 "+size);
		size = ns.sizeFile("/root/", "test1");
		System.out.println("size3 "+size);
		HashMap<Integer,ArrayList<UUID>> tempdeld = ns.delDir("/root/", "test3");
		System.out.println("deld1 "+tempdeld.toString());
		HashMap<Integer,ArrayList<UUID>> tempdeld2 = ns.delDir("/root/", "test2");
		System.out.println("deld2 "+tempdeld2.toString());
		HashMap<String,ArrayList<String>> list = ns.list("/root/", "test");
		System.out.println("list1 "+list);
		mk=ns.mkdir("/root/", "test4");
		list = ns.list("/root/", "test4");
		System.out.println("list2 "+list);
*/
	}

}
