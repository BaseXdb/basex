package org.basex.test.build;

import static org.junit.Assert.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Set;
import org.basex.core.cmd.XQuery;
import org.basex.io.IOFile;
import org.basex.util.Util;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * CSV Parser Test.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class CSVTest {
  /** Database context. */
  private static final Context CONTEXT = new Context();
  /** Test database name. */
  private static final String NAME = Util.name(CSVTest.class);
  /** Test CSV file. */
  private static final String FILE = "src/test/resources/input.csv";

  /**
   * Creates the initial database.
   * @throws BaseXException exception
   */
  @BeforeClass
  public static void before() throws BaseXException {
    new Set(Prop.PARSER, "csv").execute(CONTEXT);
  }

  /**
   * Removes the temporary CSV file.
   */
  @AfterClass
  public static void after() {
    new IOFile(Prop.TMP, NAME).delete();
  }

  /**
   * Sets initial options.
   * @throws BaseXException exception
   */
  @Before
  public void init() throws BaseXException {
    new Set(Prop.PARSEROPT, "header=true").execute(CONTEXT);
  }

  /**
   * Drops the database.
   * @throws BaseXException exception
   */
  @After
  public void finish() throws BaseXException {
    new DropDB(NAME).execute(CONTEXT);
  }

  /**
   * Adds an empty CSV file.
   * @throws Exception exception
   */
  @Test
  public void empty() throws Exception {
    write("");
    new CreateDB(NAME, Prop.TMP + NAME).execute(CONTEXT);
    assertEquals("<csv/>", new XQuery(".").execute(CONTEXT));
  }

  /**
   * Adds the sample CSV file.
   * @throws Exception exception
   */
  @Test
  public void one() throws Exception {
    new CreateDB(NAME, FILE).execute(CONTEXT);
    assertEquals("3", new XQuery("count(//Name)").execute(CONTEXT));
    assertEquals("2", new XQuery("count(//Email)").execute(CONTEXT));

    new Set(Prop.PARSEROPT, "format=simple,header=true").execute(CONTEXT);
    new CreateDB(NAME, FILE).execute(CONTEXT);
    assertEquals("3", new XQuery("count(//record)").execute(CONTEXT));
  }

  /**
   * Adds the sample CSV file, using the simple XML format.
   * @throws Exception exception
   */
  @Test
  public void simple() throws Exception {
    new Set(Prop.PARSEROPT, "format=simple,header=true").execute(CONTEXT);
    new CreateDB(NAME, FILE).execute(CONTEXT);
    assertEquals("3", new XQuery("count(//record)").execute(CONTEXT));
  }

  /**
   * Adds the sample CSV file, using different separators.
   * @throws Exception exception
   */
  @Test
  public void sep() throws Exception {
    new Set(Prop.PARSEROPT, "separator=tab,header=true").execute(CONTEXT);
    new CreateDB(NAME, FILE).execute(CONTEXT);
    assertEquals("0", new XQuery("count(//Name)").execute(CONTEXT));
  }

  /**
   * Writes the specified test file.
   * @param data data to write
   * @throws IOException I/O exception
   */
  private static void write(final String data) throws IOException {
    new IOFile(Prop.TMP, NAME).write(token(data));
  }
}
