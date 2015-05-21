package test.ma;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import a.ma.AMAMonitorService;
import a.ma.ServerCommand;

public class ServiceMonitorUpDownIT {
	Process process=null;
	// monitor service's port
	final String servicePort = "7777";
	// server under monitoring that will go up and down
	final String monitoredServicePort = "7779";
	AMAMonitorService mainService = null;
	AMAMonitorService monitoredService = null;
	Socket client = null;
	String[] args1 = new String[1];
	String[] args2 = new String[1];
	PrintWriter out = null;
	BufferedReader in = null;

	private void stopMonitoredService() {
		String response = null;
		try {
			Socket client = new Socket("localhost",
					Integer.parseInt(monitoredServicePort));
			out = new PrintWriter(client.getOutputStream(), true);

			out.println(ServerCommand.SHUTDOWN.toString());
			out.println(".end.");
		} catch (UnknownHostException e) {
			System.out.println("Unknown host");
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("No I/O");
			System.exit(1);
		}
	}

	private void startMonitorService() {
		Runnable aRunnable = new Runnable() {

			public void run() {
				mainService = new AMAMonitorService();
				mainService.prepareService();
				mainService.startService(args1);
			}
		};

		Thread thread = new Thread(aRunnable);
		thread.start();

	}

	private void startMonitoredService() {
		if (process!=null)
			process.destroy();
		Runnable aRunnable = new Runnable() {

			public void run() {

				URL location = ServiceMonitorUpDownIT.class
						.getProtectionDomain().getCodeSource().getLocation();
				List<String> command = new ArrayList<String>();
				command.add(location.getFile().substring(1)
						+ "../misc/startMonitoredService.bat");

				ProcessBuilder builder = new ProcessBuilder(command);
				Map<String, String> environ = builder.environment();
				builder.directory(new File(location.getFile()));

				
				try {
					process = builder.start();
					System.out.println("process is started");
				/*	InputStream is = process.getInputStream();
					InputStreamReader isr = new InputStreamReader(is);
					BufferedReader br = new BufferedReader(isr);
					String line;
					while ((line = br.readLine()) != null) {
						System.out.println(line);
					}
					*/
				} catch (IOException e) {
					e.printStackTrace();
				}
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
		args2[0] = monitoredServicePort;
		startMonitoredService();
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
			e.printStackTrace();
			System.out.println("No I/O");
			System.exit(1);
		}

	}

	@After
	public void tearDown()  {
	
	}

	@Test
	public void testRestartInBetweenPollingAndGracePeriod() {
		String response1 = null;
		String response2 = null;

		try {
			// discard service list content
			while (!".end.".equals(response1)) {
				response1 = in.readLine();
			}
			out.println(ServerCommand.SUBSCRIBE_SERVICE);
			out.println("monitorService");
			out.println(".end.");
			// this should UP
			response1 = in.readLine();
			response1 = response1.split(":")[4];
			// stop the monitoered service.
			// TODO:it should be in another jvm
			stopMonitoredService();
			// start it again
			startMonitoredService();

			// read the .end. terminator
			response2 = in.readLine();
			// parse last piece of string, they should be same
			response2 = response2.split(":")[4];
			assertTrue(response2.equals(response1));
		} catch (Exception e) {
			fail("Exception happened.");
		}

	}

}
