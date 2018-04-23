/*
 * ClearScenarios.java,, 23 Apr 2018
 * Created by Joao Viegas (joao.viegas@mindprogeny.com)
 *
 * Copyright (c) 2018 Mind Progeny.
 */
package com.mindprogeny.wiremock.extension.scenario.admin.task;

import java.net.HttpURLConnection;

import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.mindprogeny.wiremock.extension.scenario.ConcurrentScenarioExtension;

/**
 * Admin task to clear all scenarios.
 * <p>
 * This is a good operation to execute alongside a stub reset.
 * 
 * @author Jo&atilde;o Viegas (joao.viegas@mindprogeny.com)
 * @since 23 Apr 2018
 *
 */
public class ClearScenarios implements AdminTask {

    /**
     * @see com.github.tomakehurst.wiremock.admin.AdminTask#execute(com.github.tomakehurst.wiremock.core.Admin, com.github.tomakehurst.wiremock.http.Request, com.github.tomakehurst.wiremock.admin.model.PathParams)
     */
    @Override
    public ResponseDefinition execute(Admin admin, Request request, PathParams pathParams) {
        
        ConcurrentScenarioExtension.clearAll();
        
        return ResponseDefinitionBuilder.responseDefinition()
                                        .withStatus(HttpURLConnection.HTTP_OK)
                                        .build();
    }

}
