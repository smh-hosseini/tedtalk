package com.iodigital.assignment.tedtalks.talk.repository;

import com.iodigital.assignment.tedtalks.talk.repository.projection.YearlyTopTalk;
import com.iodigital.assignment.tedtalks.talk.model.TedTalk;
import com.iodigital.assignment.tedtalks.talk.repository.projection.InfluenceScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface TedTalkRepository extends JpaRepository<TedTalk, Long> {

    Optional<TedTalk> findByTitle(String title);

    /**
     * Find top N influential speakers based on a weighted score of views and likes
     *
     * @param limit number of records to return
     * @return list of top influential speakers with their scores
     */
    @Query("""
        SELECT
            t.speaker as speaker,
            COUNT(*) as talkCount,
            SUM(t.views) as totalViews,
            SUM(t.likes) as totalLikes,
            SUM(CAST(t.views AS double) * :viewsWeight) + SUM(CAST(t.likes AS double) * :likesWeight) as influenceScore
        FROM TedTalk t
        GROUP BY t.speaker
        ORDER BY SUM(CAST(t.views AS double) * :viewsWeight) + SUM(CAST(t.likes AS double) * :likesWeight) DESC
        LIMIT :limit
    """)
    List<InfluenceScore> findTopInfluentialSpeakers(@Param("limit") int limit,
                                                    @Param("viewsWeight") double viewsWeight,
                                                    @Param("likesWeight") double likesWeight);


    /**
     * Find the most influential TedTalk for each year
     *
     * @return list of the most influential TedTalk per year
     */
    @Query(value = """
        WITH ranked_talks AS (
            SELECT 
                t.id as talkId,
                t.title,
                t.speaker,
                EXTRACT(YEAR FROM t.date) AS year,
                t.views as totalViews,
                t.likes as totalLikes,
                (t.views * :viewsWeight + t.likes * :likesWeight) AS influenceScore,
                ROW_NUMBER() OVER (PARTITION BY EXTRACT(YEAR FROM t.date)
                                   ORDER BY (t.views * :viewsWeight + t.likes * :likesWeight) DESC) AS rn
            FROM ted_talks t
        )
        SELECT talkId, title, speaker, year, totalViews, totalLikes, influenceScore
        FROM ranked_talks
        WHERE rn = 1
        """, nativeQuery = true)
    List<YearlyTopTalk> findTopTedTalksPerYear(@Param("viewsWeight") double viewsWeight,
                                               @Param("likesWeight") double likesWeight);


    /**
     * Get ted talks by title, speaker, and date.
     *
     * @param title the title of the TedTalk
     * @param speaker the speaker of the TedTalk
     * @param date the date of the TedTalk
     * @return paginated list of TedTalks
     */
    Optional<TedTalk> findByTitleAndSpeakerAndDate(String title, String speaker, LocalDate date);

    /**
     * Get all TedTalks by a specific speaker
     *
     * @param speaker the speaker's name
     * @return list of TedTalks by the specified speaker
     */
    Stream<TedTalk> findAllBySpeaker(String speaker);
}