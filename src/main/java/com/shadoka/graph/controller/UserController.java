package com.shadoka.graph.controller;

import com.shadoka.graph.exception.InvalidSubredditRequest;
import com.shadoka.graph.repository.AuthorRepository;
import com.shadoka.graph.repository.SubredditRepository;
import com.shadoka.graph.util.Validation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class UserController {

    @Autowired
    private SubredditRepository subredditRepo;

    @Autowired
    private AuthorRepository authorRepo;

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
}
