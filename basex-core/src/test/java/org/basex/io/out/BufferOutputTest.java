package org.basex.io.out;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.junit.jupiter.api.*;

/**
 * Tests for {@link BufferOutput}.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BufferOutputTest {
  /**
   * Single-byte writes are buffered and flushed once the buffer fills up.
   * @throws IOException I/O exception
   */
  @Test public void writeByte() throws IOException {
    final TestOutputStream tos = new TestOutputStream();
    try(BufferOutput bo = new BufferOutput(tos, 4)) {
      bo.write('a');
      bo.write('b');
      bo.write('c');
      bo.write('d');
      assertEquals(0, tos.bytes.size(), "buffer not yet full");
      bo.write('e');
      assertEquals(4, tos.bytes.size(), "flush triggered when buffer would overflow");
    }
    assertArrayEquals(new byte[] { 'a', 'b', 'c', 'd', 'e' }, tos.bytes.finish());
  }

  /**
   * Bulk write smaller than the buffer is accumulated and flushed on close.
   * @throws IOException I/O exception
   */
  @Test public void writeBulkSmall() throws IOException {
    final TestOutputStream tos = new TestOutputStream();
    try(BufferOutput bo = new BufferOutput(tos, 16)) {
      bo.write(new byte[] { 1, 2, 3 }, 0, 3);
      assertEquals(0, tos.bytes.size(), "still buffered");
    }
    assertArrayEquals(new byte[] { 1, 2, 3 }, tos.bytes.finish());
  }

  /**
   * Bulk write at least as large as the buffer bypasses it and writes through directly.
   * @throws IOException I/O exception
   */
  @Test public void writeBulkLarge() throws IOException {
    final TestOutputStream tos = new TestOutputStream();
    final byte[] big = new byte[16];
    for(int i = 0; i < big.length; i++) big[i] = (byte) i;
    try(BufferOutput bo = new BufferOutput(tos, 4)) {
      bo.write('x');
      bo.write(big, 0, big.length);
      assertEquals(1 + big.length, tos.bytes.size(),
          "buffer flushed and large chunk written through");
    }
    final byte[] out = tos.bytes.finish();
    assertEquals('x', out[0]);
    for(int i = 0; i < big.length; i++) assertEquals(big[i], out[i + 1]);
  }

  /**
   * Bulk write whose tail would overflow the buffer triggers an intermediate flush.
   * @throws IOException I/O exception
   */
  @Test public void writeBulkSpan() throws IOException {
    final TestOutputStream tos = new TestOutputStream();
    try(BufferOutput bo = new BufferOutput(tos, 4)) {
      bo.write(new byte[] { 1, 2, 3 }, 0, 3);
      bo.write(new byte[] { 4, 5 }, 0, 2);
      assertEquals(3, tos.bytes.size(), "first chunk flushed when second one would not fit");
    }
    assertArrayEquals(new byte[] { 1, 2, 3, 4, 5 }, tos.bytes.finish());
  }

  /**
   * {@code flush()} writes the buffered bytes but does NOT propagate to the underlying stream.
   * REST/Jetty integration relies on this: propagating {@code flush()} would commit the servlet
   * response body before the BaseX command pipeline has decided on the final HTTP status.
   * @throws IOException I/O exception
   */
  @Test public void flushDoesNotPropagate() throws IOException {
    final TestOutputStream tos = new TestOutputStream();
    try(BufferOutput bo = new BufferOutput(tos, 16)) {
      bo.write(new byte[] { 1, 2, 3 }, 0, 3);
      bo.flush();
      assertArrayEquals(new byte[] { 1, 2, 3 }, tos.bytes.finish());
      assertEquals(0, tos.flushes, "underlying stream must not be flushed");
    }
  }

  /**
   * {@code flush()} with an empty buffer is a no-op (no write, no propagation).
   * @throws IOException I/O exception
   */
  @Test public void flushEmpty() throws IOException {
    final TestOutputStream tos = new TestOutputStream();
    try(BufferOutput bo = new BufferOutput(tos, 16)) {
      bo.flush();
      assertEquals(0, tos.bytes.size());
      assertEquals(0, tos.flushes);
    }
  }

  /**
   * {@code close()} flushes pending bytes and closes the underlying stream.
   * @throws IOException I/O exception
   */
  @Test public void closeFlushesAndClosesUnderlying() throws IOException {
    final TestOutputStream tos = new TestOutputStream();
    try(BufferOutput bo = new BufferOutput(tos, 16)) {
      bo.write(new byte[] { 9, 8, 7 }, 0, 3);
    }
    assertArrayEquals(new byte[] { 9, 8, 7 }, tos.bytes.finish());
    assertEquals(1, tos.closes);
  }

  /** {@link BufferOutput#get(OutputStream)} returns the same instance when already buffered. */
  @Test public void getPassThrough() {
    final BufferOutput bo = new BufferOutput(new TestOutputStream());
    assertSame(bo, BufferOutput.get(bo));
  }

  /** {@link BufferOutput#get(OutputStream)} wraps a plain stream. */
  @Test public void getWraps() {
    final TestOutputStream tos = new TestOutputStream();
    assertNotSame(tos, BufferOutput.get(tos));
  }

  /** Counts and records bytes, flush- and close-calls of a sink stream. */
  private static final class TestOutputStream extends OutputStream {
    /** Bytes written through. */
    final ArrayOutput bytes = new ArrayOutput();
    /** Number of {@code flush()} calls. */
    int flushes;
    /** Number of {@code close()} calls. */
    int closes;

    @Override
    public void write(final int b) {
      bytes.write(b);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) {
      bytes.write(b, off, len);
    }

    @Override
    public void flush() {
      flushes++;
    }

    @Override
    public void close() {
      closes++;
    }
  }
}
