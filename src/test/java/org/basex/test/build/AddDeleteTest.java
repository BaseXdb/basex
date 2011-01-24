package org.basex.test.build;

import static org.junit.Assert.*;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.Add;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.Delete;
import org.basex.core.cmd.DropDB;
import org.basex.io.IO;
import org.basex.util.Util;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests adding files/folders/zip files/urls to collections.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Michael Seiferle
 */
public final class AddDeleteTest {
  /** Database context. */
  private static final Context CTX = new Context();

  /** Test database name. */
  private static final String DBNAME = Util.name(AddDeleteTest.class);
  /** Test file. */
  private static final String FILE = "etc/xml/input.xml";
  /** Test folder. */
  private static final String FLDR = "etc/xml/dir";
  /** Test ZIP file, same as etc/xml. */
  private static final String ZIPFILE = "etc/xml/xml.zip";
  /** Test GZIP file. */
  private static final String GZIPFILE = "etc/xml/xml.gz";
  /** Test XML fragment. */
  private static final String XMLFRAG = "<xml a='blu'><foo /></xml>";

  /** Number of XML files for folder. */
  private static final int FCNT;

  static {
    int fc = 0;
    for(final IO c : IO.get(FLDR).children()) {
      if(c.name().endsWith(IO.XMLSUFFIX)) ++fc;
    }
    FCNT = fc;
  }

  /**
   * Creates a database.
   * @throws BaseXException exception
   */
  @Before
  public void setUp() throws BaseXException {
    new CreateDB(DBNAME).execute(CTX);
  }

  /**
   * Drops the database.
   * @throws BaseXException exception
   */
  @After
  public void tearDown() throws BaseXException {
    new DropDB(DBNAME).execute(CTX);
  }

  /**
   * Adds an XML fragment to the database.
   * 1) with name and w/o target
   * 2) with name and target set
   * 3) w/o name and target set
   * @throws BaseXException database exception
   */
  @Test
  public void testAddXMLString() throws BaseXException {
    new Add(XMLFRAG, "index.xml").execute(CTX);
    assertEquals(1, CTX.doc().length);
    new Add(XMLFRAG, "index2.xml", "a/b/c").execute(CTX);
    assertEquals(2, CTX.doc().length);
    new Add(XMLFRAG, null, "a/d/c").execute(CTX);
    assertEquals(3, CTX.doc().length);
  }

  /**
   * Adds a single file to the database.
   * @throws BaseXException exception
   */
  @Test
  public void testAddFile() throws BaseXException {
    new Add(FILE).execute(CTX);
    assertEquals(1, CTX.doc().length);
  }

  /**
   * Adds a zip file.
   * @throws BaseXException exception
   */
  @Test
  public void testAddZip() throws BaseXException {
    new Add(ZIPFILE, null, "target").execute(CTX);
    assertEquals(4, CTX.doc().length);
  }

  /**
   * Adds/deletes a GZIP file.
   * @throws BaseXException exception
   */
  @Test
  public void testAddGzip() throws BaseXException {
    new Add(GZIPFILE).execute(CTX);
    new Add(GZIPFILE, null, "bar").execute(CTX);
    new Delete("bar").execute(CTX);
    assertEquals(1, CTX.doc().length);
  }

  /**
   * Adds a folder. As etc/xml contains a zip file, it is added as well.
   * @throws BaseXException exception
   */
  @Test
  public void testAddFolder() throws BaseXException {
    new Add(FLDR).execute(CTX);
    assertEquals(FCNT, CTX.doc().length);
  }

  /**
   * Adds/deletes with target.
   * @throws BaseXException exception
   */
  @Test
  public void deletePath() throws BaseXException {
    new Add(FLDR, null, "foo/pub").execute(CTX);
    new Add(FILE, null, "/foo///bar////").execute(CTX);
    new Add(FLDR, null, "foobar").execute(CTX);
    new Delete("foo").execute(CTX);
    assertEquals(FCNT, CTX.doc().length);
  }

  /**
   * Adds/deletes a file/folder.
   * @throws BaseXException exception
   */
  @Test
  public void addFoldersDeleteFiles() throws BaseXException {
    new Add(FLDR, null, "folder").execute(CTX);
    new Add(FILE).execute(CTX);
    new Delete("input.xml").execute(CTX);
    new Delete("folder/input.xml").execute(CTX);
    assertEquals(FCNT - 1, CTX.doc().length);
  }

  /**
   * Adds a non-existent file.
   * @throws BaseXException expected.
   */
  @Test(expected = BaseXException.class)
  public void testAddFileFail() throws BaseXException {
    new Add(FILE + "/doesnotexist").execute(CTX);
  }
}
