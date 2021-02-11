package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.util.http.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Web Module.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class WebModuleTest extends SandboxTest {
  /** Test method. */
  @Test public void contentType() {
    final Function func = _WEB_CONTENT_TYPE;
    query(func.args("sample.mp3"), new MediaType("audio/mpeg").toString());
    query(func.args("a/b/input.xml"), MediaType.APPLICATION_XML.toString());
    query(func.args("a.xxxx"), MediaType.APPLICATION_OCTET_STREAM.toString());
  }

  /** Test method. */
  @Test public void createUrl() {
    final Function func = _WEB_CREATE_URL;
    query(func.args("http://x.com", " map {}"), "http://x.com");
    query(func.args("url", " map { 'a': 'b' }"), "url?a=b");
    query(func.args("url", " map { 'a': ('b','c') }"), "url?a=b&a=c");
    query(func.args("url", " map { 12: true() }"), "url?12=true");

    query(func.args("url", " map { }", "a"), "url#a");

    error(func.args("url", " map { (): 'a' }"), EMPTYFOUND);
    error(func.args("url", " map { ('a','b'): () }"), SEQFOUND_X);
    error(func.args("url", " map { 'a': true#0 }"), FIATOM_X);
  }

  /** Test method. */
  @Test public void decodeUrl() {
    final Function func = _WEB_DECODE_URL;
    query(func.args("a+-._*"), "a -._*");
    query("let $s := codepoints-to-string((9, 10, 13, 32 to 55295, 57344 to 65533, 65536)) " +
        "return $s = web:decode-url(web:encode-url($s))", true);

    error(func.args("%1"), WEB_INVALID2_X);
    error(func.args("%1F"), WEB_INVALID1_X);
    error(func.args("%D8%00"), WEB_INVALID1_X);
  }

  /** Test method. */
  @Test public void encodeUrl() {
    final Function func = _WEB_ENCODE_URL;
    query(func.args("a&#xd; *-._"), "a%0D+*-._");
  }

  /** Test method. */
  @Test public void error() {
    final Function func = _WEB_ERROR;
    query("try { " + func.args(400, "x") + " } catch rest:error { 'x' }", "x");
    error(func.args(-1, "x"), WEB_STATUS_X);
  }

  /** Test method. */
  @Test public void forward() {
    final Function func = _WEB_FORWARD;
    query(func.args("a/b") + "/text() = 'a/b'", true);
    query(func.args("a/b", " map { 'c': 'd' }") + "/text() = 'a/b?c=d'", true);
  }

  /** Test method. */
  @Test public void redirect() {
    final Function func = _WEB_REDIRECT;
    query(func.args("a/b") + "/*:response/*:header/@value = 'a/b'", true);
    query(func.args("a/b") + "/*:response/*:header/@name = 'Location'", true);
    query(func.args("a/b") + "/*:response/@status = 302", true);
    query(func.args("a/b", " map { }", "a") + "/*:response/*:header/@value = 'a/b#a'", true);

    query(func.args("a/b", " map { 'a':'b' }") +
        "/*:response/*:header[@name = 'Location']/@value/string()", "a/b?a=b");

    // GH-1585
    query("count((" + func.args("a") + " update {})/http:response)", 1);
  }

  /** Test method. */
  @Test public void responseHeader() {
    final Function func = _WEB_RESPONSE_HEADER;
    query(func.args() + "/http:response/http:header", "");
    query(func.args() + "/output:serialization-parameters/output:media-type", "");

    // overwrite header
    query(func.args(" map { 'media-type': 'X' }") +
        "/output:serialization-parameters/output:media-type/@value/string()", "X");
    // header is not generated if value is empty
    query("count(" + func.args(" map { 'media-type': '' }") +
        "/output:serialization-parameters/*)", 0);

    // overwrite header
    query(func.args(" map {}", " map { 'Cache-Control': 'X' }") +
        "/http:response/http:header[@name = 'Cache-Control']/@value/string()", "X");
    // header is not generated if value is empty
    query("count(" + func.args(" map {}", " map { 'Cache-Control': '' }") +
        "/http:response/*)", 0);

    // status/message arguments
    query(func.args(" map {}", " map {}", " map { 'status': 200, 'message': 'OK' }") +
        "/http:response ! (@status, @message) ! string()", "200\nOK");

    // GH-1585
    query("count((" + func.args() + " update {})/http:response)", 1);
    query("count((" + func.args() + " update {})/output:*)", 1);
  }
}
