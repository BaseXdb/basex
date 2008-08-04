package org.basex.test;

import java.util.Properties;
import javax.xml.namespace.QName;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;
import javax.xml.xquery.XQSequence;
import junit.framework.TestCase;
import org.basex.io.CachedOutput;
import org.junit.Before;
import org.junit.Test;

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

    final XQExpression expr = conn.createExpression();
    final XQSequence seq = expr.executeQuery("100.1");
    seq.next();
    System.out.println(seq.getByte());
  }
}
