package Model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server
{
	Socket clientSocket = null;
	ServerSocket serverSocket = null;
	
	public void openServerSockets(int serverPort)
	{
		try
		{
			serverSocket = new ServerSocket(serverPort);
			clientSocket = serverSocket.accept();
	
		} 
		catch (IOException e)
		{
			System.out.println("Could not open server's sockets");
			e.printStackTrace();
		}
	}
	
	public void receiveMessage()
	{
		if(serverSocket != null && clientSocket != null)
		{
			try
			{
				InputStream bIStream = clientSocket.getInputStream();
				
				byte[] byteBuffer = new byte[8000];
				String msgInput = "";
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
	}
	
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
}
