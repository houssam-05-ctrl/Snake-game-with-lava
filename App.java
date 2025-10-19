import javax.swing.*;

public class App {
    public static void main(String[] args) throws Exception {
        int boardWidth = 600;
        int boardHeight = boardWidth;

        JFrame frame = new JFrame("Snake Game - Don't be LINA!");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);

        SnakeGame game = new SnakeGame(boardWidth, boardHeight);
        frame.add(game);
        frame.pack();
        frame.setVisible(true);
    }
}