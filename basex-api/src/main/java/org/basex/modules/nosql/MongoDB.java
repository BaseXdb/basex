package org.basex.modules.nosql;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.basex.query.QueryException;
import org.basex.query.func.FuncOptions;
import org.basex.query.value.Value;
import org.basex.query.value.item.Bln;
import org.basex.query.value.item.Int;
import org.basex.query.value.item.Item;
import org.basex.query.value.item.QNm;
import org.basex.query.value.item.Str;
import org.basex.query.value.map.Map;
import org.basex.query.value.type.SeqType;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBEncoder;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceCommand.OutputType;
import com.mongodb.MapReduceOutput;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;
import com.mongodb.util.JSONParseException;

/**
 * This is the primary class for MongoDb processing in Basex.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Prakash Thapa
 */
public class MongoDB extends Nosql {
    /** URL of MongDB module. */
    private static final String MONGO_URL = "http://basex.org/modules/mongodb";
    /** QName of MongoDB options. */
    private static final QNm Q_MONGODB = QNm.get("mongodb", "options",
            MONGO_URL);
    /** mongoclient instances. */
    private HashMap<String, MongoClient> mongoClients =
            new HashMap<>();
    /** DB instances instances. */
    private HashMap<String, DB> dbs = new HashMap<>();
    /** Mongo Options instances. */
    private HashMap<String, NosqlOptions> mongopts = new HashMap<>();
    /**
     * Connect MongoDB with it's url in the format of mongodb://root:root@localhost/test.
     * @param  MongoDB URL for connection
     * @return  Str Key of HashMap that contains all DB Object of Mongodb
     * connection instances.
     * @throws QueryException query exception
     */
//    public Str connection(final Str url) throws QueryException {
//        return connection(url, null);
//    }
    /**
     * Connect parameters in map like: {"host":"localhost","port":27017,
     * "database":"test", "username":"user", "password":"pass"}.
     * @param connectionMap Map
     * @return Str Key of HashMap that contains all DB Object of Mongodb
     * connection instances.
     * @throws QueryException query exception
     */
    public Str connect(final Map connectionMap) throws QueryException {
        final NosqlOptions opts = new NosqlOptions();
        if(connectionMap != null) {
            new FuncOptions(Q_MONGODB, null).parse(connectionMap, opts);
        }
        if(opts.get(NosqlOptions.URL) != null) {
            return connect(Str.get(opts.get(NosqlOptions.URL)), connectionMap);
        }
        String handler = "Client" + mongoClients.size();
        try {
          MongoClient mongoClient = new MongoClient(opts.get(
                  NosqlOptions.HOST), opts.get(NosqlOptions.PORT));
          mongoClients.put(handler, mongoClient);
          char[] pass = (opts.get(NosqlOptions.PASSWORD) != null) ?
                  opts.get(NosqlOptions.PASSWORD).toCharArray() : null;
          return mongoConnect(handler, opts.get(NosqlOptions.DATABASE),
                  opts.get(NosqlOptions.USERNAME), pass, connectionMap);
        } catch (final MongoException ex) {
          throw MongoDBErrors.mongoExceptionError(ex);
        } catch (UnknownHostException ex) {
          throw MongoDBErrors.generalExceptionError(ex);
        }
    }
    /**
     * Mongodb connection with options.
     * @param url mongodb url like: "mongodb://127.0.0.1:27017/enron"
     * @param options nosql options
     * @return Str
     * @throws QueryException query exception
     */
    public Str connect(final Str url, final Map options)
            throws QueryException {
        MongoClientURI uri = new MongoClientURI(url.toJava());
        String handler = "mongoClient" + mongoClients.size();
        try {
            MongoClient mongoClient = new MongoClient(uri);
            mongoClients.put(handler, mongoClient);
            return mongoConnect(handler, uri.getDatabase(), uri.getUsername(),
                    uri.getPassword(), options);
            } catch (final MongoException ex) {
                throw MongoDBErrors.mongoExceptionError(ex);
              } catch (UnknownHostException ex) {
                  throw MongoDBErrors.generalExceptionError(ex);
              }
        }
    /**
     * Mongodb connection with separate Hostname, port and database.
     * @param host hostname of server.
     * @param port Port Number
     * @param database Database name.
     * @return Str key of Hashmap that contains all DB Instances
     * @throws QueryException query exception
     */
    public Str connect(final Str host, final Int port, final Str database)
            throws QueryException {
        return connection(host, port, database, null, null, null);
    }
    /**
     * Mongodb connection with separate Hostname, port and database.
     * @param host hostname of server.
     * @param port Port Number
     * @param database Database name.
     * @param options other nosql options like {'type':'json'}
     * @return Str key of Hashmap that contains all DB Instances
     * @throws QueryException query exception
     */
    public Str connection(final Str host, final Int port, final Str database,
            final Map options) throws QueryException {
        return connection(host, port, database, options,  null, null);
      }
    /**
     * Mongodb connection with separate Hostname, port and database.
     * @param host hostname of server.
     * @param port Port Number
     * @param database name of databse to be connected
     * @param options other nosql options like {'type':'json'}
     * @param username username of Mongodb
     * @param password password of Mongodb
     * @return Str key of Hashmap that contains all DB Instances
     * @throws QueryException query exception
     */
    public Str connection(final Str host, final Int port, final Str database,
            final Map options, final Str username, final Str password)
            throws QueryException {
        String handler = "Client" + mongoClients.size();
        try {
          MongoClient mongoClient = new MongoClient(host.toJava(),
              (int) port.itr());
          mongoClients.put(handler, mongoClient);
          char[] pass = (password != null) ? password.toJava().toCharArray() : null;
          String user = (String) ((username != null) ? username.toJava() : username);
          return mongoConnect(handler, database.toJava(), user, pass, options);
        } catch (final MongoException ex) {
          throw MongoDBErrors.mongoExceptionError(ex);
        } catch (UnknownHostException ex) {
          throw MongoDBErrors.generalExceptionError(ex);
        }
    }
    /**
     * This method take key of Hashmap, create DB object and store to another
     * Hashmap.
     * @param mongoClientHandler Key of Hashmap that contains all Mongoclient instances
     * @param database name of database to be connect
     * @param username user's name for mongodb Connect
     * @param password password of user to connect Mongodb
     * @param options Other nosql options like {"type":"json"} and so on
     * @return Str key of Hashmap that contains all DB Instances
     * @throws QueryException query exception
     */
    private Str mongoConnect(final String mongoClientHandler, final String database,
            final String username, final char[] password, final Map options)
                    throws QueryException {
        final NosqlOptions opts = new NosqlOptions();
        if(options != null) {
            new FuncOptions(Q_MONGODB, null).parse(options, opts);
        }
        MongoClient mongoClient = mongoClients.get(mongoClientHandler);
        final String dbh = "DB" + dbs.size();
        try {
            DB db = mongoClient.getDB(database);
            if (username != null && password != null) {
                boolean auth = db.authenticate(username, password);
                if (!auth) {
                    throw  MongoDBErrors.unAuthorised();
                }
             }
            dbs.put(dbh, db);
            if(options != null) {
                mongopts.put(dbh, opts);
            }
            return Str.get(dbh);
            } catch (final MongoException ex) {
                throw MongoDBErrors.mongoExceptionError(ex);
             }
    }
    /**
     * This Method take Map as parameters and create array of <code>DBObject</code>
     * of MongoDB in first level like {"a":{"b":"c"},"x":{"y":"z"}} here there
     * are two array with key "a" and "x" other will be be the key value parameter
     * inBasicDBObject.
     *  * <blockquote><pre>
     *  DBObject obj = new BasicDBObject();
     *  obj.put( "foo", "bar" );
     *  </pre></blockquote>
     * @param map Basex Map
     * @return array of DBObject
     */
    private DBObject[] mapToDBObjectArray(final Map map) {
        if((map != null) && (!map.isEmpty())) {
            try {
                final Value keys = map.keys();
                DBObject[] dbObject = null;
                int length = (int) keys.size();
                if(length > 0) {
                    dbObject = new BasicDBObject[length];
                    int i = 0;
                    for(Item key : keys) {
                        final Value value = map.get(key, null);
                        DBObject singleObject = new BasicDBObject();
                        if(value instanceof Map) {
                            singleObject.put(((Str) key).toJava(),
                                    this.mapToDBObject((Map) value));
                        } else if(value.type().instanceOf(SeqType.ITR_OM)) {
                                long l = ((Item) value).itr(null);
                                singleObject.put(((Str) key).toJava(), l);
                        } else {
                            singleObject.put(((Str) key).toJava(), value.toJava());
                        }
                        dbObject[i] = singleObject;
                        i++;
                    }
                }
              return dbObject;
            } catch (Exception e) {
                MongoDBErrors.generalExceptionError(e);
            }
        }
        return null;
    }
    /**
     * This Method convert Basex Map to MongoDB's DBObject.
     * {"foo":"bar"}
     * <blockquote><pre>
     *  DBObject obj = new BasicDBObject();
     *  obj.put( "foo", "bar" );
     *  </pre></blockquote>
     * @param map Basex's Map
     * @return DBObject
     * @throws QueryException query exception
     */
    private DBObject mapToDBObject(final Map map) throws QueryException {
        if(map != null) {
            final DBObject dbObject = new BasicDBObject();
            final Value keys = map.keys();
            for(Item key : keys) {
                final Value value = map.get(key, null);
                if(value instanceof Map) {
                    dbObject.put(((Str) key).toJava(), this.mapToDBObject((Map) value));
                } else if(value.type().instanceOf(SeqType.ITR_OM)) {
                        long l = ((Item) value).itr(null);
                        dbObject.put(((Str) key).toJava(), l);
                } else {
                   dbObject.put(((Str) key).toJava(), value.toJava());
                }
            }
          return dbObject;
        }
        return null;
    }
    /**
     * This Method gives the DB Handler from Hashmap with given Key.
     * @param handler database handler key for Hashmap that contains all DB objects
     * @return DB Object
     * @throws QueryException query exception
     */
    protected DB getDbHandler(final Str handler) throws QueryException {
        final DB db = dbs.get(handler.toJava());
        if(db == null)
            throw MongoDBErrors.mongoDBError(handler.toJava());
        return db;
      }
    /**
     * Get Mongodb Options for particular database handler.
     * @param handler database handler Database handler
     * @return MongoOptions object
     */
    private NosqlOptions getMongoOption(final Str handler) {
        NosqlOptions opt = mongopts.get(handler.toJava());
        if(opt != null)
            return opt;
        return null;
    }
    /**
     * All the result from Mongodb will come to this Method in the form of Json String.
     * First it checks the assigned NOSQL {@link NosqlOptions} options and then
     * return the final result {@link #finalResult(Str, NosqlOptions)} process by parent
     * class[{@link Nosql}].
     * @param handler database handler Database handler
     * @param json Str object that contains Json string
     * @return Item
     * @throws Exception exception
     */
    private Item returnResult(final Str handler, final Str json)
            throws Exception {
        NosqlOptions opt =   getMongoOption(handler);
        if(json != null) {
                if(opt != null) {
                    return finalResult(json, opt);
                }
                return finalResult(json, null);
        }
        return  null;
    }
    /**
     * Convert collection result(DBCursor) into Item {@link Item} element.
     * @param handler database handler
     * @param cursor DBcursor
     * @return Item
     * @throws QueryException query exception
     */
    private Item cursorToItem(final Str handler, final DBCursor cursor)
            throws QueryException {
        if(cursor != null) {
            try {
                if(cursor.count() == 1) {
                    Iterator<DBObject> row = cursor.iterator();
                    return objectToItem(handler, row.next());
                }
                final Str json = Str.get(JSON.serialize(cursor));
                return returnResult(handler, json);
            } catch (final Exception ex) {
                throw MongoDBErrors.generalExceptionError(ex);
            }
        }
        return  null;
    }
    /**
     * Convert collection DBObject into Item {@link Item} element.
     * @param handler databse handler
     * @param object DBObject  (one row result)
     * @return Item
     * @throws QueryException query exception
     */
    private Item objectToItem(final Str handler, final DBObject object)
            throws QueryException {
        if(object != null) {
            try {
                final Str json = Str.get(JSON.serialize(object));
                return returnResult(handler, json);

            } catch (final Exception ex) {
                throw MongoDBErrors.generalExceptionError(ex);
            }
        }
        return  null;
    }
    /**
     * Take string as Str and return DBObject of mongodb.
     * @param item item(Map or Str)
     * @return DBObject
     * @throws QueryException query exception
     */
    protected DBObject getDbObjectFromStr(final Item item) throws QueryException {
        try {
            if(item instanceof Map) {
                return mapToDBObject((Map) item);
            } else if(item instanceof Str) {
                final String string = itemToString(item);
                return  (DBObject) JSON.parse(string);
            } else {
                throw MongoDBErrors.
                generalExceptionError("Number Expected for key '");
            }
    } catch (JSONParseException e) {
      throw MongoDBErrors.jsonFormatError(item.toJava());
        }
    }
    /**
     * Return all the collections in current database.
     * @param handler database handler DB handler.
     * @return Item
     * @throws QueryException query exception
     */
    public Item collections(final Str handler) throws QueryException {
        final DB db = getDbHandler(handler);
      Set<String> col = db.getCollectionNames();
      BasicDBObject collection = new BasicDBObject("collections", col);
      try {
        return objectToItem(handler, collection);
      } catch (JSONParseException e) {
          throw MongoDBErrors.jsonFormatError(col);
         }
    }
    /**
     * MongoDB find() without any parameters. eg. db.collections.find()
     * @param handler database handler Database handler
     * @param col collection name
     * @return result in xml element
     * @throws QueryException query exception
     */
    public Item find(final Str handler, final Item col) throws QueryException {
        return find(handler, col, null, null, null);
    }
    /**
     * MongoDB find() with query. eg. db.collections.find({'_id':2})
     * @param handler database handler Database handler
     * @param col collection collection
     * @param query conditions
     * @return Item
     * @throws QueryException query exception
     */
    public Item find(final Str handler, final Item col, final Item query)
            throws QueryException {
      return find(handler, col, query, null, null);
    }
    /**
     * MongoDB find() Query and Projection.
     * @param handler database handler Database handler
     * @param col collection collection
     * @param query conditions
     * @param opt options in Map like: {"limit":2}
     * @return Item
     * @throws QueryException query exception
     */
    public Item find(final Str handler, final Str col, final Str query,
             final Item opt) throws QueryException {
         return find(handler, col, query, opt, null);
       }
    /**
     * MongoDB find with all parameters.
     * @param handler database handler Database handler
     * @param col collection collection
     * @param query Query parameters
     * @param opt options in Map like: {"limit":2}
     * @param projection projection (selection field)
     * @return Item
     * @throws QueryException query exception
     */
    public Item find(final Str handler, final Item col, final Item query,
            final Item opt, final Item projection) throws QueryException {
          final DB db = getDbHandler(handler);
          db.requestStart();
              try {
                  DBObject p = null;
                  if(opt != null && opt instanceof Str) {
                      p = getDbObjectFromStr(opt);
                  } else if (projection != null && projection instanceof Str) {
                      p = getDbObjectFromStr(projection);
                  }
                final DBObject q = query != null ?
                        getDbObjectFromStr(query) : null;
                final DBCollection coll = db.getCollection(itemToString(col));
                final DBCursor cursor = coll.find(q, p);
                Map options = null;
                options = (opt != null && opt instanceof Map) ? (Map) opt :
                    (projection != null && projection instanceof Map) ?
                            (Map) projection : null;
                if(options != null) {
                     Value keys = options.keys();
                     for(final Item key : keys) {
                       if(!(key instanceof Str))
                           throw MongoDBErrors.
                           generalExceptionError("String expected " + key.toJava());
                       final String k = ((Str) key).toJava();
                       final Value v = options.get(key, null);
                      if(v instanceof Str || v.type().instanceOf(SeqType.ITR)) {
                          if(k.equals(LIMIT)) {
                              if(v.type().instanceOf(SeqType.ITR_OM)) {
                                  long l = ((Item) v).itr(null);
                                  cursor.limit((int) l);
                              } else {
                                  throw MongoDBErrors.
                                  generalExceptionError("Number Expected for key '"
                                  + key.toJava() + "'");
                              }
                          } else if(k.equals(SKIP)) {
                              //cursor.skip(Token.toInt(v));
                          } else if(k.equals(SORT)) {
                              BasicDBObject sort = new BasicDBObject(k, v);
                              sort.append("name", "-1");
                              cursor.sort(sort);
                          } else if(k.equals(COUNT)) {
                              int count = cursor.count();
                              BasicDBObject res = new BasicDBObject();
                              res.append("count", count);
                              return objectToItem(handler, res);
                          } else if(k.equals(EXPLAIN)) {
                            DBObject result = cursor.explain();
                             return objectToItem(handler, result);
                          }
                      } else if(v instanceof Map) {
                      } else {
                          throw MongoDBErrors.
                          generalExceptionError("Invalid value 2...");
                      }
                     }
                }
                return cursorToItem(handler, cursor);
              } catch (MongoException e) {
                  throw MongoDBErrors.generalExceptionError(e.getMessage());
              } finally {
                     db.requestDone();
              }
        }
    /**
     * Mongodb's findOne() function.
     * @param handler database handler
     * @param col collection
     * @return Item
     * @throws QueryException query exception
     */
    public Item findOne(final Str handler, final Item col)throws QueryException {
        return findOne(handler, col, null, null);
    }
    /**
     * Mongodb's findOne({'_id':2}) function with query.
     * @param handler database handler
     * @param col collection
     * @param query selection query
     * @return Item
     * @throws QueryException query exception
     */
    public Item findOne(final Str handler, final Item col, final Item query)
            throws QueryException {
       return findOne(handler, col, query, null);
    }
    /**
     * findOne with query and projection projection.
     * @param handler database handler
     * @param col collection
     * @param query selection query
     * @param projection  projection
     * @return Item
     * @throws QueryException query exception
     */
    public Item findOne(final Str handler, final Item col, final Item query,
            final Item projection) throws QueryException {

        final DB db = getDbHandler(handler);
        db.requestStart();
        try {
            final DBObject p = projection != null ?
                    getDbObjectFromStr(projection) : null;
            final DBObject q = query != null ?
                    getDbObjectFromStr(query) : null;
            final DBCollection coll = db.getCollection(itemToString(col));
            final DBObject cursor = coll.findOne(q, p);
            return  objectToItem(handler, cursor);
        } catch (MongoException e) {
            throw MongoDBErrors.generalExceptionError(e.getMessage());
        } finally {
               db.requestDone();
        }
    }
    /**
     * Insert data in MongoDB.
     * @param handler database handler DB Handler
     * @param col collection name
     * @param insertString string to insert in json formart or in Basex's Map
     * @return Item result from Mongodb.
     * @throws Exception exception
     */
    public Item insert(final Str handler, final Str col, final Str insertString)
            throws Exception {
        final DB db = getDbHandler(handler);
        db.requestStart();
        try {
            DBObject obj = getDbObjectFromStr(insertString);
            WriteResult wr = db.getCollection(col.toJava()).insert(obj);
           return returnResult(handler, Str.get(wr.toString()));
        } catch (MongoException e) {
           throw MongoDBErrors.generalExceptionError(db.getLastError().getString("err"));
        } finally {
           db.requestDone();
        }
    }
    /**
     * Mongodb update with two parameters like update({},{}).
     * @param handler database handler
     * @param col collection collection's name
     * @param query selection query
     * @param updatestring Json Str or Basex's Map
     * @return Item
     * @throws Exception exception
     */
    public Item update(final Str handler, final Item col, final Item query,
            final Str updatestring) throws Exception {
        return update(handler, col, query, updatestring, null, null);
    }
    /**
     * Mongodb update with 4 parameters like update({},{}, upsert, multi).
     * @param handler database handler Db Handler string
     * @param col collection name
     * @param query selection query
     * @param updatestring String to be updated
     * @param upsert true/false for mongodb upsert(Json Str or Map)
     * @param multi true/false for mongodb multi
     * @return Item
     * @throws Exception exception
     */
    public Item update(final Str handler, final Item col, final Item query,
            final Str updatestring, final Bln upsert, final Bln multi)
                    throws Exception {
        final DB db = getDbHandler(handler);
        db.requestStart();
        try {
            DBObject q =  getDbObjectFromStr(query);
            DBObject updateValue = getDbObjectFromStr(updatestring);
            WriteResult wr;
            if(upsert != null && multi != null) {
                wr = db.getCollection(itemToString(col)).
                        update(q, updateValue, upsert.toJava(), multi.toJava());
            } else {
                wr = db.getCollection(itemToString(col)).
                        update(q, updateValue);
            }
            return returnResult(handler, Str.get(wr.toString()));
        } catch (MongoException e) {
            throw MongoDBErrors.generalExceptionError(db.getLastError().getString("err"));
        } finally {
           db.requestDone();
        }
    }
    /**
     * Mongodb Save function.
     * @param handler database handler DB handler
     * @param col collection name
     * @param saveStr string to save(Map or Josn)
     * @return Item
     * @throws Exception exception
     */
    public Item save(final Str handler, final Str col, final Item saveStr)
            throws Exception {
        final DB db = getDbHandler(handler);
        db.requestStart();
        try {
           WriteResult wr = db.getCollection(col.toJava()).
                   save(getDbObjectFromStr(saveStr));
           return returnResult(handler, Str.get(wr.toString()));
        } catch (MongoException e) {
            throw MongoDBErrors.generalExceptionError(db.getLastError().getString("err"));
        } finally {
           db.requestDone();
        }
    }
    /**
     * Mongodb remove(). This will delete the document with specified query.
     * @param handler database handler
     * @param col collection name;
     * @param query Query to select document.(Map or Json Str)
     * @throws QueryException query exception
     */
    public void remove(final Str handler, final Item col, final Item query)
            throws QueryException {
        final DB db = getDbHandler(handler);
        db.requestStart();
        try {
            db.getCollection(itemToString(col)).remove(getDbObjectFromStr(query));
            DBObject err = db.getLastError();
            if(err != null) {
                throw MongoDBErrors.generalExceptionError(err.get("err").toString());
            }
        } catch (MongoException e) {
            throw MongoDBErrors.generalExceptionError(e);
        } finally {
           db.requestDone();
        }
    }
    /**
     * Mongodb Aggregate function with single aggregation parameter.
     * @param handler database handler database handler
     * @param col collection name
     * @param first Only one parameter of Aggregate function.
     * @return Item
     * @throws Exception exception
     */
    public Item aggregate(final Str handler, final Item col, final Item first)
            throws Exception {
        return aggregate(handler, col, first, null);
    }
    /**
     * This method is for aggregating with all pipeline options. All the options
     * should be given in sequence like: ('$group:{..}',...).
     * @param handler database handler database handler
     * @param col collection name
     * @param first aggregation compulsary
     * @param additionalOps other pipeline options in sequence.
     * @return Item
     * @throws Exception exception
     */
    public Item aggregate(final Str handler, final Item col, final Item first,
            final Value  additionalOps) throws Exception {
        final DB db = getDbHandler(handler);
        AggregationOutput agg;
        DBObject[] pipeline = null;
        if(additionalOps != null && (!additionalOps.isEmpty())) {
            if(additionalOps instanceof Map) {
                pipeline = mapToDBObjectArray((Map) additionalOps);
            } else {
                int length = (int) additionalOps.size();
                if(length > 0) {
                    pipeline = new BasicDBObject[length];
                    int i = 0;
                    for (Item x: additionalOps) {
                        pipeline[i++] = getDbObjectFromStr(x);
                    }
                }
            }
        }
        db.requestStart();
        try {
            if(additionalOps != null && (!additionalOps.isEmpty())) {
                agg =  db.getCollection(itemToString(col)).
                        aggregate(getDbObjectFromStr(first), pipeline);
            } else {
                agg =  db.getCollection(itemToString(col)).
                        aggregate(getDbObjectFromStr(first));
            }
           final Iterable<DBObject> d = agg.results();
          return returnResult(handler, Str.get(JSON.serialize(d)));
        } catch (MongoException e) {
            throw MongoDBErrors.generalExceptionError(e.getMessage());
        } finally {
           db.requestDone();
        }

    }
    /**
     * count numbers of documents in a collection.
     * @param handler database handler
     * @param col collection
     * @return number
     * @throws QueryException query exception
     */
    public long count(final Str handler, final Item col) throws QueryException {
        final DB db = getDbHandler(handler);
        long count = db.getCollection(itemToString(col)).count();
        return count;
    }
    /**
     * Copy data from One collection to another collection within same Database.
     * @param handler database handler
     * @param source Source collection name
     * @param dest Destination Collection name.
     * @throws QueryException query exception
     */
    public void copy(final Str handler, final Item source, final Item dest)
            throws QueryException {
        final DB db = getDbHandler(handler);
        db.requestStart();
        try {
            List<DBObject> cursor = db.getCollection(itemToString(source)).
                    find().toArray();
            db.getCollection(itemToString(dest)).insert(cursor);
       } catch (MongoException e) {
           throw MongoDBErrors.generalExceptionError(e);
        } finally {
           db.requestDone();
        }
    }
    /**
     * Copy collection from one Database insert to another database.
     * @param handler database handler Mongodb Connection for source database
     * @param source source collection
     * @param handlerDest Mongodb Connection for Destination database
     * @param dest Destination Collection
     * @throws QueryException query exception
     */
    public void copy(final Str handler, final Item source, final Str handlerDest,
            final Item dest) throws QueryException {
        final DB db = getDbHandler(handler);
        final DB dbDestionation = getDbHandler(handlerDest);
        db.requestStart();
        try {
            List<DBObject> cursor = db.getCollection(itemToString(source)).
                    find().toArray();
            dbDestionation.getCollection(itemToString(dest)).drop();
            dbDestionation.getCollection(itemToString(dest)).insert(cursor);
       } catch (MongoException e) {
           throw MongoDBErrors.generalExceptionError(e);
        } finally {
           db.requestDone();
        }
    }
    /**
     * Drop collection from a database.
     * @param handler database handler
     * @param col collection name
     * @throws QueryException query exception
     */
    public void drop(final Str handler, final Item col)throws QueryException {
        final DB db = getDbHandler(handler);
        db.requestStart();
        try {
            db.getCollection(itemToString(col)).drop();
       } catch (MongoException e) {
           throw MongoDBErrors.generalExceptionError(e);
        } finally {
           db.requestDone();
        }
    }
    /**
     * MongoDB runCommand() function.
     * @param handler database handler
     * @param command Command to Execute
     * @return Item
     * @throws Exception exception
     */
    public Item runCommand(final Str handler, final Item command)throws Exception {
       return runCommand(handler, command, null);
    }
    /**
     * MongoDB runCommand() function with integer as parameter.
     * @param handler database handler
     * @param command command to execute in Map or  JSON or simply String
     * @param options integer options.
     * @return Item
     * @throws Exception exception
     */
    public Item runCommand(final Str handler, final Item command, final Int options)
            throws Exception {
        final DB db = getDbHandler(handler);
        db.requestStart();
        try {
            CommandResult result = null;
            if(command instanceof Map) {
                DBObject  cmd = mapToDBObject((Map) command);
                if(options != null) {
                    result = db.command(cmd, (DBEncoder) options.toJava());
                } else {
                    result = db.command(cmd);
                }
            } else {
                result = db.command(((Str) command).toJava());
            }
           return returnResult(handler, Str.get(result.toString()));
       } catch (MongoException e) {
           throw MongoDBErrors.generalExceptionError(e);
        } finally {
           db.requestDone();
        }
    }
    /**
     * Create Index in specified field.
     * @param handler database handler
     * @param col collection name of collection
     * @param indexStr string to create index json or Map
     * @throws QueryException query exception
     */
    public void ensureIndex(final Str handler, final Str col,
            final Item indexStr)throws QueryException {
        final DB db = getDbHandler(handler);
        db.requestStart();
        try {
             db.getCollection(itemToString(col)).ensureIndex(
                    getDbObjectFromStr(indexStr));
           //return returnResult(handler, Str.get(result.toString()));
       } catch (MongoException e) {
           throw MongoDBErrors.generalExceptionError(e);
        } finally {
           db.requestDone();
        }
    }
    /**
     * drop Index in specified field.
     * @param handler database handler
     * @param col collection name of collection
     * @param indexStr string to create index Json or Map
     * @throws QueryException query exception
     */
    public void dropIndex(final Str handler, final Str col,
            final Item indexStr)throws QueryException {
        final DB db = getDbHandler(handler);
        db.requestStart();
        try {
             db.getCollection(itemToString(col)).createIndex(
                    getDbObjectFromStr(indexStr));
           //return returnResult(handler, Str.get(result.toString()));
       } catch (MongoException e) {
           throw MongoDBErrors.generalExceptionError(e);
        } finally {
           db.requestDone();
        }
    }
    /**
     * take DB handler as parameter and get MongoClient and then close it.
     * @param handler database handler DB handler
     * @throws QueryException query exception
     */
    public void close(final Str handler) throws QueryException {
        String ch = handler.toJava();
        try {
            final MongoClient client = (MongoClient) getDbHandler(handler).getMongo();
            if(client == null)
                throw MongoDBErrors.mongoDBError(ch);
            client.close();
        } catch (MongoException e) {
            throw MongoDBErrors.mongoDBError(e);
        }
    }
    /**
     * This method is implemented for MongoDB Mapreduce.
     * @param handler database handler
     * @param col collection name
     * @param map Map method
     * @param reduce Reduce Method
     * @return Item
     * @throws Exception exception
     */
    public Item mapreduce(final Str handler, final Str col, final Str map,
            final Str reduce) throws Exception {
        return mapreduce(handler, col, map, reduce, null, null, null);
    }
    /**
     * Mongodb Mapreduce function with 3 parameters.
     * @param handler database handler
     * @param col collection name
     * @param map Map method
     * @param reduce Reduce Method
     * @param finalalize mongodb finalize parameter
     * @return Items
     * @throws Exception exception
     */
    public Item mapreduce(final Str handler, final Str col, final Str map,
            final Str reduce, final Item finalalize) throws Exception {
        return mapreduce(handler, col, map, reduce, finalalize, null, null);
    }
    /**
     * Mongodb Mapreduce function with 4 parameters.
     * @param handler database handler
     * @param col collection name
     * @param map Map method
     * @param reduce Reduce Method
     * @param finalalize mongodb finalize parameter
     * @param query Selection options.
     * @return Items
     * @throws Exception exception
     */
    public Item mapreduce(final Str handler, final Str col, final Str map,
            final Str reduce, final Item finalalize, final Item query) throws Exception {
        return mapreduce(handler, col, map, reduce, finalalize, query, null);
    }
    /**
     * Mongodb Mapreduce function with 5 parameters, Map, reduce and query Option.
     * @param handler database handler
     * @param col collection name
     * @param map Map method
     * @param reduce Reduce Method
     * @param finalalize mongodb finalize parameter
     * @param query Selection options.
     * @param options additional options
     * @return Item
     * @throws Exception exception
     */
    public Item mapreduce(final Str handler, final Str col, final Str map,
            final Str reduce, final Item finalalize, final Item query,
            final Map options) throws Exception {
        final DB db = getDbHandler(handler);
        if(map == null) {
            throw MongoDBErrors.
            generalExceptionError("Map function cannot be empty in Mapreduce");
        }
        final DBObject q = query != null ?
                getDbObjectFromStr(query) : null;
        final DBCollection collection = db.getCollection(itemToString(col));
        String out = null;
        String outType = null;
        OutputType op = MapReduceCommand.OutputType.INLINE;
        if(options != null) {
            for(Item k : options.keys()) {
                String key = (String) k.toJava();
                if(key.equals("outputs")) {
                    out = (String) options.get(k, null).toJava();
                }
                if(key.equals("outputype")) {
                    outType = (String) options.get(k, null).toJava();
                }
            }
            if(out != null) {
                if(outType.toUpperCase().equals("REPLACE")) {
                    op = MapReduceCommand.OutputType.REPLACE;
                } else if(outType.toUpperCase().equals("MERGE")) {
                    op = MapReduceCommand.OutputType.MERGE;
                } else if(outType.toUpperCase().equals("REDUCE")) {
                    op = MapReduceCommand.OutputType.REDUCE;
                }
            }
        }
        db.requestStart();
        try {
            MapReduceCommand cmd = new MapReduceCommand(collection,
                    map.toJava(), reduce.toJava(), out, op, q);
             if(finalalize != null) {
                 cmd.setFinalize((String) finalalize.toJava());
             }
            final MapReduceOutput outcmd = collection.mapReduce(cmd);
            return returnResult(handler, Str.get(JSON.serialize(outcmd.results())));
        } catch (MongoException e) {
            throw MongoDBErrors.generalExceptionError(e);
        } finally {
            db.requestDone();
        }
    }
    /**
     * Mapreduce all functions in xquery's Map like :{"map":"function(){..}"
     * , "reduce":"function(){}"}.
     * @param handler database handler
     * @param col collection name
     * @param options all options of Mapreduce including "map" in key.
     * @return Item
     * @throws Exception exception
     */
    public Item mapreduce(final Str handler, final Str col, final Map options)
            throws Exception {
        if(options == null) {
            throw MongoDBErrors.generalExceptionError("Map optoins are empty");
        }
        final DB db = getDbHandler(handler);
        final DBCollection collection = db.getCollection(itemToString(col));
        String out = null;
        String outType = null;
        String map = null;
        String reduce = null;
        DBObject query = null;
        DBObject sort = null;
        int limit = 0;
        String finalalize = null;
        OutputType op = MapReduceCommand.OutputType.INLINE;
        for(Item k : options.keys()) {
            String key = (String) k.toJava();
            Value val = options.get(k, null);
            String value = (String) val.toJava();
            if(key.toLowerCase().equals("map")) {
               map = value;
            } else if(key.toLowerCase().equals("reduce")) {
                reduce = value;
            } else  if(key.toLowerCase().equals("outputs")) {
                out = value;
            } else if(key.toLowerCase().equals("outputype")) {
                outType = value;
            } else if(key.toLowerCase().equals("limit")) {
                if(val.type().instanceOf(SeqType.ITR_OM)) {
                    long l = ((Item) val).itr(null);
                    limit = (int) l;
                } else {
                    throw MongoDBErrors.
                    generalExceptionError(" Expected integer Value");
                }
            } else if(key.toLowerCase().equals(SORT)) {
                sort = getDbObjectFromStr(Str.get(value));
            } else if(key.toLowerCase().equals(QUERY)) {
                query = getDbObjectFromStr(Str.get(value));
            } else if(key.toLowerCase().equals(FINALIZE)) {
                finalalize = value;
            }
        }
        if(out != null && outType != null) {
            if(outType.toUpperCase().equals("REPLACE")) {
                op = MapReduceCommand.OutputType.REPLACE;
            } else if(outType.toUpperCase().equals("MERGE")) {
                op = MapReduceCommand.OutputType.MERGE;
            } else if(outType.toUpperCase().equals("REDUCE")) {
                op = MapReduceCommand.OutputType.REDUCE;
            }
        } else if(out != null) {
            op = MapReduceCommand.OutputType.REPLACE;
        }
        if(map == null) {
            throw MongoDBErrors.generalExceptionError("Map function cannot be empty");
        }
        db.requestStart();
        try {
            MapReduceCommand cmd = new MapReduceCommand(collection,
                    map, reduce, out, op, query);
             if(finalalize != null) {
                 cmd.setFinalize(finalalize);
             }
             if(limit != 0) {
                 cmd.setLimit(limit);
             }
             if(sort != null) {
                 cmd.setSort(sort);
             }
            final MapReduceOutput outcmd = collection.mapReduce(cmd);
            return returnResult(handler, Str.get(JSON.serialize(outcmd.results())));
        } catch (MongoException e) {
            throw MongoDBErrors.generalExceptionError(e);
        } finally {
            db.requestDone();
        }
    }
}