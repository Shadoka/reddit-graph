package com.shadoka.graph.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.*;

/**
 * Represents a subreddit from reddit.com.
 */
@Entity
public class Subreddit {

    public static final List<String> ALLOWED_SUBREDDITS = Arrays
            .asList("pathofexile",
                    "leagueoflegends",
                    "GlobalOffensive",
                    "funny",
                    "nfl",
                    "soccer");

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    private String bannerImageUrl;

    @ManyToMany
    @JoinTable(
            name = "authors_in",
            joinColumns = @JoinColumn(name = "subreddit_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private Set<Author> authors;

    @Enumerated(EnumType.ORDINAL)
    private Status status;

    private Timestamp importDate;

    public Subreddit() {
        this.authors = new HashSet<>();
        this.status = Status.LOADING;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subreddit subreddit = (Subreddit) o;
        return name.equals(subreddit.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
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

    public Set<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(Set<Author> authors) {
        this.authors = authors;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Timestamp getImportDate() {
        return importDate;
    }

    public void setImportDate(Timestamp importDate) {
        this.importDate = importDate;
    }

    public String getBannerImageUrl() {
        return bannerImageUrl;
    }

    public void setBannerImageUrl(String bannerImageUrl) {
        this.bannerImageUrl = bannerImageUrl;
    }
}
