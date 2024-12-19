package org.example;

import java.awt.*;
import java.awt.event.*;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.*;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.query.NativeQuery;
import org.w3c.dom.*;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
/**
 * Класс, реализующий экранную форму списка клиентов.
 */
public class ClientList {
    private static final Logger logger = LogManager.getLogger(ClientList.class);
    private JFrame clientList;
    private DefaultTableModel model;

    private JButton clnt;
    private JButton edit;
    private JButton dbplus;
    private JButton dbmin;
    private JButton savef;
    private JButton openf;
    private JButton back;

    private JToolBar toolBar;
    private JScrollPane scroll;
    private JTable table;
    EntityManagerFactory emf = null;
    EntityManager em = null;

    public static boolean isValidEmail(String email) {
        return email.matches("^[\\w-\\.]+@[\\w-]+(\\.[\\w-]+)*\\.[a-z]{2,}$");
    }

    public static boolean isValidName(String name){
        return name.matches("^([\\p{L}\\p{M}0-9][\\p{L}\\p{M}0-9_-]*[\\p{L}\\p{M}0-9])(?:\\s([\\p{L}\\p{M}0-9][\\p{L}\\p{M}0-9_-]*[\\p{L}\\p{M}0-9]))*$");
    }


    public static void printMethod(String XMLFile, String JRXMLFile, String Result) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(XMLFile));
            document.getDocumentElement().normalize();
            JasperReport jasperReport = JasperCompileManager.compileReport(JRXMLFile);
            JRXmlDataSource dataSource = new JRXmlDataSource(document, "/clientList/cln");
            Map<String, Object> parameters = new HashMap<>();
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            if (Result.toLowerCase().endsWith("pdf")){
                JasperExportManager.exportReportToPdfFile(jasperPrint, Result);
            } else {
                JasperExportManager.exportReportToHtmlFile(jasperPrint, Result);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void show() {
        emf = Persistence.createEntityManagerFactory("manager_persistence");
        em = emf.createEntityManager();
        logger.info("Открытие экранной формы списка клиентов");
        clientList = new JFrame("Список клиентов");
        clientList.setSize(1150, 600);
        clientList.setLocation(0, 0);
        clientList.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Создание кнопок и прикрепление иконок
        clnt = new JButton(new ImageIcon("./img/emp.png"));
        edit = new JButton(new ImageIcon("./img/edit.png"));
        dbplus = new JButton(new ImageIcon("./img/edit_add.png"));
        dbmin = new JButton(new ImageIcon("./img/edit_remove.png"));
        savef = new JButton(new ImageIcon("./img/document-save-as.png"));
        openf = new JButton(new ImageIcon("./img/folder_yellow_open.png"));
        back = new JButton(new ImageIcon("./img/back.png"));

        //Настройка подсказок для кнопок
        clnt.setToolTipText("Вывод списка клиентов");
        edit.setToolTipText("Редактирование информации о клиенте");
        dbplus.setToolTipText("Добавление информации о клиенте");
        dbmin.setToolTipText("Удаление информации о клиенте");
        savef.setToolTipText("Сохранение файла");
        back.setToolTipText("Возврат к меню");

        //Добавление кнопок на панель инструментов
        toolBar = new JToolBar("Панель инструментов");
        toolBar.add(clnt);
        toolBar.add(edit);
        toolBar.add(dbplus);
        toolBar.add(dbmin);
        toolBar.add(savef);
        toolBar.add(openf);
        toolBar.add(back);

        //Размещение панели инструментов
        clientList.setLayout(new BorderLayout());
        clientList.add(toolBar, BorderLayout.NORTH);

        //Размещение таблицы с данными
        //Создание таблицы с данными
        String[] columns = {"Заказчик", "Контакты"};
        String[][] data = {};
        model = new DefaultTableModel(data, columns) {

            @Override
            public boolean isCellEditable(int row, int column) {
                //all cells false
                return false;
            }
        };
        table = new JTable(model);
        scroll = new JScrollPane(table);
        clientList.add(scroll, BorderLayout.CENTER);
        model.setRowCount(0);
        em.getTransaction().begin();
        NativeQuery<Client> query = (NativeQuery<Client>) em.createNativeQuery("SELECT * FROM customer", Client.class);
        List<Client> resultlist = query.list();
        em.getTransaction().commit();
        String s1, s2;
        for (Client i : resultlist) {
            s1 = i.getCustomerName();
            s2 = i.getCustomerContacts();
            String[] adds = new String[]{s1, s2};
            model.addRow(adds);
        }


            //Слушатели кнопок
            back.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    logger.info("Закрыта экранная форма списка клиентов");
                    clientList.setVisible(false);
                    new Menu().show();
                }
            });




            //Добавление информации о клиенте
            dbplus.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int ev = 0;
                    int nv = 0;
                    String s1 = (String) JOptionPane.showInputDialog(clientList, "Введите название заказчика");
                    while (nv == 0){
                        if (isValidName(s1)){
                            nv = 1;
                        }
                        else {
                            s1 = (String) JOptionPane.showInputDialog(clientList, "Введен неверный формат названия. \n Введите название заказчика");
                        }
                    }
                    String s2 = (String) JOptionPane.showInputDialog(clientList, "Введите контакты заказчика");
                    while (ev == 0){
                        if (isValidEmail(s2)){
                            ev = 1;
                        }
                        else {
                            s2 = (String) JOptionPane.showInputDialog(clientList, "Введен неверный формат почты. \n Введите контакты заказчика");
                        }
                    }
                    String[] adds = new String[]{s1, s2};
                    model.addRow(adds);
                    em.getTransaction().begin();
                    Client cln = new Client();
                    cln.setCustomerName(s1);
                    cln.setCustomerContacts(s2);
                    em.persist(cln);
                    em.getTransaction().commit();
                    logger.info("Добавлен клиент: {}", s1);
                }
            });
            //Удаление информации о клиенте
            dbmin.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    int rowindex = table.getSelectedRow();

                    int confirm = JOptionPane.showConfirmDialog(
                            clientList,
                            "Вы уверены, что хотите удалить заказчика " +  model.getValueAt(rowindex, 0) + "?",
                            "Подтверждение удаления",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                    );

                    // Обрабатываем выбор пользователя
                    if (confirm == JOptionPane.YES_OPTION) {
                        // Пользователь подтвердил удаление
                        em.getTransaction().begin();
                        NativeQuery<Client> query = (NativeQuery<Client>) em.createNativeQuery("SELECT * FROM customer", Client.class);
                        List<Client> resultlist = query.list();
                        int ide = resultlist.get(rowindex).getIdCustomer();
                        String clientName = resultlist.get(rowindex).getCustomerName();
                        logger.info("Удален клиент: {}", clientName);
                        em.createNativeQuery("DELETE FROM manager.customer WHERE id_customer = :id")
                                .setParameter("id", ide)
                                .executeUpdate();
                        em.getTransaction().commit();
                        model.removeRow(rowindex);

                        JOptionPane.showMessageDialog(
                                clientList,
                                "Заказчик успешно удален!",
                                "Успех",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    } else {
                        // Пользователь отменил удаление
                        JOptionPane.showMessageDialog(
                                clientList,
                                "Удаление отменено.",
                                "Отмена",
                                JOptionPane.WARNING_MESSAGE
                        );
                    }
                }
            });
            edit.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int rowindex = table.getSelectedRow();
                    int ev = 0;
                    int nv = 0;
                    String s1 = (String) JOptionPane.showInputDialog(clientList, "Введите название заказчика");
                    while (nv == 0){
                        if (isValidName(s1)){
                            nv = 1;
                        }
                        else {
                            s1 = (String) JOptionPane.showInputDialog(clientList, "Введен неверный формат названия. \n Введите название заказчика");
                        }
                    }
                    String s2 = (String) JOptionPane.showInputDialog(clientList, "Введите контакты заказчика");
                    while (ev == 0){
                        if (isValidEmail(s2)){
                            ev = 1;
                        }
                        else {
                            s2 = (String) JOptionPane.showInputDialog(clientList, "Введен неверный формат почты. \n Введите контакты заказчика");
                        }
                    }
                    model.removeRow(rowindex);
                    model.insertRow(rowindex, new Object[]{s1, s2});
                    em.getTransaction().begin();
                    NativeQuery<Client> query = (NativeQuery<Client>) em.createNativeQuery("SELECT * FROM customer", Client.class);
                    List<Client> resultlist = query.list();
                    int ide = resultlist.get(rowindex).getIdCustomer();
                    String clientName = resultlist.get(rowindex).getCustomerName();
                    logger.info("Изменены данные о клиенте: {}", clientName);
                    em.createNativeQuery("UPDATE manager.customer SET customer_name = :name, customer_contacts = :contacts WHERE id_customer = :id")
                            .setParameter("id", ide)
                            .setParameter("name", s1)
                            .setParameter("contacts", s2)
                            .executeUpdate();
                    em.getTransaction().commit();
                }
            });

            clnt.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int rowindex = table.getSelectedRow();
                    NativeQuery<Client> query = (NativeQuery<Client>) em.createNativeQuery("SELECT * FROM customer", Client.class);
                    List<Client> resultlist = query.list();
                    int ide = resultlist.get(rowindex).getIdCustomer();
                    new ProjectForClient().show(ide);
                    clientList.setVisible(false);
                }
            });

        //Выгрузка данных в XML файл
        openf.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileDialog save = new FileDialog(clientList, "Сохранение данных", FileDialog.SAVE);
                save.setFile("*.xml");
                save.setVisible(true);
                String fileName = save.getDirectory() + save.getFile();
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))){
                    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    Document doc = builder.newDocument();
                    Node clientList = doc.createElement("clientList");
                    doc.appendChild(clientList);
                    for (int i = 0; i < model.getRowCount(); i++) {
                        Element cln = doc.createElement("cln");
                        clientList.appendChild(cln);
                        cln.setAttribute("customer",  String.valueOf(model.getValueAt(i, 0)));
                        cln.setAttribute("contacts", String.valueOf(model.getValueAt(i, 1)));
                    }
                    Transformer trans = TransformerFactory.newInstance().newTransformer();
                    trans.transform(new DOMSource(doc), new StreamResult(writer));
                    logger.info("Данные о клиентах сохранены в XML");

                }catch (TransformerConfigurationException er) { er.printStackTrace(); }
                catch (TransformerException er) { er.printStackTrace(); }
                catch (IOException er) { er.printStackTrace(); }
                catch (ParserConfigurationException er) { er.printStackTrace(); }
            }
        });

        savef.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileDialog saveReport = new FileDialog(clientList, "Формирование отчета", FileDialog.SAVE);
                saveReport.setVisible(true);
                String fileName = saveReport.getDirectory() + saveReport.getFile();
                if (fileName.toLowerCase().endsWith(".pdf")){
                    printMethod("C:\\Users\\user\\IdeaProjects\\courseworkoop\\files\\ClientList.xml","C:\\Users\\user\\IdeaProjects\\courseworkoop\\files\\reportClientList.jrxml",
                            fileName);
                    logger.info("Данные о клиентах сохранены в PDF");
                }
            }
        });


            clientList.setVisible(true);

        }

    };
