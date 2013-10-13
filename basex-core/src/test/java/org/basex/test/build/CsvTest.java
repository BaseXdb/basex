package org.basex.test.build;

import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.test.*;
import org.junit.*;

/**
 * CSV Parser Test.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class CsvTest extends SandboxTest {
  /** Test CSV file. */
  private static final String FILE = "src/test/resources/input.csv";
  /** Temporary CSV file. */
  private static final String TEMP = Prop.TMP + NAME + IO.CSVSUFFIX;

  /**
   * Creates the initial database.
   * @throws BaseXException exception
   */
  @BeforeClass
  public static void before() throws BaseXException {
    new Set(MainOptions.PARSER, DataText.M_CSV).execute(context);
  }

  /**
   * Removes the temporary CSV file.
   */
  @AfterClass
  public static void after() {
    new IOFile(TEMP).delete();
  }

  /**
   * Sets initial options.
   * @throws BaseXException exception
   */
  @Before
  public void init() throws BaseXException {
    new Set(MainOptions.CSVPARSER, "header=true").execute(context);
  }

  /**
   * Drops the database.
   * @throws BaseXException exception
   */
  @After
  public void finish() throws BaseXException {
    new DropDB(NAME).execute(context);
  }

  /**
   * Adds an empty CSV file.
   * @throws Exception exception
   */
  @Test
  public void empty() throws Exception {
    write("");
    new CreateDB(NAME, TEMP).execute(context);
    assertEquals("<csv/>", new XQuery(".").execute(context));
  }

  /**
   * Adds the sample CSV file.
   * @throws Exception exception
   */
  @Test
  public void one() throws Exception {
    new CreateDB(NAME, FILE).execute(context);
    assertEquals("3", new XQuery("count(//Name)").execute(context));
    assertEquals("2", new XQuery("count(//Email)").execute(context));

    new Set(MainOptions.CSVPARSER, "header=true").execute(context);
    new CreateDB(NAME, FILE).execute(context);
    assertEquals("3", new XQuery("count(//record)").execute(context));
    assertEquals("true", new XQuery("//text() = 'Picard?'").execute(context));
  }

  /**
   * Adds the sample CSV file, using different separators.
   * @throws Exception exception
   */
  @Test
  public void sep() throws Exception {
    new Set(MainOptions.CSVPARSER, "separator=tab,header=true").execute(context);
    new CreateDB(NAME, FILE).execute(context);
    assertEquals("0", new XQuery("count(//Name)").execute(context));
    new Set(MainOptions.CSVPARSER, "separator=;,header=true").execute(context);
    new CreateDB(NAME, FILE).execute(context);
    assertEquals("0", new XQuery("count(//Name)").execute(context));
  }

  /**
   * Writes the specified test file.
   * @param data data to write
   * @throws IOException I/O exception
   */
  private static void write(final String data) throws IOException {
    new IOFile(TEMP).write(token(data));
  }
}
