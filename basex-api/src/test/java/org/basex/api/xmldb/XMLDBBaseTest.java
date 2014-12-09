package org.basex.api.xmldb;

import static org.basex.core.Text.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.users.*;
import org.basex.util.*;

/**
 * Base class for all XMLDB tests.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Dimitar Popov
 */
public abstract class XMLDBBaseTest extends SandboxTest {
  /** Test document. */
  static final String DOCPATH = "src/test/resources/";
  /** XMLDB driver. */
  static final String DRIVER = BXDatabase.class.getName();
  /** Database/document path. */
  static final String URL =
      "xmldb:" + Prop.PROJECT_NAME + "://" + S_LOCALHOST + ':' + StaticOptions.PORT.value + '/';
  /** Name of the collection. */
  static final String COLL = "XMLDB";
  /** Database/document path. */
  static final String PATH = URL + COLL;
  /** Optional login. */
  static final String LOGIN = UserText.ADMIN;
  /** Optional password. */
  static final String PW = UserText.ADMIN;
  /** Test document. */
  static final String DOC1 = "first.xml";
  /** Test document. */
  static final String DOC2 = "second.xml";
  /** Test document. */
  static final String DOC3 = "third.xml";

  /** Context. */
  private static final Context CONTEXT = new Context();

  /**
   * Create XMLDB database.
   * @throws BaseXException exception during database create
   */
  static void createDB() throws BaseXException {
    new CreateDB(COLL, DOCPATH + DOC1).execute(CONTEXT);
    new Close().execute(CONTEXT);
  }

  /**
   * Drop XMLDB database.
   * @throws BaseXException exception during database drop
   */
  static void dropDB() throws BaseXException {
    new DropDB(COLL).execute(CONTEXT);
  }
}
