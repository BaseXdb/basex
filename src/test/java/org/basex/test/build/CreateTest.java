package org.basex.test.build;

import static org.junit.Assert.*;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.Add;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.util.Token;
import org.basex.util.Util;
import org.junit.After;
import org.junit.Test;

/**
 * Tests for creating databases and adding documents.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class CreateTest {
  /** Database context. */
  private static final Context CONTEXT = new Context();
  /** Test database name. */
  private static final String DB = Util.name(CreateTest.class);
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
    PATH.replaceAll(".*/", ""), FOLDERFILE, DB + IO.XMLSUFFIX
  };

  /**
   * Drops the initial collection.
   * @throws BaseXException exception
   */
  @After
  public void tearDown() throws BaseXException {
    new DropDB(DB).execute(CONTEXT);
  }

  /**
   * CREATE DB {DB}.
   * @throws BaseXException exception
   */
  @Test
  public void createDB() throws BaseXException {
    new CreateDB(DB).execute(CONTEXT);
    // check if database name equals argument of create command
    assertEquals(db(), DB);
  }

  /**
   * CREATE DB {DB} {INPUT[]}.
   * @throws BaseXException exception
   */
  @Test
  public void createDBWithInput() throws BaseXException {
    for(int i = 0; i < INPUTS.length; ++i) {
      new CreateDB(DB, INPUTS[i]).execute(CONTEXT);
      // check name of database
      assertEquals(DB, db());
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
      new CreateDB(DB).execute(CONTEXT);
      new Add("", INPUTS[i]).execute(CONTEXT);
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
      new CreateDB(DB).execute(CONTEXT);
      new Add(DOCNAME, INPUTS[i]).execute(CONTEXT);
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
      new CreateDB(DB).execute(CONTEXT);
      new Add(TARGET, INPUTS[i]).execute(CONTEXT);
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
      new CreateDB(DB).execute(CONTEXT);
      new Add(TARGET + DOCNAME, INPUTS[i]).execute(CONTEXT);
      assertEquals(TARGET + DOCNAME, docName());
    }
  }

  /**
   * Returns the name of the database.
   * @return database name
   */
  private static String db() {
    return CONTEXT.data().meta.name;
  }

  /**
   * Returns the name of the first document in the database.
   * @return first document name
   */
  private static String docName() {
    return Token.string(CONTEXT.data().text(CONTEXT.current().list[0], true));
  }
}
