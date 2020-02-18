package UI;

import Backend.App;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.crypto.Cipher;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;

public class Main extends JFrame {
    public static final String PUBLICKEY = "publickey";
    public static final String USER = "user";
    public static final String SIGNATURE = "signature";
    public static final String DATA = "data";
    private JPanel mainPanel;
    private JTextField usernameField;
    private JButton chooseUserButton;
    private JTextArea textArea;

    private final JFrame cipherWindow;

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private String currentUser;

    private String importedUser;
    private PublicKey importedKey;

    public Main(JFrame cipherWindow) {
        this.cipherWindow = cipherWindow;
        textArea.setEnabled(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        chooseUserButton.addActionListener(actionEvent -> {
            String username = usernameField.getText();
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Имя пользователя не должно быть пустым.");
            } else {
                textArea.requestFocus();
                usernameField.setEnabled(false);
                currentUser = usernameField.getText();
                cipherWindow.setTitle("Подписанный документ");
                App.enableMenuItems(true);
                textArea.setEnabled(true);
                generateKeyPair();
                chooseUserButton.setEnabled(false);
            }
        });
        usernameField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    chooseUserButton.doClick();
                }
            }
        });
    }

    public void requestTextfieldFocus() {
        usernameField.requestFocus();
    }

    public void savePublicKey() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(USER, currentUser);
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey.getEncoded());
        String base64String = encodeBase64String(x509EncodedKeySpec.getEncoded());
        jsonObject.put(PUBLICKEY, base64String);
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter(".ps", "ps"));
        fileChooser.setDialogTitle("Сохранить открытый ключ");
        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            if (!fileChooser.getSelectedFile().getName().isEmpty()) {
                try {
                    File file = new File(fileChooser.getSelectedFile().getAbsoluteFile() + ".ps");
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    fileOutputStream.write(jsonObject.toString().getBytes());
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(null, "Выберите имя файла!");
            }
        }
    }

    public void importPublicKey() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter(".ps", "ps"));
        fileChooser.setDialogTitle("Импортировать открытый ключ");
        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToImport = fileChooser.getSelectedFile();
            if (fileToImport.exists()) {
                try {
                    String data = Files.asCharSource(fileToImport, Charsets.UTF_8).read();
                    JSONParser jsonParser = new JSONParser();
                    org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject) jsonParser.parse(data);
                    JSONObject object = new JSONObject(jsonObject.toJSONString());
                    byte[] keyBytes = decodeBase64(object.getString(PUBLICKEY));
                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                    X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(keyBytes);
                    importedKey = keyFactory.generatePublic(publicKeySpec);
                    importedUser = object.getString(USER);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(null, "Выберите имя файла!");
            }
        }
    }

    public void loadFile() {
        try {
            if (importedKey != null && importedUser != null) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter(".sd", "sd"));
                fileChooser.setDialogTitle("Импортировать файл");
                int userSelection = fileChooser.showSaveDialog(null);
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File fileToImport = fileChooser.getSelectedFile();
                    if (fileToImport.exists()) {
                        String data = Files.asCharSource(fileToImport, Charsets.UTF_8).read();
                        JSONParser jsonParser = new JSONParser();
                        org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject) jsonParser.parse(data);
                        JSONObject object = new JSONObject(jsonObject.toJSONString());
                        byte[] signature = decodeBase64(object.getString(SIGNATURE));
                        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                        cipher.init(Cipher.DECRYPT_MODE, importedKey);
                        byte[] decr = cipher.doFinal(signature);
                        String u = object.getString(USER);
                        String d = object.getString(DATA);
                        MessageDigest mdHashFunction = MessageDigest.getInstance("MD2");
                        byte[] hashedData = mdHashFunction.digest(d.getBytes());
                        if (u.equals(importedUser) && Arrays.equals(hashedData, decr)) {
                            textArea.setText(d);
                            cipherWindow.setTitle(cipherWindow.getTitle() + String.format(" Автор документа: %s", u));
                        } else {
                            JOptionPane.showMessageDialog(null, "");
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Выберите имя файла!");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null, "Выберите ключ!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Проверьте ключ!");
        }
    }

    public void newFile() {
        textArea.setText("");
        cipherWindow.setTitle(cipherWindow.getTitle() + String.format(" Автор документа: %s", currentUser));
    }

    public void saveFile() {
        try {
            MessageDigest mdHashFunction = MessageDigest.getInstance("MD2");
            byte[] hashedData = mdHashFunction.digest(textArea.getText().getBytes());
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            byte[] bytes = cipher.doFinal(hashedData);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(USER, currentUser);
            jsonObject.put(SIGNATURE, encodeBase64String(bytes));
            jsonObject.put(DATA, textArea.getText());
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter(".sd", "sd"));
            fileChooser.setDialogTitle("Сохранить файл");
            int userSelection = fileChooser.showSaveDialog(null);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                if (!fileChooser.getSelectedFile().getName().isEmpty()) {
                    try {
                        File file = new File(fileChooser.getSelectedFile().getAbsoluteFile() + ".sd");
                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        fileOutputStream.write(jsonObject.toString().getBytes());
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Выберите имя файла!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }

    public void removeKeys() {
        privateKey = null;
        publicKey = null;
        textArea.setEnabled(true);
        textArea.setText("");
        usernameField.setText("");
        usernameField.setEnabled(true);
        currentUser = null;
        importedKey = null;
        importedUser = null;
        chooseUserButton.setEnabled(true);
        cipherWindow.setTitle("Главное меню");
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }


    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        usernameField = new JTextField();
        mainPanel.add(usernameField, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Имя пользователя:");
        mainPanel.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        mainPanel.add(scrollPane1, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        textArea = new JTextArea();
        scrollPane1.setViewportView(textArea);
        chooseUserButton = new JButton();
        chooseUserButton.setText("Выбрать пользователя");
        mainPanel.add(chooseUserButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }


    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}

