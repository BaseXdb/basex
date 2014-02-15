package org.basex.build;

import static org.junit.Assert.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.query.*;
import org.basex.*;
import org.junit.*;

/**
 * Tests queries on collections.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Michael Seiferle
 */
public final class CollectionPathTest extends SandboxTest {
  /** Test files directory. */
  private static final String DIR = "src/test/resources/";
  /** Test files. */
  private static final String[] FILES = {
    DIR + "input.xml", DIR + "xmark.xml", DIR + "test.xml"
  };
  /** Test ZIP. */
  private static final String ZIP = DIR + "xml.zip";

  /**
   * Creates an initial database.
   * @throws BaseXException exception
   */
  @BeforeClass
  public static void before() throws BaseXException {
    new CreateDB(NAME).execute(context);
    for(final String file : FILES) {
      new Add(DIR, file).execute(context);
    }
    new Add("test/zipped", ZIP).execute(context);
  }

  /**
   * Drops the initial collection.
   * @throws BaseXException exception
   */
  @AfterClass
  public static void after() throws BaseXException {
    new DropDB(NAME).execute(context);
  }

  /**
   * Finds single doc.
   * @throws Exception exception
   */
  @Test
  public void findDoc() throws Exception {
    final String find =
      "for $x in collection('" + NAME + '/' + DIR + "xmark.xml') " +
      "where $x//location contains text 'uzbekistan' " +
      "return $x";
    final QueryProcessor qp = new QueryProcessor(find, context);
    assertEquals(1, qp.execute().size());
    qp.close();
  }

  /**
   * Finds documents in path.
   * @throws Exception exception
   */
  @Test
  public void findDocs() throws Exception {
    final String find = "collection('" + NAME + "/test/zipped') ";
    final QueryProcessor qp = new QueryProcessor(find, context);
    assertEquals(4, qp.execute().size());
    qp.close();
  }

  /**
   * Checks if the constructed base-uri matches the base-uri of added documents.
   * @throws Exception exception
   */
  @Test
  public void baseUri() throws Exception {
    final String find =
      "for $x in collection('" + NAME + '/' + DIR + "xmark.xml') " +
      "return base-uri($x)";
    final QueryProcessor qp = new QueryProcessor(find, context);
    assertEquals(NAME + '/' + FILES[1], qp.iter().next().toJava());
    qp.close();
  }
}
