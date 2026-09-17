package org.basex.http.restxq;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import java.io.*;

import org.junit.jupiter.api.*;

/**
 * This class tests the digest authentication of the HTTP Client.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RestXqDigestTest extends RestXqTest {
  /** HTTP client namespace declaration. */
  private static final String HTTP_NS = "xmlns:http='http://expath.org/ns/http-client'";
  /** Challenge parameters. */
  private static final String PARAMS = "realm=\"r\", nonce=\"n\"";
  /** Services that return challenges, validate credentials, and redirect. */
  private static final String SERVICES =
    "declare variable $m:CHALLENGES := {"
    + "  'auth': 'Digest " + PARAMS + ", qop=\"auth,auth-int\", opaque=\"o\"',"
    + "  'legacy': 'Digest " + PARAMS + "',"
    + "  'int': 'Digest " + PARAMS + ", qop=\"auth-int\"',"
    + "  'sha': 'Digest " + PARAMS + ", qop=\"auth\", algorithm=SHA-256',"
    + "  'lines': ('Negotiate', 'Digest " + PARAMS + ", qop=\"auth\", algorithm=SHA-512-256'),"
    + "  'combined': 'Negotiate, Basic realm=\"b\", digest " + PARAMS
    + ", qop=\"auth\", algorithm=MD5-sess',"
    + "  'quoted': 'Digest realm=\"a \\\"b\\\", c\", nonce=\"n\", qop=\"auth\"'"
    + "};"
    + "declare function m:challenge($c) {"
    + "  <R:response><http:response status='401' " + HTTP_NS + ">{"
    + "    for $v in $m:CHALLENGES($c) return <http:header name='WWW-Authenticate' value='{$v}'/>"
    + "  }</http:response></R:response>"
    + "};"
    + "declare %R:path('digest/{$c}') %output:method('text') function m:digest($c) {"
    + "  let $auth := request:header('Authorization')"
    + "  return if(empty($auth)) then m:challenge($c) else ("
    + "    let $f := map:merge("
    + "      for $m in analyze-string($auth,"
    + "        '([\\w-]+)=(?:\"((?:[^\"\\\\]|\\\\.)*)\"|([^,]*))')/fn:match"
    + "      let $q := $m/fn:group[@nr = 2]"
    + "      return map:entry(string($m/fn:group[@nr = 1]),"
    + "        if($q) then replace($q, '\\\\(.)', '$1') else string($m/fn:group[@nr = 3])))"
    + "    let $alg := upper-case($f?algorithm) ! replace(., '-SESS$', '')"
    + "      ! replace(., '-512-', '-512/')"
    + "    let $h := fn($s) { lower-case(string(hash($s, $alg))) }"
    + "    let $ha1 := $h(string-join(($f?username, $f?realm, 'pw'), ':'))"
    + "    let $ha1 := if(ends-with($f?algorithm, '-sess'))"
    + "      then $h(string-join(($ha1, $f?nonce, $f?cnonce), ':')) else $ha1"
    + "    let $ha2 := $h(request:method() || ':' || $f?uri)"
    + "    let $rsp := $h(string-join(($ha1, $f?nonce, $f?nc, $f?cnonce, $f?qop, $ha2), ':'))"
    + "    return string-join(($f?uri, $f?qop otherwise '-', $f?algorithm, $f?opaque otherwise '-',"
    + "      $rsp = $f?response), ' ')"
    + "  )"
    + "};"
    + "declare %R:path('digest-redirect') function m:redirect() {"
    + "  <R:response><http:response status='302' " + HTTP_NS + ">"
    + "    <http:header name='Location' value='" + HTTP_ROOT + "digest/auth?a=1'/>"
    + "  </http:response></R:response>"
    + "};"
    + "declare %R:path('redirect-loop') function m:loop() {"
    + "  <R:response><http:response status='302' " + HTTP_NS + ">"
    + "    <http:header name='Location' value='" + HTTP_ROOT + "redirect-loop'/>"
    + "  </http:response></R:response>"
    + "};"
    + "declare %R:path('redirect-origin') function m:origin() {"
    + "  <R:response><http:response status='302' " + HTTP_NS + ">"
    + "    <http:header name='Location' value='"
    + HTTP_ROOT.replace("localhost", "127.0.0.1") + "digest-type'/>"
    + "  </http:response></R:response>"
    + "};"
    + "declare %R:path('digest-see-other') function m:see-other() {"
    + "  <R:response><http:response status='303' " + HTTP_NS + ">"
    + "    <http:header name='Location' value='" + HTTP_ROOT + "digest-type'/>"
    + "  </http:response></R:response>"
    + "};"
    + "declare %R:path('digest-type') %output:method('text') function m:type() {"
    + "  if(empty(request:header('Authorization'))) then m:challenge('auth') else m:echo()"
    + "};"
    + "declare %R:path('slow') %output:method('text') function m:slow() {"
    + "  prof:sleep(500), 'slow'"
    + "};"
    + "declare %R:path('raw') %R:POST('{$b}') %output:method('text')"
    + " function m:raw($b) {"
    + "  if($b instance of xs:base64Binary) then bin:decode-string($b) else string($b)"
    + "};"
    + "declare %R:path('xml') function m:xml() { <a>{' '}<b/>{' '}</a> };"
    + "declare %R:path('echo') %output:method('text') function m:echo() {"
    + "  string-join((request:method(), request:header('Content-Type', 'none'),"
    + "    request:header('Expect', 'none')), ' ')"
    + "};";

  /**
   * Registers the services.
   * @throws IOException I/O exception
   */
  @BeforeAll public static void services() throws IOException {
    register(SERVICES);
  }

  /** Challenge with several quality-of-protection values and an opaque value. */
  @Test public void auth() {
    query(request("digest/auth") + "[2]", "/digest/auth auth MD5 o true");
  }

  /** Challenge without quality of protection (RFC 2069). */
  @Test public void legacy() {
    query(request("digest/legacy") + "[2]", "/digest/legacy - MD5 - true");
  }

  /** Challenge that only offers the unsupported "auth-int" protection. */
  @Test public void authInt() {
    query(request("digest/int") + "[1]/@status/string()", 401);
  }

  /** SHA-256 challenge. */
  @Test public void sha256() {
    query(request("digest/sha") + "[2]", "/digest/sha auth SHA-256 - true");
  }

  /** Challenges in separate headers. */
  @Test public void lines() {
    query(request("digest/lines") + "[2]", "/digest/lines auth SHA-512-256 - true");
  }

  /** Challenges in a single header, lower-case scheme, session algorithm. */
  @Test public void combined() {
    query(request("digest/combined") + "[2]", "/digest/combined auth MD5-sess - true");
  }

  /** Challenge of a redirected request. */
  @Test public void redirect() {
    query(request("digest-redirect") + "[2]", "/digest/auth?a=1 auth MD5 o true");
  }

  /** Challenge and credentials with escaped quotes. */
  @Test public void quoted() {
    query(_HTTP_SEND_REQUEST.args(" <http:request " + HTTP_NS + " method='get' "
      + "auth-method='Digest' username='a\"b' password='pw'/>", HTTP_ROOT + "digest/quoted")
      + "[2]", "/digest/quoted auth MD5 - true");
  }

  /** Challenge after a redirect that drops the request body. */
  @Test public void seeOther() {
    query(_HTTP_SEND_REQUEST.args(" <http:request " + HTTP_NS + " method='post' "
      + "auth-method='Digest' username='admin' password='pw'>"
      + "<http:body media-type='text/plain'>x</http:body></http:request>",
      HTTP_ROOT + "digest-see-other") + "[2]", "GET none none");
  }

  /** Scheme names of the auth-method attribute are case-insensitive. */
  @Test public void lowerCaseMethod() {
    query(_HTTP_SEND_REQUEST.args(" <http:request " + HTTP_NS + " method='get' "
      + "auth-method='digest' username='admin' password='pw'/>", HTTP_ROOT + "digest/auth")
      + "[2]", "/digest/auth auth MD5 o true");
  }

  /** Credentials supplied as a record to the HTTP Client 2.0 functions. */
  @Test public void getAuth() {
    query(_HTTP_GET.args(HTTP_ROOT + "digest/auth",
      " { 'auth': { 'username': 'admin', 'password': 'pw', 'method': 'digest' } }") + "?body",
      "/digest/auth auth MD5 o true");
  }

  /** The number of redirects can be limited, and zero returns the redirect response. */
  @Test public void maxRedirects() {
    // 1.0: the attribute accepts a maximum
    query(_HTTP_SEND_REQUEST.args(" <http:request " + HTTP_NS + " method='get' "
      + "follow-redirect='0'/>", HTTP_ROOT + "redirect-loop") + "[1]/@status/string()", 302);
    error(_HTTP_SEND_REQUEST.args(" <http:request " + HTTP_NS + " method='get' "
      + "follow-redirect='2'/>", HTTP_ROOT + "redirect-loop"), HC_ERROR_X);
    // 2.0: the option accepts a boolean or a maximum
    query(_HTTP_GET.args(HTTP_ROOT + "redirect-loop", " { 'redirects': 0 }")
      + "?status", 302);
    error(_HTTP_GET.args(HTTP_ROOT + "redirect-loop", " { 'redirects': 2 }"),
      HTTP_REDIRECT_X);
  }

  /** Every method with a dedicated function reaches the server. */
  @Test public void methods() {
    final String url = HTTP_ROOT + "echo";
    final String method = "?body ! substring-before(., ' ')";
    query(_HTTP_DELETE.args(url) + method, "DELETE");
    // OPTIONS is answered by the server itself, so only the status is checked
    query(_HTTP_OPTIONS.args(url) + "?status", 200);
    query(_HTTP_PUT.args(url, " 'x'") + method, "PUT");
    query(_HTTP_PATCH.args(url, " 'x'") + method, "PATCH");
    query(_HTTP_QUERY.args(url, " 'x'") + method, "QUERY");
    query(_HTTP_SEND.args(url, "POST", " 'x'") + method, "POST");
    // a HEAD response has no body
    query(_HTTP_HEAD.args(url) + " ! string-join((?status, empty(?body)), ' ')", "200 true");
  }

  /** Header fields without value are not sent. */
  @Test public void suppressedHeaders() {
    // the media type of the payload does not replace a suppressed field
    query(_HTTP_POST.args(HTTP_ROOT + "echo", " 'x'",
      " { 'headers': { 'Content-Type': () } }") + "?body", "POST none none");
    // a suppressed part header is omitted, and not replaced by the default media type
    query(_HTTP_POST.args(HTTP_ROOT + "raw",
      " { 'headers': { 'Content-Type': () }, 'body': 'a' }", " { 'multipart': true() }")
      + "?body ! contains(lower-case(.), 'content-type')", false);
  }

  /** A body that cannot be serialized is rejected. */
  @Test public void serialize() {
    error(_HTTP_POST.args(HTTP_ROOT + "echo", " { 'a': true#0 }"), HTTP_SERIALIZE_X);
  }

  /** Header fields are checked before a request is sent. */
  @Test public void invalidHeaders() {
    error(_HTTP_GET.args(HTTP_ROOT + "echo", " { 'headers': { 'a b': 'c' } }"),
      HTTP_INVALID_OPTION_X);
  }

  /** XML parser options are applied to response bodies. */
  @Test public void xmlOptions() {
    final String url = HTTP_ROOT + "xml";
    final String count = " ! count(a/text())";
    query(_HTTP_GET.args(url) + "?body" + count, 2);
    query(_HTTP_GET.args(url, " { 'parse-options': { 'xml': { 'strip-space': 'all' } } }")
      + "?body" + count, 0);
    query(_HTTP_SEND_REQUEST.args(" <http:request " + HTTP_NS + " method='get'"
      + " xml='strip-space=all'/>", url) + "[2]" + count, 0);
  }

  /** Method names are checked before a request is sent. */
  @Test public void invalidMethods() {
    final String url = HTTP_ROOT + "echo";
    for(final String method : new String[] { "", "CONNECT", "BAD METHOD" }) {
      error(_HTTP_SEND.args(url, method), HTTP_INVALID_OPTION_X);
    }
  }

  /** A timeout is raised if the server does not respond in time. */
  @Test public void timeout() {
    final String url = HTTP_ROOT + "slow";
    error(_HTTP_GET.args(url, " { 'timeout': 0.1 }"), HTTP_TIMEOUT_X);
    error(_HTTP_SEND_REQUEST.args(" <http:request " + HTTP_NS + " method='get' timeout='0.1'/>",
      url), HC_TIMEOUT);
  }

  /** The type of the body determines the media type of the request. */
  @Test public void postBody() {
    final String url = HTTP_ROOT + "echo";
    query(_HTTP_POST.args(url, " 'x'") + "?body", "POST text/plain; charset=utf-8 none");
    query(_HTTP_POST.args(url, " <x/>") + "?body", "POST application/xml none");
    query(_HTTP_POST.args(url, " { 'a': 1 }") + "?body", "POST application/json none");
    query(_HTTP_POST.args(url, " xs:hexBinary('41')") + "?body",
      "POST application/octet-stream none");
    // a supplied header wins, and a map with a form media type is form-encoded
    query(_HTTP_POST.args(url, " { 'a': 1 }", " { 'headers': "
      + "{ 'Content-Type': 'application/x-www-form-urlencoded' } }") + "?body",
      "POST application/x-www-form-urlencoded none");
    // several items are rejected
    error(_HTTP_POST.args(url, " (1, 2)"), HTTP_INVALID_BODY_X);
  }

  /** Multipart request bodies are sent as parts. */
  @Test public void postMultipart() {
    final String url = HTTP_ROOT + "echo";
    final String parts = " ({ 'body': 'a' }, "
      + "{ 'headers': { 'Content-Type': 'text/csv' }, 'body': 'b' })";
    // the default media type is multipart/form-data, with a generated boundary
    query(_HTTP_POST.args(url, parts, " { 'multipart': true() }")
      + "?body ! substring-before(., ';')", "POST multipart/form-data");
    // a supplied multipart type wins
    query(_HTTP_POST.args(url, parts, " { 'multipart': true(), 'headers': "
      + "{ 'Content-Type': 'multipart/mixed' } }") + "?body ! substring-before(., ';')",
      "POST multipart/mixed");
    // a non-multipart media type and an empty body are rejected
    error(_HTTP_POST.args(url, parts, " { 'multipart': true(), 'headers': "
      + "{ 'Content-Type': 'text/plain' } }"), HTTP_INVALID_OPTION_X);
    error(_HTTP_POST.args(url, " ()", " { 'multipart': true() }"), HTTP_INVALID_BODY_X);
    // parts must have the declared record type
    error(_HTTP_POST.args(url, " { 'x': 1 }", " { 'multipart': true() }"), INVTYPE_X);
  }

  /** Certificates are supplied as key stores. */
  @Test public void getCertificates() {
    final String url = HTTP_ROOT + "echo";
    // a missing path, an unreadable key store and an alias are rejected
    error(_HTTP_GET.args(url, " { 'certificates': { 'trust': {} } }"), HTTP_INVALID_OPTION_X);
    error(_HTTP_GET.args(url, " { 'certificates': { 'trust': { 'keystore': 'x' } } }"),
      HTTP_INVALID_OPTION_X);
    error(_HTTP_GET.args(url, " { 'certificates': { 'client': "
      + "{ 'keystore': 'x', 'alias': 'a' } } }"), HTTP_INVALID_OPTION_X);
  }

  /** Parse options are passed to the parsers of the response body. */
  @Test public void getParseOptions() {
    // the echo service returns text; a CSV separator is applied to the parsed body
    final String url = HTTP_ROOT + "echo";
    query(_HTTP_GET.args(url, " { 'response-body': 'text' }") + "?body", "GET none none");
    query(_HTTP_GET.args(url, " { 'encoding': 'US-ASCII' }") + "?body", "GET none none");
    // XML parse options are applied; external resources are rejected
    query(_HTTP_GET.args(url, " { 'parse-options': { 'xml': { 'strip-space': 'all' } } }")
      + "?status", 200);
    error(_HTTP_GET.args(url, " { 'parse-options': { 'xml': { 'xinclude': true() } } }"),
      HTTP_INVALID_OPTION_X);
    error(_HTTP_GET.args(url, " { 'parse-options': { 'xml': { 'trust-external': true() } } }"),
      HTTP_INVALID_OPTION_X);
    // unknown entries of the record are rejected
    error(_HTTP_GET.args(url, " { 'parse-options': { 'x': {} } }"), INVALIDOPTION_X_X_X_X);
  }

  /** Connection and parser attributes of http:send-request. */
  @Test public void requestAttributes() {
    final String url = HTTP_ROOT + "echo";
    // per-request proxy and certificate check
    query(_HTTP_SEND_REQUEST.args(" <http:request " + HTTP_NS + " method='get' proxy='' "
      + "verify='false'/>", url) + "[1]/@status/string()", 200);
    error(_HTTP_SEND_REQUEST.args(" <http:request " + HTTP_NS + " method='get' proxy='x'/>",
      url), HC_REQ_X);
    // XML parser options, with the same rejection of external resources
    query(_HTTP_SEND_REQUEST.args(" <http:request " + HTTP_NS + " method='get' "
      + "xml='strip-space=all'/>", url) + "[1]/@status/string()", 200);
    error(_HTTP_SEND_REQUEST.args(" <http:request " + HTTP_NS + " method='get' "
      + "xml='xinclude=true'/>", url), HC_REQ_X);
  }

  /** Connection options are applied per request. */
  @Test public void getConnection() {
    final String url = HTTP_ROOT + "echo";
    // an explicit direct connection and a disabled certificate check
    query(_HTTP_GET.args(url, " { 'proxy': '' }") + "?status", 200);
    query(_HTTP_GET.args(url, " { 'verify': false() }") + "?status", 200);
    query(_HTTP_GET.args(url, " { 'proxy': '', 'cookies': true() }") + "?status", 200);
    error(_HTTP_GET.args(url, " { 'proxy': 'x' }"), HTTP_INVALID_OPTION_X);
  }

  /** Errors of the HTTP Client 2.0 functions use the codes of the module namespace. */
  @Test public void getErrors() {
    error(_HTTP_GET.args(HTTP_ROOT + "digest/auth", " { 'timeout': 0 }"),
      HTTP_INVALID_OPTION_X);
    error(_HTTP_GET.args("x:/y"), HTTP_INVALID_URI_X);
  }

  /** A redirect loop is aborted instead of being followed indefinitely. */
  @Test public void redirectLoop() {
    error(_HTTP_SEND_REQUEST.args(" <http:request " + HTTP_NS + " method='get'/>",
      HTTP_ROOT + "redirect-loop"), HC_ERROR_X);
  }

  /** An Authorization header is not sent to a redirect target with another origin. */
  @Test public void redirectOrigin() {
    query(_HTTP_SEND_REQUEST.args(" <http:request " + HTTP_NS + " method='get'>"
      + "<http:header name='Authorization' value='Basic eDp5'/></http:request>",
      HTTP_ROOT + "redirect-origin") + "[1]/@status/string()", 401);
  }

  /** Requests with credentials and body are sent without waiting for a continue response. */
  @Test public void expect() {
    final String request = " <http:request " + HTTP_NS + " method='post' "
      + "auth-method='Digest' username='admin' password='pw'>"
      + "<http:body media-type='text/plain'>x</http:body></http:request>";
    query(_HTTP_SEND_REQUEST.args(request, HTTP_ROOT + "echo") + "[2]", "POST text/plain none");
    query(_HTTP_SEND_REQUEST.args(request, HTTP_ROOT + "digest-type") + "[2]",
      "POST text/plain none");
  }

  /** Request with body. */
  @Test public void body() {
    query(_HTTP_SEND_REQUEST.args(" <http:request " + HTTP_NS + " method='post' "
      + "auth-method='Digest' username='admin' password='pw'>"
      + "<http:body media-type='text/plain'>x</http:body></http:request>",
      HTTP_ROOT + "digest/auth") + "[2]", "/digest/auth auth MD5 o true");
  }

  /** Final URI, protocol version and lower-case header names of the response. */
  @Test public void response() {
    final String response = request("digest-redirect") + "[1]";
    query(response + "/@href/string()", HTTP_ROOT + "digest/auth?a=1");
    query(response + "/@version/string()", "HTTP/1.1");
    query("every $name in " + response + "/http:header/@name satisfies "
      + "$name = lower-case($name)", true);
  }

  /** Credentials in the URI are used for digest authentication. */
  @Test public void uriDigest() {
    final String url = HTTP_ROOT.replace("://", "://admin:pw@") + "digest/auth";
    query(_HTTP_SEND_REQUEST.args(" <http:request " + HTTP_NS + " method='get' "
      + "auth-method='Digest'/>", url) + "[2]", "/digest/auth auth MD5 o true");
  }

  /** Credentials in the URI are not returned. */
  @Test public void uriCredentials() {
    final String url = HTTP_ROOT.replace("://", "://admin:pw@") + "digest-redirect";
    query(_HTTP_SEND_REQUEST.args(" <http:request " + HTTP_NS + " method='get' "
      + "follow-redirect='false'/>", url) + "/@href/string()", HTTP_ROOT + "digest-redirect");
  }

  /**
   * Returns a query that sends a request with digest credentials.
   * @param path path of the addressed service
   * @return query
   */
  private static String request(final String path) {
    return _HTTP_SEND_REQUEST.args(" <http:request " + HTTP_NS + " method='get' "
      + "auth-method='Digest' username='admin' password='pw'/>", HTTP_ROOT + path);
  }
}
