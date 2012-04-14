package org.basex.test.qt3ts.app;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the UseCaseSGML.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class AppUseCaseSGML extends QT3TestSet {

  /**
.
   */
  @org.junit.Test
  public void sgmlQueriesResultsQ1() {
    final XQuery query = new XQuery(
      "<result> { //report//para } </result>",
      ctx);
    query.context(node(file("docs/sgml.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result><para>With the ever-changing and growing global market, companies and\n large organizations are searching for ways to become more viable and\n competitive. Downsizing and other cost-cutting measures demand more\n efficient use of corporate resources. One very important resource is\n an organization's information.</para><para>As part of the move toward integrated information management,\n whole industries are developing and implementing standards for\n exchanging technical information. This report describes how one such\n standard, the Standard Generalized Markup Language (SGML), works as\n part of an overall information management strategy.</para><para>While SGML is a fairly recent technology, the use of\n <emph>markup</emph> in computer-generated documents has existed for a\n while.</para><para>Markup is everything in a document that is not content. The\n traditional meaning of markup is the manual <emph>marking</emph> up\n of typewritten text to give instructions for a typesetter or\n compositor about how to fit the text on a page and what typefaces to\n use. This kind of markup is known as <emph>procedural markup</emph>.</para><para>Most electronic publishing systems today use some form of\n procedural markup. Procedural markup codes are good for one\n presentation of the information.</para><para>Generic markup (also known as descriptive markup) describes the\n <emph>purpose</emph> of the text in a document. A basic concept of\n generic markup is that the content of a document must be separate from\n the style. Generic markup allows for multiple presentations of the\n information.</para><para>Industries involved in technical documentation increasingly\n prefer generic over procedural markup schemes. When a company changes\n software or hardware systems, enormous data translation tasks arise,\n often resulting in errors.</para><para>SGML defines a strict markup scheme with a syntax for defining\n document data elements and an overall framework for marking up\n documents.</para><para>SGML can describe and create documents that are not dependent on\n any hardware, software, formatter, or operating system. Since SGML documents\n conform to an international standard, they are portable.</para><para>You can break a typical document into three layers: structure,\n content, and style. SGML works by separating these three aspects and\n deals mainly with the relationship between structure and content.</para><para>At the heart of an SGML application is a file called the DTD, or\n Document Type Definition. The DTD sets up the structure of a document,\n much like a database schema describes the types of information it\n handles.</para><para>A database schema also defines the relationships between the\n various types of data. Similarly, a DTD specifies <emph>rules</emph>\n to help ensure documents have a consistent, logical structure.</para><para>Content is the information itself. The method for identifying\n the information and its meaning within this framework is called\n <emph>tagging</emph>. Tagging must\n conform to the rules established in the DTD (see <xref xrefid=\"top4\"/>).</para><para>SGML does not standardize style or other processing methods for\n information stored in SGML.</para><para>The Graphic Communications Association has been\n instrumental in the development of SGML. GCA provides conferences,\n tutorials, newsletters, and publication sales for both members and\n non-members.</para><para security=\"c\">Exiled members of the former Soviet Union's secret\n police, the KGB, have infiltrated the upper ranks of the GCA and are\n planning the Final Revolution as soon as DSSSL is completed.</para></result>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void sgmlQueriesResultsQ10() {
    final XQuery query = new XQuery(
      "<result> { let $x := //xref[@xrefid = \"top4\"], $t := //title[. << exactly-one($x)] return $t[last()] } </result>",
      ctx);
    query.context(node(file("docs/sgml.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result><title>Content</title></result>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void sgmlQueriesResultsQ2() {
    final XQuery query = new XQuery(
      "<result> { //intro/para } </result>",
      ctx);
    query.context(node(file("docs/sgml.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result><para>With the ever-changing and growing global market, companies and\n large organizations are searching for ways to become more viable and\n competitive. Downsizing and other cost-cutting measures demand more\n efficient use of corporate resources. One very important resource is\n an organization's information.</para><para>As part of the move toward integrated information management,\n whole industries are developing and implementing standards for\n exchanging technical information. This report describes how one such\n standard, the Standard Generalized Markup Language (SGML), works as\n part of an overall information management strategy.</para><para>While SGML is a fairly recent technology, the use of\n <emph>markup</emph> in computer-generated documents has existed for a\n while.</para><para>Markup is everything in a document that is not content. The\n traditional meaning of markup is the manual <emph>marking</emph> up\n of typewritten text to give instructions for a typesetter or\n compositor about how to fit the text on a page and what typefaces to\n use. This kind of markup is known as <emph>procedural markup</emph>.</para><para>SGML defines a strict markup scheme with a syntax for defining\n document data elements and an overall framework for marking up\n documents.</para><para>SGML can describe and create documents that are not dependent on\n any hardware, software, formatter, or operating system. Since SGML documents\n conform to an international standard, they are portable.</para><para>You can break a typical document into three layers: structure,\n content, and style. SGML works by separating these three aspects and\n deals mainly with the relationship between structure and content.</para><para>The Graphic Communications Association has been\n instrumental in the development of SGML. GCA provides conferences,\n tutorials, newsletters, and publication sales for both members and\n non-members.</para><para security=\"c\">Exiled members of the former Soviet Union's secret\n police, the KGB, have infiltrated the upper ranks of the GCA and are\n planning the Final Revolution as soon as DSSSL is completed.</para></result>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void sgmlQueriesResultsQ3() {
    final XQuery query = new XQuery(
      "<result> { for $c in //chapter where empty($c/intro) return $c/section/intro/para } </result>",
      ctx);
    query.context(node(file("docs/sgml.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result><para>The Graphic Communications Association has been\n instrumental in the development of SGML. GCA provides conferences,\n tutorials, newsletters, and publication sales for both members and\n non-members.</para><para security=\"c\">Exiled members of the former Soviet Union's secret\n police, the KGB, have infiltrated the upper ranks of the GCA and are\n planning the Final Revolution as soon as DSSSL is completed.</para></result>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void sgmlQueriesResultsQ4() {
    final XQuery query = new XQuery(
      "<result> { (((//chapter)[2]//section)[3]//para)[2] } </result>",
      ctx);
    query.context(node(file("docs/sgml.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result><para>At the heart of an SGML application is a file called the DTD, or\n Document Type Definition. The DTD sets up the structure of a document,\n much like a database schema describes the types of information it\n handles.</para></result>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void sgmlQueriesResultsQ5() {
    final XQuery query = new XQuery(
      "<result> { //para[@security = \"c\"] } </result>",
      ctx);
    query.context(node(file("docs/sgml.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result><para security=\"c\">Exiled members of the former Soviet Union's secret\n police, the KGB, have infiltrated the upper ranks of the GCA and are\n planning the Final Revolution as soon as DSSSL is completed.</para></result>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void sgmlQueriesResultsQ6() {
    final XQuery query = new XQuery(
      "<result> { for $s in //section/@shorttitle return <stitle>{ $s }</stitle> } </result>",
      ctx);
    query.context(node(file("docs/sgml.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result><stitle shorttitle=\"What is markup?\"/><stitle shorttitle=\"What is SGML?\"/><stitle shorttitle=\"How does SGML work?\"/></result>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void sgmlQueriesResultsQ7() {
    final XQuery query = new XQuery(
      "<result> { for $i in //intro/para[1] return <first_letter>{ substring(string($i), 1, 1) }</first_letter> } </result>",
      ctx);
    query.context(node(file("docs/sgml.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result><first_letter>W</first_letter><first_letter>W</first_letter><first_letter>M</first_letter><first_letter>S</first_letter><first_letter>Y</first_letter><first_letter>T</first_letter></result>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void sgmlQueriesResultsQ8a() {
    final XQuery query = new XQuery(
      "<result> { //section[.//title[contains(., \"is SGML\")]] } </result>",
      ctx);
    query.context(node(file("docs/sgml.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result><section shorttitle=\"What is SGML?\">\n <title>What <emph>is</emph> SGML in the grand scheme of the universe, anyway?</title>\n <intro>\n <para>SGML defines a strict markup scheme with a syntax for defining\n document data elements and an overall framework for marking up\n documents.</para>\n <para>SGML can describe and create documents that are not dependent on\n any hardware, software, formatter, or operating system. Since SGML documents\n conform to an international standard, they are portable.</para></intro></section><section shorttitle=\"How does SGML work?\">\n <title>How is SGML and would you recommend it to your grandmother?</title>\n <intro>\n <para>You can break a typical document into three layers: structure,\n content, and style. SGML works by separating these three aspects and\n deals mainly with the relationship between structure and content.</para></intro>\n <topic topicid=\"top4\">\n <title>Structure</title>\n <para>At the heart of an SGML application is a file called the DTD, or\n Document Type Definition. The DTD sets up the structure of a document,\n much like a database schema describes the types of information it\n handles.</para>\n <para>A database schema also defines the relationships between the\n various types of data. Similarly, a DTD specifies <emph>rules</emph>\n to help ensure documents have a consistent, logical structure.</para></topic>\n <topic topicid=\"top5\">\n <title>Content</title>\n <para>Content is the information itself. The method for identifying\n the information and its meaning within this framework is called\n <emph>tagging</emph>. Tagging must\n conform to the rules established in the DTD (see <xref xrefid=\"top4\"/>).</para>\n <graphic graphname=\"tagexamp\"/></topic>\n <topic topicid=\"top6\">\n <title>Style</title>\n <para>SGML does not standardize style or other processing methods for\n information stored in SGML.</para></topic></section></result>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void sgmlQueriesResultsQ8b() {
    final XQuery query = new XQuery(
      "<result> { //section[.//title/text()[contains(., \"is SGML\")]] } </result>",
      ctx);
    query.context(node(file("docs/sgml.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      (
        assertSerialization("<result><section shorttitle=\"How does SGML work?\">\n <title>How is SGML and would you recommend it to your grandmother?</title>\n <intro>\n <para>You can break a typical document into three layers: structure,\n content, and style. SGML works by separating these three aspects and\n deals mainly with the relationship between structure and content.</para></intro>\n <topic topicid=\"top4\">\n <title>Structure</title>\n <para>At the heart of an SGML application is a file called the DTD, or\n Document Type Definition. The DTD sets up the structure of a document,\n much like a database schema describes the types of information it\n handles.</para>\n <para>A database schema also defines the relationships between the\n various types of data. Similarly, a DTD specifies <emph>rules</emph>\n to help ensure documents have a consistent, logical structure.</para></topic>\n <topic topicid=\"top5\">\n <title>Content</title>\n <para>Content is the information itself. The method for identifying\n the information and its meaning within this framework is called\n <emph>tagging</emph>. Tagging must\n conform to the rules established in the DTD (see <xref xrefid=\"top4\"/>).</para>\n <graphic graphname=\"tagexamp\"/></topic>\n <topic topicid=\"top6\">\n <title>Style</title>\n <para>SGML does not standardize style or other processing methods for\n information stored in SGML.</para></topic></section></result>", false)
      ||
        error("XPST0005")
      )
    );
  }

  /**
.
   */
  @org.junit.Test
  public void sgmlQueriesResultsQ9() {
    final XQuery query = new XQuery(
      "<result> { for $id in //xref/@xrefid return //topic[@topicid = $id] } </result>",
      ctx);
    query.context(node(file("docs/sgml.xml")));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result><topic topicid=\"top4\">\n <title>Structure</title>\n <para>At the heart of an SGML application is a file called the DTD, or\n Document Type Definition. The DTD sets up the structure of a document,\n much like a database schema describes the types of information it\n handles.</para>\n <para>A database schema also defines the relationships between the\n various types of data. Similarly, a DTD specifies <emph>rules</emph>\n to help ensure documents have a consistent, logical structure.</para></topic></result>", false)
    );
  }
}
