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
public final class DataUpdateTestAttributes {
  /** Context. */
  private static final Context CONTEXT = new Context();
  /** Test file size in nodes. */
  private int size;
  /** Data. */
  private Data data;
  /** Test file we do updates with. */
  private static final String TESTFILE = "etc/xml/test.xml";
  /** Test database name. */
  private static final String DBNAME = "DataUpdateTestAttributes";

  /**
   * Setup the test.
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
  private void assertByteArraysEqual(final byte[] expected,
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
   * Test the update of an existing attribute.
   * @throws Exception in case of problems.
   */
  @Test
  public void testUpdateAttribute() throws Exception {
    data.update(7, token("name"), token("junit"));
    assertEquals(size, data.size);
    assertByteArraysEqual(token("name"), data.attName(7));
    assertByteArraysEqual(token("junit"), data.attValue(7));
    reload();
    assertEquals(size, data.size);
    assertByteArraysEqual(token("name"), data.attName(7));
    assertByteArraysEqual(token("junit"), data.attValue(7));
  }

  /**
   * Test the update of an existing attribute.
   * @throws Exception in case of problems.
   */
  @Test
  public void testUpdateAttribute2() throws Exception {
    data.update(8, token("id"), token("junit"));
    assertEquals(size, data.size);
    assertByteArraysEqual(token("junit"), data.attValue(8));
    reload();
    assertEquals(size, data.size);
    assertByteArraysEqual(token("junit"), data.attValue(8));
  }

  /**
   * Test the insertion of a new attribute.
   * @throws Exception in case of problems.
   */
  @Test
  public void testAddAttribute() throws Exception {
    final long nextid = data.meta.lastid;
    data.insert(9, 6, token("foo"), token("junit"));
    assertEquals(size + 1, data.size);
    assertEquals(size + 1, data.size(0, Data.DOC));
    assertEquals((int) Data.ATTR, data.kind(9));
    assertEquals(6, data.parent(9, Data.ATTR));
    assertEquals(6, data.parent(8, Data.ATTR));
    assertEquals(6, data.parent(10, Data.ELEM));
    assertEquals(10, data.parent(11, Data.TEXT));
    assertEquals(nextid + 1, data.meta.lastid);
    assertByteArraysEqual(token("foo"), data.attName(9));
    assertByteArraysEqual(token("junit"), data.attValue(9));
    reload();
    assertEquals(size + 1, data.size);
    assertEquals(size + 1, data.size(0, Data.DOC));
    assertEquals((int) Data.ATTR, data.kind(9));
    assertEquals(6, data.parent(9, Data.ATTR));
    assertEquals(6, data.parent(8, Data.ATTR));
    assertEquals(6, data.parent(10, Data.ELEM));
    assertEquals(10, data.parent(11, Data.TEXT));
    assertEquals(nextid + 1, data.meta.lastid);
    assertByteArraysEqual(token("foo"), data.attName(9));
    assertByteArraysEqual(token("junit"), data.attValue(9));
  }

}
