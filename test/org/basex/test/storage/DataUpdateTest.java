package org.basex.test.storage;

import static org.basex.util.Token.*;
import static org.junit.Assert.*;
import org.basex.core.Prop;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.DropDB;
import org.basex.data.Data;
import org.basex.data.DiskData;
import org.basex.io.IO;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the update features of the Data class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Tim Petrowsky
 */
public abstract class DataUpdateTest {
  /** JUnit tag. */
  static final byte[] JUNIT = token("junit");
  /** Test file we do updates with. */
  static final String TESTFILE = "etc/xml/test.xml";
  /** Test database name. */
  final String dbname = getClass().getSimpleName();

  /** Test file size in nodes. */
  int size;
  /** Data. */
  Data data;

  /**
   * Delete the test-database.
   */
  @BeforeClass
  public static final void setUpBeforeClass() {
    Prop.read();
    Prop.textindex = false;
    Prop.attrindex = false;
    Prop.chop = true;
  }

  /**
   * Create the database.
   * @throws Exception exception
   */
  @Before
  @SuppressWarnings("unused")
  public void setUp() throws Exception {
    data = CreateDB.xml(IO.get(TESTFILE), dbname);
    size = data.meta.size;
  }
  
  /**
   * Delete the test-database.
   * @throws Exception in case of problems.
   */
  @After
  public void tearDown() throws Exception {
    data.close();
    DropDB.drop(dbname);
  }

  /**
   * Reload Data class.
   * @throws Exception in case of problems.
   */
  void reload() throws Exception {
    data.close();
    data = new DiskData(dbname);
  }

  /**
   * Test byte-arrays for equality.
   * @param exp expected value
   * @param act actual value
   */
  void assertByteArraysEqual(final byte[] exp, final byte[] act) {
    assertEquals("array lengths don't equal", exp.length, act.length);
    for(int i = 0; i < exp.length; i++) {
      assertEquals(exp[i], act[i]);
    }
  }

  /**
   * Test for correct data size.
   * @throws Exception in case of problems.
   */
  @Test
  public void testSize() throws Exception {
    assertEquals("Unexpected size!", size, data.meta.size);
    reload();
    assertEquals("Unexpected size!", size, data.meta.size);
  }
}
