package org.basex.test.storage;

import org.basex.build.MemBuilder;
import org.basex.build.xml.XMLParser;
import org.basex.core.proc.Copy;
import org.basex.data.Data;
import org.basex.io.IO;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * This class tests the update features of the Data class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Tim Petrowsky
 */
public final class DataUpdateTestBulk extends DataUpdateTest {
  /** InsertData. */
  private Data insertData;
  /** Test file we do updates with. */
  private static final String INSERTFILE = "input.xml";

  @Before
  @Override
  public void setUp() {
    super.setUp();
    final IO file = IO.get(INSERTFILE);
    try {
      insertData = new MemBuilder().build(new XMLParser(file), INSERTFILE);
    } catch(final Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * Tests nodes for deep equality (including atts/children).
   * @param data1 first node data
   * @param pre1 first node pre
   * @param data2 second node data
   * @param pre2 second node pre
   */
  private void assertNodesDeepEqual(final Data data1, final int pre1,
      final Data data2, final int pre2) {

    final int kind = data1.kind(pre1);
    assertEquals(kind, data2.kind(pre2));
    switch(kind) {
      case Data.TEXT:
        assertByteArraysEqual(data1.text(pre1), data2.text(pre2));
        break;
      case Data.ATTR:
        assertByteArraysEqual(data1.attName(pre1), data2.attName(pre2));
        assertByteArraysEqual(data1.attValue(pre1), data2.attValue(pre2));
        break;
      case Data.ELEM:
        assertByteArraysEqual(data1.tag(pre1), data2.tag(pre2));
        assertEquals(data1.size(pre1, kind), data2.size(pre2, kind));
        final int siz = data1.size(pre1, kind);
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
   * Tests nodes for correct parent reference.
   * @param par parent value
   * @param pre pre value
   * @param d node data
   */
  private void assertParentEqual(final int par, final int pre, final Data d) {
    assertEquals(par, d.parent(pre, d.kind(pre)));
  }

  /**
   * Tests bulk insertion.
   */
  @Test
  public void testBulkInsertSmall() {
    final Data data = ctx.data();
    insert(6, 2, Copy.copy(insertData, 3));
    assertEquals(size + 4, data.meta.size);
    assertNodesDeepEqual(insertData, 3, data, 11);
    assertParentEqual(11, 13, data);
  }

  /**
   * Tests bulk insertion.
   */
  @Test
  public void testBulkInsertLarge() {
    final Data data = ctx.data();
    insert(6, 2, Copy.copy(insertData, 5));
    assertEquals(size + 2, data.meta.size);
    assertNodesDeepEqual(insertData, 5, data, 11);
    assertParentEqual(21, 22, data);
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
    final Data data = ctx.data();
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
