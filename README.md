# KRY code assignment

* [About](#about)
* [Tasks](#tasks)
* [Building](#building)
* [Trade-offs](#trade-offs)
* [Going further](#going-further)

## About
One of our developers built a simple service poller.
The service consists of a backend service written in Vert.x (https://vertx.io/) that keeps a list of services (defined by a URL), and periodically does a HTTP GET to each and saves the response ("OK" or "FAIL").

Unfortunately, the original developer din't finish the job, and it's now up to you to complete the thing.
Some of the issues are critical, and absolutely need to be fixed for this assignment to be considered complete.
There is also a wishlist of features in two separate tracks - if you have time left, please choose *one* of the tracks and complete as many of those issues as you can.

Spend maximum four hours working on this assignment - make sure to finish the issues you start.

Put the code in a git repo on GitHub and send us the link (niklas.holmqvist@kry.se) when you are done.

Good luck!


## Tasks
Critical issues (required to complete the assignment):

- ~Whenever the server is restarted, any added services disappear~
- ~There's no way to delete individual services~
- ~We want to be able to name services and remember when they were added~
- ~The HTTP poller is not implemented~

Frontend/Web track:
- We want full create/update/delete functionality for services
- The results from the poller are not automatically shown to the user (you have to reload the page to see results)
- We want to have informative and nice looking animations on add/remove services

Backend track
- Simultaneous writes sometimes causes strange behavior
- Protect the poller from misbehaving services (for example answering really slowly)
- ~Service URL's are not validated in any way ("sdgf" is probably not a valid service)~
- A user (with a different cookie/local storage) should not see the services added by another user


## Building
We recommend using IntelliJ as it's what we use day to day at the KRY office.
In intelliJ, choose
```
New -> New from existing sources -> Import project from external model -> Gradle -> select "use gradle wrapper configuration"
```

You can also run gradle directly from the command line:
```
./gradle clean run
```


## Trade-offs
- Regarding code architecture, to win some time I decided to not segregate the controllers and the presenters, both now in the MainVerticle. However, with more time I would separate the presenters for each endpoint (succcess and errors) in different classes so we can adjust and test easily what is returned by the server.
- Regarding database migrations, I may have misundertood the usage to do of the DBMigration class as I don't have a deep knowledge of Gradle and the Java ecosystem. For this reason, I decided to separate the dev and test databases and run the migrations when the MainVerticle is started. In a production situation, migrations would be run by the CI independently and before the application is deployed and run on the servers/containers.
- As I couldn't get `gradlew` to work on my mac, I installed `gradle` using brew and ended up with the latest 6.2.2 version. To make the tasks work I commited a change on the `build.gradle` but the `gradle build` does not work because of an outdated shadowJar dependency. This could be fixed bump that dependency.


## Going further
- **Simultaneous writing**: this can happen when trying to insert multiple times at the time as the first insert locks the table and makes the other requests fail. We could add an explicit lock on the insert and make all the requests wait or push the insertions in a Hashmap and have a vertx periodic dequeue and insert the services synchronously.
- **Services segregation by user**: when reaching the homepage, end users could create a session and be given a unique ID stored in a different table. Then, when creating or fetching services, end users can provide their session ID and only interact with their service. Separating this identity in a different store would allow to extend it with other features (sign-in, more detailed identity, ...)
