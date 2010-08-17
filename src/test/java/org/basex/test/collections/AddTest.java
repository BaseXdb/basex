package org.basex.test.collections;

import static org.junit.Assert.*;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.Add;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.io.IO;
import org.basex.util.Token;
import org.junit.After;
import org.junit.Test;

/**
 * Tests for creating databases and adding documents.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public class AddTest {
  /** Database context. */
  private static final Context CTX = new Context();

  /** Test database name. */
  private static final String DBNAME = AddTest.class.getSimpleName();
  /** Test document name. */
  private static final String DOCNAME = "t.xml";
  /** Target. */
  private static final String TARGET = "target/";

  /** Path to test file. */
  private static final String PATH = "etc/xml/input.xml";
  /** Test folder. */
  private static final String FOLDER = "etc/xml/dir";
  /** Test XML Fragment. */
  private static final String FRAG = "<xml/>";

  /** First document in test folder. */
  private static final String FOLDERFILE;

  // finds the first document in the xml folder
  static {
    String file = "";
    for(final IO c : IO.get(FOLDER).children()) {
      if(c.name().endsWith(IO.XMLSUFFIX)) {
        file = c.name();
        break;
      }
    }
    FOLDERFILE = file;
  }

  /** Test inputs (path, folder, fragment). */
  private static final String[] INPUTS = { PATH, FRAG, FOLDER };
  /** Names of test inputs (path, folder, fragment). */
  private static final String[] NAMES = {
    PATH.replaceAll(".*/", ""), DBNAME + IO.XMLSUFFIX, FOLDERFILE
  };

  /**
   * Drops the initial collection.
   * @throws BaseXException exception
   */
  @After
  public void tearDown() throws BaseXException {
    new DropDB(DBNAME).execute(CTX);
  }

  /**
   * CREATE DB {DBNAME}.
   * @throws BaseXException exception
   */
  @Test
  public final void createDB() throws BaseXException {
    new CreateDB(DBNAME).execute(CTX);
    // check if database name equals argument of create command
    assertEquals(dbName(), DBNAME);
  }

  /**
   * CREATE DB {DBNAME} {INPUT[]}.
   * @throws BaseXException exception
   */
  @Test
  public final void createDBWithInput() throws BaseXException {
    for(int i = 0; i < INPUTS.length; ++i) {
      new CreateDB(DBNAME, INPUTS[i]).execute(CTX);
      // check name of database
      assertEquals(DBNAME, dbName());
      // check name of document
      assertEquals(NAMES[i], docName());
    }
  }

  /**
   * CREATE DB {DBNAME}; ADD {INPUT[]}.
   * @throws BaseXException exception
   */
  @Test
  public final void createDBandAdd() throws BaseXException {
    for(int i = 0; i < INPUTS.length; ++i) {
      new CreateDB(DBNAME).execute(CTX);
      new Add(INPUTS[i]).execute(CTX);
      // check name of document
      assertEquals(NAMES[i], docName());
    }
  }

  /**
   * CREATE DB {DBNAME}; ADD AS {DOCNAME} {INPUT[]}.
   * @throws BaseXException exception
   */
  @Test
  public final void createDBandAddAs() throws BaseXException {
    for(final String in : INPUTS) {
      new CreateDB(DBNAME).execute(CTX);
      new Add(in, DOCNAME).execute(CTX);
      // check name of document (first file in folder or specified name)
      assertEquals(in == FOLDER ? FOLDERFILE : DOCNAME, docName());
    }
  }

  /**
   * CREATE DB {DBNAME}; ADD TO {TARGET} {INPUT[]}.
   * @throws BaseXException exception
   */
  @Test
  public final void createDBandAddTo() throws BaseXException {
    for(int i = 0; i < INPUTS.length; ++i) {
      new CreateDB(DBNAME).execute(CTX);
      new Add(INPUTS[i], null, TARGET).execute(CTX);
      // check name of document
      assertEquals(TARGET + NAMES[i], docName());
    }
  }

  /**
   * CREATE DB {DBNAME}; ADD AS {DOCNAME} TO {TARGET} {INPUT[]}.
   * @throws BaseXException exception
   */
  @Test
  public final void createDBandAddAsTO() throws BaseXException {
    for(final String in : INPUTS) {
      new CreateDB(DBNAME).execute(CTX);
      new Add(in, DOCNAME, TARGET).execute(CTX);
      // check name of document (first file in folder or specified name)
      assertEquals(TARGET + (in == FOLDER ? FOLDERFILE : DOCNAME), docName());
    }
  }

  /**
   * Returns the name of the database.
   * @return database name
   */
  private String dbName() {
    return CTX.data.meta.name;
  }

  /**
   * Returns the name of the first document in the database.
   * @return first document name
   */
  private String docName() {
    return Token.string(CTX.data.text(CTX.current.nodes[0], true));
  }
}
