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
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public class FnParseUri extends FnJsonDoc {
  /** Merge options. */
  public static final class ParseOptions extends Options {
    /** Option. */
    public static final StringOption PATH_SEPARATOR =
        new StringOption("path-separator", "/");
    /** Option. */
    public static final StringOption QUERY_SEPARATOR =
        new StringOption("query-separator", "&");
    /** Option. */
    public static final BooleanOption ALLOW_DEPRECATED_FEATURES =
        new BooleanOption("allow-deprecated-features", false);
    /** Option. */
    public static final BooleanOption OMIT_DEFAULT_PORTS =
        new BooleanOption("omit-default-ports", false);
    /** Option. */
    public static final BooleanOption UNC_PATH =
        new BooleanOption("unc-path", false);
  }

  /** File scheme. */
  private static final String FILE = "file";
  /** Standard ports. */
  private static HashMap<String, String> ports = new HashMap<>();

  static {
    ports.put("http", "80");
    ports.put("https", "443");
    ports.put("ftp", "21");
    ports.put("ssh", "22");
  }

  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String value = toString(arg(0), qc);
    final ParseOptions options = toOptions(arg(1), new ParseOptions(), false, qc);

    String string = value.replace('\\', '/');
    String fragment = "", query = "", scheme = "", filepath = "", authority = "", userinfo = "";
    String host = "", port = "", path = "";
    Item hierarchical = Empty.VALUE;

    Matcher m = Pattern.compile("^(.*)#([^#]*)$").matcher(string);
    if(m.matches()) {
      fragment = m.group(2);
      string = m.group(1);
    }
    m = Pattern.compile("^(.*)\\?([^?]*)$").matcher(string);
    if(m.matches()) {
      query = m.group(2);
      string = m.group(1);
    }
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
    if(options.get(ParseOptions.UNC_PATH) && scheme.isEmpty() && string.matches("^//[^/].*$")) {
      scheme = FILE;
      filepath = string;
    }
    if(!string.isEmpty()) {
      hierarchical = Bln.get(string.startsWith("/"));
    }
    m = Pattern.compile("^//*([a-zA-Z]:.*)$").matcher(string);
    if(m.matches()) {
      string = m.group(1);
    } else {
      m = Pattern.compile("^///*([^/]+)(/.*)?$").matcher(string);
      if(m.matches()) {
        authority = m.group(1);
        string = m.group(2);
      }
    }
    if(string == null) string = "";
    m = Pattern.compile("^(([^@]*)@)(.*)(:([^:]*))?$").matcher(authority);
    if(m.matches()) {
      userinfo = m.group(2);
      if(!options.get(ParseOptions.ALLOW_DEPRECATED_FEATURES) && userinfo.contains(":")) {
        userinfo = "";
      }
    }
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
    m = Pattern.compile("^(([^@]*)@)?(\\[[^\\]]*\\])(:([^:]*))?$").matcher(authority);
    if(m.matches()) {
      port = m.group(5);
    } else {
      m = Pattern.compile("^(([^@]*)@)?([^:]+)(:([^:]*))?$").matcher(authority);
      if(m.matches()) port = m.group(5);
    }
    if(port == null) port = "";
    if(options.get(ParseOptions.OMIT_DEFAULT_PORTS) && Objects.equals(ports.get(scheme), port)) {
      port = "";
    }
    path = string;
    if(filepath.isEmpty() && (scheme.isEmpty() || scheme.equals(FILE))) {
      filepath = string;
    }

    ArrayBuilder segments = new ArrayBuilder();
    if(!string.isEmpty()) {
      for(final String s : string.split(Pattern.quote(options.get(ParseOptions.PATH_SEPARATOR)))) {
        // TODO: invalid Unicode?
        segments.append(Str.get(decode(s)));
      }
    }

    ArrayBuilder queries = new ArrayBuilder();
    if(!query.isEmpty()) {
      for(final String q : query.split(Pattern.quote(options.get(ParseOptions.QUERY_SEPARATOR)))) {
        final int eq = q.indexOf('=');
        final String k = eq == -1 ? "" : q.substring(0, eq), v = q.substring(eq + 1);
        queries.append(new MapBuilder(info).put("key", k).put("value", v).map());
      }
    }
    filepath = decode(filepath);

    final MapBuilder map = new MapBuilder(info);
    add(map, "uri", value);
    add(map, "scheme", scheme);
    add(map, "hierarchical", hierarchical);
    add(map, "authority", authority);
    add(map, "userinfo", userinfo);
    add(map, "host", host);
    add(map, "port", port);
    add(map, "path", path);
    add(map, "query", query);
    add(map, "fragment", fragment);
    add(map, "path-segments", segments.array());
    add(map, "query-segments", queries.array());
    add(map, "filepath", filepath);
    return map.map();
  }

  /**
   * Adds a non-empty map entry.
   * @param map map
   * @param k key
   * @param v value
   * @throws QueryException query exception
   */
  private static void add(final MapBuilder map, final String k, final Object v)
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
  private static String decode(final String string) {
    final int sl = string.length();
    final TokenBuilder tb = new TokenBuilder(sl);
    for(int s = 0; s < sl; s++) {
      int b = string.codePointAt(s);
      if(b == '+') {
        b = ' ';
      } else if(b == '%') {
        b = Token.REPLACEMENT;
        final int b1 = s + 1 < sl ? Token.dec(string.charAt(s + 1)) : -1;
        final int b2 = s + 2 < sl ? Token.dec(string.charAt(s + 2)) : -1;
        if(b1 != -1 && b2 != -1) {
          b = b1 << 4 | b2;
          s += 2;
        } else if(b2 == -1) {
          s += 1;
        }
      }
      if(b == Token.REPLACEMENT) tb.add(Token.REPLACEMENT);
      else tb.addByte((byte) b);
    }
    return tb.toString();
  }
}
