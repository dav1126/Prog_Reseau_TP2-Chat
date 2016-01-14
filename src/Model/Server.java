package Model;

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
	 * @return message that came through the channel
	 */
	private String receiveMessage()
	{
		String msgInput = null;
		if(serverSocket != null && clientSocket != null)
		{
			try
			{
				InputStream bIStream = clientSocket.getInputStream();
				
				byte[] byteBuffer = new byte[8000];
				msgInput = "";
				bIStream.read(byteBuffer);
				{
					for (byte b: byteBuffer)
					{
						msgInput += (char)b;
						System.out.print((char)b);
					}
				}
				
				System.out.println("Received Message" + msgInput);
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
	
	/**
	 * Closes the server's sockets
	 */
	public synchronized void closeServerSockets()
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
