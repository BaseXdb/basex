package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.util.http.*;
import org.junit.*;

/**
 * This class tests the functions of the Web Module.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class WebModuleTest extends AdvancedQueryTest {
  /** Test method. */
  @Test
  public void contentType() {
    query(_WEB_CONTENT_TYPE.args("sample.mp3"), new MediaType("audio/mpeg").toString());
    query(_WEB_CONTENT_TYPE.args("a/b/input.xml"), MediaType.APPLICATION_XML.toString());
    query(_WEB_CONTENT_TYPE.args("a.xxxx"), MediaType.APPLICATION_OCTET_STREAM.toString());
  }

  /** Test method. */
  @Test
  public void createUrl() {
    query(_WEB_CREATE_URL.args("http://x.com", " map {}"), "http://x.com");
    query(_WEB_CREATE_URL.args("url", " map { 'a':'b' }"), "url?a=b");
    query(_WEB_CREATE_URL.args("url", " map { 'a':('b','c') }"), "url?a=b&a=c");
    query(_WEB_CREATE_URL.args("url", " map { 12:true() }"), "url?12=true");

    error(_WEB_CREATE_URL.args("url", " map { ():'a' }"), SEQFOUND_X);
    error(_WEB_CREATE_URL.args("url", " map { ('a','b'):() }"), SEQFOUND_X);
    error(_WEB_CREATE_URL.args("url", " map { 'a':true#0 }"), FIATOM_X);
  }

  /** Test method. */
  @Test
  public void redirect() {
    query(_WEB_REDIRECT.args("a/b") + "/*:response/*:header/@value = 'a/b'", true);
    query(_WEB_REDIRECT.args("a/b") + "/*:response/*:header/@name = 'location'", true);
    query(_WEB_REDIRECT.args("a/b") + "/*:response/@status = 302", true);

    query(_WEB_REDIRECT.args("a/b", " map { 'a':'b' }") +
        "/*:response/*:header[@name = 'location']/@value/string()", "a/b?a=b");
  }

  /** Test method. */
  @Test
  public void responseHeader() {
    query(_WEB_RESPONSE_HEADER.args() + "/http:response/http:header", "");
    query(_WEB_RESPONSE_HEADER.args() + "/output:serialization-parameters/output:media-type", "");

    // overwrite header
    query(_WEB_RESPONSE_HEADER.args(" map { 'media-type': 'X' }") +
        "/output:serialization-parameters/output:media-type/@value/string()", "X");
    // header is not generated if value is empty
    query("count(" + _WEB_RESPONSE_HEADER.args(" map { 'media-type': '' }") +
        "/output:serialization-parameters/*)", 0);

    // overwrite header
    query(_WEB_RESPONSE_HEADER.args(" map {}", " map { 'Cache-Control': 'X' }") +
        "/http:response/http:header[@name = 'Cache-Control']/@value/string()", "X");
    // header is not generated if value is empty
    query("count(" + _WEB_RESPONSE_HEADER.args(" map {}", " map { 'Cache-Control': '' }") +
        "/http:response/*)", 0);

    // status/message arguments
    query(_WEB_RESPONSE_HEADER.args(" map {}", " map {}", " map {'status':200,'message':'OK'}") +
        " ! (@status, @message) ! string()", "200\nOK");
  }

  /** Test method. */
  @Test
  public void encodeUrl() {
    query(_WEB_ENCODE_URL.args("a&#xd; *-._"), "a%0D+*-._");
  }

  /** Test method. */
  @Test
  public void decodeUrl() {
    query(_WEB_DECODE_URL.args("a+-._*"), "a -._*");
    query("let $s := codepoints-to-string((9, 10, 13, 32 to 55295, 57344 to 65533, 65536)) " +
        "return $s = web:decode-url(web:encode-url($s))", true);

    error(_WEB_DECODE_URL.args("%1"), WEB_INVALID2_X);
    error(_WEB_DECODE_URL.args("%1F"), WEB_INVALID1_X);
    error(_WEB_DECODE_URL.args("%D8%00"), WEB_INVALID1_X);
  }
}
