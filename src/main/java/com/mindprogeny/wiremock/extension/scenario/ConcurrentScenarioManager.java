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
import com.mindprogeny.wiremock.extension.scenario.admin.task.ClearScenarios;
import com.mindprogeny.wiremock.extension.scenario.admin.task.DeleteScenario;
import com.mindprogeny.wiremock.extension.scenario.admin.task.DeleteScenarioInstance;
import com.mindprogeny.wiremock.extension.scenario.admin.task.DeleteScenariosWithBody;
import com.mindprogeny.wiremock.extension.scenario.admin.task.GetScenario;
import com.mindprogeny.wiremock.extension.scenario.admin.task.ListActiveScenarios;
import com.mindprogeny.wiremock.extension.scenario.admin.task.SetScenarioSessionState;

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
        router.add(RequestMethod.GET, "/concurrent-scenarios/{scenario}", GetScenario.class);
        router.add(RequestMethod.DELETE, "/concurrent-scenarios", DeleteScenariosWithBody.class);
        router.add(RequestMethod.DELETE, "/concurrent-scenarios/all", ClearScenarios.class);
        router.add(RequestMethod.DELETE, "/concurrent-scenarios/{scenario}", DeleteScenario.class);
        router.add(RequestMethod.DELETE, "/concurrent-scenarios/{scenario}/{instance}", DeleteScenarioInstance.class);
        router.add(RequestMethod.PUT, "/concurrent-scenarios/{scenario}/{instance}/{state}", SetScenarioSessionState.class);

    }

}
