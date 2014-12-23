package org.basex.build;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import org.basex.*;
import org.basex.api.client.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.cmd.Set;
import org.basex.io.*;
import org.basex.query.func.*;
import org.junit.*;
import org.junit.Test;

/**
 * Tests for the {@link MainOptions#ADDRAW} option.
 * @author BaseX Team 2005-14, BSD License
 * @author Dimitar Popov
 */
public final class AddRawOptionTest extends SandboxTest {
  /** Test directory. */
  private static final String DIR = "src/test/resources/dir";
  /** Test files from {@link AddRawOptionTest#DIR}}. */
  private static final IOFile[] FILES = new IOFile(DIR).children();

  /**
   * Class set up method.
   * @throws BaseXException error
   */
  @BeforeClass
  public static void classSetUp() throws BaseXException {
    new Set(MainOptions.ADDRAW, true).execute(context);
  }

  /**
   * Set up method.
   * @throws BaseXException error
   */
  @Before
  public void setUp() throws BaseXException {
    new CreateDB(NAME).execute(context);
  }

  /**
   * Test if raw files are added on executing a {@code CREATE} command.
   * @throws Exception error
   */
  @Test
  public void testCreate() throws Exception {
    new CreateDB(NAME, DIR).execute(context);
    assertAllFilesExist();
  }

  /**
   * Test if raw files are added on executing an {@code ADD} command.
   * @throws Exception error
   */
  @Test
  public void testAdd() throws Exception {
    new Add("", DIR).execute(context);
    assertAllFilesExist();
  }

  /**
   * Check if all files and only they exist in the database.
   * @throws IOException I/O exception
   */
  private static void assertAllFilesExist() throws IOException {
    final HashSet<String> files = new HashSet<>();
    try(final Session session = new LocalSession(context)) {
      try(final Query q = session.query(Function._DB_LIST.args(NAME))) {
        while(q.more()) files.add(q.next());
      }
    }

    assertFalse("No files were imported", files.isEmpty());
    for(final IOFile f : FILES) {
      final String fname = f.name();
      assertTrue("File " + fname + " is not imported", files.contains(fname));
    }
    assertEquals("Expected number of imported files is different", FILES.length, files.size());
  }
}
