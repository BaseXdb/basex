package org.basex.qt3ts.app;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the UseCaseTREE.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class AppUseCaseTREE extends QT3TestSet {

  /**
.
   */
  @org.junit.Test
  public void treeQueriesResultsQ1() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:toc($book-or-section as element()) as element()* { \n" +
      "            for $section in $book-or-section/section \n" +
      "            return <section> { $section/@* , $section/title , local:toc($section) } </section> };\n" +
      "        <toc> { for $s in /book return local:toc($s) } </toc>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("docs/book.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<toc><section id=\"intro\" difficulty=\"easy\"><title>Introduction</title><section><title>Audience</title></section><section><title>Web Data and the Two Cultures</title></section></section><section id=\"syntax\" difficulty=\"medium\"><title>A Syntax For Data</title><section><title>Base Types</title></section><section><title>Representing Relational Databases</title></section><section><title>Representing Object Databases</title></section></section></toc>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void treeQueriesResultsQ2() {
    final XQuery query = new XQuery(
      "<figlist> { for $f in //figure return <figure> { $f/@* } { $f/title } </figure> } </figlist>",
      ctx);
    try {
      query.context(node(file("docs/book.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<figlist><figure height=\"400\" width=\"400\"><title>Traditional client/server architecture</title></figure><figure height=\"200\" width=\"500\"><title>Graph representations of structures</title></figure><figure height=\"250\" width=\"400\"><title>Examples of Relations</title></figure></figlist>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void treeQueriesResultsQ3() {
    final XQuery query = new XQuery(
      "<section_count>{ count(//section) }</section_count>, <figure_count>{ count(//figure) }</figure_count>",
      ctx);
    try {
      query.context(node(file("docs/book.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<section_count>7</section_count><figure_count>3</figure_count>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void treeQueriesResultsQ4() {
    final XQuery query = new XQuery(
      "<top_section_count> { count(/book/section) } </top_section_count>",
      ctx);
    try {
      query.context(node(file("docs/book.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<top_section_count>2</top_section_count>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void treeQueriesResultsQ5() {
    final XQuery query = new XQuery(
      "<section_list> { for $s in //section let $f := $s/figure return <section title=\"{ $s/title/text() }\" figcount=\"{ count($f) }\"/> } </section_list>",
      ctx);
    try {
      query.context(node(file("docs/book.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<section_list><section title=\"Introduction\" figcount=\"0\"/><section title=\"Audience\" figcount=\"0\"/><section title=\"Web Data and the Two Cultures\" figcount=\"1\"/><section title=\"A Syntax For Data\" figcount=\"1\"/><section title=\"Base Types\" figcount=\"0\"/><section title=\"Representing Relational Databases\" figcount=\"1\"/><section title=\"Representing Object Databases\" figcount=\"0\"/></section_list>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void treeQueriesResultsQ6() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:section-summary($book-or-section as element()*) as element()* { \n" +
      "            for $section in $book-or-section \n" +
      "            return <section> { $section/@* } { $section/title } <figcount> { count($section/figure) } </figcount> { local:section-summary($section/section) } </section> \n" +
      "        }; \n" +
      "        <toc> { \n" +
      "            for $s in /book/section \n" +
      "            return local:section-summary($s) \n" +
      "        } </toc>\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("docs/book.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<toc><section id=\"intro\" difficulty=\"easy\"><title>Introduction</title><figcount>0</figcount><section><title>Audience</title><figcount>0</figcount></section><section><title>Web Data and the Two Cultures</title><figcount>1</figcount></section></section><section id=\"syntax\" difficulty=\"medium\"><title>A Syntax For Data</title><figcount>1</figcount><section><title>Base Types</title><figcount>0</figcount></section><section><title>Representing Relational Databases</title><figcount>1</figcount></section><section><title>Representing Object Databases</title><figcount>0</figcount></section></section></toc>", false)
    );
  }
}
