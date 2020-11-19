# reddit-graph

reddit-graph aims to be a tool to load publicly available data from reddit.
Specifically, it will load data from a given subreddit as a social graph into a relational database.

This data consists of all the users, who made a submission or post on a "hot" topic. By default, four pages are being 
loaded into the database. Loading data of a subreddit takes about 90-120 seconds via Reddit API (not a lot we can do 
about that) and additional ~30 seconds to persist the data into the database (presumably a lot we can do about this).
 The data model is probably bad and will be changed at a later date.

The application offers HTTP endpoints to query the data.

## how to run this application

* some properties are encrypted via [jasypt](http://www.jasypt.org/)
    * [usage cli](http://www.jasypt.org/cli.html) Otherwise, Stackoverflow might be your friend
    * which properties are encrypted?
        * *application.properties*: spring.datasource.password
        * *reddit_credentials.properties*: reddit.username, reddit.userpassword, reddit.clientId, reddit.clientSecret
    * run the application with **gradlew -Djasypt.encryptor.password=yourjasyptpassword bootRun** or edit run config in your IDE
* database is PostGreSQL, credentials are to be supplied via application.properties
* access to reddit developer api is neccessary, along with a user account. to be supplied in reddit_credentials.properties

## what endpoints are available?

Documentation will follow when I'm fairly sure that the interface is stable enough for it. For now, just dig a bit into
the *controller* package.

## why a relational database, isn't that a bit painful?

I'm doing this project to learn about Spring Boot and related technologies, as I will soon be working with them. So it's
a purely educational thing for me.

Maybe I will add support for a key-value or graph database at a later date.