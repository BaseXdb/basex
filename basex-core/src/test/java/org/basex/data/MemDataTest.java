package org.basex.data;

import static org.junit.Assert.*;

import java.io.*;

import org.basex.*;
import org.basex.io.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.junit.*;

/**
 * Test index updates when using memory storage ({@link MemData}).
 *
 * @author BaseX Team 2005-20, BSD License
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
  @Before public void setUp() throws IOException {
    data = new DBNode(new IOContent(XML)).data();
    context.openDB(data);
  }

  /** Clean up method; executed after each test. */
  @After public void end() {
    context.closeDB();
    data = null;
  }

  /**
   * Replace value update test.
   */
  @Test public void replaceValue() {
    query("replace value of node /a/b with 'test2'");
    final String o = query("/a/b[text() = 'test']");
    assertTrue("Old node found", o.isEmpty());
    final String n = query("/a/b[text() = 'test2']");
    assertFalse("New node not found", n.isEmpty());
  }

  /**
   * Replace node update test.
   */
  @Test public void replaceNode() {
    query("replace node /a/b with <d f='test2'/>");
    final String o = query("/a/b");
    assertTrue("Old node found", o.isEmpty());
    final String n = query("//d[@f = 'test2']");
    assertFalse("New node not found", n.isEmpty());
  }

  /**
   * Insert node update test.
   */
  @Test public void insertNode() {
    query("insert node <d>test2</d> as first into /a");
    final String r = query("//d[text() = 'test2']");
    assertFalse("Node not found", r.isEmpty());
    query("insert node <d>test2</d> as first into /a");
    final String c = query("count(//d[text() = 'test2'])");
    assertEquals("Second node not found", 2, Integer.parseInt(c));
  }

  /**
   * Insert node update test.
   */
  @Test public void insertDuplicateNode() {
    query("insert node <d>test</d> as first into /a");
    final String r = query("//d[text() = 'test']");
    assertFalse("Node not found", r.isEmpty());
    final String c = query("count(//*[text() = 'test'])");
    assertEquals("Second node not found", 2, Integer.parseInt(c));
  }

  /**
   * Delete node update test.
   */
  @Test public void deleteNode() {
    query("delete node //b");
    final String r = query("//*[text() = 'test']");
    assertTrue("Node not deleted", r.isEmpty());
  }

  /**
   * Try to find non-existing node.
   */
  @Test public void findNonexistingNode() {
    final String r = query("//*[text() = 'test0']");
    assertTrue("Found non-existing node", r.isEmpty());
  }
}
