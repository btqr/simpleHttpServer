package com.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.Executors;

import java.util.function.Consumer;

public class Main {
  
    private static final String LOAD = "99999999";
    
    public static void main(String[] args) throws IOException, InterruptedException {
//        HttpServer server = new HttpServer(8080, sampleRequestHandler());
        HttpServer server = new HttpServer(8080, Executors.newFixedThreadPool(10), sampleRequestHandler());

        Thread serverThread = new Thread(server);
        serverThread.start();

        makeRequests(200);
    }

    private static void makeRequests(int numberOfRequests) throws IOException {

        URL url = new URL("http://localhost:8080");
        int i = 0;
        while (i++<numberOfRequests) {

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            con.connect();
        }

    }

    // We can mock this to be CPU intensive if we wish :)
    static Consumer<Socket> sampleRequestHandler() {
        return request -> {

            for (long i = 0; i <  Long.parseLong(LOAD);) { i++; }

            try (PrintWriter out = new PrintWriter(request.getOutputStream())) {
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: text/html");
                out.println("\r\n");
                out.println("<p> Hello world </p>");
                out.flush();
                out.close();
                request.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        };
    }

}