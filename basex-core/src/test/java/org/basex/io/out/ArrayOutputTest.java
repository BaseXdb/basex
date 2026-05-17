package org.basex.io.out;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Tests for {@link ArrayOutput}.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArrayOutputTest {
  /** Single byte writes are accumulated. */
  @Test public void writeByte() {
    final ArrayOutput ao = new ArrayOutput();
    ao.write('a');
    ao.write('b');
    ao.write('c');
    assertEquals(3, ao.size());
    assertArrayEquals(new byte[] { 'a', 'b', 'c' }, ao.toArray());
  }

  /** Bulk writes are accumulated and copied at the requested offset/length. */
  @Test public void writeBulk() {
    final ArrayOutput ao = new ArrayOutput();
    final byte[] src = { 1, 2, 3, 4, 5 };
    ao.write(src, 1, 3);
    assertArrayEquals(new byte[] { 2, 3, 4 }, ao.toArray());
  }

  /** Writes grow the buffer beyond its initial capacity. */
  @Test public void writeGrows() {
    final ArrayOutput ao = new ArrayOutput();
    final byte[] src = new byte[Array.INITIAL_CAPACITY * 4];
    for(int i = 0; i < src.length; i++) src[i] = (byte) i;
    ao.write(src, 0, src.length);
    assertEquals(src.length, ao.size());
    assertArrayEquals(src, ao.toArray());
  }

  /** Empty bulk write is a no-op. */
  @Test public void writeBulkEmpty() {
    final ArrayOutput ao = new ArrayOutput();
    ao.write(new byte[] { 1, 2, 3 }, 0, 0);
    assertEquals(0, ao.size());
    assertArrayEquals(Token.EMPTY, ao.toArray());
  }

  /** {@link ArrayOutput#setLimit(int)} truncates both single and bulk writes. */
  @Test public void limit() {
    final ArrayOutput ao = new ArrayOutput();
    ao.setLimit(3);
    ao.write('a');
    ao.write(new byte[] { 'b', 'c', 'd', 'e' }, 0, 4);
    ao.write('f');
    assertEquals(3, ao.size());
    assertArrayEquals(new byte[] { 'a', 'b', 'c' }, ao.toArray());
  }

  /** {@code next()} returns and resets, without losing data. */
  @Test public void next() {
    final ArrayOutput ao = new ArrayOutput();
    ao.write(new byte[] { 1, 2, 3 }, 0, 3);
    assertArrayEquals(new byte[] { 1, 2, 3 }, ao.next());
    assertEquals(0, ao.size());
    ao.write(new byte[] { 4, 5 }, 0, 2);
    assertArrayEquals(new byte[] { 4, 5 }, ao.next());
  }

  /** {@code next()} returns an empty array for an empty buffer. */
  @Test public void nextEmpty() {
    final ArrayOutput ao = new ArrayOutput();
    assertEquals(0, ao.next().length);
    assertEquals(0, ao.size());
  }

  /** {@code finish()} returns the data and invalidates the buffer. */
  @Test public void finish() {
    final ArrayOutput ao = new ArrayOutput();
    ao.write(new byte[] { 7, 8, 9 }, 0, 3);
    assertArrayEquals(new byte[] { 7, 8, 9 }, ao.finish());
    assertNull(ao.buffer());
  }

  /** {@code finish()} returns {@link Token#EMPTY} for an empty buffer. */
  @Test public void finishEmpty() {
    assertSame(Token.EMPTY, new ArrayOutput().finish());
  }

  /** Exactly full buffer. */
  @Test public void finishExactlyFullReturnsBuffer() {
    final ArrayOutput ao = new ArrayOutput();
    final byte[] src = new byte[Array.INITIAL_CAPACITY];
    for(int i = 0; i < src.length; i++) src[i] = (byte) (i + 1);
    ao.write(src, 0, src.length);
    final byte[] internal = ao.buffer();
    assertSame(internal, ao.finish(), "exactly-full buffer should be handed out without copy");
  }

  /** {@code reset()} clears the size but keeps the buffer. */
  @Test public void reset() {
    final ArrayOutput ao = new ArrayOutput();
    ao.write(new byte[] { 1, 2, 3 }, 0, 3);
    final byte[] internal = ao.buffer();
    ao.reset();
    assertEquals(0, ao.size());
    assertSame(internal, ao.buffer());
  }

  /** {@code print(byte[])} counts ASCII codepoints and resets on newline. */
  @Test public void printAsciiLineLength() {
    final ArrayOutput ao = new ArrayOutput();
    ao.print(Token.token("abc\ndef"));
    assertEquals(3, ao.lineLength());
    assertArrayEquals(Token.token("abc\ndef"), ao.toArray());
  }

  /** {@code print(byte[])} counts UTF-8 multi-byte sequences as a single codepoint. */
  @Test public void printUtf8LineLength() {
    final ArrayOutput ao = new ArrayOutput();
    final byte[] token = Token.token("üöä");
    assertEquals(6, token.length, "expected three two-byte UTF-8 codepoints");
    ao.print(token);
    assertEquals(3, ao.lineLength());
    assertArrayEquals(token, ao.toArray());
  }

  /** {@code print(byte[])} on an empty token is a no-op. */
  @Test public void printEmpty() {
    final ArrayOutput ao = new ArrayOutput();
    ao.print(Token.EMPTY);
    assertEquals(0, ao.size());
    assertEquals(0, ao.lineLength());
  }

  /** {@code toString()} returns the UTF-8 view of the written bytes. */
  @Test public void asString() {
    final ArrayOutput ao = new ArrayOutput();
    ao.print(Token.token("hello"));
    assertEquals("hello", ao.toString());
  }

  /** {@code flush()} and {@code close()} are no-ops and do not throw. */
  @Test public void flushAndClose() {
    final ArrayOutput ao = new ArrayOutput();
    assertDoesNotThrow(ao::flush);
    assertDoesNotThrow(ao::close);
  }
}
