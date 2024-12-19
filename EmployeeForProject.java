package org.example;

import java.awt.*;
import java.awt.event.*;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.util.*;
import java.util.List;

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
 * Класс, реализующий экранную форму списка сотрудников, занятых на выбранном проекте.
 */
public class EmployeeForProject {
    private static final Logger logger = LogManager.getLogger(EmployeeForProject.class);
    private JFrame projectList;
    private DefaultTableModel model;

    private JButton openf;
    private JButton back;
    private JButton savef;

    private JToolBar toolBar;
    private JScrollPane scroll;
    private JTable table;
    EntityManagerFactory emf = null;
    EntityManager em = null;

    public static void printMethod(String XMLFile, String JRXMLFile, String Result) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(XMLFile));
            document.getDocumentElement().normalize();
            JasperReport jasperReport = JasperCompileManager.compileReport(JRXMLFile);
            JRXmlDataSource dataSource = new JRXmlDataSource(document, "/projectList/emp");
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


    public void show(int project_id) {
        emf = Persistence.createEntityManagerFactory("manager_persistence");
        em = emf.createEntityManager();
        logger.info("Открыта экранная форма сотрудников на проекте");
        projectList = new JFrame("Список проектов");
        projectList.setSize(1150, 600);
        projectList.setLocation(0, 0);
        projectList.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Создание кнопок и прикрепление иконок
        openf = new JButton(new ImageIcon("./img/folder_yellow_open.png"));
        back = new JButton(new ImageIcon("./img/back.png"));
        savef = new JButton(new ImageIcon("./img/document-save-as.png"));

        //Настройка подсказок для кнопок

        openf.setToolTipText("Сохранение XML файла");
        back.setToolTipText("Возврат к меню");
        savef.setToolTipText("Формирование PDF отчета");
        //Добавление кнопок на панель инструментов

        toolBar = new JToolBar("Панель инструментов");
        toolBar.add(savef);
        toolBar.add(openf);
        toolBar.add(back);
        //Размещение панели инструментов
        projectList.setLayout(new BorderLayout());
        projectList.add(toolBar, BorderLayout.NORTH);
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
        projectList.add(scroll, BorderLayout.CENTER);

        //Вывод информации из БД на экран
        model.setRowCount(0);
        em.getTransaction().begin();
        NativeQuery<Problem> query = (NativeQuery<Problem>) em.createNativeQuery("SELECT * FROM problem WHERE id_project = :id", Problem.class)
                .setParameter("id", project_id);
        List<Problem> resultlist = query.list();
        em.getTransaction().commit();
        Set<Employee> emp = new HashSet<Employee>();
        String s1, s2, s3;
        Employee empl;
        for (Problem i : resultlist) {
            empl = i.getEmployee();
            emp.add(empl);
        }

        for (Employee i : emp) {
            s1 = i.getEmployeeName();
            s2 = i.getEmployeeContacts();
            s3 = i.getEmployeeJobtitle();
            String[] adds = new String[]{s1, s2, s3};
            model.addRow(adds);
        }

        back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("Закрыта экранная форма сотрудников на проекте");
                projectList.setVisible(false);
                new ProjectList().show();
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
                    Node projectList = doc.createElement("projectList");
                    doc.appendChild(projectList);
                    for (int i = 0; i < model.getRowCount(); i++) {
                        Element emp = doc.createElement("emp");
                        projectList.appendChild(emp);
                        emp.setAttribute("name",  String.valueOf(model.getValueAt(i, 0)));
                        emp.setAttribute("contacts", String.valueOf(model.getValueAt(i, 1)));
                        emp.setAttribute("jobtitle", String.valueOf(model.getValueAt(i, 2)));
                    }
                    Transformer trans = TransformerFactory.newInstance().newTransformer();
                    trans.transform(new DOMSource(doc), new StreamResult(writer));
                    logger.info("Данные о сотрудниках на проекте сохранены в XML");

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

                    printMethod("C:\\Users\\user\\IdeaProjects\\courseworkoop\\files\\EmployeeForProject.xml","C:\\Users\\user\\IdeaProjects\\courseworkoop\\files\\reportEmployeeForProject.jrxml",
                            fileName);
                    logger.info("Данные о сотрудниках на проекте сохранены в PDF");
                }
            }
        });

        projectList.setVisible(true);
    }
};