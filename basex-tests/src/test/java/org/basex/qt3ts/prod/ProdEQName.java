package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the EQName production.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdEQName extends QT3TestSet {

  /**
   *  Use EQName in a path expression .
   */
  @org.junit.Test
  public void eqname001() {
    final XQuery query = new XQuery(
      "<out>{ (<my:a xmlns:my=\"http://www.example.com/ns/my\"><my:b>42</my:b></my:a>) / Q{http://www.example.com/ns/my}b }</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<out><my:b xmlns:my=\"http://www.example.com/ns/my\">42</my:b></out>", false)
    );
  }

  /**
   *  Use EQName in a variable name .
   */
  @org.junit.Test
  public void eqname002() {
    final XQuery query = new XQuery(
      "\n" +
      "            declare variable $Q{http://www.example.com/ns/my}var := 12; \n" +
      "            <out>{$Q{http://www.example.com/ns/my}var}</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<out>12</out>", false)
    );
  }

  /**
   *  Use EQName in a function name .
   */
  @org.junit.Test
  public void eqname003() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function Q{http://www.example.com/ns/my}fn ($a as xs:integer) as xs:integer { $a + 2 }; \n" +
      "        <out>{Q{http://www.example.com/ns/my}fn(12)}</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<out>14</out>", false)
    );
  }

  /**
   *  Use EQName in a type name .
   */
  @org.junit.Test
  public void eqname004() {
    final XQuery query = new XQuery(
      "declare function local:fn ($a as Q{http://www.w3.org/2001/XMLSchema}integer) as element(Q{http://www.example.com/ns/my}e) { <e xmlns=\"http://www.example.com/ns/my\">{$a}</e> }; <out>{local:fn(12)}</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<out><e xmlns=\"http://www.example.com/ns/my\">12</e></out>", false)
    );
  }

  /**
   *  Use EQName in declare option .
   */
  @org.junit.Test
  public void eqname005() {
    final XQuery query = new XQuery(
      "declare option Q{http://www.example.com/ns}option \"ignore me\"; <a/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a/>", false)
    );
  }

  /**
   *  Use EQName in a pragma .
   */
  @org.junit.Test
  public void eqname006() {
    final XQuery query = new XQuery(
      "(# Q{http://www.example.com/ns}pragma ignore me #) {<a/>}",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a/>", false)
    );
  }

  /**
   *  Use EQName in a decimal format .
   */
  @org.junit.Test
  public void eqname007() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare decimal-format Q{http://www.example.com/ns}format grouping-separator=\"'\"; \n" +
      "        <a xmlns:ex=\"http://www.example.com/ns\">{format-number(1e9, \"#'###'###'##0.00\", 'ex:format')}</a>\n" +
      "      ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a xmlns:ex=\"http://www.example.com/ns\">1'000'000'000.00</a>", false)
    );
  }

  /**
   *  Use character references in an EQName .
   */
  @org.junit.Test
  public void eqname008() {
    final XQuery query = new XQuery(
      "<out>{ (<a xmlns:ex=\"http://www.example.com/ns\"><ex:b>93.7</ex:b></a>) /Q{http://www&#x2e;example&#x2E;com/ns}b }</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<out><ex:b xmlns:ex=\"http://www.example.com/ns\">93.7</ex:b></out>", false)
    );
  }

  /**
   *  Use whitespace in an EQName .
   */
  @org.junit.Test
  public void eqname009() {
    final XQuery query = new XQuery(
      "<out>{ (<a xmlns:ex=\"http://www.example.com/ ns\"><ex:b>93.7</ex:b></a>) /Q{ http://www.example.com/ ns }b/namespace-uri() }</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<out>http://www.example.com/ ns</out>", false)
    );
  }

  /**
   *  Use quotes in an EQName .
   */
  @org.junit.Test
  public void eqname010() {
    final XQuery query = new XQuery(
      "<out>{ (<a xmlns:ex=\"http://www.example.com/ns?p='23'\"><ex:b>93.7</ex:b></a>) /Q{http://www.example.com/ns?p='23'}b }</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<out><ex:b xmlns:ex=\"http://www.example.com/ns?p='23'\">93.7</ex:b></out>", false)
    );
  }

  /**
   *  Use quotes in an EQName .
   */
  @org.junit.Test
  public void eqname011() {
    final XQuery query = new XQuery(
      "<out>{ (<a xmlns:ex=\"http://www.example.com/ns?p='23'\"><ex:b>93.7</ex:b></a>) /Q{http://www.example.com/ns?p='23'}b }</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<out><ex:b xmlns:ex=\"http://www.example.com/ns?p='23'\">93.7</ex:b></out>", false)
    );
  }

  /**
   *  Use EQName-style wildcard .
   */
  @org.junit.Test
  public void eqname012() {
    final XQuery query = new XQuery(
      "<out>{ (<my:a xmlns:my=\"http://www.example.com/ns/my\"><my:b>42</my:b></my:a>) / Q{http://www.example.com/ns/my}* + 5 }</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<out>47</out>", false)
    );
  }

  /**
   *  Use EQName-style wildcard (no namespace) .
   */
  @org.junit.Test
  public void eqname013() {
    final XQuery query = new XQuery(
      "<out xmlns=\"http://www.example.com/one\">{ (<my:a xmlns:my=\"http://www.example.com/ns/my\"><my:b>42</my:b><b xmlns=\"\">93</b></my:a>) / Q{}* + 5 }</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<out xmlns=\"http://www.example.com/one\">98</out>", false)
    );
  }

  /**
   *  EQName in XPath element name.
   */
  @org.junit.Test
  public void eqname014() {
    final XQuery query = new XQuery(
      "string((//Q{http://www.example.com/AuctionWatch}Start)[1])",
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
      assertStringValue(false, "3.00")
    );
  }

  /**
   *  EQName in XPath variable name.
   */
  @org.junit.Test
  public void eqname015() {
    final XQuery query = new XQuery(
      "for $Q{http://example.com/ns}x in 1 to 10 return $Q{http://example.com/ns}x + 1",
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
      assertStringValue(false, "2 3 4 5 6 7 8 9 10 11")
    );
  }

  /**
   *  Use EQName-style wildcard (no namespace) to select attribute nodes .
   */
  @org.junit.Test
  public void eqname016() {
    final XQuery query = new XQuery(
      "string-join(<a foo=\"3\" bar=\"5\" xml:space=\"preserve\"/> / @Q{}*, '.')",
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
        assertEq("\"3.5\"")
      ||
        assertEq("\"5.3\"")
      )
    );
  }

  /**
   *  Use EQName-style wildcard (XML namespace) to select attribute nodes .
   */
  @org.junit.Test
  public void eqname017() {
    final XQuery query = new XQuery(
      "string-join(<a foo=\"3\" bar=\"5\" xml:space=\"preserve\"/> / @Q{http://www.w3.org/XML/1998/namespace}*, '.')",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("\"preserve\"")
    );
  }

  /**
   *  Use EQName-style wildcard (no namespace URI) to select PI nodes: not allowed .
   */
  @org.junit.Test
  public void eqname019() {
    final XQuery query = new XQuery(
      "(<?alpha?>, <?beta?>, <?gamma?>)/processing-instruction(Q{}alpha)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Invalid EQName - superfluous colon .
   */
  @org.junit.Test
  public void eqname901() {
    final XQuery query = new XQuery(
      "<out>{ (<a xmlns:ex=\"http://www.example.com/ns?p='23'\"><ex:b>93.7</ex:b></a>) /Q{http://www.example.com/ns?p='23'}:b }</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Invalid EQName - mismatched braces .
   */
  @org.junit.Test
  public void eqname902() {
    final XQuery query = new XQuery(
      "<out>{ (<a xmlns:ex=\"http://www.example.com/ns?p='23'\"><ex:b>93.7</ex:b></a>) /Q{http://www.example.com/ns?p='23'}}b }</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  EQName - not allowed in element constructor .
   */
  @org.junit.Test
  public void eqname903() {
    final XQuery query = new XQuery(
      "<out>{ <Q{http://www.example.com/ns}/> }</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }
}
