package Model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;

public class Server
{
	Socket clientSocket = null;
	ServerSocket serverSocket = null;
	boolean connectionEstablished = false;
	private static final int MAX_TRANSMISSION_BYTE_SIZE = 10000000;
	private static final String FILE_TRANSMISSION_ALERT_MSG = 
			"NHRTYFHAPWLM*?DYXN!848145489WJD23243212owahAwfligLOP)(* ALPHA";
	private static final String PATH_TO_FILE_DIRECTORY = "C:\\temp\\destination\\";
	
	/**
	 * Opens the server's sockets and establish a wait to establish a remote
	 * connection with a client
	 * @param serverPort
	 */
	private void openServerSockets(int serverPort)
	{
		try
		{
			serverSocket = new ServerSocket(serverPort);
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
				
				
				for (int i = 0; i <= count ; i++)
					{
						msgInput += (char)byteBuffer[i];
						//System.out.println((char)byteBuffer[i]);
						//System.out.println(msgInput);
					}
				
				System.out.println("Received Message" + msgInput);
				
				//If the received message is the file transmission alert message
				if (msgInput.compareTo(FILE_TRANSMISSION_ALERT_MSG) == 0)
				{
					receiveFile();
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
	
	private void receiveFile()
	{
		String fileInput = null;
		if(serverSocket != null && clientSocket != null)
		{
			try
			{
				InputStream bIStream = clientSocket.getInputStream();
				File file = new File(PATH_TO_FILE_DIRECTORY + "tempFileName");
				System.out.println("default file name: " + file.getName());
				FileOutputStream fos = new FileOutputStream(file);
				
				
				byte[] byteBuffer = new byte[MAX_TRANSMISSION_BYTE_SIZE];
				int count = bIStream.read(byteBuffer);
				
				for (int i = 0; i <= count ; i++)
					{
						fos.write(byteBuffer[i]);
					}
				
				System.out.println("File received: " + file.getName());
				
			}
			catch (IOException e)
			{
				System.out.println("Could not receive file.");
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("Could not receive file: server's sockets are null");
		}
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
	 * @param serverPort
	 */
	public void startOpenSocketThread(int serverPort)
	{
		Thread thread =  new Thread(() ->
		{
			openServerSockets(serverPort);
		});
		thread.start();
	}
	
	/**
	 * Creates and start a thread that wait for a message to come through the 
	 *  opened channel. 
	 * @param serverPort
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
