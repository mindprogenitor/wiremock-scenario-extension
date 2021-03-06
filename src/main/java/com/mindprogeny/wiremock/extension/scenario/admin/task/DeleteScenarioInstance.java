/*
 * DeleteScenarioInstance.java, 24 Apr 2018
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

import java.net.HttpURLConnection;

import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.mindprogeny.wiremock.extension.scenario.ConcurrentScenarioExtension;

/**
 * Admin task to remove a scenario's specific instance from the repository.
 * <p>
 * This operation efectivelly resets a scenario instance
 * <p>
 * All parameters are expected to come from the endpoint.
 * 
 * @author Jo&atilde;o Viegas (joao.viegas@mindprogeny.com)
 * @since 24 Apr 2018
 *
 */
public class DeleteScenarioInstance implements AdminTask {

    /**
     * @see com.github.tomakehurst.wiremock.admin.AdminTask#execute(com.github.tomakehurst.wiremock.core.Admin, com.github.tomakehurst.wiremock.http.Request, com.github.tomakehurst.wiremock.admin.model.PathParams)
     */
    @Override
    public ResponseDefinition execute(Admin admin, Request request, PathParams pathParams) {
        
        if (!ConcurrentScenarioExtension.clearInstance(pathParams.get("scenario"),pathParams.get("instance"))) {
            return ResponseDefinitionBuilder.responseDefinition()
                                            .withStatus(HttpURLConnection.HTTP_NOT_FOUND)
                                            .build();
        }
        
        return ResponseDefinitionBuilder.responseDefinition()
                                        .withStatus(HttpURLConnection.HTTP_OK)
                                        .build();
    }

}
