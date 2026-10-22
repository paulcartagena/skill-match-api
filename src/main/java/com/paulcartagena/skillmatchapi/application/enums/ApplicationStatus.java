package com.paulcartagena.skillmatchapi.application.enums;

public enum ApplicationStatus {
    APPLIED, REVIEWED, ACCEPTED, REJECTED;

    // APPLIED -> REVIEWED -> ACCEPTED | REJECTED; ACCEPTED and REJECTED are final
    public boolean canTransitionTo(ApplicationStatus next) {
        return switch (this) {
            case APPLIED -> next == REVIEWED;
            case REVIEWED -> next == ACCEPTED || next == REJECTED;
            case ACCEPTED, REJECTED -> false;
        };
    }
}
