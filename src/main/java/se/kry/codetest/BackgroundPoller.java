package se.kry.codetest;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceReference;
import io.vertx.servicediscovery.types.HttpEndpoint;

import java.util.List;
import java.util.stream.Collectors;

public class BackgroundPoller {
  private ServiceDiscovery discovery;

  public BackgroundPoller(Vertx vertx) {
    discovery = ServiceDiscovery.create(vertx);
  }

  public Future<List<Future<JsonObject>>> pollServices() {
    Future<List<Future<JsonObject>>> future = Future.future();

    discovery.getRecords(r -> true, future_get -> {
      if (future_get.succeeded()) {
        List<Record> records = future_get.result();
        List<Future<JsonObject>> statuses = records
          .stream()
          .map(record -> pollService(record))
          .collect(Collectors.toList());

        future.complete(statuses);
      }
      else {
        future_get.cause().printStackTrace();
        future.fail(future_get.cause());
      }
    });

    return future;
  }

  public Future<Void> addService(JsonObject service) {
    Future<Void> future = Future.future();
    Record record = HttpEndpoint.createRecord(
      service.getString("url"),
      service.getString("url"),
      80,
      "/"
    );

    discovery.publish(record, future_publish -> {
      if (future_publish.succeeded()) {
        future.complete();
      }
      else {
        future_publish.cause().printStackTrace();
        future.fail(future_publish.cause());
      }
    });

    return future;
  }

  public Future<Void> removeService(JsonObject service) {
    Future<Void> future = Future.future();

    discovery.getRecord(r -> r.getName().equals(service.getString("name")), future_get -> {
      if (future_get.succeeded()) {
        Record record = future_get.result();

        if (record != null) {
          discovery.unpublish(record.getRegistration(), future_unpublish -> {
            if (future_unpublish.succeeded()) {
              future.complete();
            }
            else {
              future_unpublish.cause().printStackTrace();
              future.fail(future.cause());
            }
          });
        }
        else {
          future.fail("service does not exist");
        }
      }
      else {
        future_get.cause().printStackTrace();
        future.fail(future_get.cause());
      }
    });

    return future;
  }

  private Future<JsonObject> pollService(Record record) {
    Future<JsonObject> future = Future.future();
    ServiceReference reference = discovery.getReference(record);
    WebClient client = reference.getAs(WebClient.class);

    client.get("/").send(response -> {

      if (response.succeeded() && response.result().statusCode() == 200) {
        future.complete(
          new JsonObject()
            .put("url", record.getName())
            .put("status", "UP")
        );
      }
      else {
        future.complete(
          new JsonObject()
            .put("url", record.getName())
            .put("status", "DOWN")
        );
      }
    });

    return future;
  }
}
