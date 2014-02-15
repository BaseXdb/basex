package org.basex.build;

import static org.junit.Assert.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.*;
import org.basex.util.*;
import org.junit.*;

/**
 * Tests for creating databases and adding documents.
 *
 * @author BaseX Team 2005-14, BSD License
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
   * @throws BaseXException exception
   */
  @After
  public void tearDown() throws BaseXException {
    new DropDB(NAME).execute(context);
  }

  /**
   * CREATE DB {DB}.
   * @throws BaseXException exception
   */
  @Test
  public void createDB() throws BaseXException {
    new CreateDB(NAME).execute(context);
    // check if database name equals argument of create command
    assertEquals(db(), NAME);
  }

  /**
   * CREATE DB {DB} {INPUT[]}.
   * @throws BaseXException exception
   */
  @Test
  public void createDBWithInput() throws BaseXException {
    for(int i = 0; i < INPUTS.length; ++i) {
      new CreateDB(NAME, INPUTS[i]).execute(context);
      // check name of database
      assertEquals(NAME, db());
      // check name of document
      assertEquals(NAMES[i], docName());
    }
  }

  /**
   * CREATE DB {DB}; ADD {INPUT[]}.
   * @throws BaseXException exception
   */
  @Test
  public void createDBandAdd() throws BaseXException {
    // add file and folder, skip fragment
    for(int i = 0; i < INPUTS.length - 1; i++) {
      new CreateDB(NAME).execute(context);
      new Add("", INPUTS[i]).execute(context);
      assertEquals(NAMES[i], docName());
    }
  }

  /**
   * CREATE DB {DB}; ADD TO {DOCNAME} {INPUT[]}.
   * @throws BaseXException exception
   */
  @Test
  public void createDBandAddToName() throws BaseXException {
    // add file and fragment, skip folder
    for(int i = 0; i < INPUTS.length; i += 2) {
      new CreateDB(NAME).execute(context);
      new Add(DOCNAME, INPUTS[i]).execute(context);
      assertEquals(DOCNAME, docName());
    }
  }

  /**
   * CREATE DB {DB}; ADD TO {TARGET} {INPUT[]}.
   * @throws BaseXException exception
   */
  @Test
  public void createDBandAddToTarget() throws BaseXException {
    // add file and folder, skip fragment
    for(int i = 0; i < INPUTS.length - 1; i++) {
      new CreateDB(NAME).execute(context);
      new Add(TARGET, INPUTS[i]).execute(context);
      assertEquals(TARGET + NAMES[i], docName());
    }
  }

  /**
   * CREATE DB {DB}; ADD TO {TARGET/DOCNAME} {INPUT[]}.
   * @throws BaseXException exception
   */
  @Test
  public void createDBandAddToTargetName() throws BaseXException {
    // add file and fragment, skip folder
    for(int i = 0; i < INPUTS.length; i += 2) {
      new CreateDB(NAME).execute(context);
      new Add(TARGET + DOCNAME, INPUTS[i]).execute(context);
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
    return Token.string(context.data().text(context.current().pres[0], true));
  }
}
