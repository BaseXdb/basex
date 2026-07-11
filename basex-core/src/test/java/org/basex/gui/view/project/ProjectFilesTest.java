package org.basex.gui.view.project;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

import org.junit.jupiter.api.*;

/**
 * Tests the streaming content matcher of the project view.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ProjectFilesTest {
  /** Temporary file. */
  private Path file;

  /**
   * Creates the temporary file.
   * @throws IOException I/O exception
   */
  @BeforeEach public void before() throws IOException {
    file = Files.createTempFile("basex-filter", ".txt");
  }

  /**
   * Deletes the temporary file.
   * @throws IOException I/O exception
   */
  @AfterEach public void after() throws IOException {
    Files.deleteIfExists(file);
  }

  /**
   * Regression: self-overlapping patterns that the old naive scan missed.
   * @throws IOException I/O exception
   */
  @Test public void overlap() throws IOException {
    write("aaaab");
    assertTrue(contains("aab"), "\"aab\" occurs in \"aaaab\"");
    write("aabaabaaab");
    assertTrue(contains("aaab"), "\"aaab\" occurs in \"aabaabaaab\"");
    write("aaaa");
    assertFalse(contains("aab"), "\"aab\" does not occur in \"aaaa\"");
  }

  /**
   * Differential fuzz test: the case-sensitive scan must agree with {@link String#contains}.
   * @throws IOException I/O exception
   */
  @Test public void differential() throws IOException {
    // small alphabets maximise self-overlap, the case the old naive scan got wrong
    final Random rnd = new Random(0x5EED);
    for(final String alphabet : new String[] { "ab", "abc" }) {
      for(int t = 0; t < 500; t++) {
        final String text = random(rnd, alphabet, rnd.nextInt(20));
        write(text);
        for(int p = 0; p < 5; p++) {
          final String search = random(rnd, alphabet, 1 + rnd.nextInt(6));
          assertEquals(text.contains(search), contains(search),
              "search \"" + search + "\" in \"" + text + '"');
        }
      }
    }
  }

  /**
   * Differential fuzz test: the case-insensitive scan must agree with a lower-cased
   * {@link String#contains}.
   * @throws IOException I/O exception
   */
  @Test public void differentialFold() throws IOException {
    final Random rnd = new Random(0xF01D);
    final String alphabet = "aAbB";
    for(int t = 0; t < 500; t++) {
      final String text = random(rnd, alphabet, rnd.nextInt(20));
      write(text);
      for(int p = 0; p < 5; p++) {
        final String search = random(rnd, alphabet, 1 + rnd.nextInt(6));
        final boolean expected = text.toLowerCase(Locale.ENGLISH).contains(
            search.toLowerCase(Locale.ENGLISH));
        assertEquals(expected, containsFold(search),
            "search \"" + search + "\" in \"" + text + '"');
      }
    }
  }

  /**
   * Writes text to the temporary file.
   * @param text file content
   * @throws IOException I/O exception
   */
  private void write(final String text) throws IOException {
    Files.write(file, text.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Runs a case-sensitive streaming search of the temporary file.
   * @param search search string
   * @return result of check
   */
  private boolean contains(final String search) {
    final int[] cps = search.codePoints().toArray();
    return ProjectFiles.filterContent(file.toString(), cps, ProjectFiles.prefixes(cps), false)
        == ProjectFiles.FOUND;
  }

  /**
   * Runs a case-insensitive streaming search of the temporary file.
   * @param search search string
   * @return result of check
   */
  private boolean containsFold(final String search) {
    // the streaming scan lower-cases file characters; the search string is lower-cased upfront
    final int[] cps = search.toLowerCase(Locale.ENGLISH).codePoints().toArray();
    return ProjectFiles.filterContent(file.toString(), cps, ProjectFiles.prefixes(cps), true)
        == ProjectFiles.FOUND;
  }

  /**
   * Generates a random string.
   * @param rnd random generator
   * @param alphabet characters to choose from
   * @param length string length
   * @return random string
   */
  private static String random(final Random rnd, final String alphabet, final int length) {
    final StringBuilder sb = new StringBuilder(length);
    for(int i = 0; i < length; i++) sb.append(alphabet.charAt(rnd.nextInt(alphabet.length())));
    return sb.toString();
  }
}
