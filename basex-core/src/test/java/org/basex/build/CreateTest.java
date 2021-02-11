package org.basex.build;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Tests for creating databases and adding documents.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class CreateTest extends SandboxTest {
  /** Test document name. */
  private static final String DOCNAME = "t.xml";
  /** Target. */
  private static final String TARGET = "target/";
  /** Path to test file. */
  private static final String PATH = "src/test/resources/input.xml";
  /** Test folder. */
  private static final String FOLDER = "src/test/resources/dir";
  /** Test XML fragment. */
  private static final String FRAG = "<xml/>";

  /** First document in test folder. */
  private static final String FOLDERFILE;

  // finds the first document in the xml folder
  static {
    String file = "";
    for(final IOFile c : new IOFile(FOLDER).children()) {
      file = c.name();
      if(file.endsWith(IO.XMLSUFFIX)) break;
    }
    FOLDERFILE = file;
  }

  /** Test inputs (path, folder, fragment). */
  private static final String[] INPUTS = { PATH, FOLDER, FRAG };
  /** Names of test inputs (path, folder, fragment). */
  private static final String[] NAMES = {
    PATH.replaceAll(".*/", ""), FOLDERFILE, NAME + IO.XMLSUFFIX
  };

  /**
   * Drops the initial collection.
   */
  @AfterEach public void tearDown() {
    execute(new DropDB(NAME));
  }

  /**
   * CREATE DB {DB}.
   */
  @Test public void createDB() {
    execute(new CreateDB(NAME));
    // check if database name equals argument of create command
    assertEquals(db(), NAME);
  }

  /**
   * CREATE DB {DB} {INPUT[]}.
   */
  @Test public void createDBWithInput() {
    final int il = INPUTS.length;
    for(int i = 0; i < il; i++) {
      execute(new CreateDB(NAME, INPUTS[i]));
      // check name of database
      assertEquals(NAME, db());
      // check name of document
      assertEquals(NAMES[i], docName());
    }
  }

  /**
   * CREATE DB {DB}; ADD {INPUT[]}.
   */
  @Test public void createDBandAdd() {
    // add file and folder, skip fragment
    final int il = INPUTS.length;
    for(int i = 0; i < il - 1; i++) {
      execute(new CreateDB(NAME));
      execute(new Add("", INPUTS[i]));
      assertEquals(NAMES[i], docName());
    }
  }

  /**
   * CREATE DB {DB}; ADD TO {DOCNAME} {INPUT[]}.
   */
  @Test public void createDBandAddToName() {
    // add file and fragment, skip folder
    final int il = INPUTS.length;
    for(int i = 0; i < il; i += 2) {
      execute(new CreateDB(NAME));
      execute(new Add(DOCNAME, INPUTS[i]));
      assertEquals(DOCNAME, docName());
    }
  }

  /**
   * CREATE DB {DB}; ADD TO {TARGET} {INPUT[]}.
   */
  @Test public void createDBandAddToTarget() {
    // add file and folder, skip fragment
    final int il = INPUTS.length;
    for(int i = 0; i < il - 1; i++) {
      execute(new CreateDB(NAME));
      execute(new Add(TARGET, INPUTS[i]));
      assertEquals(TARGET + NAMES[i], docName());
    }
  }

  /**
   * CREATE DB {DB}; ADD TO {TARGET/DOCNAME} {INPUT[]}.
   */
  @Test public void createDBandAddToTargetName() {
    // add file and fragment, skip folder
    final int il = INPUTS.length;
    for(int i = 0; i < il; i += 2) {
      execute(new CreateDB(NAME));
      execute(new Add(TARGET + DOCNAME, INPUTS[i]));
      assertEquals(TARGET + DOCNAME, docName());
    }
  }

  /**
   * Returns the name of the database.
   * @return database name
   */
  private static String db() {
    return context.data().meta.name;
  }

  /**
   * Returns the name of the first document in the database.
   * @return first document name
   */
  private static String docName() {
    return Token.string(context.data().text(context.current().pre(0), true));
  }
}
