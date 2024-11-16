import java.util.Random;

public class Bot extends Player {
    private static final Random random = new Random();

    public Bot(int id, int x, int y) {
        super(id, x, y);
    }

    public void makeMove() {
        int direction = random.nextInt(4);
        switch (direction) {
            case 0 -> moveUp();
            case 1 -> moveDown();
            case 2 -> moveLeft();
            case 3 -> moveRight();
        }
    }
}
