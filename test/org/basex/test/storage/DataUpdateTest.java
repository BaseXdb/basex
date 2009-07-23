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
   * Initializes the test class.
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
   */
  @Before
  public void setUp() {
    try {
      data = CreateDB.xml(IO.get(TESTFILE), dbname);
      size = data.meta.size;
    } catch(final Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * Delete the test-database.
   */
  @After
  public void tearDown() {
    try {
      data.close();
      DropDB.drop(dbname);
    } catch(final Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Reload Data class.
   */
  void reload() {
    try {
      data.close();
      data = new DiskData(dbname);
    } catch(final Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * Test byte-arrays for equality.
   * @param exp expected value
   * @param act actual value
   */
  void assertByteArraysEqual(final byte[] exp, final byte[] act) {
    assertEquals("array lengths don't equal", exp.length, act.length);
    for(int i = 0; i < exp.length; i++) assertEquals(exp[i], act[i]);
  }

  /**
   * Test for correct data size.
   */
  @Test
  public void testSize() {
    assertEquals("Unexpected size!", size, data.meta.size);
    reload();
    assertEquals("Unexpected size!", size, data.meta.size);
  }
}
