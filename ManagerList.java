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


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.*;


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
 * Класс, реализующий экранную форму списка сотрудников.
 * @author Габдулвалеев Артур
 */

public class ManagerList {
    private static final Logger logger = LogManager.getLogger(ManagerList.class);
    private JFrame managerList;
    private DefaultTableModel model;

    private JButton emp;
    private JButton edit;
    private JButton dbplus;
    private JButton dbmin;
    private JButton savef;
    private JButton openf;
    private JButton back;

    private JToolBar toolBar;
    private JScrollPane scroll;
    private JTable table;

    public static boolean isValidEmail(String email) {
        return email.matches("^[\\w-\\.]+@[\\w-]+(\\.[\\w-]+)*\\.[a-z]{2,}$");
    }

    public static boolean isValidName(String name) {
        return name.matches("^(?=.{1,40}$)[а-яёА-ЯЁ]+(?:[-' ][а-яёА-ЯЁ]+)*$");
    }

    public static boolean isValidSmt(String smt){
        return smt.matches("^([\\p{L}\\p{M}0-9][\\p{L}\\p{M}0-9_-]*[\\p{L}\\p{M}0-9])(?:\\s([\\p{L}\\p{M}0-9][\\p{L}\\p{M}0-9_-]*[\\p{L}\\p{M}0-9]))*$");
    }



    public void checkName(String bName) throws MyException, NullPointerException {
        if (bName.contains("Название задачи")) throw new MyException();
        if (bName.length() == 0) throw new NullPointerException();
    }

    public static void printMethod(String XMLFile, String JRXMLFile, String Result) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(XMLFile));
            document.getDocumentElement().normalize();
            JasperReport jasperReport = JasperCompileManager.compileReport(JRXMLFile);
            JRXmlDataSource dataSource = new JRXmlDataSource(document, "/managerlist/emp");
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

    EntityManagerFactory emf = null;
    EntityManager em = null;




    public void show() {
        emf = Persistence.createEntityManagerFactory("manager_persistence");
        em = emf.createEntityManager();
        logger.info("Открытие экранной формы списка сотрудников");
        managerList = new JFrame("Список сотрудников");
        managerList.setSize(1000,600);
        managerList.setLocation(0, 0);
        managerList.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Создание кнопок и прикрепление иконок
        emp = new JButton(new ImageIcon("./img/emp.png"));
        edit = new JButton(new ImageIcon("./img/edit.png"));
        dbplus = new JButton(new ImageIcon("./img/edit_add.png"));
        dbmin = new JButton(new ImageIcon("./img/edit_remove.png"));
        savef = new JButton(new ImageIcon("./img/document-save-as.png"));
        openf = new JButton(new ImageIcon("./img/folder_yellow_open.png"));
        back = new JButton(new ImageIcon("./img/back.png"));

        //Настройка подсказок для кнопок
        emp.setToolTipText("Загрузка данных о сотруднике");
        edit.setToolTipText("Редактирование информации о сотруднике");
        dbplus.setToolTipText("Добавление информации о сотруднике");
        dbmin.setToolTipText("Удаление информации о сотруднике");
        savef.setToolTipText("Выгрузка PDF отчета");
        openf.setToolTipText("Выгрузка в XML файл");
        back.setToolTipText("Возврат к меню");
        //Добавление кнопок на панель инструментов
        toolBar = new JToolBar("Панель инструментов");
        toolBar.add(emp);
        toolBar.add(edit);
        toolBar.add(dbplus);
        toolBar.add(dbmin);
        toolBar.add(savef);
        toolBar.add(openf);
        toolBar.add(back);

        //Размещение панели инструментов
        managerList.setLayout(new BorderLayout());
        managerList.add(toolBar, BorderLayout.NORTH);

        //Размещение таблицы с данными
        //Создание таблицы с данными
        String[] columns = {"Имя", "Контакты", "Должность"};
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
        managerList.add(scroll, BorderLayout.CENTER);
        model.setRowCount(0);
        em.getTransaction().begin();
        NativeQuery<Employee> query = (NativeQuery<Employee>) em.createNativeQuery("SELECT * FROM employee", Employee.class);
        List<Employee> resultlist = query.list();
        em.getTransaction().commit();
        String s1, s2, s3;
        for(Employee i : resultlist){
            s1 = i.getEmployeeName();
            s2 = i.getEmployeeContacts();
            s3 = i.getEmployeeJobtitle();
            String[] adds = new String[] {s1, s2, s3};
            model.addRow(adds);
        }

        //Подготовка компонентов поиска

        //Слушатели кнопок
        //Добавление информации о сотруднике
        dbplus.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int nv = 0;
                int ev = 0;
                int jv = 0;
                String s1 = (String)JOptionPane.showInputDialog(managerList, "Введите имя сотрудника");
                while (nv == 0){
                    if (isValidName(s1)){
                        nv = 1;
                    }
                    else {
                        s1 = (String) JOptionPane.showInputDialog(managerList, "Введен неверный формат имени. \n Введите имя сотрудника");
                    }
                }
                String s2 = (String)JOptionPane.showInputDialog(managerList, "Введите контакты сотрудника");
                while (ev == 0){
                    if (isValidEmail(s2)){
                        ev = 1;
                    }
                    else {
                        s2 = (String) JOptionPane.showInputDialog(managerList, "Введен неверный формат почты. \n Введите контакты сотрудника");
                    }
                }
                String s3 = (String)JOptionPane.showInputDialog(managerList, "Введите должность сотрудника");
                while (jv == 0){
                    if (isValidSmt(s3)){
                        jv = 1;
                    }
                    else {
                        s3 = (String) JOptionPane.showInputDialog(managerList, "Введен неверный формат должности. \n Введите должность сотрудника");
                    }
                }
                String[] adds = new String[] {s1, s2, s3};
                model.addRow(adds);
                em.getTransaction().begin();

                Employee emp = new Employee();
                emp.setEmployeeName(s1);
                emp.setEmployeeContacts(s2);
                emp.setEmployeeJobtitle(s3);

                em.persist(emp);
                em.getTransaction().commit();
                logger.info("Добавлен сотрудник: {}", s1);
            }
        });

        //Удаление информации о сотруднике
        dbmin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                int rowindex = table.getSelectedRow();
                int confirm = JOptionPane.showConfirmDialog(
                        managerList,
                        "Вы уверены, что хотите удалить сотрудника " + model.getValueAt(rowindex, 0) + "?",
                        "Подтверждение удаления",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                // Обрабатываем выбор пользователя
                if (confirm == JOptionPane.YES_OPTION) {
                    // Пользователь подтвердил удаление
                    em.getTransaction().begin();
                    NativeQuery<Employee> query = (NativeQuery<Employee>) em.createNativeQuery("SELECT * FROM employee", Employee.class);
                    List<Employee> resultlist = query.list();
                    int ide = resultlist.get(rowindex).getIdEmployee();
                    em.createNativeQuery("DELETE FROM manager.employee WHERE id_employee = :id")
                            .setParameter("id", ide)
                            .executeUpdate();
                    em.getTransaction().commit();
                    String employeeName = resultlist.get(rowindex).getEmployeeName();
                    logger.info("Удален сотрудник: {}", employeeName);
                    model.removeRow(rowindex);
                    JOptionPane.showMessageDialog(
                            managerList,
                            "Сотрудник успешно удален!",
                            "Успех",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    // Пользователь отменил удаление
                    JOptionPane.showMessageDialog(
                            managerList,
                            "Удаление отменено.",
                            "Отмена",
                            JOptionPane.WARNING_MESSAGE
                    );


                }
            }
        });


        //Редактирование данных о сотруднике
        edit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int rowindex = table.getSelectedRow();
                int nv = 0;
                int ev = 0;
                int jv = 0;
                String s1 = (String)JOptionPane.showInputDialog(managerList, "Введите имя сотрудника");
                while (nv == 0){
                    if (isValidName(s1)){
                        nv = 1;
                    }
                    else {
                        s1 = (String) JOptionPane.showInputDialog(managerList, "Введен неверный формат имени. \n Введите имя сотрудника");
                    }
                }
                String s2 = (String)JOptionPane.showInputDialog(managerList, "Введите контакты сотрудника");
                while (ev == 0){
                    if (isValidEmail(s2)){
                        ev = 1;
                    }
                    else {
                        s2 = (String) JOptionPane.showInputDialog(managerList, "Введен неверный формат почты. \n Введите контакты сотрудника");
                    }
                }
                String s3 = (String)JOptionPane.showInputDialog(managerList, "Введите должность сотрудника");
                while (jv == 0){
                    if (isValidSmt(s3)){
                        jv = 1;
                    }
                    else {
                        s3 = (String) JOptionPane.showInputDialog(managerList, "Введен неверный формат должности. \n Введите должность сотрудника");
                    }
                }
                model.removeRow(rowindex);
                model.insertRow(rowindex,new Object[] {s1, s2, s3});
                em.getTransaction().begin();
                NativeQuery<Employee> query = (NativeQuery<Employee>) em.createNativeQuery("SELECT * FROM employee", Employee.class);
                List<Employee> resultlist = query.list();
                int ide = resultlist.get(rowindex).getIdEmployee();
                String employeeName = resultlist.get(rowindex).getEmployeeName();
                logger.info("Изменены данные о сотруднике: {}", employeeName);
                em.createNativeQuery("UPDATE manager.employee SET employee_name = :name, employee_contacts = :contacts, employee_jobtitle = :jobtitle WHERE id_employee = :id")
                        .setParameter("id", ide)
                        .setParameter("name", s1)
                        .setParameter("contacts", s2)
                        .setParameter("jobtitle", s3)
                        .executeUpdate();
                em.getTransaction().commit();
            }
        });

        //Загрузка выбранного сотрудника: в каких проектах и над какими задачами он работает
        emp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int rowindex = table.getSelectedRow();
                NativeQuery<Employee> query = (NativeQuery<Employee>) em.createNativeQuery("SELECT * FROM employee", Employee.class);
                List<Employee> resultlist = query.list();
                int ide = resultlist.get(rowindex).getIdEmployee();
                new ProjectForEmployee().show(ide);
                managerList.setVisible(false);
            }
        });

        openf.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileDialog save = new FileDialog(managerList, "Сохранение данных", FileDialog.SAVE);
                save.setFile("*.xml");
                save.setVisible(true);
                String fileName = save.getDirectory() + save.getFile();
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))){
                    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    Document doc = builder.newDocument();
                    Node managerlist = doc.createElement("managerlist");
                    doc.appendChild(managerlist);
                    for (int i = 0; i < model.getRowCount(); i++) {
                        Element emp = doc.createElement("emp");
                        managerlist.appendChild(emp);
                        emp.setAttribute("name",  String.valueOf(model.getValueAt(i, 0)));
                        emp.setAttribute("contacts", String.valueOf(model.getValueAt(i, 1)));
                        emp.setAttribute("jobtitle", String.valueOf(model.getValueAt(i, 2)));
                    }
                    Transformer trans = TransformerFactory.newInstance().newTransformer();
                    trans.transform(new DOMSource(doc), new StreamResult(writer));
                    logger.info("Данные о списке сотрудников сохранены в XML");

                }catch (TransformerConfigurationException er) { er.printStackTrace(); }
                catch (TransformerException er) { er.printStackTrace(); }
                catch (IOException er) { er.printStackTrace(); }
                catch (ParserConfigurationException er) { er.printStackTrace(); }
            }
        });

        //Выгрузка отчета в PDF
        savef.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("Формирование отчета");
                FileDialog saveReport = new FileDialog(managerList, "Формирование отчета", FileDialog.SAVE);
                saveReport.setVisible(true);
                String fileName = saveReport.getDirectory() + saveReport.getFile();
                if (fileName.toLowerCase().endsWith(".pdf")){
                    logger.info("Данные о списке сотрудников сохранены в PDF");
                    printMethod("C:\\Users\\user\\IdeaProjects\\courseworkoop\\files\\EmployeeList.xml","C:\\Users\\user\\IdeaProjects\\courseworkoop\\files\\reportEmployeeList.jrxml",
                            fileName);
                }
            }
        });



        back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("Закрыта экранная форма списка сотрудников");
                managerList.setVisible(false);
                new Menu().show();
            }
        });


        managerList.setVisible(true);

    }



}
