package Model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;

public class Server
{
	DatagramSocket UDPSocket;
	Socket clientSocket = null;
	ServerSocket serverSocket = null;
	boolean connectionEstablished = false;
	private static final int MAX_TRANSMISSION_BYTE_SIZE = 10000000;
	private static final String FILE_TRANSMISSION_ALERT_MSG = 
			"NHRTYFHAPWLM*?DYXN!848145489WJD23243212owahAwfligLOP)(* ALPHA";
	private static final String PATH_TO_FILE_DIRECTORY = "C:\\temp\\destination\\";
	private static final int UDP_SOCKET_NUMBER = 5556;
	private static final int SERVER_SOCKET_NUMBER = 5555;
	
	/**
	 * Opens the server's sockets and establish a wait to establish a remote
	 * connection with a client
	 */
	private void openServerSockets()
	{
		try
		{
			serverSocket = new ServerSocket(SERVER_SOCKET_NUMBER);
			clientSocket = serverSocket.accept();
			connectionEstablished = true;
		} 
		catch (IOException e)
		{
			System.out.println("Could not open server's sockets");
			e.printStackTrace();
		}
	}
	
	/**
	 * Wait for a message to come through the channel, then returns it.
	 * It is synchronized because it if the message received is the file 
	 * transmission code, it must wait for a second message to come through the 
	 * channel (the file) before to give access to another thread that would 
	 * wait for a normal message.
	 * @return message that came through the channel
	 */
	private synchronized String receiveMessage()
	{
		String msgInput = null;
		if(serverSocket != null && clientSocket != null)
		{
			try
			{
				InputStream bIStream = clientSocket.getInputStream();
				
				byte[] byteBuffer = new byte[MAX_TRANSMISSION_BYTE_SIZE];
				int count = bIStream.read(byteBuffer);
				msgInput = "";
				
				
				for (int i = 0; i < count ; i++)
					{
						msgInput += (char)byteBuffer[i];
						//System.out.println((char)byteBuffer[i]);
						//System.out.println(msgInput);
					}
				
				System.out.println("Received Message" + msgInput);
				
				//If the received message is the file transmission alert message
				if (msgInput.compareTo(FILE_TRANSMISSION_ALERT_MSG) == 0)
				{
					//Confirm the reception of the file transmission alert t
					//the client
					OutputStream bOStream = clientSocket.getOutputStream();
					String msg = "File transmission alert received. Waiting for file name...";
					bOStream.write(msg.getBytes());
					bOStream.flush();
					
					//Receive the file
					startReceiveFile();
				}
			}
			catch (IOException e)
			{
				System.out.println("Could not receive message.");
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("Could not receive message: server's sockets are null");
		}
		return msgInput;
	}
	
	private void startReceiveFile()
	{
		Thread thread =  new Thread(() ->
		{
			//Wait for the sockets to be openend
			while (serverSocket == null || clientSocket == null)
			{
				try
				{
					Thread.sleep(10);
				} 
				catch (Exception e)
				{
					System.out.println("Server receive file thread sleep error");
					e.printStackTrace();
				}
			}
				String fileName = receiveFileName();
				receiveFile(fileName);
		});
		thread.start();
	}
	
	/**
	 * Receive a file name from the client, the send a confirmation to the client
	 * @return the name of the file
	 */
	public String receiveFileName()
	{
		String fileName = null;
		
		try
		{
			InputStream bIStream = clientSocket.getInputStream();
			
			byte[] byteBuffer = new byte[MAX_TRANSMISSION_BYTE_SIZE];
			int count = bIStream.read(byteBuffer);
			fileName = "";
			
			
			for (int i = 0; i < count ; i++)
			{
				fileName += (char)byteBuffer[i];
			}
			
			//Confirm the reception of the file name to the client
			OutputStream bOStream = clientSocket.getOutputStream();
			String msg = "File name received. Waiting for file...";
			bOStream.write(msg.getBytes());
			bOStream.flush();
		}
			
		catch (IOException e)
		{
			System.out.println("Could not receive file.");
			e.printStackTrace();
		}
		
		return fileName;
	}
	
	/**
	 * Receive a file from the client, the send a confirmation to the client
	 */
	public void receiveFile(String fileName)
	{
		FileOutputStream fos = null;
		try
		{
			//Get the file
			InputStream bIStream = clientSocket.getInputStream();
			File file = new File(PATH_TO_FILE_DIRECTORY, fileName);
			file.createNewFile();
			file.mkdirs();
			fos = new FileOutputStream(file);
			
			
			byte[] byteBuffer = new byte[MAX_TRANSMISSION_BYTE_SIZE];
			int count = bIStream.read(byteBuffer);
			
			for (int i = 0; i < count ; i++)
				{
					fos.write(byteBuffer[i]);
				}
			
			System.out.println("File received: " + file.getName());
			
			//Confirm the reception of the file to the client
			OutputStream bOStream = clientSocket.getOutputStream();
			String msg = "File transmission successful!";
			bOStream.write(msg.getBytes());
			bOStream.flush();
		}
		catch (IOException e)
		{
			System.out.println("Could not receive file.");
			e.printStackTrace();
		}
		finally
		{
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Opens a UDP socket that is used to wait for broadcast messages.
	 */
	public void openUDPSocket()
	{	
		Thread thread =  new Thread(() ->
		{
			try
			{
				UDPSocket = new DatagramSocket(UDP_SOCKET_NUMBER);
				UDPSocket.setBroadcast(true);
			} 
			catch (SocketException e)
			{
				System.out.println("UDP socket " + UDP_SOCKET_NUMBER +" could not be openend." );
				e.printStackTrace();
			}
		});
		thread.start();
	}
	
	/**
	 * Receive broadcast messages from remote clients.
	 */
	public void receiveBroadcastRequest()
	{
		
		Thread thread =  new Thread(() ->
		{
			//Wait for the UDPSocket to be opened
			while (UDPSocket == null)
			{
				try
				{
					Thread.sleep(10);
				} catch (Exception e)
				{
					System.out.println("Receive broadcast thread sleep error");
					e.printStackTrace();
				}
			}
			//Buffer to receive the broadcast request
			byte[] buffer = new byte[1];
			DatagramPacket remoteHostPacket = new DatagramPacket(buffer, buffer.length);
			try
			{
				UDPSocket.receive(remoteHostPacket);
				String remoteIpAddress = remoteHostPacket.getAddress().getHostAddress();
				System.out.println( "BroadCast request from: " + remoteIpAddress);
				//Send a response to the remote client
				byte[] bufferResponse = new byte[1];
				DatagramPacket responsePacket = new DatagramPacket
						(bufferResponse, bufferResponse.length, 
								remoteHostPacket.getAddress(), 
								remoteHostPacket.getPort());
				UDPSocket.send(responsePacket);
				System.out.println( "BroadCast answer sent to: " + remoteIpAddress);
			} 
			catch (IOException e)
			{
				System.out.println("UDP Packet reception failed");
				e.printStackTrace();
			}
		});
		
		thread.start();
	}
	
	/**
	 * Closes the server's sockets
	 */
	public void closeServerSockets()
	{
		if(serverSocket != null && clientSocket != null)
		{
			try
			{
				clientSocket.close();
				serverSocket.close();
				UDPSocket.close();
			}
			catch (IOException e)
			{
				System.out.println("Could not close server sockets");
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("Could not close message: server's sockets are null");
		}	
	}
	
	/**
	 * Creates and starts a thread that opens the server's sockets and wait
	 * for a message to come trough the channel
	 */
	public void startOpenSocketThread()
	{
		Thread thread =  new Thread(() ->
		{
			openServerSockets();
		});
		thread.start();
	}
	
	/**
	 * Creates and start a thread that wait for a message to come through the 
	 *  opened channel. 
	 */
	public void startReceiveMessageThread()
	{
		Thread thread =  new Thread(() ->
		{
			receiveMessage();
		});
		thread.start();
	}
}
