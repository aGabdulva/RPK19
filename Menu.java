package org.example;


import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Класс, реализующий экранную форму меню.
 */
public class Menu {
    private JFrame menu;
    private DefaultTableModel model;

    private JButton project;
    private JButton problem;
    private JButton client;
    private JButton employee;

    private JToolBar toolBar;
    private JScrollPane scroll;
    private JTable table;
    private JComboBox prjcts;
    private JTextField projectName;
    private JButton filter;
    private int count;
    private String filename = "./textfiles/empscsv.txt";

    public void show() {
        menu = new JFrame("Меню");
        menu.setSize(500,450);
        menu.setLocation(0, 0);
        menu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Создание кнопок и прикрепление иконок
        project = new JButton("Прокеты");
        problem = new JButton("Задачи");
        client = new JButton("Клиенты");
        employee = new JButton("Сотрудники");

       JPanel panel = new JPanel();
       panel.setLayout(null);

       employee.setBounds(150, 20, 200, 30);
       panel.add(employee);

       client.setBounds(150, 130, 200, 30);
       panel.add(client);

       project.setBounds(150, 240, 200, 30);
       panel.add(project);

       problem.setBounds(150, 350, 200, 30);
       panel.add(problem);

       menu.getContentPane().add(panel);

       //Добавление слушателей

        project.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                menu.setVisible(false);
                new ProjectList().show();
            }
        });

        problem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                menu.setVisible(false);
                new ProblemList().show();
            }
        });

        client.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                menu.setVisible(false);
                new ClientList().show();
            }
        });

        employee.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                menu.setVisible(false);
                new ManagerList().show();
            }
        });

       menu.setVisible(true);

    }

}
