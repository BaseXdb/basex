package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the count() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnCount extends QT3TestSet {

  /**
   *  count() applied to nodes (with optimization opportunities) .
   */
  @org.junit.Test
  public void count001() {
    final XQuery query = new XQuery(
      "count(//employee[@name='John Doe 4']) = 1",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  count() applied to nodes (with optimization opportunities) .
   */
  @org.junit.Test
  public void count002() {
    final XQuery query = new XQuery(
      "count(//employee[@name='John Doe 4']) < 2",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  count() applied to nodes (with optimization opportunities) .
   */
  @org.junit.Test
  public void count003() {
    final XQuery query = new XQuery(
      "count(//employee[@name='John Doe 4']) > 0",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  count() applied to nodes (with optimization opportunities) .
   */
  @org.junit.Test
  public void count004() {
    final XQuery query = new XQuery(
      "count(//employee[@name='John Doe 4']/@name) > 0.5",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  count() applied to nodes (with optimization opportunities) .
   */
  @org.junit.Test
  public void count005() {
    final XQuery query = new XQuery(
      "count(//*[@name='John Doe 4']) lt 1.5",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  count() applied to nodes (with optimization opportunities) .
   */
  @org.junit.Test
  public void count006() {
    final XQuery query = new XQuery(
      "count(//*[@name='John Doe 4']) eq 0",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  count() applied to nodes (with optimization opportunities) .
   */
  @org.junit.Test
  public void count007() {
    final XQuery query = new XQuery(
      "count(//*[@name='John Doe 498']) eq 0",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  count() applied to nodes (with optimization opportunities) .
   */
  @org.junit.Test
  public void count008() {
    final XQuery query = new XQuery(
      "count(//*[@name='John Doe 4']) lt 1000000000000",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  count() applied to nodes (with optimization opportunities) .
   */
  @org.junit.Test
  public void count009() {
    final XQuery query = new XQuery(
      "count(//*[@name='John Doe 4']) gt -5",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  count() applied to nodes (with optimization opportunities) .
   */
  @org.junit.Test
  public void count010() {
    final XQuery query = new XQuery(
      "count(//*[@name='John Doe 4']) eq 0.3",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  count() applied to nodes (with optimization opportunities) .
   */
  @org.junit.Test
  public void count011() {
    final XQuery query = new XQuery(
      "count(//node()) gt 40",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  count() applied to nodes (with optimization opportunities) .
   */
  @org.junit.Test
  public void count012() {
    final XQuery query = new XQuery(
      "count(//node()) ne -1",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  count() applied to nodes (with optimization opportunities) .
   */
  @org.junit.Test
  public void count013() {
    final XQuery query = new XQuery(
      "0 = count(//node())",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  count() applied to nodes (with optimization opportunities) .
   */
  @org.junit.Test
  public void count014() {
    final XQuery query = new XQuery(
      "40 gt count(//node())",
      ctx);
    query.context(node(file("docs/works-mod.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   *  A test whose essence is: `count()`. .
   */
  @org.junit.Test
  public void kSeqCountFunc1() {
    final XQuery query = new XQuery(
      "count()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `count( ((), "one", 2, "three")) eq 3`. .
   */
  @org.junit.Test
  public void kSeqCountFunc10() {
    final XQuery query = new XQuery(
      "count( ((), \"one\", 2, \"three\")) eq 3",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count( ("one", (2, "three")) ) eq 3`. .
   */
  @org.junit.Test
  public void kSeqCountFunc11() {
    final XQuery query = new XQuery(
      "count( (\"one\", (2, \"three\")) ) eq 3",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count((1, 2)) eq 2`. .
   */
  @org.junit.Test
  public void kSeqCountFunc12() {
    final XQuery query = new XQuery(
      "count((1, 2)) eq 2",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count((1, 2, 3, "four")) eq 4`. .
   */
  @org.junit.Test
  public void kSeqCountFunc13() {
    final XQuery query = new XQuery(
      "count((1, 2, 3, \"four\")) eq 4",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count((1, 2, 3, "four")) eq 4`. .
   */
  @org.junit.Test
  public void kSeqCountFunc14() {
    final XQuery query = new XQuery(
      "count((1, 2, 3, \"four\")) eq 4",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(1 to 3) eq 3`. .
   */
  @org.junit.Test
  public void kSeqCountFunc15() {
    final XQuery query = new XQuery(
      "count(1 to 3) eq 3",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(reverse((1, 2, 3))) eq 3`. .
   */
  @org.junit.Test
  public void kSeqCountFunc16() {
    final XQuery query = new XQuery(
      "count(reverse((1, 2, 3))) eq 3",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(reverse((1, 2, 3))) eq 3`. .
   */
  @org.junit.Test
  public void kSeqCountFunc17() {
    final XQuery query = new XQuery(
      "count(reverse((1, 2, 3))) eq 3",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `not(count((1, 2, current-time(), 4))) eq false()`. .
   */
  @org.junit.Test
  public void kSeqCountFunc18() {
    final XQuery query = new XQuery(
      "not(count((1, 2, current-time(), 4))) eq false()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(1, ())`. .
   */
  @org.junit.Test
  public void kSeqCountFunc2() {
    final XQuery query = new XQuery(
      "count(1, ())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `count( () ) eq 0`. .
   */
  @org.junit.Test
  public void kSeqCountFunc3() {
    final XQuery query = new XQuery(
      "count( () ) eq 0",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count( (1, 2, 3) ) eq 3`. .
   */
  @org.junit.Test
  public void kSeqCountFunc4() {
    final XQuery query = new XQuery(
      "count( (1, 2, 3) ) eq 3",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count( (1, 2, ()) ) eq 2`. .
   */
  @org.junit.Test
  public void kSeqCountFunc5() {
    final XQuery query = new XQuery(
      "count( (1, 2, ()) ) eq 2",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(()) eq 0`. .
   */
  @org.junit.Test
  public void kSeqCountFunc6() {
    final XQuery query = new XQuery(
      "count(()) eq 0",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count(((((()))))) eq 0`. .
   */
  @org.junit.Test
  public void kSeqCountFunc7() {
    final XQuery query = new XQuery(
      "count(((((()))))) eq 0",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count( (((), (), ()), (), (), (), ()) ) eq 0`. .
   */
  @org.junit.Test
  public void kSeqCountFunc8() {
    final XQuery query = new XQuery(
      "count( (((), (), ()), (), (), (), ()) ) eq 0",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `count( (1, 2, 3) ) eq 3`. .
   */
  @org.junit.Test
  public void kSeqCountFunc9() {
    final XQuery query = new XQuery(
      "count( (1, 2, 3) ) eq 3",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:count() doesn't implicitly get the context node. .
   */
  @org.junit.Test
  public void k2SeqCountFunc1() {
    final XQuery query = new XQuery(
      "(1 to 10)/count()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnCountdbl1args1() {
    final XQuery query = new XQuery(
      "fn:count((xs:double(\"-1.7976931348623157E308\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:double(mid range) .
   */
  @org.junit.Test
  public void fnCountdbl1args2() {
    final XQuery query = new XQuery(
      "fn:count((xs:double(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:double(upper bound) .
   */
  @org.junit.Test
  public void fnCountdbl1args3() {
    final XQuery query = new XQuery(
      "fn:count((xs:double(\"1.7976931348623157E308\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnCountdec1args1() {
    final XQuery query = new XQuery(
      "fn:count((xs:decimal(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void fnCountdec1args2() {
    final XQuery query = new XQuery(
      "fn:count((xs:decimal(\"617375191608514839\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void fnCountdec1args3() {
    final XQuery query = new XQuery(
      "fn:count((xs:decimal(\"999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnCountflt1args1() {
    final XQuery query = new XQuery(
      "fn:count((xs:float(\"-3.4028235E38\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:float(mid range) .
   */
  @org.junit.Test
  public void fnCountflt1args2() {
    final XQuery query = new XQuery(
      "fn:count((xs:float(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:float(upper bound) .
   */
  @org.junit.Test
  public void fnCountflt1args3() {
    final XQuery query = new XQuery(
      "fn:count((xs:float(\"3.4028235E38\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnCountint1args1() {
    final XQuery query = new XQuery(
      "fn:count((xs:int(\"-2147483648\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:int(mid range) .
   */
  @org.junit.Test
  public void fnCountint1args2() {
    final XQuery query = new XQuery(
      "fn:count((xs:int(\"-1873914410\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:int(upper bound) .
   */
  @org.junit.Test
  public void fnCountint1args3() {
    final XQuery query = new XQuery(
      "fn:count((xs:int(\"2147483647\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnCountintg1args1() {
    final XQuery query = new XQuery(
      "fn:count((xs:integer(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:integer(mid range) .
   */
  @org.junit.Test
  public void fnCountintg1args2() {
    final XQuery query = new XQuery(
      "fn:count((xs:integer(\"830993497117024304\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:integer(upper bound) .
   */
  @org.junit.Test
  public void fnCountintg1args3() {
    final XQuery query = new XQuery(
      "fn:count((xs:integer(\"999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnCountlng1args1() {
    final XQuery query = new XQuery(
      "fn:count((xs:long(\"-92233720368547758\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:long(mid range) .
   */
  @org.junit.Test
  public void fnCountlng1args2() {
    final XQuery query = new XQuery(
      "fn:count((xs:long(\"-47175562203048468\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:long(upper bound) .
   */
  @org.junit.Test
  public void fnCountlng1args3() {
    final XQuery query = new XQuery(
      "fn:count((xs:long(\"92233720368547758\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnCountnint1args1() {
    final XQuery query = new XQuery(
      "fn:count((xs:negativeInteger(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:negativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnCountnint1args2() {
    final XQuery query = new XQuery(
      "fn:count((xs:negativeInteger(\"-297014075999096793\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:negativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnCountnint1args3() {
    final XQuery query = new XQuery(
      "fn:count((xs:negativeInteger(\"-1\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnCountnni1args1() {
    final XQuery query = new XQuery(
      "fn:count((xs:nonNegativeInteger(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnCountnni1args2() {
    final XQuery query = new XQuery(
      "fn:count((xs:nonNegativeInteger(\"303884545991464527\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnCountnni1args3() {
    final XQuery query = new XQuery(
      "fn:count((xs:nonNegativeInteger(\"999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnCountnpi1args1() {
    final XQuery query = new XQuery(
      "fn:count((xs:nonPositiveInteger(\"-999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:nonPositiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnCountnpi1args2() {
    final XQuery query = new XQuery(
      "fn:count((xs:nonPositiveInteger(\"-475688437271870490\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:nonPositiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnCountnpi1args3() {
    final XQuery query = new XQuery(
      "fn:count((xs:nonPositiveInteger(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnCountpint1args1() {
    final XQuery query = new XQuery(
      "fn:count((xs:positiveInteger(\"1\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnCountpint1args2() {
    final XQuery query = new XQuery(
      "fn:count((xs:positiveInteger(\"52704602390610033\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnCountpint1args3() {
    final XQuery query = new XQuery(
      "fn:count((xs:positiveInteger(\"999999999999999999\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnCountsht1args1() {
    final XQuery query = new XQuery(
      "fn:count((xs:short(\"-32768\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:short(mid range) .
   */
  @org.junit.Test
  public void fnCountsht1args2() {
    final XQuery query = new XQuery(
      "fn:count((xs:short(\"-5324\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:short(upper bound) .
   */
  @org.junit.Test
  public void fnCountsht1args3() {
    final XQuery query = new XQuery(
      "fn:count((xs:short(\"32767\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnCountulng1args1() {
    final XQuery query = new XQuery(
      "fn:count((xs:unsignedLong(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void fnCountulng1args2() {
    final XQuery query = new XQuery(
      "fn:count((xs:unsignedLong(\"130747108607674654\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void fnCountulng1args3() {
    final XQuery query = new XQuery(
      "fn:count((xs:unsignedLong(\"184467440737095516\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnCountusht1args1() {
    final XQuery query = new XQuery(
      "fn:count((xs:unsignedShort(\"0\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void fnCountusht1args2() {
    final XQuery query = new XQuery(
      "fn:count((xs:unsignedShort(\"44633\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "count" function with the arguments set as follows: $arg = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void fnCountusht1args3() {
    final XQuery query = new XQuery(
      "fn:count((xs:unsignedShort(\"65535\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }
}
