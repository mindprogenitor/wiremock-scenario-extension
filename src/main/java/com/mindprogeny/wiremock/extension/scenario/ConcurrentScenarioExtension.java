/*
 * ConcurrentScenarioExtension.java, 23 Mar 2018
 * Created by Joao Viegas (joao.viegas@mindprogeny.com)
 *
 * Copyright (c) 2018 Mind Progeny.
 */

package com.mindprogeny.wiremock.extension.scenario;

import java.util.Map;

import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.matching.RequestPattern;

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
              , null
              , null
              , null
              , basicCredentials
              , null
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
}
