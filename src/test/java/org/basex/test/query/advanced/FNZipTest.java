package org.basex.test.query.advanced;

import static org.basex.util.Token.*;
import static org.junit.Assert.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.basex.core.Prop;
import org.basex.query.func.Function;
import org.basex.query.util.Err;
import org.basex.util.Util;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the functions of the file library.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FNZipTest extends AdvancedQueryTest {
  /** Temporary file. */
  private static final String NAME = Util.name(FNZipTest.class);
  /** Test ZIP file. */
  private static final String ZIP = "etc/test/xml.zip";
  /** Temporary ZIP file. */
  private static final String TMPZIP = Prop.TMP + NAME + ".zip";
  /** Temporary file. */
  private static final String TMPFILE = Prop.TMP + NAME;
  /** Test ZIP entry. */
  private static final String ENTRY1 = "infos/stopWords";
  /** Test ZIP entry. */
  private static final String ENTRY2 = "test/input.xml";

  /** Initializes the test.
   * @throws Exception exception */
  @BeforeClass
  public static void init() throws Exception {
    // create temporary file
    final FileOutputStream fos = new FileOutputStream(TMPFILE);
    fos.write('!');
    fos.close();
  }

  /** Finishes the test. */
  @AfterClass
  public static void finish() {
    new File(TMPZIP).delete();
    new File(TMPFILE).delete();
  }

  /**
   * Test method for the zip:binary-entry() function.
   * @throws Exception database exception
   */
  @Test
  public void binaryEntry() throws Exception {
    final String fun = check(Function.ZIPBIN);
    query(fun + "('" + ZIP + "', '" + ENTRY1 + "')");
    contains("xs:hexBinary(" + fun + "('" + ZIP + "', '" + ENTRY1 + "'))",
        "610A61626F");

    error(fun + "('abc', 'xyz')", Err.ZIPNOTFOUND);
    error(fun + "('" + ZIP + "', '')", Err.ZIPNOTFOUND);
  }

  /**
   * Test method for the zip:text-entry() function.
   * @throws Exception database exception
   */
  @Test
  public void textEntry() throws Exception {
    final String fun = check(Function.ZIPTEXT);
    query(fun + "('" + ZIP + "', '" + ENTRY1 + "')");
    query(fun + "('" + ZIP + "', '" + ENTRY1 + "', 'US-ASCII')");
    error(fun + "('" + ZIP + "', '" + ENTRY1 + "', 'xyz')", Err.ZIPFAIL);
    // newlines are removed from the result..
    contains(fun + "('" + ZIP + "', '" + ENTRY1 + "')", "aaboutab");
  }

  /**
   * Test method for the zip:xml-entry() function.
   * @throws Exception database exception
   */
  @Test
  public void xmlEntry() throws Exception {
    final String fun = check(Function.ZIPXML);
    query(fun + "('" + ZIP + "', '" + ENTRY2 + "')");
    query(fun + "('" + ZIP + "', '" + ENTRY2 + "')//title/text()", "XML");
  }

  /**
   * Test method for the zip:entries() function.
   * @throws Exception database exception
   */
  @Test
  public void entries() throws Exception {
    final String fun = check(Function.ZIPENTRIES);
    query(fun + "('" + ZIP + "')");
  }

  /**
   * Test method for the zip:zip-file() function.
   * @throws Exception database exception
   */
  @Test
  public void zipFile() throws Exception {
    final String fun = check(Function.ZIPFILE);
    // check first file
    query(fun + "(" + zipParams("<entry name='one'/>") + ")");
    checkZipEntry("one", new byte[0]);
    // check second file
    query(fun + "(" + zipParams("<entry name='two'>!</entry>") + ")");
    checkZipEntry("two", new byte[] { '!' });
    // check third file
    query(fun + "(" +
        zipParams("<entry name='three' encoding='UTF-16'>!</entry>") + ")");
    checkZipEntry("three", new byte[] { '\0', '!' });
    // check fourth file
    query(fun + "(" + zipParams("<entry name='four' src='" +
        TMPFILE + "'/>") + ")");
    checkZipEntry("four", new byte[] { '!' });
    // check fifth file
    query(fun + "(" + zipParams("<entry src='" + TMPFILE + "'/>") + ")");
    checkZipEntry(NAME, new byte[] { '!' });
    // check sixth file
    query(fun + "(" + zipParams("<dir name='a'><entry name='b' src='" +
        TMPFILE + "'/></dir>") + ")");
    checkZipEntry("a/b", new byte[] { '!' });
    /* [CG] update zip files: remove zip namespace
    query(fun + "(" + zipParams("<entry name='seven'><a/></entry>") + ")");
    checkZipEntry("seven", token("<a/>"));
    */

    // error: no entry specified
    error(fun + "(" + zipParams("") + ")", Err.ZIPFAIL);
    // error: duplicate entry specified
    error(fun + "(" + zipParams("<entry src='" + TMPFILE + "'/>" +
        "<entry src='" + TMPFILE + "'/>") + ")", Err.ZIPFAIL);
  }

  /**
   * Test method for the zip:update-entries() function.
   * @throws Exception database exception
   * @throws IOException I/O exception
   */
  @Test
  public void updateEntries() throws Exception {
    final String fun = check(Function.ZIPUPDATE);
    String list = query("zip:entries('" + ZIP + "')");

    // create and compare identical zip file
    query(fun + "(" + list + ", '" + TMPZIP + "')");
    final String list2 = query("zip:entries('" + TMPZIP + "')");
    assertEquals(list.replaceAll(" href=\\\".*?\\\"", ""),
        list2.replaceAll(" href=\\\".*?\\\"", ""));

    // remove one directory
    list = list.replaceAll("<zip:dir name=.test.>.*</zip:dir>", "");
    query(fun + "(" + list + ", '" + TMPZIP + "')");

    // new file has no entries
    list = list.replaceAll("<zip:dir.*</zip:dir>", "");
    error(fun + "(" + list + ", '" +
        new File(TMPZIP).getCanonicalPath() + "')", Err.ZIPFAIL);
  }

  /**
   * Returns a zip archive description.
   * @param arg zip arguments
   * @return parameter string
   * @throws IOException IO exception
   */
  protected static String zipParams(final String arg) throws IOException {
    return "<file xmlns='http://expath.org/ns/zip' href='" +
    new File(TMPZIP).getCanonicalPath() + "'>" + arg + "</file>";
  }

  /**
   * Checks the contents of the specified zip entry.
   * @param file file to be checked
   * @param data expected file contents
   * @throws IOException I/O exception
   */
  protected static void checkZipEntry(final String file, final byte[] data)
      throws IOException {

    ZipFile zf = null;
    try {
      zf = new ZipFile(TMPZIP);
      final ZipEntry ze = zf.getEntry(file);
      assertTrue("File not found: " + file, ze != null);
      final InputStream is = zf.getInputStream(ze);
      final byte[] dt = new byte[(int) ze.getSize()];
      is.read(dt);
      assertTrue("Wrong contents in file \"" + file + "\":" + Prop.NL +
          "Expected: " + string(data) + Prop.NL + "Found: " + string(dt),
          eq(data, dt));
    } finally {
      if(zf != null) zf.close();
    }
  }
}
