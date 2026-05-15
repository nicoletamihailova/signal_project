package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

/**
 * Sends simulated patient data to a connected TCP client.
 * A TCP server is started on the specified port, and once a client connects,
 * data is streamed to it.
 *
 * <p>Each message is sent as comma-separated values:
 * patient ID, timestamp, label, and data.
 */
public class TcpOutputStrategy implements OutputStrategy {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;

    /**
     * Creates a TCP output strategy and starts a server on the given port.
     * The client connection is handled on a separate thread so the simulator
     * can keep running while waiting for a connection.
     *
     * @param port the TCP port used to listen for clients
     */
    public TcpOutputStrategy(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("TCP Server started on port " + port);

            // Accept clients in a new thread to not block the main thread
            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    clientSocket = serverSocket.accept();
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    System.out.println("Client connected: " + clientSocket.getInetAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a single patient data entry to the connected TCP client.
     * If no client is connected, nothing is sent.
     *
     * @param patientId the patient’s unique ID
     * @param timestamp when the data was generated (ms since epoch)
     * @param label     the data type or category
     * @param data      the generated value or message
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        if (out != null) {
            String message = String.format("%d,%d,%s,%s", patientId, timestamp, label, data);
            out.println(message);
        }
    }
}
