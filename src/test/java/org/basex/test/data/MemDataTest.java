package org.basex.test.data;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.XQuery;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.io.IOContent;
import org.basex.util.Token;

/**
 * Test index updates when using memory storage ({@link MemData}).
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Dimitar Popov
 */
public class MemDataTest {
  /** XML document. */
  static final String XMLSTR = "<a><b>test</b><c/><f>test1</f><f>test3</f></a>";
  /** XML document. */
  private static final byte[] XML = Token.token(XMLSTR);
  /** Database context. */
  protected static final Context CTX = new Context();
  /** Tested {@link MemData} instance. */
  private Data data;

  /**
   * Set up method; executed before each test.
   * @throws IOException should never be thrown
   */
  @Before
  public void setUp() throws IOException {
    data = CreateDB.mainMem(new IOContent(XML), CTX);
    CTX.openDB(data);
  }

  /** Clean up method; executed after each test. */
  @After
  public void end() {
    CTX.closeDB();
    data = null;
  }

  /**
   * Replace value update test.
   * @throws BaseXException query exception
   */
  @Test
  public void replaceValue() throws BaseXException {
    new XQuery("replace value of node /a/b with 'test2'").execute(CTX);
    final String o = new XQuery("/a/b[text() = 'test']").execute(CTX);
    assertTrue("Old node found", o.length() == 0);
    final String n = new XQuery("/a/b[text() = 'test2']").execute(CTX);
    assertTrue("New node not found", n.length() > 0);
  }

  /**
   * Replace node update test.
   * @throws BaseXException query exception
   */
  @Test
  public void replaceNode() throws BaseXException {
    new XQuery("replace node /a/b with <d f='test2'/>").execute(CTX);
    final String o = new XQuery("/a/b").execute(CTX);
    assertTrue("Old node found", o.length() == 0);
    final String n = new XQuery("//d[@f = 'test2']").execute(CTX);
    assertTrue("New node not found", n.length() > 0);
  }

  /**
   * Insert node update test.
   * @throws BaseXException query exception
   */
  @Test
  public void insertNode() throws BaseXException {
    new XQuery("insert node <d>test2</d> as first into /a").execute(CTX);
    final String r = new XQuery("//d[text() = 'test2']").execute(CTX);
    assertTrue("Node not found", r.length() > 0);
    new XQuery("insert node <d>test2</d> as first into /a").execute(CTX);
    final String c = new XQuery("count(//d[text() = 'test2'])").execute(CTX);
    assertTrue("Second node not found", 2 == Integer.parseInt(c));
  }


  /**
   * Insert node update test.
   * @throws BaseXException query exception
   */
  @Test
  public void insertDuplicateNode() throws BaseXException {
    new XQuery("insert node <d>test</d> as first into /a").execute(CTX);
    final String r = new XQuery("//d[text() = 'test']").execute(CTX);
    assertTrue("Node not found", r.length() > 0);
    final String c = new XQuery("count(//*[text() = 'test'])").execute(CTX);
    assertTrue("Second node not found", 2 == Integer.parseInt(c));
  }

  /**
   * Delete node update test.
   * @throws BaseXException query exception
   */
  @Test
  public void deleteNode() throws BaseXException {
    new XQuery("delete node //b").execute(CTX);
    final String r = new XQuery("//*[text() = 'test']").execute(CTX);
    assertTrue("Node not deleted", r.length() == 0);
  }

  /**
   * Try to find non-existing node.
   * @throws BaseXException query exception
   */
  @Test
  public void findNonexistingNode() throws BaseXException {
    final String r = new XQuery("//*[text() = 'test0']").execute(CTX);
    assertTrue("Found non-existing node", r.length() == 0);
  }
}
