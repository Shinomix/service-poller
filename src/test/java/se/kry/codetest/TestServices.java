package se.kry.codetest;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import se.kry.codetest.migrate.DBMigration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
public class TestServices {

  @BeforeEach
  void create_and_empty_database(Vertx vertx, VertxTestContext testContext) {
    DBConnector client = new DBConnector(vertx);

    client.query("DROP TABLE IF EXISTS service")
          .compose(v -> DBMigration.run())
          .setHandler(v -> testContext.completeNow());
  }

  @Test
  @DisplayName("Add a service with an empty URL")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void add_service_empty_url(Vertx vertx, VertxTestContext testContext) {
    final Services s = new Services(new DBConnector(vertx));
    final String url = "";

    testContext.verify(() -> {
      s.add(url).setHandler(future_add -> {
        assertTrue(future_add.failed());
        assertEquals("empty service url", future_add.cause().getMessage());

        testContext.completeNow();
      });
    });
  }
}
