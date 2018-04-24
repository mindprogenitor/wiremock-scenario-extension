/*
 * DeleteScenario.java, 24 Apr 2018
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
 * Admin task to remove a scenario and all its instances from the repository.
 * <p>
 * This operation can also effectively be used as a scenario reset.
 * 
 * @author Jo&atilde;o Viegas (joao.viegas@mindprogeny.com)
 * @since 24 Apr 2018
 *
 */
public class DeleteScenario implements AdminTask {

    /**
     * @see com.github.tomakehurst.wiremock.admin.AdminTask#execute(com.github.tomakehurst.wiremock.core.Admin, com.github.tomakehurst.wiremock.http.Request, com.github.tomakehurst.wiremock.admin.model.PathParams)
     */
    @Override
    public ResponseDefinition execute(Admin admin, Request request, PathParams pathParams) {
        
        if (!ConcurrentScenarioExtension.clearScenario(pathParams.get("scenario"))) {
            return ResponseDefinitionBuilder.responseDefinition()
                                            .withStatus(HttpURLConnection.HTTP_NOT_FOUND)
                                            .build();
        }
        
        return ResponseDefinitionBuilder.responseDefinition()
                                        .withStatus(HttpURLConnection.HTTP_OK)
                                        .build();
    }

}
