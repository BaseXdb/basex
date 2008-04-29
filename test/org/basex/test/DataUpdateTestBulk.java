package org.basex.test;

import org.basex.build.MemBuilder;
import org.basex.build.xml.XMLParser;
import org.basex.core.Prop;
import org.basex.core.proc.Create;
import org.basex.core.proc.Drop;
import org.basex.core.proc.Insert;
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
public final class DataUpdateTestBulk {
  /** Test file size in nodes. */
  private int size;
  /** Data. */
  private Data data;
  /** InsertData. */
  private Data insertData;
  /** Test file we do updates with. */
  private static final String TESTFILE = "etc/xml/test.xml";
  /** Test file we do updates with. */
  private static final String INSERTFILE = "input.xml";
  /** Test database name. */
  private static final String DBNAME = "DataUpdateTestBulk";

  /**
   * Delete the test-database.
   */
  @BeforeClass
  public static void setUpBeforeClass() {
    Prop.textindex = false;
    Prop.attrindex = false;
    Prop.ftindex = false;
    Prop.chop = true;
    Prop.mainmem = false;
  }

  /**
   * Create the database.
   * @throws java.lang.Exception in case of problems.
   */
  @Before
  public void setUp() throws Exception {
    data = Create.xml(new IO(TESTFILE), DBNAME);
    size = data.size;
    final IO file = new IO(INSERTFILE);
    insertData = new MemBuilder().build(new XMLParser(file), INSERTFILE);
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
   * Test nodes for deep equality (including atts/children).
   * @param data1 first node data
   * @param pre1 first node pre
   * @param data2 second node data
   * @param pre2 second node pre
   */
  private void assertNodesDeepEqual(final Data data1, final int pre1,
      final Data data2, final int pre2) {
    assertEquals(data1.kind(pre1), data2.kind(pre2));
    switch(data1.kind(pre1)) {
      case Data.TEXT:
        assertByteArraysEqual(data1.text(pre1), data2.text(pre2));
        break;
      case Data.ATTR:
        assertByteArraysEqual(data1.attName(pre1), data2.attName(pre2));
        assertByteArraysEqual(data1.attValue(pre1), data2.attValue(pre2));
        break;
      case Data.ELEM:
        assertByteArraysEqual(data1.tag(pre1), data2.tag(pre2));
        assertEquals(data1.size(pre1, Data.ELEM), data2.size(pre2, Data.ELEM));
        final int siz = data1.size(pre1, Data.ELEM);
        int pos = 1;
        while(pos < siz) {
          assertNodesDeepEqual(data1, pre1 + pos, data2, pre2 + pos);
          pos += data1.size(pre1 + pos, data1.kind(pre1 + pos));
        }
        break;
      default:
        fail("Unknown nodekind!");
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
   * Test bulk insertion.
   */
  @Test
  public void testBulkInsertSmall() {
    insert(6, 2, Insert.copy(insertData, 3));
    assertEquals(size + 4, data.size);
    assertNodesDeepEqual(insertData, 3, data, 11);
    assertEquals(11, data.parent(13, Data.ELEM));
  }

  /**
   * Test bulk insertion.
   */
  @Test
  public void testBulkInsertLarge() {
    insert(6, 2, Insert.copy(insertData, 5));
    assertEquals(size + 2, data.size);
    assertNodesDeepEqual(insertData, 5, data, 11);
    assertEquals(26, data.parent(26, Data.ELEM));
  }

  /**
   * Inserts a fragment to the database.
   * @param par parent value
   * @param pos inserting position
   * @param tmp temporary data instance
   */
  private void insert(final int par, final int pos, final Data tmp) {
    // find inserting position
    int pre = par;
    int k = data.kind(pre);
    if(pos == 0) {
      pre += data.size(pre, k);
    } else {
      pre += data.attSize(pre, k);
      for(int p = 1; p < pos && data.parent(pre, k) == par;
        pre += data.size(pre, k), p++) {
        k = data.kind(pre);
      }
    }
    data.insert(pre, par, tmp);
  }
}
