package Model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Model
{
	Socket socket;
	
	public static void main(String[] args)
	{
		
	}
	
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
		
		socket = null;	
		try
		{
			socket =  new Socket(adress, remotePort);
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void sendMessage(String message)
	{
		if (socket != null)
		{
			try
			{
				OutputStream output = socket.getOutputStream();
				output.write(new String("Mon premier message client").getBytes());	
			} 
			catch (IOException e)
			{
				
				System.out.println("Output failure");
			}
		}		
	}
}
