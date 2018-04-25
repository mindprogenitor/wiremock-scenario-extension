/*
 * SetScenarioSessionState.java, 25 Apr 2018
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
 * Admin task allowing to set the current state for a scenario's instance.
 * <p>
 * If the instance hasn't been triggered yet (no scenario stub for that particular instance has been accessed yet), a new scenario 
 * instance is created and initialized with the desired state.
 * <p>
 * All parameters come from the endpoint path
 * 
 * @author Jo&atilde;o Viegas (joao.viegas@mindprogeny.com)
 * @since 25 Apr 2018
 *
 */
public class SetScenarioSessionState implements AdminTask {

    /**
     * @see com.github.tomakehurst.wiremock.admin.AdminTask#execute(com.github.tomakehurst.wiremock.core.Admin, com.github.tomakehurst.wiremock.http.Request, com.github.tomakehurst.wiremock.admin.model.PathParams)
     */
    @Override
    public ResponseDefinition execute(Admin admin, Request request, PathParams pathParams) {
        ConcurrentScenarioExtension.setScenarioState(pathParams.get("scenario"), pathParams.get("instance"), pathParams.get("state"));
        return ResponseDefinitionBuilder.responseDefinition()
                                        .withStatus(HttpURLConnection.HTTP_OK)
                                        .build();
    }

}
