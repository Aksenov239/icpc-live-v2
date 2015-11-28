package ru.ifmo.acm.datapassing;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Aksenov239 on 21.11.2015.
 */
public class DataLoader {
    public static Gson gson = new Gson();

    private AtomicReference<Data> data;

    public DataLoader() {
    }

    private static ServerSocket serverSocket;
    private static List<PrintWriter> openSockets;
    private static int soTimeout;

    public static synchronized void free(){
        for (PrintWriter pw :openSockets) {
            pw.close();
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void iterateFrontend() {
        try {
            if (serverSocket == null || serverSocket.isClosed()) {
                Properties properties = new Properties();
                try {
                    properties.load(DataLoader.class.getResourceAsStream("/mainscreen.properties"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int port = Integer.parseInt(properties.getProperty("data.port"));
                try {
                    serverSocket = new ServerSocket(port);
                    serverSocket.setReuseAddress(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                openSockets = new ArrayList<>();
                soTimeout = Integer.parseInt(properties.getProperty("data.sotimeout", "200"));
            }
            try {
                serverSocket.setSoTimeout(soTimeout);
                Socket newSocket = serverSocket.accept();
                openSockets.add(new PrintWriter(newSocket.getOutputStream()));
                System.err.println("Accepted socket");
            } catch (Exception e) {
            }
            List<PrintWriter> newOpenSockets = new ArrayList<>();
            for (PrintWriter pw : openSockets) {
                String data = getDataFrontend();
                System.err.println(data);
                pw.println(getDataFrontend());
                pw.flush();
                if (pw.checkError()) {
                    System.err.println("Client socket is closed");
                    continue;
                }
                newOpenSockets.add(pw);
            }
            openSockets = newOpenSockets;
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void backendInitialize() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("/mainscreen.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String host = properties.getProperty("data.host");
        int port = Integer.parseInt(properties.getProperty("data.port"));
        //update = Long.parseLong(properties.getProperty("data.update"));

        data = new AtomicReference<>();

        //data.set(load(link));

//        new Timer().schedule(new TimerTask() {
        new Thread(() -> {
            while (true) {
                try (Socket socket = new Socket(host, port)) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    while (!socket.isClosed()) {
//                    System.err.println("Trying to read");
                        String line = reader.readLine();
//                    System.out.println(line);
                        Data newData = gson.fromJson(line, Data.class);
                        if (newData != null)
                            data.set(newData);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static String getDataFrontend() {
        return gson.toJson(new Data().initialize());
    }

    public Data getDataBackend() {
        return data.get();
    }

}
