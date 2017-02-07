package de.tisoft.junit5.drools.testscenario;

import org.drools.workbench.models.testscenarios.backend.ScenarioRunner;
import org.drools.workbench.models.testscenarios.backend.util.ScenarioXMLPersistence;
import org.drools.workbench.models.testscenarios.shared.Scenario;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DroolsTestEngine implements TestEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(DroolsTestEngine.class);

    @Override
    public String getId() {
        return "drools";
    }

    @Override
    public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
        EngineDescriptor engineDescriptor = new EngineDescriptor(uniqueId, "Drools Scenario Engine");
        Reflections reflections = new Reflections("", new ResourcesScanner());
        reflections.getResources(Pattern.compile(".*\\.scenario")).forEach(f -> {
            engineDescriptor.addChild(new AbstractTestDescriptor(uniqueId.append("scenario", f), f) {
                {
                    //TODO: this should really be a file source, but the RunListAdapter does not support that yet
                    setSource(new ClassSource(DroolsTestEngine.class.getName()));
                }
                @Override
                public boolean isContainer() {
                    return false;
                }

                @Override
                public boolean isTest() {
                    return true;
                }
            });
        });

        return engineDescriptor;
    }

    @Override
    public void execute(ExecutionRequest request) {
        request.getRootTestDescriptor().getChildren().forEach(td -> {
            request.getEngineExecutionListener().executionStarted(td);
            Scenario scenario = ScenarioXMLPersistence.getInstance().unmarshal(
                    new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/" + td.getDisplayName())))
                            .lines().collect(Collectors.joining("\n")));

            KieServices ks = KieServices.Factory.get();
            KieContainer kieClasspathContainer = ks.getKieClasspathContainer();

            try {
                KieSession kieSession = kieClasspathContainer.newKieSession(scenario.getKSessions().get(0));
                ScenarioRunner scenarioRunner = new ScenarioRunner(kieSession);

                LOGGER.info("Running scenario " + scenario.getName());
                scenarioRunner.run(scenario);

                if (scenario.wasSuccessful()) {
                    request.getEngineExecutionListener().executionFinished(td, TestExecutionResult.successful());
                } else {
                    String message = scenario.getFailureMessages().stream().collect(Collectors.joining(", "));
                    LOGGER.error(message);
                    request.getEngineExecutionListener().executionFinished(td,
                            TestExecutionResult.failed(new RuntimeException(message)));
                }
            } catch (Exception e) {
                request.getEngineExecutionListener().executionFinished(td, TestExecutionResult.failed(e));
            }
        });

    }
}
