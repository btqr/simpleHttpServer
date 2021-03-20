package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import static server.HttpServer.RequestHandlingStrategy.WITHOUT_EXECUTOR_SERVICE;
import static server.HttpServer.RequestHandlingStrategy.WITH_EXECUTOR_SERVICE;

public class HttpServer implements Runnable {

    enum RequestHandlingStrategy {
        WITH_EXECUTOR_SERVICE,
        WITHOUT_EXECUTOR_SERVICE
    }

    private final ServerSocket serverSocket;
    private final Consumer<Socket> requestHandler;
    private final RequestHandlingStrategy strategy;

    private ExecutorService executorService;
    private boolean toBeTerminated = false;

    /**
     * HTTP Server not using executor service, each request is handled by new, separate thread
     * @param port port of http server
     * @param requestHandler function handling incoming requests
     * @throws IOException exception if server socket cannot be created on given port, e.x. port is used by the system
     */
    public HttpServer(int port, Consumer<Socket> requestHandler) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.requestHandler = requestHandler;
        this.strategy = WITHOUT_EXECUTOR_SERVICE;
    }

    /**
     * HTTP Server using executor service, each request is submitted to the executor service
     * @param port port of http server
     * @param executorService executor service responsible for scheduling how to handle requests
     * @param requestHandler function handling incoming requests
     * @throws IOException exception if server socket cannot be created on given port, e.x. port is used by the system
     */
    public HttpServer(int port, ExecutorService executorService, Consumer<Socket> requestHandler) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.requestHandler = requestHandler;
        this.executorService = executorService;
        this.strategy = WITH_EXECUTOR_SERVICE;
    }

    @Override
    public void run() {
        try {
            runServer();
        } catch (InterruptedException | IOException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public void terminate() {
        toBeTerminated = true;
    }

    private void runServer() throws InterruptedException, IOException {
        List<Thread> activeThreads = new ArrayList<>();

        while (!toBeTerminated) {
            Socket request = serverSocket.accept();
            if (request != null) {
                Runnable handlingAction = () -> requestHandler.accept(request);
                switch (strategy) {
                    case WITHOUT_EXECUTOR_SERVICE:
                        Thread thread = new Thread(handlingAction);
                        thread.start();
                        activeThreads.add(thread);
                        break;
                    case WITH_EXECUTOR_SERVICE:
                        executorService.submit(handlingAction);
                        break;
                    default:
                        throw new IllegalArgumentException("Strategy " + strategy + " handling not implemented!");
                }
            }
        }

        for (Thread activeThread : activeThreads) {
            activeThread.join();
        }
    }
}


