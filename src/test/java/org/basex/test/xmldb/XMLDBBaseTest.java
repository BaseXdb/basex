package org.basex.test.xmldb;

import static org.basex.core.Text.*;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.basex.api.xmldb.BXDatabase;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.MainProp;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;

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
      "xmldb:" + NAMELC + "://" + LOCALHOST + ":" + MainProp.PORT[1] + '/';
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
  private static final Context CTX = new Context();

  /**
   * Create XMLDB database.
   * @throws BaseXException exception during database create
   */
  protected static void createDB() throws BaseXException {
    new CreateDB(COLL, DOCPATH + DOC1).execute(CTX);
  }

  /**
   * Drop XMLDB database.
   * @throws BaseXException exception during database drop
   */
  protected static void dropDB() throws BaseXException {
    new DropDB(COLL).execute(CTX);
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
