/*
 * ConcurrentScenarioExtensionTest.java, 25 Mar 2018
 * Created by Joao Viegas (joao.viegas@mindprogeny.com)
 *
 * Copyright (c)2016, 2017 Mind Progeny.
 */

package com.mindprogeny.wiremock.extension.scenario;

import static com.jayway.restassured.RestAssured.given;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.mindprogeny.util.http.SimpleHttp;
import com.mindprogeny.util.http.SimpleHttpResponse;
import static org.hamcrest.Matchers.*;

/**
 * @author Jo&atilde;o Viegas (joao.viegas@mindprogeny.com)
 * @since 25 Mar 2018
 *
 */
public class ConcurrentScenarioExtensionTest {

	/**
	 * WireMock server
	 */
	@Rule
	public WireMockRule wiremock = new WireMockRule(
			WireMockConfiguration.wireMockConfig().port(55080).extensions(new ConcurrentScenarioExtension()));

	@Before
	public void beforeTest() throws Exception {
		wiremock.resetAll();
		loadStub("/stub/match-default.json");
	}

	private void loadStub(String stub) throws Exception {
		SimpleHttpResponse response = SimpleHttp.call("POST", "http://localhost:55080/__admin/mappings/new",
				Files.readAllBytes(Paths.get(getClass().getResource(stub).toURI())));
		if (response.getResponseCode() != 201) {
			throw new RuntimeException("Failed to load stub " + stub);
		}
	}

	@Test
	public void testUrlMatchingRules() throws Exception {
		loadStub("/stub/match-url-stub.json");
		given().port(55080)
		   .when().get("/testUrl")
		   .then().body(equalTo("DEFAULT"));

		given().port(55080)
	       .when().get("/testUrl?with=nothing")
	       .then().body(equalTo("DEFAULT"));

		given().port(55080)
	       .when().get("/testUrl?with=something")
	       .then().body(equalTo("MATCHED"));

	}

	@Test
	public void testUrlRegexMatchingRules() throws Exception {
		loadStub("/stub/match-urlregex-stub.json");
		given().port(55080)
		   .when().get("/testUrl")
		   .then().body(equalTo("DEFAULT"));

		given().port(55080)
	       .when().get("/testUrl?with=nothing")
	       .then().body(equalTo("MATCHED"));

		given().port(55080)
	       .when().get("/testUrl?with=something&and=somethingelse")
	       .then().body(equalTo("MATCHED"));

	}

	@Test
	public void testUrlPathMatchingRules() throws Exception {
		loadStub("/stub/match-urlpath-stub.json");
		given().port(55080)
		   .when().get("/testUrl")
		   .then().body(equalTo("MATCHED"));

		given().port(55080)
	       .when().get("/testUr")
	       .then().body(equalTo("DEFAULT"));

		given().port(55080)
	       .when().get("/testUrl?with=something")
	       .then().body(equalTo("MATCHED"));

	}

	@Test
	public void testUrlPathRegexMatchingRules() throws Exception {
		loadStub("/stub/match-urlpathregex-stub.json");
		given().port(55080)
		   .when().get("/test")
		   .then().body(equalTo("MATCHED"));

		given().port(55080)
	       .when().get("/testUrl")
	       .then().body(equalTo("MATCHED"));

		given().port(55080)
	       .when().get("/testUrl?with=something")
	       .then().body(equalTo("MATCHED"));

		given().port(55080)
	       .when().get("/tes?with=something")
	       .then().body(equalTo("DEFAULT"));

	}

	@Test
	public void testMethodMatchingRules() throws Exception {
		loadStub("/stub/match-urlpathregex-stub.json");
		given().port(55080)
		   .when().get("/test")
		   .then().body(equalTo("MATCHED"));

		given().port(55080)
	       .when().post("/test")
	       .then().body(equalTo("DEFAULT"));
	}

	@Test
	public void testBasicAuthenticationMatchingRules() throws Exception {
		loadStub("/stub/match-basic-credentials-stub.json");
		given().port(55080)
		   .when().post("/test")
		   .then().body(equalTo("DEFAULT"));

		given().port(55080)
	       .auth().preemptive().basic("user", "otherpassword")
	       .when().post("/test")
	       .then().body(equalTo("DEFAULT"));

		given().port(55080)
	       .auth().preemptive().basic("user", "password")
	       .when().post("/test")
	       .then().body(equalTo("MATCHED"));
	}
	
	@Test
	public void testHeadersMatchingRules() throws Exception {
		loadStub("/stub/match-headers-stub.json");
		
		given().port(55080)
		   .when().post("/test")
		   .then().body(equalTo("DEFAULT"));
		
        given().port(55080)
		   .with().header("Content-Type", "text/xml")
		   .and().header("Cache-Control", "private")
		   .and().header("If-None-Match", "aa09")
		   .and().header("Accept","text/text")
		   .when().post("/test")
		   .then().body(equalTo("MATCHED"));
		
		given().port(55080)
		   .with().header("Content-Type", "text/text")
		   .and().header("Cache-Control", "private")
		   .and().header("If-None-Match", "aa09")
		   .and().header("Accept","text/text")
		   .when().post("/test")
		   .then().body(equalTo("DEFAULT"));
		
		given().port(55080)
		   .with().header("Content-Type", "text/xml")
		   .and().header("Cache-Control", "private-please")
		   .and().header("If-None-Match", "aa09")
		   .and().header("Accept","text/text")
		   .when().post("/test")
		   .then().body(equalTo("DEFAULT"));
		
		given().port(55080)
		   .with().header("Content-Type", "text/xml")
		   .and().header("Cache-Control", "public")
		   .and().header("If-None-Match", "aa09")
		   .and().header("Accept","text/text")
		   .when().post("/test")
		   .then().body(equalTo("DEFAULT"));

		
		given().port(55080)
		   .with().header("Content-Type", "text/xml")
		   .and().header("Cache-Control", "private")
		   .and().header("If-None-Match", "aa8sdf8h3ui409")
		   .and().header("Accept","text/text")
		   .when().post("/test")
		   .then().body(equalTo("MATCHED"));
		
		given().port(55080)
		   .with().header("Content-Type", "text/xml")
		   .and().header("Cache-Control", "private")
		   .and().header("If-None-Match", "aA09")
		   .and().header("Accept","text/text")
		   .when().post("/test")
		   .then().body(equalTo("DEFAULT"));
		
		given().port(55080)
		   .with().header("Content-Type", "text/xml")
		   .and().header("Cache-Control", "private")
		   .and().header("If-None-Match", "aa09")
		   .and().header("Accept","text/xml")
		   .when().post("/test")
		   .then().body(equalTo("DEFAULT"));
		
		given().port(55080)
		   .with().header("Content-Type", "text/xml")
		   .and().header("Cache-Control", "private")
		   .and().header("If-None-Match", "aa09")
		   .and().header("Accept","text/text")
		   .and().header("Timestamp","0")
		   .when().post("/test")
		   .then().body(equalTo("MATCHED"));
		
		given().port(55080)
		   .with().header("Content-Type", "text/xml")
		   .and().header("Cache-Control", "private")
		   .and().header("If-None-Match", "aa09")
		   .and().header("Accept","text/text")
		   .and().header("ETag","0")
		   .when().post("/test")
		   .then().body(equalTo("DEFAULT"));
	}
	
    @Test
    public void testQueryParameterMatchingRules() throws Exception{
		loadStub("/stub/match-query-stub.json");
		
		given().port(55080)
		   .when().post("/test")
		   .then().body(equalTo("DEFAULT"));
		
		given().port(55080)
		   .with().queryParameter("one", 1)
		   .and().queryParameter("two", 42)
		   .and().queryParameter("three", 333)
		   .and().queryParameter("four", 444)
		   .when().post("/test")
		   .then().body(equalTo("MATCHED"));
		
		given().port(55080)
		   .with().queryParameter("one", 1)
		   .and().queryParameter("two", 42)
		   .and().queryParameter("three", 333)
		   .and().queryParameter("four", 444)
		   .and().queryParameter("six", 6)
		   .when().post("/test")
		   .then().body(equalTo("MATCHED"));
		
		given().port(55080)
		   .with().queryParameter("one", 1)
		   .and().queryParameter("two", 42)
		   .and().queryParameter("three", 333)
		   .and().queryParameter("four", 444)
		   .and().queryParameter("five", 6)
		   .when().post("/test")
		   .then().body(equalTo("DEFAULT"));
		
		
		given().port(55080)
		   .with().queryParameter("one", 2)
		   .and().queryParameter("two", 42)
		   .and().queryParameter("three", 333)
		   .and().queryParameter("four", 444)
		   .when().post("/test")
		   .then().body(equalTo("DEFAULT"));

		given().port(55080)
		   .with().queryParameter("one", 1)
		   .and().queryParameter("two", 43)
		   .and().queryParameter("three", 333)
		   .and().queryParameter("four", 444)
		   .when().post("/test")
		   .then().body(equalTo("DEFAULT"));

		given().port(55080)
		   .with().queryParameter("one", 1)
		   .and().queryParameter("two", 42)
		   .and().queryParameter("three", 3331)
		   .and().queryParameter("four", 444)
		   .when().post("/test")
		   .then().body(equalTo("DEFAULT"));

		given().port(55080)
		   .with().queryParameter("one", 1)
		   .and().queryParameter("two", 42)
		   .and().queryParameter("three", 333)
		   .and().queryParameter("four", 44)
		   .when().post("/test")
		   .then().body(equalTo("DEFAULT"));
    }
	
    @Test
    public void testCookieMatchingRules() throws Exception{
		loadStub("/stub/match-cookies-stub.json");
		
		given().port(55080)
		   .when().post("/test")
		   .then().body(equalTo("DEFAULT"));
		
		given().port(55080)
		   .with().cookie("ONE", 1)
		   .and().cookie("TWO", 42)
		   .and().cookie("THREE", 333)
		   .and().cookie("FOUR", 444)
		   .when().post("/test")
		   .then().body(equalTo("MATCHED"));
		
		given().port(55080)
		   .with().cookie("ONE", 1)
		   .and().cookie("TWO", 42)
		   .and().cookie("THREE", 333)
		   .and().cookie("FOUR", 444)
		   .and().cookie("SIX", 6)
		   .when().post("/test")
		   .then().body(equalTo("MATCHED"));
		
		given().port(55080)
		   .with().cookie("ONE", 1)
		   .and().cookie("TWO", 42)
		   .and().cookie("THREE", 333)
		   .and().cookie("FOUR", 444)
		   .and().cookie("FIVE", 6)
		   .when().post("/test")
		   .then().body(equalTo("DEFAULT"));
		
		
		given().port(55080)
		   .with().cookie("ONE", 2)
		   .and().cookie("TWO", 42)
		   .and().cookie("THREE", 333)
		   .and().cookie("FOUR", 444)
		   .when().post("/test")
		   .then().body(equalTo("DEFAULT"));

		given().port(55080)
		   .with().cookie("ONE", 1)
		   .and().cookie("TWO", 43)
		   .and().cookie("THREE", 333)
		   .and().cookie("FOUR", 444)
		   .when().post("/test")
		   .then().body(equalTo("DEFAULT"));

		given().port(55080)
		   .with().cookie("ONE", 1)
		   .and().cookie("TWO", 42)
		   .and().cookie("THREE", 3331)
		   .and().cookie("FOUR", 444)
		   .when().post("/test")
		   .then().body(equalTo("DEFAULT"));

		given().port(55080)
		   .with().cookie("ONE", 1)
		   .and().cookie("TWO", 42)
		   .and().cookie("THREE", 333)
		   .and().cookie("FOUR", 44)
		   .when().post("/test")
		   .then().body(equalTo("DEFAULT"));
    }
    
    @Test
    public void testSimpleBodyPatternRules() throws Exception{
		loadStub("/stub/match-body-pattern1-stub.json");
		loadStub("/stub/match-body-pattern2-stub.json");
		
		given().port(55080)
		   .when().post("/test")
		   .then().body(equalTo("DEFAULT"));
		
		given().port(55080)
		   .with().body("the number is 5678")
		   .when().post("/test")
		   .then().body(equalTo("MATCHED"));
		
		given().port(55080)
		   .with().body("the number is 1234")
		   .when().post("/test")
		   .then().body(equalTo("MATCHED"));
		
		given().port(55080)
		   .with().body("the number is 1234")
		   .when().post("/test")
		   .then().body(equalTo("MATCHED"));
		
		given().port(55080)
		   .with().body("number is 1234")
		   .when().post("/test")
		   .then().body(equalTo("MATCHED"));
		
		given().port(55080)
		   .with().body("number is 5678")
		   .when().post("/test")
		   .then().body(equalTo("DEFAULT"));
		
		given().port(55080)
		   .with().body("numbe is 1234")
		   .when().post("/test")
		   .then().body(equalTo("DEFAULT"));
		
		given().port(55080)
		   .with().body("number is 123")
		   .when().post("/test")
		   .then().body(equalTo("DEFAULT"));
    }
    
    @Test
    public void testBinaryBodyPatternRules() throws Exception{
		loadStub("/stub/match-body-pattern3-stub.json");
		
		given().port(55080)
		   .when().post("/test")
		   .then().body(equalTo("DEFAULT"));
		
		given().port(55080)
		   .with().body(new byte[]{1,2,3})
		   .when().post("/test")
		   .then().body(equalTo("MATCHED"));
		
		given().port(55080)
		   .with().body(new byte[]{1,2,4})
		   .when().post("/test")
		   .then().body(equalTo("DEFAULT"));
    }
}
