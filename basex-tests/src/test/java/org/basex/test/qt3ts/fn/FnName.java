package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnName extends QT3TestSet {

  /**
   * Test: K-NameFunc-1 `name((), "wrong param")`. .
   */
  @org.junit.Test
  public void kNameFunc1() {
    final XQuery query = new XQuery(
      "name((), \"wrong param\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0017")
    );
  }

  /**
   * Test: K-NameFunc-2 `if(false()) then name() else true()`. .
   */
  @org.junit.Test
  public void kNameFunc2() {
    final XQuery query = new XQuery(
      "if(false()) then name() else true()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertBoolean(true)
      ||
        error("XPDY0002")
      )
    );
  }

  /**
   * Test: K-NameFunc-3 `name(()) eq ""`. .
   */
  @org.junit.Test
  public void kNameFunc3() {
    final XQuery query = new XQuery(
      "name(()) eq \"\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * argument set to the empty sequence. Uses the fn:string-length function to avoid empty file..
   */
  @org.junit.Test
  public void fnName1() {
    final XQuery query = new XQuery(
      "fn:string-length(fn:name(()))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   * that uses the "parent" axes.
   */
  @org.junit.Test
  public void fnName10() {
    final XQuery query = new XQuery(
      "for $h in (./works/employee[2]) return fn:name($h/parent::node())",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "works")
    );
  }

  /**
   * that uses the "descendant" axes.
   */
  @org.junit.Test
  public void fnName11() {
    final XQuery query = new XQuery(
      "for $h in (./works/employee[2]) return fn:name($h/descendant::empnum[position() =\n" +
      "         1])",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "empnum")
    );
  }

  /**
   * that uses the "descendant-or-self" axes.
   */
  @org.junit.Test
  public void fnName12() {
    final XQuery query = new XQuery(
      "for $h in (./works/employee[2]) return fn:name($h/descendant-or-self::empnum[position()\n" +
      "         = 1])",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "empnum")
    );
  }

  /**
   * argument to the fn-subtstring function..
   */
  @org.junit.Test
  public void fnName13() {
    final XQuery query = new XQuery(
      "for $h in (./works/employee[2]) return fn:substring(fn:name($h),2)",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "mployee")
    );
  }

  /**
   * argument to the fn:concat function..
   */
  @org.junit.Test
  public void fnName14() {
    final XQuery query = new XQuery(
      "for $h in (/works/employee[2]) return fn:concat(fn:name($h),\"A String\")",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "employeeA String")
    );
  }

  /**
   * that uses the "self" axes. Returns a string.
   */
  @org.junit.Test
  public void fnName15() {
    final XQuery query = new XQuery(
      "for $h in (./works/employee[2]) return fn:name($h/self::employee)",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "employee")
    );
  }

  /**
   * that uses the "self" axes. Returns a empty sequence Uses fn:count to avoid empty file..
   */
  @org.junit.Test
  public void fnName16() {
    final XQuery query = new XQuery(
      "for $h in (./works/employee[2]) return fn:count(fn:name($h/self::div))",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "1")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   * that uses the "parent::node()". The context node is an attribute node..
   */
  @org.junit.Test
  public void fnName17() {
    final XQuery query = new XQuery(
      " for $h in (/works/employee[2]/@name) return fn:name($h/parent::node())",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "employee")
    );
  }

  /**
   * Evaluation of the fn:name function as an argument to the string-length function. The context node is an attribute node..
   */
  @org.junit.Test
  public void fnName18() {
    final XQuery query = new XQuery(
      "fn:string-length(fn:name(./works[1]/employee[2]/@name))",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "4")
    );
  }

  /**
   * Evaluation of the fn:name function, for which the argument is a direct element constructor. The context node is an attribute node. .
   */
  @org.junit.Test
  public void fnName19() {
    final XQuery query = new XQuery(
      "fn:name(<anElement>Content</anElement>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "anElement")
    );
  }

  /**
   * argument set to an element node..
   */
  @org.junit.Test
  public void fnName2() {
    final XQuery query = new XQuery(
      "(fn:name(./works[1]/employee[1]))",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "employee")
    );
  }

  /**
   * Evaluation of the fn:name function, for which the argument is a direct element constructor with an attribute..
   */
  @org.junit.Test
  public void fnName20() {
    final XQuery query = new XQuery(
      "fn:name(<anElement name=\"attribute1\">Content</anElement>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "anElement")
    );
  }

  /**
   * Evaluation of the fn:name function used as part of a sequence..
   */
  @org.junit.Test
  public void fnName21() {
    final XQuery query = new XQuery(
      "(fn:name(./works[1]/employee[1]),fn:name(./works[1]/employee[2]))",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "employee employee")
    );
  }

  /**
   * fn:count..
   */
  @org.junit.Test
  public void fnName22() {
    final XQuery query = new XQuery(
      "fn:count(((fn:name(/works[1]/employee[1]),fn:name(/works[1]/employee[2]))))",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("2")
    );
  }

  /**
   * undefined context node and argument set to "."..
   */
  @org.junit.Test
  public void fnName23() {
    final XQuery query = new XQuery(
      "fn:name(.)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPDY0002")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void fnName24() {
    final XQuery query = new XQuery(
      "name(/*)",
      ctx);
    try {
      query.namespace("ma", "http://www.example.com/AuctionWatch");
      query.namespace("xlink", "http://www.w3.org/1999/xlink");
      query.namespace("anyzone", "http://www.example.com/auctioneers#anyzone");
      query.namespace("eachbay", "http://www.example.com/auctioneers#eachbay");
      query.namespace("yabadoo", "http://www.example.com/auctioneers#yabadoo");
      query.context(node(file("docs/auction.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ma:AuctionWatchList")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void fnName25() {
    final XQuery query = new XQuery(
      "name((//*:Start)[1]/@*)",
      ctx);
    try {
      query.namespace("ma", "http://www.example.com/AuctionWatch");
      query.namespace("xlink", "http://www.w3.org/1999/xlink");
      query.namespace("anyzone", "http://www.example.com/auctioneers#anyzone");
      query.namespace("eachbay", "http://www.example.com/auctioneers#eachbay");
      query.namespace("yabadoo", "http://www.example.com/auctioneers#yabadoo");
      query.context(node(file("docs/auction.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ma:currency")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void fnName26() {
    final XQuery query = new XQuery(
      "name((//@xml:*)[1])",
      ctx);
    try {
      query.namespace("ma", "http://www.example.com/AuctionWatch");
      query.namespace("xlink", "http://www.w3.org/1999/xlink");
      query.namespace("anyzone", "http://www.example.com/auctioneers#anyzone");
      query.namespace("eachbay", "http://www.example.com/auctioneers#eachbay");
      query.namespace("yabadoo", "http://www.example.com/auctioneers#yabadoo");
      query.context(node(file("docs/auction.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "xml:lang")
    );
  }

  /**
   * Get the name of a processing-instruction node..
   */
  @org.junit.Test
  public void fnName28() {
    final XQuery query = new XQuery(
      "name((//processing-instruction())[1])",
      ctx);
    try {
      query.namespace("ma", "http://www.example.com/AuctionWatch");
      query.namespace("xlink", "http://www.w3.org/1999/xlink");
      query.namespace("anyzone", "http://www.example.com/auctioneers#anyzone");
      query.namespace("eachbay", "http://www.example.com/auctioneers#eachbay");
      query.namespace("yabadoo", "http://www.example.com/auctioneers#yabadoo");
      query.context(node(file("docs/auction.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "xml-stylesheet")
    );
  }

  /**
   * Get the name of an element in a default but non-null namespace..
   */
  @org.junit.Test
  public void fnName29() {
    final XQuery query = new XQuery(
      "name((//*[.='1983'])[1])",
      ctx);
    try {
      query.namespace("ma", "http://www.example.com/AuctionWatch");
      query.namespace("xlink", "http://www.w3.org/1999/xlink");
      query.namespace("anyzone", "http://www.example.com/auctioneers#anyzone");
      query.namespace("eachbay", "http://www.example.com/auctioneers#eachbay");
      query.namespace("yabadoo", "http://www.example.com/auctioneers#yabadoo");
      query.context(node(file("docs/auction.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "recorded")
    );
  }

  /**
   * argument set to an attribute node. insert-start insert-end.
   */
  @org.junit.Test
  public void fnName3() {
    final XQuery query = new XQuery(
      "(fn:name(./works[1]/employee[1]/@name))",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "name")
    );
  }

  /**
   * Get the name of a comment node.
   */
  @org.junit.Test
  public void fnName30() {
    final XQuery query = new XQuery(
      "name((//comment())[1]) = ''",
      ctx);
    try {
      query.namespace("ma", "http://www.example.com/AuctionWatch");
      query.namespace("xlink", "http://www.w3.org/1999/xlink");
      query.namespace("anyzone", "http://www.example.com/auctioneers#anyzone");
      query.namespace("eachbay", "http://www.example.com/auctioneers#eachbay");
      query.namespace("yabadoo", "http://www.example.com/auctioneers#yabadoo");
      query.context(node(file("docs/auction.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * Get the name of a text node.
   */
  @org.junit.Test
  public void fnName31() {
    final XQuery query = new XQuery(
      "name((//text())[1]) = ''",
      ctx);
    try {
      query.namespace("ma", "http://www.example.com/AuctionWatch");
      query.namespace("xlink", "http://www.w3.org/1999/xlink");
      query.namespace("anyzone", "http://www.example.com/auctioneers#anyzone");
      query.namespace("eachbay", "http://www.example.com/auctioneers#eachbay");
      query.namespace("yabadoo", "http://www.example.com/auctioneers#yabadoo");
      query.context(node(file("docs/auction.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * argument set to a document node. Use of "fn:string-length" to avoid empty file. insert-start insert-end.
   */
  @org.junit.Test
  public void fnName4() {
    final XQuery query = new XQuery(
      "fn:string-length(fn:name(.))",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   * argument set to a non existing element. Use of "fn:string-length" to avoid empty file. insert-start insert-end.
   */
  @org.junit.Test
  public void fnName5() {
    final XQuery query = new XQuery(
      "fn:string-length(fn:name(./works[1]/nonexistent[1]))",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   * argument set to a non existing element. Use of "fn:string-length" to avoid empty file..
   */
  @org.junit.Test
  public void fnName6() {
    final XQuery query = new XQuery(
      "for $h in ./works[1]/employee[2] return\n" +
      "         fn:string-length(fn:name($h/child::text()[last()]))",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   * undefined context node..
   */
  @org.junit.Test
  public void fnName7() {
    final XQuery query = new XQuery(
      "fn:name()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPDY0002")
    );
  }

  /**
   * argument to the fn:upper-case function.
   */
  @org.junit.Test
  public void fnName8() {
    final XQuery query = new XQuery(
      "for $h in (./works/employee[2]) return fn:upper-case(fn:name($h))",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "EMPLOYEE")
    );
  }

  /**
   * argument to the fn:lower-case function.
   */
  @org.junit.Test
  public void fnName9() {
    final XQuery query = new XQuery(
      "for $h in (./works/employee[2]) return fn:lower-case(fn:name($h))",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "employee")
    );
  }
}
