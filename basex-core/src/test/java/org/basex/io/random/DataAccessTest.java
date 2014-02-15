package org.basex.io.random;

import static org.junit.Assert.*;

import java.io.*;

import org.basex.io.*;
import org.basex.util.*;
import org.junit.*;

/**
 * Tests for class {@link DataAccess}.
 * <br/>
 * <p>The read operations are tested using a pre-created file with different
 * types of values at fixed positions; the values occupy the first
 * {@link IO#BLOCKSIZE} bytes of the file.</p>
 * <br/>
 * <p>In order to test cross-block reads, a string is written at position
 * {@link IO#BLOCKSIZE} - 5.</p>
 * <br/>
 * <p>Write operations are tested by writing a value at a specified random
 * position.</p>
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Dimitar Popov
 */
public class DataAccessTest {
  /** String. */
  private static final String STR = "string with characters: 10";
  /** Long string. */
  private static final String STR_LONG = repeat(STR, 1000);
  /** Byte. */
  private static final byte BYTE = Byte.MIN_VALUE;
  /** Long (5-byte long). */
  private static final long LONG = 1L << 5 * Byte.SIZE - 1; // 39 shifts
  /** Integer. */
  private static final int INT = Integer.MAX_VALUE;
  /** Integer, which should take 5 bytes when compressed. */
  private static final int CINT5 = 0x3FFFFFFF + 1;
  /** Integer, which should take 4 bytes when compressed. */
  private static final int CINT4 = 0x3FFF + 1;
  /** Integer, which should take 2 bytes when compressed. */
  private static final int CINT2 = 0x3F + 1;
  /** Integer, which should take 1 byte when compressed. */
  private static final int CINT1 = 0x3F;
  /** String: binary representation with unsigned bytes. */
  private static final int[] STR_BIN = stringToByteArray(STR);
  /** Long string: binary representation with unsigned bytes. */
  private static final int[] STR_LONG_BIN = stringToByteArray(STR_LONG);
  /** Byte: binary representation with unsigned bytes. */
  private static final int[] BYTE_BIN = { toUnsignedByte(BYTE) };
  /** Long (5-byte long): binary representation with unsigned bytes. */
  private static final int[] LONG_BIN = longToByteArray(LONG);
  /** Integer: binary representation with unsigned bytes. */
  private static final int[] INT_BIN = intToByteArray(INT);
  /** Compressed integer: 5-bytes: binary representation with unsigned bytes. */
  private static final int[] CINT5_BIN = numToByteArray(CINT5);
  /** Compressed integer: 4-bytes: binary representation with unsigned bytes. */
  private static final int[] CINT4_BIN = numToByteArray(CINT4);
  /** Compressed integer: 2-bytes: binary representation with unsigned bytes. */
  private static final int[] CINT2_BIN = numToByteArray(CINT2);
  /** Compressed integer: 1-byte: binary representation with unsigned bytes. */
  private static final int[] CINT1_BIN = numToByteArray(CINT1);
  /** Block boundary position for testing cross block reads and writes. */
  private static final long BLOCK_BOUNDARY_POS = IO.BLOCKSIZE - 5;
  /** Random position to test write operations. */
  private static final long RANDOM_POS = 15L;

  /** Temporary file. */
  private IOFile file;
  /** Instance under test. */
  private DataAccess da;

  /**
   * Set up method.
   * @throws IOException I/O exception
   */
  @Before
  public void setUp() throws IOException {
    file = new IOFile(File.createTempFile("page", IO.BASEXSUFFIX));
    final RandomAccessFile f = new RandomAccessFile(file.file(), "rw");
    try {
      initialContent(f);
    } finally {
      f.close();
    }
    da = new DataAccess(file);
  }

  /**
   * Tear down method.
   */
  @After
  public void tearDown() {
    da.close();
    file.delete();
  }

  /** Test method for {@link DataAccess#cursor()}. */
  @Test
  public final void testCursor() {
    assertEquals(0L, da.cursor());
  }

  /** Test method for {@link DataAccess#length()}. */
  @Test
  public final void testLength() {
    final long len = BLOCK_BOUNDARY_POS + STR_BIN.length;
    assertEquals(len, da.length());
  }

  /** Test method for {@link DataAccess#more()}. */
  @Test
  public final void testMore() {
    assertTrue(da.more());

    da.cursor(da.length());
    assertFalse(da.more());
  }

  /** Test method for {@link DataAccess#read1(long)}. */
  @Test
  public final void testRead1Long() {
    final long off = STR_BIN.length;
    assertEquals(BYTE, da.read1(off));
  }

  /** Test method for {@link DataAccess#read1()}. */
  @Test
  public final void testRead1() {
    final long off = STR_BIN.length;
    da.cursor(off);
    assertEquals(BYTE, da.read1());
  }

  /** Test method for {@link DataAccess#read4(long)}. */
  @Test
  public final void testRead4Long() {
    final long off = STR_BIN.length + BYTE_BIN.length + LONG_BIN.length;
    assertEquals(INT, da.read4(off));
  }

  /** Test method for {@link DataAccess#read4()}. */
  @Test
  public final void testRead4() {
    final long off = STR_BIN.length + BYTE_BIN.length + LONG_BIN.length;
    da.cursor(off);
    assertEquals(INT, da.read4());
  }

  /** Test method for {@link DataAccess#read5(long)}. */
  @Test
  public final void testRead5Long() {
    final long off = STR_BIN.length + BYTE_BIN.length;
    assertEquals(LONG, da.read5(off));
  }

  /** Test method for {@link DataAccess#read5()}. */
  @Test
  public final void testRead5() {
    final long off = STR_BIN.length + BYTE_BIN.length;
    da.cursor(off);
    assertEquals(LONG, da.read5());
  }

  /** Test method for {@link DataAccess#readNum(long)}. */
  @Test
  public final void testReadNumLong() {
    long off = STR_BIN.length + BYTE_BIN.length + LONG_BIN.length +
        INT_BIN.length;
    assertEquals(CINT5, da.readNum(off));
    off += CINT5_BIN.length;
    assertEquals(CINT4, da.readNum(off));
    off += CINT4_BIN.length;
    assertEquals(CINT2, da.readNum(off));
    off += CINT2_BIN.length;
    assertEquals(CINT1, da.readNum(off));
  }

  /** Test method for {@link DataAccess#readNum()}. */
  @Test
  public final void testReadNum() {
    final long off = STR_BIN.length + BYTE_BIN.length + LONG_BIN.length +
        INT_BIN.length;
    da.cursor(off);
    assertEquals(CINT5, da.readNum());
    assertEquals(CINT4, da.readNum());
    assertEquals(CINT2, da.readNum());
    assertEquals(CINT1, da.readNum());
  }

  /** Test method for {@link DataAccess#readToken(long)}. */
  @Test
  public final void testReadTokenLong() {
    assertEquals(STR, Token.string(da.readToken(0L)));
    assertEquals(STR, Token.string(da.readToken(BLOCK_BOUNDARY_POS)));
  }

  /** Test method for {@link DataAccess#readToken()}. */
  @Test
  public final void testReadToken() {
    assertEquals(STR, Token.string(da.readToken()));
  }

  /** Test method for {@link DataAccess#readBytes(long, int)}. */
  @Test
  public final void testReadBytesLongInt() {
    final byte[] bytes = Token.token(STR);
    final long off = numToByteArray(bytes.length).length;

    assertEquals(STR, Token.string(da.readBytes(off, bytes.length)));
  }

  /** Test method for {@link DataAccess#readBytes(int)}. */
  @Test
  public final void testReadBytesInt() {
    final byte[] bytes = Token.token(STR);
    final long off = numToByteArray(bytes.length).length;

    da.cursor(off);
    assertEquals(STR, Token.string(da.readBytes(bytes.length)));
  }

  /** Test method for {@link DataAccess#cursor(long)}. */
  @Test
  public final void testCursorLong() {
    long off = STR_BIN.length + BYTE_BIN.length + LONG_BIN.length +
        INT_BIN.length + CINT5_BIN.length + CINT4_BIN.length + CINT2_BIN.length;
    da.cursor(off);
    assertEquals(CINT1, da.readNum());

    off -= CINT2_BIN.length;
    da.cursor(off);
    assertEquals(CINT2, da.readNum());

    off -= CINT4_BIN.length;
    da.cursor(off);
    assertEquals(CINT4, da.readNum());

    off -= CINT5_BIN.length;
    da.cursor(off);
    assertEquals(CINT5, da.readNum());

    off -= INT_BIN.length;
    da.cursor(off);
    assertEquals(INT, da.read4());

    off -= LONG_BIN.length;
    da.cursor(off);
    assertEquals(LONG, da.read5());

    off -= BYTE_BIN.length;
    da.cursor(off);
    assertEquals(BYTE, da.read1());

    off -= STR_BIN.length;
    da.cursor(off);
    assertEquals(STR, Token.string(da.readToken()));
  }

  /**
   * Test method for {@link DataAccess#write4(long, int)}.
   * @throws IOException I/O exception
   */
  @Test
  public final void testWrite4LongInt() throws IOException {
    final long pos = RANDOM_POS;
    da.write4(pos, INT);
    da.flush();

    assertContent(pos, INT_BIN);
  }

  /**
   * Test method for {@link DataAccess#write4(int)}.
   * @throws IOException I/O exception
   */
  @Test
  public final void testWrite4Int() throws IOException {
    final long pos = RANDOM_POS;
    da.cursor(pos);
    da.write4(pos, INT);
    da.flush();

    assertContent(pos, INT_BIN);
  }

  /**
   * Test method for {@link DataAccess#writeToken(long, byte[])}.
   * @throws IOException I/O exception
   */
  @Test
  public final void testWriteToken() throws IOException {
    final long pos = RANDOM_POS;
    da.writeToken(pos, Token.token(STR));
    da.flush();

    assertContent(pos, STR_BIN);
  }

  /**
   * Test method for {@link DataAccess#writeToken(long, byte[])}.
   * @throws IOException I/O exception
   */
  @Test
  public final void testWriteTokenBig() throws IOException {
    final long pos = RANDOM_POS;
    da.writeToken(pos, Token.token(STR_LONG));
    da.flush();

    assertContent(pos, STR_LONG_BIN);
  }

  /** Performance test for {@link DataAccess#writeToken(long, byte[])}. */
  @Test
  public final void testPerfWriteTokenBig() {
    final byte[] token = Token.token(STR_LONG);
    for(int i = 0; i < 10000; ++i) da.writeToken(da.cursor(), token);
  }

  /**
   * Test method for {@link DataAccess#writeNum(long, int)}.
   * @throws IOException I/O exception
   */
  @Test
  public final void testWriteNum() throws IOException {
    long pos = RANDOM_POS;
    da.cursor(pos);
    da.writeNum(da.cursor(), CINT5);
    da.writeNum(da.cursor(), CINT4);
    da.writeNum(da.cursor(), CINT2);
    da.writeNum(da.cursor(), CINT1);
    da.flush();

    assertContent(pos, CINT5_BIN);
    pos += CINT5_BIN.length;
    assertContent(pos, CINT4_BIN);
    pos += CINT4_BIN.length;
    assertContent(pos, CINT2_BIN);
    pos += CINT2_BIN.length;
    assertContent(pos, CINT1_BIN);
  }

  /** Test method for {@link DataAccess#free(long, int)}. */
  @Ignore
  @Test
  public final void testFree() {
    fail("Not yet implemented");
  }

  /**
   * Check that the test file {@link #file} has the specified unsigned bytes at
   * the specified position.
   * @param pos file position
   * @param bytes expected unsigned bytes
   * @throws IOException I/O exception
   */
  void assertContent(final long pos, final int[] bytes) throws IOException {
    final RandomAccessFile f = new RandomAccessFile(file.file(), "r");
    try {
      f.seek(pos);
      for(final int b : bytes)
        assertEquals(b, f.read());
    } finally {
      f.close();
    }
  }

  /**
   * Write initialization data.
   * @param out file.
   * @throws IOException I/O exception
   */
  private static void initialContent(final RandomAccessFile out) throws IOException {
    write(out, STR_BIN);
    write(out, BYTE_BIN);
    write(out, LONG_BIN);
    write(out, INT_BIN);
    write(out, CINT5_BIN);
    write(out, CINT4_BIN);
    write(out, CINT2_BIN);
    write(out, CINT1_BIN);

    final long off = out.getFilePointer() >>> 12 << 12;
    out.seek(off + BLOCK_BOUNDARY_POS);
    write(out, STR_BIN);
  }

  /**
   * Write an array of unsigned bytes in the specified file.
   * @param out file
   * @param bytes unsigned bytes
   * @throws IOException I/O exception
   */
  private static void write(final RandomAccessFile out, final int[] bytes) throws IOException {
    for(final int b : bytes)
      out.write(b);
  }

  /**
   * Convert the last byte of a value to an unsigned byte.
   * @param v value
   * @return the last byte (unsigned)
   */
  private static int toUnsignedByte(final long v) {
    return (int) (v & 0xFFL);
  }

  /**
   * Convert the last byte of a value to an unsigned byte.
   * @param v value
   * @return the last byte (unsigned)
   */
  private static int toUnsignedByte(final int v) {
    return v & 0xFF;
  }

  /**
   * Convert the last byte of a value to an unsigned byte.
   * @param v value
   * @return the last byte (unsigned)
   */
  private static int toUnsignedByte(final byte v) {
    return v & 0xFF;
  }

  /**
   * Get the binary representation of a string as defined in
   * {@link DataAccess#writeToken(long, byte[])}.
   * @param s string
   * @return array of unsigned bytes
   */
  private static int[] stringToByteArray(final String s) {
    final byte[] token = Token.token(s);
    final int[] len = numToByteArray(token.length);

    final int[] bytes = new int[len.length + token.length];
    System.arraycopy(len, 0, bytes, 0, len.length);
    for(int i = 0; i < token.length; ++i)
      bytes[len.length + i] = toUnsignedByte(token[i]);

    return bytes;
  }

  /**
   * Get the binary representation of a long (5-byte long) as defined in
   * {@link DataAccess#read5()}.
   * @param v long (5-byte long)
   * @return array of unsigned bytes
   */
  private static int[] longToByteArray(final long v) {
    return new int[] {
        toUnsignedByte(v >>> 32),
        toUnsignedByte(v >>> 24),
        toUnsignedByte(v >>> 16),
        toUnsignedByte(v >>>  8),
        toUnsignedByte(v)
    };
  }

  /**
   * Get the binary representation of an integer as defined in
   * {@link DataAccess#write4(int)}.
   * @param v integer
   * @return array of unsigned bytes
   */
  private static int[] intToByteArray(final int v) {
    return new int[] {
        toUnsignedByte(v >>> 24),
        toUnsignedByte(v >>> 16),
        toUnsignedByte(v >>>  8),
        toUnsignedByte(v)
    };
  }

  /**
   * Get the compressed binary representation of an integer as defined in
   * {@code DataAccess.writeNum(int)}.
   * @param v integer
   * @return array of unsigned bytes
   */
  private static int[] numToByteArray(final int v) {
    if(v < 0 || v > 0x3FFFFFFF) {
      return new int[] {
          0xC0,
          toUnsignedByte(v >>> 24),
          toUnsignedByte(v >>> 16),
          toUnsignedByte(v >>> 8),
          toUnsignedByte(v) };
    } else if(v > 0x3FFF) {
      return new int[] {
          toUnsignedByte(v >>> 24 | 0x80),
          toUnsignedByte(v >>> 16),
          toUnsignedByte(v >>> 8),
          toUnsignedByte(v) };
    } else if(v > 0x3F) {
      return new int[] {
          toUnsignedByte(v >>> 8 | 0x40),
          toUnsignedByte(v) };
    } else {
      return new int[] { v };
    }
  }

  /**
   * Construct a new string by repeating a given string several times.
   * @param s string
   * @param n number of time to repeat the string
   * @return result string
   */
  private static String repeat(final String s, final int n) {
    final StringBuilder str = new StringBuilder(n * s.length());
    for(int i = 0; i < n; ++i) str.append(s);
    return str.toString();
  }
}
