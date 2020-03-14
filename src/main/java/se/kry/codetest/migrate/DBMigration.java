package se.kry.codetest.migrate;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import se.kry.codetest.DBConnector;

public class DBMigration {

  public static Future<Void> run() {
    Future<Void> future = Future.future();
    Vertx vertx = Vertx.vertx();
    DBConnector connector = new DBConnector(vertx);

    connector.query("CREATE TABLE IF NOT EXISTS service (url VARCHAR(128) NOT NULL)").setHandler(done -> {
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
}
