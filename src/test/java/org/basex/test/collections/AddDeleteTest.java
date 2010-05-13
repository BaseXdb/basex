package org.basex.test.collections;

import static org.junit.Assert.*;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.proc.Add;
import org.basex.core.proc.CreateColl;
import org.basex.core.proc.Delete;
import org.basex.core.proc.DropDB;
import org.basex.io.IO;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests adding files/folders/zipfiles/urls to collections.
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Michael Seiferle
 * 
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
  static final int FCNT;
  static {
    int fc = 0;
    for(IO c : IO.get(FLDR).children()) {
      if(c.name().endsWith(".xml")) fc++;
    }
    FCNT = fc + 4; // +4 for the zipfile
  }

  /**
   * Creates initial database.
   * @throws Exception e
   */
  @Before
  public void setUp() throws Exception {
    new CreateColl(NAME).execute(CTX);
  }

  /**
   * Drops the initial collection.
   * @throws Exception e
   */
  @After
  public void tearDown() throws Exception {
    new DropDB(NAME).execute(CTX);
  }

  /**
   * Adds an <?xml…>-String to the database. *TODO*
   */
  // @Test
  public final void testAddXMLString() {
    fail("Not yet implemented");
  }

  /**
   * Adds a single file to the databse.
   * @throws Exception e
   */
  @Test
  public final void testAddFile() throws Exception {
    new Add(FILE).execute(CTX);
    assertEquals(1, CTX.doc().length);
  }

  /**
   * Add Zipfile.
   * @throws Exception ex
   */
  @Test
  public void testAddZip() throws Exception {
    new Add(ZIPFILE, "target").execute(CTX);
    assertEquals(4, CTX.doc().length);
  }

  /**
   * Add / Delete URL.
   * Disabled to allow "offline" execution.
   * @throws Exception ex.
   */
  // @Test 
  public void testAddUrl() throws Exception {
    new Add(URL).execute(CTX);
    new Add(URL, "bar").execute(CTX);
    new Delete("xmark.xml").execute(CTX);
    assertEquals(1, CTX.doc().length);
  }

  /**
   * Add / Delete GZIP.
   * @throws Exception ex.
   */
  @Test
  public void testAddGzip() throws Exception {
    new Add(GZIPFILE).execute(CTX);
    new Add(GZIPFILE, "bar").execute(CTX);
    new Delete("bar").execute(CTX);
    assertEquals(1, CTX.doc().length);
  }

  /**
   * Add Folder. As etc/xml contains a zipfile it is added as well.
   * @throws Exception ex
   */
  @Test
  public void testAddFolder() throws Exception {
    new Add(FLDR).execute(CTX);
    assertEquals(FCNT, CTX.doc().length);
  }

  /**
   * Adding / Deletion with target.
   * @throws Exception ex
   */
  @Test
  public void deletePath() throws Exception {
    new Add(FLDR, "foo/pub").execute(CTX);
    new Add(FILE, "foo/bar").execute(CTX);
    new Add(FLDR, "foobar").execute(CTX);
    new Delete("foo").execute(CTX);
    assertEquals(FCNT, CTX.doc().length);

  }

  /**
   * Add / Delete file/folder.
   * @throws Exception ex
   */
  @Test
  public void addFoldersDeleteFiles() throws Exception {
    new Add(FLDR, "folder").execute(CTX);
    new Add(FILE).execute(CTX);
    new Delete("input.xml").execute(CTX);
    new Delete("folder/input.xml").execute(CTX);
    assertEquals(FCNT - 1, CTX.doc().length);
  }

  /**
   * Add non existent file.
   * @throws Exception expected.
   */
  @Test(expected = BaseXException.class)
  public final void testAddFileFail() throws Exception {
    new Add(FILE + "/doesnotexist").execute(CTX);
  }

}
