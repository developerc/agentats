package ru.rtk.tih;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
public class HttpResponse implements Runnable, HttpHandler{

	@Override
	public void handle(HttpExchange exc) throws IOException {
		// TODO Auto-generated method stub
		exc.sendResponseHeaders(200, 0);
	    PrintWriter out = new PrintWriter(exc.getResponseBody());
	    out.println(DerbyConnect.alarmState);
	    out.close();
	    exc.close();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		HttpServer server;
		try {
			server = HttpServer.create(new InetSocketAddress(8000), 10);
		
	    server.createContext("/", new HttpResponse());
	    server.setExecutor(null);
	    server.start();
	    System.out.println("Server started\nPress any key to stop...");
	    System.in.read();
	    server.stop(0);
	    System.out.println("Server stoped");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("Exception:" + e);
		}
	}

}
