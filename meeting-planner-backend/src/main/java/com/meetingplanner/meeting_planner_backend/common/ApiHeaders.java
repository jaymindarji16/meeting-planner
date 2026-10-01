package com.meetingplanner.meeting_planner_backend.common;

/**
 * HTTP headers used by the API.
 * Centralised so a rename only touches one place.
 */
public final class ApiHeaders {

    private ApiHeaders() {}

    /** Prototype auth: client sends the logged-in user's id in this header. */
    public static final String USER_ID = "X-User-Id";
}