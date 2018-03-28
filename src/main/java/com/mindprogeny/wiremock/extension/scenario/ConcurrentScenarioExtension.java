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

import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.ContentPattern;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.MultiValuePattern;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

/**
 * @author Jo&atilde;o Viegas (joao.viegas@mindprogeny.com)
 * @since 23 Mar 2018
 *
 */
public class ConcurrentScenarioExtension extends RequestMatcherExtension {

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
              , null).match(request);
        
		return matchResult;
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
     * Transforms a list of pattern matching parameters (in the form of a map) to a list of StringValuePattern matching rules.
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
            result.add(StringValuePatternBuilder.build(e));
        });
        
        return result;
    }
}
