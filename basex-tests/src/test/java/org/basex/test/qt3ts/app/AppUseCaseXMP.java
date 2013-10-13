package org.basex.test.qt3ts.app;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the UseCaseXMP.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class AppUseCaseXMP extends QT3TestSet {

  /**
.
   */
  @org.junit.Test
  public void xmpQueriesResultsQ1() {
    final XQuery query = new XQuery(
      "\n" +
      "      \t<bib> { \n" +
      "      \t\tfor $b in /bib/book \n" +
      "      \t\twhere $b/publisher = \"Addison-Wesley\" and $b/@year > 1991 \n" +
      "      \t\treturn <book year=\"{ $b/@year }\">{ $b/title }</book> } \n" +
      "      \t</bib>",
      ctx);
    try {
      query.context(node(file("docs/bib.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<bib><book year=\"1994\"><title>TCP/IP Illustrated</title></book><book year=\"1992\"><title>Advanced Programming in the Unix environment</title></book></bib>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void xmpQueriesResultsQ10() {
    final XQuery query = new XQuery(
      "\n" +
      "      \t<results> { \n" +
      "      \t\tlet $doc := (/) \n" +
      "      \t\tfor $t in distinct-values($doc//book/title) \n" +
      "      \t\tlet $p := $doc//book[title = $t]/price \n" +
      "      \t\treturn <minprice title=\"{ $t }\"> <price>{ min($p) }</price> </minprice> } \n" +
      "      \t</results>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("docs/prices.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results><minprice title=\"Advanced Programming in the Unix environment\"><price>65.95</price></minprice><minprice title=\"TCP/IP Illustrated\"><price>65.95</price></minprice><minprice title=\"Data on the Web\"><price>34.95</price></minprice></results>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void xmpQueriesResultsQ11() {
    final XQuery query = new XQuery(
      "\n" +
      "      \t<bib> { \n" +
      "      \t\tfor $b in //book[author] \n" +
      "      \t\treturn <book> \n" +
      "      \t\t\t{ $b/title } { $b/author } </book> } \n" +
      "      \t\t\t{ for $b in //book[editor] \n" +
      "      \t\t\t\treturn <reference> { $b/title } {$b/editor/affiliation} </reference> } \n" +
      "      \t</bib>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("docs/bib.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<bib><book><title>TCP/IP Illustrated</title><author><last>Stevens</last><first>W.</first></author></book><book><title>Advanced Programming in the Unix environment</title><author><last>Stevens</last><first>W.</first></author></book><book><title>Data on the Web</title><author><last>Abiteboul</last><first>Serge</first></author><author><last>Buneman</last><first>Peter</first></author><author><last>Suciu</last><first>Dan</first></author></book><reference><title>The Economics of Technology and Content for Digital TV</title><affiliation>CITI</affiliation></reference></bib>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void xmpQueriesResultsQ12() {
    final XQuery query = new XQuery(
      "\n" +
      "      \t<bib> { \n" +
      "      \t\tfor $book1 in //book, $book2 in //book \n" +
      "      \t\tlet $aut1 := \n" +
      "      \t\t\tfor $a in $book1/author \n" +
      "      \t\t\torder by exactly-one($a/last), exactly-one($a/first) \n" +
      "      \t\t\treturn $a \n" +
      "      \t\tlet $aut2 := \n" +
      "      \t\t\tfor $a in $book2/author \n" +
      "      \t\t\torder by exactly-one($a/last), exactly-one($a/first) \n" +
      "      \t\t\treturn $a \n" +
      "      \t\twhere $book1 << $book2 and not($book1/title = $book2/title) and deep-equal($aut1, $aut2) \n" +
      "      \t\treturn <book-pair> { $book1/title } { $book2/title } </book-pair> } \n" +
      "      \t</bib>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("docs/bib.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<bib><book-pair><title>TCP/IP Illustrated</title><title>Advanced Programming in the Unix environment</title></book-pair></bib>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void xmpQueriesResultsQ2() {
    final XQuery query = new XQuery(
      "\n" +
      "      \t<results> { \n" +
      "      \t\tfor $b in /bib/book, $t in $b/title, $a in $b/author \n" +
      "      \t\treturn <result> { $t } { $a } </result> } \n" +
      "      \t</results>",
      ctx);
    try {
      query.context(node(file("docs/bib.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results><result><title>TCP/IP Illustrated</title><author><last>Stevens</last><first>W.</first></author></result><result><title>Advanced Programming in the Unix environment</title><author><last>Stevens</last><first>W.</first></author></result><result><title>Data on the Web</title><author><last>Abiteboul</last><first>Serge</first></author></result><result><title>Data on the Web</title><author><last>Buneman</last><first>Peter</first></author></result><result><title>Data on the Web</title><author><last>Suciu</last><first>Dan</first></author></result></results>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void xmpQueriesResultsQ3() {
    final XQuery query = new XQuery(
      "\n" +
      "      \t<results> { \n" +
      "      \t\tfor $b in /bib/book \n" +
      "      \t\treturn <result> { $b/title } { $b/author } </result> } \n" +
      "      \t</results>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("docs/bib.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results><result><title>TCP/IP Illustrated</title><author><last>Stevens</last><first>W.</first></author></result><result><title>Advanced Programming in the Unix environment</title><author><last>Stevens</last><first>W.</first></author></result><result><title>Data on the Web</title><author><last>Abiteboul</last><first>Serge</first></author><author><last>Buneman</last><first>Peter</first></author><author><last>Suciu</last><first>Dan</first></author></result><result><title>The Economics of Technology and Content for Digital TV</title></result></results>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void xmpQueriesResultsQ4() {
    final XQuery query = new XQuery(
      "\n" +
      "      \t<results> { \n" +
      "      \t\tlet $a := //author \n" +
      "      \t\tfor $last in distinct-values($a/last), $first in distinct-values($a[last=$last]/first) \n" +
      "      \t\torder by $last, $first \n" +
      "      \t\treturn <result> \n" +
      "      \t\t\t\t<author> <last>{ $last }</last> <first>{ $first }</first> </author> \n" +
      "      \t\t\t\t{ for $b in /bib/book \n" +
      "      \t\t\t\t\twhere some $ba in $b/author satisfies ($ba/last = $last and $ba/first=$first) \n" +
      "      \t\t\t\t\treturn $b/title } \n" +
      "      \t\t\t   </result> } \n" +
      "      \t</results>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("docs/bib.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results><result><author><last>Abiteboul</last><first>Serge</first></author><title>Data on the Web</title></result><result><author><last>Buneman</last><first>Peter</first></author><title>Data on the Web</title></result><result><author><last>Stevens</last><first>W.</first></author><title>TCP/IP Illustrated</title><title>Advanced Programming in the Unix environment</title></result><result><author><last>Suciu</last><first>Dan</first></author><title>Data on the Web</title></result></results>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void xmpQueriesResultsQ5() {
    final XQuery query = new XQuery(
      "<books-with-prices> { \n" +
      "        for $b in $bib//book, $a in $reviews//entry \n" +
      "        where $b/title = $a/title \n" +
      "        return <book-with-prices> { $b/title } <price-bstore2>{ $a/price/text() }</price-bstore2> <price-bstore1>{ $b/price/text() }</price-bstore1> </book-with-prices> } </books-with-prices>\n" +
      "      ",
      ctx);
    try {
      query.bind("$bib", node(file("docs/bib.xml")));
      query.bind("$reviews", node(file("docs/reviews.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<books-with-prices><book-with-prices><title>TCP/IP Illustrated</title><price-bstore2>65.95</price-bstore2><price-bstore1>65.95</price-bstore1></book-with-prices><book-with-prices><title>Advanced Programming in the Unix environment</title><price-bstore2>65.95</price-bstore2><price-bstore1>65.95</price-bstore1></book-with-prices><book-with-prices><title>Data on the Web</title><price-bstore2>34.95</price-bstore2><price-bstore1>39.95</price-bstore1></book-with-prices></books-with-prices>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void xmpQueriesResultsQ6() {
    final XQuery query = new XQuery(
      "\n" +
      "      \t<bib> { \n" +
      "      \t\tfor $b in //book \n" +
      "      \t\twhere count($b/author) > 0 \n" +
      "      \t\treturn <book> \n" +
      "      \t\t\t{ $b/title } \n" +
      "      \t\t\t{ for $a in $b/author[position()<=2] return $a } \n" +
      "      \t\t\t{ if (count($b/author) > 2) then <et-al/> else () } \n" +
      "      \t\t\t</book> } \n" +
      "      \t</bib>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("docs/bib.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<bib><book><title>TCP/IP Illustrated</title><author><last>Stevens</last><first>W.</first></author></book><book><title>Advanced Programming in the Unix environment</title><author><last>Stevens</last><first>W.</first></author></book><book><title>Data on the Web</title><author><last>Abiteboul</last><first>Serge</first></author><author><last>Buneman</last><first>Peter</first></author><et-al/></book></bib>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void xmpQueriesResultsQ7() {
    final XQuery query = new XQuery(
      "\n" +
      "      \t<bib> { \n" +
      "      \t\tfor $b in //book \n" +
      "      \t\twhere $b/publisher = \"Addison-Wesley\" and $b/@year > 1991 \n" +
      "      \t\torder by exactly-one($b/title) \n" +
      "      \t\treturn <book> { $b/@year } { $b/title } </book> } \n" +
      "      \t</bib>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("docs/bib.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<bib><book year=\"1992\"><title>Advanced Programming in the Unix environment</title></book><book year=\"1994\"><title>TCP/IP Illustrated</title></book></bib>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void xmpQueriesResultsQ8() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tfor $b in //book \n" +
      "      \tlet $e := $b/*[contains(string(.), \"Suciu\") and ends-with(local-name(.), \"or\")] \n" +
      "      \twhere exists($e) \n" +
      "      \treturn <book> { $b/title } { $e } </book>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("docs/bib.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<book><title>Data on the Web</title><author><last>Suciu</last><first>Dan</first></author></book>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void xmpQueriesResultsQ9() {
    final XQuery query = new XQuery(
      "\n" +
      "      \t<results> { \n" +
      "      \t\tfor $t in //(chapter | section)/title \n" +
      "      \t\twhere contains(exactly-one($t/text()), \"XML\") \n" +
      "      \t\treturn $t } \n" +
      "      \t</results>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("docs/books.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<results><title>XML</title><title>XML and Semistructured Data</title></results>", false)
    );
  }
}
