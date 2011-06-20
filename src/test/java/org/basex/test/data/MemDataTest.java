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

/** Test index updates when using memory storage ({@link MemData}). */
public final class MemDataTest {
  /** XML document. */
  private static final byte[] XML = Token.token("<a><b>test</b><c/></a>");
  /** Database context. */
  private static final Context CTX = new Context();
  /** Tested {@link MemData} instance. */
  private Data data;

  /**
   * Set up method; executed before each test.
   * @throws IOException should never be thrown
   */
  @Before
  public void setUp() throws IOException {
    data = CreateDB.xml(new IOContent(XML), CTX);
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
  public void testReplaceValue() throws BaseXException {
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
  public void testReplaceNode() throws BaseXException {
    new XQuery("replace node /a/b with <d f='test4'/>").execute(CTX);
    final String o = new XQuery("/a/b").execute(CTX);
    assertTrue("Old node found", o.length() == 0);
    final String n = new XQuery("//d[@f = 'test4']").execute(CTX);
    assertTrue("New node not found", n.length() > 0);
  }

  /**
   * Insert node update test.
   * @throws BaseXException query exception
   */
  @Test
  public void testInsertNode() throws BaseXException {
    new XQuery("insert node <d>test3</d> as first into /a").execute(CTX);
    final String r = new XQuery("//*[text() = 'test3']").execute(CTX);
    assertTrue("Node not found", r.length() > 0);
  }

  /**
   * Delete node update test.
   * @throws BaseXException query exception
   */
  @Test
  public void testDeleteNode() throws BaseXException {
    new XQuery("delete node //b").execute(CTX);
    final String r = new XQuery("//*[text() = 'test']").execute(CTX);
    assertTrue("Node not deleted", r.length() == 0);
  }

  /**
   * Try to find non-existing node.
   * @throws BaseXException query exception
   */
  @Test
  public void testFindNonexistingNode() throws BaseXException {
    final String r = new XQuery("//*[text() = 'test1']").execute(CTX);
    assertTrue("Found non-existing node", r.length() == 0);
  }
}
