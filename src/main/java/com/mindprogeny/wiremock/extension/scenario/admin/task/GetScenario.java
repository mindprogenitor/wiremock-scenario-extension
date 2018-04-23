/*
 * GetScenario.java, 23 Apr 2018
 * Created by Joao Viegas (joao.viegas@mindprogeny.com)
 *
 * Copyright (c) 2018 Mind Progeny.
 */
package com.mindprogeny.wiremock.extension.scenario.admin.task;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;

import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.mindprogeny.wiremock.extension.scenario.ConcurrentScenarioExtension;

import wiremock.com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Admin task to show all the existing Scenario instances and their current states.
 * 
 * @author Jo&atilde;o Viegas (joao.viegas@mindprogeny.com)
 * @since 23 Apr 2018
 *
 */
public class GetScenario implements AdminTask {
    
    /**
     * @see com.github.tomakehurst.wiremock.admin.AdminTask#execute(com.github.tomakehurst.wiremock.core.Admin, com.github.tomakehurst.wiremock.http.Request, com.github.tomakehurst.wiremock.admin.model.PathParams)
     */
    @Override
    public ResponseDefinition execute(Admin admin, Request request, PathParams pathParams) {
        try {
            return ResponseDefinitionBuilder.responseDefinition()
                                            .withStatus(HttpURLConnection.HTTP_OK)
                                            .withHeader("content-type", "application/json")
                                            .withBody(ConcurrentScenarioExtension.serializeScenarioInstances(pathParams.get("scenario")))
                                            .build();
        } catch (JsonProcessingException jpe) {
            StringWriter writer = new StringWriter();
            jpe.printStackTrace(new PrintWriter(writer));
            return ResponseDefinitionBuilder.responseDefinition()
                                            .withStatus(HttpURLConnection.HTTP_INTERNAL_ERROR)
                                            .withStatusMessage("(WireMock) Not able to serialize Scenarios.")
                                            .withHeader("content-type", "text/text")
                                            .withBody(writer.toString())
                                            .build();
        }
    }

}
