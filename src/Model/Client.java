package Model;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;

import javafx.application.Platform;

public class Client
{
	Socket clientSocket;
	DatagramSocket UDPsocket;
	private static final int MAX_TRANSMISSION_BYTE_SIZE = 10000000;
	private static final String FILE_TRANSMISSION_ALERT_MSG = 
			"NHRTYFHAPWLM*?DYXN!848145489WJD23243212owahAwfligLOP)(* ALPHA";
	private static final int UDP_SOCKET_NUMBER = 5556;
	private static final int SERVER_SOCKET_NUMBER = 5555;
	
	/**
	 * Objet servant a locker la liste observable de Ip atteignable
	 */
		Object lock = new Object();

	public void openClientSocket(String remoteIpAddress)
	{
		Thread thread =  new Thread(() ->
		{
			InetAddress adress = null;
			try
			{
				adress = InetAddress.getByName(remoteIpAddress);
			} catch (UnknownHostException e)
			{
				e.printStackTrace();
			}
	
			clientSocket = null;
			try
			{
				clientSocket = new Socket(adress, SERVER_SOCKET_NUMBER);
			} catch (IOException e)
			{
				System.out.println("Cannot open client socket");
				e.printStackTrace();
			}
		});
		thread.start();
	}

	public void sendMessage(String message)
	{
		Thread thread =  new Thread(() ->
		{
			//Wait for the clientSocket to be openend 
			//(this is done in another thread)
			while (clientSocket == null)
			{
				try
				{
					Thread.sleep(10);
				} catch (Exception e)
				{
					System.out.println("Send message thread sleep error");
					e.printStackTrace();
				}
			}
			try
			{
				OutputStream output = clientSocket.getOutputStream();
				output.write(message.getBytes());
			} catch (IOException e)
			{
				System.out.println("Message could not be sent");
				e.printStackTrace();
			}
		});
		thread.start();
	}

	public void closeSockets()
	{
		if (clientSocket != null)
		{
			try
			{
				clientSocket.close();
				UDPsocket.close();
			} catch (IOException e)
			{
				System.out.println("Client sockets could not be closed");
				clientSocket = null;
				e.printStackTrace();
			}
		} else
		{
			System.out
					.println("Could not close client sockets");
		}
	}

	/**
	 * Calls the appropriate functions to send a file to the server
	 * @param fileToSend 
	 */
	public void startSendFile(File fileToSend)
	{
		if (fileToSend.length() <= MAX_TRANSMISSION_BYTE_SIZE)
		{
			Thread thread =  new Thread(() ->
			{
				//Wait for the clientSocket to be openend 
				//(this is done in another thread)
				while (clientSocket == null)
				{
					try
					{
						Thread.sleep(10);
					} catch (Exception e)
					{
						System.out.println("Send file thread sleep error");
						e.printStackTrace();
					}
				}
				try
				{
					sendFileTransmissionAlert();
					sendFileName(fileToSend);
					sendFile(fileToSend);	
				} 
				catch (IOException e)
				{
					System.out.println("File could not be sent");
					e.printStackTrace();
				}
			});
			thread.start();
		}
		else
		{
			System.out.println("File is too big. 10 Mo maximum.");
		}
	}
	
	/**
	 * Send a message to the server that tells that the next transmission is
	 * gonna be a file, the wait for a confirmation form the server
	 */
	private void sendFileTransmissionAlert() throws IOException
	{
		// Send the message
			OutputStream output = clientSocket.getOutputStream();
			byte[] fileTransmissionCode = new String(
					FILE_TRANSMISSION_ALERT_MSG).getBytes();
			output.write(fileTransmissionCode);
			
			//Wait for a receipt confirmation from the server
			InputStream bIStream = clientSocket.getInputStream();
			
			byte[] byteBuffer = new byte[MAX_TRANSMISSION_BYTE_SIZE];
			int count = bIStream.read(byteBuffer);
			String msgInput = "";
			
			
			for (int i = 0; i < count ; i++)
				{
					msgInput += (char)byteBuffer[i];
				}
			
			System.out.println("Received Message from Server: " + msgInput);
	}
	
	/**
	 * Send the name of the file to the server, then wait for a confirmation
	 * from the server
	 */
	private void sendFileName(File fileToSend) throws IOException
	{
		//Send the name of the file
		OutputStream output = clientSocket.getOutputStream();
		byte[] nameOfFile = new String(fileToSend.getName()).getBytes();
		output.write(nameOfFile);
	
		//Wait for a receipt confirmation from the server
		InputStream bIStream = clientSocket.getInputStream();
		
		byte[] byteBuffer = new byte[MAX_TRANSMISSION_BYTE_SIZE];
		int countName = bIStream.read(byteBuffer);
		String msgInput = "";
		
		
		for (int i = 0; i < countName ; i++)
			{
				msgInput += (char)byteBuffer[i];
			}
		
		System.out.println("Received Message from Server: " 
							+ msgInput);
	}
	/**
	 * Send the file to the server
	 */
	private void sendFile(File fileToSend) throws IOException
	{
		//Send the file
		OutputStream output = clientSocket.getOutputStream();
		InputStream input = new FileInputStream(fileToSend);
		byte[] fileByteArray = new byte[MAX_TRANSMISSION_BYTE_SIZE];
		
		int count;
		while((count = input.read(fileByteArray)) > 0)
		{
			output.write(fileByteArray, 0, count);
		}
	
		//Wait for a receipt confirmation from the server
		InputStream bIStream = clientSocket.getInputStream();
		
		byte[] byteBuffer = new byte[MAX_TRANSMISSION_BYTE_SIZE];
		int countName = bIStream.read(byteBuffer);
		String msgInput = "";
		
		
		for (int i = 0; i < countName ; i++)
			{
				msgInput += (char)byteBuffer[i];
			}
		
		System.out.println("Received Message from Server: " 
							+ msgInput);
		input.close();
	}
	
	public void openUDPSocket()
	{
		Thread thread =  new Thread(() ->
		{
			try
			{
				UDPsocket = new DatagramSocket();
			} 
			catch (SocketException e)
			{
				System.out.println("Client could not open UDP socket");
				e.printStackTrace();
			}
		});
		thread.start();
	}
	
		
	/**
	 * Broadcast a message for a specific port on the subnetwork of a given ip
	 * address
	 *  @param ipAddress given ip address
	 */
	public void getRemoteUserAvailableForChat()
	{	//J'Ai utilisé le lAN2 est cest ce qui est renvoyé par localhost...je dois utiliser le lan1******************************* 
		Thread thread =  new Thread(() ->
		{
			//Keep checking for new remote users
			while (true)
			{
				//Wait for the UDPsocket to be openend (done by another thread)
				while (UDPsocket == null)
				{
					try
					{
						Thread.sleep(10);
					} catch (Exception e)
					{
						System.out.println("Get remote ips thread sleep error");
						e.printStackTrace();
					}
				}
				
				//Get the broadcast address of the LAN
				
				
				//Get the subnet (works for /16 subnets only)
//				String localAddress = null;
//				try
//				{
//					localAddress = InetAddress.getLocalHost().getHostAddress();
//					System.out.println("bla" +localAddress);
//				} catch (Exception e1)
//				{
//					
//					e1.printStackTrace();
//				}
//				String[] ipSplit = localAddress.split("\\.");
//				String subnetworkPartOfIpAddress = ipSplit[0] + "." + 
//						ipSplit[1] + ".";
//				
//				//Create the broadcast address
//				String broadcastAddress = subnetworkPartOfIpAddress + "255.255";
//				System.out.println("BROADCAST:" +broadcastAddress);
//				InetAddress brodcastInetAddress = null;
				
				InetAddress brodcastInetAddress = null;
				try
				{
					brodcastInetAddress = getLANBroadcastAddress();
				} 
				catch (Exception e1)
				{
					e1.printStackTrace();
				}
				
				byte[] buffer = new byte[1];
				DatagramPacket brodcastPacket = new DatagramPacket
						(buffer, buffer.length, brodcastInetAddress, UDP_SOCKET_NUMBER);
				try
				{
					UDPsocket.send(brodcastPacket);
				} 
				catch (IOException e)
				{
					System.out.println("Client could not send broadcast packet");
					e.printStackTrace();
				}
				System.out.println("Brodcast message sent from client.");
				
				//Wait for a response from a server, the server responds with
				//the username specified by the user
				byte[] bufferResponse = new byte[1024];
				DatagramPacket responsePacket = new DatagramPacket(bufferResponse, bufferResponse.length);
				try
				{
					UDPsocket.receive(responsePacket);
				} 
				catch (IOException e)
				{
					e.printStackTrace();
				}
				System.out.println("test");
				//Get the username sent by the server
				
				String username = new String(responsePacket.getData()).trim();
				
				//Get the ip address of the responding machine
				String remoteMachineIp = responsePacket.getAddress().getHostName();
		
				
				//Add the remote machine ip address to the available for chat list
				synchronized (lock)
				{
					ChatModel.getInstance().getAvailableForChatIpAddressList().add(username);
					ChatModel.getInstance().getUserAvailableToChatMap().put(remoteMachineIp, username);
				}
				System.out.println("Chat available with: " + username);
				
				//Put the thread to sleep for 2 seconds
				try
				{
					Thread.sleep(2000);
				} 
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		thread.start();
	}
	
	/**Find and returns the DNS suffix of the. 
	 * Plagiat alert!! This method is taken from internet!
	 * Source: http://stackoverflow.com/questions/6134790/how-do-i-access-the-connection-specific-dns-suffix-for-each-networkinterface-in 
	 * @return the dns suffix
	 */
	public String findDnsSuffix() {

		// First I get the hosts name
		// This one never contains the DNS suffix (Don't know if that is the case for all VM vendors)
		String hostName = null;
		try
		{
			hostName = InetAddress.getLocalHost().getHostName().toLowerCase();
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
	
	public InetAddress getLANBroadcastAddress() throws SocketException
	{
		InetAddress broadcastAddress = null;
		
		mainloop:for (
			    final Enumeration< NetworkInterface > interfaces =
			        NetworkInterface.getNetworkInterfaces( );
			    interfaces.hasMoreElements( );
			)
			{
			    final NetworkInterface cur = interfaces.nextElement( );

			    if ( cur.isLoopback( ) )
			    {
			        continue;
			    }
			    
			    
			    for ( final InterfaceAddress addr : cur.getInterfaceAddresses( ) )
			    {
			        final InetAddress inet_addr = addr.getAddress( );

			        if ( !( inet_addr instanceof Inet4Address ) )
			        {
			            continue;
			        }
			        
			        broadcastAddress = addr.getBroadcast( );
			        System.out.println("Broadcast address found: " 
			        		+ broadcastAddress.getHostAddress());
			        break mainloop;
			    }
			}
		return broadcastAddress;
	}
}
