import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class GameServer {
    private static final int PORT = 12345;
    private static final int GRID_SIZE = 500;
    private final Map<Integer, Player> players = new ConcurrentHashMap<>();
    private final List<Food> foodList = new CopyOnWriteArrayList<>();
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private int playerIdCounter = 1;

    public static void main(String[] args) {
        new GameServer().startServer();
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server avviato sulla porta " + PORT);
            generateFood(50);

            // Avvia thread per gestire i bot
            threadPool.execute(this::botLoop);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuovo client connesso: " + clientSocket.getInetAddress());
                threadPool.execute(new ClientHandler(clientSocket, this));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void generateFood(int count) {
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            foodList.add(new Food(random.nextInt(GRID_SIZE), random.nextInt(GRID_SIZE)));
        }
    }

    public List<Food> getFoodList() {
        return foodList;
    }

    public Map<Integer, Player> getPlayers() {
        return players;
    }

    public synchronized int addPlayer(Player player) {
        int id = playerIdCounter++;
        player.setId(id); // Aggiungi un metodo setter per aggiornare l'ID
        players.put(id, player);
        return id;
    }

    public void botLoop() {
        Random random = new Random();
        while (true) {
            for (Player player : players.values()) {
                if (player instanceof Bot bot) {
                    bot.makeMove();
                    checkCollisions(bot);
                }
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void checkCollisions(Player player) {
        synchronized (foodList) {
            foodList.removeIf(food -> {
                if (Math.abs(food.getX() - player.getX()) < 10 && Math.abs(food.getY() - player.getY()) < 10) {
                    player.grow();
                    return true;
                }
                return false;
            });
        }
    }

}

class ClientHandler implements Runnable {
    private final Socket socket;
    private final GameServer server;
    private Player player;

    public ClientHandler(Socket socket, GameServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try (DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            Player newPlayer = new Player(1, 250, 250); // Crea il giocatore
            server.addPlayer(newPlayer);               // Aggiungi il giocatore al server
            player = newPlayer;
            out.writeUTF("ID:" + player.getId());

            while (true) {
                String command = in.readUTF();
                if ("UP".equals(command)) player.moveUp();
                else if ("DOWN".equals(command)) player.moveDown();
                else if ("LEFT".equals(command)) player.moveLeft();
                else if ("RIGHT".equals(command)) player.moveRight();

                server.checkCollisions(player);

                // Aggiorna lo stato del gioco
                StringBuilder state = new StringBuilder();
                for (Player p : server.getPlayers().values()) {
                    state.append(p.getId()).append(",").append(p.getX()).append(",").append(p.getY()).append(";");
                }
                for (Food food : server.getFoodList()) {
                    state.append("F,").append(food.getX()).append(",").append(food.getY()).append(";");
                }
                out.writeUTF(state.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
