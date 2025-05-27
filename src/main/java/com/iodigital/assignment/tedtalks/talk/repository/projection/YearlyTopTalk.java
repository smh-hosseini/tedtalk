package com.iodigital.assignment.tedtalks.talk.repository.projection;

public interface YearlyTopTalk extends InfluenceScore {

    Long getTalkId();
    String getTitle();
    Integer getYear();
}
