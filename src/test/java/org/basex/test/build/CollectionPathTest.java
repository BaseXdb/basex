package org.basex.test.build;

import static org.junit.Assert.*;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.Add;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.query.QueryProcessor;
import org.basex.util.Util;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests queries on collections.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Michael Seiferle
 */
public final class CollectionPathTest {
  /** Database context. */
  private static final Context CONTEXT = new Context();
  /** Test database name. */
  private static final String DB = Util.name(CollectionPathTest.class);
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
    new CreateDB(DB).execute(CONTEXT);
    for(final String file : FILES) {
      new Add(DIR, file).execute(CONTEXT);
    }
    new Add("test/zipped", ZIP).execute(CONTEXT);
  }

  /**
   * Drops the initial collection.
   * @throws BaseXException exception
   */
  @AfterClass
  public static void after() throws BaseXException {
    new DropDB(DB).execute(CONTEXT);
  }

  /**
   * Finds single doc.
   * @throws Exception exception
   */
  @Test
  public void findDoc() throws Exception {
    final String find =
      "for $x in collection('" + DB + '/' + DIR + "xmark.xml') " +
      "where $x//location contains text 'uzbekistan' " +
      "return $x";
    final QueryProcessor qp = new QueryProcessor(find, CONTEXT);
    assertEquals(1, qp.execute().size());
    qp.close();
  }

  /**
   * Finds documents in path.
   * @throws Exception exception
   */
  @Test
  public void findDocs() throws Exception {
    final String find = "collection('" + DB + "/test/zipped') ";
    final QueryProcessor qp = new QueryProcessor(find, CONTEXT);
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
      "for $x in collection('" + DB + '/' + DIR + "xmark.xml') " +
      "return base-uri($x)";
    final QueryProcessor qp = new QueryProcessor(find, CONTEXT);
    assertEquals(FILES[1], qp.iter().next().toJava());
    qp.close();
  }
}
