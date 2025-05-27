package com.iodigital.assignment.tedtalks.talk.repository.projection;

public interface InfluenceScore {

    String getSpeaker();
    Long getTalkCount();
    Long getTotalViews();
    Long getTotalLikes();
    Double getInfluenceScore();

}
