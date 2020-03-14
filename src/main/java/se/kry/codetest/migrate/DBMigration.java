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
      .compose(v -> addStatusColumn())
      .compose(v -> addNameColumn())
      .compose(v -> addCreatedAtColumn());
  }

  public static Future<Void> run(String environment) {
    Vertx vertx = Vertx.vertx();
    client = new DBConnector(vertx, environment);

    return createServiceTable()
      .compose(v -> addStatusColumn())
      .compose(v -> addNameColumn())
      .compose(v -> addCreatedAtColumn());
  }

  public static Future<Void> createServiceTable() {
    Future<Void> future = Future.future();

    client.query("CREATE TABLE IF NOT EXISTS service (url VARCHAR(128) NOT NULL)").setHandler(done -> {
      if (done.succeeded()) {
        System.out.println("created service table");
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
        System.out.println("added status column to service table");
        future.complete();
      } else {
        done.cause().printStackTrace();
        future.complete();
      }
    });

    return future;
  }

  public static Future<Void> addNameColumn() {
    Future<Void> future = Future.future();

    // return complete in both case because SQlite do not handle
    // column creation if it already exists
    client.query("ALTER TABLE service ADD name VARCHAR(256)").setHandler(done -> {
      if (done.succeeded()) {
        System.out.println("added name column to service table");
        future.complete();
      } else {
        done.cause().printStackTrace();
        future.complete();
      }
    });

    return future;
  }

  public static Future<Void> addCreatedAtColumn() {
    Future<Void> future = Future.future();

    // return complete in both case because SQlite do not handle
    // column creation if it already exists
    client.query("ALTER TABLE service ADD created_at DATETIME").setHandler(done -> {
      if (done.succeeded()) {
        System.out.println("added created_at column to service table");
        future.complete();
      } else {
        done.cause().printStackTrace();
        future.complete();
      }
    });

    return future;
  }
}
