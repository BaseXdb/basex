package org.basex.test.data;

import static org.junit.Assert.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.test.*;
import org.basex.util.*;
import org.junit.*;

/**
 * Test index updates when using memory storage ({@link MemData}).
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Dimitar Popov
 */
public class MemDataTest extends SandboxTest {
  /** XML document. */
  static final String XMLSTR = "<a><b>test</b><c/><f>test1</f><f>test3</f></a>";
  /** XML document. */
  private static final byte[] XML = Token.token(XMLSTR);
  /** Tested {@link MemData} instance. */
  private Data data;

  /**
   * Set up method; executed before each test.
   * @throws IOException should never be thrown
   */
  @Before
  public void setUp() throws IOException {
    data = CreateDB.mainMem(new IOContent(XML), context);
    context.openDB(data);
  }

  /** Clean up method; executed after each test. */
  @After
  public void end() {
    context.closeDB();
    data = null;
  }

  /**
   * Replace value update test.
   * @throws BaseXException query exception
   */
  @Test
  public void replaceValue() throws BaseXException {
    new XQuery("replace value of node /a/b with 'test2'").execute(context);
    final String o = new XQuery("/a/b[text() = 'test']").execute(context);
    assertTrue("Old node found", o.isEmpty());
    final String n = new XQuery("/a/b[text() = 'test2']").execute(context);
    assertFalse("New node not found", n.isEmpty());
  }

  /**
   * Replace node update test.
   * @throws BaseXException query exception
   */
  @Test
  public void replaceNode() throws BaseXException {
    new XQuery("replace node /a/b with <d f='test2'/>").execute(context);
    final String o = new XQuery("/a/b").execute(context);
    assertTrue("Old node found", o.isEmpty());
    final String n = new XQuery("//d[@f = 'test2']").execute(context);
    assertFalse("New node not found", n.isEmpty());
  }

  /**
   * Insert node update test.
   * @throws BaseXException query exception
   */
  @Test
  public void insertNode() throws BaseXException {
    new XQuery("insert node <d>test2</d> as first into /a").execute(context);
    final String r = new XQuery("//d[text() = 'test2']").execute(context);
    assertFalse("Node not found", r.isEmpty());
    new XQuery("insert node <d>test2</d> as first into /a").execute(context);
    final String c = new XQuery("count(//d[text() = 'test2'])").execute(context);
    assertEquals("Second node not found", 2, Integer.parseInt(c));
  }


  /**
   * Insert node update test.
   * @throws BaseXException query exception
   */
  @Test
  public void insertDuplicateNode() throws BaseXException {
    new XQuery("insert node <d>test</d> as first into /a").execute(context);
    final String r = new XQuery("//d[text() = 'test']").execute(context);
    assertFalse("Node not found", r.isEmpty());
    final String c = new XQuery("count(//*[text() = 'test'])").execute(context);
    assertEquals("Second node not found", 2, Integer.parseInt(c));
  }

  /**
   * Delete node update test.
   * @throws BaseXException query exception
   */
  @Test
  public void deleteNode() throws BaseXException {
    new XQuery("delete node //b").execute(context);
    final String r = new XQuery("//*[text() = 'test']").execute(context);
    assertTrue("Node not deleted", r.isEmpty());
  }

  /**
   * Try to find non-existing node.
   * @throws BaseXException query exception
   */
  @Test
  public void findNonexistingNode() throws BaseXException {
    final String r = new XQuery("//*[text() = 'test0']").execute(context);
    assertTrue("Found non-existing node", r.isEmpty());
  }
}
