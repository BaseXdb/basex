package org.basex.test.build;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.Add;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Set;
import org.basex.query.func.Function;
import org.basex.server.LocalSession;
import org.basex.server.Query;
import org.basex.server.Session;
import org.basex.util.Util;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for the {@link Prop#ADDRAW} option.
 * @author BaseX Team 2005-12, BSD License
 * @author Dimitar Popov
 */
public class AddRawOptionTest {
  /** Test database name. */
  private static final String DBNAME = Util.name(AddRawOptionTest.class);
  /** Test directory. */
  private static final String DIR = "src/test/resources/dir";
  /** Test files from {@link AddRawOptionTest#DIR}}. */
  private static final File[] FILES = new File(DIR).listFiles();
  /** Database context. */
  private static final Context CTX = new Context();

  /**
   * Class set up method.
   * @throws BaseXException error
   */
  @BeforeClass
  public static void classSetUp() throws BaseXException {
    new Set(Prop.ADDRAW, true).execute(CTX);
  }

  /**
   * Set up method.
   * @throws BaseXException error
   */
  @Before
  public void setUp() throws BaseXException {
    new CreateDB(DBNAME).execute(CTX);
  }

  /**
   * Clean up method.
   * @throws BaseXException error
   */
  @After
  public void cleanUp() throws BaseXException {
    new DropDB(DBNAME).execute(CTX);
    CTX.close();
  }

  /**
   * Test if raw files are added on executing a {@code CREATE} command.
   * @throws Exception error
   */
  @Test
  public void testCreate() throws Exception {
    new CreateDB(DBNAME, DIR).execute(CTX);
    assertAllFilesExist();
  }

  /**
   * Test if raw files are added on executing an {@code ADD} command.
   * @throws Exception error
   */
  @Test
  public void testAdd() throws Exception {
    new Add("", DIR).execute(CTX);
    assertAllFilesExist();
  }

  /**
   * Check if all files and only they exist in the database.
   * @throws IOException I/O exception
   */
  private void assertAllFilesExist() throws IOException {
    final HashSet<String> files = new HashSet<String>();
    final Session session = new LocalSession(CTX);
    try {
      final Query q = session.query(Function._DB_LIST.args(DBNAME));
      while(q.more()) files.add(q.next());
      q.close();
    } finally {
      session.close();
    }

    assertTrue("No files were imported", files.size() > 0);
    for(final File f : FILES) {
      final String fname = f.getName();
      assertTrue("File " + fname + " is not imported", files.contains(fname));
    }
    assertEquals("Expected number of imported files is different",
        FILES.length, files.size());
  }
}
