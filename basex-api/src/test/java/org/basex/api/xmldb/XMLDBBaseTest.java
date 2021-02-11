package org.basex.api.xmldb;

import static org.basex.core.Text.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.basex.core.users.*;
import org.basex.util.*;

/**
 * Base class for all XMLDB tests.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Dimitar Popov
 */
public abstract class XMLDBBaseTest extends SandboxTest {
  /** Test document. */
  static final String DOCPATH = "src/test/resources/";
  /** XMLDB driver. */
  static final String DRIVER = BXDatabase.class.getName();
  /** Database/document path. */
  static final String URL = "xmldb:" + Prop.PROJECT + "://" + S_LOCALHOST + ':' + DB_PORT + '/';
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

  /**
   * Create XMLDB database.
   */
  static void createDB() {
    execute(new CreateDB(COLL, DOCPATH + DOC1));
    execute(new Close());
  }

  /**
   * Drop XMLDB database.
   */
  static void dropDB() {
    execute(new DropDB(COLL));
  }
}
