package com.travelconnect.mock.car.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

/**
 * Spring WS configuration for the mock car supplier.
 *
 * Registers the MessageDispatcherServlet at /ws/* and auto-generates a WSDL
 * from the XSD schema.  The actual request handling is done by the plain
 * CarBookingController (which accepts raw SOAP envelopes as strings) — this
 * config is included to demonstrate Spring WS wiring knowledge.
 *
 * WSDL available at: GET /ws/car-booking.wsdl
 */
@EnableWs
@Configuration
public class WsConfig extends WsConfigurerAdapter {

    @Bean(name = "car-booking")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema carBookingSchema) {
        DefaultWsdl11Definition definition = new DefaultWsdl11Definition();
        definition.setPortTypeName("CarBookingPort");
        definition.setLocationUri("/ws");
        definition.setTargetNamespace("http://travelconnect.com/car-supplier");
        definition.setSchema(carBookingSchema);
        return definition;
    }

    @Bean
    public XsdSchema carBookingSchema() {
        return new SimpleXsdSchema(new ClassPathResource("xsd/car-booking.xsd"));
    }

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(
            ApplicationContext applicationContext) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }
}
