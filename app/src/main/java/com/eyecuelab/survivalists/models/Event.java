package com.eyecuelab.survivalists.models;


import android.os.Parcelable;

import org.parceler.Parcel;

/**
 * Created by eyecuelab on 6/3/16.
 */
@Parcel
public class Event implements Parcelable {
    String description;
    String outcomeA;
    String outcomeB;
    String title;
    int penaltyHP;
    int stepsRequired;
    boolean getItemOnFlee;
    boolean getItemOnInspect;

    public Event() {}

    public Event (String description, String outcomeA, String outcomeB, String title, int penaltyHP, int stepsRequired) {
        this.description = description;
        this.outcomeA = outcomeA;
        this.outcomeB = outcomeB;
        this.title = title;
        this.penaltyHP = penaltyHP;
        this.stepsRequired = stepsRequired;
    }
    public Event (Event otherEvent) {
        this.description = otherEvent.description;
        this.outcomeA = otherEvent.outcomeA;
        this.outcomeB = otherEvent.outcomeB;
        this.title = otherEvent.title;
        this.penaltyHP = otherEvent.penaltyHP;
        this.stepsRequired = otherEvent.stepsRequired;
    }


    protected Event(android.os.Parcel in) {
        description = in.readString();
        outcomeA = in.readString();
        outcomeB = in.readString();
        title = in.readString();
        penaltyHP = in.readInt();
        stepsRequired = in.readInt();
        getItemOnFlee = in.readByte() != 0;
        getItemOnInspect = in.readByte() != 0;
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(android.os.Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOutcomeA() {
        return outcomeA;
    }

    public void setOutcomeA(String outcomeA) {
        this.outcomeA = outcomeA;
    }

    public String getOutcomeB() {
        return outcomeB;
    }

    public void setOutcomeB(String outcomeB) {
        this.outcomeB = outcomeB;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPenaltyHP() {
        return penaltyHP;
    }

    public void setPenaltyHP(int penaltyHP) {
        this.penaltyHP = penaltyHP;
    }

    public int getStepsRequired() {
        return stepsRequired;
    }

    public void setStepsRequired(int stepsRequired) {
        this.stepsRequired = stepsRequired;
    }

    public boolean isGetItemOnFlee() {
        return getItemOnFlee;
    }

    public void setGetItemOnFlee(boolean getItemOnFlee) {
        this.getItemOnFlee = getItemOnFlee;
    }

    public boolean isGetItemOnInspect() {
        return getItemOnInspect;
    }

    public void setGetItemOnInspect(boolean getItemOnInspect) {
        this.getItemOnInspect = getItemOnInspect;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(android.os.Parcel dest, int flags) {
        dest.writeString(description);
        dest.writeString(outcomeA);
        dest.writeString(outcomeB);
        dest.writeString(title);
        dest.writeInt(penaltyHP);
        dest.writeInt(stepsRequired);
        dest.writeByte((byte) (getItemOnFlee ? 1 : 0));
        dest.writeByte((byte) (getItemOnInspect ? 1 : 0));
    }
}
