/*
 * ConcurrentScenarioManagerTest.java, 23 Apr 2018
 * Created by Joao Viegas (joao.viegas@mindprogeny.com)
 *
 * Copyright (c)2016, 2017 Mind Progeny.
 */

package com.mindprogeny.wiremock.extension.scenario;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.mindprogeny.util.http.SimpleHttp;
import com.mindprogeny.util.http.SimpleHttpResponse;

/**
 * @author Jo&atilde;o Viegas (joao.viegas@mindprogeny.com)
 * @since 23 Apr 2018
 *
 */
public class ConcurrentScenarioManagerTest {

	/**
	 * WireMock server
	 */
	@Rule
	public WireMockRule wiremock = new WireMockRule(
			WireMockConfiguration.wireMockConfig()
			                     .port(55080)
			                     .extensions( new ConcurrentScenarioExtension()
					                        , new ConcurrentScenarioManager()));

	private void loadStub(String stub) throws Exception {
		SimpleHttpResponse response = SimpleHttp.call("POST", "http://localhost:55080/__admin/mappings/new",
				Files.readAllBytes(Paths.get(getClass().getResource(stub).toURI())));
		if (response.getResponseCode() != 201) {
			throw new RuntimeException("Failed to load stub " + stub);
		}
	}

	@Before
	public void loadScenarios() throws Exception {
		wiremock.resetAll();
        for (int i=1; i < 7; i++) {
            loadStub("/stub/custom-step" + i + ".json");
            loadStub("/stub/custom-concurrent-step" + i + ".json");
        }
	}
	
	@Test
	public void testListActiveScenarios() {
        given().port(55080)
    	   .when().get("/__admin/concurrent-scenarios")
 	       .then().body("scenarios", nullValue());
        
        given().port(55080)
     	   .with().cookie("SESSION", "1")
   	       .when().get("/testCustomConcurrent")
   	       .then().body(equalTo("1"));

        given().port(55080)
    	   .when().get("/__admin/concurrent-scenarios")
 	       .then().body("TestConcurrency", notNullValue())
 	              .body("TestConcurrency.1", equalTo("TWO"));

        given().port(55080)
     	   .with().cookie("SESSION", "1")
   	       .when().get("/testCustomConcurrent")
   	       .then().body(equalTo("2"));

        given().port(55080)
    	   .when().get("/__admin/concurrent-scenarios")
 	       .then().body("TestConcurrency", notNullValue())
 	              .body("TestConcurrency.1", equalTo("THREE"));

        given().port(55080)
     	   .with().cookie("SESSION", "2")
   	       .when().get("/testCustomConcurrent")
   	       .then().body(equalTo("1"));

        given().port(55080)
    	   .when().get("/__admin/concurrent-scenarios")
 	       .then().body("TestConcurrency", notNullValue())
                  .body("TestConcurrency.1", equalTo("THREE"))
   	              .body("TestConcurrency.2", equalTo("TWO"));

        given().port(55080)
     	   .with().cookie("SESSION", "2")
   	       .when().get("/testCustom")
   	       .then().body(equalTo("1"));

        given().port(55080)
    	   .when().get("/__admin/concurrent-scenarios")
 	       .then().body("TestConcurrency", notNullValue())
 	              .body("TestCustom", notNullValue())
                  .body("TestConcurrency.1", equalTo("THREE"))
   	              .body("TestConcurrency.2", equalTo("TWO"))
   	              .body("TestCustom.$ID", equalTo("TWO"));
	}
	
	@Test
	public void testListActiveScenario() {
        given().port(55080)
    	   .when().get("/__admin/concurrent-scenarios")
 	       .then().body("scenarios", nullValue());
        
        given().port(55080)
     	   .with().cookie("SESSION", "1")
   	       .when().get("/testCustomConcurrent")
   	       .then().body(equalTo("1"));

        given().port(55080)
    	   .when().get("/__admin/concurrent-scenarios/TestConcurrency")
 	       .then().body("1", equalTo("TWO"));

        given().port(55080)
     	   .with().cookie("SESSION", "1")
   	       .when().get("/testCustomConcurrent")
   	       .then().body(equalTo("2"));

        given().port(55080)
    	   .when().get("/__admin/concurrent-scenarios")
 	       .then().body("1", equalTo("THREE"));

        given().port(55080)
     	   .with().cookie("SESSION", "2")
   	       .when().get("/testCustomConcurrent")
   	       .then().body(equalTo("1"));

        given().port(55080)
    	   .when().get("/__admin/concurrent-scenarios")
 	       .then().body("1", equalTo("THREE"))
   	              .body("2", equalTo("TWO"));

        given().port(55080)
     	   .with().cookie("SESSION", "2")
   	       .when().get("/testCustom")
   	       .then().body(equalTo("1"));

        given().port(55080)
    	   .when().get("/__admin/concurrent-scenarios")
 	       .then().body("1", equalTo("THREE"))
   	              .body("2", equalTo("TWO"))
   	              .body("$ID", nullValue());
	}
}
