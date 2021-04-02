// camel-k: language=java property-file=consumer.properties
// camel-k: dependency=camel:gson
// camel-k: dependency=camel:jdbc 
// camel-k: dependency=mvn:org.postgresql:postgresql:jar:42.2.13 
// camel-k: dependency=mvn:org.apache.commons:commons-dbcp2:jar:2.7.0

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.Map;

public class DataConsumer extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // Preparing properties to build a GeoJSON Feature
        Processor processDB = new DBProcessor();

        from("kafka:sales-data?brokers={{kafka.bootstrap.address}}")
        .unmarshal().json(JsonLibrary.Gson)
                // we store on exchange properties all the data we are interested in
                .process(processDB)
        .to("jdbc:postgresBean")
        .to("log:info:showBody=true");
    }

    private final class DBProcessor implements Processor {
        @Override
        public void process(Exchange exchange) throws Exception {
            @SuppressWarnings("unchecked")
            Map<String, String> body = exchange.getIn().getBody(Map.class);
            String query = "INSERT INTO sales(ORDERNUMBER,ORDERDATE,STATUS,CUSTOMERNAME,DEALSIZE,AMOUNT)" + 
                  "values('" + body.get("orderNumber") + "'" + ",'" + body.get("orderDate") + "'" +
                  ",'" + body.get("status") + "'" + ",'" + body.get("customerName") + "'" + 
                  ",'" + body.get("dealSize") + "'" + ",'" + String.valueOf(body.get("amount")) + "')";
            
            exchange.getIn().setBody(query);
         }
    }
}