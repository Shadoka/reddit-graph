package com.shadoka.graph.controller;

import com.shadoka.graph.exception.InvalidSubredditRequest;
import com.shadoka.graph.model.Author;
import com.shadoka.graph.model.Relation;
import com.shadoka.graph.model.Subreddit;
import com.shadoka.graph.outbound.FriendMapping;
import com.shadoka.graph.repository.AuthorRepository;
import com.shadoka.graph.repository.RelationRepository;
import com.shadoka.graph.repository.SubredditRepository;
import com.shadoka.graph.util.Validation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class UserController {

    @Autowired
    private SubredditRepository subredditRepo;

    @Autowired
    private AuthorRepository authorRepo;

    @Autowired
    private RelationRepository relationRepo;

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping(path = "/user/crossposter")
    public List<String> findCrossposter(@RequestParam String firstSubreddit, @RequestParam String secondSubreddit) {
        if (!Validation.isValidSubreddit(firstSubreddit) || !Validation.isValidSubreddit(secondSubreddit)) {
            throw new InvalidSubredditRequest();
        }

        Long firstId = this.subredditRepo.getIdByName(firstSubreddit);
        Long secondId = this.subredditRepo.getIdByName(secondSubreddit);

        List<Long> userIds = this.authorRepo.findCrossposterBetweenSubreddits(firstId, secondId);

        List<String> crossposter = userIds.stream()
                .map(this.authorRepo::getNameById)
                .collect(Collectors.toList());

        return crossposter;
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping(path = "/user/{subredditName}/friends")
    public FriendMapping getUserWithFriends(@PathVariable String subredditName) {
        if (!Validation.isValidSubreddit(subredditName)) {
            throw new InvalidSubredditRequest();
        }
        long start = System.currentTimeMillis();
        Subreddit sub = this.subredditRepo.getSubredditByName(subredditName);
        long endReadSub = System.currentTimeMillis();
        System.out.println("duration sub read: " + (endReadSub - start));

        Map<String, List<String>> userMap = new HashMap<>();
        for (Author a : sub.getAuthors()) {
            List<String> friends = new ArrayList<>();

            List<Relation> relations = this.relationRepo.findRelationByAuthorId(a.getId(), sub.getId());
            relations.forEach(rel -> {
                if (rel.getFrom().getName().equals(a.getName())) {
                    friends.add(rel.getTo().getName());
                } else {
                    friends.add(rel.getFrom().getName());
                }
            });

            userMap.put(a.getName(), friends);
        }

        long end = System.currentTimeMillis();
        System.out.println("duration: " + (end - start));

        FriendMapping mapping = new FriendMapping();
        mapping.setUserToFriend(userMap);
        return mapping;
    }
}
