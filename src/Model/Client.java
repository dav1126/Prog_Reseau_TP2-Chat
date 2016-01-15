package Model;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client
{
	Socket clientSocket;
	private static final int MAX_TRANSMISSION_BYTE_SIZE = 1000000;
	
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
	
	public void sendFile(File fileToSend)
	{
		//First, send a message that tells the server that the next transmission
		//is gonna be a file
		try
		{
			OutputStream output = clientSocket.getOutputStream();
			byte[] fileTransmissionCode = new String(
					"THE NEXT TRANSMISSION IS GONNA BE A FILE BE READY YOU "
					+ "STUPID SERVER, HERES THE SUPER COOL FILE TRANSMISSION "
					+ "SECRET CODE: "
					+ "NHRTYFHAPWLM*?DYXN!848145489WJD23243212WDWDAAWD)"
					+ "(* ALPHA").getBytes();
			output.write(fileTransmissionCode);	
		} 
		catch (IOException e)
		{	
			System.out.println("Message could not be sent");
			e.printStackTrace();
		}
		
		
		
		FileInputStream fis = null;
	    BufferedInputStream bis = null;
	    OutputStream os = null;
	    try
	    {
			byte[] fileByteArray = new byte[(int)fileToSend.length()];
			fis = new FileInputStream(fileToSend);
			bis = new BufferedInputStream(fis);
			bis.read(fileByteArray, 0, fileByteArray.length);
			os = clientSocket.getOutputStream();
			os.write(fileByteArray, 0, fileByteArray.length);
			os.flush();
	    }
	    catch (Exception e)
	    {
	    	System.out.println("File could not be sent: File not found or IOException");
	    	e.printStackTrace();
	    }
	    //http://www.rgagnon.com/javadetails/java-0542.html	
		
	}
}
