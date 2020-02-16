package Backend;

import UI.Main;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class App {

    private static final List<JMenuItem> MENU_ITEMS = new ArrayList<>();
    public static final JFrame CIPHER_WINDOW = new JFrame("Auth window");
    public static final Main MAIN = new Main(CIPHER_WINDOW);

    public static void main(String[] args) {
        CIPHER_WINDOW.setContentPane(MAIN.getMainPanel());
        CIPHER_WINDOW.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        menuBar.add(createKeyMenu());
        enableMenuItems(false);
        CIPHER_WINDOW.setJMenuBar(menuBar);
        CIPHER_WINDOW.pack();
        CIPHER_WINDOW.setSize(700, 500);
        CIPHER_WINDOW.setResizable(false);
        CIPHER_WINDOW.setVisible(true);
    }

    private static JMenu createFileMenu() {
        JMenu jMenu = new JMenu("Файл");
        JMenuItem menuItem = new JMenuItem("Создать");
        MENU_ITEMS.add(menuItem);
        jMenu.add(menuItem);
        JMenuItem menuItem1 = new JMenuItem("Загрузить");
        menuItem1.addActionListener(actionEvent -> {
            MAIN.loadFile();
        });
        MENU_ITEMS.add(menuItem1);
        jMenu.add(menuItem1);
        JMenuItem menuItem2 = new JMenuItem("Сохранить");
        menuItem2.addActionListener(actionEvent -> {
            MAIN.saveFile();
        });
        MENU_ITEMS.add(menuItem2);
        jMenu.add(menuItem2);
        jMenu.addSeparator();
        JMenuItem menuItem3 = new JMenuItem("Выход");
        menuItem3.addActionListener(actionEvent -> System.exit(1));
        jMenu.add(menuItem3);
        jMenu.addSeparator();
        jMenu.add(new JMenuItem("О программе"));
        return jMenu;
    }

    private static JMenu createKeyMenu() {
        JMenu jMenu = new JMenu("Управление ключами");
        JMenuItem menuItem = new JMenuItem("Экспорт открытого ключа");
        menuItem.addActionListener(actionEvent -> MAIN.savePublicKey());
        MENU_ITEMS.add(menuItem);
        jMenu.add(menuItem);
        JMenuItem menuItem1 = new JMenuItem("Импорт открытого ключа");
        menuItem1.addActionListener(actionEvent -> MAIN.importPublicKey());
        MENU_ITEMS.add(menuItem1);
        jMenu.add(menuItem1);
        JMenuItem menuItem2 = new JMenuItem("Удаление пары ключей");
        menuItem2.addActionListener(actionEvent -> {
            MAIN.removeKeys();
            enableMenuItems(false);
            MAIN.requestTextfieldFocus();
        });
        MENU_ITEMS.add(menuItem2);
        jMenu.add(menuItem2);
        return jMenu;
    }

    public static void enableMenuItems(boolean enabled) {
        MENU_ITEMS.forEach(jMenuItem -> jMenuItem.setEnabled(enabled));
    }
}
