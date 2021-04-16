package mx.com.redhat;

import javax.sql.DataSource;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.camel.support.DefaultRegistry;


@Component
public class ConsumerRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        String registryconfig="&additionalProperties.apicurio.registry.url={{registryurl}}";
        String registryconfigAvro = registryconfig+"&additionalProperties.apicurio.registry.avro-datum-provider={{datumprovider}}";
        registryconfigAvro +="&additionalProperties.apicurio.registry.global-id={{globalid}}";
    
        // This is the actual route
        from("kafka:{{kafka.consumer.topic}}?brokers={{kafka.brokers}}&valueDeserializer={{deserializerClass}}"+registryconfigAvro)
 //       .process(processDB)
        .setBody(simple("INSERT INTO sales(ORDERNUMBER,ORDERDATE,STATUS,CUSTOMERNAME,DEALSIZE,AMOUNT,PRODUCTLINE) values ('${body.getOrderNumber}', '${body.getOrderDate}'" +
                ", '${body.getStatus}', '${body.getCustomerName}', '${body.getDealSize}', '${body.getAmount}', '${body.getProductline}')"))
        .to("jdbc:dataSource")
        .to("log:info:showBody=true")
        .log("Information Received");
    }
}
