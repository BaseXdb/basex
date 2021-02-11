package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;
import static org.basex.util.Token.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.zip.*;

import org.basex.*;
import org.basex.io.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the ZIP Module.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ZipModuleTest extends SandboxTest {
  /** Test ZIP file. */
  private static final String ZIP = "src/test/resources/xml.zip";
  /** Temporary ZIP file. */
  private static final String TMPZIP = Prop.TEMPDIR + NAME + ".zip";
  /** Temporary file. */
  private static final String TMPFILE = Prop.TEMPDIR + NAME + ".tmp";
  /** Test ZIP entry. */
  private static final String ENTRY1 = "infos/stopWords";
  /** Test ZIP entry. */
  private static final String ENTRY2 = "test/input.xml";

  /**
   * Initializes the test.
   * @throws IOException I/O exception.
   */
  @BeforeAll public static void init() throws IOException {
    // create temporary file
    new IOFile(TMPFILE).write("!");
  }

  /** Finishes the test. */
  @AfterAll public static void finish() {
    new IOFile(TMPZIP).delete();
    new IOFile(TMPFILE).delete();
  }

  /** Test method. */
  @Test public void binaryEntry() {
    final Function func = _ZIP_BINARY_ENTRY;
    query(func.args(ZIP, ENTRY1));
    contains("string(xs:hexBinary(" + func.args(ZIP, ENTRY1) + "))", "610A61626F");

    error(func.args("abc", "xyz"), ZIP_NOTFOUND_X);
    error(func.args(ZIP, ""), ZIP_NOTFOUND_X);
  }

  /** Test method. */
  @Test public void entries() {
    final Function func = _ZIP_ENTRIES;
    query(func.args(ZIP));
  }

  /** Test method. */
  @Test public void textEntry() {
    final Function func = _ZIP_TEXT_ENTRY;
    query(func.args(ZIP, ENTRY1));
    query(func.args(ZIP, ENTRY1, "US-ASCII"));
    error(func.args(ZIP, ENTRY1, "xyz"), ZIP_FAIL_X);
    // newlines are removed from the result..
    contains(func.args(ZIP, ENTRY1), "a\nabout\nabove\n");
  }

  /** Test method. */
  @Test public void xmlEntry() {
    final Function func = _ZIP_XML_ENTRY;
    query(func.args(ZIP, ENTRY2));
    query(func.args(ZIP, ENTRY2) + "//title/text()", "XML");
  }

  /**
   * Test method.
   * @throws IOException I/O exception
   */
  @Test public void zipFile() throws IOException {
    final Function func = _ZIP_ZIP_FILE;
    // check first file
    query(func.args(params("<entry name='one'/>")));
    checkEntry("one", new byte[0]);
    // check second file
    query(func.args(params("<entry name='two'>!</entry>")));
    checkEntry("two", new byte[] { '!' });
    // check third file
    query(func.args(params("<entry name='three' encoding='UTF-16'>!</entry>")));
    checkEntry("three", new byte[] { '\0', '!' });
    // check fourth file
    query(func.args(params("<entry name='four' src='" + TMPFILE + "'/>")));
    checkEntry("four", new byte[] { '!' });
    // check fifth file
    query(func.args(params("<entry src='" + TMPFILE + "'/>")));
    checkEntry(NAME + ".tmp", new byte[] { '!' });
    // check sixth file
    query(func.args(params("<dir name='a'><entry name='b' src='" + TMPFILE + "'/></dir>")));
    checkEntry("a/b", new byte[] { '!' });

    // error: duplicate entry specified
    error(func.args(params("<entry src='" + TMPFILE + "'/>" +
        "<entry src='" + TMPFILE + "'/>")), ZIP_FAIL_X);
  }

  /**
   * Test method.
   * @throws IOException I/O exception
   */
  @Test public void zipFileNamespaces() throws IOException {
    final Function func = _ZIP_ZIP_FILE;
    // ZIP namespace must be removed from zipped node
    query(func.args(params("<entry name='1'><a/></entry>")));
    checkEntry("1", token("<a/>"));
    // ZIP namespace must be removed from zipped node
    query(func.args(params("<entry name='2'><a b='c'/></entry>")));
    checkEntry("2", token("<a b=\"c\"/>"));
    // ZIP namespace must be removed from zipped node and its descendants
    query(func.args(params("<entry name='3'><a><b/></a></entry>")));
    checkEntry("3", token("<a>" + Prop.NL + "  <b/>" + Prop.NL + "</a>"));
    // ZIP namespace must be removed from zipped entry
    query(func.args(params("<entry name='4'><a xmlns=''/></entry>")));
    checkEntry("4", token("<a/>"));

    // ZIP namespace must be removed from zipped entry
    query(func.args(paramsPrefix("5", "<a/>")));
    checkEntry("5", token("<a/>"));
    query(func.args(paramsPrefix("6", "<a><b/></a>")));
    checkEntry("6", token("<a>" + Prop.NL + "  <b/>" + Prop.NL + "</a>"));
    query(func.args(paramsPrefix("7", "<z:a xmlns:z='z'/>")));
    checkEntry("7", token("<z:a xmlns:z=\"z\"/>"));
    query(func.args(paramsPrefix("8", "<zip:a xmlns:zip='z'/>")));
    checkEntry("8", token("<zip:a xmlns:zip=\"z\"/>"));
    query(func.args(paramsPrefix("9", "<a xmlns='z'/>")));
    checkEntry("9", token("<a xmlns=\"z\"/>"));
  }

  /**
   * Returns a zip archive description with ZIP prefix.
   * @param name file name
   * @param entry entry
   * @return parameter string
   */
  private static String paramsPrefix(final String name, final String entry) {
    return " <zip:file xmlns:zip='http://expath.org/ns/zip' href='" +
        new IOFile(TMPZIP).path() + "'>" +
        "<zip:entry name='" + name + "'>" + entry + "</zip:entry></zip:file>";
  }

  /** Test method. */
  @Test public void updateEntries() {
    final Function func = _ZIP_UPDATE_ENTRIES;
    String list = query(_ZIP_ENTRIES.args(ZIP));

    // create and compare identical zip file
    query(func.args(' ' + list, TMPZIP));
    final String list2 = query(_ZIP_ENTRIES.args(TMPZIP));
    assertEquals(list.replaceAll(" href=\".*?\"", ""),
        list2.replaceAll(" href=\".*?\"", ""));

    // remove one directory
    list = list.replaceAll("<zip:dir name=.test.>.*</zip:dir>", "");
    query(func.args(' ' + list, TMPZIP));
  }

  /**
   * Returns a zip archive description.
   * @param arg zip arguments
   * @return parameter string
   */
  private static String params(final String arg) {
    return " <file xmlns='http://expath.org/ns/zip' href='" +
        new IOFile(TMPZIP).path() + "'>" + arg + "</file>";
  }

  /**
   * Checks the contents of the specified zip entry.
   * @param file file to be checked
   * @param data expected file contents
   * @throws IOException I/O exception
   */
  private static void checkEntry(final String file, final byte[] data) throws IOException {
    try(ZipFile zf = new ZipFile(TMPZIP)) {
      final ZipEntry ze = zf.getEntry(file);
      assertNotNull(ze, "File not found: " + file);
      final byte[] dt = new IOStream(zf.getInputStream(ze)).read();
      assertTrue(eq(data, dt),
        "Wrong contents in file \"" + file + "\":" + Prop.NL +
          "Expected: " + string(data) + Prop.NL + "Found: " + string(dt));
    }
  }
}
