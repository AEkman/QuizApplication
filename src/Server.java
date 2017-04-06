import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {

    private static Map<String, Integer> names = new HashMap<String, Integer>();
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();
    private static Iterator<Map.Entry<String, String>> entryIter;
    private static Map.Entry<String, String> currentEntry;
    private static Map<String, String> qaMap = new HashMap<String, String>();
    private static final int PORT = 5565;

    public static void main(String[] args) throws IOException {
        System.out.println("Quiz server started");

        // Sending out random questions
        new sendQuestionThread().start();
        ServerSocket listener = new ServerSocket(PORT);

        try {
            while (true) {
                // Engage thread handling
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }

    // Send message method loops through sockets and send to everyone that is connected
    private static void sendMessage(String message) {
        for(PrintWriter writer: writers) {
            writer.println(message);
            writer.flush();
        }
    }

    // Get current online users and send info to all users online
    public static void getOnlineUsers() {
        try {
            // Clear old list
            sendMessage("#CLEAR");
            for(String users:names.keySet()) {
                sendMessage("#USERNAME" + users + " " + names.get(users));
            }
        } catch (Exception exception) {
            System.out.println("getOnlineUsers failed");
        }
    }

    //todo - get the size of names Map
    public static int getOnlineUserNumber() {
        System.out.println(names.size());
        return names.size();
    }

    // Get current online users + score and send to everyone listening
    public static void getScore() {
        try {
            for(String users:names.keySet()) {
                sendMessage(users + " has " + names.get(users) + " points!");
            }
        } catch (Exception exception) {
            System.out.println("getScore failed");
        }
    }

    // Class for handling threads
    private static class Handler extends Thread {
        private String name;
        private Socket socket;
        private BufferedReader input;
        private PrintWriter output;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                // Create character streams for the socket.
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);

                // Check username
                while (true) {
                    name = input.readLine();
                    if (name == null) {
                        return;
                    }
                    synchronized (names) {
                        if (!names.containsKey(name)) {
                            names.put(name, 0);
                            break;
                        }
                    }
                }

                // Tell everyone who joined
                sendMessage(name + " has joined the server");

                // Add printWriter to list
                writers.add(output);

                // Update current online users
                getOnlineUsers();

                // Accept messages from client and broadcast them.
                while (true) {
                    receive();
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                // Closing client removes name, writer and close socket
                if (name != null) {
                    names.remove(name);
                }
                if (output != null) {
                    writers.remove(output);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.print(e);
                }
            }
        }

        // Recieve messages from clients
        private void receive() throws IOException {
            String message = input.readLine();
            if (message == null) {
                return;
            } else if (message != null && message.equalsIgnoreCase(currentEntry.getValue())) {
                // Award player
                sendMessage(name + " had the correct answer! 1 Point awarded!");
                names.put(name, names.get(name) + 1);
                // Unable to get more points
                currentEntry.setValue("waiting for next question");
                // Update score
                getOnlineUsers();
            } else if (message.contains("/SCORE")) {
                getScore();
                getOnlineUsers();
            } else if (message.contains("/QUIT")) {
                getOnlineUsers();
            }

            // Send to all clients
            for (PrintWriter writer : writers) {
                writer.println(message);
            }
        }
    }

    // Class for sending questions to users
    public static class sendQuestionThread extends Thread {
        public void run() {

            // Initialize data from textfile to hashmap
            loadQuestions();

            // Send questions
            System.out.println("Quiz server active - sending out questions");
            while (true) {
                try {
                    generateQuestion();

                    // Print to server
                    System.out.print(currentEntry.getKey() + "\n");

                    // Send question to clients
                    sendMessage(currentEntry.getKey() + "\n");

                    // Time to answer question "30 seconds"
                    Thread.sleep(30000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Load questions and answers from textfile "qa.txt"
    private static void loadQuestions() {
        try {
            InputStream is = ClassLoader.getSystemResourceAsStream("qa.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String key;
            while ((key = br.readLine()) != null) {
                qaMap.put(key, br.readLine());
            }
        } catch (FileNotFoundException e) {
            System.out.print("File not found!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Loading random question via iterator and hashmap as Key/Value pair
    private static String generateQuestion() {
        if (entryIter == null || !entryIter.hasNext()) {
            entryIter = qaMap.entrySet().iterator();
        }
        currentEntry = entryIter.next();
        return currentEntry.getKey();
    }
}
