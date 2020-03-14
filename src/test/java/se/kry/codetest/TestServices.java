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

  @Test
  @DisplayName("Add a new service")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void add_service_new(Vertx vertx, VertxTestContext testContext) {
    final Services s = new Services(new DBConnector(vertx));
    final String url = "http://kry.se";

    testContext.verify(() -> {
      s.add(url).setHandler(future_add -> {
        assertTrue(future_add.succeeded());

        testContext.completeNow();
      });
    });
  }

  @Test
  @DisplayName("Add an already existing service")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void add_service_existing(Vertx vertx, VertxTestContext testContext) {
    final Services s = new Services(new DBConnector(vertx));
    final String url = "http://kry.se";

    testContext.verify(() -> {
      s.add(url).setHandler(future_pre_add -> {
        s.add(url).setHandler(future_add -> {
          assertTrue(future_add.failed());
          assertEquals("service already exists", future_add.cause().getMessage());

          testContext.completeNow();
        });
      });
    });
  }

  @Test
  @DisplayName("Get an existing service")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void get_service_existing(Vertx vertx, VertxTestContext testContext) {
    final Services s = new Services(new DBConnector(vertx));
    final String url = "http://kry.se";

    testContext.verify(() -> {
      s.add(url).setHandler(future_pre_add -> {
        s.get(url).setHandler(future_get -> {
          assertTrue(future_get.succeeded());
          assertEquals(url, future_get.result().getString("url"));

          testContext.completeNow();
        });
      });
    });
  }

  @Test
  @DisplayName("Get a not-existing service")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void get_service_not_existing(Vertx vertx, VertxTestContext testContext) {
    final Services s = new Services(new DBConnector(vertx));
    final String url = "http://kry.se";

    testContext.verify(() -> {
      s.get(url).setHandler(future_get -> {
        assertTrue(future_get.succeeded());
        assertEquals(null, future_get.result());

        testContext.completeNow();
      });
    });
  }
}
