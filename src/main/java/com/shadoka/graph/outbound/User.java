package com.shadoka.graph.outbound;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.shadoka.graph.model.Author;
import com.shadoka.graph.model.Relation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "name"
)
public class User {

    private String name;

    private String redditLink;

    private List<User> friends;

    private User() {
        this.friends = new ArrayList<>();
    }

    /**
     * Creates a new User based on an Author but without their friend relations.
     * @param author The Author the newly created User is based on
     * @return User
     */
    public static User createFromAuthor(Author author) {
        User newUser = new User();
        newUser.setName(author.getName());
        newUser.setRedditLink(author.buildRedditLink());

        return newUser;
    }

    public User addRelations(Map<String, User> userMap, List<Relation> relations) {
        for (Relation r : relations) {
            Author other = r.getFrom().getName().equals(this.name) ? r.getTo() : r.getFrom();
            this.friends.add(userMap.get(other.getName()));
        }

        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRedditLink() {
        return redditLink;
    }

    public void setRedditLink(String redditLink) {
        this.redditLink = redditLink;
    }

    public List<User> getFriends() {
        return friends;
    }

    public void setFriends(List<User> friends) {
        this.friends = friends;
    }
}
