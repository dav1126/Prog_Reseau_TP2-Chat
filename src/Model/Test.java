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
		Enumeration<NetworkInterface> nets =null;
		try
		{
			nets = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//        for (NetworkInterface netint : Collections.list(nets))
//			try
//			{
				findDnsSuffix();
//			} catch (SocketException e)
//			{
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
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
		
		
//		Client client = new Client();
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
	
	static void displayInterfaceInformation(NetworkInterface netint) throws SocketException {
        out.printf("Display name: %s\n", netint.getDisplayName());
        out.printf("Name: %s\n", netint.getName());
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            out.printf("InetAddress: %s\n", inetAddress);
        }
        out.printf("\n");
     }
	
	static String findDnsSuffix() {

		// First I get the hosts name
		// This one never contains the DNS suffix (Don't know if that is the case for all VM vendors)
		String hostName = null;
		try
		{
			hostName = InetAddress.getLocalHost().getHostName().toLowerCase();
			System.out.println("hostName"+hostName);
		} catch (UnknownHostException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Alsways convert host names to lower case. Host names are 
		// case insensitive and I want to simplify comparison.

		// Then I iterate over all network adapters that might be of interest
		Enumeration<NetworkInterface> ifs = null;
		try
		{
			ifs = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (ifs == null) return ""; // Might be null

		for (NetworkInterface iF : Collections.list(ifs)) { // convert enumeration to list.
		    try
			{
				if (!iF.isUp()) continue;
			} catch (SocketException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		    for (InetAddress address : Collections.list(iF.getInetAddresses())) {
		        if (address.isMulticastAddress()) continue;

		        // This name typically contains the DNS suffix. Again, at least on Oracle JDK
		        String name = address.getHostName().toLowerCase();
		        System.out.println(name);
		        if (name.startsWith(hostName)) {
		            String dnsSuffix = name.substring(hostName.length());
		            if (dnsSuffix.startsWith(".")) return dnsSuffix;
		        }
		    }
		}

		return "";
		}
}
