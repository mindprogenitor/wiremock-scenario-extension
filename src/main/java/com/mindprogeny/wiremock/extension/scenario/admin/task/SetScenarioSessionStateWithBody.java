/*
 * SetScenarioSessionStateWithBody.java, 25 Apr 2018
 * Created by Joao Viegas (joao.viegas@mindprogeny.com)
 *
 * Copyright (c) 2018 Mind Progeny.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mindprogeny.wiremock.extension.scenario.admin.task;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Map.Entry;

import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.mindprogeny.wiremock.extension.scenario.ConcurrentScenarioExtension;

import wiremock.com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Admin task allowing to set the current state for a scenario's instance, taking the parameters as a json body.
 * <p>
 * This task is preferred when instance id's might have characters that do not resolve well as path parameters (namely the forward slash)
 * <p>
 * If the instance hasn't been triggered yet (no scenario stub for that particular instance has been accessed yet), a new scenario 
 * instance is created and initialized with the desired state.
 * 
 * @author Jo&atilde;o Viegas (joao.viegas@mindprogeny.com)
 * @since 25 Apr 2018
 *
 */
public class SetScenarioSessionStateWithBody implements AdminTask {
    
    /**
     * Object Mapper to deserialize maps
     */
    private ObjectMapper jsonMapper = new ObjectMapper();

    /**
     * @see com.github.tomakehurst.wiremock.admin.AdminTask#execute(com.github.tomakehurst.wiremock.core.Admin, com.github.tomakehurst.wiremock.http.Request, com.github.tomakehurst.wiremock.admin.model.PathParams)
     */
    @Override
    @SuppressWarnings("unchecked")
    public ResponseDefinition execute(Admin paramAdmin, Request request, PathParams pathParams) {
        try {
            Map<String, Map<String, String>> params = jsonMapper.readValue(request.getBodyAsString(), Map.class);
            for (String scenario : params.keySet()) {
            	for (Entry<String,String> instanceState: params.get(scenario).entrySet()) {
            		ConcurrentScenarioExtension.setScenarioState(scenario, instanceState.getKey(), instanceState.getValue());;
            	}
            }
            return ResponseDefinitionBuilder.responseDefinition()
                                            .withStatus(HttpURLConnection.HTTP_OK)
                                            .build();
        } catch (IOException ioe) {
            StringWriter writer = new StringWriter();
            ioe.printStackTrace(new PrintWriter(writer));
            return ResponseDefinitionBuilder.responseDefinition()
                                            .withStatus(HttpURLConnection.HTTP_BAD_REQUEST)
                                            .withStatusMessage("Scenario Parameters in incorrect format.")
                                            .withHeader("content-type", "text/text")
                                            .withBody("Expected format is:\n{\n  \"<scenario name>\": {\n    \"<instance id>\": \"<state>\"}\n}")
                                            .build();
        }
    }
}
