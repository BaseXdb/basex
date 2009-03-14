package org.basex.test.xmldb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.basex.api.xmldb.BXCollection;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Resource;
import org.xmldb.api.modules.XMLResource;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This class registers and runs all available XMLDB/API tests.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Andreas Weiler
 */
public class AllTests {
  /** XMLDB driver. */
  static final String DRIVER = "org.basex.api.xmldb.BXDatabase";
  /** Database/document path. */
  static final String URL = "xmldb:basex://localhost:1984/";
  /** Collection. */
  static final String COLL = "collection";
  /** Database/document path. */
  static final String PATH = URL + COLL;
  /** Optional login. */
  static final String LOGIN = null;
  /** Optional password. */
  static final String PW = null;
  
  /** Test Documents. */
  static final String DOC1 = "first.xml";
  /** Test Documents. */
  static final String DOC2 = "second.xml";
  /** Test Documents. */
  static final String DOC3 = "third.xml";

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
  private static void init() throws Exception {
    // create an initial collection for testing
    final Collection coll = new BXCollection(COLL);
    final Resource res = coll.createResource(DOC1, XMLResource.RESOURCE_TYPE);
    res.setContent(read(DOC1));
    coll.storeResource(res);
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
