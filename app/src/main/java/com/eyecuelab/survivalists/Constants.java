package com.eyecuelab.survivalists;

/**
 * Created by eyecue on 5/6/16.
 */
public class Constants {
    public static final String FIREBASE_URL = BuildConfig.FIREBASE_ROOT_URL;
    public static final String FIREBASE_TEAM = "teams";
    public static final String FIREBASE_STEPS = "steps";
    public static final String FIREBASE_USERS = "users";
    public static final String FIREBASE_SAFEHOUSES = "safehouses";
    public static final String FIREBASE_EVENTS = "events";
    public static final String FIREBASE_ITEMS = "items";
    public static final String FIREBASE_CHARACTERS = "characters";
    public static final String FIREBASE_URL_TEAM = FIREBASE_URL + "/" + FIREBASE_TEAM;
    public static final String FIREBASE_URL_STEPS = FIREBASE_URL + "/" + FIREBASE_STEPS;
    public static final String FIREBASE_URL_USERS = FIREBASE_URL + "/" + FIREBASE_USERS;
    public static final String FIREBASE_URL_SAFEHOUSES = FIREBASE_URL + "/" + FIREBASE_SAFEHOUSES;
    public static final String FIREBASE_URL_EVENTS = FIREBASE_URL + "/" + FIREBASE_EVENTS;
    public static final String FIREBASE_URL_ITEMS = FIREBASE_URL + "/" + FIREBASE_ITEMS;
    public static final String FIREBASE_URL_CHARACTERS = FIREBASE_URL + "/" + FIREBASE_CHARACTERS;
    public static final String PREFERENCES_PREVIOUS_STEPS_KEY = "previousSteps";
    public static final String PREFERENCES_GOOGLE_PLAYER_ID = "playerId";
    public static final String PREFERENCES_MATCH_ID = "matchId";
    public static final String PREFERENCES_STEPS_IN_SENSOR_KEY = "stepsInSensor";
    public static final String PREFERENCES_DAILY_STEPS = "dailySteps";
    public static final String PREFERENCES_EVENT_1_STEPS = "eventOneSteps";
    public static final String PREFERENCES_EVENT_2_STEPS = "eventTwoSteps";
    public static final String PREFERENCES_EVENT_3_STEPS = "eventThreeSteps";
    public static final String PREFERENCES_EVENT_4_STEPS = "eventFourSteps";
    public static final String PREFERENCES_EVENT_5_STEPS = "eventFiveSteps";
    public static final String PREFERENCES_INITIATE_EVENT_1 = "initiateEvent1";
    public static final String PREFERENCES_INITIATE_EVENT_2 = "initiateEvent2";
    public static final String PREFERENCES_INITIATE_EVENT_3 = "initiateEvent3";
    public static final String PREFERENCES_INITIATE_EVENT_4 = "initiateEvent4";
    public static final String PREFERENCES_INITIATE_EVENT_5 = "initiateEvent5";
    public static final String PREFERENCES_LAST_SAFEHOUSE_ID = "lastSafehouseId";
    public static final String PREFERENCES_NEXT_SAFEHOUSE_ID = "nextSafehouseId";
    public static final String PREFERENCES_USER = "user";
    public static final String PREFERENCES_CHARACTER = "character";
    public static final String PREFERENCES_TEAM_IDs = "teamIDs";
    public static final String PREFERENCES_REACHED_SAFEHOUSE = "reachedSafehouse";
    public static final String PREFERENCES_DAILY_GOAL = "dailyGoal";
    public static final String PREFERENCES_DURATION_SETTING = "duration";
    public static final String PREFERENCES_DEFAULT_DAILY_GOAL_SETTING = "difficulty";
    public static final String PREFERENCES_TEAM_SIZE_SETTING = "teamSize";
    public static final int START_CAMPAIGN_INTENT = 22222;
    public static final int JOIN_CAMPAIGN_INTENT = 33333;
    public static final String INVITATION_INTENT_EXTRA = "extraExtra";
}
