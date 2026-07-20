package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * Tests for fn:not.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnNotTest extends SandboxTest {
  /** fn:not on numeric arguments. */
  @Test public void notNumeric() {
    query("fn:not(xs:int('-2147483648'))", false);
    query("fn:not(xs:int('-1873914410'))", false);
    query("fn:not(xs:int('2147483647'))", false);
    query("fn:not(xs:integer('-999999999999999999'))", false);
    query("fn:not(xs:integer('830993497117024304'))", false);
    query("fn:not(xs:integer('999999999999999999'))", false);
    query("fn:not(xs:decimal('-999999999999999999'))", false);
    query("fn:not(xs:decimal('617375191608514839'))", false);
    query("fn:not(xs:decimal('999999999999999999'))", false);
    query("fn:not(xs:double('-1.7976931348623157E308'))", false);
    query("fn:not(xs:double('0'))", true);
    query("fn:not(xs:double('1.7976931348623157E308'))", false);
    query("fn:not(xs:float('-3.4028235E38'))", false);
    query("fn:not(xs:float('0'))", true);
    query("fn:not(xs:float('3.4028235E38'))", false);
    query("fn:not(xs:long('-92233720368547758'))", false);
    query("fn:not(xs:long('-47175562203048468'))", false);
    query("fn:not(xs:long('92233720368547758'))", false);
    query("fn:not(xs:unsignedShort('0'))", true);
    query("fn:not(xs:unsignedShort('44633'))", false);
    query("fn:not(xs:unsignedShort('65535'))", false);
    query("fn:not(xs:negativeInteger('-999999999999999999'))", false);
    query("fn:not(xs:negativeInteger('-297014075999096793'))", false);
    query("fn:not(xs:negativeInteger('-1'))", false);
    query("fn:not(xs:positiveInteger('1'))", false);
    query("fn:not(xs:positiveInteger('52704602390610033'))", false);
    query("fn:not(xs:positiveInteger('999999999999999999'))", false);
    query("fn:not(xs:unsignedLong('0'))", true);
    query("fn:not(xs:unsignedLong('130747108607674654'))", false);
    query("fn:not(xs:unsignedLong('184467440737095516'))", false);
    query("fn:not(xs:nonPositiveInteger('-999999999999999999'))", false);
    query("fn:not(xs:nonPositiveInteger('-475688437271870490'))", false);
    query("fn:not(xs:nonPositiveInteger('0'))", true);
    query("fn:not(xs:nonNegativeInteger('0'))", true);
    query("fn:not(xs:nonNegativeInteger('303884545991464527'))", false);
    query("fn:not(xs:nonNegativeInteger('999999999999999999'))", false);
    query("fn:not(xs:short('-32768'))", false);
    query("fn:not(xs:short('-5324'))", false);
    query("fn:not(xs:short('32767'))", false);
  }

  /** fn:not on string arguments and in comparisons. */
  @Test public void notString() {
    query("fn:not('true')", false);
    query("fn:not('fn:not()')", false);
    query("fn:not('true') and fn:not('true')", false);
    query("fn:not('true') or fn:not('true')", false);
    query("fn:not('true') eq fn:not('true')", true);
    query("fn:not('true') ne fn:not('true')", false);
    query("fn:not('true') lt fn:not('true')", false);
    query("fn:not('true') le fn:not('true')", true);
    query("fn:not('true') gt fn:not('true')", false);
    query("fn:not('true') ge fn:not('true')", true);
    query("fn:not('true') = fn:not('true')", true);
    query("fn:not('true') != fn:not('true')", false);
    query("fn:not('true') < fn:not('true')", false);
    query("fn:not('true') <= fn:not('true')", true);
    query("fn:not('true') > fn:not('true')", false);
    query("fn:not('true') >= fn:not('true')", true);
    query("xs:boolean(fn:not('true'))", false);
    query("fn:string(fn:not('true'))", "false");
    query("fn:concat(xs:string(fn:not('true')), xs:string(fn:not('true')))", "falsefalse");
    query("fn:contains(xs:string(fn:not('true')), xs:string(fn:not('true')))", true);
    query("fn:string-length(xs:string(fn:not('true')))", 5);
  }

  /** fn:not with wrong number of arguments and general cases. */
  @Test public void notMisc() {
    error("not()", INVNARGS_X_X);
    error("not(1, 2, 3, 4, 5, 6)", INVNARGS_X_X);
    query("not(false() and false())", true);
    query("not(not(true()))", true);
    query("not(false())", true);
    query("not(0)", true);
    query("not(())", true);
    query("not(xs:anyURI(''))", true);
    query("not(not(xs:anyURI('example.com/')))", true);
    query("not(fn:boolean((1, 2, 3, current-time())[1] treat as xs:integer)) eq false()", true);
  }

  /** fn:not on constructed nodes and variables. */
  @Test public void notNodes() {
    query("not(<X/> = <X/>)", false);
    query("not(<X/> != <X/>)", true);
    query("not(<X>a</X> < <X>b</X>)", false);
    query("not(<X>a</X> > <X>b</X>)", true);
    query("not(<X>a</X> >= <X>a</X>)", false);
    query("not(<X>a</X> <= <X>a</X>)", false);
    query("not((<X>b</X>, <X>a</X>) <= <X>a</X>)", false);
    query("not((<X>b</X>, <X>a</X>) >= <X>a</X>)", false);
    query("not(not(<X/>))", true);
    query("for $b in (true(), false()) return not($b)", "false\ntrue");
    query("for $b in (true(), false()) return not(not($b))", "true\nfalse");

    query("let $s := 1 let $e := 2 return not($s <= $e)", false);
    query("let $s := 1 let $e := 2 return not($s >= $e)", true);
    query("let $s := 1 let $e := 2 return not($s <  $e)", false);
    query("let $s := 1 let $e := 2 return not($s >  $e)", true);
    query("let $s := 1 let $e := 2 return not($s =  $e)", true);
    query("let $s := 1 let $e := 2 return not($s != $e)", false);
    query("let $s := 1 let $e := 2 return not($s le $e)", false);
    query("let $s := 1 let $e := 2 return not($s ge $e)", true);
    query("let $s := 1 let $e := 2 return not($s lt $e)", false);
    query("let $s := 1 let $e := 2 return not($s gt $e)", true);
    query("let $s := 1 let $e := 2 return not($s eq $e)", true);
    query("let $s := 1 let $e := 2 return not($s ne $e)", false);
  }
}
