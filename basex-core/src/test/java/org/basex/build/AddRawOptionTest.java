package org.basex.build;

import static org.junit.Assert.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.query.func.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.junit.*;
import org.junit.Test;

/**
 * Tests for the {@link MainOptions#ADDRAW} option.
 * @author BaseX Team 2005-18, BSD License
 * @author Dimitar Popov
 */
public final class AddRawOptionTest extends SandboxTest {
  /** Test directory. */
  private static final String DIR = "src/test/resources/dir";
  /** Test files from {@link AddRawOptionTest#DIR}}. */
  private static final IOFile[] FILES = new IOFile(DIR).children();

  /**
   * Class set up method.
   */
  @BeforeClass public static void classSetUp() {
    set(MainOptions.ADDRAW, true);
  }

  /**
   * Set up method.
   */
  @Before public void setUp() {
    execute(new CreateDB(NAME));
  }

  /**
   * Test if raw files are added on executing a {@code CREATE} command.
   */
  @Test public void testCreate() {
    execute(new CreateDB(NAME, DIR));
    assertAllFilesExist();
  }

  /**
   * Test if raw files are added on executing an {@code ADD} command.
   */
  @Test public void testAdd() {
    execute(new Add("", DIR));
    assertAllFilesExist();
  }

  /**
   * Check if all files and only they exist in the database.
   */
  private static void assertAllFilesExist() {
    final StringList files = new StringList(query(Function._DB_LIST.args(NAME)).split(Prop.NL));
    assertFalse("No files were imported", files.isEmpty());
    for(final IOFile f : FILES) {
      final String fname = f.name();
      assertTrue("File " + fname + " is not imported", files.contains(fname));
    }
    assertEquals("Expected number of imported files is different", FILES.length, files.size());
  }
}
