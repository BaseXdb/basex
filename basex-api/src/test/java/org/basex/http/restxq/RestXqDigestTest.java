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
