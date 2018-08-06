package com.babel88.paycal;

import com.babel88.paycal.config.PaycalContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Bootstrap for the paycal application
 */
public class Main {


    public static void main(String[] args) {

        ApplicationContext context = new AnnotationConfigApplicationContext(PaycalContext.class);
        /*ApplicationContext context =
                new ClassPathXmlApplicationContext("Beans.xml");*/

        PaycalApp paycalApp = (PaycalApp) context.getBean(PaycalApp.class);

        paycalApp.run();


    }
}

