package Backend;

import UI.Main;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class App {

    private static final List<JMenuItem> MENU_ITEMS = new ArrayList<>();
    public static final JFrame CIPHER_WINDOW = new JFrame("Главное меню");
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
        menuItem.addActionListener(actionEvent -> MAIN.newFile());
        MENU_ITEMS.add(menuItem);
        jMenu.add(menuItem);
        JMenuItem menuItem1 = new JMenuItem("Загрузить");
        menuItem1.addActionListener(actionEvent -> MAIN.loadFile());
        MENU_ITEMS.add(menuItem1);
        jMenu.add(menuItem1);
        JMenuItem menuItem2 = new JMenuItem("Сохранить");
        menuItem2.addActionListener(actionEvent -> MAIN.saveFile());
        MENU_ITEMS.add(menuItem2);
        jMenu.add(menuItem2);
        jMenu.addSeparator();
        JMenuItem menuItem3 = new JMenuItem("Выход");
        menuItem3.addActionListener(actionEvent -> System.exit(1));
        jMenu.add(menuItem3);
        jMenu.addSeparator();
        JMenuItem menuItem4 = new JMenuItem(new AboutAction());
        jMenu.add(menuItem4);
        return jMenu;
    }

    private static JMenu createKeyMenu() {
        JMenu jMenu = new JMenu("Управление сертификатами");
        JMenuItem menuItem = new JMenuItem("Импорт сертификата");
        menuItem.addActionListener(actionEvent -> MAIN.importCert());
        jMenu.add(menuItem);
        JMenuItem menuItem1 = new JMenuItem("Удалить сертификат");
        menuItem1.addActionListener(actionEvent -> MAIN.removeCert());
        MENU_ITEMS.add(menuItem1);
        jMenu.add(menuItem1);
        return jMenu;
    }

    public static void enableMenuItems(boolean enabled) {
        MENU_ITEMS.forEach(jMenuItem -> jMenuItem.setEnabled(enabled));
    }

    private static class AboutAction extends AbstractAction {
        AboutAction() {
            putValue(NAME, "О программе");
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            JOptionPane.showMessageDialog(null, "Лабораторную выполнил Анкушев А.Д.\n" +
                    "Группа А-05-16\n" +
                    "Вариант №1\n" +
                    "\n" +
                    "Алгоритм хеширования документа : MD5\n" +
                    "Алгоритм подписи документа : RSA\n" +
                    "Алгоритм хеширования открытого ключа : MD5\n" +
                    "Алгоритм подписи открытого ключа : RSA\n" +
                    "МЭИ, 2020.", "Лабораторная №1", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
