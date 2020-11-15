package com.shadoka.graph.util;

import com.shadoka.graph.model.Subreddit;

public class Validation {

    public static boolean isValidSubreddit(String name) {
        return name != null
                && Subreddit.ALLOWED_SUBREDDITS.contains(name);
    }
}
