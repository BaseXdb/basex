package org.basex.build;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Tests adding files/folders/zip files/urls to collections.
 *
 * @author BaseX Team 2005-21, BSD License
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
  private static final String TEMP = Prop.TEMPDIR + NAME + IO.XMLSUFFIX;

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
   */
  @BeforeEach public void setUp() {
    execute(new CreateDB(NAME));
  }

  /**
   * Drops the database.
   */
  @AfterEach public void tearDown() {
    execute(new DropDB(NAME));
  }

  /**
   * <p>Adds an XML fragment to the database.</p><ol>
   * <li> with name and w/o target</li>
   * <li> with name and target set</li>
   * <li> w/o name and target set</li></ol>
   */
  @Test public void addXMLString() {
    execute(new Add("index.xml", XMLFRAG));
    assertEquals(1, docs());
    execute(new Add("a/b/c/index2.xml", XMLFRAG));
    assertEquals(2, docs());
    execute(new Add("a/d/c", XMLFRAG));
    assertEquals(3, docs());
  }

  /**
   * Adds a single file to the database.
   */
  @Test public void addFile() {
    execute(new Add(null, FILE));
    assertEquals(1, docs());
  }

  /**
   * Adds a zip file.
   */
  @Test public void addZip() {
    execute(new Add("target", ZIPFILE));
    assertEquals(4, docs());

    // do not add archives
    try {
      set(MainOptions.ADDARCHIVES, false);
      execute(new Add("", ZIPFILE));
      assertEquals(4, docs());
    } finally {
      set(MainOptions.ADDARCHIVES, true);
    }

    // prefix database path with name of archive
    try {
      set(MainOptions.ARCHIVENAME, true);
      execute(new Add("", ZIPFILE));
      assertEquals(4, context.data().resources.docs("xml.zip/").size());
      execute(new Add("", GZIPFILE));
      assertEquals(1, context.data().resources.docs("xml.gz/").size());
    } finally {
      set(MainOptions.ARCHIVENAME, false);
    }
  }

  /**
   * Adds/deletes a GZIP file.
   */
  @Test public void addGzip() {
    execute(new Add("", GZIPFILE));
    execute(new Add("bar", GZIPFILE));
    execute(new Delete("bar"));
    assertEquals(1, docs());
  }

  /**
   * Adds a folder. The contained a zip file is added as well.
   */
  @Test public void addFolder() {
    execute(new Add("", FLDR));
    assertEquals(NFLDR, docs());
  }

  /**
   * Adds/deletes with target.
   */
  @Test public void deletePath() {
    execute(new Add("foo/pub", FLDR));
    assertEquals(NFLDR, docs());
    execute(new Delete("foo"));
    assertEquals(0, docs());
    execute(new Add("/foo///bar////", FILE));
    execute(new Add("foobar", FLDR));
    execute(new Delete("foo"));
    assertEquals(NFLDR, docs());
  }

  /**
   * Adds/deletes a file/folder.
   */
  @Test public void addFoldersDeleteFiles() {
    execute(new Add("folder", FLDR));
    execute(new Add("", FILE));
    execute(new Delete("input.xml"));
    execute(new Delete("folder/input.xml"));
    assertEquals(NFLDR - 1, docs());
  }

  /**
   * Adds/deletes with target.
   */
  @Test public void createDeleteAdd() {
    execute(new CreateDB(NAME, "<a/>"));
    execute(new Delete("/"));
    assertEquals(0, docs());
    final StringBuilder sb = new StringBuilder();
    for(int i = 0; i < 256; i++) sb.append("<a").append(i).append("/>");
    execute(new Add("x", "<x>" + sb + "</x>"));
    assertEquals(1, docs());
    assertEquals("1", query("count(//x)"));
    assertEquals("0", query("count(//a)"));
  }

  /**
   * Adds a non-existent file.
   */
  @Test public void addFileFail() {
    assertThrows(BaseXException.class,
      () -> new Add("", FILE + "/doesnotexist").execute(context));
  }

  /**
   * Adds a broken input file to the database and checks if the file can be
   * deleted afterwards.
   */
  @Test public void addCorrupt() {
    final IOFile io = new IOFile(TEMP);
    write(io, "<x");
    try {
      new Add("", io.path()).execute(context);
      fail("Broken file was added to the database.");
    } catch(final BaseXException ignored) { }

    assertTrue(io.delete());
  }

  /**
   * Creates a database from a broken input.
   */
  @Test public void createCorrupt() {
    assertThrows(BaseXException.class, () -> new CreateDB(NAME, "<x").execute(context));
  }

  /**
   * Creates a database from a broken input file.
   */
  @Test public void createCorruptFromFile() {
    final IOFile io = new IOFile(TEMP);
    write(io, "<x");
    assertThrows(BaseXException.class, () -> new CreateDB(NAME, io.path()).execute(context));
  }

  /**
   * Skips a corrupt file.
   */
  @Test public void skipCorrupt() {
    final IOFile io = new IOFile(TEMP);
    write(io, "<x");

    try {
      set(MainOptions.SKIPCORRUPT, true);
      assertEquals(0, docs());
      execute(new Add("x", "<x"));
      execute(new Add("x", CORRUPT));
      assertEquals(0, docs());
    } finally {
      set(MainOptions.SKIPCORRUPT, false);
    }

    try {
      new Add("", "<x").execute(context);
      fail("Broken file was added to the database.");
    } catch(final BaseXException ignored) { }
  }

  /**
   * Returns the number of documents in the current database.
   * @return number of documents
   */
  private static int docs() {
    return context.data().resources.docs("").size();
  }
}
