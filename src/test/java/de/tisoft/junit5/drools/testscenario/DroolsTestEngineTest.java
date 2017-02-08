package de.tisoft.junit5.drools.testscenario;

import org.drools.workbench.models.testscenarios.shared.Scenario;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class DroolsTestEngineTest {
    @Test
    void discover() {
        DroolsTestEngine droolsTestEngine = new DroolsTestEngine();
        TestDescriptor testDescriptor = droolsTestEngine.discover(null, UniqueId.forEngine(droolsTestEngine.getId()));
        assertThat(testDescriptor.getChildren()).extracting(TestDescriptor::getDisplayName)
                .containsExactly("test.scenario");
    }

    @Test
    void executeInvalidScenario() {
        Scenario scenario = new Scenario();

        DroolsTestEngine droolsTestEngine = new DroolsTestEngine();

        TestDescriptor rootTestDescriptor = new EngineDescriptor(UniqueId.forEngine("drools"), "root");
        TestDescriptor droolsTestDescriptor = new DroolsTestEngine.DroolsTestDescriptor(
                rootTestDescriptor.getUniqueId().append("engine", "drools"), scenario);
        rootTestDescriptor.addChild(droolsTestDescriptor);

        EngineExecutionListener engineExecutionListener = mock(EngineExecutionListener.class);

        droolsTestEngine.execute(new ExecutionRequest(rootTestDescriptor, engineExecutionListener, null));

        verify(engineExecutionListener).executionStarted(eq(droolsTestDescriptor));
        verify(engineExecutionListener).executionFinished(eq(droolsTestDescriptor),
                argThat(argument -> argument.getStatus() == TestExecutionResult.Status.FAILED));
        verifyNoMoreInteractions(engineExecutionListener);
    }

}