package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the generate-id() function transferred from the XSLT 2.0 specification to XPath/XQuery 3.0.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnGenerateId extends QT3TestSet {

  /**
   * generate-id() applied to an empty sequence.
   */
  @org.junit.Test
  public void generateId000() {
    final XQuery query = new XQuery(
      "generate-id(())",
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
        assertType("xs:string")
      &&
        assertEq("\"\"")
      )
    );
  }

  /**
   * generate-id() applied to an element node.
   */
  @org.junit.Test
  public void generateId001() {
    final XQuery query = new XQuery(
      "generate-id(/*)",
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
      assertQuery("matches($result, '^[A-Za-z][A-Za-z0-9]*$')")
    );
  }

  /**
   * generate-id() applied to an attribute node.
   */
  @org.junit.Test
  public void generateId002() {
    final XQuery query = new XQuery(
      "generate-id((//@*)[1])",
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
      assertQuery("matches($result, '^[A-Za-z][A-Za-z0-9]*$')")
    );
  }

  /**
   * generate-id() applied to a document node.
   */
  @org.junit.Test
  public void generateId003() {
    final XQuery query = new XQuery(
      "generate-id(/)",
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
      assertQuery("matches($result, '^[A-Za-z][A-Za-z0-9]*$')")
    );
  }

  /**
   * generate-id() applied to a comment node.
   */
  @org.junit.Test
  public void generateId004() {
    final XQuery query = new XQuery(
      "generate-id((//comment())[1])",
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
      assertQuery("matches($result, '^[A-Za-z][A-Za-z0-9]*$')")
    );
  }

  /**
   * generate-id() applied to a processing instruction node.
   */
  @org.junit.Test
  public void generateId005() {
    final XQuery query = new XQuery(
      "generate-id((//processing-instruction())[1])",
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
      assertQuery("matches($result, '^[A-Za-z][A-Za-z0-9]*$')")
    );
  }

  /**
   * generate-id() applied to a text node.
   */
  @org.junit.Test
  public void generateId006() {
    final XQuery query = new XQuery(
      "generate-id((//text())[1])",
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
      assertQuery("matches($result, '^[A-Za-z][A-Za-z0-9]*$')")
    );
  }

  /**
   * generate-id() with no arguments applied to a document node.
   */
  @org.junit.Test
  public void generateId008() {
    final XQuery query = new XQuery(
      "generate-id() eq generate-id(/)",
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
   * generate-id() with no arguments applied to an element node.
   */
  @org.junit.Test
  public void generateId009() {
    final XQuery query = new XQuery(
      "/*/(generate-id() eq generate-id(.))",
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
   * Uniqueness of generated IDs.
   */
  @org.junit.Test
  public void generateId010() {
    final XQuery query = new XQuery(
      "let $nodes := (/ | //*/(.|@*|comment()|processing-instruction()|text())) \n" +
      "            return count($nodes) eq count(distinct-values($nodes/generate-id()))",
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
   * Uniqueness of generated IDs for document nodes across multiple documents.
   */
  @org.junit.Test
  public void generateId012() {
    final XQuery query = new XQuery(
      "let $nodes := collection()\n" +
      "            return count($nodes) eq count(distinct-values($nodes/generate-id()))",
      ctx);
    try {
      query.addCollection("", new String[] { file("docs/auction.xml"), file("docs/works-mod.xml") });
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
   * Uniqueness of generated IDs for element nodes across multiple documents.
   */
  @org.junit.Test
  public void generateId013() {
    final XQuery query = new XQuery(
      "let $nodes := collection()//*\n" +
      "            return count($nodes) eq count(distinct-values($nodes/generate-id()))",
      ctx);
    try {
      query.addCollection("", new String[] { file("docs/auction.xml"), file("docs/works-mod.xml") });
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
   * generate-id() applied to a parentless element node.
   */
  @org.junit.Test
  public void generateId014() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace copy=\"http://www.w3.org/QT3/copy\";\n" +
      "        generate-id(copy:copy(/*))\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      query.addModule("http://www.w3.org/QT3/copy", file("fn/id/copy.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertQuery("matches($result, '^[A-Za-z][A-Za-z0-9]*$')")
    );
  }

  /**
   * generate-id() applied to a parentless attribute node.
   */
  @org.junit.Test
  public void generateId015() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace copy=\"http://www.w3.org/QT3/copy\";\n" +
      "        generate-id(copy:copy((//@*)[1]))\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      query.addModule("http://www.w3.org/QT3/copy", file("fn/id/copy.xq"));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertQuery("matches($result, '^[A-Za-z][A-Za-z0-9]*$')")
    );
  }

  /**
   * generate-id() changes when a node is copied.
   */
  @org.junit.Test
  public void generateId016() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace copy=\"http://www.w3.org/QT3/copy\";\n" +
      "        generate-id(copy:copy(/*)) eq generate-id(/*)\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      query.addModule("http://www.w3.org/QT3/copy", file("fn/id/copy.xq"));
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
   * generate-id() applied to a parentless attribute node.
   */
  @org.junit.Test
  public void generateId017() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace copy=\"http://www.w3.org/QT3/copy\";\n" +
      "        let $att := (//@*)[1] return generate-id(copy:copy($att)) eq generate-id($att)\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      query.addModule("http://www.w3.org/QT3/copy", file("fn/id/copy.xq"));
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
   * generate-id() with no context item.
   */
  @org.junit.Test
  public void generateId901() {
    final XQuery query = new XQuery(
      "let $f := function() {generate-id()} return $f()",
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
   * generate-id() with context item not a node.
   */
  @org.junit.Test
  public void generateId902() {
    final XQuery query = new XQuery(
      "let $f := function($x as item()) {generate-id($x)} return $f(3)",
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
   * generate-id() with context item not a node.
   */
  @org.junit.Test
  public void generateId903() {
    final XQuery query = new XQuery(
      "for $i in 1 to 20 return generate-id($i)",
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
   * generate-id() with context item not a node.
   */
  @org.junit.Test
  public void generateId904() {
    final XQuery query = new XQuery(
      "let $f := function($x as item()) {\"\"} return generate-id($f)",
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
   * generate-id() applied to a sequence of nodes.
   */
  @org.junit.Test
  public void generateId905() {
    final XQuery query = new XQuery(
      "generate-id(//*)",
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
      error("XPTY0004")
    );
  }
}
