// camel-k: language=java property-file=consumer.properties
// camel-k: dependency=camel:gson

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;


public class DataConsumer extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("kafka:sales-data?brokers={{kafka.bootstrap.address}}")
        .unmarshal().json(JsonLibrary.Gson)
        .to("log:info:showBody=true");
    }
}