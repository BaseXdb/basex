package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the collection function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnCollection extends QT3TestSet {

  /**
   *  Pass an invalid xs:anyURI to fn:collection(). .
   */
  @org.junit.Test
  public void k2SeqCollectionFunc1() {
    final XQuery query = new XQuery(
      "collection(\"http:\\\\invalidURI\\someURI%gg\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FODC0004")
    );
  }

  /**
   *  ':/ is an invalid URI. .
   */
  @org.junit.Test
  public void k2SeqCollectionFunc2() {
    final XQuery query = new XQuery(
      "collection(\":/\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FODC0004")
    );
  }

  /**
   * default collection.
   */
  @org.junit.Test
  public void collection001() {
    final XQuery query = new XQuery(
      "collection()",
      ctx);
    query.addCollection("", new String[] { "docs/bib.xml", "docs/reviews.xml" });

    final QT3Result res = result(query);
    result = res;
    test(
      (
        (
          assertQuery("count($result) = 2")
        &&
          assertType("document-node()*")
        )
      ||
        error("FODC0003")
      )
    );
  }

  /**
   * default collection selected by supplying empty sequence.
   */
  @org.junit.Test
  public void collection002() {
    final XQuery query = new XQuery(
      "collection(())",
      ctx);
    query.addCollection("", new String[] { "docs/bib.xml", "docs/reviews.xml" });

    final QT3Result res = result(query);
    result = res;
    test(
      (
        (
          assertQuery("count($result) = 2")
        &&
          assertType("document-node()*")
        )
      ||
        error("FODC0003")
      )
    );
  }

  /**
   * default collection is stable.
   */
  @org.junit.Test
  public void collection003() {
    final XQuery query = new XQuery(
      "collection() | collection(())",
      ctx);
    query.addCollection("", new String[] { "docs/books.xml", "docs/bib.xml", "docs/reviews.xml" });

    final QT3Result res = result(query);
    result = res;
    test(
      (
        (
          assertQuery("count($result) = 3")
        &&
          assertType("document-node()*")
        )
      ||
        error("FODC0003")
      )
    );
  }

  /**
   * default collection is not guaranteed stable if an appropriate user option is set.
   */
  @org.junit.Test
  public void collection004() {
    final XQuery query = new XQuery(
      "collection() | collection(())",
      ctx);
    query.addCollection("", new String[] { "docs/bib.xml", "docs/reviews.xml" });

    final QT3Result res = result(query);
    result = res;
    test(
      (
        (
          assertQuery("count($result) = 2")
        &&
          assertType("document-node()*")
        )
      ||
        (
          assertQuery("count($result) = 4")
        &&
          assertType("document-node()*")
        )
      )
    );
  }

  /**
   * simple collection, absolute URI.
   */
  @org.junit.Test
  public void collection005() {
    final XQuery query = new XQuery(
      "collection(\"http://www.w3.org/2010/09/qt-fots-catalog/collection1\")",
      ctx);
    query.addCollection("http://www.w3.org/2010/09/qt-fots-catalog/collection1", new String[] { "docs/bib.xml", "docs/reviews.xml" });
    query.baseURI("http://www.w3.org/2010/09/qt-fots-catalog/");
    query.bind("collection-uri", new XQuery("'http://www.w3.org/2010/09/qt-fots-catalog/collection1'", ctx).value());

    final QT3Result res = result(query);
    result = res;
    test(
      (
        (
          assertQuery("count($result) = 2")
        &&
          assertType("document-node()*")
        )
      ||
        error("FODC0003")
      )
    );
  }

  /**
   * simple collection, relative URI.
   */
  @org.junit.Test
  public void collection006() {
    final XQuery query = new XQuery(
      "collection(\"collection1\")",
      ctx);
    query.addCollection("http://www.w3.org/2010/09/qt-fots-catalog/collection1", new String[] { "docs/bib.xml", "docs/reviews.xml" });
    query.baseURI("http://www.w3.org/2010/09/qt-fots-catalog/");
    query.bind("collection-uri", new XQuery("'http://www.w3.org/2010/09/qt-fots-catalog/collection1'", ctx).value());

    final QT3Result res = result(query);
    result = res;
    test(
      (
        (
          assertQuery("count($result) = 2")
        &&
          assertType("document-node()*")
        )
      ||
        error("FODC0003")
      )
    );
  }

  /**
   * simple collection, results are stable.
   */
  @org.junit.Test
  public void collection007() {
    final XQuery query = new XQuery(
      "collection(\"http://www.w3.org/2010/09/qt-fots-catalog/collection1\") | collection(\"collection1\")",
      ctx);
    query.addCollection("http://www.w3.org/2010/09/qt-fots-catalog/collection1", new String[] { "docs/bib.xml", "docs/reviews.xml" });
    query.baseURI("http://www.w3.org/2010/09/qt-fots-catalog/");
    query.bind("collection-uri", new XQuery("'http://www.w3.org/2010/09/qt-fots-catalog/collection1'", ctx).value());

    final QT3Result res = result(query);
    result = res;
    test(
      (
        (
          assertQuery("count($result) = 2")
        &&
          assertType("document-node()*")
        )
      ||
        error("FODC0003")
      )
    );
  }

  /**
   * simple collection, results are not stable at user option.
   */
  @org.junit.Test
  public void collection008() {
    final XQuery query = new XQuery(
      "collection(\"http://www.w3.org/2010/09/qt-fots-catalog/collection1\") | collection(\"collection1\")",
      ctx);
    query.addCollection("http://www.w3.org/2010/09/qt-fots-catalog/collection1", new String[] { "docs/bib.xml", "docs/reviews.xml" });
    query.baseURI("http://www.w3.org/2010/09/qt-fots-catalog/");
    query.bind("collection-uri", new XQuery("'http://www.w3.org/2010/09/qt-fots-catalog/collection1'", ctx).value());

    final QT3Result res = result(query);
    result = res;
    test(
      (
        (
          assertQuery("count($result) = 2")
        &&
          assertType("document-node()*")
        )
      ||
        (
          assertQuery("count($result) = 4")
        &&
          assertType("document-node()*")
        )
      )
    );
  }

  /**
   * simple collection, interaction with document-uri().
   */
  @org.junit.Test
  public void collection009() {
    final XQuery query = new XQuery(
      "let $c := collection(\"http://www.w3.org/2010/09/qt-fots-catalog/collection1\") \n" +
      "            return $c | (for $doc in $c return doc(document-uri($doc)))",
      ctx);
    query.addCollection("http://www.w3.org/2010/09/qt-fots-catalog/collection1", new String[] { "docs/bib.xml", "docs/reviews.xml" });
    query.baseURI("http://www.w3.org/2010/09/qt-fots-catalog/");
    query.bind("collection-uri", new XQuery("'http://www.w3.org/2010/09/qt-fots-catalog/collection1'", ctx).value());

    final QT3Result res = result(query);
    result = res;
    test(
      (
        (
          assertQuery("count($result) = 2")
        &&
          assertType("document-node()*")
        )
      ||
        error("FODC0003")
      )
    );
  }

  /**
   * Use a directory URI as a collection URI, if supported.
   */
  @org.junit.Test
  public void collection010() {
    final XQuery query = new XQuery(
      "collection(\"collection/one/\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        (
          assertQuery("count($result) = 3")
        &&
          assertQuery("sum($result/*/number()) = 6")
        &&
          assertType("document-node()*")
        )
      ||
        error("FODC0003")
      )
    );
  }

  /**
   * Unknown collection URI.
   */
  @org.junit.Test
  public void collection900() {
    final XQuery query = new XQuery(
      "collection(\"nonexistent\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FODC0002")
    );
  }

  /**
   * Unknown default collection.
   */
  @org.junit.Test
  public void collection901() {
    final XQuery query = new XQuery(
      "collection()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FODC0002")
    );
  }

  /**
   * Invalid collection URI.
   */
  @org.junit.Test
  public void collection902() {
    final XQuery query = new XQuery(
      "collection(\"##invalid\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FODC0004")
    );
  }

  /**
   *  Evaluation of an fn:collection function with wrong arity. .
   */
  @org.junit.Test
  public void fnCollection1() {
    final XQuery query = new XQuery(
      "fn:collection(\"argument1\",\"argument2\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  Test that two uses of fn:collection are stable. .
   */
  @org.junit.Test
  public void fnCollection10() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $c1 := fn:collection($collection-uri) \n" +
      "        let $c2 := fn:collection($collection-uri) \n" +
      "        for $c at $p in $c1 \n" +
      "        return $c is exactly-one($c2[$p])",
      ctx);
    query.addCollection("http://www.w3.org/2010/09/qt-fots-catalog/collection2", new String[] { "docs/books.xml", "docs/bib.xml", "docs/reviews.xml" });
    query.bind("collection-uri", new XQuery("'http://www.w3.org/2010/09/qt-fots-catalog/collection2'", ctx).value());

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "true true true")
    );
  }

  /**
   *  Test that two uses of fn:collection are stable. .
   */
  @org.junit.Test
  public void fnCollection10d() {
    final XQuery query = new XQuery(
      "\n" +
      "        let $c1 := fn:collection() \n" +
      "        let $c2 := fn:collection() \n" +
      "        for $c at $p in $c1 \n" +
      "        return $c is exactly-one($c2[$p])",
      ctx);
    query.addCollection("", new String[] { "docs/books.xml", "docs/bib.xml", "docs/reviews.xml" });

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "true true true")
    );
  }

  /**
   *  Evaluation of ana fn:collection, which tries to retrieve a non-existent resource. .
   */
  @org.junit.Test
  public void fnCollection2() {
    final XQuery query = new XQuery(
      "fn:collection(\"thisfileshouldnotexists\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FODC0002")
    );
  }

  /**
   *  Evaluation of ana fn:collection with argument set to an invalid URI. .
   */
  @org.junit.Test
  public void fnCollection3() {
    final XQuery query = new XQuery(
      "fn:collection(\"invalidURI%gg\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FODC0004")
    );
  }

  /**
   *  Count the number of nodes in a named collection. .
   */
  @org.junit.Test
  public void fnCollection4() {
    final XQuery query = new XQuery(
      "count(fn:collection($collection-uri))",
      ctx);
    query.addCollection("http://www.w3.org/2010/09/qt-fots-catalog/collection1", new String[] { "docs/bib.xml", "docs/reviews.xml" });
    query.baseURI("http://www.w3.org/2010/09/qt-fots-catalog/");
    query.bind("collection-uri", new XQuery("'http://www.w3.org/2010/09/qt-fots-catalog/collection1'", ctx).value());

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("2")
    );
  }

  /**
   *  Count the number of nodes in the default collection. .
   */
  @org.junit.Test
  public void fnCollection4d() {
    final XQuery query = new XQuery(
      "count(fn:collection())",
      ctx);
    query.addCollection("", new String[] { "docs/bib.xml", "docs/reviews.xml" });

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("2")
    );
  }

  /**
   *  Count the number of nodes in the collection. .
   */
  @org.junit.Test
  public void fnCollection5() {
    final XQuery query = new XQuery(
      "count(fn:collection($collection-uri))",
      ctx);
    query.addCollection("http://www.w3.org/2010/09/qt-fots-catalog/collection2", new String[] { "docs/books.xml", "docs/bib.xml", "docs/reviews.xml" });
    query.bind("collection-uri", new XQuery("'http://www.w3.org/2010/09/qt-fots-catalog/collection2'", ctx).value());

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("3")
    );
  }

  /**
   *  Count the number of nodes in the collection. .
   */
  @org.junit.Test
  public void fnCollection5d() {
    final XQuery query = new XQuery(
      "count(fn:collection())",
      ctx);
    query.addCollection("", new String[] { "docs/books.xml", "docs/bib.xml", "docs/reviews.xml" });

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("3")
    );
  }

  /**
   *  Return the titles in the collection ordered by the title. .
   */
  @org.junit.Test
  public void fnCollection6() {
    final XQuery query = new XQuery(
      "for $x in fn:collection($collection-uri)//title order by string($x) return $x",
      ctx);
    query.addCollection("http://www.w3.org/2010/09/qt-fots-catalog/collection2", new String[] { "docs/books.xml", "docs/bib.xml", "docs/reviews.xml" });
    query.bind("collection-uri", new XQuery("'http://www.w3.org/2010/09/qt-fots-catalog/collection2'", ctx).value());

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<title>Advanced Programming in the Unix environment</title><title>Advanced Programming in the Unix environment</title><title>Basic Syntax</title><title>Data Model</title><title>Data on the Web</title><title>Data on the Web</title><title>Syntax For Data Model</title><title>TCP/IP Illustrated</title><title>TCP/IP Illustrated</title><title>The Economics of Technology and Content for Digital TV</title><title>XML</title><title>XML and Semistructured Data</title>", false)
    );
  }

  /**
   *  Return elements that immediately contain TCP/IP. .
   */
  @org.junit.Test
  public void fnCollection7() {
    final XQuery query = new XQuery(
      "distinct-values(fn:collection($collection-uri)//*[text()[contains(.,\"TCP/IP\")]]/normalize-space())",
      ctx);
    query.addCollection("http://www.w3.org/2010/09/qt-fots-catalog/collection2", new String[] { "docs/books.xml", "docs/bib.xml", "docs/reviews.xml" });
    query.bind("collection-uri", new XQuery("'http://www.w3.org/2010/09/qt-fots-catalog/collection2'", ctx).value());

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertPermutation("\"TCP/IP Illustrated\", \"One of the best books on TCP/IP.\"")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Return the first title element in each document. .
   */
  @org.junit.Test
  public void fnCollection8() {
    final XQuery query = new XQuery(
      "for $d in fn:collection($collection-uri) return ($d//title)[1]",
      ctx);
    query.addCollection("http://www.w3.org/2010/09/qt-fots-catalog/collection1", new String[] { "docs/bib.xml", "docs/reviews.xml" });
    query.baseURI("http://www.w3.org/2010/09/qt-fots-catalog/");
    query.bind("collection-uri", new XQuery("'http://www.w3.org/2010/09/qt-fots-catalog/collection1'", ctx).value());

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertSerialization("<title>TCP/IP Illustrated</title><title>Data on the Web</title>", false)
      ||
        assertSerialization("<title>Data on the Web</title><title>TCP/IP Illustrated</title>", false)
      )
    );
  }

  /**
   *  Count the title elements in each document. .
   */
  @org.junit.Test
  public void fnCollection9() {
    final XQuery query = new XQuery(
      "for $d in fn:collection($collection-uri) order by count($d//title) return count($d//title)",
      ctx);
    query.addCollection("http://www.w3.org/2010/09/qt-fots-catalog/collection2", new String[] { "docs/books.xml", "docs/bib.xml", "docs/reviews.xml" });
    query.bind("collection-uri", new XQuery("'http://www.w3.org/2010/09/qt-fots-catalog/collection2'", ctx).value());

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "3 4 5")
    );
  }
}
