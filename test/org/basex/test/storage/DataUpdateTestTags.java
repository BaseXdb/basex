package org.basex.test.storage;

import static org.basex.util.Token.*;
import org.basex.core.Prop;
import org.basex.core.proc.Create;
import org.basex.core.proc.Drop;
import org.basex.data.Data;
import org.basex.data.DiskData;
import org.basex.io.IO;
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
public final class DataUpdateTestTags {
  /** Test file size in nodes. */
  private int size;
  /** Data. */
  private Data data;
  /** Test file we do updates with. */
  private static final String TESTFILE = "etc/xml/test.xml";
  /** Test database name. */
  private static final String DBNAME = "DataUpdateTestTags";

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
   * @throws java.lang.Exception in case of problems.
   */
  @Before
  public void setUp() throws Exception {
    data = Create.xml(new IO(TESTFILE), DBNAME);
    size = data.size;
  }

  /**
   * Delete the test-database.
   * @throws java.lang.Exception in case of problems.
   */
  @After
  public void tearDown() throws Exception {
    data.close();
    Drop.drop(DBNAME);
  }

  /**
   * Reload Data class.
   * @throws java.lang.Exception in case of problems.
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
   * @throws java.lang.Exception in case of problems.
   */
  @Test
  public void testSize() throws Exception {
    assertEquals("Unexpected size!", size, data.size);
    reload();
    assertEquals("Unexpected size!", size, data.size);
  }

  /**
   * Test insert as last child.
   * @throws java.lang.Exception in case of problems.
   */
  @Test
  public void testInsertTagAsOnly1() throws Exception {
    final long nextid = data.meta.lastid;
    insertTag(3, 0, token("junit"), Data.ELEM);
    assertEquals(size + 1, data.size);
    assertEquals(3, data.parent(4, Data.ELEM));
    assertEquals((int) Data.ATTR, data.kind(9));
    assertEquals((int) Data.ELEM, data.kind(5));
    assertEquals(1, data.parent(5, Data.ELEM));
    assertEquals(5, data.parent(6, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    assertByteArraysEqual(token("junit"), data.tag(4));
    reload();
    assertEquals(size + 1, data.size);
    assertEquals(3, data.parent(4, Data.ELEM));
    assertEquals((int) Data.ATTR, data.kind(9));
    assertEquals(1, data.parent(5, Data.ELEM));
    assertEquals(5, data.parent(6, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    assertByteArraysEqual(token("junit"), data.tag(4));
  }

  /**
   * Test insert as last child.
   * @throws java.lang.Exception in case of problems.
   */
  @Test
  public void testInsertTagAsOnly2() throws Exception {
    final long nextid = data.meta.lastid;
    insertTag(3, 1, token("junit"), Data.ELEM);
    assertEquals(size + 1, data.size);
    assertEquals(3, data.parent(4, Data.ELEM));
    assertEquals((int) Data.ATTR, data.kind(9));
    assertEquals((int) Data.ELEM, data.kind(5));
    assertEquals(1, data.parent(5, Data.ELEM));
    assertEquals(5, data.parent(6, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    assertByteArraysEqual(token("junit"), data.tag(4));
    reload();
    assertEquals(size + 1, data.size);
    assertEquals(3, data.parent(4, Data.ELEM));
    assertEquals((int) Data.ATTR, data.kind(9));
    assertEquals(1, data.parent(5, Data.ELEM));
    assertEquals(5, data.parent(6, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    assertByteArraysEqual(token("junit"), data.tag(4));
  }

  /**
   * Test insert as last child.
   * @throws java.lang.Exception in case of problems.
   */
  @Test
  public void testInsertTagAsOnly3() throws Exception {
    final long nextid = data.meta.lastid;
    insertTag(3, 2, token("junit"), Data.ELEM);
    assertEquals(size + 1, data.size);
    assertEquals(3, data.parent(4, Data.ELEM));
    assertEquals((int) Data.ATTR, data.kind(9));
    assertEquals((int) Data.ELEM, data.kind(5));
    assertEquals(1, data.parent(5, Data.ELEM));
    assertEquals(5, data.parent(6, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    assertByteArraysEqual(token("junit"), data.tag(4));
    reload();
    assertEquals(size + 1, data.size);
    assertEquals(3, data.parent(4, Data.ELEM));
    assertEquals((int) Data.ATTR, data.kind(9));
    assertEquals(1, data.parent(5, Data.ELEM));
    assertEquals(5, data.parent(6, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    assertByteArraysEqual(token("junit"), data.tag(4));
  }

  /**
   * Test insert as last child.
   * @throws java.lang.Exception in case of problems.
   */
  @Test
  public void testInsertTagAfterAttsAsFirst() throws Exception {
    final long nextid = data.meta.lastid;
    insertTag(6, 1, token("junit"), Data.ELEM);
    assertEquals(size + 1, data.size);
    assertEquals((int) Data.ELEM, data.kind(9));
    assertEquals(6, data.parent(9, Data.ELEM));
    assertByteArraysEqual(token("junit"), data.tag(9));
    assertEquals(6, data.parent(10, Data.ELEM));
    assertEquals(4, data.parent(12, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    reload();
    assertEquals(size + 1, data.size);
    assertEquals((int) Data.ELEM, data.kind(9));
    assertEquals(6, data.parent(9, Data.ELEM));
    assertByteArraysEqual(token("junit"), data.tag(9));
    assertEquals(6, data.parent(10, Data.ELEM));
    assertEquals(4, data.parent(12, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
  }

  /**
   * Test insert as last child.
   * @throws java.lang.Exception in case of problems.
   */
  @Test
  public void testInsertTagAfterAttsAsSecond() throws Exception {
    final long nextid = data.meta.lastid;
    insertTag(6, 2, token("junit"), Data.ELEM);
    assertEquals(size + 1, data.size);
    assertEquals((int) Data.ELEM, data.kind(9));
    assertByteArraysEqual(token("junit"), data.tag(11));
    assertEquals(6, data.parent(11, Data.ELEM));
    assertEquals(6, data.parent(9, Data.ELEM));
    assertEquals(4, data.parent(12, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);

    reload();
    assertEquals(size + 1, data.size);
    assertEquals((int) Data.ELEM, data.kind(9));
    assertByteArraysEqual(token("junit"), data.tag(11));
    assertEquals(6, data.parent(11, Data.ELEM));
    assertEquals(6, data.parent(9, Data.ELEM));
    assertEquals(4, data.parent(12, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
  }

  /**
   * Test insert as last child.
   * @throws java.lang.Exception in case of problems.
   */
  @Test
  public void testInsertTagAfterAttsAsLast() throws Exception {
    final long nextid = data.meta.lastid;
    insertTag(6, 0, token("junit"), Data.ELEM);
    assertEquals(size + 1, data.size);
    assertEquals((int) Data.ELEM, data.kind(9));
    assertByteArraysEqual(token("junit"), data.tag(11));
    assertEquals(6, data.parent(11, Data.ELEM));
    assertEquals(6, data.parent(9, Data.ELEM));
    assertEquals(4, data.parent(12, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    reload();
    assertEquals(size + 1, data.size);
    assertEquals((int) Data.ELEM, data.kind(9));
    assertByteArraysEqual(token("junit"), data.tag(11));
    assertEquals(6, data.parent(11, Data.ELEM));
    assertEquals(6, data.parent(9, Data.ELEM));
    assertEquals(4, data.parent(12, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
  }

  /**
   * Test updateTagName.
   * @throws Exception in case of problems
   */
  @Test
  public void testUpdateTagName() throws Exception {
    data.update(6, token("JUnit"));
    assertEquals((int) Data.ELEM, data.kind(6));
    assertByteArraysEqual(token("JUnit"), data.tag(6));
    reload();
    assertEquals((int) Data.ELEM, data.kind(6));
    assertByteArraysEqual(token("JUnit"), data.tag(6));
  }

  /**
   * Don't remove.
   *
   */
  @Test
  public void foo() {
    return;
  }

  /**
   * Inserts a tag.
   * @param par parent node
   * @param pos inserting position
   * @param name tag name
   * @param kind node kind
   */
  private void insertTag(final int par, final int pos,
      final byte[] name, final int kind) {
    int root = par;
    if(pos == 0) {
      root = par + data.size(par, kind);
    } else {
      int currPos = 1;
      root = par + data.attSize(par, kind);
      while(currPos < pos) {
        int k = data.kind(root);
        if(data.parent(root, k) != par) break;
        root = root + data.size(root, k);
        currPos++;
      }
    }
    data.insert(root, par, name, Data.ELEM);
  }
}
