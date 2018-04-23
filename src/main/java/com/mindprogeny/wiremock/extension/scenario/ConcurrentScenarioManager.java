/*
 * ConcurrentScenarioManager.java, 23 Apr 2018
 * Created by Joao Viegas (joao.viegas@mindprogeny.com)
 *
 * Copyright (c) 2018 Mind Progeny.
 */
package com.mindprogeny.wiremock.extension.scenario;

import com.github.tomakehurst.wiremock.admin.Router;
import com.github.tomakehurst.wiremock.extension.AdminApiExtension;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.mindprogeny.wiremock.extension.scenario.admin.task.ListActiveScenarios;

/**
 * Admin Extension providing api endpoints to manage the scenario repository.
 * 
 * @author Jo&atilde;o Viegas (joao.viegas@mindprogeny.com)
 * @since 23 Apr 2018
 *
 */
public class ConcurrentScenarioManager implements AdminApiExtension {

    /**
     * @see com.github.tomakehurst.wiremock.extension.Extension#getName()
     */
    @Override
    public String getName() {
        return "concurrent-scenarios-admin";
    }

    /**
     * @see com.github.tomakehurst.wiremock.extension.AdminApiExtension#contributeAdminApiRoutes(com.github.tomakehurst.wiremock.admin.Router)
     */
    @Override
    public void contributeAdminApiRoutes(Router router) {
        
        router.add(RequestMethod.GET, "/concurrent-scenarios", ListActiveScenarios.class);
    }

}