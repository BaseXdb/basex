package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * tests for the fn:map() higher-order function introduced in XPath 3.0.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnMap extends QT3TestSet {

  /**
   * Convert names to upper-case (one-to-one mapping).
   */
  @org.junit.Test
  public void map001() {
    final XQuery query = new XQuery(
      "map(upper-case#1, (\"aa\", \"bb\", \"cc\", \"dd\", \"ee\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"AA\", \"BB\", \"CC\", \"DD\", \"EE\"")
    );
  }

  /**
   * Get lengths of names (one-to-many mapping).
   */
  @org.junit.Test
  public void map002() {
    final XQuery query = new XQuery(
      "map(string-to-codepoints#1, (\"john\", \"jane\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("106, 111, 104, 110, 106, 97, 110, 101")
    );
  }

  /**
   * Use map function as a filter.
   */
  @org.junit.Test
  public void map003() {
    final XQuery query = new XQuery(
      "map(function($x){$x[contains(., 'e')]}, (\"john\", \"mary\", \"jane\", \"anne\", \"peter\", \"ian\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"jane\", \"anne\", \"peter\"")
    );
  }

  /**
   * Map using user-defined anonymous function.
   */
  @org.junit.Test
  public void map004() {
    final XQuery query = new XQuery(
      "map(function($x){upper-case($x)} , (\"john\", \"mary\", \"jane\", \"anne\", \"peter\", \"ian\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"JOHN\", \"MARY\", \"JANE\", \"ANNE\", \"PETER\", \"IAN\"")
    );
  }

  /**
   * Map using user-defined anonymous function.
   */
  @org.junit.Test
  public void map005() {
    final XQuery query = new XQuery(
      "\n" +
      "            map(function($e as xs:string) as xs:string { lower-case($e) }, map(function($n as xs:string){upper-case($n)},(\"john\", \"mary\", \"jane\", \"anne\", \"peter\", \"ian\")))\n" +
      "        ",
      ctx);
    try {
      query.addDocument("fn/higherOrder/names.xml", file("fn/higherOrder/names.xml"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("\"john\", \"mary\", \"jane\", \"anne\", \"peter\", \"ian\"")
    );
  }

  /**
   * Map using a named recursive function.
   */
  @org.junit.Test
  public void map006() {
    final XQuery query = new XQuery(
      "\n" +
      "            declare function local:children($n as node()) as node()* \n" +
      "                { $n/child::node() }; \n" +
      "            declare function local:attributes($e as node()) as node()* \n" +
      "                { $e/attribute::node() }; \n" +
      "            declare function local:self($e as node()) as node() \n" +
      "                { $e }; \n" +
      "            declare function local:union(\n" +
      "                        $f as function(node()) as node()*, \n" +
      "                        $g as function(node()) as node()*) as function(node()) as node()* { \n" +
      "                function($a) {$f($a) | $g($a)} };\n" +
      "            let $data := (/a), \n" +
      "                $f := local:union(local:children#1, local:union(local:attributes#1, local:self#1)) \n" +
      "            return map($f, $data/*)[not(. instance of attribute())]\n" +
      "        ",
      ctx);
    try {
      query.context(node(file("fn/higherOrder/doc1.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<b bid=\"b\"><b1/></b><b1/><c cid=\"c\"><c1/></c><c1/>", false)
    );
  }

  /**
   * map function - input is an empty sequence.
   */
  @org.junit.Test
  public void map007() {
    final XQuery query = new XQuery(
      "map(round#1, ())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * map function - partial application.
   */
  @org.junit.Test
  public void map008() {
    final XQuery query = new XQuery(
      "let $f := function($x as xs:double*){map(round#1, $x)} return $f((1.2345, 6.789))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertDeepEq("1, 7")
    );
  }

  /**
   * map function - error, function has the wrong arity.
   */
  @org.junit.Test
  public void map901() {
    final XQuery query = new XQuery(
      "map(starts-with#2, (\"aa\", \"bb\", \"cc\", \"dd\", \"ee\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * map function - error, function can't handle one item in the sequence.
   */
  @org.junit.Test
  public void map902() {
    final XQuery query = new XQuery(
      "map(upper-case#1, (\"aa\", \"bb\", \"cc\", \"dd\", 12))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * map function - error, first argument is not a function.
   */
  @org.junit.Test
  public void map903() {
    final XQuery query = new XQuery(
      "map((), (\"aa\", \"bb\", \"cc\", \"dd\", 12))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }
}
