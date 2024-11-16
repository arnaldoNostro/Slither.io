import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class GameClient extends Application {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private Canvas canvas;
    private GraphicsContext gc;

    private final List<Food> foodList = new ArrayList<>();
    private final List<Player> players = new ArrayList<>();

    @Override
    public void start(Stage stage) {
        canvas = new Canvas(500, 500);
        gc = canvas.getGraphicsContext2D();

        Scene scene = new Scene(canvas.getParent());
        stage.setScene(scene);
        stage.setTitle("Slither.io Client");
        stage.show();

        connectToServer();

        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case UP -> sendDirection("UP");
                case DOWN -> sendDirection("DOWN");
                case LEFT -> sendDirection("LEFT");
                case RIGHT -> sendDirection("RIGHT");
            }
        });

        new Thread(this::gameLoop).start();
    }

    private void connectToServer() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendDirection(String direction) {
        try {
            out.writeUTF(direction);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void gameLoop() {
        while (true) {
            try {
                String data = in.readUTF();
                updateGameState(data);
                render();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private void updateGameState(String data) {
        players.clear();
        foodList.clear();

        String[] elements = data.split(";");
        for (String element : elements) {
            String[] parts = element.split(",");
            if (parts[0].equals("F")) {
                foodList.add(new Food(Integer.parseInt(parts[1]), Integer.parseInt(parts[2])));
            } else {
                players.add(new Player(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2])));
            }
        }
    }

    private void render() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setFill(Color.RED);
        for (Food food : foodList) {
            gc.fillOval(food.getX(), food.getY(), 5, 5);
        }

        gc.setFill(Color.BLUE);
        for (Player player : players) {
            gc.fillOval(player.getX(), player.getY(), 10, 10);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
