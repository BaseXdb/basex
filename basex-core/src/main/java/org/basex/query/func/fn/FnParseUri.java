package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.util.*;
import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public class FnParseUri extends FnJsonDoc {
  /** URI part. */
  static final String URI = "uri";
  /** URI part. */
  static final String SCHEME = "scheme";
  /** URI part. */
  static final String HIERARCHICAL = "hierarchical";
  /** URI part. */
  static final String AUTHORITY = "authority";
  /** URI part. */
  static final String USERINFO = "userinfo";
  /** URI part. */
  static final String HOST = "host";
  /** URI part. */
  static final String PORT = "port";
  /** URI part. */
  static final String PATH = "path";
  /** URI part. */
  static final String QUERY = "query";
  /** URI part. */
  static final String FRAGMENT = "fragment";
  /** URI part. */
  static final String PATH_SEGMENTS = "path-segments";
  /** URI part. */
  static final String QUERY_PARAMETERS = "query-parameters";
  /** URI part. */
  static final String FILEPATH = "filepath";
  /** URI part. */
  static final String KEY = "key";
  /** URI part. */
  static final String VALUE = "value";

  /** File scheme. */
  static final String FILE = "file";
  /** Non-hierarchical schemes. */
  static final HashSet<String> NON_HIERARCHICAL = new HashSet<>(
      Arrays.asList("jar", "mailto", "news", "tag", "tel", "urn"));
  /** Scheme/port mappings. */
  static final Map<String, String> PORTS = Map.of(
      "http", "80", "https", "443", "ftp", "21", "ssh", "22");

  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String value = toString(arg(0), qc);
    final UriOptions options = toOptions(arg(1), new UriOptions(), false, qc);

    String string = value.replace('\\', '/');
    String fragment = "", query = "", scheme = "", filepath = "", authority = "", userinfo = "";
    String host = "", port = "", path = "";

    // strip off the fragment identifier and any query
    Matcher m = Pattern.compile("^(.*?)#(.*)$").matcher(string);
    if(m.matches()) {
      string = m.group(1);
      fragment = m.group(2);
    }
    m = Pattern.compile("^(.*?)\\?(.*)$").matcher(string);
    if(m.matches()) {
      string = m.group(1);
      query = m.group(2);
    }

    // attempt to identify the scheme
    if(string.matches("^[a-zA-Z][:|].*$")) {
      scheme = FILE;
      string = string.replaceAll("^(.)\\|", "$1:");
      filepath = string;
      string = "/" + string;
    } else {
      m = Pattern.compile("^([a-zA-Z][-+.A-Za-z0-9]*):(.*)$").matcher(string);
      if(m.matches()) {
        scheme = m.group(1);
        string = m.group(2);
      }
    }

    // determine if the URI is hierarchical
    final Item hierarchical = NON_HIERARCHICAL.contains(scheme) ? Bln.FALSE :
      string.isEmpty() ? Empty.VALUE : Bln.get(string.startsWith("/"));

    // examine the remaining parts of the string
    if(scheme.isEmpty() && options.get(UriOptions.UNC_PATH) && string.matches("^//[^/].*$")) {
      scheme = FILE;
      filepath = string;
    } else {
      m = Pattern.compile("^/*(/[a-zA-Z]:.*)$").matcher(string);
      if((scheme.isEmpty() || scheme.equals(FILE)) && m.matches()) {
        string = m.group(1);
      } else {
        m = Pattern.compile("^///*([^/]+)?(/.*)?$").matcher(string);
        if(m.matches()) {
          authority = m.group(1);
          string = m.group(2);
        }
      }
    }
    if(string == null) string = "";

    // parse userinfo
    m = Pattern.compile("^(([^@]*)@)(.*)(:([^:]*))?$").matcher(authority);
    if(m.matches()) {
      userinfo = m.group(2);
      if(!options.get(UriOptions.ALLOW_DEPRECATED_FEATURES) && userinfo.contains(":")) {
        userinfo = "";
      }
    }
    // parse host
    m = Pattern.compile("^(([^@]*)@)?(\\[[^\\]]*\\])(:([^:]*))?$").matcher(authority);
    if(m.matches()) {
      host = m.group(3);
    } else if(authority.matches("^(([^@]*)@)?\\[.*$")) {
      throw PARSE_URI_X.get(info, value);
    } else {
      m = Pattern.compile("^(([^@]*)@)?([^:]+)(:([^:]*))?$").matcher(authority);
      if(m.matches()) host = m.group(3);
    }
    if(host == null) host = "";
    // an IPv6/IPvFuture address may contain a colon
    m = Pattern.compile("^(([^@]*)@)?(\\[[^\\]]*\\])(:([^:]*))?$").matcher(authority);
    if(m.matches()) {
      port = m.group(5);
    } else {
      m = Pattern.compile("^(([^@]*)@)?([^:]+)(:([^:]*))?$").matcher(authority);
      if(m.matches()) port = m.group(5);
    }
    if(port == null) port = "";
    if(omitPort(port, scheme, options)) port = "";

    path = string;
    if(filepath.isEmpty() && (scheme.isEmpty() || scheme.equals(FILE))) {
      filepath = string;
    }

    final TokenList segments = new TokenList();
    if(!string.isEmpty()) {
      final String separator = Pattern.quote(options.get(UriOptions.PATH_SEPARATOR));
      for(final String s : string.split(separator)) segments.add(decode(s));
    }

    XQMap queries = XQMap.empty();
    if(!query.isEmpty()) {
      final String separator = Pattern.quote(options.get(UriOptions.QUERY_SEPARATOR));
      for(final String q : query.split(separator)) {
        final int eq = q.indexOf('=');
        final Str key = eq == -1 ? Str.EMPTY : Str.get(q.substring(0, eq));
        final Str val = Str.get(q.substring(eq + 1));
        queries = queries.put(key, ValueBuilder.concat(queries.get(key, info), val, qc), info);
      }
    }
    filepath = decode(filepath);

    final MapBuilder map = new MapBuilder(info);
    add(map, URI, value);
    add(map, SCHEME, scheme);
    add(map, HIERARCHICAL, hierarchical);
    add(map, AUTHORITY, authority);
    add(map, USERINFO, userinfo);
    add(map, HOST, host);
    add(map, PORT, port);
    add(map, PATH, path);
    add(map, QUERY, query);
    add(map, FRAGMENT, fragment);
    add(map, PATH_SEGMENTS, StrSeq.get(segments));
    add(map, QUERY_PARAMETERS, queries);
    add(map, FILEPATH, filepath);
    return map.map();
  }

  /**
   * Adds a non-empty map entry.
   * @param map map
   * @param k key
   * @param v value
   * @throws QueryException query exception
   */
  static void add(final MapBuilder map, final String k, final Object v)
      throws QueryException {

    final Value value = v instanceof Value ? (Value) v : v.toString().isEmpty() ? Empty.VALUE :
      Str.get(v.toString());
    if(!(value.isEmpty() ||
        value instanceof XQMap && ((XQMap) value).mapSize() == 0 ||
        value instanceof XQArray && ((XQArray) value).arraySize() == 0)) {
      map.put(k, value);
    }
  }

  /**
   * URI-decodes a string.
   * @param string encoded string
   * @return decoded string
   */
  static String decode(final String string) {
    return Token.string(XMLToken.decodeUri(Token.token(string), true));
  }

  /**
   * Checks if the port can be omitted.
   * @param port port
   * @param scheme scheme
   * @param options options
   * @return result of check
   */
  static boolean omitPort(final String port, final String scheme, final UriOptions options) {
    return options.get(UriOptions.OMIT_DEFAULT_PORTS) &&
        Objects.equals(PORTS.get(scheme), port);
  }
}
