package com.redhat.training.jb421;


import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.redhat.training.jb421.model.Order;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/META-INF/spring/camel-context.xml" })
public class RouteTest {

	// Add secondRoute mock
	@EndpointInject(uri = "mock:secondRoute")
	protected MockEndpoint resultHeaderMockEndpoint;

	// Add orderHeader direct endpoint
	@Produce(uri = "direct:orderHeader")
	protected ProducerTemplate ordersHeaderProducerTemplate;

	// Autowire CamelContext
	@Autowired
	private CamelContext context;

	@Test
	public void testRouteHeader() throws Exception {
		// Evaluate if the second route does not process a null body and header
		// named test with a null string
		resultHeaderMockEndpoint.setExpectedMessageCount(0);
		NotifyBuilder builder = new NotifyBuilder(context).fromRoute("orderHeader").whenDone(1).create();
		ordersHeaderProducerTemplate.sendBodyAndHeader(null, "test", null);
		builder.matches(2, TimeUnit.SECONDS);
		resultHeaderMockEndpoint.assertIsSatisfied();
	}

	@Test
	public void testRouteHeaderWithHeader() throws Exception {
		resultHeaderMockEndpoint.setExpectedMessageCount(0);
		NotifyBuilder builder = new NotifyBuilder(context).from("orderHeader").whenDone(1).create();
		ordersHeaderProducerTemplate.sendBodyAndHeader(null, "test", "input");
		builder.matches(2, TimeUnit.SECONDS);
		resultHeaderMockEndpoint.assertIsSatisfied();
	}

	@Test
	public void testRouteHeaderWithHeaderAndBody() throws Exception {
		resultHeaderMockEndpoint.setExpectedMessageCount(0);
		NotifyBuilder builder = new NotifyBuilder(context).from("orderHeader").whenDone(1).create();
		ordersHeaderProducerTemplate.sendBodyAndHeader(new Order(), "test", "input");
		builder.matches(2,TimeUnit.SECONDS);
		resultHeaderMockEndpoint.assertIsNotSatisfied();
	}
}
