package io.spring.cloud.sleuth.docs.service1;

import com.dynatrace.oneagent.adk.OneAgentADKFactory;
import com.dynatrace.oneagent.adk.api.OneAgentADK;
import com.dynatrace.oneagent.adk.api.OutgoingRemoteCallTracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.lang.invoke.MethodHandles;
import java.math.BigInteger;
import java.util.StringTokenizer;

/**
 * @author Marcin Grzejszczak
 */
@Component
class Service2Client {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final RestTemplate restTemplate;
	private final String serviceAddress;
//	private final Tracer tracer;

	Service2Client(RestTemplate restTemplate,
			@Value("${service2.address:localhost:8082}") String serviceAddress) {
		this.restTemplate = restTemplate;
		this.serviceAddress = serviceAddress;
//		this.tracer = tracer;
	}

	public String start() throws InterruptedException {
//		log.info("Hello from service1. Setting baggage foo=>bar");
//		String secretBaggage = tracer.getCurrentSpan().getBaggageItem("baggage");
//		log.info("Super secret baggage item for key [baggage] is [{}]", secretBaggage);
//		if (StringUtils.hasText(secretBaggage)) {
//			tracer.getCurrentSpan().logEvent("secret_baggage_received");
//			tracer.addTag("baggage", secretBaggage);
//		}
//		tracer.getCurrentSpan().setBaggageItem("foo", "bar");
		log.info("Hello from service1. Calling service2");
//		String response = restTemplate.getForObject("http://" + serviceAddress + "/foo", String.class);
		String url = "http://" + serviceAddress + "/foo";


		OneAgentADK adk = OneAgentADKFactory.createInstance();
		log.info("ADK state: "+adk.getCurrentADKState());
		OutgoingRemoteCallTracer ot = adk.traceExternalOutgoingRemoteCall("foo","service2",serviceAddress,serviceAddress);
		ot.start();
		log.info("ADK dt string tag: "+ot.getDynatraceStringTag());

		String str = ot.getDynatraceStringTag();
		String[] strarr = str.split(";");
		//FW2;666;4;-869503904;0;0;41080356

		int clusterid= Integer.parseInt(strarr[2]);
		int agentid= Integer.parseInt(strarr[3]);
		int tagid= Integer.parseInt(strarr[4]);
		int linkid= Integer.parseInt(strarr[5]);


		BigInteger result = new BigInteger(strarr[2]); //clusterid
		result.shiftLeft(32);
		result.add(new BigInteger(strarr[3]));//agentid
		result.shiftLeft(32);
		result.add(new BigInteger(strarr[4]));//tagid
		result.shiftLeft(32);
		result.add(new BigInteger(strarr[5]));//linkid

		HttpHeaders headers = new HttpHeaders();

		log.info("ADK: Setting B3 traceid for clusterid={}, agentid={}, tagid={}, linkid={} --> {}",clusterid,agentid,tagid,linkid,result.toString());
		headers.add("X-B3-TraceId",result.toString());

		HttpEntity<String> entity = new HttpEntity<String>("parameters",headers);

		ResponseEntity response = restTemplate.exchange(url, HttpMethod.GET,entity,String.class);
		ot.end();


		Thread.sleep(100);
		log.info("Got response from service2 [{}]", response);
//		log.info("Service1: Baggage for [foo] is [" + tracer.getCurrentSpan().getBaggageItem("foo") + "]");
		return response.toString();
	}

//	@NewSpan("first_span")
//	String timeout(@SpanTag("someTag") String tag) {
//		try {
//			Thread.sleep(300);
//			log.info("Hello from service1. Calling service2 - should end up with read timeout");
//			String response = restTemplate.getForObject("http://" + serviceAddress + "/readtimeout", String.class);
//			log.info("Got response from service2 [{}]", response);
//			return response;
//		} catch (Exception e) {
//			log.error("Exception occurred while trying to send a request to service 2", e);
//			throw new RuntimeException(e);
//		}
//	}
}
