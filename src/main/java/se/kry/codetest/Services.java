package se.kry.codetest;

import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class Services {
  private DBConnector client;

  public Services(DBConnector connector) {
    client = connector;
  }

  public Future<JsonObject> add(String url, String status, String name) {
    if (url == "") {
      return Future.failedFuture("empty service url");
    }
    if (name == "") {
      return Future.failedFuture("empty service name");
    }
    if (!Pattern.matches("[\\w\\d-_]{1,110}\\.[\\w.]{2,10}", url)) {
      return Future.failedFuture("invalid service url format");
    }

    Future<JsonObject> future = Future.future();

    get(url).setHandler(future_get -> {
      if (future_get.succeeded() && future_get.result() != null) {
        future.fail("service already exists");
      }
      else {
        Instant created_at = Instant.now();
        String sql_query = String.format(
          "INSERT INTO service VALUES(\"%s\", \"%s\", \"%s\", \"%s\")",
          url,
          status,
          name,
          created_at
        );

        client.query(sql_query).setHandler(future_add -> {
          if (future_add.succeeded()) {
            JsonObject newService = new JsonObject()
              .put("url", url)
              .put("name", name)
              .put("status", status)
              .put("created_at", created_at);

              future.complete(newService);
          } else {
            future_add.cause().printStackTrace();
            future.fail(future_add.cause());
          }
        });
      }
    });

    return future;
  }

  public Future<JsonObject> get(String url) {
    Future<JsonObject> future = Future.future();

    String sql_query = String.format("SELECT * FROM service WHERE url = \"%s\" LIMIT 1", url);
    client.query(sql_query).setHandler(future_query -> {
      if (future_query.succeeded()) {
        List<JsonObject> rows = future_query.result().getRows();
        if (rows.isEmpty()) {
          future.complete(null);
        } else {
          future.complete(rows.get(0));
        }
      } else {
        future_query.cause().printStackTrace();
        future.fail(future_query.cause());
      }
    });

    return future;
  }

  public Future<JsonObject> update(String url, String status) {
    if (url == "") {
      return Future.failedFuture("empty service url");
    }
    Future<JsonObject> future = Future.future();

    get(url).setHandler(future_get -> {
      if (future_get.failed() || future_get.result() == null) {
        future.fail("service does not exist");
      }
      else {
        String sql_query = String.format("UPDATE service SET status = \"%s\" WHERE url = \"%s\"", status, url);

        client.query(sql_query).setHandler(future_remove -> {
          if (future_remove.succeeded()) {
            JsonObject updatedService = future_get.result();
            updatedService.put("status", status);

            future.complete(updatedService);
          } else {
            future_remove.cause().printStackTrace();
            future.fail(future_remove.cause());
          }
        });
      }
    });

    return future;
  }

  public Future<List<JsonObject>> getAll() {
    Future<List<JsonObject>> future = Future.future();

    String sql_query = String.format("SELECT * FROM service");
    client.query(sql_query).setHandler(future_query -> {
      if (future_query.succeeded()) {
        List<JsonObject> rows = future_query.result().getRows();

        future.complete(rows);
      } else {
        future_query.cause().printStackTrace();
        future.fail(future_query.cause());
      }
    });

    return future;
  }

  public Future<JsonObject> remove(String url) {
    if (url == "") {
      return Future.failedFuture("empty service url");
    }
    Future<JsonObject> future = Future.future();

    get(url).setHandler(future_get -> {
      if (future_get.failed() || future_get.result() == null) {
        future.fail("service does not exist");
      }
      else {
        String sql_query = String.format("DELETE FROM service WHERE url = \"%s\"", url);

        client.query(sql_query).setHandler(future_remove -> {
          if (future_remove.succeeded()) {
            future.complete(future_get.result());
          } else {
            future_remove.cause().printStackTrace();
            future.fail(future_remove.cause());
          }
        });
      }
    });

    return future;
  }
}
