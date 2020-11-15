package com.shadoka.graph.model;

import javax.persistence.*;
import java.util.Objects;

@Entity
public class Relation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Subreddit subreddit;

    @ManyToOne
    private Author from;

    @ManyToOne
    private Author to;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Relation relation = (Relation) o;
        return subreddit.equals(relation.subreddit) &&
                (from.equals(relation.to) || from.equals(relation.from)) &&
                (to.equals(relation.to) || to.equals(relation.from));
    }

    @Override
    public int hashCode() {
        return Objects.hash(subreddit, from, to);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Subreddit getSubreddit() {
        return subreddit;
    }

    public void setSubreddit(Subreddit subreddit) {
        this.subreddit = subreddit;
    }

    public Author getFrom() {
        return from;
    }

    public void setFrom(Author from) {
        this.from = from;
    }

    public Author getTo() {
        return to;
    }

    public void setTo(Author to) {
        this.to = to;
    }
}
