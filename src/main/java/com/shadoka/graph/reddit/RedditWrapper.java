package com.shadoka.graph.reddit;

import com.shadoka.graph.model.Author;
import com.shadoka.graph.model.Relation;
import com.shadoka.graph.model.Status;
import com.shadoka.graph.model.Subreddit;
import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkAdapter;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.models.*;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;
import net.dean.jraw.pagination.DefaultPaginator;
import net.dean.jraw.tree.CommentNode;
import net.dean.jraw.tree.ReplyCommentNode;
import net.dean.jraw.tree.RootCommentNode;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Component
public class RedditWrapper {

    private static final String NESTED_SUBMISSION_ERROR = "there was a submission as a reply in another submission!";

    private final RedditClient client;

    @Autowired
    public RedditWrapper(@Value("${reddit.username}") String userName,
                         @Value("${reddit.userpassword}") String userPw,
                         @Value("${reddit.clientId}") String clientId,
                         @Value("${reddit.clientSecret}") String clientSecret) {
        UserAgent agent = new UserAgent("bot", "com.shadoka.reddit.data", "0.0.1", "theshadoka");
        Credentials cred = Credentials.script(userName, userPw, clientId, clientSecret);
        NetworkAdapter adapter = new OkHttpNetworkAdapter(agent);
        this.client = OAuthHelper.automatic(adapter, cred);
    }

    public Pair<Subreddit, Set<Relation>> readSubreddit(String name) {
        long start = System.currentTimeMillis();
        DefaultPaginator<Submission> paginator =  this.client.subreddit(name).posts().sorting(SubredditSort.TOP).build();
        List<Listing<Submission>> posts = paginator.accumulate(4);

        Subreddit subreddit = new Subreddit();
        subreddit.setName(name);

        Map<String, Author> authorMap = new HashMap<>();
        Map<Integer, String> depthMap = new HashMap<>();
        Set<Relation> relations = new HashSet<>();

        for (Listing<Submission> submissionListing : posts) {
            for (Submission submission : submissionListing.getChildren()) {
                RootCommentNode rootComment = this.getFullyReadRootComment(submission);

                int lastDepth = -1;
                String lastAuthor = "";
                Iterator<CommentNode<PublicContribution<?>>> it = rootComment.walkTree().iterator();
                while (it.hasNext()) {
                    CommentNode<PublicContribution<?>> commentNode = it.next();
                    int currentDepth = commentNode.getDepth();
                    // we went up into another thread
                    if (lastDepth > currentDepth) {
                        // -1 is safe because only the submission is depth = 0
                        lastAuthor = depthMap.get(Integer.valueOf(currentDepth - 1));
                    }
                    PublicContribution<?> contribution = commentNode.getSubject();
                    if (contribution instanceof Comment) {
                        Author currentAuthor = this.processComment((Comment) contribution,
                                authorMap,
                                lastAuthor,
                                lastDepth,
                                subreddit,
                                relations);

                        if (currentDepth > lastDepth)  {
                            lastAuthor = currentAuthor.getName();
                        }
                        lastDepth = currentDepth;
                        depthMap.put(Integer.valueOf(currentDepth), currentAuthor.getName());
                    } else if (contribution instanceof Submission) {
                        Author submissionAuthor = this.processSubmission((Submission) contribution, authorMap, subreddit);

                        if (currentDepth != 0) {
                            throw new IllegalArgumentException(NESTED_SUBMISSION_ERROR);
                        }

                        depthMap.put(Integer.valueOf(0), submissionAuthor.getName());
                        lastDepth = 0;
                        lastAuthor = submissionAuthor.getName();
                    }
                }
            }
        }
        subreddit.setImportDate(Timestamp.from(Instant.now()));
        subreddit.setStatus(Status.IMPORTED);

        long end = System.currentTimeMillis();
        System.out.println("import " + name + " duration: " + (end - start) + "ms");

        return Pair.with(subreddit, relations);
    }

    private Author processSubmission(Submission submission,
                                     Map<String, Author> authorMap,
                                     Subreddit sub) {
        Author author = authorMap.getOrDefault(submission.getAuthor(), new Author());
        author.getPostedIn().add(sub);
        author.setName(submission.getAuthor());
        authorMap.put(author.getName(), author);

        sub.getAuthors().add(author);

        return author;
    }

    private Author processComment(Comment comment,
                                    Map<String, Author> authorMap,
                                    String lastParent,
                                    int lastDepth,
                                    Subreddit subreddit,
                                    Set<Relation> relations) {
        String name = comment.getAuthor();

        Author author = authorMap.getOrDefault(name, new Author());
        author.getPostedIn().add(subreddit);
        author.setName(name);

        Author parent = authorMap.get(lastParent);
        if (parent != null) {
            Relation relation = new Relation();
            relation.setSubreddit(subreddit);
            relation.setFrom(author);
            relation.setTo(parent);

            relations.add(relation);
        }
        authorMap.put(name, author);

        subreddit.getAuthors().add(author);

        return author;
    }

    private RootCommentNode getFullyReadRootComment(Submission submission) {
        // 'replaceMore' fetches only 100 comments
        RootCommentNode rootComment = this.client.submission(submission.getId()).comments();

        boolean continueLoading = false;
        while (continueLoading) {
            List<ReplyCommentNode> loadedReplies = rootComment.replaceMore(this.client);
            continueLoading = loadedReplies.size() > 0;
        }
        return rootComment;
    }
}
