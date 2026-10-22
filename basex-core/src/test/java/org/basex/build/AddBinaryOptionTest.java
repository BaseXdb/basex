package org.basex.build;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.zip.*;

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
 * @author BaseX Team, BSD License
 * @author Dimitar Popov
 */
public final class AddBinaryOptionTest extends SandboxTest {
  /** Test directory. */
  private static final String DIR = "src/test/resources/dir";
  /** Test files from {@link AddBinaryOptionTest#DIR}}. */
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
   * Test if binary files are added on executing a {@code CREATE} command.
   */
  @Test public void testCreate() {
    execute(new CreateDB(NAME, DIR));
    assertAllFilesExist();
  }

  /**
   * Test if binary files are added on executing an {@code ADD} command.
   */
  @Test public void testAdd() {
    execute(new Add("", DIR));
    assertAllFilesExist();
  }

  /**
   * Test if archive entries with relative paths are rejected.
   * @throws IOException I/O exception
   */
  @Test public void archiveSlip() throws IOException {
    final IOFile zip = new IOFile(sandbox(), "slip.zip");
    try(ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip.file()))) {
      for(final String entry : new String[] { "../../slip.txt", "a\\..\\..\\slip.txt", "ok.txt" }) {
        out.putNextEntry(new ZipEntry(entry));
        out.write(Token.token("X"));
        out.closeEntry();
      }
    }
    execute(new CreateDB(NAME, zip.path()));
    assertEquals("ok.txt", query(Function._DB_LIST.args(NAME)));
    assertFalse(context.soptions.dbPath("slip.txt").exists());
    assertFalse(new IOFile(context.soptions.dbPath(NAME), "slip.txt").exists());
  }

  /**
   * Checks if the expected files exist in the database.
   */
  private static void assertAllFilesExist() {
    final StringList files = new StringList(query(Function._DB_LIST.args(NAME)).split(Prop.NL));
    assertFalse(files.isEmpty(), "No files were imported");
    final StringBuilder sb = new StringBuilder();
    for(final String name : FILES) {
      if(!files.contains(name)) sb.append("\n- ").append(name);
    }
    if(!sb.isEmpty()) fail(FILES.size() + " files expected, missing: " + sb);
    assertEquals(FILES.size(), files.size(), "Expected number of imported files is different");
  }
}
