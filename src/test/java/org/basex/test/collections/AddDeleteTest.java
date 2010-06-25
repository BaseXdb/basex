package org.basex.test.collections;

import static org.junit.Assert.*;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.proc.Add;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.Delete;
import org.basex.core.proc.DropDB;
import org.basex.io.IO;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests adding files/folders/zip files/urls to collections.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Michael Seiferle
 */
public class AddDeleteTest {
  /** Database context. */
  private static final Context CTX = new Context();

  /** Test file. */
  private static final String FILE = "etc/xml/input.xml";
  /** Test folder. */
  private static final String FLDR = "etc/xml";
  /** Test Url. */
  private static final String URL = "http://www.inf.uni-konstanz.de/"
      + "dbis/basex/dl/xmark.xml";
  /** Test Zipfile, same as etc/xml. */
  private static final String ZIPFILE = "etc/xml/xml.zip";
  /** Test GZIPfile. */
  private static final String GZIPFILE = "etc/xml/build.xml.gz";
  /** Test DB name. */
  private static final String NAME = "CollectionUnitTest";

  /** Number of XML files for folder. */
  private static final int FCNT;

  static {
    int fc = 0;
    for(IO c : IO.get(FLDR).children()) {
      if(c.name().endsWith(".xml")) fc++;
    }
    FCNT = fc + 4; // +4 for the zipfile
  }

  /**
   * Creates the initial database.
   * @throws BaseXException exception
   */
  @Before
  public void setUp() throws BaseXException {
    new CreateDB(NAME).execute(CTX);
  }

  /**
   * Drops the initial collection.
   * @throws BaseXException exception
   */
  @After
  public void tearDown() throws BaseXException {
    new DropDB(NAME).execute(CTX);
  }

  /**
   * Adds an <?xml...>-String to the database.
   * @throws BaseXException exception
   */
  @Test
  public final void testAddXMLString() throws BaseXException {
    new Add("test", "<xml>test</xml>").execute(CTX);
    assertEquals(1, CTX.doc().length);
  }

  /**
   * Adds a single file to the database.
   * @throws BaseXException exception
   */
  @Test
  public final void testAddFile() throws BaseXException {
    new Add("input", FILE).execute(CTX);
    assertEquals(1, CTX.doc().length);
  }

  /**
   * Adds a zip file.
   * @throws BaseXException exception
   */
  @Test
  public void testAddZip() throws BaseXException {
    new Add("xml", "target", ZIPFILE).execute(CTX);
    assertEquals(4, CTX.doc().length);
  }

  /**
   * Adds/deletes a URL.
   * Disabled to allow "offline" execution.
   * @throws BaseXException exception
   */
  // @Test
  public void testAddUrl() throws BaseXException {
    new Add("xmark.xml", URL).execute(CTX);
    new Add("xmark.xml", "bar", URL).execute(CTX);
    new Delete("xmark.xml").execute(CTX);
    assertEquals(1, CTX.doc().length);
  }

  /**
   * Adds/deletes a GZIP file.
   * @throws BaseXException exception
   */
  @Test
  public void testAddGzip() throws BaseXException {
    new Add("build", "build", GZIPFILE).execute(CTX);
    new Add("build", "bar", GZIPFILE).execute(CTX);
    new Delete("bar").execute(CTX);
    assertEquals(1, CTX.doc().length);
  }

  /**
   * Adds a folder. As etc/xml contains a zip file, it is added as well.
   * @throws BaseXException exception
   */
  @Test
  public void testAddFolder() throws BaseXException {
    new Add("xml", FLDR).execute(CTX);
    assertEquals(FCNT, CTX.doc().length);
  }

  /**
   * Adds/deletes with target.
   * @throws BaseXException exception
   */
  @Test
  public void deletePath() throws BaseXException {
    new Add("xml", "foo/pub", FLDR).execute(CTX);
    new Add("input", "foo/bar", FILE).execute(CTX);
    new Add("xml", "foobar", FLDR).execute(CTX);
    new Delete("foo").execute(CTX);
    assertEquals(FCNT, CTX.doc().length);

  }

  /**
   * Adds/deletes a file/folder.
   * @throws BaseXException exception
   */
  @Test
  public void addFoldersDeleteFiles() throws BaseXException {
    new Add("xml", "folder", FLDR).execute(CTX);
    new Add("input.xml", FILE).execute(CTX);
    new Delete("input.xml").execute(CTX);
    new Delete("folder/input.xml").execute(CTX);
    assertEquals(FCNT - 1, CTX.doc().length);
  }

  /**
   * Adds a non-existent file.
   * @throws BaseXException expected.
   */
  @Test(expected = BaseXException.class)
  public final void testAddFileFail() throws BaseXException {
    new Add("input", FILE + "/doesnotexist").execute(CTX);
  }
}
