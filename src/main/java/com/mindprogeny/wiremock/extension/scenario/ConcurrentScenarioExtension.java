/*
 * ConcurrentScenarioExtension.java, 23 Mar 2018
 * Created by Joao Viegas (joao.viegas@mindprogeny.com)
 *
 * Copyright (c) 2018 Mind Progeny.
 */

package com.mindprogeny.wiremock.extension.scenario;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Cookie;
import com.github.tomakehurst.wiremock.http.QueryParameter;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.BinaryEqualToPattern;
import com.github.tomakehurst.wiremock.matching.ContentPattern;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.MultiValuePattern;
import com.github.tomakehurst.wiremock.matching.MultipartValuePattern;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.stubbing.Scenario;

import wiremock.com.fasterxml.jackson.core.JsonProcessingException;
import wiremock.com.fasterxml.jackson.databind.ObjectMapper;
import wiremock.org.custommonkey.xmlunit.exceptions.ConfigurationException;

/**
 * @author Jo&atilde;o Viegas (joao.viegas@mindprogeny.com)
 * @since 23 Mar 2018
 *
 */
public class ConcurrentScenarioExtension extends RequestMatcherExtension {
    
    /**
     * The Scenario repository
     */
    private static final ConcurrentHashMap<String,ConcurrentHashMap<String,AtomicReference<String>>> SCENARIOS = new ConcurrentHashMap<>();
	
    /**
     * Default instance identifier for uni-thread scenarios
     */
    private static final String DEFAULT_INSTANCE_ID = "$ID";

    /**
     * Object Mapper to serialize maps
     */
    private static final ObjectMapper jsonMapper = new ObjectMapper();

	/**
	 * @see com.github.tomakehurst.wiremock.matching.RequestMatcherExtension#match(com.github.tomakehurst.wiremock.http.Request, com.github.tomakehurst.wiremock.extension.Parameters)
	 */
	@Override
    @SuppressWarnings("unchecked")
	public MatchResult match(Request request, Parameters parameters) {
		// First check if the stub itself is matched by the request pattern
		
		Map<String,Object> requestParameters = (Map<String, Object>) parameters.get("request");

        BasicCredentials basicCredentials = null;
        Map<String,String> basicCredentialsMap = (Map<String, String>) requestParameters.get("basicAuthCredentials");
        if (basicCredentialsMap != null) {
            basicCredentials = new BasicCredentials(basicCredentialsMap.get("username"), 
                                                    basicCredentialsMap.get("password"));
        }

        MatchResult matchResult = new RequestPattern( 
        		(String)requestParameters.get("url")
              , (String)requestParameters.get("urlPattern")
              , (String)requestParameters.get("urlPath")
              , (String)requestParameters.get("urlPathPattern")
              , RequestMethod.fromString((String)requestParameters.get("method"))
              , getMultiValuePatternMap((Map<String,Map<String,Object>>)requestParameters.get("headers"))
              , getMultiValuePatternMap((Map<String,Map<String,Object>>)requestParameters.get("queryParameters"))
              , getStringValuePatternMap((Map<String,Map<String,Object>>)requestParameters.get("cookies"))
              , basicCredentials
              , getContentPatternList((Collection<Map<String,Object>>)requestParameters.get("bodyPatterns"))
              , null
              , getMultipartPatternList((Collection<Map<String,Object>>)requestParameters.get("multipartPatterns"))).match(request);
        
        String scenarioName = (String)parameters.get("scenarioName");
        if (scenarioName == null || !matchResult.isExactMatch()) {
            return matchResult;
        }

        String instanceIdentifier = (String)parameters.get("scenarioInstanceIdentifier");
        String instanceIdentifierPattern = (String)parameters.get("scenarioInstanceIdentifierPattern");
        String scenarioInstance = getScenarioInstance(instanceIdentifier, instanceIdentifierPattern, request);
        if (scenarioInstance == null) {
        	scenarioInstance = DEFAULT_INSTANCE_ID;
        }
        
        AtomicReference<String> state = getOrInitialize(scenarioName, scenarioInstance);
        
        String requiredState = (String)parameters.get("requiredScenarioState");
        if (requiredState != null && !requiredState.equals(state.get())) {
            return MatchResult.noMatch();
        }
        String newState = (String)parameters.get("newScenarioState");
        if (newState != null) {
        	state.set(newState);
        }
        
        
        return MatchResult.exactMatch();
	}

	/**
     * Gets the instance id from the request, based on the configured source of an instance if and the pattern to search for.
     * <p>
     * if the source is a query parameter or a cookie, the pattern is expected to be the exact match of the parameter or cookie name.
     *  
     * @param instanceIdentifier source the instance id should be looked for
     * @param instanceIdentifierPattern the pattern to search for
     * @param request the request to search in
     * @return the found instance id, or the default instance if no source was given.
     */
    private String getScenarioInstance(String instanceIdentifier, String instanceIdentifierPattern, Request request) {
    	if (instanceIdentifier == null) {
    		return null;
    	}
        switch(instanceIdentifier) {
        case "url" :
        	if (instanceIdentifierPattern == null) {
        		break;
        	}
            Pattern urlMatchPattern = Pattern.compile(instanceIdentifierPattern);
            Matcher matcher = urlMatchPattern.matcher(request.getUrl());
            if (matcher.matches()) {
                return matcher.group(1);
            }
            break;
        case "queryParameter" : 
            QueryParameter parameter = request.queryParameter(instanceIdentifierPattern);
            if (parameter != null) {
                return parameter.firstValue();
            }
            break;
        case "cookie" :
            Cookie cookie = request.getCookies().get(instanceIdentifierPattern);
            if (cookie != null) {
                return cookie.getValue();
            }
            break;
        default :
            throw new ConfigurationException("Unknown instance identifier source : " + instanceIdentifier);
        }
        return null;
    }

	/**
     * @see com.github.tomakehurst.wiremock.matching.RequestMatcherExtension#getName()
     */
    @Override
    public String getName() {
        return "concurrent-session";
    }

    /**
     * Transforms a map of named pattern matching rule parameters to a map of named multi value pattern rules.
     * 
     * @param namedPatternParameters the map of named pattern matching rule parameters
     * @return a map of named multi value pattern rules
     */
    private Map<String, MultiValuePattern> getMultiValuePatternMap(Map<String,Map<String,Object>> namedPatternParameters) {
        if (namedPatternParameters == null || namedPatternParameters.size() ==0) {
            return null;
        }
        
        Map<String, MultiValuePattern> result = new HashMap<>();
        
        namedPatternParameters.forEach((k,v) -> {
            result.put(k, new MultiValuePattern(StringValuePatternBuilder.build(v)));
        });
        
        return result;
    }

    /**
     * Transforms a map of named pattern matching rule parameters to a map of named string value pattern rules.
     * 
     * @param namedPatternParameters the map of named pattern matching rule parameters
     * @return a map of named string value pattern rules
     */
    private Map<String, StringValuePattern> getStringValuePatternMap(Map<String,Map<String,Object>> namedPatternParameters) {
        if (namedPatternParameters == null || namedPatternParameters.size() == 0) {
            return null;
        }
        
        Map<String, StringValuePattern> result = new HashMap<>();
        
        namedPatternParameters.forEach((k,v) -> {
            result.put(k, StringValuePatternBuilder.build(v));
        });
        
        return result;
    }


    /**
     * Transforms a list of pattern matching parameters (in the form of a map) to a list of ContentPattern matching rules.
     * 
     * @param patternParameters list of pattern matching parameters
     * @return list of string value pattern rules
     */
    private List<ContentPattern<?>> getContentPatternList(Collection<Map<String,Object>> patternParameters) {
        if (patternParameters == null || patternParameters.size() ==0) {
            return null;
        }
        
        List<ContentPattern<?>> result = new LinkedList<>();
        
        patternParameters.forEach(e -> {
        	Object binaryValue = e.get("binaryEqualTo");
        	if (binaryValue != null) {
        		result.add(new BinaryEqualToPattern(binaryValue.toString()));
        	} else {
                result.add(StringValuePatternBuilder.build(e));
        	}
        });
        
        return result;
    }
    
    @SuppressWarnings("unchecked")
	private List<MultipartValuePattern> getMultipartPatternList(Collection<Map<String,Object>> patternParameters) {
        if (patternParameters == null || patternParameters.size() ==0) {
            return null;
        }
        
        List<MultipartValuePattern> result = new LinkedList<>();

        patternParameters.forEach(e -> {
        	result.add(
        			new MultipartValuePattern(
        					(String)e.get("name")
        				  , MultipartValuePattern.MatchingType.valueOf((String)e.get("matchingType"))
        				  , getMultiValuePatternMap((Map<String,Map<String,Object>>)e.get("headers"))
        				  , getContentPatternList((Collection<Map<String,Object>>)e.get("bodyPatterns"))));
        });
        
        return result;
    }
    
    
    /**
     * Gets the scenario state of a specific instance.  If the scenario and/or instance are still not present in the repository
     * (no scenario stubs have been accessed yet, since the last reset) the instance scenario is initialized.
     * 
     * @param scenarioName Name of the scenario
     * @param scenarioInstance The instance id
     * @return The instance Scenario State
     */
    public static AtomicReference<String> getOrInitialize(String scenarioName, String scenarioInstance) {
        ConcurrentHashMap<String,AtomicReference<String>> instances = SCENARIOS.computeIfAbsent(scenarioName, k -> new ConcurrentHashMap<>());
        AtomicReference<String> state = instances.computeIfAbsent(scenarioInstance,k -> new AtomicReference<>(Scenario.STARTED));
        return state;
    }

	/**
	 * Returns all triggered scenarios and currently held in memory, as a json object.
	 * 
	 * @return A string representing the json object with all inmemory scenario instances and states
	 * @throws JsonProcessingException // shouldn't happen
	 */
	public static String serializeScenarios() throws JsonProcessingException {
        return jsonMapper.writeValueAsString(SCENARIOS);
	}

	/**
	 * Returns all triggered instances of a particular scenario
	 * 
	 * @param scenario name
	 * @return JsonProcessingException // shouldn't happen
	 */
	public static String serializeScenarioInstances(String scenario) throws JsonProcessingException {
        return jsonMapper.writeValueAsString(SCENARIOS.get(scenario));
	}

}
