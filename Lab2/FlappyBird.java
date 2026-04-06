import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    // Bài 1: Thiết lập kích thước cửa sổ 
    static int boardWidth = 360;
    static int boardHeight = 640;

    // Hình ảnh
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    // Bài 2. Đối tượng Bird
    class Bird {
        int x = boardWidth / 8;
        int y = boardHeight / 2;
        int width = 40;
        int height = 40;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    Bird bird;
    int velocityY = 0;
    int gravity = 1;

    //Bài 3. Đối tượng Pipe
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;  
    int pipeHeight = 512; 

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    // Danh sách ống
    ArrayList<Pipe> pipes;
    //Thiết lập game loop và timer để tạo ống mới
    Timer gameLoop;
    Timer placePipesTimer;

    // Bài 4. Cơ chế mặc định
    boolean gameOver = false;
    double score = 0;

    FlappyBird() {
        // Bài 1. Thiết lập kích thước 
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true); 
        addKeyListener(this);

        // Tải hình ảnh
        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        // Bài 2. Khởi tạo đối tượng chim
        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();

        // Bài 3. Thiết lập chu trình tạo ống
        placePipesTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });
        placePipesTimer.start();

        // Bài 3: Khởi tạo chu trình game
        gameLoop = new Timer(1000 / 60, this);
        gameLoop.start();
    }

    // Bài 3. Hàm tạo ống mới với độ cao ngẫu nhiên
    public void placePipes() {
        // Độ cao ngẫu nhiên cho ống trên
        int randomPipeY = (int) (pipeY - pipeHeight/4 - Math.random()*(pipeHeight/2));
        int openingSpace = boardHeight/4; 

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    // sử dụng phương thức paintComponent để vẽ giao diện 
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    // Vẽ hình ảnh nền và chim
    public void draw(Graphics g) {
        // Bài 1 + 2Vẽ nền và chim
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);
        g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);
        // Bài 3. Vẽ ống
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        // Bài 4: Hiển thị Điểm 
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 32));
        if (gameOver) {
            g.drawString("GAME OVER: " + (int) score, 60, boardHeight/2);
            g.setFont(new Font("Arial", Font.PLAIN, 15));
            g.drawString("Press 'SPACE' to Restart", 90, boardHeight/2 + 50);
        } else {
            g.drawString(String.valueOf((int) score), 10, 35);
        }
    }

    // Di chuyển
    public void move() {
        // Di chuyển của chim
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);
        // Di chuyển ống
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.x -= 4; 
            // Tính điểm
            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                pipe.passed = true;
                score += 0.5; 
            }

            // Kiểm tra va chạm
            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }

        if (bird.y > boardHeight) {
            gameOver = true;
        }
    }

    // Bài 4. kiểm tra va chạm
    public boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               a.y < b.y + b.height &&
               a.y + a.height > b.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint(); 
        if (gameOver) {
            placePipesTimer.stop();
            gameLoop.stop();
        }
    }

    // Xử lý sự kiện bàn phím
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER) {
            velocityY = -9;
        } 
        // Bài 4. Tính năng restart
        if (gameOver && (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER)) {
            bird.y = boardHeight / 2;
            velocityY = 0;
            pipes.clear();
            score = 0;
            gameOver = false;
            gameLoop.start();
            placePipesTimer.start();
        }
    }


    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}

    public static void main(String[] args) {
        // Tạo cửa sổ game
        JFrame frame = new JFrame("Flappy Bird");
        
        // Câu 1
        frame.setSize(boardWidth, boardHeight); 
        frame.setLocationRelativeTo(null);      
        frame.setResizable(false);              
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Tạo đối tượng game và thêm vào frame
        FlappyBird flappyBird = new FlappyBird();
        frame.add(flappyBird);
        frame.pack(); 
        
        flappyBird.requestFocus();
        frame.setVisible(true); 
    }
}