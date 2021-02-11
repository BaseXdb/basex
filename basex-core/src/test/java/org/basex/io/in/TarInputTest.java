package org.basex.io.in;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.*;

import org.basex.io.*;
import org.junit.jupiter.api.*;

/**
 * Test class for the {@link TarInputStream}.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class TarInputTest {
  /**
   * Scan TAR archive.
   * @throws IOException I/O exception
   */
  @Test public void scan() throws IOException {
    final ArrayList<TarEntry> entries = entries(false);
    assertEquals("a.txt", entries.get(0).getName());
    assertEquals("b.txt", entries.get(1).getName());
    assertEquals(11, entries.get(0).getSize());
    assertEquals(5, entries.get(1).getSize());
  }

  /**
   * Read TAR archive.
   * @throws IOException I/O exception
   */
  @Test public void read() throws IOException {
    final ArrayList<TarEntry> entries = entries(true);
    assertEquals("a.txt", entries.get(0).getName());
    assertEquals("b.txt", entries.get(1).getName());
    assertEquals(11, entries.get(0).getSize());
    assertEquals(5, entries.get(1).getSize());
  }

  /**
   * Parses a tar archive.
   * @param read read contents
   * @return entries
   * @throws IOException I/O exception
   */
  private static ArrayList<TarEntry> entries(final boolean read) throws IOException {
    final ArrayList<TarEntry> entries = new ArrayList<>();
    final IOFile input = new IOFile("src/test/resources/tar.tar");
    try(TarInputStream is = new TarInputStream(input.inputStream())) {
      for(TarEntry ze; (ze = is.getNextEntry()) != null;) entries.add(ze);
      while(read && is.read() != -1);
    }
    return entries;
  }
}
