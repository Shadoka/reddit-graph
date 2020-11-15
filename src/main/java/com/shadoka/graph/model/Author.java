package com.shadoka.graph.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;
import java.util.*;

@Entity
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    @ManyToMany(mappedBy = "authors")
    private Set<Subreddit> postedIn;

    public Author() {
        this.postedIn = new HashSet<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Author author = (Author) o;
        return name.equals(author.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public String buildRedditLink() {
        return "https://www.reddit.com/u/" + this.getName();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Subreddit> getPostedIn() {
        return postedIn;
    }

    public void setPostedIn(HashSet<Subreddit> postedIn) {
        this.postedIn = postedIn;
    }
}
