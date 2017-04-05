import javafx.beans.property.StringProperty;
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
                System.out.println("Couldn't disconnect, something went wrong \n");
            }
        });

        // TextField Input Chat
        textFieldInput.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER) {
                try {
                    send(textFieldInput.getText());
                    textFieldInput.clear();
                    textFieldInput.requestFocus();
                } catch (Exception e) {
                    textAreaConsole.setText("Failed to send message \n");
                }
            }
        });
    } // End initialize

    // Methods
    public void connect() {
        try {
            Socket socket = new Socket(HOST, PORT);
            System.out.println("You are connected to: " + HOST + ":" + PORT + "\n");
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
            if(message.contains("#USERNAME")) {
                String temp1 = message.substring(9);
                temp1 = temp1.replace("[","");
                temp1 = temp1.replace("]","");
                String[] currentUsers = temp1.split(", ");

                textAreaConnectedUsers.setText(temp1);
            } else {
                System.out.println(message);
                textAreaConsole.appendText(message +"\n");
            }

        }
    }

    public void disconnect() throws IOException {
        labelCurrentUser.setText("");
        output.println(ClientController.USERNAME + " has disconnected.");
        output.flush();
        socket.close();

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText("You have disconnected");
        alert.showAndWait();
    }

    public void send(String message) {
        output.println(ClientController.USERNAME + ": " + message);
        output.flush();
        textFieldInput.setText("");
    }

    // Getters and setters
    public void setTextAreaConsole(String text){
        textAreaConsole.appendText(text);
    }
}