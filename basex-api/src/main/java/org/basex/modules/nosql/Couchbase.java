package org.basex.modules.nosql;

import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import net.spy.memcached.internal.OperationFuture;

import org.basex.query.QueryException;
import org.basex.query.func.FuncOptions;
import org.basex.query.value.Value;
import org.basex.query.value.item.*;
import org.basex.query.value.map.Map;
import org.basex.query.value.type.SeqType;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.CouchbaseConnectionFactory;
import com.couchbase.client.protocol.views.ComplexKey;
import com.couchbase.client.protocol.views.DesignDocument;
import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.Stale;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewDesign;
import com.couchbase.client.protocol.views.ViewResponse;
import com.couchbase.client.protocol.views.ViewRow;
import com.couchbase.client.vbucket.ConfigurationException;


/**
 * CouchBase extension of Basex.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Prakash Thapa
 */
public class Couchbase extends Nosql {
  /** QName of Couchbase URL. */
  protected static final String COUCHBASE_URL = "http://basex.org/modules/nosql/couchbase";
    /** QName of Couchbase options. */
    protected static final QNm Q_COUCHBASE = QNm.get("couchbase", "options",
            COUCHBASE_URL);
    /** Couchbase instances. */
    protected HashMap<String, CouchbaseClient> couchbaseclients = new HashMap<>();
    /** Couchbase options. */
    protected HashMap<String, NosqlOptions> couchopts = new HashMap<>();
    /** nodes array. */
    //protected ArrayList<URI> nodes = new ArrayList<URI>();
    /** URL of this module. */
    protected boolean valueOnly;
    /** connection parameters in map like {'url':'localhost','bucket':'basex'}.
     * @param options mapoptions.
     * @return connetion string.
     * @throws QueryException  query exception.
     */
    public Str connect(final Map options) throws QueryException {
    final NosqlOptions opts = new NosqlOptions();
    if(options != null) {
        new FuncOptions(Q_COUCHBASE, null).parse(options, opts);
    }
    try {
      String url = opts.get(NosqlOptions.URL);
      String password = (opts.get(NosqlOptions.PASSWORD) != null) ?
                             opts.get(NosqlOptions.PASSWORD):"";
      String bucket = opts.get(NosqlOptions.BUCKET);
      return connect(Str.get(url), Str.get(bucket), Str.get(password), options);
    } catch(URISyntaxException e) {
      throw CouchbaseErrors.generalExceptionError(e);
    } catch(Exception e) {
      throw CouchbaseErrors.generalExceptionError(e);
    }
   }
    /**
     * Couchbase connection with url host bucket.
     * @param url coucnbase url
     * @param bucket bucket name
     * @param password password for gucket
     * @return Connection handler of Couchbase url
     * @throws Exception exception
     */
    public Str connect(final Str url, final Str bucket, final Str password)
            throws Exception {
        return connect(url, bucket, password, null);
    }
    /**
     * Couchbase connection with url host bucket and with option.
     * @param url coucnbase url
     * @param bucket bucket name
     * @param password password for gucket
     * @param options nosql options
     * @return Connection handler of Couchbase url
     * @throws Exception exception
     */
    public Str connect(final Str url, final Str bucket, final Str password,
            final Map options) throws Exception {
        final NosqlOptions opts = new NosqlOptions();
        if(options != null) {
            new FuncOptions(Q_COUCHBASE, null).parse(options, opts);
        }
        try {
            String handler = "cb" + couchbaseclients.size();
            List<URI> hosts = Arrays.asList(new URI(url.toJava()));
//            CouchbaseClient client = new CouchbaseClient(hosts,
//                    bucket.toJava(), password.toJava());
            CouchbaseConnectionFactory cf = new CouchbaseConnectionFactory(hosts,
                    bucket.toJava(), password.toJava());
            CouchbaseClient client    =   new CouchbaseClient(cf);
            if(options != null) {
                couchopts.put(handler, opts);
            }
            couchbaseclients.put(handler, client);
            return Str.get(handler);
        } catch (ConfigurationException e) {
            throw CouchbaseErrors.unAuthorised();
        }catch (Exception ex) {
            throw CouchbaseErrors.generalExceptionError(ex);
          }
    }
    /**
     * get CouchbaseClinet from the hashmap.
     * @param handler database handler
     * @return connection instance.
     * @throws QueryException query exception
     */
    protected CouchbaseClient getClient(final Str handler) throws QueryException {
        String ch = handler.toJava();
        final CouchbaseClient client = couchbaseclients.get(ch);
        if(client == null)
          throw CouchbaseErrors.couchbaseClientError(ch);
        return client;

    }
    /**
     * get Couchbase option from particular db handler.
     * @param handler database handler
     * @return MongoOptions
     */
    protected NosqlOptions getCouchbaseOption(final Str handler) {
        NosqlOptions opt = couchopts.get(handler.toJava());
        if(opt != null)
            return opt;
        return null;
    }
    /**
     * This will check the assigned options and then return the final result
     * process by parent class.
     * @param handler database handler
     * @param json json
     * @return Item
     * @throws Exception exception
     */
    protected Item returnResult(final Str handler, final Str json)
            throws Exception {
        final NosqlOptions opt =   getCouchbaseOption(handler);
        Str j = json;
        if(j == null) {
            j =  Str.get("{}");
        }
        if(opt != null) {
            return finalResult(j, opt);
        }
        return finalResult(j, null);
    }
    /**
     * add new document.
     * @param handler database handler Database handler
     * @param key document key
     * @param doc document name
     * @return Item
     * @throws QueryException query exception
     */
    public Item add(final Str handler, final Item key, final Item doc)
            throws QueryException {
        return put(handler, key, doc, "add");
    }
    /**
     * Set method of Couchbase.
     * @param handler database handler
     * @param key document key
     * @param doc document name
     * @return Item
     * @throws QueryException query exception
     */
    public Item set(final Str handler, final Item key,  final Item doc)
            throws QueryException {
        return put(handler, key, doc, "set");
    }
    /**
     * Replace method of Couchbase document with condition.
     * @param handler database handler
      @param key document key
     * @param doc document name
     * @return Item
     * @throws QueryException query exception
     */
    public Item replace(final Str handler, final Item key, final Item doc)
            throws QueryException {
       return put(handler, key, doc, "replace");
    }
    /**
     * Append document with key.
     * @param handler database handler
     * @param key document key
     * @param doc document name
     * @return Item
     * @throws QueryException query exception
     */
    public Item append(final Str handler, final Item key, final Item doc)
            throws QueryException {
        return put(handler, key, doc, null);
    }
    /**
     * document addition.
     * @param handler database handler
     * @param key document key
     * @param doc document name
     * @param type {set,put,append,replace}
     * @return Item
     * @throws QueryException query exception
     */
    private Item put(final Str handler, final Item key, final Item doc,
            final String type) throws QueryException {
        CouchbaseClient client = getClient(handler);
        OperationFuture<Boolean> result = null;
        try {
            if(type != null) {
                if(type.equals("add")) {
                 result = client.add(
                           itemToString(key), itemToJsonString(doc));
                } else if(type.equals("replace")) {
                    result = client.replace(
                           itemToString(key), itemToJsonString(doc));
                } else if(type.equals("set")) {
                   result = client.set(
                           itemToString(key), itemToJsonString(doc));
                } else {
                   result = client.append(
                           itemToString(key), itemToJsonString(doc));
                }
            } else {
                result = client.append(
                        itemToString(key), itemToJsonString(doc));
                //return append(handler, key, doc);
            }
            String msg = result.getStatus().getMessage();
            if(result.get().booleanValue()) {
                return Str.get(msg);
            }
            throw CouchbaseErrors.couchbaseOperationFail(type, msg);
        } catch (Exception ex) {
            throw CouchbaseErrors.generalExceptionError(ex);
        }
    }
    /**
     * get document by key.
     * @param handler database handler
     * @param key document key
     * @return Item
     * @throws QueryException query exception
     */
    public Item get(final Str handler, final Item key) throws QueryException {
        CouchbaseClient client = getClient(handler);
        try {
            Object result =  client.get(itemToString(key));
            if(result == null) {
                result = "{}";
            }
            Str json = Str.get((String) result);
            if(checkJson(json)) {
              return returnResult(handler, json);
            }
            throw CouchbaseErrors.generalExceptionError("invalid json");

        } catch (Exception ex) {
            throw CouchbaseErrors.generalExceptionError(ex);
        }
    }
    /**
     * Get document with options.
     * @param handler database handler
     * @param doc document name
     * @param options other options in map
     * @return Item
     * @throws QueryException query exception
     */
    public Item get(final Str handler, final Str doc, final Map options)
         throws QueryException {
        CouchbaseClient client = getClient(handler);
        try {
            if(options != null) {
                Value keys = options.keys();
                for(final Item key : keys) {
                    if(!(key instanceof Str))
                        throw CouchbaseErrors.
                        couchbaseMessageOneKey("String value expected for '%s' key ",
                                key.toJava());
                    final String k = ((Str) key).toJava();
                    final Value v = options.get(key, null);
                    if(k.equals("add")) {
                        if(v.type().instanceOf(SeqType.STR)) {
                            Str s = (Str) v.toJava();
                            return s;
                        }
                    }
                }
            }
            Object json = client.get(doc.toJava());
            if(json == null) {
                json = "{}";
            }
            Str jsonStr = Str.get((String) json);
            return returnResult(handler, jsonStr);
        } catch (Exception ex) {
            throw CouchbaseErrors.generalExceptionError(ex);
        }
    }
    /** Get items with multiple keys.
     * @param handler database handler
     * @param keyItems key sequences like (1,2,3)
     * @return Item
     * @throws QueryException query exception
     */
    public Item getbulk(final Str handler, final Value keyItems) throws QueryException {
        CouchbaseClient client = getClient(handler);
         try {
             if(keyItems.size() < 1) {
                 throw CouchbaseErrors.keysetEmpty();
             }
             List<String> keys = new ArrayList<>();
             for (Value v: keyItems) {
                String s = (String) v.toJava();
                keys.add(s);
             }
             java.util.Map<String, Object> bulkset = client.getBulk(keys);
             Str json = getBulkJson(bulkset);
             return returnResult(handler, json);
         } catch (Exception ex) {
            throw new QueryException(ex);
        }
    }
    /**
     * Process Java Map<String, Object> (key/value), and return JSON Str.
     * @param bulkset java Map<String Object> for key and value set
     * @return json (STR)
     */
    protected Str getBulkJson(final java.util.Map<String, Object> bulkset) {
        final StringBuilder json = new StringBuilder();
        json.append("{ ");
        for (String key: bulkset.keySet()) {
            if(json.length() > 2) json.append(", ");
            json.append('"').append(key).append('"').append(" : ");
            String value = (String) bulkset.get(key);
            if(value != null) {
                value = value.trim();
                if(value.charAt(0) == '{' || value.charAt(0) == '[') {
                    json.append(value);
                } else {
                    json.append('"').append(value.replaceAll("\"", "\\\"")).append('"');;
                }
            } else {
                json.append('"').append("").append('"');
            }
        }
        json.append(" } ");
        return Str.get(json.toString());
    }
    /**
     * Delete document by key.
     * @param handler database handler
     * @param key document key.
     * @return Item
     * @throws QueryException query exception
     */
    public Item delete(final Str handler, final Str key) throws QueryException {
        CouchbaseClient client = getClient(handler);
        try {
            OperationFuture<Boolean> result = client.delete(key.toJava());
            String msg = result.getStatus().getMessage();
            if(result.get().booleanValue()) {
                return Str.get(msg);
            }
            throw CouchbaseErrors.couchbaseOperationFail("delete", msg);
        } catch (Exception ex) {
            throw CouchbaseErrors.generalExceptionError(ex);
        }
    }
    /**
     * Create view without reduce method.
     * @param handler database handler
     * @param doc document name
     * @param viewName view name
     * @param map map function
     * @return Item
     * @throws QueryException query exception
     */
    public Item createview(final Str handler, final Str doc, final Str viewName,
            final Str map) throws QueryException {
        return createview(handler, doc, viewName, map, null);
    }
    /**
     * Create view with reduce method.
     * @param handler database handler Database handler
     * @param doc document name
     * @param viewName view name
     * @param map map function
     * @param reduce reduce function
     * @return Item
     * @throws QueryException query exception
     */
    public Item createview(final Str handler, final Str doc, final Str viewName,
            final Str map, final Str reduce) throws QueryException {
        CouchbaseClient client = getClient(handler);
        if(map == null) {
            throw CouchbaseErrors.mapEmpty();
        }
        try {
            DesignDocument designDoc = new DesignDocument(doc.toJava());
            ViewDesign viewDesign;
            if(reduce != null) {
               viewDesign = new ViewDesign(viewName.toJava(),
                       map.toJava(), reduce.toJava());
            } else {
                viewDesign = new ViewDesign(viewName.toJava(), map.toJava());
            }
            designDoc.getViews().add(viewDesign);
           boolean success = client.createDesignDoc(designDoc);
           if(success) {
               return Str.get("ok");
           }
          throw CouchbaseErrors.
          generalExceptionError("There is something wrong when creating View");
        } catch (Exception e) {
            throw CouchbaseErrors.generalExceptionError(e);
        }
    }
    /**
     * convert Map of Basex to the query object of Couchbase.
     * @param options Query in map  like {'limit':1}
     * @return Query
     * @throws QueryException query exception
     */
    protected Query query(final Map options) throws QueryException {
        Query q = new Query();
        if(options != null) {
            Value keys = options.keys();
            for(final Item key : keys) {
                if(!(key instanceof Str))
                    throw CouchbaseErrors.
                    couchbaseMessageOneKey("String value expected for '%s' key ",
                            key.toJava());
                final String k = ((Str) key).toJava();
                final Value v = options.get(key, null);
                if(k.equals(VIEWMODE)) {
                    System.setProperty(VIEWMODE, v.toJava().toString());
                } else if(k.equals(LIMIT)) {
                    if(v.type().instanceOf(SeqType.ITR_OM)) {
                        long l = ((Item) v).itr(null);
                        q.setLimit((int) l);
                    } else {
                        throw CouchbaseErrors.
                        couchbaseMessageOneKey("Integer value expected for '%s' key ",
                                key.toJava());
                    }
                } else if(k.equals(STALE)) {
                    String s = ((Item) v).toString();
                    if(s.equals(OK))
                        q.setStale(Stale.OK);
                    else if(s.equals(FALSE))
                        q.setStale(Stale.FALSE);
                    else if(s.equals(UPDATE_AFTER))
                        q.setStale(Stale.UPDATE_AFTER);
                } else if(k.equals(KEY)) {
                    q.setKey(((Item) v).toString());
                } else if(k.equals(DESCENDING)) {
                    boolean desc = ((Item) v).bool(null);
                    q.setDescending(desc);
                } else if(k.equals(DEBUG)) {
                    q.setDebug(((Item) v).bool(null));
                } else if(k.equals(REDUCE)) {
                    q.setReduce(((Item) v).bool(null));
                } else if(k.equals(GROUP)) {
                    boolean d = ((Item) v).bool(null);
                    q.setGroup(d);
                } else if(k.equals(STARTKEY)) {
                    String s = ((Str) v).toJava();
                    q.setStartkeyDocID(s);
                } else if(k.equals(ENDKEY)) {
                    String s = ((Item) v).toString();
                    q.setEndkeyDocID(s);
                } else if(k.equals(SKIP)) {
                    if(v.type().instanceOf(SeqType.ITR_OM)) {
                        long l = ((Item) v).itr(null);
                        q.setSkip((int) l);
                    } else {
                        throw CouchbaseErrors.
                        couchbaseMessageOneKey("Integer value expected for '%s' key ",
                                key.toJava());
                    }
                } else if(k.equals(GROUP_LEVEL)) {
                    if(v.type().instanceOf(SeqType.ITR_OM)) {
                        long l = ((Item) v).itr(null);
                        q.setGroupLevel((int) l);
                    } else {
                        throw CouchbaseErrors.
                        couchbaseMessageOneKey("Integer value expected for '%' key ",
                                key.toJava());
                    }
                } else if(k.equals(RANGE)) {
                    if(!(v instanceof Map)) {
                        throw CouchbaseErrors.
                        couchbaseMessageOneKey(" Map is expected for key '%'",
                                key.toJava());
                    }
                    Map range = (Map) v;
                    Value s = range.get(Str.get(STARTKEY), null);
                    Value e = range.get(Str.get(ENDKEY), null);
                    String msg = (s == null) ? " 'startkey' is empty" : (e == null) ?
                            "'endkey' is empty" : null;
                    if(msg != null) {
                        throw CouchbaseErrors.
                        generalExceptionError(msg);
                    }
                    q.setRange(ckey(s), ckey(s));
                } else if(k.toLowerCase().equals(KEYS)) {
                    ComplexKey ckey = ckey(v);
                   if(ckey != null) {
                       q.setKeys(ckey);
                   }
                } else if(k.toLowerCase().equals(VALUEONLY)) {
                    valueOnly = ((Item) v).bool(null);
                } else if(k.toLowerCase().equals("solution")) {
                    ((Item) v).bool(null);
                } else if(k.toLowerCase().equals(INCLUDEDOCS)) {
                    q.setIncludeDocs(true);
                }
            }
        }
        return q;
    }
    /**
     * Get data from view without any option.
     * @param handler database handler
     * @param doc document name
     * @param viewName view name
     * @return Item
     * @throws QueryException query exception
     */
    public Item getview(final Str handler, final Str doc, final Str viewName)
            throws QueryException {
        return getview(handler, doc, viewName, null);
    }
    /**
     * view with mode Option.
     * @param handler database handler
     * @param doc document name
     * @param viewName view name
     * @param query options like limit and so on(not completed)
     * @return Item
     * @throws QueryException query exception
     */
    public Item getview(final Str handler, final Str doc, final Str viewName,
            final Map query) throws QueryException {
        final CouchbaseClient client = getClient(handler);
        valueOnly = false;
        Query q = this.query(query);
        try {
            View view = client.getView(doc.toJava(), viewName.toJava());
            ViewResponse response = client.query(view, q);
            Str json = valueOnly ? viewResponseToJsonValueOnly(response)
                    : viewResponseToJson(response);
            return returnResult(handler, json);
        } catch (Exception e) {
            throw CouchbaseErrors.generalExceptionError(e);
        }
    }
    /**
     * convert sequence into couchbase's java complexkey.
     * @param v sequence
     * @return ComplexKey
     * @throws QueryException query exception
     */
    protected ComplexKey ckey(final Value v) throws QueryException {
        if(v.size() <= 0) {
            throw CouchbaseErrors.
            couchbaseMessageOneKey("items for '%s' cannot be empty",
                    v.toJava());
        }
        Object [] keys = new Object[(int) v.size()];
        int i = 0;
        for(Value key: v) {
            keys[i] = key.toJava();
            i++;
        }
        ComplexKey k = ComplexKey.of(keys);
        return k;
    }
    /**
     * create Json format string from view Response.
     * @param viewResponse couchbase ViewResponse
     * @return Str
     */
    protected Str viewResponseToJson(final ViewResponse viewResponse) {
        final StringBuilder json = new StringBuilder();
        json.append("{ ");
        for (ViewRow v: viewResponse) {
            if(json.length() > 2) json.append(", ");
            json.append(quote(v.getKey())).append(" : ");
            String value = v.getValue();
            if(value != null) {
                value = Str.get(value.trim()).toJava();
                if(value.charAt(0) == '{' || value.charAt(0) == '[') {
                    json.append(v.getValue());
                } else {
                    json.append('"').append(value.replaceAll("\"", "\\\"")).
       append('"');;
                }
            } else {
                json.append('"').append("").append('"');
            }
        }
        json.append(" } ");
        return Str.get(json.toString());
    }
    /**
     * create Json format string from view Response.
     * @param viewResponse couchbase ViewResponse
     * @return Str
     */
    protected Str viewResponseToJsonValueOnly(final ViewResponse viewResponse) {
        final StringBuilder json = new StringBuilder();
        json.append("[ ");
        for (ViewRow v: viewResponse) {
            if(json.length() > 2) json.append(", ");
            //json.append('"').append(v.getKey()).append('"').append(" : ");
            String value = v.getValue();
            if(value != null) {
                value = value.trim();
                if(value.charAt(0) == '{' || value.charAt(0) == '[') {
                    json.append(v.getValue());
                } else {
                    json.append('"').append(value.replaceAll("\"", "\\\"")).append('"');;
                }
            } else {
                json.append('"').append("").append('"');
            }
        }
        json.append(" ] ");
        return Str.get(json.toString());
    }
    /**
     * close database instanses.
     * @param handler database handler
     * @throws QueryException query exception
     */
    public void disconnect(final Str handler) throws QueryException {
      disconnect(handler, null);
    }
    /**
     * close database connection after certain time.
     * @param handler database handler
     * @param time in seconds
     * @throws QueryException query exception
     */
    public void disconnect(final Str handler, final Item time)
            throws QueryException {
        CouchbaseClient client = getClient(handler);
        if(time != null) {
            if(!time.type().instanceOf(SeqType.ITR)) {
                throw CouchbaseErrors.timeInvalid();
            }
            long seconds = time.itr(null);
            boolean result = client.shutdown(seconds, TimeUnit.SECONDS);
            if(!result) {
                throw CouchbaseErrors.shutdownError();
            }
        } else {
            client.shutdown();
        }
    }
    /**
     * This method check if the database handler is still connected or not.
     * @param handler database handler.
     * @return Bln true or false
     * @throws QueryException query exception
     */
    public Bln isconnected(final Str handler) throws QueryException {
        CouchbaseClient client = getClient(handler);
        if(client == null){
          return Bln.FALSE;
        }
        return Bln.TRUE;
    }
    /**
     * Convert viewresponse to JSON in pattern of Key Value like {key:value} or
     * [{key:value},{key:value}..].
     * @param vr Couchbase {@link ViewResponse}
     * @return String
     */
    protected String vrTOJson(final ViewResponse vr) {
        try {
            JSONArray j = new JSONArray();
            int size = vr.size();
            if(size > 0) {
               for(ViewRow r: vr) {
                JSONObject jo = new JSONObject();
                jo.put(r.getKey(), r.getValue());
                if(size == 1)
                    return jo.toString();
                j.put(jo);
               }
               return j.toString();
           }
        } catch (JSONException e) {
            CouchbaseErrors.generalExceptionError(e);
        }
       return null;
    }
}
