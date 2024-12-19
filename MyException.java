package org.example;
/**
 * Собственное исключение.
 */
public class MyException extends Exception {
    public MyException(){
        super ("Вы не ввели название проекта для поиска");
    }

}
