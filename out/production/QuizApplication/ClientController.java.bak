import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import java.io.PrintWriter;
import java.net.Socket;

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
    private Button buttonDisconnect;

    @FXML
    private TextArea textFieldCurrentUserName;

    @FXML
    private TextArea textFieldInput;

    @FXML
    private Button buttonTest;

    private static ChatClient chatClient;

    public static String USERNAME = "Anonymous";

    private static int PORT;
    private static String HOST;

    @FXML
    private void initialize() {

        // Button Connect
        buttonConnect.setOnAction(event -> {
            if(!textFieldUserName.getText().equals("")) {
                PORT = Integer.parseInt(textFieldPortNumber.getText());
                HOST = textFieldHostAdress.getText();
                USERNAME = textFieldUserName.getText();
                textFieldCurrentUserName.setText(USERNAME);
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
                chatClient.disconnect();
                buttonConnect.setDisable(false);
                buttonDisconnect.setDisable(true);
            } catch (Exception exception) {
                System.out.println("Couldn't disconnect, something went wrong");
            }
        });

        // TextField Input Chat
        textFieldInput.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER) {
                try {
                    chatClient.send(textFieldInput.getText());
                    textFieldInput.clear();
                    textFieldInput.requestFocus();
                } catch (Exception e) {
                    textAreaConsole.setText("Failed to send message" + "\n");
                }
            }
        });

        buttonTest.setOnAction(event -> {
            setTextAreaConsole("test" +"\n");
        });
    }

    // Methods
    public static void connect() {
        try {
            Socket socket = new Socket(HOST, PORT);
            System.out.println("You connected to: " + HOST);
            chatClient = new ChatClient(socket);

            // Send name to "current online"
            PrintWriter output = new PrintWriter(socket.getOutputStream());
            output.println(USERNAME);
            output.flush();

            Thread clientThread = new Thread(chatClient);
            clientThread.start();
        } catch (Exception exception) {
            System.out.print(exception);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("Server is not responding");
            alert.showAndWait();
        }
    }

    // Getters and setters
    public void setTextAreaConsole(String text){
        textAreaConsole.appendText(text);
    }
}