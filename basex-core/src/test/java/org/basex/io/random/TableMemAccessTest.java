package org.basex.io.random;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.*;
import org.basex.data.*;
import org.basex.util.list.*;
import org.junit.jupiter.api.*;

/**
 * Tests for {@link TableMemAccess} class.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class TableMemAccessTest extends SandboxTest {
  /** Test entry. */
  private static final byte[] ENTRY = {
    101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 127, -128, -1
  };
  /** Table instance. */
  TableMemAccess table;

  /** Set up method. */
  @BeforeEach public void setUp() {
    table = new TableMemAccess(new MetaData(context.options));
  }

  /** Test method. */
  @Test public void emptyAppend() {
    table.insert(0, new byte[0]);
    assertEquals(0, table.meta.size);
  }

  /** Test method. */
  @Test public void singleAppend() {
    singleAppend(1);
  }

  /** Test method. */
  @Test public void singleAppend8() {
    singleAppend(8);
  }

  /** Test method. */
  @Test public void singleAppend5000() {
    singleAppend(5000);
  }

  /** Test method. */
  @Test public void singleAppend100000() {
    singleAppend(100000);
  }

  /**
   * Appends the specified number of entries via a single insert operation.
   * @param n number of entries
   */
  private void singleAppend(final int n) {
    final ByteList list = new ByteList(n * ENTRY.length);
    for(int i = 0; i < n; i++) {
      ENTRY[0] = (byte) i;
      list.add(ENTRY);
    }
    table.insert(0, list.finish());
    check(n);
  }

  /** Test method. */
  @Test public void append8() {
    append(8);
  }

  /** Test method. */
  @Test public void append5000() {
    append(5000);
  }

  /** Test method. */
  @Test public void append100000() {
    append(100000);
  }

  /**
   * Appends the specified number of entries.
   * @param n number of entries
   */
  private void append(final int n) {
    for(int i = 0; i < n; i++) {
      ENTRY[0] = (byte) i;
      table.insert(i, ENTRY);
    }
    check(n);
  }

  /** Test method. */
  @Test public void prepend8() {
    prepend(8);
  }

  /** Test method. */
  @Test public void prepend5000() {
    prepend(5000);
  }

  /** Test method. */
  @Test public void prepend100000() {
    prepend(100000);
  }

  /**
   * Prepends the specified number of entries.
   * @param n number of entries
   */
  private void prepend(final int n) {
    for(int i = 0; i < n; i++) {
      ENTRY[0] = (byte) (n - i - 1);
      table.insert(0, ENTRY);
    }
    check(n);
  }

  /** Test method. */
  @Test public void emptyDelete() {
    table.insert(0, ENTRY);
    table.delete(0, 0);
    assertEquals(1, table.meta.size);
  }

  /** Test method. */
  @Test public void delete() {
    table.insert(0, ENTRY);
    table.delete(0, 1);
    assertEquals(0, table.meta.size);
  }

  /** Test method. */
  @Test public void deleteFirst8() {
    delete(8, true);
  }

  /** Test method. */
  @Test public void deleteFirst5000() {
    delete(5000, true);
  }

  /** Test method. */
  @Test public void deleteFirst100000() {
    delete(100000, true);
  }

  /** Test method. */
  @Test public void deleteLast8() {
    delete(8, false);
  }

  /** Test method. */
  @Test public void deleteLast5000() {
    delete(5000, false);
  }

  /** Test method. */
  @Test public void deleteLast100000() {
    delete(100000, false);
  }

  /**
   * Deletes the specified number of entries.
   * @param n number of entries
   * @param first delete first or last entry
   */
  private void delete(final int n, final boolean first) {
    for(int i = 0; i < n; i++) table.insert(i, ENTRY);
    for(int i = 0; i < n; i++) table.delete(first ? 0 : n - i - 1, 1);
    assertEquals(0, table.meta.size);
  }

  /** Test method. */
  @Test public void singleDelete() {
    singleDelete(1);
  }

  /** Test method. */
  @Test public void singleDelete8() {
    singleDelete(8);
  }

  /** Test method. */
  @Test public void singleDelete5000() {
    singleDelete(5000);
  }

  /** Test method. */
  @Test public void singleDelete100000() {
    singleDelete(100000);
  }

  /**
   * Deletes the specified number of entries.
   * @param n number of entries
   */
  private void singleDelete(final int n) {
    for(int i = 0; i < n; i++) table.insert(i, ENTRY);
    table.delete(0, n);
    assertEquals(0, table.meta.size);
  }

  /** Test method. */
  @Test public void random8() {
    random(8);
  }

  /** Test method. */
  @Test public void random5000() {
    random(5000);
  }

  /**
   * Performs random insert and delete operations.
   * @param n number of operations
   */
  private void random(final int n) {
    // fill table with initial entries
    ENTRY[0] = 101;
    for(int i = 0; i < n; i++) table.insert(i, ENTRY);

    // delete and reinsert random sections in the array
    final Random rnd = new Random();
    for(int r = 0; r < 100; r++) {
      final int pre = Math.abs(rnd.nextInt()) % n;
      final int count = Math.abs(rnd.nextInt()) % (n - pre);
      table.delete(pre, count);
      for(int i = 0; i < count; i++) table.insert(0, ENTRY);
    }
    // check table contents
    assertEquals(n, table.meta.size);
    for(int i = 0; i < n; i++) {
      assertEquals(101, table.read1(i, 0));
      assertEquals(255, table.read1(i, 15));
    }
  }

  /**
   * Checks the table entries.
   * @param n number of entries
   */
  private void check(final int n) {
    // check table size
    assertEquals(n, table.meta.size);
    // check table contents
    for(int i = 0; i < n; i++) {
      assertEquals(i & 0xFF, table.read1(i, 0));
      assertEquals(255, table.read1(i, 15));
    }
    // check table contents via random access
    final Random rnd = new Random();
    for(int r = 0; r < 100; r++) {
      final int i = Math.abs(rnd.nextInt()) % n;
      assertEquals(i & 0xFF, table.read1(i, 0));
      assertEquals(255, table.read1(i, 15));
    }
  }
}
