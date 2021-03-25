// camel-k: language=java property-file=application.properties
// camel-k: dependency=camel:gson
// camel-k: dependency=mvn:org.postgresql:postgresql:jar:42.2.13 
// camel-k: dependency=mvn:org.apache.commons:commons-dbcp2:jar:2.7.0
// camel-k: dependency=camel:jdbc 

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