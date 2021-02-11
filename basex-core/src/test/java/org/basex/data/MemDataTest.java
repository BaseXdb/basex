package org.basex.data;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.*;
import org.basex.io.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Test index updates when using memory storage ({@link MemData}).
 *
 * @author BaseX Team 2005-21, BSD License
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
  @BeforeEach public void setUp() throws IOException {
    data = new DBNode(new IOContent(XML)).data();
    context.openDB(data);
  }

  /** Clean up method; executed after each test. */
  @AfterEach public void end() {
    context.closeDB();
    data = null;
  }

  /**
   * Replace value update test.
   */
  @Test public void replaceValue() {
    query("replace value of node /a/b with 'test2'");
    final String o = query("/a/b[text() = 'test']");
    assertTrue(o.isEmpty(), "Old node found");
    final String n = query("/a/b[text() = 'test2']");
    assertFalse(n.isEmpty(), "New node not found");
  }

  /**
   * Replace node update test.
   */
  @Test public void replaceNode() {
    query("replace node /a/b with <d f='test2'/>");
    final String o = query("/a/b");
    assertTrue(o.isEmpty(), "Old node found");
    final String n = query("//d[@f = 'test2']");
    assertFalse(n.isEmpty(), "New node not found");
  }

  /**
   * Insert node update test.
   */
  @Test public void insertNode() {
    query("insert node <d>test2</d> as first into /a");
    final String r = query("//d[text() = 'test2']");
    assertFalse(r.isEmpty(), "Node not found");
    query("insert node <d>test2</d> as first into /a");
    final String c = query("count(//d[text() = 'test2'])");
    assertEquals(2, Integer.parseInt(c), "Second node not found");
  }

  /**
   * Insert node update test.
   */
  @Test public void insertDuplicateNode() {
    query("insert node <d>test</d> as first into /a");
    final String r = query("//d[text() = 'test']");
    assertFalse(r.isEmpty(), "Node not found");
    final String c = query("count(//*[text() = 'test'])");
    assertEquals(2, Integer.parseInt(c), "Second node not found");
  }

  /**
   * Delete node update test.
   */
  @Test public void deleteNode() {
    query("delete node //b");
    final String r = query("//*[text() = 'test']");
    assertTrue(r.isEmpty(), "Node not deleted");
  }

  /**
   * Try to find non-existing node.
   */
  @Test public void findNonexistingNode() {
    final String r = query("//*[text() = 'test0']");
    assertTrue(r.isEmpty(), "Found non-existing node");
  }
}
