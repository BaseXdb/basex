package org.basex.data;

import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import java.util.*;
import java.util.List;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.junit.*;
import org.junit.Test;
import org.junit.runner.*;
import org.junit.runners.*;
import org.junit.runners.Parameterized.*;

/**
 * This class tests the update features of the {@link Data} class.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Tim Petrowsky
 */
@RunWith(Parameterized.class)
public abstract class DataUpdateTest extends SandboxTest {
  /** Test file we do updates with. */
  private static final String TESTFILE = "src/test/resources/test.xml";

  /** JUnit tag. */
  static final byte[] T_JUNIT = token("junit");
  /** JUnit tag. */
  static final byte[] T_FOO = token("foo");
  /** JUnit tag. */
  static final byte[] T_NAME = token("name");
  /** JUnit tag. */
  static final byte[] T_PARENTNODE = token("parentnode");
  /** JUnit tag. */
  static final byte[] T_CONTEXTNODE = token("contextnode");
  /** JUnit tag. */
  static final byte[] T_ID = token("id");
  /** JUnit tag. */
  static final byte[] T_B = token("b");
  /** Test file size in nodes. */
  int size;

  /** Main memory flag. */
  @Parameter
  public Object mainmem;

  /**
   * Test parameters.
   * @return parameters
   */
  @Parameters
  public static List<Object[]> params() {
    return Arrays.asList(new Object[][] { { false }, { true } });
  }

  /**
   * Initializes the test class.
   */
  @BeforeClass
  public static void setUpBeforeClass() {
    set(MainOptions.TEXTINDEX, false);
    set(MainOptions.ATTRINDEX, false);
  }

  /**
   * Creates the database.
   */
  @Before
  public final void setUp() {
    set(MainOptions.MAINMEM, mainmem);
    execute(new CreateDB(NAME, TESTFILE));
    size = context.data().meta.size;
  }

  /**
   * Deletes the test database.
   */
  @After
  public final void tearDown() {
    if((Boolean) mainmem) return;
    execute(new Close());
    execute(new DropDB(NAME));
  }

  /**
   * Reloads the database.
   */
  void reload() {
    if((Boolean) mainmem) return;
    execute(new Close());
    execute(new Open(NAME));
  }

  /**
   * Tests byte-arrays for equality.
   * @param exp expected value
   * @param act actual value
   */
  static void assertArraysEquals(final byte[] exp, final byte[] act) {
    final int el = exp.length;
    assertEquals("array lengths don't equal", el, act.length);
    for(int e = 0; e < el; e++) assertEquals(exp[e], act[e]);
  }

  /**
   * Tests for correct data size.
   */
  @Test
  public final void size() {
    assertEquals("Unexpected size!", size, context.data().meta.size);
    reload();
    assertEquals("Unexpected size!", size, context.data().meta.size);
  }
}
