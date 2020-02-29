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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Enumeration;

import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;

public class Main extends JFrame {
    public static final String PUBLICKEY = "publickey";
    public static final String CERT = "user";
    public static final String SIGNATURE = "signature";
    public static final String DATA = "data";
    private JPanel mainPanel;
    private JTextField usernameField;
    private JTextArea textArea;

    private final JFrame cipherWindow;

    private String currentUser;

    private Key certPrivateKey;
    private PublicKey certPublicKey;
    private X509Certificate certificate;

    public Main(JFrame cipherWindow) {
        this.cipherWindow = cipherWindow;
        textArea.setEnabled(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        usernameField.setEnabled(false);
    }

    public void importCert() {
        JFileChooser fileChooser = new JFileChooser("C:\\Users\\bass4x4\\IdeaProjects\\digital-signature");
        fileChooser.setFileFilter(new FileNameExtensionFilter(".pfx", "pfx"));
        fileChooser.setDialogTitle("Импортировать сертификат");
        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToImport = fileChooser.getSelectedFile();
            if (fileToImport.exists()) {
                try (FileInputStream stream = new FileInputStream(fileToImport)) {
                    KeyStore keyStore = KeyStore.getInstance("pkcs12", "SunJSSE");
                    keyStore.load(stream, "123".toCharArray());
                    Enumeration<String> aliases = keyStore.aliases();
                    while (aliases.hasMoreElements()) {
                        String alias = aliases.nextElement();
                        certPrivateKey = keyStore.getKey(alias, "123".toCharArray());
                        certificate = (X509Certificate) keyStore.getCertificate(alias);
                        certPublicKey = certificate.getPublicKey();
                        String issuerDN = certificate.getSubjectDN().toString();
                        cipherWindow.setTitle("Автор сертификата: " + issuerDN);
                        textArea.setEnabled(true);
                        App.enableMenuItems(true);
                        usernameField.setText(issuerDN);
                        textArea.setText("");
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Проверьте сертификат!");
                    removeCert();
                }
            } else {
                JOptionPane.showMessageDialog(null, "Выберите имя файла!");
            }
        }
    }

    public void removeCert() {
        textArea.setEnabled(false);
        textArea.setText("");
        usernameField.setText("");
        currentUser = null;
        certPrivateKey = null;
        certPublicKey = null;
        App.enableMenuItems(false);
        cipherWindow.setTitle("Главное меню");
    }

    public void saveFile() {
        try {
            MessageDigest mdHashFunction = MessageDigest.getInstance("MD2");
            byte[] hashedData = mdHashFunction.digest(textArea.getText().getBytes());
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, certPrivateKey);
            byte[] bytes = cipher.doFinal(hashedData);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(CERT, certificate.hashCode());
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

    public void loadFile() {
        try {
            if (certPublicKey != null) {
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
                        cipher.init(Cipher.DECRYPT_MODE, certPublicKey);
                        byte[] decr = cipher.doFinal(signature);
                        int u = object.getInt(CERT);
                        String d = object.getString(DATA);
                        MessageDigest mdHashFunction = MessageDigest.getInstance("MD2");
                        byte[] hashedData = mdHashFunction.digest(d.getBytes());
                        if (u == certificate.hashCode() && Arrays.equals(hashedData, decr)) {
                            textArea.setText(d);
                        } else {
                            JOptionPane.showMessageDialog(null, "Проверьте сертификат!");
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Выберите имя файла!");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null, "Выберите сертификат!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Проверьте сертификат!");
        }
    }

    public void newFile() {
        textArea.setText("");
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
        mainPanel.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        usernameField = new JTextField();
        mainPanel.add(usernameField, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Имя пользователя:");
        mainPanel.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        mainPanel.add(scrollPane1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        textArea = new JTextArea();
        scrollPane1.setViewportView(textArea);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}

