package org.basex.data;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.lang.reflect.*;

import org.basex.*;
import org.basex.build.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.io.random.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * This class tests the update functionality of the block storage.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Tim Petrowsky
 */
public final class DiskTableTest extends SandboxTest {
  /** Test file we do updates with. */
  private static final String TESTFILE = "src/test/resources/xmark.xml";

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
  @BeforeAll public static void setUpBeforeClass() {
    set(MainOptions.TEXTINDEX, false);
    set(MainOptions.ATTRINDEX, false);
  }

  /**
   * Loads the JUnitTest database.
   * @throws Exception exception
   */
  @BeforeEach public void setUp() throws Exception {
    final Parser parser = Parser.xmlParser(IO.get(TESTFILE));
    data = new DiskBuilder(NAME, parser, context.soptions, context.options).build();
    size = data.meta.size;
    data.close();
    tda = new TableDiskAccess(data.meta, true);

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
   * @throws Exception exception
   */
  @AfterEach public void tearDown() throws Exception {
    if(tda != null) tda.close();
    DropDB.drop(NAME, context.soptions);
  }

  /**
   * Closes and reloads storage.
   */
  private void closeAndReload() {
    try {
      tda.close();
      tda = new TableDiskAccess(data.meta, true);
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
  private void assertEntrysEqual(final int startNodeNumber, final int currentNodeNumber,
      final int count) {

    final int startOffset = startNodeNumber << IO.NODEPOWER;
    final int currentOffset = currentNodeNumber << IO.NODEPOWER;
    for(int i = 0; i < count << IO.NODEPOWER; ++i) {
      final int startByteNum = startOffset + i;
      final int currentByteNum = currentOffset + i;
      final byte startByte = storage[startByteNum];
      final byte currentByte = (byte) tda.read1(currentByteNum >> IO.NODEPOWER,
        currentByteNum % (1 << IO.NODEPOWER));
      assertEquals(startByte, currentByte,
        "Old entry " + (startByteNum >> IO.NODEPOWER)
          + " (byte " + startByteNum % (1 << IO.NODEPOWER)
          + ") and new entry " + (currentByteNum >> IO.NODEPOWER)
          + " (byte " + currentByteNum % (1 << IO.NODEPOWER) + ')');
    }
  }

  /**
   * Tests size of file.
   */
  @Test public void size() {
    assertEquals(size, tdaSize(), "Testfile size changed!");
    assertTrue(blocks > 2, "Need at least 3 blocks for testing!");
    assertEquals(blocks, tdaBlocks(), "Unexpected number of blocks!");
    closeAndReload();
    assertEquals(size, tdaSize(), "Testfile size changed!");
    assertTrue(blocks > 2, "Need at least 3 blocks for testing!");
    assertEquals(blocks, tdaBlocks(), "Unexpected number of blocks!");
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
      final Field f = tda.getClass().getDeclaredField("used");
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
  @Test public void deleteOneNode() {
    tda.delete(3, 1);
    assertEquals(size - 1, tdaSize(), "One node deleted => size-1");
    assertEntrysEqual(0, 0, 3);
    assertEntrysEqual(4, 3, size - 4);
    closeAndReload();
    assertEquals(size - 1, tdaSize(), "One node deleted => size-1");
    assertEntrysEqual(0, 0, 3);
    assertEntrysEqual(4, 3, size - 4);
  }

  /**
   * Tests delete at beginning.
   */
  @Test public void deleteAtBeginning() {
    tda.delete(0, 3);
    assertEquals(size - 3, tdaSize(), "Three nodes deleted => size-3");
    assertEntrysEqual(3, 0, size - 3);
    closeAndReload();
    assertEquals(size - 3, tdaSize(), "Three nodes deleted => size-3");
    assertEntrysEqual(3, 0, size - 3);
  }

  /**
   * Tests delete at end.
   */
  @Test public void deleteAtEnd() {
    tda.delete(size - 3, 3);
    assertEquals(size - 3, tdaSize(), "Three nodes deleted => size-3");
    assertEntrysEqual(0, 0, size - 3);
    closeAndReload();
    assertEquals(size - 3, tdaSize(), "Three nodes deleted => size-3");
    assertEntrysEqual(0, 0, size - 3);
  }

  /**
   * Deletes first block.
   */
  @Test public void deleteFirstBlock() {
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
  @Test public void deleteSecondBlock() {
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
  @Test public void deleteLastBlock() {
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
  @Test public void deleteSecondBlockAndSurroundingNodes() {
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
  @Test public void simpleInsert() {
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
  @Test public void insertMultiple() {
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
  @Test public void insertMany() {
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
  @Test public void insertAtBlockBoundary() {
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
    final int rl = e << IO.NODEPOWER;
    final byte[] result = new byte[rl];
    for(int r = 0; r < rl; ++r) result[r] = 5;
    return result;
  }
}

