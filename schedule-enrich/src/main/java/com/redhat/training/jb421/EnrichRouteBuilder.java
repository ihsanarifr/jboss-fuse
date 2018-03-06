package com.redhat.training.jb421;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

public class EnrichRouteBuilder extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		from("jpa:com.redhat.training.jb421.model.Order?persistenceUnit=mysql" + "&consumeDelete=false"
				+ "&consumer.namedQuery=getUndeliveredOrders" + "&maximumResults=1" + "&consumer.delay=30000"
				+ "&consumeLockEntity=false")
		.from("mock:start")
		.enrich("direct:enrichOrder")
		.log("Order sent to fulfillemnt: ${body}")
		.to("mock:fulfillmentSystem");
		
		from("direct:enrichOrder").process((Processor) new OrderFulfillmentProcessor()).wireTap("direct:updateOrder");

		from("direct:updateOrder")
		  .onException(Exception.class).maximumRedeliveries(1).end()
		  .marshal().json(JsonLibrary.Jackson)
		  .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
		  .to("http://localhost:8080/bookstore/rest/order/fulfillOrder/");
	}

}