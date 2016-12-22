package com.brestopencampus.spark;

import com.brestopencampus.spark.model.Beer;
import spark.Request;
import spark.Response;
import spark.Route;

import com.mongodb.*;
import org.bson.types.ObjectId;


import static spark.Spark.*;

/**
 * Created by sca on 29/10/16.
 */
public class Application {

  public static void main(String[] args) throws Exception {

    exception(Exception.class, (e, req, res) -> e.printStackTrace()); // print all exceptions
    port(8080);

    BeerDao dao = new BeerDao(mongo());

    get("/", (req, res) -> "Hello Beers");

    /**
     * REST Part
     */
    JSonTransformer jsonT = new JSonTransformer();
    get("/json/Beers", (req, res) -> dao.all(), jsonT);
    get("/json/Beers/:id", "application/json",  (req, res) ->  dao.find(req.params("id"))
        , jsonT);

    post("/json/Beers", (req, res) -> {
          Beer b = dao.add(new Beer(req.queryParams("name"), Float.parseFloat(req.queryParams("alcohol"))));
          res.status(201);
          return b;
    }, jsonT);
    delete("/json/Beers/:id", (ICRoute) (req) -> dao.remove(req.params("id")));
  }

  private static DB mongo() throws Exception {
    String host = System.getenv("MONGODB_ADDON_HOST");
    int port = Integer.parseInt(Sytem.getenv("MONGODB_ADDON_PORT")); //27017;Integer.parseInt(System.getenv("MONGODB_DB_PORT"));
    String dbname =  System.getenv("MONGODB_ADDON_DB");//"HelloSpark";//System.getenv("HelloSpark");
    String username = System.getenv("MONGODB_ADDON_USER");
    String password = System.getenv("MONGODB_ADDON_PASSWORD");
    MongoClientOptions mongoClientOptions = MongoClientOptions.builder().build();
    MongoClient mongoClient = new MongoClient(new ServerAddress(host, port), mongoClientOptions);
    mongoClient.setWriteConcern(WriteConcern.SAFE);
    DB db = mongoClient.getDB(dbname);
    //return db;
    if (db.authenticate(username, password.toCharArray())) {
      return db;
    } else {
      throw new RuntimeException("Not able to authenticate with MongoDB");
    }
  }



  @FunctionalInterface
  private interface ICRoute extends Route {
    default Object handle(Request request, Response response) throws Exception {
      handle(request);
      return "";
    }
    void handle(Request request) throws Exception;
  }

}
