package com.shadoka.graph.repository;

import com.shadoka.graph.model.Subreddit;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository for all things related to {@link com.shadoka.graph.model.Subreddit}.
 */
public interface SubredditRepository extends CrudRepository<Subreddit, Long> {

    /**
     * Searches for a Subreddit with given name in the database and returns it.
     * @param name Name of the Subreddit.
     * @return {@link com.shadoka.graph.model.Subreddit}
     */
    public Subreddit getSubredditByName(String name);

    /**
     * Searches for a Subreddit with given name and returns its ID.
     * @param name Name of the Subreddit.
     * @return PK of the Subreddit as Long
     */
    @Query(
            value = "select id from subreddit where name = ?1",
            nativeQuery = true
    )
    public Long getIdByName(String name);

    /**
     * Searches for a Subreddit with given name and returns a URL where the current banner image can be found.
     * @param name Name of the Subreddit.
     * @return URL where the current banner image of the subreddit can be found
     */
    @Query(
            value = "select banner_image_url from subreddit where name = ?1",
            nativeQuery = true
    )
    public String getImageUrlByName(String name);
}
