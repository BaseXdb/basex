package org.basex.build;

import static org.junit.Assert.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.*;
import org.basex.util.*;
import org.junit.*;

/**
 * Tests adding files/folders/zip files/urls to collections.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Michael Seiferle
 */
public final class AddDeleteTest extends SandboxTest {
  /** Test file. */
  private static final String DIR = "src/test/resources/";
  /** Test file. */
  private static final String FILE = DIR + "input.xml";
  /** Test file. */
  private static final String CORRUPT = DIR + "corrupt.xml";
  /** Test folder. */
  private static final String FLDR = DIR + "dir";
  /** Test ZIP file. */
  private static final String ZIPFILE = DIR + "xml.zip";
  /** Test GZIP file. */
  private static final String GZIPFILE = DIR + "xml.gz";
  /** Test XML fragment. */
  private static final String XMLFRAG = "<xml a='blu'><foo /></xml>";
  /** Temporary XML file. */
  private static final String TEMP = NAME + IO.XMLSUFFIX;

  /** Number of XML files for folder. */
  private static final int NFLDR;

  static {
    int fc = 0;
    for(final IOFile c : new IOFile(FLDR).children()) {
      if(c.name().endsWith(IO.XMLSUFFIX)) ++fc;
    }
    NFLDR = fc;
  }

  /**
   * Creates a database.
   * @throws BaseXException exception
   */
  @Before
  public void setUp() throws BaseXException {
    new CreateDB(NAME).execute(context);
  }

  /**
   * Drops the database.
   * @throws BaseXException exception
   */
  @After
  public void tearDown() throws BaseXException {
    new DropDB(NAME).execute(context);
  }

  /**
   * <p>Adds an XML fragment to the database.</p><ol>
   * <li> with name and w/o target</li>
   * <li> with name and target set</li>
   * <li> w/o name and target set</li></ol>
   * @throws BaseXException database exception
   */
  @Test
  public void addXMLString() throws BaseXException {
    new Add("index.xml", XMLFRAG).execute(context);
    assertEquals(1, docs());
    new Add("a/b/c/index2.xml", XMLFRAG).execute(context);
    assertEquals(2, docs());
    new Add("a/d/c", XMLFRAG).execute(context);
    assertEquals(3, docs());
  }

  /**
   * Adds a single file to the database.
   * @throws BaseXException exception
   */
  @Test
  public void addFile() throws BaseXException {
    new Add(null, FILE).execute(context);
    assertEquals(1, docs());
  }

  /**
   * Adds a zip file.
   * @throws BaseXException exception
   */
  @Test
  public void addZip() throws BaseXException {
    new Add("target", ZIPFILE).execute(context);
    assertEquals(4, docs());
    // do not add archives
    new Set(MainOptions.ADDARCHIVES, false).execute(context);
    new Add("", ZIPFILE).execute(context);
    assertEquals(4, docs());
    new Set(MainOptions.ADDARCHIVES, true).execute(context);
  }

  /**
   * Adds/deletes a GZIP file.
   * @throws BaseXException exception
   */
  @Test
  public void addGzip() throws BaseXException {
    new Add("", GZIPFILE).execute(context);
    new Add("bar", GZIPFILE).execute(context);
    new Delete("bar").execute(context);
    assertEquals(1, docs());
  }

  /**
   * Adds a folder. The contained a zip file is added as well.
   * @throws BaseXException exception
   */
  @Test
  public void addFolder() throws BaseXException {
    new Add("", FLDR).execute(context);
    assertEquals(NFLDR, docs());
  }

  /**
   * Adds/deletes with target.
   * @throws BaseXException exception
   */
  @Test
  public void deletePath() throws BaseXException {
    new Add("foo/pub", FLDR).execute(context);
    assertEquals(NFLDR, docs());
    new Delete("foo").execute(context);
    assertEquals(0, docs());
    new Add("/foo///bar////", FILE).execute(context);
    new Add("foobar", FLDR).execute(context);
    new Delete("foo").execute(context);
    assertEquals(NFLDR, docs());
  }

  /**
   * Adds/deletes a file/folder.
   * @throws BaseXException exception
   */
  @Test
  public void addFoldersDeleteFiles() throws BaseXException {
    new Add("folder", FLDR).execute(context);
    new Add("", FILE).execute(context);
    new Delete("input.xml").execute(context);
    new Delete("folder/input.xml").execute(context);
    assertEquals(NFLDR - 1, docs());
  }

  /**
   * Adds/deletes with target.
   * @throws BaseXException exception
   */
  @Test
  public void createDeleteAdd() throws BaseXException {
    new CreateDB(NAME, "<a/>").execute(context);
    new Delete("/").execute(context);
    assertEquals(0, docs());
    final StringBuilder sb = new StringBuilder();
    for(int i = 0; i < 256; i++) sb.append("<a").append(i).append("/>");
    new Add("x", "<x>" + sb + "</x>").execute(context);
    assertEquals(1, docs());
    assertEquals("1", new XQuery("count(//x)").execute(context));
    assertEquals("0", new XQuery("count(//a)").execute(context));
  }

  /**
   * Adds a non-existent file.
   * @throws BaseXException expected.
   */
  @Test(expected = BaseXException.class)
  public void addFileFail() throws BaseXException {
    new Add("", FILE + "/doesnotexist").execute(context);
  }

  /**
   * Adds a broken input file to the database and checks if the file can be
   * deleted afterwards.
   * @throws Exception exception
   */
  @Test
  public void addCorrupt() throws Exception {
    final IOFile io = new IOFile(TEMP);
    io.write(Token.token("<x"));
    try {
      new Add("", io.path()).execute(context);
      fail("Broken file was added to the database.");
    } catch(final Exception ex) { }

    assertTrue(io.delete());
  }

  /**
   * Creates a database from a broken input.
   * @throws BaseXException exception
   */
  @Test(expected = BaseXException.class)
  public void createCorrupt() throws BaseXException {
    new CreateDB(NAME, "<x").execute(context);
  }

  /**
   * Creates a database from a broken input file.
   * @throws IOException exception
   */
  @Test(expected = BaseXException.class)
  public void createCorruptFromFile() throws IOException {
    final IOFile io = new IOFile(TEMP);
    io.write(Token.token("<x"));
    try {
      new CreateDB(NAME, io.path()).execute(context);
    } finally {
      io.delete();
    }
  }

  /**
   * Skips a corrupt file.
   * @throws Exception exception
   */
  @Test
  public void skipCorrupt() throws Exception {
    final IOFile io = new IOFile(TEMP);
    io.write(Token.token("<x"));

    new Set(MainOptions.SKIPCORRUPT, true).execute(context);
    assertEquals(0, context.data().resources.docs("").size());
    new Add("x", "<x").execute(context);
    new Add("x", CORRUPT).execute(context);
    assertEquals(0, context.data().resources.docs("").size());
    new Set(MainOptions.SKIPCORRUPT, false).execute(context);

    try {
      new Add("", "<x").execute(context);
      fail("Broken file was added to the database.");
    } catch(final Exception ex) { }

    assertTrue(io.delete());
  }

  /**
   * Returns the number of documents in the current database.
   * @return number of documents
   */
  private static int docs() {
    return context.data().resources.docs("").size();
  }
}
