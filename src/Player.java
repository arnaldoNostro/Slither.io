public class Player {
    private int id;
    private int x, y;

    public Player(int id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public int getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void moveUp() {
        y -= 10;
    }

    public void moveDown() {
        y += 10;
    }

    public void moveLeft() {
        x -= 10;
    }

    public void moveRight() {
        x += 10;
    }

    public void grow() {
        // Implementa la logica per crescere
    }
    public void setId(int id) {
        this.id = id;
    }

}
