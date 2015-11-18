package kushnirenko.ivan.client;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;


public class Client extends Application {

    private SocketChannel socketChannel;
    private ByteBuffer inputBuffer = ByteBuffer.allocate(64);
    private ByteBuffer outputBuffer = ByteBuffer.allocate(64);
    private String clientName = "Ivan";

    private Text errorText;
    private TextArea chatText;

    private Client client = this;

    private boolean connectionFlag = false;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public ByteBuffer getInputBuffer() {
        return inputBuffer;
    }

    public void setInputBuffer(ByteBuffer inputBuffer) {
        this.inputBuffer = inputBuffer;
    }

    public ByteBuffer getOutputBuffer() {
        return outputBuffer;
    }

    public void setOutputBuffer(ByteBuffer outputBuffer) {
        this.outputBuffer = outputBuffer;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        primaryStage.setResizable(false);

        BorderPane borderPane = new BorderPane();

        TextField ipText = new TextField("localhost");
        TextField portText = new TextField("30001");
        Button connectButton = new Button("Connect");
        FlowPane connect = new FlowPane(ipText, portText, connectButton);
        borderPane.setTop(connect);

        chatText = new TextArea();
        chatText.setEditable(false);
        borderPane.setCenter(chatText);

        TextField messageText = new TextField();
        Button sendMessageButton = new Button("Send");

        errorText = new Text(" ");
        errorText.setFill(Color.RED);
        FlowPane message = new FlowPane(messageText, sendMessageButton, errorText);
        borderPane.setBottom(message);

        Scene scene = new Scene(borderPane, 600, 400);
        primaryStage.setTitle("Async Messenger");
        primaryStage.setScene(scene);
        primaryStage.show();

        ipText.setMinWidth(scene.getWidth() * 0.4);
        portText.setMinWidth(scene.getWidth() * 0.4);
        connectButton.setMinWidth(scene.getWidth() * 0.2);

        messageText.setMinWidth(scene.getWidth() * 0.9);
        sendMessageButton.setMinWidth(scene.getWidth() * 0.1);

        connectButton.setOnMouseClicked(event -> {
            this.connectToServer(ipText.getText(), Integer.parseInt(portText.getText()));
        });

        sendMessageButton.setOnMouseClicked(event -> {
            if (connectionFlag) {
                String typedMessage = messageText.getText();
                if (typedMessage != null && typedMessage.length() > 0) {
                    client.sendMessage("#" + this.getClientName() + ": " + typedMessage + "\n");
                    chatText.appendText("#You: " + typedMessage + "\n");
                    messageText.clear();
                }
            } else {
                errorText.setText("You are not connected to server.");
            }
        });

        messageText.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode().toString().equals("ENTER") && connectionFlag) {
                    String typedMessage = messageText.getText();
                    if (typedMessage != null && typedMessage.length() > 0) {
                        client.sendMessage("#" + client.getClientName() + ": " + typedMessage + "\n");
                        chatText.appendText("#You: " + typedMessage + "\n");
                        messageText.clear();
                    }
                }
            }
        });

        primaryStage.setOnCloseRequest(event -> {
            clientThread.interrupt();
        });

    }

    public void connectToServer(String ipAddress, int port) {
        try {
            socketChannel = SocketChannel.open(new InetSocketAddress(ipAddress, port));
            connectionFlag = true;
            errorText.setText("");
            System.out.println("Connected successfully.");
        } catch (ConnectException exp) {
            errorText.setText("ERROR: Connection refused.");
            exp.printStackTrace();
        } catch (UnknownHostException exp) {
            errorText.setText("ERROR: Unknown server: " + ipAddress + ":" + port + ".");
            exp.printStackTrace();
        } catch (IOException exp) {
            errorText.setText("ERROR: Cannot connect to server.");
            exp.printStackTrace();
        }
        clientThread.start();
    }

    public void sendMessage(String message) {
        try {
            outputBuffer.put(message.getBytes());
            outputBuffer.flip();
            if (outputBuffer.hasRemaining()) {
                socketChannel.write(outputBuffer);
            }
        } catch (IOException exp) {
            System.out.println("ERROR: Cannot send message.");
            exp.printStackTrace();
        } finally {
            outputBuffer.clear();
        }
    }

    Thread clientThread = new Thread() {
        @Override
        public void run() {
            int readed;
            while (true) {
                if (this.isInterrupted()) {
                    return;
                }
                try {
                    if ((readed = socketChannel.read(inputBuffer)) > 0) {
                        String gettedMessage = new String(inputBuffer.array(), 0, readed);
                        chatText.appendText(gettedMessage);
                        inputBuffer.clear();
                    }
                } catch (IOException exp) {
                    System.out.println("ERROR: Cannot read message.");
                    exp.printStackTrace();
                } finally {
                    inputBuffer.clear();
                }
            }
        }
    };

}
