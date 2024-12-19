package org.example;

import java.awt.*;
import java.awt.event.*;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.export.*;

import org.hibernate.query.NativeQuery;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

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
 * Класс, реализующий экранную форму списка задач.
 * @author Габдулвалеев Артур
 */
public class ProblemList {
    private static final Logger logger = LogManager.getLogger(ProblemList.class);
    private JFrame problemList;
    private DefaultTableModel model;


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

    public static boolean isValidName(String name) {
        return name.matches("^(?=.{1,40}$)[а-яёА-ЯЁ]+(?:[-' ][а-яёА-ЯЁ]+)*$");
    }

    public static boolean isValidDate(String date) {
        return date.matches("^\\d{4}\\-(0[1-9]|1[012])\\-(0[1-9]|[12][0-9]|3[01])$");
    }

    public static boolean isValidSmt(String smt){
        return smt.matches("^([\\p{L}\\p{M}0-9][\\p{L}\\p{M}0-9_-]*[\\p{L}\\p{M}0-9])(?:\\s([\\p{L}\\p{M}0-9][\\p{L}\\p{M}0-9_-]*[\\p{L}\\p{M}0-9]))*$");
    }

    public static void printMethod(String XMLFile, String JRXMLFile, String Result) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(XMLFile));
            document.getDocumentElement().normalize();
            JasperReport jasperReport = JasperCompileManager.compileReport(JRXMLFile);
            JRXmlDataSource dataSource = new JRXmlDataSource(document, "/problemList/prb");
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
        logger.info("Открытие экранной формы списка задач");
        emf = Persistence.createEntityManagerFactory("manager_persistence");
        em = emf.createEntityManager();
        problemList = new JFrame("Список задач");
        problemList.setSize(1150,600);
        problemList.setLocation(0, 0);
        problemList.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        LocalDateTime ldt = LocalDateTime.now().plusDays(1);
        DateTimeFormatter formmat1 = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
        String formatter = formmat1.format(ldt);

        //Создание кнопок и прикрепление иконок
        edit = new JButton(new ImageIcon("./img/edit.png"));
        dbplus = new JButton(new ImageIcon("./img/edit_add.png"));
        dbmin = new JButton(new ImageIcon("./img/edit_remove.png"));
        savef = new JButton(new ImageIcon("./img/document-save-as.png"));
        openf = new JButton(new ImageIcon("./img/folder_yellow_open.png"));
        back = new JButton(new ImageIcon("./img/back.png"));


        //Настройка подсказок для кнопок
        edit.setToolTipText("Редактирование информации о задаче");
        dbplus.setToolTipText("Добавление информации о задаче");
        dbmin.setToolTipText("Удаление информации о задаче");
        savef.setToolTipText("Сохранение файла");
        back.setToolTipText("Возврат к меню");

        //Добавление кнопок на панель инструментов
        toolBar = new JToolBar("Панель инструментов");
        toolBar.add(edit);
        toolBar.add(dbplus);
        toolBar.add(dbmin);
        toolBar.add(savef);
        toolBar.add(openf);
        toolBar.add(back);

        //Размещение панели инструментов
        problemList.setLayout(new BorderLayout());
        problemList.add(toolBar, BorderLayout.NORTH);

        //Размещение таблицы с данными
        //Создание таблицы с данными
        String[] columns = {"Задача", "Исполнитель", "Проект", "Сроки"};
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
        problemList.add(scroll, BorderLayout.CENTER);
        model.setRowCount(0);
        em.getTransaction().begin();
        NativeQuery<Problem> query = (NativeQuery<Problem>) em.createNativeQuery("SELECT * FROM problem", Problem.class);
        List<Problem> resultlist = query.list();
        em.getTransaction().commit();
        String s1, s2, s3, s4;
        for(Problem i : resultlist){
            s1 = i.getProblemName();
            s2 = i.getEmployee().getEmployeeName();
            s3 = i.getProject().getProjectName();
            s4 = i.getProblemDeadline();
            String[] adds = new String[] {s1, s2, s3, s4};
            model.addRow(adds);
        }


        //Слушатели кнопок
        back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("Закрыта экранная форма списка задач");
                problemList.setVisible(false);
                new Menu().show();
            }
        });

        //Добавление задачи
        dbplus.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int nv = 0;
                int cv = 0;
                int pv = 0;
                int dv = 0;
                em.getTransaction().begin();
                String s1 = (String)JOptionPane.showInputDialog(problemList, "Введите название задачи");
                while (nv == 0){
                    if (s1 != null && isValidSmt(s1)){
                        nv = 1;
                    }
                    else {
                        s1 = (String) JOptionPane.showInputDialog(problemList, "Введен неверный формат названия. \n Введите название задачи");
                    }
                }
                //Выбор исполнителя
                List<String> a = new ArrayList<String>();
                NativeQuery<Employee> querye = (NativeQuery<Employee>) em.createNativeQuery("SELECT * FROM employee", Employee.class);
                List<Employee> resultliste = querye.list();
                String employeeName;
                for (Employee i : resultliste) {
                    employeeName = i.getEmployeeName();
                    a.add(employeeName);
                }
                String[] myArray = new String[a.size()];
                a.toArray(myArray);
                JList list = new JList(myArray);
                list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                list.setVisibleRowCount(5);
                String s2 = "";
                while(cv == 0) {
                    int result = JOptionPane.showConfirmDialog(
                            problemList,
                            list,
                            "Select an item",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.PLAIN_MESSAGE
                    );
                    if (result == JOptionPane.OK_OPTION) {
                        s2 = (String) list.getSelectedValue();
                        if(s2 != null){
                            cv = 1;
                        }
                    }
                }
                //Выбор проекта
                List<String> p = new ArrayList<String>();
                NativeQuery<Project> queryp = (NativeQuery<Project>) em.createNativeQuery("SELECT * FROM project", Project.class);
                List<Project> resultlistp = queryp.list();
                String projectName;
                for (Project i : resultlistp) {
                    projectName = i.getProjectName();
                    p.add(projectName);
                }
                String[] myArrayP = new String[p.size()];
                p.toArray(myArrayP);
                JList listp = new JList(myArrayP);
                listp.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                listp.setVisibleRowCount(5);
                String s3 = "";
                while(pv == 0) {
                    int resultP = JOptionPane.showConfirmDialog(
                            problemList,
                            listp,
                            "Select an item",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.PLAIN_MESSAGE
                    );
                    // Step 3: Retrieve and handle the selected value
                    if (resultP == JOptionPane.OK_OPTION) {
                        s3 = (String) listp.getSelectedValue();
                        if(s3 != null){
                            pv = 1;
                        }
                        // Here, 'selected' contains the user's selection
                        // You can assign it to a string variable or use it as needed
                    }
                }
                //
                String s4 = (String)JOptionPane.showInputDialog(problemList, "Введите срок исполнения задачи");
                while (dv == 0){
                    if (isValidDate(s4)){
                        LocalDate date1 = LocalDate.parse(s4);
                        LocalDate date2 = LocalDate.parse(formatter);
                        if(date1.isAfter(date2)){
                            dv = 1;
                        }
                        else {
                            s4 = (String) JOptionPane.showInputDialog(problemList, "Введена неакутальная дата. \n Введите срок исполнения задачи");
                        }
                    }
                    else {
                        s4 = (String) JOptionPane.showInputDialog(problemList, "Введен неверный формат даты. \n Введите срок исполнения задачи");
                    }
                }
                String[] adds = new String[] {s1, s2, s3, s4};
                model.addRow(adds);

                Problem prb = new Problem();
                prb.setProblemName(s1);
                prb.setProblemDeadline(s4);
                em.persist(prb);
                NativeQuery<Employee> query = (NativeQuery<Employee>) em.createNativeQuery("SELECT * FROM employee WHERE employee_name = :name", Employee.class)
                        .setParameter("name", s2);
                List<Employee> resultlist = query.list();
                prb.setEmployee(resultlist.getFirst());
                em.persist(prb);
                NativeQuery<Project> query_1 = (NativeQuery<Project>) em.createNativeQuery("SELECT * FROM project WHERE project_name = :name", Project.class)
                        .setParameter("name", s3);
                List<Project> resultlist_1 = query_1.list();
                prb.setProject(resultlist_1.getFirst());
                logger.info("Добавлена задача: {}", s1);
                em.persist(prb);
                em.getTransaction().commit();
            }
        });
        //Удаление задачи
        dbmin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {


                int rowindex = table.getSelectedRow();

                int confirm = JOptionPane.showConfirmDialog(
                        problemList,
                        "Вы уверены, что хотите удалить задачу " + model.getValueAt(rowindex, 0) + "?",
                        "Подтверждение удаления",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                // Обрабатываем выбор пользователя
                if (confirm == JOptionPane.YES_OPTION) {
                    // Пользователь подтвердил удаление
                    em.getTransaction().begin();
                    NativeQuery<Problem> query = (NativeQuery<Problem>) em.createNativeQuery("SELECT * FROM problem", Problem.class);
                    List<Problem> resultlist = query.list();
                    int ide = resultlist.get(rowindex).getIdProblem();
                    em.createNativeQuery("DELETE FROM manager.problem WHERE id_problem = :id")
                            .setParameter("id", ide)
                            .executeUpdate();
                    em.getTransaction().commit();
                    String problemName = resultlist.get(rowindex).getProblemName();
                    logger.info("Удалена задача: {}", problemName);
                    model.removeRow(rowindex);
                    JOptionPane.showMessageDialog(
                            problemList,
                            "Задача успешно удалена!",
                            "Успех",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    // Пользователь отменил удаление
                    JOptionPane.showMessageDialog(
                            problemList,
                            "Удаление отменено.",
                            "Отмена",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            }
        });
        //Редактирование задачи
        edit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int nv = 0;
                int cv = 0;
                int dv = 0;
                int rowindex = table.getSelectedRow();
                em.getTransaction().begin();
                String s1 = (String)JOptionPane.showInputDialog(problemList, "Введите название задачи");
                while (nv == 0){
                    if (s1 != null && isValidSmt(s1)){
                        nv = 1;
                    }
                    else {
                        s1 = (String) JOptionPane.showInputDialog(problemList, "Введен неверный формат названия. \n Введите название задачи");
                    }
                }
                //Выбор исполнителя
                List<String> a = new ArrayList<String>();
                NativeQuery<Employee> querye = (NativeQuery<Employee>) em.createNativeQuery("SELECT * FROM employee", Employee.class);
                List<Employee> resultliste = querye.list();
                String employeeName;
                for (Employee i : resultliste) {
                    employeeName = i.getEmployeeName();
                    a.add(employeeName);
                }
                String[] myArray = new String[a.size()];
                a.toArray(myArray);
                JList list = new JList(myArray);
                list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                list.setVisibleRowCount(5);
                String s2 = "";
                while(cv == 0) {
                    int result = JOptionPane.showConfirmDialog(
                            problemList,
                            list,
                            "Select an item",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.PLAIN_MESSAGE
                    );
                    if (result == JOptionPane.OK_OPTION) {
                        s2 = (String) list.getSelectedValue();
                        if(s2 != null){
                            cv = 1;
                        }
                    }
                }
                //
                String s3 = (String)model.getValueAt(rowindex, 2);
                String s4 = (String)JOptionPane.showInputDialog(problemList, "Введите срок исполнения задачи");
                while (dv == 0){
                    if (isValidDate(s4)){
                        LocalDate date1 = LocalDate.parse(s4);
                        LocalDate date2 = LocalDate.parse(formatter);
                        if(date1.isAfter(date2)){
                            dv = 1;
                        }
                        else {
                            s4 = (String) JOptionPane.showInputDialog(problemList, "Введена неакутальная дата. \n Введите срок исполнения задачи");
                        }
                    }
                    else {
                        s4 = (String) JOptionPane.showInputDialog(problemList, "Введен неверный формат даты. \n Введите срок исполнения задачи");
                    }
                }

                model.removeRow(rowindex);
                model.insertRow(rowindex,new Object[] {s1, s2, s3, s4});
                NativeQuery<Problem> query = (NativeQuery<Problem>) em.createNativeQuery("SELECT * FROM problem", Problem.class);
                List<Problem> resultlist = query.list();
                int ide = resultlist.get(rowindex).getIdProblem();
                String problemName = resultlist.get(rowindex).getProblemName();
                logger.info("Изменены данные о задаче: {}", problemName);
                NativeQuery<Employee> query_1 = (NativeQuery<Employee>) em.createNativeQuery("SELECT * FROM employee WHERE employee_name = :name", Employee.class)
                        .setParameter("name", s2);
                List<Employee> resultlist_1 = query_1.list();
                Employee emp = resultlist_1.getFirst();
                em.createNativeQuery("UPDATE manager.problem SET problem_name = :name, id_employee = :id_emp, problem_deadline = :deadline WHERE id_problem = :id")
                        .setParameter("id", ide)
                        .setParameter("name", s1)
                        .setParameter("id_emp", emp.getIdEmployee())
                        .setParameter("deadline", s4)
                        .executeUpdate();
                em.getTransaction().commit();
            }
        });
        //Выгрузка данных в XML файл
        openf.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileDialog save = new FileDialog(problemList, "Сохранение данных", FileDialog.SAVE);
                save.setFile("*.xml");
                save.setVisible(true);
                String fileName = save.getDirectory() + save.getFile();
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))){
                    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    Document doc = builder.newDocument();
                    Node problemList = doc.createElement("problemList");
                    doc.appendChild(problemList);
                    for (int i = 0; i < model.getRowCount(); i++) {
                        Element prb = doc.createElement("prb");
                        problemList.appendChild(prb);
                        prb.setAttribute("problem",  String.valueOf(model.getValueAt(i, 0)));
                        prb.setAttribute("employee", String.valueOf(model.getValueAt(i, 1)));
                        prb.setAttribute("project", String.valueOf(model.getValueAt(i, 2)));
                        prb.setAttribute("deadline", String.valueOf(model.getValueAt(i, 3)));
                    }
                    Transformer trans = TransformerFactory.newInstance().newTransformer();
                    trans.transform(new DOMSource(doc), new StreamResult(writer));
                    logger.info("Данные о задачах сохранены в XML");

                }catch (TransformerConfigurationException er) { er.printStackTrace(); }
                catch (TransformerException er) { er.printStackTrace(); }
                catch (IOException er) { er.printStackTrace(); }
                catch (ParserConfigurationException er) { er.printStackTrace(); }
            }
        });

        savef.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileDialog saveReport = new FileDialog(problemList, "Формирование отчета", FileDialog.SAVE);
                saveReport.setVisible(true);
                String fileName = saveReport.getDirectory() + saveReport.getFile();
                if (fileName.toLowerCase().endsWith(".pdf")){
                    logger.info("Данные о задачах сохранены в PDF");
                    printMethod("C:\\Users\\user\\IdeaProjects\\courseworkoop\\files\\ProblemList.xml","C:\\Users\\user\\IdeaProjects\\courseworkoop\\files\\reportProblemList.jrxml",
                            fileName);
                }
            }
        });

        problemList.setVisible(true);

    }

}
