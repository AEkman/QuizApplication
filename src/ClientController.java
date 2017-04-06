import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by Andreas on 2017-04-04.
 */
public class ClientController {

    @FXML
    private TextArea textAreaConsole;

    @FXML
    private TextField textFieldHostAdress;

    @FXML
    private Button buttonConnect;

    @FXML
    private TextArea textAreaConnectedUsers;

    @FXML
    private TextField textFieldPortNumber;

    @FXML
    private TextField textFieldUserName;

    @FXML
    private Label labelCurrentUser;

    @FXML
    private Button buttonDisconnect;

    @FXML
    private TextArea textFieldInput;

    @FXML
    private Label labelConnectedUsers;


    public static String USERNAME;

    private static int PORT;
    private static String HOST;

    Socket socket;
    Scanner input;
    PrintWriter output;


    @FXML
    private void initialize() {

        // Button Connect Action
        buttonConnect.setOnAction(event -> {
            if(!textFieldUserName.getText().equals("")) {
                PORT = Integer.parseInt(textFieldPortNumber.getText());
                HOST = textFieldHostAdress.getText();
                USERNAME = textFieldUserName.getText();
                labelCurrentUser.setText(USERNAME);
                buttonConnect.setDisable(true);
                buttonDisconnect.setDisable(false);
                textFieldInput.setEditable(true);
                textFieldInput.setDisable(false);
                connect();
                labelConnectedUsers.setText("Current Users (" + Server.getOnlineUserNumber() +")");
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Information");
                alert.setHeaderText(null);
                alert.setContentText("Please enter a username");
                alert.showAndWait();
            }
        });

        // Button Disconnect Action
        buttonDisconnect.setOnAction(event -> {
            try {
                buttonConnect.setDisable(false);
                buttonDisconnect.setDisable(true);
                disconnect();
            } catch (Exception exception) {
                textAreaConsole.appendText("Couldn't disconnect, something went wrong \n");
            }
        });

        // Textfield sends text when enter key is pressed
        textFieldInput.setOnKeyPressed(keyEvent -> {
            try {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    if (textFieldInput.getText().equals("/DISCONNECT")) {
                        disconnect();
                        textFieldInput.clear();
                    } else if (textFieldInput.getText().equals("/QUIT")) {
                        disconnect();
                        System.exit(1);
                    } else if (textFieldInput.getText().equals("/HELP")){
                        textFieldInput.clear();
                        textAreaConsole.appendText("Available commands:\n" +
                                "/SCORE - print out scores\n" +
                                "/DISCONNECT - disconnect from server\n" +
                                "/SERVERCOMMANDS - sends server commands to online users\n" +
                                "/QUIT - disconnect and quit program\n"
                        );
                        buttonDisconnect.setDisable(true);
                        buttonConnect.setDisable(false);
                } else {
                        String text = textFieldInput.getText();
                        send(text);
                    }
                }
            } catch (Exception exception) {
                textFieldInput.setText("message not sent! -something went wrong... \n");
            }
        }); // End textFieldInput
    } // End initialize

    // Connect to server action
    public void connect() {
        try {
            Socket socket = new Socket(HOST, PORT);
            textAreaConsole.appendText("You are connected to: " + HOST + ":" + PORT + "\n");

            // Start new thread
            Runnable chatClient = () -> {
                try {
                    try {
                        input = new Scanner(socket.getInputStream());
                        output = new PrintWriter(socket.getOutputStream());
                        output.flush();
                        while (true) {
                            // Listens to input
                            receieve();
                        }
                    } finally {
                        socket.close();
                    }
                } catch (Exception exception) {
                    System.out.print(exception + "Något gick fel med I/O");
                }
            };

            // Send name to "current online"
            PrintWriter tempOutput = new PrintWriter(socket.getOutputStream());
            tempOutput.println(USERNAME);
            tempOutput.flush();

            Thread clientThread = new Thread(chatClient);
            clientThread.start();
        } catch (Exception exception) {
            System.out.print(exception);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("Server is not responding, check if server is online!");
            alert.showAndWait();
        }
    }

    // Check incomming sockets for commands and text
    public void receieve() {
        // Messages from server
        if(input.hasNext()) {
            String message = input.nextLine();

            // Current users sent from Server
            if(message.contains("#CLEAR")) {
                textAreaConnectedUsers.setText("");
            } else if (message.contains("#USERNAME")) {
                String temp1 = message.substring(9);
                temp1 = temp1.replace("[","");
                temp1 = temp1.replace("]","");
                textAreaConnectedUsers.appendText(temp1 + "\n");
            } else if (message.contains("/SERVERCOMMANDS")) {
                textAreaConsole.appendText("Available commands:\n" +
                "/SCORE - print out scores\n" +
                "/DISCONNECT - disconnect from server\n" +
                "/QUIT - disconnect and quit program\n"
                );
            } else {
                System.out.println(message);
                textAreaConsole.appendText(message + "\n");
            }
        }
    }

    // Disconnect from server
    public void disconnect() throws IOException {
        try {
            labelCurrentUser.setText("");
            output.println(ClientController.USERNAME + " has disconnected.");
            output.flush();
            socket.close();
        } catch (Exception exception) {
            textAreaConsole.setText("Disconnect function failed! \n");
        }
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText("You have disconnected");
        alert.showAndWait();
    }

    public void send(String message) {
        try {
            output.println(ClientController.USERNAME + ": " + message);
            output.flush();
            textFieldInput.clear();
        } catch (Exception exception) {
            output.println("message not sent! -something went wrong... \n");
        }
    }
}