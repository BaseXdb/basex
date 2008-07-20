package org.basex.test.storage;

import static org.basex.util.Token.*;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.DropDB;
import org.basex.data.Data;
import org.basex.data.DiskData;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * This class tests the update features of the Data class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-07, ISC License
 * @author Tim Petrowsky
 */
public final class DataUpdateTestDeletes {
  /** Context. */
  private static final Context CONTEXT = new Context();
  /** Test file size in nodes. */
  private int size;
  /** Data. */
  private Data data;
  /** Test file we do updates with. */
  private static final String TESTFILE = "etc/xml/test.xml";
  /** Test database name. */
  private static final String DBNAME = "DataUpdateTestDeletes";

  /**
   * Delete the test-database.
   */
  @BeforeClass
  public static void setUpBeforeClass() {
    Prop.textindex = false;
    Prop.attrindex = false;
    Prop.chop = true;
  }

  /**
   * Create the database.
   * @throws Exception in case of problems.
   */
  @Before
  public void setUp() throws Exception {
    new CreateDB(TESTFILE, DBNAME).execute(CONTEXT);
    data = CONTEXT.data();
    size = data.size;
  }

  /**
   * Delete the test-database.
   * @throws Exception in case of problems.
   */
  @After
  public void tearDown() throws Exception {
    data.close();
    DropDB.drop(DBNAME);
  }

  /**
   * Reload Data class.
   * @throws Exception in case of problems.
   */
  private void reload() throws Exception {
    data.close();
    data = new DiskData(DBNAME);
  }

  /**
   * Test byte-arrays for equality.
   * @param expected expected value
   * @param actual actual value
   */
  private void assertByteArraysEqual(
      final byte[] expected,
      final byte[] actual) {
    assertEquals("array lengths don't equal", expected.length, actual.length);
    for(int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], actual[i]);
    }
  }

  /**
   * Test for correct data size.
   * @throws Exception in case of problems.
   */
  @Test
  public void testSize() throws Exception {
    assertEquals("Unexpected size!", size, data.size);
    reload();
    assertEquals("Unexpected size!", size, data.size);
  }

  /**
   * Test deletion of a simple node.
   * @throws Exception in case of problems.
   */
  @Test
  public void testSimpleNodeDelete() throws Exception {
    final int oldDocSize = data.size(0, Data.DOC);
    final int oldRootSize = data.size(1, Data.ELEM);
    final int oldParSize = data.size(2, Data.ELEM);
    data.delete(3);
    assertEquals(size - 1, data.size);
    assertByteArraysEqual(token("parentnode"), data.tag(3));
    assertEquals(oldDocSize - 1, data.size(0, Data.DOC));
    assertEquals(oldRootSize - 1, data.size(1, Data.ELEM));
    assertEquals(oldParSize - 1, data.size(2, Data.ELEM));
    assertEquals(1, data.parent(3, Data.ELEM));
    reload();
    assertEquals(size - 1, data.size);
    assertByteArraysEqual(token("parentnode"), data.tag(3));
    assertEquals(oldDocSize - 1, data.size(0, Data.DOC));
    assertEquals(oldRootSize - 1, data.size(1, Data.ELEM));
    assertEquals(oldParSize - 1, data.size(2, Data.ELEM));
    assertEquals(1, data.parent(3, Data.ELEM));
  }

  /**
   * Test deletion of a node with a child.
   * @throws Exception in case of problems.
   */
  @Test
  public void testCascadingDelete() throws Exception {
    final int oldDocSize = data.size(0, Data.DOC);
    final int oldRootSize = data.size(1, Data.ELEM);
    data.delete(2);
    assertEquals(size - 2, data.size);
    assertByteArraysEqual(token("parentnode"), data.tag(2));
    assertEquals(oldDocSize - 2, data.size(0, Data.DOC));
    assertEquals(oldRootSize - 2, data.size(1, Data.ELEM));
    assertEquals(1, data.parent(2, Data.ELEM));
    reload();
    assertEquals(size - 2, data.size);
    assertByteArraysEqual(token("parentnode"), data.tag(2));
    assertEquals(oldDocSize - 2, data.size(0, Data.DOC));
    assertEquals(oldRootSize - 2, data.size(1, Data.ELEM));
    assertEquals(1, data.parent(2, Data.ELEM));
  }

  /**
   * Test deletion of a node with a child (with text) and attribute.
   * @throws Exception in case of problems.
   */
  @Test
  public void testCascadingDelete2() throws Exception {
    final int oldDocSize = data.size(0, Data.DOC);
    final int oldRootSize = data.size(1, Data.ELEM);
    final int oldParentSize = data.size(4, Data.ELEM);
    data.delete(6);
    assertEquals(size - 5, data.size);
    assertByteArraysEqual(token("b"),         data.tag(6));
    assertEquals(oldDocSize - 5, data.size(0, Data.DOC));
    assertEquals(oldRootSize - 5, data.size(1, Data.ELEM));
    assertEquals(oldParentSize - 5, data.size(4, Data.ELEM));
    assertEquals(2, data.parent(3, Data.ELEM));
    reload();
    assertEquals(size - 5, data.size);
    assertByteArraysEqual(token("b"), data.tag(6));
    assertEquals(oldDocSize - 5, data.size(0, Data.DOC));
    assertEquals(oldRootSize - 5, data.size(1, Data.ELEM));
    assertEquals(oldParentSize - 5, data.size(4, Data.ELEM));
    assertEquals(2, data.parent(3, Data.ELEM));
  }

  /**
   * Test deletion of an attribute.
   * @throws Exception in case of problems.
   */
  @Test
  public void testDeleteAttribute() throws Exception {
    final int oldRootSize = data.size(1, Data.ELEM);
    final int oldParentSize = data.size(6, Data.ELEM);
    data.delete(7);
    assertEquals(size - 1, data.size);
    assertByteArraysEqual(token("contextnode"), data.tag(6));
    assertByteArraysEqual(token("id"), data.attName(7));
    assertEquals(oldRootSize - 1, data.size(1, Data.ELEM));
    assertEquals(oldParentSize - 1, data.size(6, Data.ELEM));
    assertEquals(6, data.parent(7, Data.ATTR));
    assertEquals(4, data.size(6, Data.ELEM));
    reload();
    assertEquals(size - 1, data.size);
    assertByteArraysEqual(token("contextnode"), data.tag(6));
    assertByteArraysEqual(token("id"), data.attName(7));
    assertEquals(oldRootSize - 1, data.size(1, Data.ELEM));
    assertEquals(oldParentSize - 1, data.size(6, Data.ELEM));
    assertEquals(6, data.parent(7, Data.ATTR));
    assertEquals(4, data.size(6, Data.ELEM));
  }

  /**
   * For the sake of completeness.
   * Test deletion of a text-node.
   * @throws Exception in case of problems.
   */
  @Test
  public void testDeleteText() throws Exception {
    data.delete(10);
    assertEquals(size - 1, data.size);
    reload();
    assertEquals(size - 1, data.size);
  }
}
