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
    DBConnector client = new DBConnector(vertx, "test");

    client.query("DROP TABLE IF EXISTS service")
          .compose(v -> DBMigration.run("test"))
          .setHandler(v -> testContext.completeNow());
  }

  @Test
  @DisplayName("Add a service with an empty URL")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void add_service_empty_url(Vertx vertx, VertxTestContext testContext) {
    final Services s = new Services(new DBConnector(vertx, "test"));
    final String url = "";
    final String status = "UNKNOWN";
    final String name = "KRY";

    testContext.verify(() -> {
      s.add(url, status, name).setHandler(future_add -> {
        assertTrue(future_add.failed());
        assertEquals("empty service url", future_add.cause().getMessage());

        testContext.completeNow();
      });
    });
  }

  @Test
  @DisplayName("Add a service with an empty name")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void add_service_empty_name(Vertx vertx, VertxTestContext testContext) {
    final Services s = new Services(new DBConnector(vertx, "test"));
    final String url = "http://kry.se";
    final String status = "UNKNOWN";
    final String name = "";

    testContext.verify(() -> {
      s.add(url, status, name).setHandler(future_add -> {
        assertTrue(future_add.failed());
        assertEquals("empty service name", future_add.cause().getMessage());

        testContext.completeNow();
      });
    });
  }

  @Test
  @DisplayName("Add a new service")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void add_service_new(Vertx vertx, VertxTestContext testContext) {
    final Services s = new Services(new DBConnector(vertx, "test"));
    final String url = "http://kry.se";
    final String status = "UNKNOWN";
    final String name = "KRY";

    testContext.verify(() -> {
      s.add(url, status, name).setHandler(future_add -> {
        assertTrue(future_add.succeeded());
        assertEquals(url, future_add.result().getString("url"));

        testContext.completeNow();
      });
    });
  }

  @Test
  @DisplayName("Add an already existing service")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void add_service_existing(Vertx vertx, VertxTestContext testContext) {
    final Services s = new Services(new DBConnector(vertx, "test"));
    final String url = "http://kry.se";
    final String status = "UNKNOWN";
    final String name = "KRY";

    testContext.verify(() -> {
      s.add(url, status, name).setHandler(future_pre_add -> {
        s.add(url, status, name).setHandler(future_add -> {
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
    final Services s = new Services(new DBConnector(vertx, "test"));
    final String url = "http://kry.se";
    final String status = "UNKNOWN";
    final String name = "KRY";

    testContext.verify(() -> {
      s.add(url, status, name).setHandler(future_pre_add -> {
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
    final Services s = new Services(new DBConnector(vertx, "test"));
    final String url = "http://kry.se";

    testContext.verify(() -> {
      s.get(url).setHandler(future_get -> {
        assertTrue(future_get.succeeded());
        assertEquals(null, future_get.result());

        testContext.completeNow();
      });
    });
  }

  @Test
  @DisplayName("Update a service with an empty url")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void update_service_empty_url(Vertx vertx, VertxTestContext testContext) {
    final Services s = new Services(new DBConnector(vertx, "test"));
    final String url = "";
    final String status = "UNKNOWN";

    testContext.verify(() -> {
      s.update(url, status).setHandler(future_remove -> {
        assertTrue(future_remove.failed());
        assertEquals("empty service url", future_remove.cause().getMessage());

        testContext.completeNow();
      });
    });
  }

  @Test
  @DisplayName("Update a not-existing service")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void update_service_not_existing(Vertx vertx, VertxTestContext testContext) {
    final Services s = new Services(new DBConnector(vertx, "test"));
    final String url = "http://kry.se";
    final String status = "UNKNOWN";

    testContext.verify(() -> {
      s.update(url, status).setHandler(future_remove -> {
        assertTrue(future_remove.failed());
        assertEquals("service does not exist", future_remove.cause().getMessage());

        testContext.completeNow();
      });
    });
  }

  @Test
  @DisplayName("Update an existing service")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void update_service_existing(Vertx vertx, VertxTestContext testContext) {
    final Services s = new Services(new DBConnector(vertx, "test"));
    final String url = "http://kry.se";
    final String status = "UNKNOWN";
    final String newStatus = "UP";
    final String name = "KRY";

    testContext.verify(() -> {
      s.add(url, status, name).setHandler(future_pre_add -> {
        s.update(url, newStatus).setHandler(future_remove -> {
          assertTrue(future_remove.succeeded());
          assertEquals(newStatus, future_remove.result().getString("status"));

          testContext.completeNow();
        });
      });
    });
  }

  @Test
  @DisplayName("Get all services when there is none")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void get_all_no_service(Vertx vertx, VertxTestContext testContext) {
    final Services s = new Services(new DBConnector(vertx, "test"));

    testContext.verify(() -> {
      s.getAll().setHandler(future_get -> {
        assertTrue(future_get.succeeded());
        assertEquals(0, future_get.result().size());

        testContext.completeNow();
      });
    });
  }

  @Test
  @DisplayName("Get all services when there is one or more")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void get_all_services_existing(Vertx vertx, VertxTestContext testContext) {
    final Services s = new Services(new DBConnector(vertx, "test"));
    final String url = "http://kry.se";
    final String status = "UNKNOWN";
    final String name = "KRY";

    testContext.verify(() -> {
      s.add(url, status, name).setHandler(future_pre_add -> {
        s.getAll().setHandler(future_get -> {
          assertTrue(future_get.succeeded());
          assertEquals(1, future_get.result().size());

          testContext.completeNow();
        });
      });
    });
  }

  @Test
  @DisplayName("Remove a service with an empty url")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void remove_service_empty_url(Vertx vertx, VertxTestContext testContext) {
    final Services s = new Services(new DBConnector(vertx, "test"));
    final String url = "";

    testContext.verify(() -> {
      s.remove(url).setHandler(future_remove -> {
        assertTrue(future_remove.failed());
        assertEquals("empty service url", future_remove.cause().getMessage());

        testContext.completeNow();
      });
    });
  }

  @Test
  @DisplayName("Remove a not-existing service")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void remove_service_not_existing(Vertx vertx, VertxTestContext testContext) {
    final Services s = new Services(new DBConnector(vertx, "test"));
    final String url = "http://kry.se";

    testContext.verify(() -> {
      s.remove(url).setHandler(future_remove -> {
        assertTrue(future_remove.failed());
        assertEquals("service does not exist", future_remove.cause().getMessage());

        testContext.completeNow();
      });
    });
  }

  @Test
  @DisplayName("Remove an existing service")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void remove_service_existing(Vertx vertx, VertxTestContext testContext) {
    final Services s = new Services(new DBConnector(vertx, "test"));
    final String url = "http://kry.se";
    final String status = "UNKNOWN";
    final String name = "KRY";

    testContext.verify(() -> {
      s.add(url, status, name).setHandler(future_pre_add -> {
        s.remove(url).setHandler(future_remove -> {
          assertTrue(future_remove.succeeded());
          assertEquals(url, future_remove.result().getString("url"));

          testContext.completeNow();
        });
      });
    });
  }
}
