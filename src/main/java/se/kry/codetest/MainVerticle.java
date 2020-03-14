package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import se.kry.codetest.migrate.DBMigration;

import java.util.List;
import java.util.stream.Collectors;

public class MainVerticle extends AbstractVerticle {
  private DBConnector connector;
  private Services services;
  private BackgroundPoller poller;

  @Override
  public void start(Future<Void> startFuture) {
    connector = new DBConnector(vertx);
    services = new Services(connector);

    runMigrations()
      .compose(v -> initBackgroundPoller())
      .setHandler(future_migration -> {
        if (future_migration.succeeded()) {
          createHttpServer(startFuture);
        }
        else {
          future_migration.cause().printStackTrace();
        }
      });
  }

  private void setRoutes(Router router){
    router.route("/*").handler(StaticHandler.create());

    setGetServicesRoute(router);
    setCreateServiceRoute(router);
    setRemoveServiceRoute(router);
  }

  private void setCreateServiceRoute(Router router) {
    router.post("/service").handler(req -> {
      JsonObject jsonBody = req.getBodyAsJson();
      services
        .add(jsonBody.getString("url"), "UNKNOWN", jsonBody.getString("name"))
        .setHandler(future_add -> {
          if (future_add.succeeded()) {
            poller
              .addService(future_add.result())
              .setHandler(v -> System.out.println("service added to poller"));

            req.response()
              .putHeader("content-type", "text/plain")
              .end("OK");
          } else {
            req.response()
              .putHeader("content-type", "text/plain")
              .setStatusCode(400)
              .end(future_add.cause().getMessage());
          }
        });
    });
  }

  private void setGetServicesRoute(Router router) {
    router.get("/service").handler(req -> {
      services.getAll().setHandler(future_getall -> {
        if (future_getall.succeeded()) {
          List<JsonObject> jsonServices = future_getall.result();

          req.response()
            .putHeader("content-type", "application/json")
            .end(new JsonArray(jsonServices).encode());
        }
        else {
          req.response()
          .putHeader("content-type", "application/json")
          .end(new JsonArray().encode());
        }
      });
    });
  }

  private void setRemoveServiceRoute(Router router) {
    router.delete("/service").handler(req -> {
      JsonObject jsonBody = req.getBodyAsJson();

      services
        .remove(jsonBody.getString("url"))
        .setHandler(future_remove -> {
          if (future_remove.succeeded()) {
            poller
              .removeService(future_remove.result())
              .setHandler(v -> System.out.println("service removed from poller"));

            req.response()
              .putHeader("content-type", "text/plain")
              .end("OK");
          } else {
            req.response()
              .putHeader("content-type", "text/plain")
              .setStatusCode(400)
              .end(future_remove.cause().getMessage());
          }
        });
    });
  }

  private Future<Void> runMigrations() {
    return DBMigration.run();
  }

  private Future<Void> initBackgroundPoller() {
    Future<Void> future = Future.future();
    poller = new BackgroundPoller(vertx);

    services.getAll().setHandler(future_get_all -> {
      if (future_get_all.succeeded()) {
        future_get_all.result().forEach(service -> {
          poller.addService(service);
        });

        future.complete();
      }
    });

    return future;
  }

  private Future<Void> createHttpServer(Future<Void> startFuture) {
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    setRoutes(router);

    vertx.setPeriodic(1000 * 10, timerId -> pollServices());

    vertx
        .createHttpServer()
        .requestHandler(router)
        .listen(8080, result -> {
          if (result.succeeded()) {
            System.out.println("KRY code test service started");
            startFuture.complete();
          } else {
            startFuture.fail(result.cause());
          }
        });

    return startFuture;
  }

  private void pollServices() {
    poller.pollServices().setHandler(future_poll -> {
      if (future_poll.succeeded()) {
        List<Future<JsonObject>> results = future_poll.result();

        results.forEach(future_object -> {
          future_object.setHandler(future_status -> {
            if (future_status.succeeded()) {
              JsonObject status = future_status.result();
              System.out.println(status);
            }
            else {
              future_status.cause().printStackTrace();
            }
          });
        });
      }
      else {
        future_poll.cause().printStackTrace();
      }
    });
  }
}



