package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        //HttpServer server = new HttpServer(8080, sampleRequestHandler());
        HttpServer server = new HttpServer(8080, Executors.newFixedThreadPool(10), sampleRequestHandler());

        Thread serverThread = new Thread(server);
        serverThread.start();
    }

    // We can mock this to be CPU intensive if we wish :)
    static Consumer<Socket> sampleRequestHandler() {
        return request -> {
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
