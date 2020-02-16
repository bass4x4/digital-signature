package Backend;

import UI.Main;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        JFrame cipherWindow = new JFrame("Auth window");
        cipherWindow.setContentPane(new Main().getMainPanel());
        cipherWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        menuBar.add(createKeyMenu());
        cipherWindow.setJMenuBar(menuBar);
        cipherWindow.pack();
        cipherWindow.setSize(700, 500);
        cipherWindow.setResizable(false);
        cipherWindow.setVisible(true);
    }

    private static JMenu createFileMenu() {
        JMenu jMenu = new JMenu("Файл");
        jMenu.add(new JMenuItem("Открыть"));
        jMenu.add(new JMenuItem("Загрузить"));
        jMenu.add(new JMenuItem("Сохранить"));
        jMenu.addSeparator();
        jMenu.add(new JMenuItem("Выход"));
        jMenu.addSeparator();
        jMenu.add(new JMenuItem("О программе"));
        return jMenu;
    }

    private static JMenu createKeyMenu() {
        JMenu jMenu = new JMenu("Управление ключами");
        jMenu.add(new JMenuItem("Экспорт открытого ключа"));
        jMenu.add(new JMenuItem("Импорт открытого ключа"));
        jMenu.add(new JMenuItem("Удаление пары ключей"));
        jMenu.add(new JMenuItem("Выбор закрытого ключа"));
        return jMenu;
    }
}
