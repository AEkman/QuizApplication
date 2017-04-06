import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

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

    public static String USERNAME;

    private static int PORT;
    private static String HOST;

    Socket socket;
    Scanner input;
    PrintWriter output;

    @FXML
    private void initialize() {

        // Button Connect
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
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Information");
                alert.setHeaderText(null);
                alert.setContentText("Please enter a username");
                alert.showAndWait();
            }
        });

        // Button Disconnect
        buttonDisconnect.setOnAction(event -> {
            try {
                buttonConnect.setDisable(false);
                buttonDisconnect.setDisable(true);
                disconnect();
            } catch (Exception exception) {
                textAreaConsole.appendText("Couldn't disconnect, something went wrong \n");
            }
        });

        textFieldInput.setOnKeyPressed(keyEvent -> {
            try {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    if (textFieldInput.getText().equals("/DISCONNECT")) {
                        disconnect();
                    } else if (textFieldInput.getText().equals("/QUIT")) {
                        disconnect();
                        System.exit(1);
                    } else if (textFieldInput.getText().equals("/HELP")){
                        textAreaConsole.appendText("Available commands:\n" +
                                "/SCORE - print out scores\n" +
                                "/DISCONNECT - disconnect from server\n" +
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
        });
    } // End initialize

    // Methods
    public void connect() {
        try {
            Socket socket = new Socket(HOST, PORT);
            textAreaConsole.appendText("You are connected to: " + HOST + ":" + PORT + "\n");

            Runnable chatClient = () -> {
                try {
                    try {
                        input = new Scanner(socket.getInputStream());
                        output = new PrintWriter(socket.getOutputStream());
                        output.flush();
                        while (true) {
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

    // Getters and setters
    public void setTextAreaConsole(String text){
        textAreaConsole.appendText(text);
    }
}