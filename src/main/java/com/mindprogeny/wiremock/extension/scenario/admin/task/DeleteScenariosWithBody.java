/*
 * DeleteScenarioSessionWithBody.java, 25 Apr 2018
 * Created by Joao Viegas (joao.viegas@mindprogeny.com)
 *
 * Copyright (c) 2018 Mind Progeny.
 */
package com.mindprogeny.wiremock.extension.scenario.admin.task;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.mindprogeny.wiremock.extension.scenario.ConcurrentScenarioExtension;

import wiremock.com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Admin task to remove a() scenario(s) or its(their) instance(s) from the repository, getting the parameters from a json body.
 * <p>
 * This task is preferred when instance id's might have characters that do not resolve well as path parameters (namely the forward slash)
 * <p>
 * This operation will effectively reset a scenario instance or instances.
 * <p>
 * The message format is:
 * <pre>
 * {
 *   "scenarios": [
 *     { "name": "&lt;scenario name&gt;" |,
 *       "instances": ["&lt;instance id&gt; |... ,"&lt;other instance ids&gt;]"
 *     }
 *     | &lt;optionally other scenarios&gt;
 *     ...
 *     ,{ "name": "&lt;scenario name&gt;" |,
 *        "instances": ["&lt;instance id&gt; |... ,"&lt;other instance ids&gt;]"
 *     }
 *   ]
 * }
 * </pre>
 * 
 * The json object should provide an array with objects containing either the name of a scenario or
 * the name of the scenario and an array with a list of instance ids. All referred instances or scenarios
 * (if no instances were given for the scenario) will be deleted.
 * <p>
 * The response will have an object with the referred scenarios as attribute keys, and having as values either
 * a boolean value indicating if the scenario was found and deleted (false is expected if a non-existent scenario
 * was referred) or an object containing scenario instance ids as attribute keys and a boolean value indicating
 * if the specific instance has been deleted (as in the case of the scenarios, a false will be returned if a
 * non-existent instance if was referred.
 * <p>
 * As an example where scenario 1 was requested to be deleted, but non-existent, and two instances of scenario 2
 * were requested to be deleted where only the first instance existed:
 * <pre>
 * { "scenario1": false
 * , "scenario2": {
 *     "instance1": true
 *   , "instance2": false }
 * }
 * </pre>
 * 
 * @author Jo&atilde;o Viegas (joao.viegas@mindprogeny.com)
 * @since 25 Apr 2018
 *
 */
public class DeleteScenariosWithBody implements AdminTask {
    
    /**
     * Object Mapper to deserialize maps
     */
    private ObjectMapper jsonMapper = new ObjectMapper();

    /**
     * @see com.github.tomakehurst.wiremock.admin.AdminTask#execute(com.github.tomakehurst.wiremock.core.Admin, com.github.tomakehurst.wiremock.http.Request, com.github.tomakehurst.wiremock.admin.model.PathParams)
     */
    @SuppressWarnings("unchecked")
	@Override
    public ResponseDefinition execute(Admin admin, Request request, PathParams pathParams) {
    	Map<String,Object> result= new HashMap<>();
        try {
            Map<String, Collection<Map<String, Object>>> params = jsonMapper.readValue(request.getBodyAsString(), Map.class);
            for (Map<String, Object> scenario : params.get("scenarios")) {
            	String name = (String)scenario.get("name");
            	Collection<String> instances = (Collection<String>) scenario.get("instances");
            	if (instances == null) {
            		result.put(name, ConcurrentScenarioExtension.clearScenario(name));
            	} else {
            		Map<String,Boolean> instancesResult = new HashMap<>();
            		result.put(name, instancesResult);
            		for (String instance : instances) {
            			instancesResult.put(instance, ConcurrentScenarioExtension.clearInstance(name, instance));
            		}
            	}
            }
            return ResponseDefinitionBuilder.responseDefinition()
                                            .withStatus(HttpURLConnection.HTTP_OK)
                                            .withHeader("content-type", "application/json")
                                            .withBody(jsonMapper.writeValueAsString(result))
                                            .build();
        } catch (IOException ioe) {
            StringWriter writer = new StringWriter();
            ioe.printStackTrace(new PrintWriter(writer));
            return ResponseDefinitionBuilder.responseDefinition()
                                            .withStatus(HttpURLConnection.HTTP_BAD_REQUEST)
                                            .withStatusMessage("Scenario Parameters in incorrect format.")
                                            .withHeader("content-type", "text/text")
                                            .withBody("Expected format is:\n{\n  \"scenarios\": [\n    {\n      \"name\": \"<scenario name>\",\n      \"instances\": [\"<instance id>...\"]}\n  ]\n}")
                                            .build();
        }
    }

}
