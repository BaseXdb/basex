package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.util.*;
import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FnParseUri extends StandardFunc {
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
  /** Absolute. */
  static final String ABSOLUTE = "absolute";

  /** File scheme. */
  static final String FILE = "file";
  /** Non-hierarchical schemes. */
  static final HashSet<String> NON_HIERARCHICAL = new HashSet<>(
      Arrays.asList("jar", "mailto", "news", "tag", "tel", "urn"));
  /** Scheme/port mappings. */
  static final Map<String, String> PORTS = Map.of(
      "http", "80", "https", "443", "ftp", "21", "ssh", "22");

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String value = toStringOrNull(arg(0), qc);
    final UriOptions options = toOptions(arg(1), new UriOptions(), qc);
    if(value == null) return Empty.VALUE;

    String string = value.replace('\\', '/'), fragment = "", query = "", scheme = "";
    String filepath = "", authority = "", userinfo = "", host = "", port = "", path;

    // strip off the fragment identifier and any query
    final Regex r = new Regex();
    if(r.has(string, "^(.*?)#(.*)$")) {
      string = r.group(1);
      fragment = XMLToken.decodeUri(r.group(2));
    }
    if(r.has(string, "^(.*?)\\?(.*)$")) {
      string = r.group(1);
      query = r.group(2);
    }

    // attempt to identify the scheme
    if(r.has(string, "^([a-zA-Z][-+.A-Za-z0-9]+):(.*)$")) {
      scheme = r.group(1);
      string = r.group(2);
    }
    boolean absolute = !scheme.isEmpty() && fragment.isEmpty();

    if(scheme.isEmpty() || scheme.equalsIgnoreCase(FILE)) {
      if(r.has(string, "^/*([a-zA-Z][:|].*)$")) {
        scheme = FILE;
        string = '/' + r.group(1).replaceAll("^(.)\\|", "$1:");
      } else if(options.get(UriOptions.UNC_PATH)) {
        scheme = FILE;
      }
    }

    // determine if the URI is hierarchical
    final Item hierarchical = NON_HIERARCHICAL.contains(scheme) ? Bln.FALSE :
      string.isEmpty() ? Empty.VALUE : Bln.get(string.startsWith("/"));
    if(hierarchical == Bln.FALSE) absolute = false;

    // identify the remaining components
    if(scheme.equalsIgnoreCase(FILE)) {
      if(options.get(UriOptions.UNC_PATH) && r.has(string, "^/*(//[^/].*)$")) {
        string = r.group(1);
        filepath = string;
      } else if(r.has(string, "^/+[A-Za-z]:/.*$")) {
        string = string.replaceAll("^/+", "/");
        filepath = string.replaceAll("^/", "");
      } else {
        string = string.replaceAll("^/+", "/");
        filepath = string;
      }
    } else if(hierarchical == Bln.TRUE) {
      if(r.has(string, "^//([^/]+)$")) {
        authority = r.group(1);
        string = "";
      } else if(r.has(string, "^//([^/]*)(/.*)$")) {
        authority = r.group(1);
        string = r.group(2);
      }
    }

    // parse userinfo
    if(r.has(authority, "^(([^@]*)@)(.*)(:([^:]*))?$")) {
      userinfo = r.group(2);
      if(!options.get(UriOptions.ALLOW_DEPRECATED_FEATURES) && userinfo.contains(":")) {
        userinfo = "";
      }
    }
    // parse host
    if(r.has(authority, "^(([^@]*)@)?(\\[[^\\]]*\\])(:([^:]*))?$")) {
      host = r.group(3);
    } else if(r.has(authority, "^(([^@]*)@)?\\[.*$")) {
      throw PARSE_URI_X.get(info, value);
    } else if(r.has(authority, "^(([^@]*)@)?([^:]+)(:([^:]*))?$")) {
      host = r.group(3);
    }
    if(host == null) host = "";
    // an IPv6/IPvFuture address may contain a colon
    if(r.has(authority, "^(([^@]*)@)?(\\[[^\\]]*\\])(:([^:]*))?$")) {
      port = r.group(5);
    } else if(r.has(authority, "^(([^@]*)@)?([^:]+)(:([^:]*))?$")) {
      port = r.group(5);
    }
    if(port == null) port = "";
    if(omitPort(port, scheme, options)) port = "";

    path = string;
    if(filepath.isEmpty() && (scheme.isEmpty() || scheme.equalsIgnoreCase(FILE))) {
      filepath = string;
    }

    final TokenList segments = new TokenList();
    if(!string.isEmpty()) {
      for(final String s : string.split("/", -1)) segments.add(XMLToken.decodeUri(s));
    }

    XQMap queries = XQMap.empty();
    if(!query.isEmpty()) {
      for(final String q : query.split("&")) {
        final int eq = q.indexOf('=');
        final Str key = eq == -1 ? Str.EMPTY : Str.get(XMLToken.decodeUri(q.substring(0, eq)));
        final Str val = Str.get(XMLToken.decodeUri(q.substring(eq + 1)));
        queries = queries.put(key, queries.get(key).append(val, qc));
      }
    }
    filepath = XMLToken.decodeUri(filepath);

    final MapBuilder mb = new MapBuilder();
    add(mb, URI, value);
    add(mb, SCHEME, scheme);
    add(mb, HIERARCHICAL, hierarchical);
    add(mb, AUTHORITY, authority);
    add(mb, USERINFO, userinfo);
    add(mb, HOST, host);
    add(mb, PORT, port);
    add(mb, PATH, path);
    add(mb, QUERY, query);
    add(mb, FRAGMENT, fragment);
    add(mb, PATH_SEGMENTS, StrSeq.get(segments));
    add(mb, QUERY_PARAMETERS, queries);
    add(mb, FILEPATH, filepath);
    if(absolute) add(mb, ABSOLUTE, Bln.TRUE);
    return mb.map();
  }

  /**
   * Adds a non-empty map entry.
   * @param mb map
   * @param k key
   * @param v value
   * @throws QueryException query exception
   */
  static void add(final MapBuilder mb, final String k, final Object v) throws QueryException {
    final Value value = v instanceof Value ? (Value) v : v.toString().isEmpty() ? Empty.VALUE :
      Str.get(v.toString());
    if(!(value.isEmpty() || value == XQMap.empty())) mb.put(k, value);
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

  /** Helper class for regex matching. */
  private static final class Regex {
    /** Current matcher. */
    private Matcher m;

    /**
     * Attempts to find the input in the pattern.
     * @param pattern pattern
     * @param input input
     * @return result of check
     */
    boolean has(final String pattern, final String input) {
      m = Pattern.compile(input).matcher(pattern);
      return m.find();
    }

    /**
     * Returns the specified group.
     * @param i index
     * @return string
     */
    String group(final int i) {
      return m.group(i);
    }
  }
}
