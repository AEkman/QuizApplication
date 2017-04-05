import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by Andreas on 2017-04-04.
 */
public class Server {

    public static ArrayList<Socket> connections = new ArrayList<Socket>();
    public static ArrayList<String> currentUsers = new ArrayList<String>();

    public static void main(String[] args) throws IOException {

        try {
            final int PORT = 5565;
            ServerSocket server = new ServerSocket(PORT);
            System.out.println("Server online");

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

    public static void addUser(Socket socket) throws IOException {
        Scanner input = new Scanner(socket.getInputStream());
        String userName = input.nextLine();
        currentUsers.add(userName);

        for (int i = 1; i <= Server.connections.size(); i++) {
            Socket tempSocket = (Socket) Server.connections.get(i-1);
            PrintWriter output = new PrintWriter(tempSocket.getOutputStream());
            output.println("#USERNAME" + currentUsers);
            output.flush();
        }
    }

}
