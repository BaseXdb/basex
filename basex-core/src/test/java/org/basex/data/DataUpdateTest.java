package org.basex.data;

import static org.basex.util.Token.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.List;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * This class tests the update features of the {@link Data} class.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Tim Petrowsky
 */
//@RunWith(Parameterized.class)
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

  /**
   * Test parameters.
   * @return parameters
   */
  public static List<Object[]> params() {
    return Arrays.asList(new Object[][] { { false }, { true } });
  }

  /**
   * Initializes the test class.
   */
  @BeforeAll public static void setUpBeforeClass() {
    set(MainOptions.TEXTINDEX, false);
    set(MainOptions.ATTRINDEX, false);
  }

  /**
   * Creates the database.
   * @param mainmem main-memory flag
   */
  public final void setUp(final boolean mainmem) {
    set(MainOptions.MAINMEM, mainmem);
    execute(new CreateDB(NAME, TESTFILE));
    size = context.data().meta.size;
  }

  /**
   * Deletes the test database.
   */
  @AfterEach public final void tearDown() {
    execute(new Close());
    execute(new DropDB(NAME));
  }

  /**
   * Reloads the database.
   * @param mainmem main memory flag
   */
  static void reload(final boolean mainmem) {
    if(mainmem) return;
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
    assertEquals(el, act.length, "array lengths don't equal");
    for(int e = 0; e < el; e++) assertEquals(exp[e], act[e]);
  }

  /**
   * Tests for correct data size.
   * @param mainmem main-memory flag
   */
  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  public final void size(final boolean mainmem) {
    setUp(mainmem);

    assertEquals(size, context.data().meta.size, "Unexpected size!");
    reload(mainmem);
    assertEquals(size, context.data().meta.size, "Unexpected size!");
  }
}
