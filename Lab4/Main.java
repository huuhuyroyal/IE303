import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Main extends JFrame {

    static class Product {
        int id;
        String title, price, brand, desc, shortDesc, imageFile;
        BufferedImage image;

        public Product(int id, String title, String price, String brand, String desc, String shortDesc,
                String imageFile) {
            this.id = id;
            this.title = title;
            this.price = price;
            this.brand = brand;
            this.desc = desc;
            this.shortDesc = shortDesc;
            this.imageFile = imageFile;
        }
    }

    private List<Product> products = new ArrayList<>();
    private List<Product> filtered = new ArrayList<>();
    private int selectedIndex = 0;

    private BufferedImage oldImage, newImage;
    private float alpha = 1.0f;
    private Timer animationTimer;

    private JPanel mainImagePanel, gridPanel;
    private JLabel mainTitleLabel, mainPriceLabel, mainBrandLabel;
    private JTextArea mainDescArea;
    private JTextField searchField;

    private static final String DB_URL;
    static final File APP_DIR;
    static {
        String loc = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        try {
            loc = java.net.URLDecoder.decode(loc, "UTF-8");
        } catch (Exception ignored) {
        }
        APP_DIR = new File(loc).getParentFile();
        DB_URL = "jdbc:sqlite:" + APP_DIR.getAbsolutePath() + "/products.db";
    }

    public Main() {
        setTitle("Lab 04 - Shoes Store with SQLite Database");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 720);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);

        loadProductsFromDB();
        filtered.addAll(products);

        // ── TOP BAR ──────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout(8, 0));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new EmptyBorder(10, 30, 0, 30));

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(5, 10, 5, 10)));

        JButton btnSearch = btn("🔍 Tìm kiếm", new Color(70, 130, 220));
        JButton btnAdd = btn("＋ Thêm", new Color(46, 170, 100));
        JButton btnEdit = btn("✎ Sửa", new Color(230, 150, 30));
        JButton btnDelete = btn("✕ Xóa", new Color(210, 60, 60));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(btnSearch);
        btnPanel.add(btnAdd);
        btnPanel.add(btnEdit);
        btnPanel.add(btnDelete);

        topBar.add(searchField, BorderLayout.CENTER);
        topBar.add(btnPanel, BorderLayout.EAST);

        // ── LEFT PANEL ───────────────────────────────────────────
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setPreferredSize(new Dimension(280, 0));

        mainImagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                int w = getWidth(), h = getHeight();
                if (oldImage != null && alpha < 1.0f) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f - alpha));
                    drawCentered(g2, oldImage, w, h);
                }
                if (newImage != null) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                    drawCentered(g2, newImage, w, h);
                }
                g2.dispose();
            }

            private void drawCentered(Graphics2D g2, BufferedImage img, int w, int h) {
                double scale = Math.min((double) w / img.getWidth(), (double) h / img.getHeight());
                int iw = (int) (img.getWidth() * scale), ih = (int) (img.getHeight() * scale);
                g2.drawImage(img, (w - iw) / 2, (h - ih) / 2, iw, ih, null);
            }
        };
        mainImagePanel.setBackground(Color.WHITE);
        mainImagePanel.setPreferredSize(new Dimension(280, 250));
        mainImagePanel.setMaximumSize(new Dimension(280, 250));
        mainImagePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(mainImagePanel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(280, 1));
        sep.setForeground(new Color(200, 200, 200));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(sep);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        mainTitleLabel = label("TITLE", 22, Font.BOLD, new Color(70, 70, 70));
        mainPriceLabel = label("$0.00", 20, Font.BOLD, new Color(70, 70, 70));
        mainBrandLabel = label("Brand", 13, Font.PLAIN, new Color(130, 130, 130));

        mainDescArea = new JTextArea("Desc");
        mainDescArea.setFont(new Font("Segoe UI", Font.BOLD, 12));
        mainDescArea.setForeground(new Color(160, 160, 160));
        mainDescArea.setLineWrap(true);
        mainDescArea.setWrapStyleWord(true);
        mainDescArea.setEditable(false);
        mainDescArea.setFocusable(false);
        mainDescArea.setBackground(Color.WHITE);
        mainDescArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftPanel.add(mainTitleLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        leftPanel.add(mainPriceLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        leftPanel.add(mainBrandLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        leftPanel.add(mainDescArea);

        // ── GRID ─────────────────────────────────────────────────
        gridPanel = new JPanel(new GridLayout(0, 3, 20, 20));
        gridPanel.setBackground(Color.WHITE);
        refreshGrid();

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel rootPanel = new JPanel(new BorderLayout(30, 0));
        rootPanel.setBackground(Color.WHITE);
        rootPanel.setBorder(new EmptyBorder(20, 30, 30, 30));
        rootPanel.add(leftPanel, BorderLayout.WEST);
        rootPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBackground(Color.WHITE);
        content.add(topBar, BorderLayout.NORTH);
        content.add(rootPanel, BorderLayout.CENTER);
        add(content);

        if (!filtered.isEmpty())
            updateLeftPanel(0, false);

        // ── EVENTS ───────────────────────────────────────────────
        btnSearch.addActionListener(e -> doSearch());
        searchField.addActionListener(e -> doSearch());
        btnAdd.addActionListener(e -> showAddDialog());
        btnEdit.addActionListener(e -> {
            if (filtered.isEmpty())
                return;
            showEditDialog(filtered.get(selectedIndex));
        });
        btnDelete.addActionListener(e -> {
            if (filtered.isEmpty())
                return;
            Product p = filtered.get(selectedIndex);
            int ok = JOptionPane.showConfirmDialog(this,
                    "Xóa sản phẩm: " + p.title + "?", "Xác nhận xóa",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ok == JOptionPane.YES_OPTION)
                deleteProduct(p.id);
        });
    }

    // Database sample
    private void loadProductsFromDB() {
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                conn.createStatement().execute(
                        "CREATE TABLE IF NOT EXISTS products (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                "title TEXT NOT NULL, price TEXT NOT NULL," +
                                "brand TEXT DEFAULT 'Adidas'," +
                                "short_desc TEXT, description TEXT, image_file TEXT)");

                ResultSet rc = conn.createStatement().executeQuery("SELECT COUNT(*) FROM products");
                if (rc.getInt(1) == 0)
                    insertSampleData(conn);

                ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM products");
                while (rs.next()) {
                    Product p = new Product(
                            rs.getInt("id"), rs.getString("title"), rs.getString("price"),
                            rs.getString("brand"), rs.getString("description"),
                            rs.getString("short_desc"), rs.getString("image_file"));
                    p.image = loadImage(p.imageFile);
                    products.add(p);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi CSDL: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void insertSampleData(Connection conn) throws SQLException {
        String sql = "INSERT INTO products(title,price,brand,short_desc,description,image_file) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            Object[][] data = {
                    { "4DFWD PULSE SHOES", "$160.00", "Adidas", "This product is excluded fr...",
                            "This product is excluded from all\npromotional discounts and offers.", "img1.png" },
                    { "FORUM MID SHOES", "$100.00", "Adidas", "This product is excluded fr...",
                            "This product is excluded from all\npromotional discounts and offers.", "img2.png" },
                    { "SUPERNOVA SHOES", "$150.00", "Adidas", "NMD City Stock 2",
                            "This product is excluded from all\npromotional discounts and offers.", "img3.png" },
                    { "Adidas", "$160.00", "Adidas", "NMD City Stock 2",
                            "This product is excluded from all\npromotional discounts and offers.", "img4.png" },
                    { "Adidas", "$120.00", "Adidas", "NMD City Stock 2",
                            "This product is excluded from all\npromotional discounts and offers.", "img5.png" },
                    { "4DFWD PULSE SHOES", "$160.00", "Adidas", "This product is excluded fr...",
                            "This product is excluded from all\npromotional discounts and offers.", "img6.png" }
            };
            for (Object[] row : data) {
                for (int i = 0; i < row.length; i++)
                    ps.setObject(i + 1, row[i]);
                ps.executeUpdate();
            }
        }
    }

    private void addProductToDB(String title, String price, String brand, String shortDesc, String desc,
            String imgFile) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO products(title,price,brand,short_desc,description,image_file) VALUES(?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, title);
            ps.setString(2, price);
            ps.setString(3, brand);
            ps.setString(4, shortDesc);
            ps.setString(5, desc);
            ps.setString(6, imgFile);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            int newId = keys.next() ? keys.getInt(1) : -1;
            Product p = new Product(newId, title, price, brand, desc, shortDesc, imgFile);
            p.image = loadImage(imgFile);
            products.add(p);
            doSearch();
            JOptionPane.showMessageDialog(this, "Thêm sản phẩm thành công!", "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateProductInDB(int id, String title, String price, String brand, String shortDesc, String desc,
            String imgFile) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE products SET title=?,price=?,brand=?,short_desc=?,description=?,image_file=? WHERE id=?");
            ps.setString(1, title);
            ps.setString(2, price);
            ps.setString(3, brand);
            ps.setString(4, shortDesc);
            ps.setString(5, desc);
            ps.setString(6, imgFile);
            ps.setInt(7, id);
            ps.executeUpdate();
            for (Product p : products) {
                if (p.id == id) {
                    p.title = title;
                    p.price = price;
                    p.brand = brand;
                    p.shortDesc = shortDesc;
                    p.desc = desc;
                    p.imageFile = imgFile;
                    p.image = loadImage(imgFile);
                    break;
                }
            }
            doSearch();
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteProduct(int id) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM products WHERE id=?");
            ps.setInt(1, id);
            ps.executeUpdate();
            products.removeIf(p -> p.id == id);
            doSearch();
            JOptionPane.showMessageDialog(this, "Xóa thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Search product

    private void doSearch() {
        String kw = searchField.getText().trim().toLowerCase();
        filtered.clear();
        for (Product p : products) {
            if (kw.isEmpty() || p.title.toLowerCase().contains(kw)
                    || p.brand.toLowerCase().contains(kw)
                    || p.price.toLowerCase().contains(kw))
                filtered.add(p);
        }
        selectedIndex = 0;
        refreshGrid();
        if (!filtered.isEmpty())
            updateLeftPanel(0, false);
        else
            clearLeft();
    }

    // UI

    private void refreshGrid() {
        gridPanel.removeAll();
        for (int i = 0; i < filtered.size(); i++)
            gridPanel.add(createProductCard(i));
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void clearLeft() {
        mainTitleLabel.setText("Không tìm thấy");
        mainPriceLabel.setText("---");
        mainBrandLabel.setText("---");
        mainDescArea.setText("Không có sản phẩm phù hợp.");
        newImage = null;
        oldImage = null;
        mainImagePanel.repaint();
    }

    private JPanel createProductCard(int index) {
        Product p = filtered.get(index);

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

        JPanel topText = new JPanel();
        topText.setLayout(new BoxLayout(topText, BoxLayout.Y_AXIS));
        topText.setOpaque(false);

        JLabel title = new JLabel(p.title);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(new Color(70, 70, 70));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel(p.shortDesc != null ? p.shortDesc : "");
        subtitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        subtitle.setForeground(new Color(160, 160, 160));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        topText.add(title);
        topText.add(Box.createRigidArea(new Dimension(0, 4)));
        topText.add(subtitle);

        JLabel imgLabel = new JLabel();
        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        if (p.image != null)
            imgLabel.setIcon(new ImageIcon(p.image.getScaledInstance(160, 160, Image.SCALE_SMOOTH)));

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

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (selectedIndex != index) {
                    updateLeftPanel(index, true);
                    gridPanel.repaint();
                }
            }
        });
        return card;
    }

    private void updateLeftPanel(int newIndex, boolean animate) {
        Product p = filtered.get(newIndex);
        mainTitleLabel.setText(p.title);
        mainPriceLabel.setText(p.price);
        mainBrandLabel.setText(p.brand);
        mainDescArea.setText(p.desc);

        if (animate) {
            if (animationTimer != null && animationTimer.isRunning())
                animationTimer.stop();
            oldImage = filtered.get(selectedIndex).image;
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

    // Dialogs

    private void showAddDialog() {
        JTextField tfTitle = new JTextField(), tfPrice = new JTextField(),
                tfBrand = new JTextField("Adidas"), tfShort = new JTextField(), tfImg = new JTextField();
        JTextArea taDesc = new JTextArea(3, 20);
        taDesc.setLineWrap(true);
        taDesc.setWrapStyleWord(true);
        showProductDialog("Thêm sản phẩm mới", tfTitle, tfPrice, tfBrand, tfShort, taDesc, tfImg, null);
    }

    private void showEditDialog(Product p) {
        JTextField tfTitle = new JTextField(p.title), tfPrice = new JTextField(p.price),
                tfBrand = new JTextField(p.brand), tfShort = new JTextField(p.shortDesc != null ? p.shortDesc : ""),
                tfImg = new JTextField(p.imageFile != null ? p.imageFile : "");
        JTextArea taDesc = new JTextArea(p.desc != null ? p.desc : "", 3, 20);
        taDesc.setLineWrap(true);
        taDesc.setWrapStyleWord(true);
        showProductDialog("Sửa sản phẩm", tfTitle, tfPrice, tfBrand, tfShort, taDesc, tfImg, p);
    }

    private void showProductDialog(String dialogTitle, JTextField tfTitle, JTextField tfPrice,
            JTextField tfBrand, JTextField tfShort, JTextArea taDesc, JTextField tfImg, Product editing) {

        JDialog dialog = new JDialog(this, dialogTitle, true);
        dialog.setSize(450, 460);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String[] lbls = { "Tên sản phẩm *", "Giá (vd: $100.00) *", "Thương hiệu", "Mô tả ngắn", "Mô tả đầy đủ",
                "File ảnh" };
        Component[] flds = { tfTitle, tfPrice, tfBrand, tfShort, new JScrollPane(taDesc), null };

        JButton btnBrowse = new JButton("...");
        btnBrowse.addActionListener(e -> {
            JFileChooser fc = new JFileChooser(APP_DIR);
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image", "png", "jpg", "jpeg"));
            if (fc.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION)
                tfImg.setText(fc.getSelectedFile().getName());
        });

        for (int i = 0; i < lbls.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0;
            form.add(new JLabel(lbls[i]), gbc);
            gbc.gridx = 1;
            gbc.weightx = 1;
            if (i == lbls.length - 1) {
                JPanel row = new JPanel(new BorderLayout(4, 0));
                row.setBackground(Color.WHITE);
                row.add(tfImg, BorderLayout.CENTER);
                row.add(btnBrowse, BorderLayout.EAST);
                form.add(row, gbc);
            } else {
                form.add(flds[i], gbc);
            }
        }

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        btnRow.setBackground(Color.WHITE);
        JButton btnSave = btn(editing == null ? "Lưu" : "Cập nhật", new Color(46, 170, 100));
        JButton btnCancel = btn("Hủy", new Color(160, 160, 160));
        btnRow.add(btnCancel);
        btnRow.add(btnSave);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnRow, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            String t = tfTitle.getText().trim(), pr = tfPrice.getText().trim();
            if (t.isEmpty() || pr.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập Tên và Giá!", "Thiếu thông tin",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            String br = tfBrand.getText().trim().isEmpty() ? "Adidas" : tfBrand.getText().trim();
            dialog.dispose();
            if (editing == null)
                addProductToDB(t, pr, br, tfShort.getText().trim(), taDesc.getText().trim(), tfImg.getText().trim());
            else
                updateProductInDB(editing.id, t, pr, br, tfShort.getText().trim(), taDesc.getText().trim(),
                        tfImg.getText().trim());
        });

        dialog.setVisible(true);
    }

    // Util

    private BufferedImage loadImage(String imgFile) {
        if (imgFile == null || imgFile.isEmpty())
            return null;
        File[] candidates = {
                new File(imgFile),
                new File(APP_DIR, imgFile),
                new File(System.getProperty("user.dir"), imgFile),
                new File(System.getProperty("user.dir"), "Lab4/" + imgFile),
                new File(System.getProperty("user.dir"), "Lab4\\" + imgFile),
        };
        for (File f : candidates) {
            try {
                if (f.exists())
                    return ImageIO.read(f);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private JButton btn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(7, 14, 7, 14));
        return b;
    }

    private JLabel label(String text, int size, int style, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", style, size));
        l.setForeground(color);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new Main().setVisible(true);
        });
    }
}
