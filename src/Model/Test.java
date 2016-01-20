package Model;

import java.io.File;
import static java.lang.System.out;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;

public class Test
{

	public static void main(String[] args)
	{
		
		//File file = new File("C:\\mshtml.dll");
		
//		Server server = new Server();
//		server.startOpenSocketThread(55555);
//		
//		System.out.println("test");
//		while(!server.connectionEstablished)
//		{
//			try {
//				Thread.sleep(10);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		server.startReceiveMessageThread();
//		server.startReceiveMessageThread();
		
		
		Client client = new Client();
	client.findDnsSuffix();
	try
	{
		client.getLANBroadcastAddress();
	} catch (SocketException e)
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
////		client.openClientSocket("172.18.10.22", 55555);
//		client.openUDPSocket();
//		client.getRemoteIpAvailableForChat("172.18.10.22");
//		
		//client.sendMessage("Coucou me voilà!");
		
//		client.startSendFile(file);
//		
//		try
//		{
//			Thread.sleep(5000);
//		} catch (InterruptedException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	
		//client.sendMessage("Me revoila");
		
		//http://michieldemey.be/blog/network-discovery-using-udp-broadcast/
	}
	
	
}
