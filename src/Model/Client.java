package Model;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import javafx.application.Platform;

public class Client
{
	Socket clientSocket;
	DatagramSocket UDPsocket;
	private static final int MAX_TRANSMISSION_BYTE_SIZE = 10000000;
	private static final String FILE_TRANSMISSION_ALERT_MSG = 
			"NHRTYFHAPWLM*?DYXN!848145489WJD23243212owahAwfligLOP)(* ALPHA";
	private static final int UDP_SOCKET_NUMBER = 5556;
	
	/**
	 * Objet servant a locker la liste observable de Ip atteignable
	 */
		Object lock = new Object();

	public void openClientSocket(String remoteIpAddress, int remotePort)
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
			clientSocket = new Socket(adress, remotePort);
		} catch (IOException e)
		{
			System.out.println("Cannot open client socket");
			e.printStackTrace();
		}
	}

	public void sendMessage(String message)
	{
		try
		{
			OutputStream output = clientSocket.getOutputStream();
			output.write(message.getBytes());
		} catch (IOException e)
		{
			System.out.println("Message could not be sent");
			e.printStackTrace();
		}
	}

	public void closeClientSocket()
	{
		if (clientSocket != null)
		{
			try
			{
				clientSocket.close();
			} catch (IOException e)
			{
				System.out.println("Client socket could not be closed");
				clientSocket = null;
				e.printStackTrace();
			}
		} else
		{
			System.out
					.println("Could not close client socket: clientSocket is null");
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
	
		
	/**
	 * Broadcast a message for a specific port on the subnetwork of a given ip
	 * address
	 *  @param ipAddress given ip address
	 */
	public void getRemoteIpAvailableForChat(String ipAddress)
	{	
		Thread thread =  new Thread(() ->
		{
			//Get the subnet (/24 subnets only)
			String[] ipSplit = ipAddress.split("\\.");
			String subnetworkPartOfIpAddress = ipSplit[0] + "." + 
					ipSplit[1] + "." + ipSplit[2] + ".";
			
			//Create the broadcast address
			String broadcastAddress = subnetworkPartOfIpAddress + "255";
			InetAddress brodcastInetAddress = null;
			try
			{
				brodcastInetAddress = InetAddress.getByName(broadcastAddress);
			} 
			catch (UnknownHostException e)
			{
				System.out.println("Client could not create broadcast address");
				e.printStackTrace();
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
			
			//Wait for a response from a server
			byte[] bufferResponse = new byte[1];
			DatagramPacket responsePacket = new DatagramPacket(bufferResponse, bufferResponse.length);
			try
			{
				UDPsocket.receive(responsePacket);
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
			//Get the ip address of the responding machine
			String remoteMachineIpAddress = responsePacket.getAddress().getHostAddress();
			
			//Add the remote machine ip address to the available for chat list
			ChatModel.getInstance().getAvailableForChatIpAddressList().add(remoteMachineIpAddress);
			System.out.println("Chat available with: " + remoteMachineIpAddress);
		});
		thread.start();
	}
}
