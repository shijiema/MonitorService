package test.ma;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import a.ma.AMAMonitorService;
import a.ma.ServerCommand;

public class ServiceMonitorListServiceIT {
	// monitor service's port
	final String servicePort = "7777";
	AMAMonitorService mainService = null;

	Socket client = null;
	String[] args1 = new String[1];
	String[] args2 = new String[1];
	PrintWriter out = null;
	BufferedReader in = null;


	private void startMonitorService() {
		Runnable aRunnable = new Runnable(){

		     public void run(){
		 		mainService = new AMAMonitorService();
				mainService.prepareService();
				mainService.startService(args1);
		     }
		   };


		   Thread thread = new Thread(aRunnable);
		   thread.start();

	}


	private void shutdown() {


	}

	@Before
	public void setUp() throws Exception {
		args1[0] = servicePort;
		startMonitorService();
		try {
			client = new Socket("localhost", Integer.parseInt(servicePort));
			out = new PrintWriter(client.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(
					client.getInputStream()));
		} catch (UnknownHostException e) {
			System.out.println("Unknown host");
			System.exit(1);
		} catch (IOException e) {
			System.out.println("No I/O");
			System.exit(1);
		}

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testListService() {
		String response = null;
		
		try {
			//discard service list content
			while(!".end.".equals(response)){
				response = in.readLine();
			}
			out.println(ServerCommand.LIST_SERVICE);
			out.println(".end.");
			response = in.readLine();
			assertTrue("GRWeb;demonWareTelnet;demonwareWeb;gWeb;monitorService;".equals(response));
			// read the .end. terminator
			response = in.readLine();
		} catch (Exception e) {
			fail("Exception happened.");
		}

	}


}
