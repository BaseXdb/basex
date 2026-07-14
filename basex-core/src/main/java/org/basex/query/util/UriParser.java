package org.basex.query.util;

import java.util.regex.*;

import org.basex.util.*;

/**
 * A parser for RFC 3986 URIs.
 *
 * @author BaseX Team, BSD License
 * @author Dimitar Popov
 */
public final class UriParser {
  // RFC 2234 rules
  /** <pre>ALPHA =  %x41-5A / %x61-7A ; A-Z / a-z</pre>. */
  private static final String ALPHA = "A-Za-z";
  /** <pre>DIGIT = %x30-39 ; 0-9</pre>. */
  private static final String DIGIT = "0-9";
  /** <pre>HEXDIG =  DIGIT / "A" / "B" / "C" / "D" / "E" / "F"</pre>. */
  private static final String HEXDIG = "[" + DIGIT + "A-Fa-f]";

  // RFC 3986 rules
  /** <pre>sub-delims = "!" / "$" / "&" / "'" / "(" / ")" / "*" / "+" / "," / ";" / "="</pre>. */
  private static final String SUB_DELIMS = "[!$&'()*+,;=]";

  /** <pre>unreserved = ALPHA / DIGIT / "-" / "." / "_" / "~"</pre>. */
  private static final String UNRESERVED = "[" + ALPHA + DIGIT + "._~-]";
  /** <pre>pct-encoded = "%" HEXDIG HEXDIG</pre>. */
  private static final String PCT_ENCODED = "%" + HEXDIG + HEXDIG;
  /** <pre>pchar = unreserved / pct-encoded / sub-delims / ":" / "@"</pre>. */
  private static final String PCHAR = "(" + UNRESERVED + "|" + PCT_ENCODED + "|" +
      SUB_DELIMS + "|:|@)";

  /** <pre>segment = *pchar</pre>. */
  private static final String SEGMENT = PCHAR + "*";
  /** <pre>segment-nz = 1*pchar</pre>. */
  private static final String SEGMENT_NZ = PCHAR + "+";
  /** <pre>segment-nz-nc = 1*( unreserved / pct-encoded / sub-delims / "@" )</pre>. */
  private static final String SEGMENT_NZ_NC = "(" + UNRESERVED + "|" + PCT_ENCODED +
      "|" + SUB_DELIMS + "|@)+";

  /** <pre>path-abempty = *( "/" segment )</pre>. */
  private static final String PATH_ABEMPTY = "(?<pathAbempty>(/" + SEGMENT + ")*)";
  /** <pre>path-absolute = "/" [ segment-nz *( "/" segment ) ]</pre>. */
  private static final String PATH_ABSOLUTE = "(?<pathAbsolute>/(" + SEGMENT_NZ +
      "(/" + SEGMENT + ")*)?)";
  /** <pre>path-noscheme = segment-nz-nc *( "/" segment )</pre>. */
  private static final String PATH_NO_SCHEME = "(?<pathNoScheme>" + SEGMENT_NZ_NC +
      "(/" + SEGMENT + ")*)";
  /** <pre>path-rootless = segment-nz *( "/" segment )</pre>. */
  private static final String PATH_ROOTLESS = "(?<pathRootless>" + SEGMENT_NZ +
      "(/" + SEGMENT + ")*)";

  /** <pre>reg-name = *( unreserved / pct-encoded / sub-delims )</pre>. */
  private static final String REG_NAME = "(" + UNRESERVED + "|" + PCT_ENCODED + "|" +
      SUB_DELIMS + ")*";

  /**
   * <pre>
   * dec-octet = DIGIT                 ; 0-9
   *           / %x31-39 DIGIT         ; 10-99
   *           / "1" 2DIGIT            ; 100-199
   *           / "2" %x30-34 DIGIT     ; 200-249
   *           / "25" %x30-35          ; 250-255
   * </pre>.
   */
  private static final String DEC_OCTET =
      "([0-9]|([1-9][0-9])|(1[0-9]{2})|(2[0-4][0-9])|(25[0-5]))";
  /** <pre>IPv4address = dec-octet "." dec-octet "." dec-octet "." dec-octet</pre>. */
  private static final String IPV4_ADDRESS = DEC_OCTET + "\\." + DEC_OCTET + "\\." +
      DEC_OCTET + "\\." + DEC_OCTET;

  /** <pre>h16 = 1*4HEXDIG</pre>. */
  private static final String H16 = HEXDIG + "{1,4}";
  /** <pre>ls32 = ( h16 ":" h16 ) / IPv4address</pre>. */
  private static final String LS32 = "((" + H16 + ":" + H16 + ")|(" + IPV4_ADDRESS + "))";

  /**
   * <pre>
   * IPv6address =                            6( h16 ":" ) ls32
   *             /                       "::" 5( h16 ":" ) ls32
   *             / [               h16 ] "::" 4( h16 ":" ) ls32
   *             / [ *1( h16 ":" ) h16 ] "::" 3( h16 ":" ) ls32
   *             / [ *2( h16 ":" ) h16 ] "::" 2( h16 ":" ) ls32
   *             / [ *3( h16 ":" ) h16 ] "::"    h16 ":"   ls32
   *             / [ *4( h16 ":" ) h16 ] "::"              ls32
   *             / [ *5( h16 ":" ) h16 ] "::"              h16
   *             / [ *6( h16 ":" ) h16 ] "::"
   * </pre>.
   */
  private static final String IPV6_ADDRESS = "("
              +  "("                               + "(" + H16 + ":){6}" + LS32 + ")"
              + "|("                             + "::(" + H16 + ":){5}" + LS32 + ")"
              + "|(("  +                   H16 + ")?::(" + H16 + ":){4}" + LS32 + ")"
              + "|(((" + H16 + ":){0,1}" + H16 + ")?::(" + H16 + ":){3}" + LS32 + ")"
              + "|(((" + H16 + ":){0,2}" + H16 + ")?::(" + H16 + ":){2}" + LS32 + ")"
              + "|(((" + H16 + ":){0,3}" + H16 + ")?::"  + H16 + ":"     + LS32 + ")"
              + "|(((" + H16 + ":){0,4}" + H16 + ")?::"                  + LS32 + ")"
              + "|(((" + H16 + ":){0,5}" + H16 + ")?::"                  + H16  + ")"
              + "|(((" + H16 + ":){0,6}" + H16 + ")?::"                         + ")"
              + ")";

  /** <pre>IPvFuture = "v" 1*HEXDIG "." 1*( unreserved / sub-delims / ":" )</pre>. */
  private static final String IPV_FUTURE = "v" + HEXDIG + "+\\.(" + UNRESERVED + "|" +
      SUB_DELIMS + "|:)+";
  /** <pre>IP-literal = "[" ( IPv6address / IPvFuture  ) "]"</pre>. */
  private static final String IP_LITERAL = "\\[(" + IPV6_ADDRESS + "|" + IPV_FUTURE + ")\\]";

  /** <pre>host = IP-literal / IPv4address / reg-name</pre>. */
  private static final String HOST = "(?<host>(" + IP_LITERAL + "|" + IPV4_ADDRESS + "|" +
      REG_NAME + "))";
  /** <pre>port = *DIGIT</pre>. */
  private static final String PORT = "(?<port>[" + DIGIT + "]*)";
  /** <pre>userinfo = *( unreserved / pct-encoded / sub-delims / ":" )</pre>. */
  private static final String USERINFO = "(?<userinfo>(" + UNRESERVED + "|" + PCT_ENCODED + "|" +
  SUB_DELIMS + "|:)*)";
  /** <pre>authority = [ userinfo "@" ] host [ ":" port ]</pre>. */
  private static final String AUTHORITY = "(?<authority>(" + USERINFO + "@)?" + HOST +
      "(:" + PORT + ")?)";
  /** <pre>scheme = ALPHA *( ALPHA / DIGIT / "+" / "-" / "." )</pre>. */
  private static final String SCHEME = "(?<scheme>[" + ALPHA + "][" + ALPHA + DIGIT + "+.-]*)";

  /** <pre>query = *( pchar / "/" / "?" )</pre>. */
  private static final String QUERY = "(?<query>(" + PCHAR + "|/|\\?)*)";
  /** <pre>fragment = *( pchar / "/" / "?" )</pre>. */
  private static final String FRAGMENT = "(?<fragment>(" + PCHAR + "|/|\\?)*)";

  /**
   * <pre>
   * URI-reference = URI / relative-ref
   * URI           = scheme ":" hier-part [ "?" query ] [ "#" fragment ]
   * relative-ref  =        relative-part [ "?" query ] [ "#" fragment ]
   * hier-part     = "//" authority path-abempty
   *               / path-absolute
   *               / path-rootless
   *               / path-empty
   * relative-part = "//" authority path-abempty
   *               / path-absolute
   *               / path-noscheme
   *               / path-empty
   * </pre>
   *
   * Equivalent to:
   * <pre>
   * URI-Reference = ( scheme ":" hier-part / relative-part )
   *                 [ "?" query ] [ "#" fragment ]
   *               = [ scheme ":" ]
   *                 ( (?<=:) hier-part / (?<!:) relative-part )
   *                 [ "?" query ] [ "#" fragment ]
   *               = [ scheme ":" ]
   *                 [ "//" authority path-abempty / path-absolute / ( (?<=:) path-rootless /
   *                   (?<!:) path-noscheme ) ]
   *                 [ "?" query ] [ "#" fragment ]
   * </pre>
   */
  private static final Pattern URI_REF = Pattern.compile(
          "^(" +
            "(" + SCHEME + ":)?" +
            "(" +
              "(//" + AUTHORITY + PATH_ABEMPTY + ")" +
              "|" +
              PATH_ABSOLUTE +
              "|" +
              "(" +
                "((?<=:)" + PATH_ROOTLESS + ")" +
                "|" +
                "((?<!:)" + PATH_NO_SCHEME + ")" +
              ")" +
            ")?" +
            "(\\?" + QUERY + ")?" +
            "(#" + FRAGMENT + ")?" +
          ")$");

  /** Scheme of an absolute URI. */
  private static final Pattern SCHEME_ONLY = Pattern.compile('^' + SCHEME + '$');

  /** Private constructor. */
  private UriParser() { }

  /**
   * Resolves a URI reference against a base URI (RFC 3986, 5.2). Characters that are invalid in
   * URIs are treated like unreserved characters, and no percent-encoding takes place (LEIRI).
   * @param base base URI
   * @param reference URI reference to be resolved
   * @return resolved URI
   */
  public static String resolve(final String base, final String reference) {
    final Parts bs = new Parts(base), rf = new Parts(reference);
    final Parts uri = new Parts();
    uri.fragment = rf.fragment;
    if(rf.scheme != null) {
      uri.scheme = rf.scheme;
      uri.authority = rf.authority;
      uri.path = removeDots(rf.path);
      uri.query = rf.query;
    } else {
      uri.scheme = bs.scheme;
      if(rf.authority != null) {
        uri.authority = rf.authority;
        uri.path = removeDots(rf.path);
        uri.query = rf.query;
      } else {
        uri.authority = bs.authority;
        if(rf.path.isEmpty()) {
          uri.path = bs.path;
          uri.query = rf.query != null ? rf.query : bs.query;
        } else {
          uri.path = removeDots(Strings.startsWith(rf.path, '/') ? rf.path : bs.merge(rf.path));
          uri.query = rf.query;
        }
      }
    }
    return uri.toString();
  }

  /**
   * Removes dot segments from a path (RFC 3986, 5.2.4).
   * @param path path
   * @return normalized path
   */
  private static String removeDots(final String path) {
    final boolean absolute = Strings.startsWith(path, '/');
    final StringBuilder sb = new StringBuilder();
    String in = absolute ? path : '/' + path;
    // parent references of a relative path that can be resolved against a subsequent base URI
    int dots = 0;
    while(!in.isEmpty()) {
      if(in.startsWith("/./")) {
        in = in.substring(2);
      } else if(in.equals("/.")) {
        in = "/";
      } else if(in.startsWith("/../") || in.equals("/..")) {
        in = in.equals("/..") ? "/" : in.substring(3);
        if(sb.isEmpty()) ++dots;
        else sb.setLength(sb.lastIndexOf("/"));
      } else {
        int s = in.indexOf('/', 1);
        if(s == -1) s = in.length();
        sb.append(in, 0, s);
        in = in.substring(s);
      }
    }
    if(absolute) return sb.toString();
    final String result = sb.isEmpty() ? "" : sb.substring(1);
    return dots == 0 ? result : "../".repeat(dots) + result;
  }

  /**
   * Parses an RFC 3986 URI.
   * @param uri the URI to parse
   * @return parsed URI
   */
  public static ParsedUri parse(final String uri) {
    final Matcher matcher = URI_REF.matcher(uri);
    try {
      if(uri.length() <= 2048 && matcher.matches()) {
        final ParsedUri pu = new ParsedUri();
        pu.absolute = matcher.group("scheme") != null;
        pu.valid = true;
        return pu;
      }
    } catch(final StackOverflowError er) {
      Util.debug(er);
    }
    return ParsedUri.INVALID;
  }

  /**
   * Components of a URI reference (RFC 3986, 3).
   * @author BaseX Team, BSD License
   * @author Christian Gruen
   */
  private static final class Parts {
    /** Scheme ({@code null} if the URI is relative). */
    private String scheme;
    /** Authority (can be {@code null}). */
    private String authority;
    /** Path (never {@code null}). */
    private String path = "";
    /** Query (can be {@code null}). */
    private String query;
    /** Fragment (can be {@code null}). */
    private String fragment;

    /** Constructor. */
    private Parts() { }

    /**
     * Splits a URI reference into its components.
     * @param uri URI reference
     */
    private Parts(final String uri) {
      String rest = uri;
      int s = rest.indexOf('#');
      if(s != -1) {
        fragment = rest.substring(s + 1);
        rest = rest.substring(0, s);
      }
      s = rest.indexOf('?');
      if(s != -1) {
        query = rest.substring(s + 1);
        rest = rest.substring(0, s);
      }
      // a colon introduces a scheme only if it occurs in the first path segment
      s = rest.indexOf(':');
      final int slash = rest.indexOf('/');
      if(s != -1 && (slash == -1 || s < slash) &&
          SCHEME_ONLY.matcher(rest.substring(0, s)).matches()) {
        scheme = rest.substring(0, s);
        rest = rest.substring(s + 1);
      }
      if(rest.startsWith("//")) {
        s = rest.indexOf('/', 2);
        if(s == -1) s = rest.length();
        authority = rest.substring(2, s);
        rest = rest.substring(s);
      }
      path = rest;
    }

    /**
     * Merges a relative path with the path of this URI (RFC 3986, 5.2.3).
     * @param relative relative path
     * @return merged path
     */
    private String merge(final String relative) {
      if(authority != null && path.isEmpty()) return "/" + relative;
      final int s = path.lastIndexOf('/');
      return s == -1 ? relative : path.substring(0, s + 1) + relative;
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder();
      if(scheme != null) sb.append(scheme).append(':');
      if(authority != null) sb.append("//").append(authority);
      sb.append(path);
      if(query != null) sb.append('?').append(query);
      if(fragment != null) sb.append('#').append(fragment);
      return sb.toString();
    }
  }

  /**
   * URI data.
   * @author BaseX Team, BSD License
   * @author Dimitar Popov
   */
  public static final class ParsedUri {
    /** Invalid URI. */
    private static final ParsedUri INVALID = new ParsedUri();

    /** Absolute flag. */
    private boolean absolute;
    /** Valid flag. */
    private boolean valid;

    /**
     * Indicates if the URI is valid.
     * @return valid flag
     */
    public boolean valid() {
      return valid;
    }

    /**
     * Indicates if the URI is absolute.
     * @return absolute flag
     */
    public boolean absolute() {
      return absolute;
    }
  }
}
