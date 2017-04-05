import javafx.scene.control.Alert;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by Andreas on 2017-04-04.
 */
public class ChatClient implements Runnable {

    ClientController clientController = new ClientController();

    Socket socket;
    Scanner input;
    Scanner send = new Scanner(System.in);
    PrintWriter output;

    public ChatClient(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            try {
                input = new Scanner(socket.getInputStream());
                output = new PrintWriter(socket.getOutputStream());
                output.flush();
                checkStream();
            } finally {
                socket.close();
            }
        } catch (Exception exception) {
            System.out.print(exception + "Något gick fel i I/O");
        }
    }

    public void disconnect() throws IOException{
        output.println(ClientController.USERNAME + " has disconnected.");
        output.flush();
        socket.close();

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText("You have disconnected");
        alert.showAndWait();
    }

    public void checkStream() {
        while (true) {
            receieve();
        }
    }

    public void receieve() {
        // Message from server
        if(input.hasNext()) {
            String message = input.nextLine();

            // If coded string methods
            if(message.contains("#USERNAME")) {
                String temp1 = message.substring(8);
                temp1 = temp1.replace("[","");
                temp1 = temp1.replace("]","");

                String[] currentUsers = temp1.split(", ");

                // TODO: 2017-04-04 Skicka currentUsers till clientController
                System.out.println(currentUsers);
                // ClientController.textAreaConnectedUsers.setText(currentUsers);
//                clientController.setTextAreaConnectedUsers(currentUsers);
            } else {
                // TODO: Skicka medellenade till consol
                System.out.println(message);
//                clientController.setTextAreaConsole(message);
//                clientController.getTextAreaConsole().setText(message);

            }

        }
    }

    public void send(String message) {
        output.println(ClientController.USERNAME + ": " + message);
        output.flush();
//        ClientController.textFieldInput.setText("");
    }
}
