package se.kry.codetest;

import java.util.List;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;

public class Services {
  private DBConnector client;

  public Services(DBConnector connector) {
    client = connector;
  }

  public Future<ResultSet> add(String url) {
    if (url == "") {
      return Future.failedFuture("empty service url");
    }
    Future<ResultSet> future = Future.future();

    get(url).setHandler(future_get -> {
      if (future_get.succeeded() && future_get.result() != null) {
        future.fail("service already exists");
      }
      else {
        String sql_query = String.format("INSERT INTO service VALUES(\"%s\")", url);

        client.query(sql_query).setHandler(future_add -> {
          if (future_add.succeeded()) {
            future.complete(future_add.result());
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
}