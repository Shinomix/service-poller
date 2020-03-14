package se.kry.codetest.migrate;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import se.kry.codetest.DBConnector;

public class DBMigration {

  private static DBConnector client;

  public static Future<Void> run() {
    Vertx vertx = Vertx.vertx();
    client = new DBConnector(vertx);

    return createServiceTable()
      .compose(v -> addStatusColumn());
  }

  public static Future<Void> run(String environment) {
    Vertx vertx = Vertx.vertx();
    client = new DBConnector(vertx, environment);

    return createServiceTable()
      .compose(v -> addStatusColumn());
  }

  public static Future<Void> createServiceTable() {
    Future<Void> future = Future.future();

    client.query("CREATE TABLE IF NOT EXISTS service (url VARCHAR(128) NOT NULL)").setHandler(done -> {
      if (done.succeeded()) {
        System.out.println("completed db migrations");
        future.complete();
      } else {
        done.cause().printStackTrace();
        future.fail(done.cause());
      }
    });

    return future;
  }

  public static Future<Void> addStatusColumn() {
    Future<Void> future = Future.future();

    // return complete in both case because SQlite do not handle
    // column creation if it already exists
    client.query("ALTER TABLE service ADD status VARCHAR(128)").setHandler(done -> {
      if (done.succeeded()) {
        System.out.println("completed db migrations");
        future.complete();
      } else {
        done.cause().printStackTrace();
        future.complete();
      }
    });

    return future;
  }
}
