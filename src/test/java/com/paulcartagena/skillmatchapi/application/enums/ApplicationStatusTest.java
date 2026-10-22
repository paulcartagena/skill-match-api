package com.paulcartagena.skillmatchapi.application.enums;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationStatusTest {

    @ParameterizedTest(name = "{0} -> {1} allowed={2}")
    @CsvSource({
            "APPLIED,  REVIEWED, true",
            "APPLIED,  ACCEPTED, false",
            "APPLIED,  REJECTED, false",
            "APPLIED,  APPLIED,  false",
            "REVIEWED, ACCEPTED, true",
            "REVIEWED, REJECTED, true",
            "REVIEWED, APPLIED,  false",
            "ACCEPTED, REJECTED, false",
            "ACCEPTED, REVIEWED, false",
            "REJECTED, ACCEPTED, false",
            "REJECTED, REVIEWED, false"
    })
    void canTransitionTo_followsApplicationWorkflow(ApplicationStatus from, ApplicationStatus to, boolean allowed) {
        assertThat(from.canTransitionTo(to)).isEqualTo(allowed);
    }
}
