package org.icpclive.datapassing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Aksenov239 on 21.11.2015.
 */
public class DataLoader {
    private static final Logger log = LogManager.getLogger(DataLoader.class);

    public static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(TeamData.class, new TeamData.TeamDataDeserializer())
            .registerTypeAdapter(TeamData.class, new TeamData.TeamDataSerializer())
            .registerTypeAdapter(PollData.class, new PollData.PollDataDeserializer())
            .registerTypeAdapter(PollData.class, new PollData.PollDataSerializer())
            .create();

    private AtomicReference<Data> data;

    public DataLoader() {
    }

    private static ServerSocket serverSocket;
    private static List<PrintWriter> openPW;
    private static List<BufferedReader> openBR;
    private static List<Integer> tries;
    private static int soTimeout;

    public static synchronized void free() {
        for (PrintWriter pw : openPW) {
            pw.close();
        }
        for (BufferedReader br : openBR) {
            try {
                br.close();
            } catch (IOException e) {
            }
        }
        if (serverSocket != null) {
            try {
                serverSocket.close();
                serverSocket = null;
                log.info("Socket is closed!");
            } catch (IOException e) {
                log.error("error", e);
            }
        }
    }

    public static synchronized void iterateFrontend() {
        try {
            if (serverSocket == null || serverSocket.isClosed()) {
                Properties properties = new Properties();
                try {
                    properties.load(DataLoader.class.getResourceAsStream("/mainscreen.properties"));
                } catch (IOException e) {
                    log.error("error", e);
                }
                int port = Integer.parseInt(properties.getProperty("data.port"));
                try {
                    serverSocket = new ServerSocket(port);
                    serverSocket.setReuseAddress(true);
                } catch (Exception e) {
                    log.error("error", e);
                }
                openPW = new ArrayList<>();
                openBR = new ArrayList<>();
                tries = new ArrayList<>();
                soTimeout = Integer.parseInt(properties.getProperty("data.sotimeout", "200"));
            }
            try {
                serverSocket.setSoTimeout(soTimeout);
                Socket newSocket = serverSocket.accept();
//                PrintWriter pw = new PrintWriter(newSocket.getOutputStream());
//                pw.println(getDataFrontend());
                openPW.add(new PrintWriter(new OutputStreamWriter(newSocket.getOutputStream(), StandardCharsets.UTF_8)));
                openBR.add(new BufferedReader(new InputStreamReader(newSocket.getInputStream(), StandardCharsets.UTF_8)));
                tries.add(0);
                log.info("Accepted socket");
            } catch (Exception e) {
//                log.error("Failed socket", e);
            }
            List<PrintWriter> newOpenPW = new ArrayList<>();
            List<BufferedReader> newOpenBR = new ArrayList<>();
            List<Integer> newTries = new ArrayList<>();
            for (int i = 0; i < openPW.size(); i++) {
                PrintWriter pw = openPW.get(i);
                BufferedReader br = openBR.get(i);
                int ntries = tries.get(i);
                boolean send = false;
                //log.info("Start waiting");
                try {
                    while (br.ready()) {
                        br.readLine();
                        //log.info("Ready to send");
                        send = true;
                    }
                } catch (IOException e) {
                    log.info("Client socket is closed");
                    continue;
                }
                if (send) {
                    String data = getDataFrontend();
//                    log.info(data);
                    pw.println(getDataFrontend());
                    pw.flush();
                    ntries = 0;
                    if (pw.checkError()) {
                        log.info("Client socket is closed");
                        pw.close();
                        br.close();
                        continue;
                    }
                } else {
                    ntries++;
                    if (ntries == 5) {
                        log.info("Client socket is closed");
                        pw.close();
                        br.close();
                        continue;
                    }
                }
                newOpenPW.add(pw);
                newOpenBR.add(br);
                newTries.add(ntries);
            }
            openPW = newOpenPW;
            openBR = newOpenBR;
            tries = newTries;
        } catch (Throwable e) {
            log.error("error", e);
        }
    }

    public void backendInitialize() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("/mainscreen.properties"));
        } catch (IOException e) {
            log.error("error", e);
        }
        String host = properties.getProperty("data.host");
        int port = Integer.parseInt(properties.getProperty("data.port"));
        //update = Long.parseLong(properties.getProperty("data.update"));
        System.err.println(host + ":" + port);
        data = new AtomicReference<>();

        //data.set(load(link));

//        new Timer().schedule(new TimerTask() {
        new Thread(() -> {
            while (true) {
                try (
                        Socket socket = new Socket(host, port);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                        PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))
                ) {
                    while (!socket.isClosed())

                    {
                        writer.println("ready");
                        writer.flush();
                        if (writer.checkError()) {
                            log.info("Socket closed");
                            break;
                        }

//                    log.info("Trying to read");
                        String line = reader.readLine();
//                    System.out.println(line);
                        Data newData = gson.fromJson(line, Data.class);
                        if (newData != null)
                            data.set(newData);
                    }
                } catch (Exception e) {
                    log.error("error", e);
                }
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                    log.error("error", e);
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
