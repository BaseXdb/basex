package org.basex.query.value.item;

import static org.basex.query.QueryError.URIARG_X;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.basex.query.QueryException;
import org.basex.query.value.type.AtomType;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * URI item ({@code xs:anyURI}).
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class Uri extends AStr {
  /** Empty URI. */
  public static final Uri EMPTY = new Uri(Token.EMPTY);
  /** String data. */
  private final byte[] value;
  private final UriParser.ParsedUri parsed;

  /**
   * Constructor.
   * @param value value
   */
  private Uri(final byte[] value) {
    super(AtomType.URI);
    this.value = value;
    parsed = UriParser.parse(Token.string(Token.uri(value, true)));
  }

  /**
   * Creates a new uri instance.
   * @param value value
   * @return uri instance
   */
  public static Uri uri(final byte[] value) {
    return uri(value, true);
  }

  /**
   * Creates a new uri instance.
   * @param value string value
   * @return uri instance
   */
  public static Uri uri(final String value) {
    return uri(Token.token(value), true);
  }

  /**
   * Creates a new uri instance.
   * @param value value
   * @param normalize chop leading and trailing whitespaces
   * @return uri instance
   */
  public static Uri uri(final byte[] value, final boolean normalize) {
    final byte[] u = normalize ? Token.normalize(value) : value;
    return u.length == 0 ? EMPTY : new Uri(u);
  }

  /**
   * Checks the URIs for equality.
   * @param uri to be compared
   * @return result of check
   */
  public boolean eq(final Uri uri) {
    return Token.eq(string(), uri.string());
  }

  /**
   * Appends the specified address. If one of the URIs is invalid,
   * the original uri is returned.
   * @param add address to be appended
   * @param info input info
   * @return new uri
   * @throws QueryException query exception
   */
  public Uri resolve(final Uri add, final InputInfo info) throws QueryException {
    if(add.value.length == 0) return this;
    try {
      final URI base = new URI(Token.string(value));
      final URI res = new URI(Token.string(add.value));
      final URI uri = base.resolve(res);
      return uri(Token.token(uri.toString()), false);
    } catch(final URISyntaxException ex) {
      throw URIARG_X.get(info, ex.getMessage());
    }
  }

  /**
   * Tests if this is an absolute URI.
   * @return result of check
   */
  public boolean isAbsolute() {
    return parsed.valid && parsed.scheme != null;
  }

  /**
   * Checks the validity of this URI.
   * @return result of check
   */
  public boolean isValid() {
    return parsed.valid;
  }

  @Override
  public byte[] string(final InputInfo ii) {
    return value;
  }

  /**
   * Returns the string value.
   * @return string value
   */
  public byte[] string() {
    return value;
  }

  @Override
  public URI toJava() throws QueryException {
    try {
      return new URI(Token.string(value));
    } catch(final URISyntaxException ex) {
      throw new QueryException(ex);
    }
  }
}


/**
 * A parser for RFC 3986 URIs.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Dimitar Popov
 */
class UriParser {
  // RFC 2234 rules
  /** <pre>ALPHA =  %x41-5A / %x61-7A ; A-Z / a-z</pre> */
  private static final String ALPHA = "A-Za-z";
  /** <pre>DIGIT = %x30-39 ; 0-9</pre> */
  private static final String DIGIT = "0-9";
  /** <pre>HEXDIG =  DIGIT / "A" / "B" / "C" / "D" / "E" / "F"</pre> */
  private static final String HEXDIG = "[" + DIGIT + "A-Fa-f]";

  // RFC 3986 rules
  /** <pre>sub-delims = "!" / "$" / "&" / "'" / "(" / ")" / "*" / "+" / "," / ";" / "="</pre> */
  private static final String subDelims = "[!$&'()*+,;=]";

  /** <pre>unreserved = ALPHA / DIGIT / "-" / "." / "_" / "~"</pre> */
  private static final String unreserved = "[" + ALPHA + DIGIT + "._~-]";
  /** <pre>pct-encoded = "%" HEXDIG HEXDIG</pre> */
  private static final String pctEncoded = "%" + HEXDIG + HEXDIG;
  /** <pre>pchar = unreserved / pct-encoded / sub-delims / ":" / "@"</pre> */
  private static final String  pchar = "(" + unreserved + "|" + pctEncoded + "|" + subDelims + "|:|@)";

  /** <pre>segment = *pchar</pre> */
  private static final String segment = pchar + "*";
  /** <pre>segment-nz = 1*pchar</pre> */
  private static final String segmentNz = pchar + "+";
  /** <pre>segment-nz-nc = 1*( unreserved / pct-encoded / sub-delims / "@" )</pre> */
  private static final String segmentNzNc = "(" + unreserved + "|" + pctEncoded + "|" + subDelims + "|@)+";

  /** <pre>path-abempty = *( "/" segment )</pre> */
  private static final String pathAbempty = "(?<pathAbempty>(/" + segment + ")*)";
  /** <pre>path-absolute = "/" [ segment-nz *( "/" segment ) ]</pre> */
  private static final String pathAbsolute = "(?<pathAbsolute>/(" + segmentNz + "(/" + segment + ")*)?)";
  /** <pre>path-noscheme = segment-nz-nc *( "/" segment )</pre> */
  private static final String pathNoScheme = "(?<pathNoScheme>" + segmentNzNc + "(/" + segment + ")*)";
  /** <pre>path-rootless = segment-nz *( "/" segment )</pre> */
  private static final String pathRootless = "(?<pathRootless>" + segmentNz + "(/" + segment + ")*)";


  /** <pre>reg-name = *( unreserved / pct-encoded / sub-delims )</pre> */
  private static final String regName = "(" + unreserved + "|" + pctEncoded + "|" + subDelims + ")*";

  /**
   * <pre>
   * dec-octet = DIGIT                 ; 0-9
   *           / %x31-39 DIGIT         ; 10-99
   *           / "1" 2DIGIT            ; 100-199
   *           / "2" %x30-34 DIGIT     ; 200-249
   *           / "25" %x30-35          ; 250-255
   * </pre>
   */
  private static final String decOctet = "([0-9]|([1-9][0-9])|(1[0-9]{2})|(2[0-4][0-9])|(25[0-5]))";
  /** <pre>IPv4address = dec-octet "." dec-octet "." dec-octet "." dec-octet</pre> */
  private static final String ipv4Address = decOctet + "\\." + decOctet + "\\." + decOctet + "\\." + decOctet;

  /** <pre>h16 = 1*4HEXDIG</pre> */
  private static final String h16 = HEXDIG + "{1,4}";
  /** <pre>ls32 = ( h16 ":" h16 ) / IPv4address</pre> */
  private static final String ls32 = "((" + h16 + ":" + h16 + ")|(" + ipv4Address + "))";

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
   * </pre>
   */
  private static final String ipv6Address = "("
              +  "("                               + "(" + h16 + ":){6}" + ls32 + ")"
              + "|("                             + "::(" + h16 + ":){5}" + ls32 + ")"
              + "|(("  +                   h16 + ")?::(" + h16 + ":){4}" + ls32 + ")"
              + "|(((" + h16 + ":){0,1}" + h16 + ")?::(" + h16 + ":){3}" + ls32 + ")"
              + "|(((" + h16 + ":){0,2}" + h16 + ")?::(" + h16 + ":){2}" + ls32 + ")"
              + "|(((" + h16 + ":){0,3}" + h16 + ")?::"  + h16 + ":"     + ls32 + ")"
              + "|(((" + h16 + ":){0,4}" + h16 + ")?::"                  + ls32 + ")"
              + "|(((" + h16 + ":){0,5}" + h16 + ")?::"                  + h16  + ")"
              + "|(((" + h16 + ":){0,6}" + h16 + ")?::"                         + ")"
              + ")";

  /** <pre>IPvFuture = "v" 1*HEXDIG "." 1*( unreserved / sub-delims / ":" )</pre> */
  private static final String ipvFuture = "v" + HEXDIG + "+\\.(" + unreserved + "|" + subDelims + "|:)+";
  /** <pre>IP-literal = "[" ( IPv6address / IPvFuture  ) "]"</pre> */
  private static final String ipLiteral = "\\[(" + ipv6Address + "|" + ipvFuture + ")\\]";

  /** <pre>host = IP-literal / IPv4address / reg-name</pre> */
  private static final String host = "(?<host>(" + ipLiteral + "|" + ipv4Address + "|" + regName + "))";
  /** <pre>port = *DIGIT</pre> */
  private static final String port = "(?<port>[" + DIGIT + "]*)";
  /** <pre>userinfo = *( unreserved / pct-encoded / sub-delims / ":" )</pre> */
  private static final String userinfo = "(?<userinfo>(" + unreserved + "|" + pctEncoded + "|" + subDelims + "|:)*)";
  /** <pre>authority = [ userinfo "@" ] host [ ":" port ]</pre> */
  private static final String authority = "(?<authority>(" + userinfo + "@)?" + host + "(:" + port + ")?)";
  /** <pre>scheme = ALPHA *( ALPHA / DIGIT / "+" / "-" / "." )</pre> */
  private static final String scheme = "(?<scheme>[" + ALPHA + "][" + ALPHA + DIGIT + "+.-]*)";

  /** <pre>query = *( pchar / "/" / "?" )</pre> */
  private static final String query = "(?<query>(" + pchar + "|/|\\?)*)";
  /** <pre>fragment = *( pchar / "/" / "?" )</pre> */
  private static final String fragment = "(?<fragment>(" + pchar + "|/|\\?)*)";

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
   *                 [ "//" authority path-abempty / path-absolute / ( (?<=:) path-rootless / (?<!:) path-noscheme ) ]
   *                 [ "?" query ] [ "#" fragment ]
   * </pre>
   */
  private static final Pattern uriRef = Pattern.compile(
          "^(" +
            "(" + scheme + ":)?" +
            "(" +
              "(//" + authority + pathAbempty + ")" +
              "|" +
              pathAbsolute +
              "|" +
              "(" +
                "((?<=:)" + pathRootless + ")" +
                "|" +
                "((?<!:)" + pathNoScheme + ")" +
              ")" +
            ")?" +
            "(\\?" + query + ")?" +
            "(#" + fragment + ")?" +
          ")$");

  /**
   * Construct a new RFC 3986 URI parser.
   */
  private UriParser() {}

  /**
   * Parse an RFC 3986 URI.
   * @param uri the uri to parse
   * @return parsed URI
   */
  public static ParsedUri parse(final String uri) {
    final Matcher matcher = uriRef.matcher(uri);
    if (!matcher.matches()) return ParsedUri.invalid;

    final String scheme = matcher.group("scheme");
    final String authority = matcher.group("authority");
    final String userInfo = matcher.group("userinfo");
    final String host = matcher.group("host");
    final String port = matcher.group("port");
    final String query = matcher.group("query");
    final String fragment = matcher.group("fragment");
    String path = matcher.group("pathAbempty");
    if (path == null) {
      path = matcher.group("pathAbsolute");
      if (path == null) {
        path = matcher.group("pathRootless");
        if (path == null) {
          path = matcher.group("pathNoScheme");
        }
      }
    }

    return new ParsedUri.Builder()
      .valid(true)
      .scheme(scheme)
      .authority(authority)
      .userInfo(userInfo)
      .host(host)
      .port(port == null ? -1 : Integer.parseInt(port))
      .path(path)
      .query(query)
      .fragment(fragment)
      .build();
  }

  public static final class ParsedUri {
    static final ParsedUri invalid = new ParsedUri.Builder().build();

    public final String scheme;
    public final String authority;
    public final String userInfo;
    public final String host;
    public final int port;
    public final String path;
    public final String query;
    public final String fragment;
    public final boolean valid;

    private ParsedUri(boolean valid, String scheme, String authority, String userInfo, String host, int port, String path, String query, String fragment) {
      this.valid = valid;
      this.scheme = scheme;
      this.authority = authority;
      this.userInfo = userInfo;
      this.host = host;
      this.port = port;
      this.path = path;
      this.query = query;
      this.fragment = fragment;
    }

    @Override
    public String toString() {
      return "ParsedUri{" +
        "valid='" + valid + "'" +
        ", scheme='" + scheme + "'" +
        ", authority='" + authority + "'" +
        ", userInfo='" + userInfo + "'" +
        ", host='" + host + "'" +
        ", port=" + port +
        ", path='" + path + "'" +
        ", query='" + query + "'" +
        ", fragment='" + fragment + "'" +
        '}';
    }

    private static final class Builder {
      private boolean valid;
      private String scheme;
      private String authority;
      private String userInfo;
      private String host;
      private int port = -1;
      private String path;
      private String query;
      private String fragment;

      Builder() {}

      public Builder valid(boolean valid) {
        this.valid = valid;
        return this;
      }

      public Builder scheme(String scheme) {
        this.scheme = scheme;
        return this;
      }

      public Builder authority(String authority) {
        this.authority = authority;
        return this;
      }

      public Builder userInfo(String userInfo) {
        this.userInfo = userInfo;
        return this;
      }

      public Builder host(String host) {
        this.host = host;
        return this;
      }

      public Builder port(int port) {
        this.port = port;
        return this;
      }

      public Builder path(String path) {
        this.path = path;
        return this;
      }

      public Builder query(String query) {
        this.query = query;
        return this;
      }

      public Builder fragment(String fragment) {
        this.fragment = fragment;
        return this;
      }

      public Builder but() {
        return new Builder()
          .authority(authority)
          .scheme(scheme)
          .host(host)
          .port(port)
          .path(path)
          .query(query)
          .fragment(fragment);
      }

      public ParsedUri build() {
        return new ParsedUri(
          valid,
          scheme,
          authority,
          userInfo,
          host,
          port,
          path,
          query,
          fragment);
      }
    }
  }
}
