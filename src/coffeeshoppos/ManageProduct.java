/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package coffeeshoppos;

/**
 *
 * @author denal
 */

import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import coffeeshoppos.DBConnection;

public class ManageProduct extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ManageProduct.class.getName());

    /**
     * Creates new form ManageProduct
     */
    Connection con;
    PreparedStatement pst;
    ResultSet rs;
    byte[] productImage = null;
    String userRole;
    
    
    public ManageProduct() {
        initComponents();
        con = DBConnection.getConnection();
        
        this.userRole = "admin";// role
        LoadTable();

        // Role-based security
        if (!"admin".equals(userRole)) {
            JOptionPane.showMessageDialog(this, "Access denied! Only admin can manage products.");
            this.dispose();
        }
    }
    


    public void LoadTable() {
        try {
            pst = con.prepareStatement("SELECT product_id, product_name, category, price, stock_quantity FROM tbl_product");
            rs = pst.executeQuery();
            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            model.setRowCount(0);
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getString("category"),
                    rs.getDouble("price"),
                    rs.getInt("stock_quantity")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error Loading Table: " + e.getMessage());
        }
    }
    private void loadProducts() {
        try {
            pst = con.prepareStatement("SELECT product_id, product_name, category, price, stock_quantity FROM tbl_product");
            rs = pst.executeQuery();

            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            model.setRowCount(0);

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getString("category"),
                    rs.getDouble("price"),
                    rs.getInt("stock_quantity")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading products: " + e.getMessage());
        }
    }
    
    
    
    private void uploadImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images", "jpg", "png", "jpeg"));
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            try (FileInputStream fis = new FileInputStream(f); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                byte[] buf = new byte[1024];
                for (int readNum; (readNum = fis.read(buf)) != -1;) {
                    bos.write(buf, 0, readNum);
                }
                productImage = bos.toByteArray();

                ImageIcon icon = new ImageIcon(new ImageIcon(productImage).getImage()
                        .getScaledInstance(lbl_img.getWidth(), lbl_img.getHeight(), java.awt.Image.SCALE_SMOOTH));
                lbl_img.setIcon(icon);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Image Upload Error: " + e.getMessage());
            }
        }
    }

    private void addProduct() {
        if (!validateFields())
            return;
        
        if (isDuplicateProduct(fld_name.getText().trim(), null)) {
            JOptionPane.showMessageDialog(this, "Product name already exists!");
            return;
        }
        
        try {
            pst = con.prepareStatement("INSERT INTO tbl_product (product_name, category, price, stock_quantity, image) VALUES (?,?,?,?,?)");
            pst.setString(1, fld_name.getText());
            pst.setString(2, (String) jComboBoxCategory.getSelectedItem());
            pst.setDouble(3, Double.parseDouble(fld_price.getText()));
            pst.setInt(4, Integer.parseInt(fld_qty.getText()));
            pst.setBytes(5, productImage);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Product Added!");
            loadProducts();
            clearFields();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error Adding Product: " + e.getMessage());
            clearFields();
        }
    }

    private void updateProduct() {
        
        if (!validateFields())
            return;
        
        if (lbl_prodID.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a product to update.");
            return;
        }

        int productId = Integer.parseInt(lbl_prodID.getText());

        //  Duplicate checker
        if (isDuplicateProduct(fld_name.getText().trim(), productId)) {
            JOptionPane.showMessageDialog(this, "Another product with this name already exists!");
            return;
        }
        
        try {
            pst = con.prepareStatement("UPDATE tbl_product SET product_name=?, category=?, price=?, stock_quantity=?, image=? WHERE product_id=?");
            pst.setString(1, fld_name.getText());
            pst.setString(2, (String) jComboBoxCategory.getSelectedItem());
            pst.setDouble(3, Double.parseDouble(fld_price.getText()));
            pst.setInt(4, Integer.parseInt(fld_qty.getText()));
            pst.setBytes(5, productImage);
            pst.setInt(6, Integer.parseInt(lbl_prodID.getText()));
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Product Updated!");
            loadProducts();
            clearFields();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error Updating Product: " + e.getMessage());
            clearFields();
        }
    }

    private void deleteProduct() {
        
        
        try {
            pst = con.prepareStatement("DELETE FROM tbl_product WHERE product_id=?");
            pst.setInt(1, Integer.parseInt(lbl_prodID.getText()));
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Product Deleted!");
            loadProducts();
            clearFields();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error Deleting Product: " + e.getMessage());
            clearFields();
        }
    }

    private void findProduct() {
        try {
            pst = con.prepareStatement("SELECT * FROM tbl_product WHERE product_id=?");
            pst.setInt(1, Integer.parseInt(fld_search.getText()));
            rs = pst.executeQuery();

            if (rs.next()) {
                lbl_prodID.setText(rs.getString("product_id"));
                fld_name.setText(rs.getString("product_name"));
                jComboBoxCategory.setSelectedItem(rs.getString("category"));
                fld_price.setText(rs.getString("price"));
                fld_qty.setText(rs.getString("stock_quantity"));
                productImage = rs.getBytes("image");

                if (productImage != null) {
                    ImageIcon icon = new ImageIcon(new ImageIcon(productImage).getImage()
                            .getScaledInstance(lbl_img.getWidth(), lbl_img.getHeight(), java.awt.Image.SCALE_SMOOTH));
                    lbl_img.setIcon(icon);
                } else {
                    lbl_img.setIcon(null);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Product not found!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error Finding Product: " + e.getMessage());
        }
    }
    
    private void clearFields() {
        lbl_prodID.setText("");
        fld_name.setText("");
        jComboBoxCategory.setSelectedIndex(-1);
        fld_price.setText("");
        fld_qty.setText("");
        lbl_img.setIcon(null);
        productImage = null;
    }
    
    private void tableRowClicked() {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        int selectedRow = jTable1.getSelectedRow();

        if (selectedRow != -1) {
            
            lbl_prodID.setText(model.getValueAt(selectedRow, 0).toString());
            fld_name.setText(model.getValueAt(selectedRow, 1).toString());
            jComboBoxCategory.setSelectedItem(model.getValueAt(selectedRow, 2).toString());
            fld_price.setText(model.getValueAt(selectedRow, 3).toString());
            fld_qty.setText(model.getValueAt(selectedRow, 4).toString());

            
            try {
                int id = Integer.parseInt(lbl_prodID.getText());
                PreparedStatement pst = con.prepareStatement("SELECT image FROM tbl_product WHERE product_id = ?");
                pst.setInt(1, id);
                ResultSet rs = pst.executeQuery();

                if (rs.next()) {
                    byte[] imageBytes = rs.getBytes("image");
                    if (imageBytes != null) {
                        productImage = imageBytes; // keep it stored for update
                        ImageIcon icon = new ImageIcon(new ImageIcon(imageBytes)
                                .getImage().getScaledInstance(lbl_img.getWidth(), lbl_img.getHeight(), java.awt.Image.SCALE_SMOOTH));
                        lbl_img.setIcon(icon);
                    } else {
                        lbl_img.setIcon(null);
                        productImage = null;
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error loading image: " + e.getMessage());
            }
        }
    }
    
    private boolean validateFields() {
        if (fld_name.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Product name is required.");
            fld_name.requestFocus();
            return false;
        }

        if (jComboBoxCategory.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a category.");
            jComboBoxCategory.requestFocus();
            return false;
        }

        if (fld_price.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Price is required.");
            fld_price.requestFocus();
            return false;
        }

        try {
            double price = Double.parseDouble(fld_price.getText());
            if (price < 0) {
                JOptionPane.showMessageDialog(this, "Price cannot be negative.");
                fld_price.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for price.");
            fld_price.requestFocus();
            return false;
        }

        if (fld_qty.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Quantity is required.");
            fld_qty.requestFocus();
            return false;
        }

        try {
            int qty = Integer.parseInt(fld_qty.getText());
            if (qty < 0) {
                JOptionPane.showMessageDialog(this, "Quantity cannot be negative.");
                fld_qty.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for quantity.");
            fld_qty.requestFocus();
            return false;
        }

        // Optional: require image when adding new product
        if (productImage == null && lbl_prodID.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please upload a product image.");
            return false;
        }

        return true;
    }
    
    private boolean isDuplicateProduct(String productName, Integer excludeId) {
        try {
            String query = "SELECT COUNT(*) FROM tbl_product WHERE product_name = ?";
            if (excludeId != null) {
                query += " AND product_id != ?"; // exclude current ID when updating
            }

            PreparedStatement checkStmt = con.prepareStatement(query);
            checkStmt.setString(1, productName);
            if (excludeId != null) {
                checkStmt.setInt(2, excludeId);
            }

            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0; // duplicate found
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error checking duplicate: " + e.getMessage());
        }
        return false;
    }
    
    private void filterProducts() {
        String selectedCategory = jcmbFilter.getSelectedItem().toString();

        Connection conn = DBConnection.getConnection();
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0); // Clear table

        String query;
        if (selectedCategory.equals("All Products")) {
            query = "SELECT * FROM tbl_product";
        } else {
            query = "SELECT * FROM tbl_product WHERE category = ?";
        }

        try (PreparedStatement pst = conn.prepareStatement(query)) {
            if (!selectedCategory.equals("All Products")) {
                pst.setString(1, selectedCategory);
            }

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("product_id"),
                    rs.getString("product_name"),
                    rs.getString("price"),
                    rs.getString("category")
                        ,rs.getString("image_path")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error filtering products: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        fld_search = new javax.swing.JTextField();
        fld_name = new javax.swing.JTextField();
        fld_price = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        lbl_img = new javax.swing.JLabel();
        btn_upload = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        btn_find = new javax.swing.JButton();
        btn_add = new javax.swing.JButton();
        btn_update = new javax.swing.JButton();
        btn_delete = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        fld_qty = new javax.swing.JTextField();
        jComboBoxCategory = new javax.swing.JComboBox<>();
        lbl_prodID = new javax.swing.JLabel();
        jcmbFilter = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Name", "Category", "Price", "Quantity", "Image"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("Search ID:");

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Name:");

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Category:");

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("Price:");

        fld_name.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fld_nameActionPerformed(evt);
            }
        });

        fld_price.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fld_priceActionPerformed(evt);
            }
        });

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("Image");

        lbl_img.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        btn_upload.setText("upload");
        btn_upload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_uploadActionPerformed(evt);
            }
        });

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText("Product ID:");

        btn_find.setText("find");
        btn_find.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_findActionPerformed(evt);
            }
        });

        btn_add.setText("ADD");
        btn_add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_addActionPerformed(evt);
            }
        });

        btn_update.setText("UPDATE");
        btn_update.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_updateActionPerformed(evt);
            }
        });

        btn_delete.setText("DELETE");
        btn_delete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_deleteActionPerformed(evt);
            }
        });

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel8.setText("Category");

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setText("Qty:");

        fld_qty.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fld_qtyActionPerformed(evt);
            }
        });

        jComboBoxCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Hot Coffee", "Cold Coffee", "Frappuccino", "Refresher", "Snacks" }));
        jComboBoxCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxCategoryActionPerformed(evt);
            }
        });

        jcmbFilter.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All Products", "Hot Coffee", "Cold Coffee", "Frappuccino", "Refresher", "Snacks" }));
        jcmbFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcmbFilterActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(31, 31, 31)
                                .addComponent(lbl_img, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(jComboBoxCategory, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(fld_price, javax.swing.GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
                                    .addComponent(fld_name, javax.swing.GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
                                    .addComponent(fld_qty, javax.swing.GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
                                    .addComponent(lbl_prodID, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(btn_upload)
                                .addGap(155, 155, 155))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(fld_search)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btn_find)
                                .addGap(12, 12, 12))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(15, 15, 15)
                                .addComponent(btn_add, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btn_update, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btn_delete, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jcmbFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 532, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(fld_search, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btn_find))
                        .addGap(29, 29, 29)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lbl_prodID, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(fld_name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBoxCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(fld_price, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(fld_qty, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lbl_img, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btn_upload)
                        .addGap(35, 35, 35))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 489, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_add, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_update, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_delete, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(jcmbFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(81, 81, 81))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fld_priceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fld_priceActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_fld_priceActionPerformed

    private void btn_findActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_findActionPerformed
        findProduct();
    }//GEN-LAST:event_btn_findActionPerformed

    private void fld_nameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fld_nameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_fld_nameActionPerformed

    private void fld_qtyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fld_qtyActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_fld_qtyActionPerformed

    private void btn_addActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_addActionPerformed
        addProduct();
    }//GEN-LAST:event_btn_addActionPerformed

    private void btn_updateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_updateActionPerformed
        updateProduct();     
    }//GEN-LAST:event_btn_updateActionPerformed

    private void btn_deleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_deleteActionPerformed
        deleteProduct();
    }//GEN-LAST:event_btn_deleteActionPerformed

    private void btn_uploadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_uploadActionPerformed
        uploadImage();
    }//GEN-LAST:event_btn_uploadActionPerformed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        tableRowClicked();
    }//GEN-LAST:event_jTable1MouseClicked

    private void jComboBoxCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxCategoryActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBoxCategoryActionPerformed

    private void jcmbFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcmbFilterActionPerformed
        filterProducts();
    }//GEN-LAST:event_jcmbFilterActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new ManageProduct().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_add;
    private javax.swing.JButton btn_delete;
    private javax.swing.JButton btn_find;
    private javax.swing.JButton btn_update;
    private javax.swing.JButton btn_upload;
    private javax.swing.JTextField fld_name;
    private javax.swing.JTextField fld_price;
    private javax.swing.JTextField fld_qty;
    private javax.swing.JTextField fld_search;
    private javax.swing.JComboBox<String> jComboBoxCategory;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JComboBox<String> jcmbFilter;
    private javax.swing.JLabel lbl_img;
    private javax.swing.JLabel lbl_prodID;
    // End of variables declaration//GEN-END:variables

    private void conn() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
