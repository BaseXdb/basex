package org.basex.test.api.xqj;

import static org.junit.Assert.*;

import java.io.*;
import java.math.*;
import java.util.*;

import javax.xml.namespace.*;
import javax.xml.parsers.*;
import javax.xml.stream.*;
import javax.xml.stream.events.*;
import javax.xml.transform.stream.*;
import javax.xml.xquery.*;

import org.basex.io.out.*;
import org.basex.util.*;
import org.junit.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * This class tests some XQJ features (arbitrary samples).
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class XQJTest {
  /** Driver reference. */
  private static final String DRIVER = "org.basex.api.xqj.BXQDataSource";

  /**
   * Creates and returns a connection to the specified driver.
   * @param drv driver
   * @return connection
   * @throws Exception exception
   */
  private static XQConnection conn(final String drv) throws Exception {
    return ((XQDataSource) Class.forName(drv).newInstance()).getConnection();
  }

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void test1() throws Exception {
    final XQConnection conn = conn(DRIVER);
    final XQPreparedExpression expr = conn.prepareExpression(
        "doc('src/test/resources/input.xml')//li");

    // query execution
    final XQResultSequence result = expr.executeQuery();

    // output of result sequence
    final StringBuilder sb = new StringBuilder();
    while(result.next()) sb.append(result.getItemAsString(null));
    assertEquals("", "<li>Exercise 1</li><li>Exercise 2</li>", sb.toString());
  }

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void test2() throws Exception {
    final XQConnection conn = conn(DRIVER);
    final XQExpression expr = conn.createExpression();
    final XQSequence seq = expr.executeQuery("'Hello World!'");
    final ArrayOutput ao = new ArrayOutput();
    seq.writeSequence(ao, new Properties());
    final String str = ao.toString().replaceAll("<\\?.*?>", "");
    assertEquals("", "Hello World!", str);
  }

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void test4() throws Exception {
    final XQConnection conn = conn(DRIVER);

    final XQPreparedExpression expr = conn.prepareExpression(
        "declare variable $i as xs:integer external; $i");
    expr.bindInt(new QName("i"), 21, null);
    final XQSequence result = expr.executeQuery();

    final StringBuilder sb = new StringBuilder();
    while(result.next()) sb.append(result.getItemAsString(null));
    assertEquals("", "21", sb.toString());
  }

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void test5() throws Exception {
    final XQConnection conn = conn(DRIVER);

    conn.createItemFromString("Hello",
        conn.createAtomicType(XQItemType.XQBASETYPE_NCNAME));
  }

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void test6() throws Exception {
    final XQConnection conn = conn(DRIVER);

    try {
      conn.createItemFromInt(1000,
          conn.createAtomicType(XQItemType.XQBASETYPE_BYTE));
      fail("1000 cannot be converted to byte.");
    } catch(final Exception ex) { /* ignored */
    }
  }

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void test7() throws Exception {
    final XQConnection conn = conn(DRIVER);

    try {
      conn.createItemFromByte((byte) 123,
          conn.createAtomicType(XQItemType.XQBASETYPE_INTEGER));
    } catch(final Exception ex) {
      fail(Util.message(ex));
    }
  }

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void test9() throws Exception {
    final XQConnection conn = conn(DRIVER);
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    final DocumentBuilder parser = factory.newDocumentBuilder();
    final Document doc = parser.parse(
        new InputSource(new StringReader("<a>b</a>")));

    try {
      conn.createItemFromNode(doc, null);
    } catch(final Exception ex) {
      fail(Util.message(ex));
    }
  }

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void test10() throws Exception {
    final XQConnection conn = conn(DRIVER);
    final XQStaticContext sc = conn.getStaticContext();
    sc.setScrollability(XQConstants.SCROLLTYPE_SCROLLABLE);

    final XQExpression ex = conn.createExpression();
    final XQResultSequence seq = ex.executeQuery("1,2,3,4");
    seq.absolute(2);
    assertEquals(2, seq.getPosition());
  }

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void test11() throws Exception {
    final XQConnection conn = conn(DRIVER);
    final XQStaticContext sc = conn.getStaticContext();
    sc.setScrollability(XQConstants.SCROLLTYPE_SCROLLABLE);

    final XQExpression ex = conn.createExpression();
    final String query = "doc('src/test/resources/input.xml')//title";
    //String query = "1,2";
    final XQResultSequence seq = ex.executeQuery(query);
    final XMLStreamReader xsr = seq.getSequenceAsStream();
    while(xsr.hasNext()) xsr.next();
  }

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void test12() throws Exception {
    final XQConnection conn = conn(DRIVER);
    final XQStaticContext sc = conn.getStaticContext();
    sc.setScrollability(XQConstants.SCROLLTYPE_SCROLLABLE);

    final XQExpression ex = conn.createExpression();
    final String query = "1,'haha',2.0e3,2";
    final XQResultSequence seq = ex.executeQuery(query);
    final XMLStreamReader xsr = seq.getSequenceAsStream();

    final XMLInputFactory xif = XMLInputFactory.newInstance();
    final XMLEventReader xer = xif.createXMLEventReader(xsr);

    while(xer.hasNext()) {
      final XMLEvent ev = xer.nextEvent();
      if(ev.isStartElement()) {
        final StartElement se = ev.asStartElement();
        se.getName();
      }
    }
  }

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void test13() throws Exception {
    final XQConnection conn = conn(DRIVER);
    final XQExpression expr = conn.createExpression();

    final XQResultSequence seq = expr.executeQuery("<H><K/><K/></H>");
    seq.next();
    seq.getSequenceAsString(null);
  }

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void test14() throws Exception {
    final XQConnection conn = conn(DRIVER);
    final XQExpression expr = conn.createExpression();

    final XQResultSequence seq = expr.executeQuery("1,'test'");
    final StringWriter sw = new StringWriter();
    seq.next();
    seq.writeItemToResult(new StreamResult(sw));
    seq.next();
    seq.writeItemToResult(new StreamResult(sw));

    assertEquals("1test", sw.toString());
  }

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void test15() throws Exception {
    final XQConnection conn = conn(DRIVER);
    final XQExpression expr = conn.createExpression();

    final String query = "<H><K>B</K></H>";
    final XQResultSequence seq = expr.executeQuery(query);
    seq.next();
    final TestContentHandler result = new TestContentHandler();
    seq.writeItemToSAX(result);

    assertEquals(query, result.buffer.toString());
  }

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void test16() throws Exception {
    final XQConnection conn = conn(DRIVER);
    final XQExpression expr = conn.createExpression();

    final String query = "<ee>Hello world!</ee>";
    final XQItem item = conn.createItemFromDocument(
        expr.executeQuery(query).getSequenceAsStream(), null);

    assertEquals(query, item.getItemAsString(null));
  }

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void test19() throws Exception {
    final XQConnection conn = conn(DRIVER);

    try {
      final XQPreparedExpression expr = conn.prepareExpression(
        "declare variable $v external; $v");

      expr.bindInt(new QName("v"), 123, null);
      final XQResultSequence result = expr.executeQuery();
      result.next();
      assertEquals(123, result.getInt());
    } catch(final XQException ex) {
      fail(Util.message(ex));
    }
  }

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void test20() throws Exception {
    final XQConnection conn = conn(DRIVER);
    final XQExpression expr = conn.createExpression();

    final XQResultSequence result = expr.executeQuery("'Hello world!'");
    result.isScrollable();
    expr.close();
  }

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void test21() throws Exception {
    final XQConnection conn = conn(DRIVER);
    final XQItemType elm = conn.createAtomicType(XQItemType.XQBASETYPE_INTEGER);
    elm.getItemOccurrence();
  }

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void test22() throws Exception {
    final XQConnection conn = conn(DRIVER);
    final XQStaticContext xqsc = conn.getStaticContext();
    xqsc.setScrollability(XQConstants.SCROLLTYPE_SCROLLABLE);
    final XQExpression expr = conn.createExpression();
    final XQResultSequence seq = expr.executeQuery("1,2,3,4");
    seq.afterLast();
    seq.relative(-4);
    seq.relative(-1);
  }

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void test23() throws Exception {
    final XQConnection conn = conn(DRIVER);
    final XQItemType expr = conn.createItemType();
    try {
      expr.getTypeName();
      fail("Test should fail");
    } catch(final Exception ex) { /* ignored */
    }
  }

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void test25() throws Exception {
    final XQConnection conn = conn(DRIVER);

    final Object[] objects = {
      true, (byte) 2, 3f, (double) 4, 5, (long) 6, (short) 7, "8",
      new BigDecimal(9), new BigInteger("10"), new QName("elf"),
    };

    for(final Object o : objects) {
      for(int t = 1; t <= 51; ++t) {
        try {
          conn.createItemFromObject(o, conn.createAtomicType(t));
        } catch(final Exception ex) { /* ignored */
        }
      }
    }
  }

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void test32() throws Exception {
    final XQConnection conn = conn(DRIVER);
    final XQExpression xqe = conn.createExpression();
    XQSequence xqs;

    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    final DocumentBuilder parser = factory.newDocumentBuilder();
    final Document document = parser.parse(new InputSource(
        new StringReader("<e>Hello world!</e>")));
    final DocumentFragment frag = document.createDocumentFragment();
    final Element el1 = document.createElement("A");
    final Element el2 = document.createElement("B");
    frag.appendChild(el1);
    frag.appendChild(el2);

    //xqe.bindNode(new QName("v"), frag, null);
    xqe.bindNode(new QName("v"), document, null);
    xqs = xqe.executeQuery("declare variable $v external; $v");
    while(xqs.next()) xqs.getNode();
    conn.close();
  }

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void test34() throws Exception {
    final XQConnection conn = conn(DRIVER);
    final XQPreparedExpression expr = conn.prepareExpression(
        "declare variable $v external; $v");

    final XQItemType type = conn.createAtomicType(
        XQItemType.XQBASETYPE_STRING);
    expr.bindAtomicValue(new QName("v"), "A", type);
    conn.close();
  }

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void contextItem() throws Exception {
    final XQConnection conn = conn(DRIVER);
    conn.getStaticContext().setScrollability(XQConstants.SCROLLTYPE_SCROLLABLE);

    XQExpression xqe = conn.createExpression();
    final XQSequence xqs = xqe.executeQuery("doc('src/test/resources/input.xml')");
    xqs.first();
    final XQItem xqi = xqs.getItem();

    xqe = conn.createExpression();
    xqe.bindItem(new QName("v"), xqi);
    xqe.executeQuery("declare variable $v external; $v");
    conn.close();
  }

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void exec() throws Exception {
    final XQConnection conn = conn(DRIVER);
    final XQExpression expr = conn.createExpression();
    try {
      expr.executeCommand("info db");
      fail("Error expected: no database opened.");
    } catch(final XQException ex) { /* ignored */
    }
    expr.executeCommand("create db tmp src/test/resources/input.xml");
    expr.executeCommand("info db");
    expr.executeCommand("drop db tmp");
    expr.executeCommand("close");
  }

  /**
   * Test (mailing list, Jul 10).
   * @throws Exception exception
   */
  @Test
  public void createVar() throws Exception {
    final XQConnection conn = conn(DRIVER);
    final XQExpression expr = conn.createExpression();
    expr.executeQuery("declare variable $x := 1; $x");
    conn.close();
  }

  /**
   * Test (Sourceforge #2937184); bind context item.
   * @throws Exception exception
   */
  @Test
  public void context() throws Exception {
    final XQConnection conn = conn(DRIVER);
    final XQExpression expr = conn.createExpression();
    XQResultSequence result =
      expr.executeQuery("doc('src/test/resources/input.xml')//title/text()");
    result.next();
    final XQItem item = conn.createItem(result.getItem());

    // bind
    final XQPreparedExpression pe = conn.prepareExpression(".");
    pe.bindItem(XQConstants.CONTEXT_ITEM, item);
    result = pe.executeQuery();
    result.next();
    assertEquals("XML", result.getItemAsString(null));
  }

  /**
   * Bind variable before parsing the query (1).
   * @throws Exception exception
   */
  @Test
  public void bind() throws Exception {
    final XQConnection conn = conn(DRIVER);
    final XQExpression expr = conn.createExpression();
    expr.bindInt(new QName("x"), 21, null);

    final XQSequence result = expr.executeQuery(
       "declare variable $x external; $x");
    result.next();
    assertEquals("21", result.getItemAsString(null));
  }

  /**
   * Bind variable before parsing the query (2).
   * @throws Exception exception
   */
  @Test
  public void bindWithType() throws Exception {
    final XQConnection conn = conn(DRIVER);
    final XQExpression expr = conn.createExpression();
    expr.bindInt(new QName("x"), 21, null);
    expr.executeQuery("declare variable $x as xs:integer external; $x");
  }

  /**
   * Bind variable before parsing the query, using a wrong type.
   * @throws Exception exception
   */
  @Test
  public void bindTwice() throws Exception {
    final XQConnection conn = conn(DRIVER);
    final XQExpression expr = conn.createExpression();
    XQResultSequence xqs;

    try {
      expr.bindInt(new QName("v"), 1, null);
      xqs = expr.executeQuery("declare variable $v external; $v");
      xqs.next();
      assertEquals(1, xqs.getInt());

      expr.bindInt(new QName("v"), 2, null);
      xqs = expr.executeQuery("declare variable $v external; $v");
      xqs.next();
      assertEquals(2, xqs.getInt());
    } catch(final XQException ex) { /* ignored */
      fail(Util.message(ex));
    }
  }

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void testStatic() throws Exception {
    final XQConnection conn = conn(DRIVER);
    final XQStaticContext xqs = conn.getStaticContext();
    xqs.declareNamespace("p", "u");
    conn.setStaticContext(xqs);
    final XQPreparedExpression xqps = conn.prepareExpression("<p:e/>");
    xqps.executeQuery();
    xqps.close();
  }
}
