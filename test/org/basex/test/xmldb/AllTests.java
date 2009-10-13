package org.basex.test.xmldb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.basex.api.xmldb.BXCollection;
import org.basex.core.Context;
import org.basex.core.proc.DropDB;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Resource;
import org.xmldb.api.modules.XMLResource;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * This class registers and runs all available XMLDB/API tests.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class AllTests {
  /** XMLDB driver. */
  static final String DRIVER = "org.basex.api.xmldb.BXDatabase";
  /** Database/document path. */
  static final String URL = "xmldb:basex://localhost:1984/";
  /** Name of the collection. */
  static final String COLL = "XMLDB";
  /** Database/document path. */
  static final String PATH = URL + COLL;
  /** Optional login. */
  static final String LOGIN = null;
  /** Optional password. */
  static final String PW = null;

  /** Test document. */
  static final String DOC1 = "first.xml";
  /** Test document. */
  static final String DOC2 = "second.xml";
  /** Test document. */
  static final String DOC3 = "third.xml";

  /** Collection. */
  static Collection coll;

  /** Private constructor. */
  private AllTests() { }

  /**
   * Main method.
   * @param args (ignored) command-line arguments
   * @throws Exception exceptions
   */
  public static void main(final String[] args) throws Exception {
    TestRunner.run(suite());
  }

  /**
   * Registers all JUnit tests.
   * @return test suite
   * @throws Exception exception
   */
  public static Test suite() throws Exception {
    init();
    final TestSuite tests = new TestSuite("XMLDB/API Tests");
    tests.addTestSuite(CollectionTest.class);
    tests.addTestSuite(CollectionManagementServiceTest.class);
    tests.addTestSuite(DatabaseTest.class);
    tests.addTestSuite(ResourceIteratorTest.class);
    tests.addTestSuite(ResourceSetTest.class);
    tests.addTestSuite(XMLResourceTest.class);
    tests.addTestSuite(XPathQueryServiceTest.class);
    return tests;
  }

  /**
   * Initializes some implementation specific data.
   * @throws Exception exception
   */
  static void init() throws Exception {
    // create an initial collection for testing
    final Context ctx = new Context();
    coll = new BXCollection(COLL, false, ctx);
    final Resource res = coll.createResource(DOC1, XMLResource.RESOURCE_TYPE);
    res.setContent(read(DOC1));
    coll.storeResource(res);
    coll.close();

    // guarantee correct shutdown...
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        DropDB.drop(COLL, ctx.prop);
      }
    });
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
    fis.read(buffer);
    fis.close();
    return buffer;
  }
}
