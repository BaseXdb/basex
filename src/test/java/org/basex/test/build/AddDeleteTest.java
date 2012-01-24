package org.basex.test.build;

import static org.junit.Assert.*;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.Add;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.Delete;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Set;
import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.util.Token;
import org.basex.util.Util;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests adding files/folders/zip files/urls to collections.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Michael Seiferle
 */
public final class AddDeleteTest {
  /** Database context. */
  private static final Context CONTEXT = new Context();

  /** Test database name. */
  private static final String DB = Util.name(AddDeleteTest.class);
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
    new CreateDB(DB).execute(CONTEXT);
  }

  /**
   * Drops the database.
   * @throws BaseXException exception
   */
  @After
  public void tearDown() throws BaseXException {
    new DropDB(DB).execute(CONTEXT);
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
    new Add("index.xml", XMLFRAG).execute(CONTEXT);
    assertEquals(1, docs());
    new Add("a/b/c/index2.xml", XMLFRAG).execute(CONTEXT);
    assertEquals(2, docs());
    new Add("a/d/c", XMLFRAG).execute(CONTEXT);
    assertEquals(3, docs());
  }

  /**
   * Adds a single file to the database.
   * @throws BaseXException exception
   */
  @Test
  public void addFile() throws BaseXException {
    new Add(null, FILE).execute(CONTEXT);
    assertEquals(1, docs());
  }

  /**
   * Adds a zip file.
   * @throws BaseXException exception
   */
  @Test
  public void addZip() throws BaseXException {
    new Add("target", ZIPFILE).execute(CONTEXT);
    assertEquals(4, docs());
    // do not add archives
    new Set(Prop.ADDARCHIVES, false).execute(CONTEXT);
    new Add("", ZIPFILE).execute(CONTEXT);
    assertEquals(4, docs());
    new Set(Prop.ADDARCHIVES, true).execute(CONTEXT);
  }

  /**
   * Adds/deletes a GZIP file.
   * @throws BaseXException exception
   */
  @Test
  public void addGzip() throws BaseXException {
    new Add("", GZIPFILE).execute(CONTEXT);
    new Add("bar", GZIPFILE).execute(CONTEXT);
    new Delete("bar").execute(CONTEXT);
    assertEquals(1, docs());
  }

  /**
   * Adds a folder. The contained a zip file is added as well.
   * @throws BaseXException exception
   */
  @Test
  public void addFolder() throws BaseXException {
    new Add("", FLDR).execute(CONTEXT);
    assertEquals(NFLDR, docs());
  }

  /**
   * Adds/deletes with target.
   * @throws BaseXException exception
   */
  @Test
  public void deletePath() throws BaseXException {
    new Add("foo/pub", FLDR).execute(CONTEXT);
    assertEquals(NFLDR, docs());
    new Delete("foo").execute(CONTEXT);
    assertEquals(0, docs());
    new Add("/foo///bar////", FILE).execute(CONTEXT);
    new Add("foobar", FLDR).execute(CONTEXT);
    new Delete("foo").execute(CONTEXT);
    assertEquals(NFLDR, docs());
  }

  /**
   * Adds/deletes a file/folder.
   * @throws BaseXException exception
   */
  @Test
  public void addFoldersDeleteFiles() throws BaseXException {
    new Add("folder", FLDR).execute(CONTEXT);
    new Add("", FILE).execute(CONTEXT);
    new Delete("input.xml").execute(CONTEXT);
    new Delete("folder/input.xml").execute(CONTEXT);
    assertEquals(NFLDR - 1, docs());
  }

  /**
   * Adds a non-existent file.
   * @throws BaseXException expected.
   */
  @Test(expected = BaseXException.class)
  public void addFileFail() throws BaseXException {
    new Add("", FILE + "/doesnotexist").execute(CONTEXT);
  }

  /**
   * Adds a broken input file to the database and checks if the file can be
   * deleted afterwards.
   * @throws Exception exception
   */
  @Test
  public void addCorrupt() throws Exception {
    final IOFile io = new IOFile(Prop.TMP, DB);
    io.write(Token.token("<x"));
    try {
      new Add("", io.path()).execute(CONTEXT);
      fail("Broken file was added to the database.");
    } catch(final Exception ex) { }

    assertTrue(io.delete());
  }

  /**
   * Creates a database from a broken input file and checks if the file can be
   * deleted afterwards.
   * @throws Exception exception
   */
  @Test
  public void createCorrupt() throws Exception {
    try {
      new CreateDB(DB, "<x").execute(CONTEXT);
      fail("Broken file was added to the database.");
    } catch(final Exception ex) { }

    final IOFile io = new IOFile(Prop.TMP, DB);
    io.write(Token.token("<x"));
    try {
      new CreateDB(DB, io.path()).execute(CONTEXT);
      fail("Broken file was added to the database.");
    } catch(final Exception ex) { }
    assertTrue(io.delete());
  }

  /**
   * Skips a corrupt file.
   * @throws Exception exception
   */
  @Test
  public void skipCorrupt() throws Exception {
    final IOFile io = new IOFile(Prop.TMP, DB);
    io.write(Token.token("<x"));

    new Set(Prop.SKIPCORRUPT, true).execute(CONTEXT);
    assertEquals(0, CONTEXT.data().resources.docs("").size());
    new Add("x", "<x").execute(CONTEXT);
    new Add("x", CORRUPT).execute(CONTEXT);
    assertEquals(0, CONTEXT.data().resources.docs("").size());
    new Set(Prop.SKIPCORRUPT, false).execute(CONTEXT);

    try {
      new Add("", "<x").execute(CONTEXT);
      fail("Broken file was added to the database.");
    } catch(final Exception ex) { }

    assertTrue(io.delete());
  }

  /**
   * Returns the number of documents in the current database.
   * @return number of documents
   */
  private int docs() {
    return CONTEXT.data().resources.docs("").size();
  }
}
