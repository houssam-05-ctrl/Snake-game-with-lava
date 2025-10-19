import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Random;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {
    private class Tile {
        int x;
        int y;

        Tile(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Tile tile = (Tile) obj;
            return x == tile.x && y == tile.y;
        }
    }
    
    int boardWidth;
    int boardHeight;
    int tileSize = 25;
    
    // snake
    Tile snakeHead;
    ArrayList<Tile> snakeBody;
    
    // food 
    Tile food;
    Random random; 
    
    // lava cubes
    ArrayList<Tile> lavaCubes;
    boolean lavaActive = false;

    // game logic
    Timer gameLoop;
    int velocityX;
    int velocityY;
    boolean gameOver = false;
    int baseDelay = 100; // base speed (higher = slower)

    SnakeGame(int boardWidth, int boardHeight) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        setPreferredSize(new Dimension(this.boardWidth, this.boardHeight));
        setBackground(Color.black);
        
        // add key listener for controls
        addKeyListener(this);
        setFocusable(true);

        // snake
        snakeHead = new Tile(5, 5);
        snakeBody = new ArrayList<>();
        
        // lava
        lavaCubes = new ArrayList<>();
        
        // put food at random position
        random = new Random();
        placeFood();

        // Initialize game loop with base speed
        gameLoop = new Timer(baseDelay, this);
        gameLoop.start();

        // movement direction
        velocityX = 1; // Start moving right
        velocityY = 0;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    private void draw(Graphics g) {
        // Draw grid
        g.setColor(Color.darkGray);
        for (int i = 0; i < boardWidth / tileSize; i++) {
            g.drawLine(i * tileSize, 0, i * tileSize, boardHeight);
            g.drawLine(0, i * tileSize, boardWidth, i * tileSize);
        } 

        // Draw food
        g.setColor(Color.red);
        g.fillRect(food.x * tileSize, food.y * tileSize, tileSize, tileSize);

        // Draw lava cubes if active
        if (lavaActive) {
            g.setColor(Color.orange);
            for (Tile lava : lavaCubes) {
                g.fillRect(lava.x * tileSize, lava.y * tileSize, tileSize, tileSize);
            }
        }

        // Draw snake head
        g.setColor(Color.green);
        g.fillRect(snakeHead.x * tileSize, snakeHead.y * tileSize, tileSize, tileSize);
        
        // Draw snake body
        for (int i = 0; i < snakeBody.size(); i++) {
            // Gradient color for snake body
            float ratio = (float) i / snakeBody.size();
            int green = (int) (255 * (1 - ratio));
            int blue = (int) (255 * ratio);
            g.setColor(new Color(0, green, blue));
            g.fillRect(snakeBody.get(i).x * tileSize, snakeBody.get(i).y * tileSize, tileSize, tileSize);
        }

        // Draw score
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.drawString("Score: " + snakeBody.size(), 10, 20);
        
        // Draw speed indicator if faster
        if (gameLoop.getDelay() < baseDelay) {
            g.setColor(Color.yellow);
            g.drawString("Speed: " + (baseDelay - gameLoop.getDelay()) + "%", 10, 40);
        }

        // Game over display
        if (gameOver) {
            g.setColor(Color.red);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            
            // Create a semi-transparent background
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(boardWidth/4, boardHeight/3, boardWidth/2, boardHeight/3);
            
            // Draw the "LINA LOSER" message in two lines
            g.setColor(Color.red);
            FontMetrics metrics = g.getFontMetrics();
            String line1 = "SUCH A";
            String line2 = "LOSER";
            
            int x1 = (boardWidth - metrics.stringWidth(line1)) / 2;
            int x2 = (boardWidth - metrics.stringWidth(line2)) / 2;
            int y = boardHeight / 2;
            
            g.drawString(line1, x1, y - 10);
            g.drawString(line2, x2, y + 40);
            
            // Draw restart instruction
            g.setColor(Color.white);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Press SPACE to restart", boardWidth/2 - 100, y + 80);
        }
    }

    private void placeFood() {
        while (true) {
            int x = random.nextInt(boardWidth / tileSize);
            int y = random.nextInt(boardHeight / tileSize);
            food = new Tile(x, y);
            
            // Check if food doesn't overlap with snake or lava
            boolean validPosition = true;
            
            // Check snake head
            if (collision(food, snakeHead)) {
                validPosition = false;
            }
            
            // Check snake body
            for (Tile segment : snakeBody) {
                if (collision(food, segment)) {
                    validPosition = false;
                    break;
                }
            }
            
            // Check lava cubes
            if (lavaActive) {
                for (Tile lava : lavaCubes) {
                    if (collision(food, lava)) {
                        validPosition = false;
                        break;
                    }
                }
            }
            
            if (validPosition) break;
        }
    }
    
    private void generateLava() {
        lavaCubes.clear();
        int lavaCount = 5 + random.nextInt(6); // 5-10 lava cubes
        
        for (int i = 0; i < lavaCount; i++) {
            while (true) {
                int x = random.nextInt(boardWidth / tileSize);
                int y = random.nextInt(boardHeight / tileSize);
                Tile lava = new Tile(x, y);
                
                // Check if lava doesn't overlap with snake, food, or other lava
                boolean validPosition = true;
                
                if (collision(lava, snakeHead) || collision(lava, food)) {
                    validPosition = false;
                }
                
                for (Tile segment : snakeBody) {
                    if (collision(lava, segment)) {
                        validPosition = false;
                        break;
                    }
                }
                
                for (Tile existingLava : lavaCubes) {
                    if (collision(lava, existingLava)) {
                        validPosition = false;
                        break;
                    }
                }
                
                if (validPosition) {
                    lavaCubes.add(lava);
                    break;
                }
            }
        }
        lavaActive = true;
    }

    public boolean collision(Tile tile1, Tile tile2) {
        return tile1.x == tile2.x && tile1.y == tile2.y;
    }

    private void move() {
        // Add current head position to body before moving
        snakeBody.add(new Tile(snakeHead.x, snakeHead.y));

        // Move head
        snakeHead.x += velocityX;
        snakeHead.y += velocityY;

        // Check if snake ate food
        if (collision(snakeHead, food)) {
            placeFood(); // Place new food, don't remove tail segment
            
            // Increase speed when score > 5
            if (snakeBody.size() > 5) {
                int newDelay = Math.max(50, baseDelay - (snakeBody.size() - 5) * 5);
                gameLoop.setDelay(newDelay);
            }
            
            // Generate lava when score > 15
            if (snakeBody.size() > 15 && !lavaActive) {
                generateLava();
            }
        } else if (snakeBody.size() > 0) {
            // Remove tail segment if no food was eaten
            snakeBody.remove(0);
        }

        // Check for wall collisions (wrap around)
        if (snakeHead.x < 0) {
            snakeHead.x = (boardWidth / tileSize) - 1;
        } else if (snakeHead.x >= boardWidth / tileSize) {
            snakeHead.x = 0;
        }

        if (snakeHead.y < 0) {
            snakeHead.y = (boardHeight / tileSize) - 1;
        } else if (snakeHead.y >= boardHeight / tileSize) {
            snakeHead.y = 0;
        }

        // Check for self-collision
        for (Tile segment : snakeBody) {
            if (collision(snakeHead, segment)) {
                gameOver = true;
                break;
            }
        }
        
        // Check for lava collision
        if (lavaActive) {
            for (Tile lava : lavaCubes) {
                if (collision(snakeHead, lava)) {
                    gameOver = true;
                    break;
                }
            }
        }
    }
    
    private void resetGame() {
        snakeHead = new Tile(5, 5);
        snakeBody.clear();
        lavaCubes.clear();
        lavaActive = false;
        placeFood();
        velocityX = 1;
        velocityY = 0;
        gameOver = false;
        gameLoop.setDelay(baseDelay);
        gameLoop.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            move();
            repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        
        if (gameOver && keyCode == KeyEvent.VK_SPACE) {
            resetGame();
            return;
        }
        
        if (!gameOver) {
            // Prevent 180-degree turns
            if (keyCode == KeyEvent.VK_UP && velocityY != 1) {
                velocityX = 0;
                velocityY = -1;
            } else if (keyCode == KeyEvent.VK_DOWN && velocityY != -1) {
                velocityX = 0;
                velocityY = 1;
            } else if (keyCode == KeyEvent.VK_LEFT && velocityX != 1) {
                velocityX = -1;
                velocityY = 0;
            } else if (keyCode == KeyEvent.VK_RIGHT && velocityX != -1) {
                velocityX = 1;
                velocityY = 0;
            }
        }
    }

    // Required KeyListener methods
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}