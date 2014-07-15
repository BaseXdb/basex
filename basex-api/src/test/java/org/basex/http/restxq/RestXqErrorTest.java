package org.basex.http.restxq;

import org.junit.*;

/**
 * This test contains RESTXQ errors.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class RestXqErrorTest extends RestXqTest {
  /**
   * Error annotation.
   * @throws Exception exception
   */
  @Test
  public void error() throws Exception {
    // catch errors
    get("declare %R:path('') function m:a() { error() };" +
        "declare %R:error('*') function m:b() { 'F' };", "", "F");
    get("declare %R:path('') function m:a() { error(xs:QName('x')) };" +
        "declare %R:error('x') function m:b() { 'F' };", "", "F");
    get("declare %R:path('') function m:a() { 1 + <a/> };" +
        "declare %R:error('*:FORG0001') function m:b() { 'F' };", "", "F");
    get("declare %R:path('') function m:a() { 1 + <a/> };" +
        "declare %R:error('err:*') function m:b() { 'F' };", "", "F");
    get("declare %R:path('') function m:a() { 1 + <a/> };" +
        "declare %R:error('err:FORG0001') function m:b() { 'F' };", "", "F");
    get("declare %R:path('') function m:a() { 1 + <a/> };" +
        "declare %R:error('err:FORG0001') function m:b() { 'F' };", "", "F");
    get("declare %R:path('') function m:a() { 1 + <a/> };" +
        "declare %R:error('Q{http://www.w3.org/2005/xqt-errors}FORG0001')"
        + "function m:b() { 'F' };", "", "F");
    get("declare %R:path('') function m:a() { 1 + <a/> };" +
        "declare %R:error('Q{http://www.w3.org/2005/xqt-errors}*')"
        + "function m:b() { 'F' };", "", "F");

    // competing error annotations
    get("declare %R:path('') function m:a() { 1 + <a/> };" +
        "declare %R:error('err:FORG0001') function m:b() { '1' };" +
        "declare %R:error('err:*') function m:d() { '2' };" +
        "declare %R:error('*:FORG0001') function m:c() { '3' };" +
        "declare %R:error('*') function m:e() { '4' };", "", "1");
    get("declare %R:path('') function m:a() { 1 + <a/> };" +
        "declare %R:error('err:*') function m:d() { '2' };" +
        "declare %R:error('*:FORG0001') function m:c() { '3' };" +
        "declare %R:error('*') function m:e() { '4' };", "", "2");
    get("declare %R:path('') function m:a() { 1 + <a/> };" +
        "declare %R:error('err:*') function m:d() { '3' };" +
        "declare %R:error('*') function m:e() { '4' };", "", "3");

    // duplicate error annotations
    get("declare %R:path('') function m:a() { 1 + <a/> };" +
        "declare %R:error('*') function m:b() { 'F' };", "", "F");
    get("declare %R:path('') function m:a() { 1 + <a/> };" +
        "declare %R:error('err:FORG0001', 'err:FORG0002') function m:b() { 'F' };", "", "F");
    get("declare %R:path('') function m:a() { 1 + <a/> };" +
        "declare %R:error('*:FORG0001', '*:FORG0002') function m:b() { 'F' };", "", "F");
    get("declare %R:path('') function m:a() { 1 + <a/> };" +
        "declare %R:error('err:*', 'unit:*') function m:b() { 'F' };", "", "F");
    get("declare %R:path('') function m:a() { 1 + <a/> };" +
        "declare %R:error('unit:*') %R:error('err:*') function m:b() { 'F' };", "", "F");
}

  /**
   * Errors with error annotation.
   * @throws Exception exception
   */
  @Test
  public void errorErrors() throws Exception {
    // error (no appropriate error annotation)
    getE("declare %R:path('') function m:a() { error(xs:QName('x')) };" +
         "declare %R:error('y') function m:b() { 'F' };", "");
    // error (invalid name test)
    getE("declare %R:path('') function m:a() { () };" +
         "declare %R:error('unknown:*') function m:b() { 'F' };", "");
    getE("declare %R:path('') function m:a() { () };" +
         "declare %R:error('*:In Valid') function m:b() { 'F' };", "");
    getE("declare %R:path('') function m:a() { () };" +
         "declare %R:error('In Valid') function m:b() { 'F' };", "");
    getE("declare %R:path('') function m:a() { () };" +
         "declare %R:error('Q{http://www.w3.org/2005/xqt-errors}') function m:b() { 'F' };", "");
    getE("declare %R:path('') function m:a() { () };" +
         "declare %R:error('Q{{}}x') function m:b() { 'F' };", "");
    // duplicate error annotations
    getE("declare %R:path('') function m:a() { () };" +
         "declare %R:error('*') function m:b() { 'F' };" +
         "declare %R:error('*') function m:b() { 'F' };", "");
    getE("declare %R:path('') function m:a() { () };" +
         "declare %R:error('*:FORG0001') function m:b() { 'F' };" +
         "declare %R:error('*:FORG0001') function m:b() { 'F' };", "");
    getE("declare %R:path('') function m:a() { () };" +
         "declare %R:error('err:*') function m:b() { 'F' };" +
         "declare %R:error('err:*') function m:b() { 'F' };", "");
    getE("declare %R:path('') function m:a() { () };" +
         "declare %R:error('error') function m:b() { 'F' };" +
         "declare %R:error('error') function m:b() { 'F' };", "");
    getE("declare %R:path('') function m:a() { () };" +
         "declare %R:error('err:FORG0001') function m:b() { 'F' };" +
         "declare %R:error('Q{http://www.w3.org/2005/xqt-errors}FORG0001')"
         + "function m:b() { 'F' };", "");
    // duplicate error annotations
    getE("declare %R:path('') function m:a() { () };" +
         "declare %R:error('*') %R:error('*') function m:b() { 'F' };", "");
    getE("declare %R:path('') function m:a() { () };" +
         "declare %R:error('*', '*') function m:b() { 'F' };", "");
    getE("declare %R:path('') function m:a() { () };" +
         "declare %R:error('x', 'x') function m:b() { 'F' };", "");
    getE("declare %R:path('') function m:a() { () };" +
         "declare %R:error('err:x', 'err:x') function m:b() { 'F' };", "");
    getE("declare %R:path('') function m:a() { () };" +
         "declare %R:error('*:x', '*:x') function m:b() { 'F' };", "");
    getE("declare %R:path('') function m:a() { () };" +
         "declare %R:error('x:*', 'x:*') function m:b() { 'F' };", "");
  }

  /**
   * Error.
   * @throws Exception exception
   */
  @Test
  public void errorParam() throws Exception {
    // catch errors
    get("declare %R:path('') function m:a() { error() };" +
        "declare %R:error('*') %R:error-param('code','{$x}') " +
        "function m:b($x) { $x };", "", "err:FOER0000");
    get("declare %R:path('') function m:a() { error(xs:QName('x')) };" +
        "declare %R:error('*') %R:error-param('code','{$x}') " +
        "function m:b($x) { $x };", "", "x");
    get("declare %R:path('') function m:a() { error(xs:QName('x'), '!!!') };" +
        "declare %R:error('*') %R:error-param('description','{$x}') " +
        "function m:b($x) { $x };", "", "!!!");
  }
}
