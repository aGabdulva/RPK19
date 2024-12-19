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
 * Класс, реализующий экранную форму списка проектов.
 * @author Габдулвалеев Артур
 */
public class ProjectList {
    private static final Logger logger = LogManager.getLogger(ProjectList.class);
    private JFrame projectList;
    private DefaultTableModel model;

    private JButton prjct;
    private JButton edit;
    private JButton dbplus;
    private JButton dbmin;
    private JButton savef;
    private JButton openf;
    private JButton ddline;
    private JButton back;

    private JToolBar toolBar;
    private JScrollPane scroll;
    private JTable table;
    EntityManagerFactory emf = null;
    EntityManager em = null;

    public static boolean isValidDate(String date) {
        return date.matches("^\\d{4}\\-(0[1-9]|1[012])\\-(0[1-9]|[12][0-9]|3[01])$");
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
            JRXmlDataSource dataSource = new JRXmlDataSource(document, "/projectlist/prj");
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
        logger.info("Открытие экранной формы списка проектов");
        emf = Persistence.createEntityManagerFactory("manager_persistence");
        em = emf.createEntityManager();
        projectList = new JFrame("Список проектов");
        projectList.setSize(1150,600);
        projectList.setLocation(0, 0);
        projectList.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        LocalDateTime ldt = LocalDateTime.now().plusDays(1);
        DateTimeFormatter formmat1 = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
        String formatter = formmat1.format(ldt);

        //Создание кнопок и прикрепление иконок
        prjct = new JButton(new ImageIcon("./img/emp.png"));
        edit = new JButton(new ImageIcon("./img/edit.png"));
        dbplus = new JButton(new ImageIcon("./img/edit_add.png"));
        dbmin = new JButton(new ImageIcon("./img/edit_remove.png"));
        savef = new JButton(new ImageIcon("./img/document-save-as.png"));
        openf = new JButton(new ImageIcon("./img/folder_yellow_open.png"));
        ddline = new JButton(new ImageIcon("./img/warning.png"));
        back = new JButton(new ImageIcon("./img/back.png"));

        //Настройка подсказок для кнопок
        prjct.setToolTipText("Вывод списка проектов");
        edit.setToolTipText("Редактирование информации о проекте");
        dbplus.setToolTipText("Добавление информации о проекте");
        dbmin.setToolTipText("Удаление информации о проекте");
        ddline.setToolTipText("Вывод проектов, по которым нарушаются сроки");
        savef.setToolTipText("Сохранение файла");
        back.setToolTipText("Возврат в меню");

        //Добавление кнопок на панель инструментов
        toolBar = new JToolBar("Панель инструментов");
        toolBar.add(prjct);
        toolBar.add(ddline);
        toolBar.add(edit);
        toolBar.add(dbplus);
        toolBar.add(dbmin);
        toolBar.add(savef);
        toolBar.add(openf);
        toolBar.add(back);

        //Размещение панели инструментов
        projectList.setLayout(new BorderLayout());
        projectList.add(toolBar, BorderLayout.NORTH);

        //Размещение таблицы с данными
        //Создание таблицы с данными
        String[] columns = {"Проект", "Заказчик", "Сроки"};
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
        projectList.add(scroll, BorderLayout.CENTER);
        model.setRowCount(0);
        em.getTransaction().begin();
        NativeQuery<Project> query = (NativeQuery<Project>) em.createNativeQuery("SELECT * FROM project", Project.class);
        List<Project> resultlist = query.list();
        em.getTransaction().commit();
        String s1, s2, s3;
        for(Project i : resultlist) {
            s1 = i.getProjectName();
            s2 = i.getClient().getCustomerName();
            s3 = i.getDeadline();
            String[] adds = new String[]{s1, s2, s3};
            model.addRow(adds);
        }


        //Слушатели кнопок
        back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("Закрыта экранная форма списка проектов");
                projectList.setVisible(false);
                new Menu().show();
            }
        });

        //Добавление проекта
        dbplus.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int nv = 0;
                int cv = 0;
                int dv = 0;
                String s1 = (String)JOptionPane.showInputDialog(projectList, "Введите название проекта");
                while (nv == 0){
                    if (s1 != null && isValidName(s1)){
                        nv = 1;
                    }
                    else {
                        s1 = (String) JOptionPane.showInputDialog(projectList, "Введен неверный формат названия. \n Введите название задачи");
                    }
                }
                //Выбор заказчика из списка
                em.getTransaction().begin();
                List<String> a = new ArrayList<String>();
                NativeQuery<Client> query = (NativeQuery<Client>) em.createNativeQuery("SELECT * FROM customer", Client.class);
                List<Client> resultlist = query.list();
                String customerName;
                for (Client i : resultlist) {
                    customerName = i.getCustomerName();
                    a.add(customerName);
                }
                em.getTransaction().commit();
                String[] myArray = new String[a.size()];
                a.toArray(myArray);
                JList list = new JList(myArray);
                list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                list.setVisibleRowCount(5);
                String s2 = "";
                while(cv == 0) {
                    int result = JOptionPane.showConfirmDialog(
                            projectList,
                            list,
                            "Select an item",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.PLAIN_MESSAGE
                    );
                    // Step 3: Retrieve and handle the selected value
                    if (result == JOptionPane.OK_OPTION) {
                        s2 = (String) list.getSelectedValue();
                        if(s2 != null){
                            cv = 1;
                        }
                        // Here, 'selected' contains the user's selection
                        // You can assign it to a string variable or use it as needed
                    }
                }
                String s3 = (String)JOptionPane.showInputDialog(projectList, "Введите срок исполнения проекта");
                while (dv == 0){
                    if (isValidDate(s3)){
                        LocalDate date1 = LocalDate.parse(s3);
                        LocalDate date2 = LocalDate.parse(formatter);
                        if(date1.isAfter(date2)){
                            dv = 1;
                        }
                        else {
                            s3 = (String) JOptionPane.showInputDialog(projectList, "Введена неакутальная дата. \n Введите срок исполнения проекта");
                        }
                    }
                    else {
                        s3 = (String) JOptionPane.showInputDialog(projectList, "Введен неверный формат даты. \n Введите срок исполнения проекта");
                    }
                }
                String[] adds = new String[] {s1, s2, s3};
                model.addRow(adds);
                em.getTransaction().begin();

                Project prj = new Project();
                prj.setProjectName(s1);
                prj.setDeadline(s3);
                em.persist(prj);
                NativeQuery<Client> query1 = (NativeQuery<Client>) em.createNativeQuery("SELECT * FROM customer WHERE customer_name = :name", Client.class)
                        .setParameter("name", s2);
                List<Client> resultlist1 = query1.list();
                prj.setClient(resultlist1.getFirst());
                logger.info("Добавлен проект: {}", s1);
                em.persist(prj);
                em.getTransaction().commit();
            }
        });

        //Удаление проекта
        dbmin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                int rowindex = table.getSelectedRow();
                int confirm = JOptionPane.showConfirmDialog(
                        null,
                        "Вы уверены, что хотите удалить проект " + model.getValueAt(rowindex, 0) + "?",
                        "Подтверждение удаления",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                // Обрабатываем выбор пользователя
                if (confirm == JOptionPane.YES_OPTION) {
                    // Пользователь подтвердил удаление
                    em.getTransaction().begin();
                    NativeQuery<Project> query = (NativeQuery<Project>) em.createNativeQuery("SELECT * FROM project", Project.class);
                    List<Project> resultlist = query.list();
                    int ide = resultlist.get(rowindex).getIdProject();
                    String projectName = resultlist.get(rowindex).getProjectName();
                    logger.info("Удален проект: {}", projectName);
                    em.createNativeQuery("DELETE FROM manager.project WHERE id_project = :id")
                            .setParameter("id", ide)
                            .executeUpdate();
                    em.getTransaction().commit();
                    model.removeRow(rowindex);
                    JOptionPane.showMessageDialog(
                            null,
                            "Элемент успешно удален!",
                            "Успех",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    // Пользователь отменил удаление
                    JOptionPane.showMessageDialog(
                            null,
                            "Удаление отменено.",
                            "Отмена",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            }
        });

        //Редактирование проекта
        edit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int rowindex = table.getSelectedRow();
                int nv = 0;
                int dv = 0;
                String s1 = (String)JOptionPane.showInputDialog(projectList, "Введите название проекта");
                while (nv == 0){
                    if (s1 != null && isValidName(s1)){
                        nv = 1;
                    }
                    else {
                        s1 = (String) JOptionPane.showInputDialog(projectList, "Введен неверный формат названия. \n Введите название задачи");
                    }
                }
                String s2 = (String)JOptionPane.showInputDialog(projectList, "Введите срок исполнения проекта");
                while (dv == 0){
                    if (isValidDate(s2)){
                        LocalDate date1 = LocalDate.parse(s2);
                        LocalDate date2 = LocalDate.parse(formatter);
                        if(date1.isAfter(date2)){
                            dv = 1;
                        }
                        else {
                            s2 = (String) JOptionPane.showInputDialog(projectList, "Введена неакутальная дата. \n Введите срок исполнения проекта");
                        }
                    }
                    else {
                        s2 = (String) JOptionPane.showInputDialog(projectList, "Введен неверный формат даты. \n Введите срок исполнения проекта");
                    }
                }
                String s3 = (String)model.getValueAt(rowindex, 1);
                model.removeRow(rowindex);
                model.insertRow(rowindex,new Object[] {s1, s3, s2});
                em.getTransaction().begin();
                NativeQuery<Project> query = (NativeQuery<Project>) em.createNativeQuery("SELECT * FROM project", Project.class);
                List<Project> resultlist = query.list();
                int ide = resultlist.get(rowindex).getIdProject();
                String projectName = resultlist.get(rowindex).getProjectName();
                logger.info("изменены данные о проекте: {}", projectName);
                em.createNativeQuery("UPDATE manager.project SET project_name = :name, deadline = :deadline WHERE id_project = :id")
                        .setParameter("id", ide)
                        .setParameter("name", s1)
                        .setParameter("deadline", s2)
                        .executeUpdate();
                em.getTransaction().commit();
            }
        });

        ddline.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ProjectDeadline().show();
                projectList.setVisible(false);
            }
        });

        prjct.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int rowindex = table.getSelectedRow();
                NativeQuery<Project> query = (NativeQuery<Project>) em.createNativeQuery("SELECT * FROM project", Project.class);
                List<Project> resultlist = query.list();
                int ide = resultlist.get(rowindex).getIdProject();
                new EmployeeForProject().show(ide);
                projectList.setVisible(false);
            }
        });

        openf.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileDialog save = new FileDialog(projectList, "Сохранение данных", FileDialog.SAVE);
                save.setFile("*.xml");
                save.setVisible(true);
                String fileName = save.getDirectory() + save.getFile();
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))){
                    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    Document doc = builder.newDocument();
                    Node projectlist = doc.createElement("projectlist");
                    doc.appendChild(projectlist);
                    for (int i = 0; i < model.getRowCount(); i++) {
                        Element prj = doc.createElement("prj");
                        projectlist.appendChild(prj);
                        prj.setAttribute("name",  String.valueOf(model.getValueAt(i, 0)));
                        prj.setAttribute("customer", String.valueOf(model.getValueAt(i, 1)));
                        prj.setAttribute("deadline", String.valueOf(model.getValueAt(i, 2)));
                    }
                    Transformer trans = TransformerFactory.newInstance().newTransformer();
                    trans.transform(new DOMSource(doc), new StreamResult(writer));
                    logger.info("Данные о проектах сохранены в XML");

                }catch (TransformerConfigurationException er) { er.printStackTrace(); }
                catch (TransformerException er) { er.printStackTrace(); }
                catch (IOException er) { er.printStackTrace(); }
                catch (ParserConfigurationException er) { er.printStackTrace(); }
            }
        });

        savef.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileDialog saveReport = new FileDialog(projectList, "Формирование отчета", FileDialog.SAVE);
                saveReport.setVisible(true);
                String fileName = saveReport.getDirectory() + saveReport.getFile();
                if (fileName.toLowerCase().endsWith(".pdf")){
                    printMethod("C:\\Users\\user\\IdeaProjects\\courseworkoop\\files\\ProjectList.xml","C:\\Users\\user\\IdeaProjects\\courseworkoop\\files\\ReportProjectList.jrxml",
                            fileName);
                    logger.info("Данные о проектах сохранены в PDF");
                }
            }
        });

        projectList.setVisible(true);

    }

}
