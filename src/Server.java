import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * Created by Andreas on 2017-04-04.
 */
public class Server {

    public static ArrayList<Socket> connections = new ArrayList<Socket>();
    public static ArrayList<String> currentUsers = new ArrayList<String>();
    private static Iterator<Map.Entry<String, String>> entryIter;
    private static Map.Entry<String, String> currentEntry;
    private static Map<String, String> qaMap = new HashMap<String, String>();

    public static void main(String[] args) throws IOException {

        try {
            final int PORT = 5565;
            ServerSocket server = new ServerSocket(PORT);
            System.out.println("Server online on port: " + PORT);
            new sendQuestionThread().start();

            while (true) {
                Socket socket = server.accept();
                connections.add(socket);

                System.out.println("Client connected from: " + socket.getLocalAddress().getHostName());

                addUser(socket);

                ServerReturn chat = new ServerReturn(socket);
                Thread serverThread = new Thread(chat);
                serverThread.start();
            }
        } catch (Exception exception) {
            System.out.println(exception);
        }
    }

    // Add new user and send information too all other users
    public static void addUser(Socket socket) throws IOException {
        Scanner input = new Scanner(socket.getInputStream());
        String userName = input.nextLine();
        currentUsers.add(userName);
        System.out.println(currentUsers +"3");

        // Send out usernames to all users
        for (int i = 1; i <= Server.connections.size(); i++) {
            Socket tempSocket = (Socket) Server.connections.get(i-1);
            PrintWriter output = new PrintWriter(tempSocket.getOutputStream());
            output.println("#USERNAME" + currentUsers);
            output.println(userName + " entered");
            System.out.println(currentUsers);
            output.flush();
        }
    }

    // Method for sending messages
    public static void sendMessage(String message) throws IOException {
        for (int i = 1; i <= Server.connections.size(); i++) {
            Socket tempSocket = (Socket) Server.connections.get(i - 1);
            PrintWriter tempOutput = new PrintWriter(tempSocket.getOutputStream());
            tempOutput.println(message); // Question to client
            tempOutput.flush();
        }
    }

    // Class for sending questions to users
    public static class sendQuestionThread extends Thread {
        public void run() {

            // Initialize data
            loadQuestions();

            // Send questions
            System.out.println("Quiz server active - sending out questions");
            while (true) {

                try {
                    generateQuestion();

                    // Print to server
                    System.out.print(currentEntry.getKey() + "\n");

                    // Send question to clients
                    sendMessage(currentEntry.getKey() + currentEntry.getValue() + "\n");

                    // Time to answer question
                    Thread.sleep(30000);

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Load questions and answers from textfile "qa.txt
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

    // Loading question via iterator and hashmap as Key/Value pair
    private static String generateQuestion() {
        if (entryIter == null || !entryIter.hasNext()) {
            entryIter = qaMap.entrySet().iterator();
        }
        currentEntry = entryIter.next();
        return currentEntry.getKey();
    }

}
