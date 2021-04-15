package mx.com.redhat;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Map;

@Component
public class ProducerRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // The following processors store relevant info as properties
        Processor processCsv = new CSVProcessor();

        String registryconfig="&additionalProperties.apicurio.registry.url={{registryurl}}";
        String registryconfigAvro = registryconfig+"&additionalProperties.apicurio.registry.avro-datum-provider={{datumprovider}}";
        registryconfigAvro +="&additionalProperties.apicurio.registry.global-id={{globalid}}";

        // This is the actual route
        from("timer:java?period=100000")

                // We start by reading our data.csv file, looping on each row
                .to("{{source.csv}}").unmarshal().csv().split(body()).streaming()
                // we store on exchange body all the data we are interested in
                .process(processCsv).log("${body}")
                .to("kafka:{{kafka.producer.topic}}?brokers={{kafka.brokers}}&valueSerializer={{serializerClass}}"+registryconfigAvro)
                // Write some log to know it finishes properly
                .log("Information stored");
    }

    private final class CSVProcessor implements Processor {
        @Override
        public void process(Exchange exchange) throws Exception {
            @SuppressWarnings("unchecked")
            Map<String, String> body = exchange.getIn().getBody(Map.class);
            Sale res = new Sale();

           if (body != null) {

                res.setOrderNumber(extractValue(exchange, body, "ORDERNUMBER"));
                res.setOrderDate(new SimpleDateFormat("MM/dd/yyyy").parse(extractValue(exchange, body, "ORDERDATE")).toString());
                res.setStatus(extractValue(exchange, body, "STATUS"));
                res.setCustomerName(extractValue(exchange, body, "CUSTOMERNAME"));
                res.setDealSize(extractValue(exchange, body, "DEALSIZE"));
                res.setAmount(Double.parseDouble(extractValue(exchange, body, "SALES")));
                res.setProductline(extractValue(exchange, body, "PRODUCTLINE"));

                exchange.getIn().setBody(res);
            }
        }

        private String extractValue(Exchange exchange, Map<String, String> body, String param) {
            if (body.containsKey(param)) {
                return (String) body.get(param);
            }
            return null;
        }
    }

}
