package org.basex.test;

//import static javax.xml.stream.XMLStreamConstants.*;
import java.io.StringReader;
import java.util.Properties;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQConstants;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;
import javax.xml.xquery.XQSequence;
import javax.xml.xquery.XQStaticContext;
import junit.framework.TestCase;
import org.basex.io.CachedOutput;
import org.basex.test.xqj.TestXMLFilter;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * This class tests the XQJ features.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-07, ISC License
 * @author Christian Gruen
 */
public class XQJTest extends TestCase {
  /** Test file. */
  private final String input = "doc('/home/db/xml/input.xml')";
  /** Driver reference. */
  protected String drv;

  @Before
  @Override
  protected void setUp() {
    drv = "org.basex.api.xqj.BXQDataSource";
  }

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
    final XQConnection conn = conn(drv);
    final XQPreparedExpression expr = conn.prepareExpression(input + "//li");

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
    final XQConnection conn = conn(drv);
    final XQExpression expr = conn.createExpression();
    final XQSequence seq = expr.executeQuery("'Hello World!'");
    final CachedOutput co = new CachedOutput();
    seq.writeSequence(co, new Properties());
    final String str = co.toString().replaceAll("<\\?.*?>", "");
    assertEquals("", "Hello World!", str);
  }

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void test3() throws Exception {
    final XQConnection conn = conn(drv);
    final XQExpression expr = conn.createExpression();
    expr.bindInt(new QName("x"), 21, null);

    final XQSequence result = expr.executeQuery(
       "declare variable $x as xs:integer external; for $i in $x return $i");

    final StringBuilder sb = new StringBuilder();
    while(result.next()) sb.append(result.getItemAsString(null));
    assertEquals("", "21", sb.toString());
  }

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void test4() throws Exception {
    final XQConnection conn = conn(drv);

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
    final XQConnection conn = conn(drv);

    conn.createItemFromString("Hello",
        conn.createAtomicType(XQItemType.XQBASETYPE_NCNAME));
  }

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void test6() throws Exception {
    final XQConnection conn = conn(drv);

    try {
      conn.createItemFromInt(1000,
          conn.createAtomicType(XQItemType.XQBASETYPE_BYTE));
      fail("1000 cannot be converted to byte.");
    } catch(final Exception ex) {
    }
  }

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void test7() throws Exception {
    final XQConnection conn = conn(drv);

    try {
      conn.createItemFromByte((byte) 123,
          conn.createAtomicType(XQItemType.XQBASETYPE_INTEGER));
    } catch(final Exception ex) {
      fail(ex.getMessage());
    }
  }

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void test8() throws Exception {
    final XQConnection conn = conn(drv);
    final XMLReader r = new TestXMLFilter("<e>ha</e>");

    try {
      conn.createItemFromDocument(r, null);
    } catch(final Exception ex) {
      fail(ex.getMessage());
    }
  }

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void test9() throws Exception {
    final XQConnection conn = conn(drv);
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder parser = factory.newDocumentBuilder();
    Document doc = parser.parse(new InputSource(new StringReader("<a>b</a>")));
    
    try {
      conn.createItemFromNode(doc, null);
    } catch(final Exception ex) {
      fail(ex.getMessage());
    }
  }

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void test10() throws Exception {
    final XQConnection conn = conn(drv);
    XQStaticContext sc = conn.getStaticContext();
    sc.setScrollability(XQConstants.SCROLLTYPE_SCROLLABLE);
    
    XQExpression ex = conn.createExpression();
    XQResultSequence seq = ex.executeQuery("1,2,3,4");
    seq.absolute(2);
    assertEquals(2, seq.getPosition());
  }

  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void test11() throws Exception {
    final XQConnection conn = conn(drv);
    XQStaticContext sc = conn.getStaticContext();
    sc.setScrollability(XQConstants.SCROLLTYPE_SCROLLABLE);
    
    XQExpression ex = conn.createExpression();
    String query = "doc('/home/db/projects/basex/input.xml')//title";
    //String query = "1,2";
    XQResultSequence seq = ex.executeQuery(query);
    XMLStreamReader xsr = seq.getSequenceAsStream();
    while(xsr.hasNext()) {
      System.out.println(xsr.next());
    }
  }
 
  /**
   * Test.
   * @throws Exception exception
   */
  @Test
  public void test12() throws Exception {
    final XQConnection conn = conn(drv);
    XQStaticContext sc = conn.getStaticContext();
    sc.setScrollability(XQConstants.SCROLLTYPE_SCROLLABLE);
    
    XQExpression ex = conn.createExpression();
    String query = "1,'haha',2.0e3,2";
    //String query = "1";
    XQResultSequence seq = ex.executeQuery(query);
    XMLStreamReader xsr = seq.getSequenceAsStream();
    
    XMLInputFactory xif = XMLInputFactory.newInstance();
    XMLEventReader xer = xif.createXMLEventReader(xsr);
    
    while(xer.hasNext()) {
      XMLEvent ev = xer.nextEvent();
      if(ev.isStartElement()) {
        StartElement se = ev.asStartElement();
        QName qnm = se.getName();
        System.out.println("TAG: " + qnm);
      } else if(ev.isCharacters()) {
        Characters ch = ev.asCharacters();
        System.out.println("TXT: " + ch.getData());
      } else {
        System.out.println("TYP: " + ev.getEventType());
      }
      //System.out.println(xer.getElementText());
    }
  }
}
