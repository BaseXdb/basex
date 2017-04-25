package org.basex.modules.nosql;

import org.basex.build.JsonOptions;
import org.basex.build.JsonParserOptions;
import org.basex.io.parse.json.JsonConverter;
import org.basex.io.serial.SerialMethod;
import org.basex.io.serial.SerializerOptions;
import org.basex.modules.nosql.NosqlOptions.NosqlFormat;
import org.basex.query.QueryException;
import org.basex.query.QueryModule;
import org.basex.query.func.FNJson;
import org.basex.query.func.Function;
import org.basex.query.value.item.Item;
import org.basex.query.value.item.Str;
import org.basex.query.value.map.Map;

/**
 * All Nosql database common functionality.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Prakash Thapa
 */
abstract class Nosql extends QueryModule {
  /* for couchbase. **/
  /** descending. */
  protected static final String DESCENDING = "descending";
  /** endkey. */
  protected static final String ENDKEY = "endkey";
  /** group. */
  protected static final String GROUP = "group";
  /** group_level. */
  protected static final String GROUP_LEVEL = "group_level";
  /** key. */
  protected static final String KEY = "key";
  /** keys. */
  protected static final String KEYS = "keys";
  /** limit. */
  protected static final String LIMIT = "limit";
  /** reduce. */
  protected static final String REDUCE = "reduce";
  /** skip. */
  protected static final String SKIP = "skip";
  /** stale. */
  protected static final String STALE = "stale";
  /** startkey. */
  protected static final String STARTKEY = "startkey";
  /** debug. */
  protected static final String DEBUG = "debug";
  /** viewmode. */
  protected static final String VIEWMODE = "viewmode";
  /** ok. */
  protected static final String OK = "ok";
  /** false. */
  protected static final String FALSE = "false";
  /** update_after. */
  protected static final String UPDATE_AFTER = "update_after";
  /** range. */
  protected static final String RANGE = "range";
  /** valueonly. */
  protected static final String VALUEONLY = "valueonly";
  /** includedocs. */
  protected static final String INCLUDEDOCS = "includedocs";
  /** descending. */
  /** for mongodb **/
  /** sort. */
  protected static final String SORT = "sort";
  /** count. */
  protected static final String COUNT = "count";
  /** explain. */
  protected static final String EXPLAIN = "explain";
  /** map. */
  protected static final String MAP = "map";
  /** query. */
  protected static final String QUERY = "query";
  /** finalalize. */
  protected static final String FINALIZE = "finalalize";
  /** for rethink **/
  /** id. */
  protected static final String ID = "id";
  /** qnmOptions. */
    /**
     * convert Str to java string.
     * @param item Str.
     * @return String
     * @throws QueryException query exception
     */
    protected String itemToString(final Item item) throws QueryException {
        if(item instanceof Str) {
            try {
                String string = ((Str) item).toJava();
                return string;
            } catch (Exception e) {
                throw new QueryException("Item is not in well format");
            }
        }
        throw new QueryException("Item is not in Str format");
    }
    /**
     * string format for json.
     * @param json json string
     * @return Item formated json
     * @throws QueryException query exception
     */
    protected Item formatjson(final Str json)
            throws QueryException {
        final SerializerOptions sopts = new SerializerOptions();
        sopts.set(SerializerOptions.METHOD, SerialMethod.JSON);
        Item x = new FNJson(staticContext, null, Function.SERIALIZE,
                json).item(queryContext, null);
        return x;
    }
    /**
     * check json string and if valid return java string as result.
     * @param json json string
     * @return Item
     * @throws QueryException query exception
     */
    protected String itemToJsonString(final Item json) throws QueryException {
        if(json instanceof Str) {
            try {
                boolean jsoncheck = checkJson(json);
                if(jsoncheck) {
                    String string = ((Str) json).toJava();
                    return string;
                }
            } catch (Exception e) {
                throw new QueryException("Item is not in well format");
            }
        } else {
            throw new QueryException("Item is not in Str format");
        }
        return null;
    }
    /**
     *check if Item is valid json not.
     * @param doc Item (string)
     * @return boolean
     * @throws QueryException exception
     */
    protected boolean checkJson(final Item doc) throws QueryException {
        try {
            new FNJson(staticContext, null, Function._JSON_PARSE,
                    doc).item(queryContext, null);
            return true;
        } catch (Exception e) {
            throw new QueryException("document is not in json format");
        }
    }
    /**
     * Convert json string to appropriate result using NosqlOption's type.
     * @param json json Str
     * @param opt Nosql Options
     * @return Item
     * @throws Exception exception
     */
    protected Item finalResult(final Str json, final NosqlOptions opt)
            throws Exception {
            try {
                if(opt != null) {
                    if(opt.get(NosqlOptions.TYPE) == NosqlFormat.XML) {
                        final JsonParserOptions opts = new JsonParserOptions();
                        opts.set(JsonOptions.FORMAT, opt.get(JsonOptions.FORMAT));
                        final JsonConverter conv = JsonConverter.get(opts);
                        conv.convert(json.string(), null);
                        return conv.finish();
                    }
                    Item xXml = new FNJson(staticContext, null,
                            Function._JSON_PARSE, json).
                            item(queryContext, null);
                    return new FNJson(staticContext, null,
                            Function._JSON_SERIALIZE, xXml).
                            item(queryContext, null);
                }
                return new FNJson(staticContext, null, Function._JSON_PARSE, json).
                        item(queryContext, null);
            } catch (final Exception ex) {
                throw new QueryException(ex);
            }
    }
    /**
     * insert key/value pair into Basex Map.
     * @param m Map
     * @param k key
     * @param v value
     * @return Map
     * @throws QueryException query exception
     */
   protected Map insert(final Map m, final String k, final String v)
           throws QueryException {
       m.insert(Str.get(k), Str.get(v), null);
       return m;
   }
   /**
    * check all special characters in string for valid json key.
    * @param string String value to be checked
    * @return String
    */
   protected String quote(final String string) {
     if (string == null || string.length() == 0) {
         return "\"\"";
     }

     char         c = 0;
     int          i;
     int          len = string.length();
     StringBuilder sb = new StringBuilder(len + 4);
     String       t;

     sb.append('"');
     for (i = 0; i < len; i += 1) {
         c = string.charAt(i);
         switch (c) {
         case '\\':
         case '"':
             sb.append('\\');
             sb.append(c);
             break;
         case '/':
//               if (b == '<') {
                 sb.append('\\');
//               }
             sb.append(c);
             break;
         case '\b':
             sb.append("\\b");
             break;
         case '\t':
             sb.append("\\t");
             break;
         case '\n':
             sb.append("\\n");
             break;
         case '\f':
             sb.append("\\f");
             break;
         case '\r':
            sb.append("\\r");
            break;
         default:
             if (c < ' ') {
                 t = "000" + Integer.toHexString(c);
                 sb.append("\\u" + t.substring(t.length() - 4));
             } else {
                 sb.append(c);
             }
         }
     }
     sb.append('"');
     return sb.toString();
 }
}
