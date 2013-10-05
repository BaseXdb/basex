package org.basex.test.api.xmldb;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.api.xmldb.*;
import org.basex.core.*;
import org.basex.core.cmd.*;

/**
 * Base class for all XMLDB tests.
 * @author dimitar
 *
 */
public abstract class XMLDBBaseTest {
  /** Test document. */
  protected static final String DOCPATH = "src/test/resources/";
  /** XMLDB driver. */
  protected static final String DRIVER = BXDatabase.class.getName();
  /** Database/document path. */
  protected static final String URL =
      "xmldb:" + NAMELC + "://" + LOCALHOST + ':' + GlobalOptions.PORT[1] + '/';
  /** Name of the collection. */
  protected static final String COLL = "XMLDB";
  /** Database/document path. */
  protected static final String PATH = URL + COLL;
  /** Optional login. */
  protected static final String LOGIN = ADMIN;
  /** Optional password. */
  protected static final String PW = ADMIN;
  /** Test document. */
  protected static final String DOC1 = "first.xml";
  /** Test document. */
  protected static final String DOC2 = "second.xml";
  /** Test document. */
  protected static final String DOC3 = "third.xml";

  /** Context. */
  private static Context context = new Context();

  /**
   * Create XMLDB database.
   * @throws BaseXException exception during database create
   */
  protected static void createDB() throws BaseXException {
    new CreateDB(COLL, DOCPATH + DOC1).execute(context);
    new Close().execute(context);
  }

  /**
   * Drop XMLDB database.
   * @throws BaseXException exception during database drop
   */
  protected static void dropDB() throws BaseXException {
    new DropDB(COLL).execute(context);
  }

  /**
   * Convenience method for returning the contents of the specified file.
   * @param fn file name
   * @return contents as byte array
   * @throws IOException I/O exception
   */
  protected static byte[] read(final String fn) throws IOException {
    final File file = new File(fn);
    final byte[] buffer = new byte[(int) file.length()];
    final FileInputStream fis = new FileInputStream(file);
    final DataInputStream dis = new DataInputStream(fis);
    dis.readFully(buffer);
    dis.close();
    return buffer;
  }
}
