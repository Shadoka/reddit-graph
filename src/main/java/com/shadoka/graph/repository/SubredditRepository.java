package com.shadoka.graph.repository;

import com.shadoka.graph.model.Subreddit;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface SubredditRepository extends CrudRepository<Subreddit, Long> {

    public Subreddit getSubredditByName(String name);

    @Query(
            value = "select id from subreddit where name = ?1",
            nativeQuery = true
    )
    public Long getIdByName(String name);
}
