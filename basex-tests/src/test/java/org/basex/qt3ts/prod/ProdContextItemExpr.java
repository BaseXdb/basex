package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the ContextItemExpr production.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdContextItemExpr extends QT3TestSet {

  /**
   *  Simple context item test (uses just "name" .
   */
  @org.junit.Test
  public void externalcontextitem1() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace eg = \"http://example.org\"; \n" +
      "        declare function eg:noContextFunction() { name }; \n" +
      "        eg:noContextFunction()",
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
   *  context item expression where context item used in addition operation. .
   */
  @org.junit.Test
  public void externalcontextitem10() {
    final XQuery query = new XQuery(
      "for $var in (/works/employee[1]) return $var/(exactly-one(hours) + exactly-one(hours))",
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
      assertEq("80")
    );
  }

  /**
   *  context item expression where context item used in subtraction operation. .
   */
  @org.junit.Test
  public void externalcontextitem11() {
    final XQuery query = new XQuery(
      "for $var in (/works/employee[1]) return $var/(exactly-one(hours) - exactly-one(hours))",
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
   *  context item expression where context item used in multiplication operation. .
   */
  @org.junit.Test
  public void externalcontextitem12() {
    final XQuery query = new XQuery(
      "for $var in (/works/employee[1]) return $var/(exactly-one(hours) * exactly-one(hours))",
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
      assertEq("1600")
    );
  }

  /**
   *  context item expression where context item used in mod operation. .
   */
  @org.junit.Test
  public void externalcontextitem13() {
    final XQuery query = new XQuery(
      "for $var in (/works/employee[1]) return $var/(exactly-one(hours) mod exactly-one(hours))",
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
   *  context item expression where context item used in division (div operator) operation. .
   */
  @org.junit.Test
  public void externalcontextitem14() {
    final XQuery query = new XQuery(
      "for $var in (/works/employee[1]) return $var/(exactly-one(hours) div exactly-one(hours))",
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
      assertEq("1")
    );
  }

  /**
   *  context item expression where context item used in division (idiv operator) operation. .
   */
  @org.junit.Test
  public void externalcontextitem15() {
    final XQuery query = new XQuery(
      "for $var in (/works/employee[1]) return $var/(exactly-one(hours) idiv exactly-one(hours))",
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
      assertEq("1")
    );
  }

  /**
   *  context item expression where context item used in a boolean (and operator)expression. .
   */
  @org.junit.Test
  public void externalcontextitem16() {
    final XQuery query = new XQuery(
      "for $var in (/works/employee[1]) return $var/(xs:boolean(exactly-one(hours) - 39) and xs:boolean(exactly-one(hours) - 39))",
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
      assertBoolean(true)
    );
  }

  /**
   *  context item expression where context item used in a boolean (or operator)expression. .
   */
  @org.junit.Test
  public void externalcontextitem17() {
    final XQuery query = new XQuery(
      "for $var in (/works/employee[1]) return $var/(xs:boolean(exactly-one(hours) - 39) or xs:boolean(exactly-one(hours) - 39))",
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
      assertBoolean(true)
    );
  }

  /**
   *  context item expression where context item used with string-length function. .
   */
  @org.junit.Test
  public void externalcontextitem18() {
    final XQuery query = new XQuery(
      "for $var in (/works/employee[1]) return $var/fn:string-length(exactly-one(hours))",
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
   *  context item expression where context item used with "avg" function. .
   */
  @org.junit.Test
  public void externalcontextitem19() {
    final XQuery query = new XQuery(
      "for $var in (/works/employee[1]) return $var/fn:avg((hours,hours,hours))",
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
      assertEq("40")
    );
  }

  /**
   *  context item expression where context item is used as string. .
   */
  @org.junit.Test
  public void externalcontextitem2() {
    final XQuery query = new XQuery(
      "for $var in /works/employee[1] return $var/xs:string(exactly-one(empnum))",
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
      assertStringValue(false, "E1")
    );
  }

  /**
   *  context item expression where context item used with "min" function. .
   */
  @org.junit.Test
  public void externalcontextitem20() {
    final XQuery query = new XQuery(
      "for $var in (/works/employee[1]) return $var/fn:min((hours,hours,22))",
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
      assertEq("22")
    );
  }

  /**
   *  context item expression where context item used with "max" function. .
   */
  @org.junit.Test
  public void externalcontextitem21() {
    final XQuery query = new XQuery(
      "for $var in (/works/employee[1]) return $var/fn:max((hours,exactly-one(hours) + 1,22))",
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
      assertEq("41")
    );
  }

  /**
   *  context item expression .
   */
  @org.junit.Test
  public void externalcontextitem22() {
    final XQuery query = new XQuery(
      "./works/employee[1]",
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
      assertSerialization("<employee name=\"Jane Doe 1\" gender=\"female\">\n   <empnum>E1</empnum>\n   <pnum>P1</pnum>\n   <hours>40</hours>\n  </employee>", false)
    );
  }

  /**
   *  Reference to a context item that has not been bound. .
   */
  @org.junit.Test
  public void externalcontextitem23() {
    final XQuery query = new XQuery(
      "./works/employee[1]",
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
        error("XPDY0002")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Reference to a context item that has not been bound. .
   */
  @org.junit.Test
  public void externalcontextitem24() {
    final XQuery query = new XQuery(
      "works/employee[1]",
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
      assertSerialization("<employee name=\"Jane Doe 1\" gender=\"female\">\n   <empnum>E1</empnum>\n   <pnum>P1</pnum>\n   <hours>40</hours>\n  </employee>", false)
    );
  }

  /**
   *  context item expression where context item is used as an integer. .
   */
  @org.junit.Test
  public void externalcontextitem3() {
    final XQuery query = new XQuery(
      "for $var in (/works/employee[1]) return $var/xs:integer(exactly-one(hours))",
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
      assertEq("40")
    );
  }

  /**
   *  context item expression where context item is used as a decimal. .
   */
  @org.junit.Test
  public void externalcontextitem4() {
    final XQuery query = new XQuery(
      "for $var in (/works/employee[1]) return $var/xs:decimal(exactly-one(hours))",
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
      assertEq("40")
    );
  }

  /**
   *  context item expression where context item isused as a float. .
   */
  @org.junit.Test
  public void externalcontextitem5() {
    final XQuery query = new XQuery(
      "for $var in /works/employee[1] return $var/xs:float(exactly-one(hours))",
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
      assertEq("40")
    );
  }

  /**
   *  context item expression where context item is used sa a double. .
   */
  @org.junit.Test
  public void externalcontextitem6() {
    final XQuery query = new XQuery(
      "for $var in (/works/employee[1]) return $var/xs:double(exactly-one(hours))",
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
      assertEq("40")
    );
  }

  /**
   *  context item expression where context item is used as an xs:boolean. .
   */
  @org.junit.Test
  public void externalcontextitem7() {
    final XQuery query = new XQuery(
      "for $var in (/works/employee[1]) return $var/xs:boolean(exactly-one(hours) - 39)",
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
      assertBoolean(true)
    );
  }

  /**
   *  context item expression where context item is an xs:boolean used with fn:not(). .
   */
  @org.junit.Test
  public void externalcontextitem8() {
    final XQuery query = new XQuery(
      "for $var in (/works/employee[1]) return $var/fn:not(xs:boolean(exactly-one(hours) - 39))",
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
      assertBoolean(false)
    );
  }

  /**
   *  context item expression where context item used as argumet to fn:sum. .
   */
  @org.junit.Test
  public void externalcontextitem9() {
    final XQuery query = new XQuery(
      "for $var in (/works/employee[1]) return $var/fn:sum((hours,hours))",
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
      assertEq("80")
    );
  }

  /**
   *  Simple context item test (uses just "." .
   */
  @org.junit.Test
  public void internalcontextitem1() {
    final XQuery query = new XQuery(
      "declare namespace eg = \"http://example.org\"; declare function eg:noContextFunction() { . }; eg:noContextFunction()",
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
   *  context item expression where context item used in addition operation. .
   */
  @org.junit.Test
  public void internalcontextitem10() {
    final XQuery query = new XQuery(
      "(1,2,3)[(. + .) gt 1]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3")
    );
  }

  /**
   *  context item expression where context item used in subtraction operation. .
   */
  @org.junit.Test
  public void internalcontextitem11() {
    final XQuery query = new XQuery(
      "(3,4,5)[(xs:integer(5) - xs:integer(.)) gt 1]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("3")
    );
  }

  /**
   *  context item expression where context item used in multiplication operation. .
   */
  @org.junit.Test
  public void internalcontextitem12() {
    final XQuery query = new XQuery(
      "(3,4,5)[(xs:integer(.) * xs:integer(.)) gt 2]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3 4 5")
    );
  }

  /**
   *  context item expression where context item used in mod operation. .
   */
  @org.junit.Test
  public void internalcontextitem13() {
    final XQuery query = new XQuery(
      "(6,10,14)[(xs:integer(.) mod xs:integer(3)) gt 1]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("14")
    );
  }

  /**
   *  context item expression where context item used in division (div operator) operation. .
   */
  @org.junit.Test
  public void internalcontextitem14() {
    final XQuery query = new XQuery(
      "(6,10,14)[(xs:integer(.) div xs:integer(3)) gt 2]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "10 14")
    );
  }

  /**
   *  context item expression where context item used in division (idiv operator) operation. .
   */
  @org.junit.Test
  public void internalcontextitem15() {
    final XQuery query = new XQuery(
      "(6,10,14)[(xs:integer(.) idiv xs:integer(3)) gt 2]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "10 14")
    );
  }

  /**
   *  context item expression where context item used in a boolean (and operator)expression. .
   */
  @org.junit.Test
  public void internalcontextitem16() {
    final XQuery query = new XQuery(
      "(fn:true(),fn:false(),fn:true())[xs:boolean(.) and xs:boolean(.)]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "true true")
    );
  }

  /**
   *  context item expression where context item used in a boolean (or operator)expression. .
   */
  @org.junit.Test
  public void internalcontextitem17() {
    final XQuery query = new XQuery(
      "(fn:true(),fn:false(),fn:true())[xs:boolean(.) or xs:boolean(.)]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "true true")
    );
  }

  /**
   *  context item expression where context item used with string-length function. .
   */
  @org.junit.Test
  public void internalcontextitem18() {
    final XQuery query = new XQuery(
      "(\"ABC\", \"DEF\",\"A\")[fn:string-length(.) gt 2]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "ABC DEF")
    );
  }

  /**
   *  context item expression where context item used with "avg" function. .
   */
  @org.junit.Test
  public void internalcontextitem19() {
    final XQuery query = new XQuery(
      "(1,2,3)[fn:avg((.,2,3)) gt 2]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2 3")
    );
  }

  /**
   *  context item expression wher context item is string. .
   */
  @org.junit.Test
  public void internalcontextitem2() {
    final XQuery query = new XQuery(
      "(\"A\",\"B\",\"C\")[xs:string(.)]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "A B C")
    );
  }

  /**
   *  context item expression where context item used with "min" function. .
   */
  @org.junit.Test
  public void internalcontextitem20() {
    final XQuery query = new XQuery(
      "(1,2,3)[fn:min((.,2)) eq 2]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2 3")
    );
  }

  /**
   *  context item expression where context item used with "max" function. .
   */
  @org.junit.Test
  public void internalcontextitem21() {
    final XQuery query = new XQuery(
      "(1,2,3)[fn:min((.,3)) eq 3]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("3")
    );
  }

  /**
   *  context item expression wher context item is an integer. .
   */
  @org.junit.Test
  public void internalcontextitem3() {
    final XQuery query = new XQuery(
      "(1,2,3)[xs:integer(.)]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3")
    );
  }

  /**
   *  context item expression wher context item is a decimal. .
   */
  @org.junit.Test
  public void internalcontextitem4() {
    final XQuery query = new XQuery(
      "(1,2,3)[xs:decimal(.)]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3")
    );
  }

  /**
   *  context item expression where context item is a float. .
   */
  @org.junit.Test
  public void internalcontextitem5() {
    final XQuery query = new XQuery(
      "(1,2,3)[xs:float(.)]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3")
    );
  }

  /**
   *  context item expression where context item is a double. .
   */
  @org.junit.Test
  public void internalcontextitem6() {
    final XQuery query = new XQuery(
      "(1,2,3)[xs:double(.)]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3")
    );
  }

  /**
   *  context item expression where context item is an xs:boolean. .
   */
  @org.junit.Test
  public void internalcontextitem7() {
    final XQuery query = new XQuery(
      "(fn:true(),fn:false(),fn:true())[xs:boolean(.)]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "true true")
    );
  }

  /**
   *  context item expression where context item is an xs:boolean used with fn:not(). .
   */
  @org.junit.Test
  public void internalcontextitem8() {
    final XQuery query = new XQuery(
      "(fn:false(),fn:true(),fn:false())[fn:not(xs:boolean(.))]",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "false false")
    );
  }

  /**
   *  context item expression where context item is an empty sequence. uses fn:count to avoid empty file. .
   */
  @org.junit.Test
  public void internalcontextitem9() {
    final XQuery query = new XQuery(
      "fn:count(((),(),())[xs:string(.)])",
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
        assertEq("0")
      ||
        error("XPST0005")
      )
    );
  }
}
