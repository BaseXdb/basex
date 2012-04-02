package org.basex.test.data;

import java.io.IOException;
import java.lang.reflect.Field;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.basex.build.DiskBuilder;
import org.basex.build.Parser;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.DropDB;
import org.basex.data.Data;
import org.basex.data.MetaData;
import org.basex.io.IO;
import org.basex.io.random.TableDiskAccess;
import org.basex.util.Util;
import static org.junit.Assert.*;

/**
 * This class tests the update functionality of the block storage.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Tim Petrowsky
 */
public final class DiskTableTest {
  /** Test file we do updates with. */
  private static final String TESTFILE = "src/test/resources/xmark.xml";
  /** Test database name. */
  private static final String DB = Util.name(DiskTableTest.class);
  /** Test file we do updates with. */
  private static final Context CONTEXT = new Context();

  /** BlockStorage. */
  private TableDiskAccess tda;
  /** Data reference. */
  private Data data;
  /** Test file size. */
  private int size;
  /** Starting storage. */
  private byte[] storage;
  /** Expected blocks in file. */
  private int blocks;
  /** Nodes per block. */
  private int nodes;

  /**
   * Initializes the test class.
   */
  @BeforeClass
  public static void setUpBeforeClass() {
    CONTEXT.prop.set(Prop.TEXTINDEX, false);
    CONTEXT.prop.set(Prop.ATTRINDEX, false);
  }

  /**
   * Loads the JUnitTest database.
   */
  @Before
  public void setUp() {
    try {
      final Parser parser = Parser.xmlParser(IO.get(TESTFILE), CONTEXT.prop);
      data = new DiskBuilder(DB, parser, CONTEXT).build();
      size = data.meta.size;
      data.close();
      tda = new TableDiskAccess(data.meta);
    } catch(final Exception ex) {
      Util.stack(ex);
    }

    final int bc = size * (1 << IO.NODEPOWER);
    storage = new byte[bc];
    for(int i = 0; i < bc; ++i) {
      storage[i] = (byte) tda.read1(i >> IO.NODEPOWER, i % (1 << IO.NODEPOWER));
    }
    nodes = IO.BLOCKSIZE >>> IO.NODEPOWER;
    blocks = (int) Math.ceil((double) size / nodes);
  }

  /**
   * Drops the JUnitTest database.
   */
  @After
  public void tearDown() {
    try {
      if(tda != null) tda.close();
      DropDB.drop(DB, CONTEXT);
    } catch(final Exception ex) {
      Util.stack(ex);
    }
  }

  /**
   * Closes and reloads storage.
   */
  private void closeAndReload() {
    try {
      tda.close();
      tda = new TableDiskAccess(data.meta);
    } catch(final IOException ex) {
      fail(Util.message(ex));
    }
  }

  /**
   * Compares old with new entries.
   * @param startNodeNumber first old entry to compare
   * @param currentNodeNumber first new entry to compare
   * @param count number of entries to compare
   */
  private void assertEntrysEqual(final int startNodeNumber,
      final int currentNodeNumber, final int count) {
    final int startOffset = startNodeNumber << IO.NODEPOWER;
    final int currentOffset = currentNodeNumber << IO.NODEPOWER;
    for(int i = 0; i < count << IO.NODEPOWER; ++i) {
      final int startByteNum = startOffset + i;
      final int currentByteNum = currentOffset + i;
      final byte startByte = storage[startByteNum];
      final byte currentByte = (byte) tda.read1(currentByteNum >> IO.NODEPOWER,
        currentByteNum % (1 << IO.NODEPOWER));
      assertEquals("Old entry " + (startByteNum >> IO.NODEPOWER)
          + " (byte " + startByteNum % (1 << IO.NODEPOWER)
          + ") and new entry " + (currentByteNum >> IO.NODEPOWER)
          + " (byte " + currentByteNum % (1 << IO.NODEPOWER) + ')',
          startByte, currentByte);
    }
  }

  /**
   * Tests size of file.
   */
  @Test
  public void size() {
    assertEquals("Testfile size changed!", size, tdaSize());
    assertTrue("Need at least 3 blocks for testing!", blocks > 2);
    assertEquals("Unexpected number of blocks!", blocks, tdaBlocks());
    closeAndReload();
    assertEquals("Testfile size changed!", size, tdaSize());
    assertTrue("Need at least 3 blocks for testing!", blocks > 2);
    assertEquals("Unexpected number of blocks!", blocks, tdaBlocks());
  }

  /**
   * Returns the number of block entries.
   * @return number of entries
   */
  private int tdaSize() {
    try {
      final Field f = tda.getClass().getSuperclass().getDeclaredField("meta");
      f.setAccessible(true);
      return ((MetaData) f.get(tda)).size;
    } catch(final Exception ex) {
      Util.stack(ex);
      return 0;
    }
  }

  /**
   * Returns the number of blocks.
   * @return number of blocks
   */
  private int tdaBlocks() {
    try {
      final Field f = tda.getClass().getDeclaredField("blocks");
      f.setAccessible(true);
      return f.getInt(tda);
    } catch(final Exception ex) {
      Util.stack(ex);
      return 0;
    }
  }

  /**
   * Tests delete.
   */
  @Test
  public void deleteOneNode() {
    tda.delete(3, 1);
    assertEquals("One node deleted => size-1", size - 1, tdaSize());
    assertEntrysEqual(0, 0, 3);
    assertEntrysEqual(4, 3, size - 4);
    closeAndReload();
    assertEquals("One node deleted => size-1", size - 1, tdaSize());
    assertEntrysEqual(0, 0, 3);
    assertEntrysEqual(4, 3, size - 4);
  }

  /**
   * Tests delete at beginning.
   */
  @Test
  public void deleteAtBeginning() {
    tda.delete(0, 3);
    assertEquals("Three nodes deleted => size-3", size - 3, tdaSize());
    assertEntrysEqual(3, 0, size - 3);
    closeAndReload();
    assertEquals("Three nodes deleted => size-3", size - 3, tdaSize());
    assertEntrysEqual(3, 0, size - 3);
  }

  /**
   * Tests delete at end.
   */
  @Test
  public void deleteAtEnd() {
    tda.delete(size - 3, 3);
    assertEquals("Three nodes deleted => size-3", size - 3, tdaSize());
    assertEntrysEqual(0, 0, size - 3);
    closeAndReload();
    assertEquals("Three nodes deleted => size-3", size - 3, tdaSize());
    assertEntrysEqual(0, 0, size - 3);
  }

  /**
   * Deletes first block.
   */
  @Test
  public void deleteFirstBlock() {
    tda.delete(0, nodes);
    assertEquals(blocks - 1, tdaBlocks());
    assertEntrysEqual(nodes, 0, size - nodes);
    closeAndReload();
    assertEquals(blocks - 1, tdaBlocks());
    assertEntrysEqual(nodes, 0, size - nodes);
  }

  /**
   * Deletes the second block.
   */
  @Test
  public void deleteSecondBlock() {
    tda.delete(nodes, nodes);
    assertEquals(blocks - 1, tdaBlocks());
    assertEntrysEqual(0, 0, nodes);
    assertEntrysEqual(2 * nodes, nodes, size - 2 * nodes);
    closeAndReload();
    assertEquals(blocks - 1, tdaBlocks());
    assertEntrysEqual(0, 0, nodes);
    assertEntrysEqual(2 * nodes, nodes, size - 2 * nodes);
  }

  /**
   * Deletes the last block.
   */
  @Test
  public void deleteLastBlock() {
    tda.delete(size / nodes * nodes, size % nodes);
    assertEquals(blocks - 1, tdaBlocks());
    assertEntrysEqual(0, 0, nodes - size % nodes);
    closeAndReload();
    assertEquals(blocks - 1, tdaBlocks());
    assertEntrysEqual(0, 0, nodes - size % nodes);
  }

  /**
   * Deletes the second block with some surrounding nodes.
   */
  @Test
  public void deleteSecondBlockAndSurroundingNodes() {
    tda.delete(nodes - 1, nodes + 2);
    assertEquals(size - 2 - nodes, tdaSize());
    assertEquals(blocks - 1, tdaBlocks());
    assertEntrysEqual(0, 0, nodes - 1);
    assertEntrysEqual(2 * nodes + 1, nodes - 1, size - 2 * nodes - 1);
    closeAndReload();
    assertEquals(size - 2 - nodes, tdaSize());
    assertEquals(blocks - 1, tdaBlocks());
    assertEntrysEqual(0, 0, nodes - 1);
    assertEntrysEqual(2 * nodes + 1, nodes - 1, size - 2 * nodes - 1);
  }

  /**
   * Tests basic insertion.
   */
  @Test
  public void simpleInsert() {
    tda.insert(4, getTestEntries(1));
    assertEquals(size + 1, tdaSize());
    assertEntrysEqual(0, 0, 4);
    assertAreInserted(4, 1);
    assertEntrysEqual(4, 5, size - 4);
    closeAndReload();
    assertEquals(size + 1, tdaSize());
    assertEntrysEqual(0, 0, 4);
    assertAreInserted(4, 1);
    assertEntrysEqual(4, 5, size - 4);
  }

  /**
   * Tests inserting multiple entries.
   */
  @Test
  public void insertMultiple() {
    tda.insert(4, getTestEntries(3));
    assertEquals(size + 3, tdaSize());
    assertEntrysEqual(0, 0, 4);
    assertAreInserted(4, 3);
    assertEntrysEqual(4, 7, size - 4);
    closeAndReload();
    assertEquals(size + 3, tdaSize());
    assertEntrysEqual(0, 0, 4);
    assertAreInserted(4, 3);
    assertEntrysEqual(4, 7, size - 4);
  }

  /**
   * Tests inserting multiple entries.
   */
  @Test
  public void insertMany() {
    tda.insert(4, getTestEntries(nodes - 1));
    assertEquals(size + nodes - 1, tdaSize());
    assertEntrysEqual(0, 0, 4);
    assertAreInserted(4, nodes - 1);
    assertEntrysEqual(4, 4 + nodes - 1, size - 4);
    closeAndReload();
    assertEquals(size + nodes - 1, tdaSize());
    assertEntrysEqual(0, 0, 4);
    assertAreInserted(4, nodes - 1);
    assertEntrysEqual(4, 4 + nodes - 1, size - 4);
  }

  /**
   * Tests inserting multiple entries.
   */
  @Test
  public void insertAtBlockBoundary() {
    tda.insert(nodes, getTestEntries(nodes));
    assertEquals(size + nodes, tdaSize());
    assertEquals(blocks + 1, tdaBlocks());
    assertEntrysEqual(0, 0, nodes);
    assertAreInserted(nodes, nodes);
    assertEntrysEqual(nodes, 2 * nodes, size - nodes);
    closeAndReload();
    assertEquals(size + nodes, tdaSize());
    assertEquals(blocks + 1, tdaBlocks());
    assertEntrysEqual(0, 0, nodes);
    assertAreInserted(nodes, nodes);
    assertEntrysEqual(nodes, 2 * nodes, size - nodes);
  }

  /**
   * Asserts that the chosen entries are inserted by a test case.
   * @param startNum first entry
   * @param count number of entries
   */
  private void assertAreInserted(final int startNum, final int count) {
    for(int i = 0; i < count; ++i)
      for(int j = 0; j < 1 << IO.NODEPOWER; ++j)
        assertEquals(5, tda.read1(startNum + i, j));
  }

  /**
   * Creates a test-byte array containing the specified number of entries.
   * All bytes are set to (byte) 5.
   * @param e number of entries to create
   * @return byte array containing the number of entries (all bytes 5)
   */
  private static byte[] getTestEntries(final int e) {
    final byte[] result = new byte[e << IO.NODEPOWER];
    for(int i = 0; i < result.length; ++i) result[i] = 5;
    return result;
  }
}

