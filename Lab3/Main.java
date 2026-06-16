import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main extends JFrame {
    
    // Danh sách sản phẩm mẫu ( xuất hiện đàu tiên)
    static class Product {
        String title;
        String price;
        String brand = "Adidas";
        String desc = "This product is excluded from all\npromotional discounts and offers.";
        String shortDesc = "This product is excluded fr...";
        BufferedImage image;

        public Product(String title, String price, String shortDesc) {
            this.title = title;
            this.price = price;
            if (shortDesc != null) {
                this.shortDesc = shortDesc;
            }
        }
    }

    private List<Product> products = new ArrayList<>();
    private int selectedIndex = 0;
    
    // Biến animation
    private BufferedImage oldImage;
    private BufferedImage newImage;
    private float alpha = 1.0f;
    private Timer animationTimer;
    
    // Các UI bên trái
    private JPanel mainImagePanel;
    private JLabel mainTitleLabel;
    private JLabel mainPriceLabel;
    private JLabel mainBrandLabel;
    private JTextArea mainDescArea;
    private JPanel gridPanel;
    
    public Main() {
        setTitle("Lab 03 - Shoes Store");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);
        
        loadProducts();
        
        // Main panel
        JPanel rootPanel = new JPanel(new BorderLayout(30, 0));
        rootPanel.setBackground(Color.WHITE);
        rootPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        // Left panel - hiển thị chi tiết sản phẩm được chọn
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setPreferredSize(new Dimension(280, 0));
        
        // Main img
        mainImagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                
                int w = getWidth();
                int h = getHeight();
                
                if (oldImage != null && alpha < 1.0f) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f - alpha));
                    drawImageCentered(g2, oldImage, w, h);
                }
                
                if (newImage != null) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                    drawImageCentered(g2, newImage, w, h);
                }
                g2.dispose();
            }
            
            private void drawImageCentered(Graphics2D g2, BufferedImage img, int w, int h) {
                double scale = Math.min((double) w / img.getWidth(), (double) h / img.getHeight());
                int imgW = (int) (img.getWidth() * scale);
                int imgH = (int) (img.getHeight() * scale);
                int x = (w - imgW) / 2;
                int y = (h - imgH) / 2;
                g2.drawImage(img, x, y, imgW, imgH, null);
            }
        };
        mainImagePanel.setBackground(Color.WHITE);
        mainImagePanel.setPreferredSize(new Dimension(280, 250));
        mainImagePanel.setMaximumSize(new Dimension(280, 250));
        mainImagePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        leftPanel.add(mainImagePanel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Đường kẻ ngang phân cách
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(280, 1));
        sep.setForeground(new Color(200, 200, 200));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(sep);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Tiêu đề 
        mainTitleLabel = new JLabel("TITLE");
        mainTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        mainTitleLabel.setForeground(new Color(70, 70, 70));
        mainTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(mainTitleLabel);
        
        leftPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        
        // Giá 
        mainPriceLabel = new JLabel("$0.00");
        mainPriceLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        mainPriceLabel.setForeground(new Color(70, 70, 70));
        mainPriceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(mainPriceLabel);
        
        leftPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        
        // Thương hiệu
        mainBrandLabel = new JLabel("Adidas");
        mainBrandLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        mainBrandLabel.setForeground(new Color(130, 130, 130));
        mainBrandLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(mainBrandLabel);
        
        leftPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        
        // Đoạn mô tả 
        mainDescArea = new JTextArea("Desc");
        mainDescArea.setFont(new Font("Segoe UI", Font.BOLD, 12));
        mainDescArea.setForeground(new Color(160, 160, 160));
        mainDescArea.setLineWrap(true);
        mainDescArea.setWrapStyleWord(true);
        mainDescArea.setEditable(false);
        mainDescArea.setFocusable(false);
        mainDescArea.setBackground(Color.WHITE);
        mainDescArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(mainDescArea);
        
        // Right panel
        gridPanel = new JPanel(new GridLayout(0, 3, 20, 20));
        gridPanel.setBackground(Color.WHITE);
        
        for (int i = 0; i < products.size(); i++) {
            gridPanel.add(createProductCard(i));
        }
        
        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        rootPanel.add(leftPanel, BorderLayout.WEST);
        rootPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(rootPanel);
        
        // Khởi tạo sản phẩm
        if (!products.isEmpty()) {
            updateLeftPanel(0, false);
        }
    }
    
    // Dữ liệu các sản phẩm với nội dung gọn
    private void loadProducts() {
        products.add(new Product("4DFWD PULSE SHOES", "$160.00", "This product is excluded fr..."));
        products.add(new Product("FORUM MID SHOES", "$100.00", "This product is excluded fr..."));
        products.add(new Product("SUPERNOVA SHOES", "$150.00", "NMD City Stock 2"));
        products.add(new Product("Adidas", "$160.00", "NMD City Stock 2"));
        products.add(new Product("Adidas", "$120.00", "NMD City Stock 2"));
        products.add(new Product("4DFWD PULSE SHOES", "$160.00", "This product is excluded fr..."));
        
        for (int i = 0; i < 6; i++) {
            try {
                File imgFile = new File("Lab03/img" + (i + 1) + ".png");
                if (!imgFile.exists()) imgFile = new File("img" + (i + 1) + ".png");
                products.get(i).image = ImageIO.read(imgFile);
            } catch (Exception e) {
                System.err.println("Không thể tải hình ảnh img" + (i + 1) + ".png");
            }
        }
    }
    
    // Card sản phẩm
    private JPanel createProductCard(int index) {
        Product p = products.get(index);
        
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(242, 243, 244)); 
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                if (selectedIndex == index) {
                    g2.setColor(new Color(100, 150, 255));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 15, 15);
                }
                
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout(10, 10));
        card.setBorder(new EmptyBorder(15, 15, 15, 15));
        card.setOpaque(false); 
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Text trên
        JPanel topText = new JPanel();
        topText.setLayout(new BoxLayout(topText, BoxLayout.Y_AXIS));
        topText.setOpaque(false);
        
        JLabel title = new JLabel(p.title);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(new Color(70, 70, 70));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel subtitle = new JLabel(p.shortDesc);
        subtitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        subtitle.setForeground(new Color(160, 160, 160));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        topText.add(title);
        topText.add(Box.createRigidArea(new Dimension(0, 4)));
        topText.add(subtitle);
        
        // img
        JLabel imgLabel = new JLabel();
        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        if (p.image != null) {
            Image scaled = p.image.getScaledInstance(160, 160, Image.SCALE_SMOOTH);
            imgLabel.setIcon(new ImageIcon(scaled));
        }
        
        // text dưới
        JPanel botText = new JPanel(new BorderLayout());
        botText.setOpaque(false);
        
        JLabel brand = new JLabel(p.brand);
        brand.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        brand.setForeground(new Color(130, 130, 130));
        
        JLabel price = new JLabel(p.price);
        price.setFont(new Font("Segoe UI", Font.BOLD, 16));
        price.setForeground(new Color(70, 70, 70));
        
        botText.add(brand, BorderLayout.WEST);
        botText.add(price, BorderLayout.EAST);
        
        card.add(topText, BorderLayout.NORTH);
        card.add(imgLabel, BorderLayout.CENTER);
        card.add(botText, BorderLayout.SOUTH);
        
        // Xử lý click card
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (selectedIndex != index) {
                    updateLeftPanel(index, true); // Gọi hàm chuyển ảnh và text
                    gridPanel.repaint(); // Cập nhật lại border
                }
            }
        });
        
        return card;
    }
    
    // Cập nhật thông tin khi card bên trái được chọn
    private void updateLeftPanel(int newIndex, boolean animate) {
        Product p = products.get(newIndex);
        
        mainTitleLabel.setText(p.title);
        mainPriceLabel.setText(p.price);
        mainBrandLabel.setText(p.brand);
        mainDescArea.setText(p.desc);
        
        if (animate) {
            if (animationTimer != null && animationTimer.isRunning()) {
                animationTimer.stop();
            }
            oldImage = products.get(selectedIndex).image;
            newImage = p.image;
            alpha = 0.0f;
            
            animationTimer = new Timer(20, e -> {
                alpha += 0.06f;
                if (alpha >= 1.0f) {
                    alpha = 1.0f;
                    animationTimer.stop();
                }
                mainImagePanel.repaint();
            });
            animationTimer.start();
        } else {
            oldImage = null;
            newImage = p.image;
            alpha = 1.0f;
            mainImagePanel.repaint();
        }
        
        selectedIndex = newIndex;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            new Main().setVisible(true);
        });
    }
}