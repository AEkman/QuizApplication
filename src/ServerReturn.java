import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by Andreas on 2017-04-04.
 */
public class ServerReturn implements Runnable{

    Socket socket;
    private Scanner input;
    private PrintWriter output;
    String message ="";

    public ServerReturn(Socket socket) {
        this.socket = socket;
    }

    public void CheckConnection() throws IOException {
        if (!socket.isConnected()) {
            for(int i = 1; i <= Server.connections.size(); i++) {
                if(Server.connections.get(i) == socket) {
                    Server.connections.remove(i);
                }
            }

            for(int i = 1; i <= Server.connections.size(); i++) {
                Socket tempSocket = (Socket) Server.connections.get(i-1);
                PrintWriter tempOutput = new PrintWriter(tempSocket.getOutputStream());
                tempOutput.println(tempSocket.getLocalAddress().getHostName() + " disconnected!");
                tempOutput.flush();
                // Show disconnect at server
                System.out.println(tempSocket.getLocalAddress().getHostName() + " disconnected!");
            }
        }
    }

    public void run() {
        try {
            try {
                input = new Scanner(socket.getInputStream());
                output = new PrintWriter(socket.getOutputStream());

                while (true) {
                    CheckConnection();

                    if (!input.hasNext()) {
                        return;
                    }

                    message = input.nextLine();

                    System.out.println(message);

                    for (int i = 1; i <= Server.connections.size(); i++) {
                        Socket tempSocket = (Socket) Server.connections.get(i - 1);
                        PrintWriter tempOutput = new PrintWriter(tempSocket.getOutputStream());
                        tempOutput.println(message);
                        tempOutput.flush();
                        System.out.println("Sent to: " + tempSocket.getLocalAddress().getHostName());
                    } // Close for
                } // Close while
            } finally {
                socket.close();
            }
        } catch (Exception exception) {
            System.out.println(exception);
        }
    }
}
