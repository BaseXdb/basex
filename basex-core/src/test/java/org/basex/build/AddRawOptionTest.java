package org.basex.build;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.query.func.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link MainOptions#ADDRAW} option.
 * @author BaseX Team 2005-21, BSD License
 * @author Dimitar Popov
 */
public final class AddRawOptionTest extends SandboxTest {
  /** Test directory. */
  private static final String DIR = "src/test/resources/dir";
  /** Test files from {@link AddRawOptionTest#DIR}}. */
  private static final StringList FILES = new IOFile(DIR).descendants();

  /**
   * Class set up method.
   */
  @BeforeAll public static void classSetUp() {
    set(MainOptions.ADDRAW, true);
  }

  /**
   * Set up method.
   */
  @BeforeEach public void setUp() {
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
    assertFalse(files.isEmpty(), "No files were imported");
    for(final String name : FILES) {
      assertTrue(files.contains(name), "File " + name + " is not imported");
    }
    assertEquals(FILES.size(), files.size(), "Expected number of imported files is different");
  }
}
