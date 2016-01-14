package Model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client
{
	Socket clientSocket;
	
	public void openClientSocket(String remoteIpAddress, int remotePort)
	{
		InetAddress adress = null;
		try
		{
			adress = InetAddress.getByName(remoteIpAddress);
		} 
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
		
		clientSocket = null;	
		try
		{
			clientSocket =  new Socket(adress, remotePort);
		} 
		catch (IOException e)
		{
			System.out.println("Cannot open client socket");
			e.printStackTrace();
		}
	}
	
	public void sendMessage(String message)
	{
		if (clientSocket != null)
		{
			try
			{
				OutputStream output = clientSocket.getOutputStream();
				output.write(message.getBytes());	
			} 
			catch (IOException e)
			{	
				System.out.println("Message could not be sent");
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("Could not send message: clientSocket is null");
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
		}
		else
		{
			System.out.println("Could not close client socket: clientSocket is null");
		}
	}
}
