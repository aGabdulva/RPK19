package org.example;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
/**
 * Класс "Клиент".
 */
@Entity
@Table(name = "manager.customer")
public class Client {
    private int idCustomer;
    private String customerName;
    private String customerContacts;

    @Id
    @Column(name = "id_customer")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int getIdCustomer() {
        return idCustomer;
    }

    public void setIdCustomer(int idCustomer) {
        this.idCustomer = idCustomer;
    }

    @Column(name = "customer_name")
    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    @Column(name = "customer_contacts")
    public String getCustomerContacts() {
        return customerContacts;
    }

    public void setCustomerContacts(String customerContacts) {
        this.customerContacts = customerContacts;
    }

}