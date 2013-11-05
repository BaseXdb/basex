package org.basex.api.xmldb;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.cmd.*;

/**
 * Base class for all XMLDB tests.
 * @author dimitar
 *
 */
public abstract class XMLDBBaseTest {
  /** Test document. */
  static final String DOCPATH = "src/test/resources/";
  /** XMLDB driver. */
  static final String DRIVER = BXDatabase.class.getName();
  /** Database/document path. */
  static final String URL =
      "xmldb:" + NAMELC + "://" + LOCALHOST + ':' + GlobalOptions.PORT.value + '/';
  /** Name of the collection. */
  static final String COLL = "XMLDB";
  /** Database/document path. */
  static final String PATH = URL + COLL;
  /** Optional login. */
  static final String LOGIN = ADMIN;
  /** Optional password. */
  static final String PW = ADMIN;
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

  /**
   * Convenience method for returning the contents of the specified file.
   * @param fn file name
   * @return contents as byte array
   * @throws IOException I/O exception
   */
  static byte[] read(final String fn) throws IOException {
    final File file = new File(fn);
    final byte[] buffer = new byte[(int) file.length()];
    final FileInputStream fis = new FileInputStream(file);
    final DataInputStream dis = new DataInputStream(fis);
    dis.readFully(buffer);
    dis.close();
    return buffer;
  }
}
