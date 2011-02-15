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
import org.basex.io.IO;
import org.basex.util.Util;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * CSV Parser Test.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class CSVTest {
  /** Database context. */
  private static final Context CTX = new Context();
  /** Test database name. */
  private static final String DBNAME = Util.name(CSVTest.class);

  /**
   * Creates the initial database.
   * @throws BaseXException exception
   */
  @BeforeClass
  public static void before() throws BaseXException {
    new Set(Prop.PARSER, "csv").execute(CTX);
  }

  /**
   * Removes the temporary CSV file.
   */
  @AfterClass
  public static void after() {
    IO.get(Prop.TMP + DBNAME).delete();
  }

  /**
   * Sets initial options.
   * @throws BaseXException exception
   */
  @After
  public void init() throws BaseXException {
    new Set(Prop.PARSEROPT, "").execute(CTX);
  }

  /**
   * Drops the database.
   * @throws BaseXException exception
   */
  @After
  public void finish() throws BaseXException {
    new DropDB(DBNAME).execute(CTX);
  }

  /**
   * Adds an empty CSV file.
   * @throws Exception exception
   */
  @Test
  public void empty() throws Exception {
    write("");
    new CreateDB(DBNAME, Prop.TMP + DBNAME).execute(CTX);
    assertEquals("<csv/>", new XQuery(".").execute(CTX));
  }

  /**
   * Adds the sample CSV file.
   * @throws Exception exception
   */
  @Test
  public void one() throws Exception {
    new CreateDB(DBNAME, "etc/xml/input.csv").execute(CTX);
    assertEquals("3", new XQuery("count(//Name)").execute(CTX));
    assertEquals("2", new XQuery("count(//Email)").execute(CTX));

    new Set(Prop.PARSEROPT, "format=simple").execute(CTX);
    new CreateDB(DBNAME, "etc/xml/input.csv").execute(CTX);
    assertEquals("3", new XQuery("count(//record)").execute(CTX));
  }

  /**
   * Adds the sample CSV file, using the simple XML format.
   * @throws Exception exception
   */
  @Test
  public void simple() throws Exception {
    new Set(Prop.PARSEROPT, "format=simple").execute(CTX);
    new CreateDB(DBNAME, "etc/xml/input.csv").execute(CTX);
    assertEquals("3", new XQuery("count(//record)").execute(CTX));
  }

  /**
   * Adds the sample CSV file, using different separators.
   * @throws Exception exception
   */
  @Test
  public void sep() throws Exception {
    new Set(Prop.PARSEROPT, "separator=tab").execute(CTX);
    new CreateDB(DBNAME, "etc/xml/input.csv").execute(CTX);
    assertEquals("0", new XQuery("count(//Name)").execute(CTX));
  }

  /**
   * Writes the specified test file.
   * @param data data to write
   * @throws IOException I/O exception
   */
  private void write(final String data) throws IOException {
    IO.get(Prop.TMP + DBNAME).write(token(data));
  }
}
