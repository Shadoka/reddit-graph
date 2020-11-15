package com.shadoka.graph.repository;

import com.shadoka.graph.model.Author;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AuthorRepository extends CrudRepository<Author, Long> {

    public Author getAuthorByName(String name);

    @Query(
            value = "select author_id from authors_in where subreddit_id = ?1 " +
                    "intersect select author_id from authors_in where subreddit_id = ?2",
            nativeQuery = true
    )
    public List<Long> findCrossposterBetweenSubreddits(Long firstReddit, Long secondReddit);

    @Query("select a.name from Author a where a.id = ?1")
    public String getNameById(Long id);
}
