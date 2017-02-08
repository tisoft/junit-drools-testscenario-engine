package de.tisoft.junit5.drools.testscenario;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;

import static org.assertj.core.api.Assertions.assertThat;

class DroolsTestEngineTest {
    @Test
    void discover() {
        DroolsTestEngine droolsTestEngine = new DroolsTestEngine();
        TestDescriptor testDescriptor = droolsTestEngine.discover(null, UniqueId.forEngine(droolsTestEngine.getId()));
        assertThat(testDescriptor.getChildren()).extracting(TestDescriptor::getDisplayName)
                .containsExactly("de.tisoft.junit5.drools.testscenario.test/test.scenario");
    }

}