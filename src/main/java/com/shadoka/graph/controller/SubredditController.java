package com.shadoka.graph.controller;

import com.shadoka.graph.exception.InvalidSubredditRequest;
import com.shadoka.graph.model.Author;
import com.shadoka.graph.model.Relation;
import com.shadoka.graph.model.Status;
import com.shadoka.graph.model.Subreddit;
import com.shadoka.graph.outbound.ImageUrl;
import com.shadoka.graph.outbound.User;
import com.shadoka.graph.reddit.RedditWrapper;
import com.shadoka.graph.repository.AuthorRepository;
import com.shadoka.graph.repository.RelationRepository;
import com.shadoka.graph.repository.SubredditRepository;
import com.shadoka.graph.util.Validation;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class SubredditController {

    private static final long HOURS_24 = 1000 * 60 * 60 * 24;

    @Autowired
    private SubredditRepository subredditRepo;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private RelationRepository relationRepository;

    @Autowired
    private RedditWrapper redditClient;

    /**
     * Returns a list of names of available subreddits.
     * @return list of names of available subreddits.
     */
    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping(path = "/subreddits/available", produces = "application/hal+json")
    public List<String> getSubredditsAvailable() {
        return Subreddit.ALLOWED_SUBREDDITS;
    }

    /**
     * Returns the status of specified subreddit.
     * Returnvalue is stringification of Status.
     * @param name : Name of subreddit.
     * @return Status of subreddit or 'INVALID NAME' if no subreddit or status could be found.
     */
    @GetMapping(path = "/subreddits/status", produces = "application/hal+json")
    public String getStatus(@RequestParam String name) {
        Subreddit sub = this.subredditRepo.getSubredditByName(name);
        if (sub != null && sub.getStatus() != null) {
            return sub.getStatus().toString();
        }
        return "INVALID NAME";
    }

    /**
     * Returns the subreddit data which is gathered directly via Reddit API.<br>
     * This data is rather unprocessed and primarily for further use by this application and not a consumer.<br>
     * Calls to this function may take a considerable time (~2 minutes) if the data needs to be updated beforehand.
     * @param name name of the subreddit, of which the data is requested
     * @return Subreddit
     */
    @GetMapping(path = "/subreddits/{name}")
    public String loadData(@PathVariable String name) {
        if (!Validation.isValidSubreddit(name)) {
            throw new InvalidSubredditRequest();
        }

        Subreddit sub = this.subredditRepo.getSubredditByName(name);
        if (this.updateNeeded(sub)) {
            Pair<Subreddit, Set<Relation>> redditData = this.loadRedditData(name);
            sub = redditData.getValue0();
            Set<Relation> relations = redditData.getValue1();

            this.persistSubreddit(sub, relations);
        }

        return sub.getStatus().name();
    }

    /**
     * Returns all users related to this subreddit.
     * @param name name of the subreddit
     * @return list of Users
     */
    @GetMapping(path = "/subreddits/{name}/data")
    public List<User> getSubredditUserData(@PathVariable String name) {
        if (!Validation.isValidSubreddit(name)) {
            throw new InvalidSubredditRequest();
        }

        Subreddit sub = this.subredditRepo.getSubredditByName(name);

        Map<String, User> userMap = new HashMap<>(sub.getAuthors().size());
        sub.getAuthors().forEach(a -> userMap.put(a.getName(), User.createFromAuthor(a)));

        return sub.getAuthors().stream()
                .map(a -> Pair.with(a.getName(), this.relationRepository.findRelationByAuthorId(a.getId())))
                .map(tuple -> userMap.get(tuple.getValue0()).addRelations(userMap, tuple.getValue1()))
                .collect(Collectors.toList());
    }

    /**
     * Deletes the specified subreddit and all related data.
     * @param name : subreddit to delete
     */
    @GetMapping(path = "/subreddits/{name}/delete")
    public void delete(@PathVariable String name) {
        if (!Validation.isValidSubreddit(name)) {
            throw new InvalidSubredditRequest();
        }

        Subreddit sub = this.subredditRepo.getSubredditByName(name);
        Set<Relation> relations = this.relationRepository.findRelationsBySubreddit(sub);

        this.relationRepository.deleteAll(relations);

        Set<Author> toDelete = new HashSet<>();
        for (Author author : sub.getAuthors()) {
            if (author.getPostedIn().size() == 1) {
                toDelete.add(author);
            } else {
                author.getPostedIn().remove(sub);
            }
        }

        sub.getAuthors().clear();
        this.subredditRepo.save(sub);

        this.authorRepository.deleteAll(toDelete);
        this.subredditRepo.delete(sub);
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping(path = "/subreddits/{name}/image", produces = "application/hal+json")
    public ImageUrl getImageUrl(@PathVariable String name) {
        if (!Validation.isValidSubreddit(name)) {
            throw new InvalidSubredditRequest();
        }

        ImageUrl url = new ImageUrl();
        url.setUrl(this.subredditRepo.getImageUrlByName(name));
        return url;
    }

    /**
     * Checks if the given subreddit needs an update.<br>
     * This is the case under these conditions:
     * <ul>
     *     <li>the given subreddit is null</li>
     *     <li>the given subreddit is not in state 'IMPORTED'</li>
     *     <li>the last update was less than 24 hours before</li>
     * </ul>
     * @param sub the given subreddit.
     * @return true, if an update should be made. false otherwise.
     */
    private boolean updateNeeded(Subreddit sub) {
        long now = System.currentTimeMillis();
        return sub == null
                || sub.getStatus() != Status.IMPORTED
                || now - sub.getImportDate().getTime() > HOURS_24;
    }

    /**
     * Persists the whole subreddit structure with all it's associations into the database.<br>
     * Following process is followed:
     * <ol>
     *     <li>subreddit without authors is persisted</li>
     *     <li>all authors without relations to other authors are persisted</li>
     *     <li>relations between authors are persisted</li>
     *     <li>subreddit is persisted with all it's authors</li>
     * </ol>
     * @param sub the subreddit to persist
     * @param relations all relations between authors in the given subreddit
     */
    private void persistSubreddit(Subreddit sub, Set<Relation> relations) {
        Set<Author> authors = sub.getAuthors();
        sub.setAuthors(new HashSet<>());
        sub = this.subredditRepo.save(sub);

        this.persistAuthors(authors, sub);
        Set<Author> persistedAuthors = this.persistAuthorRelations(relations, authors);

        sub.setAuthors(persistedAuthors);
        this.subredditRepo.save(sub);
    }

    /**
     * Persists the relations between authors.<br>
     * The given map is a map from all the (relevant) authors to a list of their relations/partners.
     * @param relations Set of all authors of the current import process
     * @return set of all partners contained in the map, all persisted
     */
    private Set<Author> persistAuthorRelations(Set<Relation> relations, Set<Author> authors) {
        // we have to read all partners fresh from the database, to get the PKs
        Set<Author> refreshedAuthors = this.loadAuthors(authors);

        for (Relation relation : relations) {
            Author from = refreshedAuthors
                    .stream()
                    .filter(a -> a.getName().equals(relation.getFrom().getName()))
                    .findFirst().get();
            Author to = refreshedAuthors
                    .stream()
                    .filter(a -> a.getName().equals(relation.getTo().getName()))
                    .findFirst().get();

            relation.setFrom(from);
            relation.setTo(to);

            this.relationRepository.save(relation);
        }

        return refreshedAuthors;
    }

    /**
     * Reads all authors in the given set new from the database and returns them.
     * @param authors : Set of authors.
     * @return Set of authors, freshly read from the database.
     */
    private Set<Author> loadAuthors(Set<Author> authors) {
        return authors.stream()
                .map(author -> this.authorRepository.getAuthorByName(author.getName()))
                .collect(Collectors.toSet());
    }

    /**
     * Persists the given authors without their relations to each other.<br>
     * Those relations are instead being returned as a map.
     * @param authors authors to persist
     * @param sub subreddit those authors wrote in
     */
    private void persistAuthors(Set<Author> authors, Subreddit sub) {
        for (Author author : authors) {
            Author dbAuthor = this.authorRepository.getAuthorByName(author.getName());
            if (dbAuthor != null) {
                author = dbAuthor;
            }
            author.getPostedIn().add(sub);
            this.authorRepository.save(author);
        }
    }

    /**
     * Loads data from the given subreddit via Reddit API.
     * @param name name of subreddit to read
     * @return Subreddit
     */
    private Pair<Subreddit, Set<Relation>> loadRedditData(String name) {
        return redditClient.readSubreddit(name);
    }
}
