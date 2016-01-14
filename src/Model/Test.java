package Model;

public class Test
{

	public static void main(String[] args)
	{
		
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
		
		
		Client client = new Client();
		client.openClientSocket("172.18.10.24", 55555);
		
		client.sendMessage("Coucou me voilà!");
	
		
		try
		{
			Thread.sleep(5000);
		} catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		client.sendMessage("Me revoila");
		

	}

}
