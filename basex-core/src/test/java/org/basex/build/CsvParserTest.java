package org.basex.build;

import static org.junit.Assert.*;

import org.basex.*;
import org.basex.build.csv.*;
import org.basex.build.csv.CsvOptions.*;
import org.basex.core.*;
import org.basex.core.MainOptions.MainParser;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.util.*;
import org.junit.*;
import org.junit.Test;

/**
 * CSV Parser Test.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class CsvParserTest extends SandboxTest {
  /** CSV options. */
  private CsvParserOptions copts;

  /** Test CSV file. */
  private static final String FILE = "src/test/resources/input.csv";
  /** Temporary CSV file. */
  private static final String TEMP = Prop.TMP + NAME + IO.CSVSUFFIX;

  /**
   * Creates the initial database.
   */
  @BeforeClass
  public static void before() {
    set(MainOptions.PARSER, MainParser.CSV);
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
   */
  @Before
  public void init() {
    copts = new CsvParserOptions();
    context.options.set(MainOptions.CSVPARSER, copts);
  }

  /**
   * Drops the database.
   */
  @After
  public void finish() {
    execute(new DropDB(NAME));
  }

  /**
   * Adds an empty CSV file.
   */
  @Test
  public void empty() {
    write(new IOFile(TEMP), "");
    execute(new CreateDB(NAME, TEMP));
    assertEquals("<csv/>", query("."));
  }

  /**
   * Adds the sample CSV file.
   */
  @Test
  public void one() {
    copts.set(CsvOptions.HEADER, true);
    execute(new CreateDB(NAME, FILE));
    assertEquals("3", query("count(//Name)"));
    assertEquals("2", query("count(//Email)"));

    execute(new CreateDB(NAME, FILE));
    assertEquals("3", query("count(//record)"));
    assertEquals("true", query("//text() = 'Picard'"));
  }

  /**
   * Adds the sample CSV file, using different separators.
   */
  @Test
  public void separator() {
    copts.set(CsvOptions.HEADER, true);

    copts.set(CsvOptions.SEPARATOR, "tab");
    execute(new CreateDB(NAME, FILE));
    assertEquals("0", query("count(//Name)"));

    copts.set(CsvOptions.SEPARATOR, ";");
    execute(new CreateDB(NAME, FILE));
    assertEquals("0", query("count(//Name)"));
  }

  /**
   * Checks the quotes flag.
   */
  @Test
  public void quotes() {
    copts.set(CsvOptions.HEADER, true);

    copts.set(CsvOptions.QUOTES, false);
    execute(new CreateDB(NAME, FILE));
    assertEquals("\"H ", query("(//Props[1])/text()"));

    copts.set(CsvOptions.QUOTES, true);
    execute(new CreateDB(NAME, FILE));
    assertEquals("H \"U\\", query("normalize-space((//Props)[1])"));
  }

  /**
   * Checks the backslash flag.
   */
  @Test
  public void backslash() {
    copts.set(CsvOptions.HEADER, true);

    // "H \n""U\",a@b.c....
    copts.set(CsvOptions.BACKSLASHES, false);
    execute(new CreateDB(NAME, FILE));
    // H \n"U\
    assertEquals("H \"U\\", query("normalize-space((//Props)[1])"));

    copts.set(CsvOptions.BACKSLASHES, true);
    execute(new CreateDB(NAME, FILE));
    // H \nU,a
    assertEquals("H U\"", query("replace(normalize-space((//Props)[1]), ',.*', '')"));
  }

  /**
   * Adds the sample CSV file, using different separators.
   */
  @Test
  public void atts() {
    copts.set(CsvOptions.HEADER, true);
    copts.set(CsvOptions.FORMAT, CsvFormat.ATTRIBUTES);
    execute(new CreateDB(NAME, FILE));
    assertEquals("true", query("exists(//entry[@name = 'Name'])"));
  }
}
