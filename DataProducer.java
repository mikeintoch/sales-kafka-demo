// camel-k: language=java property-file=application.properties 
// camel-k: dependency=camel:jacksonxml 
// camel-k: dependency=camel:http 
// camel-k: dependency=camel:gson
// camel-k: dependency=camel:csv
// camel-k: dependency=mvn:javax.servlet:servlet-api:jar:2.5
// camel-k: dependency=mvn:commons-logging:commons-logging:jar:1.2


import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

public class DataProducer extends RouteBuilder {

  @Override
  public void configure() throws Exception {

    // The following processors store relevant info as properties
    Processor processCsv = new CSVProcessor();

    // This is the actual route
    from("timer:java?period=100000")

        // We start by reading our data.csv file, looping on each row
        .to("{{source.csv}}").unmarshal("customCSV").split(body()).streaming()
        // we store on exchange body all the data we are interested in
        .process(processCsv).marshal().json(JsonLibrary.Gson)
        .log("${body}")
        .to("kafka:sales-data?brokers={{kafka.bootstrap.address}}")
        // Write some log to know it finishes properly
        .log("Information stored");
  }

  private final class CSVProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
      @SuppressWarnings("unchecked")
      Map<String, String> body = exchange.getIn().getBody(Map.class);
      Map<String, String> res = new HashMap<String, String>();


      if (body != null) {

        res.put("orderNumber", extractValue(exchange, body, "ORDERNUMBER"));
        res.put("orderDate", extractValue(exchange, body, "ORDERDATE"));
        res.put("status", extractValue(exchange, body, "STATUS"));
        res.put("customerName", extractValue(exchange, body, "CUSTOMERNAME"));
        res.put("dealSize", extractValue(exchange, body, "DEALSIZE"));
        res.put("amount", extractValue(exchange, body, "SALES"));

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
