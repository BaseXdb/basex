package org.basex.test.build;

import static org.junit.Assert.*;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.Add;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.Delete;
import org.basex.core.cmd.DropDB;
import org.basex.io.IO;
import org.basex.util.Token;
import org.basex.util.Util;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests adding files/folders/zip files/urls to collections.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Michael Seiferle
 */
public final class AddDeleteTest {
  /** Database context. */
  private static final Context CONTEXT = new Context();

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
    new CreateDB(DBNAME).execute(CONTEXT);
  }

  /**
   * Drops the database.
   * @throws BaseXException exception
   */
  @After
  public void tearDown() throws BaseXException {
    new DropDB(DBNAME).execute(CONTEXT);
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
    new Add(XMLFRAG, "index.xml").execute(CONTEXT);
    assertEquals(1, CONTEXT.doc().length);
    new Add(XMLFRAG, "index2.xml", "a/b/c").execute(CONTEXT);
    assertEquals(2, CONTEXT.doc().length);
    new Add(XMLFRAG, null, "a/d/c").execute(CONTEXT);
    assertEquals(3, CONTEXT.doc().length);
  }

  /**
   * Adds a single file to the database.
   * @throws BaseXException exception
   */
  @Test
  public void testAddFile() throws BaseXException {
    new Add(FILE).execute(CONTEXT);
    assertEquals(1, CONTEXT.doc().length);
  }

  /**
   * Adds a zip file.
   * @throws BaseXException exception
   */
  @Test
  public void testAddZip() throws BaseXException {
    new Add(ZIPFILE, null, "target").execute(CONTEXT);
    assertEquals(4, CONTEXT.doc().length);
  }

  /**
   * Adds/deletes a GZIP file.
   * @throws BaseXException exception
   */
  @Test
  public void testAddGzip() throws BaseXException {
    new Add(GZIPFILE).execute(CONTEXT);
    new Add(GZIPFILE, null, "bar").execute(CONTEXT);
    new Delete("bar").execute(CONTEXT);
    assertEquals(1, CONTEXT.doc().length);
  }

  /**
   * Adds a folder. As etc/xml contains a zip file, it is added as well.
   * @throws BaseXException exception
   */
  @Test
  public void testAddFolder() throws BaseXException {
    new Add(FLDR).execute(CONTEXT);
    assertEquals(FCNT, CONTEXT.doc().length);
  }

  /**
   * Adds/deletes with target.
   * @throws BaseXException exception
   */
  @Test
  public void deletePath() throws BaseXException {
    new Add(FLDR, null, "foo/pub").execute(CONTEXT);
    new Add(FILE, null, "/foo///bar////").execute(CONTEXT);
    new Add(FLDR, null, "foobar").execute(CONTEXT);
    new Delete("foo").execute(CONTEXT);
    assertEquals(FCNT, CONTEXT.doc().length);
  }

  /**
   * Adds/deletes a file/folder.
   * @throws BaseXException exception
   */
  @Test
  public void addFoldersDeleteFiles() throws BaseXException {
    new Add(FLDR, null, "folder").execute(CONTEXT);
    new Add(FILE).execute(CONTEXT);
    new Delete("input.xml").execute(CONTEXT);
    new Delete("folder/input.xml").execute(CONTEXT);
    assertEquals(FCNT - 1, CONTEXT.doc().length);
  }

  /**
   * Adds a non-existent file.
   * @throws BaseXException expected.
   */
  @Test(expected = BaseXException.class)
  public void testAddFileFail() throws BaseXException {
    new Add(FILE + "/doesnotexist").execute(CONTEXT);
  }

  /**
   * Adds a broken input file to the database and checks if the file can be
   * deleted afterwards.
   * @throws Exception exception
   */
  @Test
  public void testBrokenAdd() throws Exception {
    final IO io = IO.get(Prop.TMP + DBNAME);
    io.write(Token.token("<x"));
    try {
      new Add(io.path()).execute(CONTEXT);
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
  public void testBrokenCreate() throws Exception {
    final IO io = IO.get(Prop.TMP + DBNAME);
    io.write(Token.token("<x"));
    try {
      new CreateDB(DBNAME, io.path()).execute(CONTEXT);
      fail("Broken file was added to the database.");
    } catch(final Exception ex) { }
    assertTrue(io.delete());
  }
}
