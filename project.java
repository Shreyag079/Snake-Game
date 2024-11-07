import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


class GameObject {
    protected int x, y;
    protected int size;

    public GameObject(int x, int y, int size) {
        this.x = x;
        this.y = y;
        this.size = size;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}


interface Movable {
    void move();
}


class GameOverException extends Exception {
    public GameOverException(String message) {
        super(message);
    }
}


class Snake extends GameObject implements Movable {
    private List<Point> body;
    private String direction;
    private boolean growing;

    public Snake(int x, int y) {
        super(x, y, 20);
        body = new ArrayList<>();
        body.add(new Point(x, y));
        direction = "RIGHT"; 
        growing = false;
    }

    public void setDirection(String newDirection) {
        
        if (!((direction.equals("UP") && newDirection.equals("DOWN")) ||
                (direction.equals("DOWN") && newDirection.equals("UP")) ||
                (direction.equals("LEFT") && newDirection.equals("RIGHT")) ||
                (direction.equals("RIGHT") && newDirection.equals("LEFT")))) {
            this.direction = newDirection;
        }
    }

    
    public void move() {
        Point head = body.get(0);
        Point newHead = new Point(head.x, head.y);

        switch (direction) {
            case "UP":
                newHead.y -= size;
                break;
            case "DOWN":
                newHead.y += size;
                break;
            case "LEFT":
                newHead.x -= size;
                break;
            case "RIGHT":
                newHead.x += size;
                break;
        }

        body.add(0, newHead);

        
        if (!growing) {
            body.remove(body.size() - 1);
        } else {
            growing = false;
        }
    }

    public void grow() {
        growing = true; 
    }

    public List<Point> getBody() {
        return body;
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        for (int i = 0; i < body.size(); i++) {
            Point point = body.get(i);
            g2d.setColor(i == 0 ? Color.YELLOW : Color.GREEN);  
            g2d.fillOval(point.x, point.y, size, size); 
        }
    }
    

    public boolean collidesWithItself() {
        Point head = body.get(0);
        for (int i = 1; i < body.size(); i++) {
            if (head.equals(body.get(i))) {
                return true;
            }
        }
        return false;
    }
}


class Food extends GameObject {
    public Food(int x, int y) {
        super(x, y, 20);
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(255, 69, 0)); 
        g2d.fillOval(x, y, size, size); 
    }
}


class Game {
    private Snake snake;
    private List<Food> foods;
    private Random random;
    private int score;
    private int screenWidth, screenHeight;
    

    public Game(int screenWidth, int screenHeight) {
        snake = new Snake(100, 100);
        foods = new ArrayList<>();
        random = new Random();
        score = 0;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        spawnFood();
    }

    public void spawnFood() {
        int x = random.nextInt(screenWidth / 20) * 20;
        int y = random.nextInt(screenHeight / 20) * 20;
        foods.add(new Food(x, y));
    }

    public void update() throws GameOverException {
        snake.move();

        
        Food foodToRemove = null;
        for (Food food : foods) {
            if (snake.getBody().get(0).x == food.getX() && snake.getBody().get(0).y == food.getY()) {
                snake.grow(); 
                score += 10;
                foodToRemove = food;
                spawnFood();
                break;
            }
        }
        if (foodToRemove != null)
            foods.remove(foodToRemove);

        
        if (snake.collidesWithItself() || snake.getBody().get(0).x < 0 || snake.getBody().get(0).y < 0 ||
                snake.getBody().get(0).x >= screenWidth || snake.getBody().get(0).y >= screenHeight) {
            throw new GameOverException("Game Over! Score: " + score);
        }
    }

    public void draw(Graphics g) {
        snake.draw(g);
        for (Food food : foods) {
            food.draw(g);
        }
    }

    public Snake getSnake() {
        return snake;
    }

    public int getScore() {
        return score;
    }
}


public class project extends JFrame implements ActionListener {
    private Game game;
    private Timer timer;
    private GamePanel gamePanel;
    private int screenWidth, screenHeight;

    public project() {
       
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenWidth = screenSize.width;
        screenHeight = screenSize.height;

        game = new Game(screenWidth, screenHeight);
        gamePanel = new GamePanel();
        add(gamePanel);

        setTitle("Snake Game");
        setSize(screenWidth, screenHeight);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        setResizable(false);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        timer = new Timer(100, this);
        timer.start();

        
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        game.getSnake().setDirection("UP");
                        break;
                    case KeyEvent.VK_DOWN:
                        game.getSnake().setDirection("DOWN");
                        break;
                    case KeyEvent.VK_LEFT:
                        game.getSnake().setDirection("LEFT");
                        break;
                    case KeyEvent.VK_RIGHT:
                        game.getSnake().setDirection("RIGHT");
                        break;
                }
            }
        });
    }

   
    public void actionPerformed(ActionEvent e) {
        try {
            game.update();
            gamePanel.repaint();
        } catch (GameOverException ex) {
            timer.stop();
            showGameOverDialog(ex.getMessage());
        }
    }

    
    private void showGameOverDialog(String message) {
        String[] options = {"OK", "Restart Game"};
        int choice = JOptionPane.showOptionDialog(
                this,
                message,
                "Game Over",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 1) { 
            restartGame();
        } else { 
            System.exit(0); 
        }
    }

 
    private void restartGame() {
        game = new Game(screenWidth, screenHeight);
        timer.start();   
    }

 
   class GamePanel extends JPanel {
  
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        Color color1 = new Color(25, 25, 112); 
        Color color2 = new Color(0, 0, 139); 
        g2d.setPaint(new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2));
        g2d.fillRect(0, 0, getWidth(), getHeight());

       
        game.draw(g);


        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Score: " + game.getScore(), 10, 30);
    }
}


    public static void main(String[] args) {
        SwingUtilities.invokeLater(project::new);
    }
}