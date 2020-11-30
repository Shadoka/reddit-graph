package com.shadoka.graph.repository;

import com.shadoka.graph.model.Relation;
import com.shadoka.graph.model.Subreddit;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Set;

public interface RelationRepository extends CrudRepository<Relation, Long> {

    public Set<Relation> findRelationsBySubreddit(Subreddit sub);

    @Query("select r from Relation r where r.from.id = ?1 or r.to.id = ?1 and r.subreddit.id = ?2")
    public List<Relation> findRelationByAuthorId(Long authorId, Long subredditId);
}
