package org.basex.test.query;

import org.basex.core.BaseXException;
import org.basex.query.func.FunDef;
import org.basex.query.util.Err;
import org.junit.Test;

/**
 * This class tests the functions of the file library.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FNZipTest extends AdvancedQueryTest {
  /** Test ZIP file. */
  private static final String ZIP = "etc/xml/xml.zip";
  /** Test ZIP entry. */
  private static final String ENTRY1 = "infos/stopWords";
  /** Test ZIP entry. */
  private static final String ENTRY2 = "test/input.xml";

  /** Constructor. */
  public FNZipTest() {
    super("zip");
  }

  /**
   * Test method for the zip:binary-entry() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testBinaryEntry() throws BaseXException {
    final String fun = check(FunDef.BENTRY, String.class, String.class);
    query(fun + "('" + ZIP + "', '" + ENTRY1 + "')");
    contains("xs:hexBinary(" + fun + "('" + ZIP + "', '" + ENTRY1 + "'))",
        "610A61626F");
  }

  /**
   * Test method for the zip:binary-entry() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testTextEntry() throws BaseXException {
    final String fun = check(FunDef.TEXTENTRY,
        String.class, String.class, String.class);
    query(fun + "('" + ZIP + "', '" + ENTRY1 + "')");
    query(fun + "('" + ZIP + "', '" + ENTRY1 + "', 'US-ASCII')");
    error(fun + "('" + ZIP + "', '" + ENTRY1 + "', 'xyz')",
        Err.ZIPFAIL);
    // newlines are removed from the result..
    contains(fun + "('" + ZIP + "', '" + ENTRY1 + "')", "aaboutab");
  }

  /**
   * Test method for the zip:binary-entry() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testXMLEntry() throws BaseXException {
    final String fun = check(FunDef.XMLENTRY, String.class, String.class);
    query(fun + "('" + ZIP + "', '" + ENTRY2 + "')");
    query(fun + "('" + ZIP + "', '" + ENTRY2 + "')//title/text()", "XML");
  }

  /**
   * Test method for the zip:entries() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testEntries() throws BaseXException {
    final String fun = check(FunDef.ENTRIES, String.class);
    query(fun + "('" + ZIP + "')");
  }
}
