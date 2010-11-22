package org.basex.test.collections;

import static org.junit.Assert.*;
import org.basex.core.BaseXException;
import org.basex.core.Commands;
import org.basex.core.Context;
import org.basex.core.cmd.Add;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.CreateIndex;
import org.basex.core.cmd.DropDB;
import org.basex.query.QueryProcessor;
import org.basex.query.item.Item;
import org.basex.util.Util;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests some queries on collections.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Michael Seiferle
 */
public final class CollectionPathTest {
  /** Database context. */
  private static final Context CTX = new Context();

  /** Test database name. */
  private static final String DBNAME = Util.name(CollectionPathTest.class);
  /** Test files. */
  private static final String[] FILES = {
    "etc/xml/input.xml", "etc/xml/xmark.xml", "etc/xml/test.xml"
  };
  /** Test ZIP. */
  private static final String ZIP = "etc/xml/xml.zip";

  /**
   * Creates an initial database.
   * @throws BaseXException exception
   */
  @BeforeClass
  public static void before() throws BaseXException {
    new CreateDB(DBNAME).execute(CTX);
    for(final String file : FILES) new Add(file, null, "etc/xml").execute(CTX);
    new Add(ZIP, null, "test/zipped").execute(CTX);
    new CreateIndex(Commands.CmdIndex.FULLTEXT);
  }

  /**
   * Drops the initial collection.
   * @throws BaseXException exception
   */
  @AfterClass
  public static void after() throws BaseXException {
    new DropDB(DBNAME).execute(CTX);
    CTX.close();
  }

  /**
   * Finds single doc.
   * @throws Exception exception
   */
  @Test
  public void testFindDoc() throws Exception {
    final String find = "for $x in ."
      + " where $x[ends-with(document-uri(.), '" + FILES[1] + "')]"
      + " and $x//location contains text 'uzbekistan' "
      + " return base-uri($x)";
    final QueryProcessor qp = new QueryProcessor(find, CTX);
    assertEquals(1, qp.execute().size());
    qp.close();
  }

  /**
   * Finds documents in path.
   * @throws Exception exception
   */
  @Test
  public void testFindDocs() throws Exception {
    final String find = "for $x in ."
        + " where $x[matches(document-uri(.), 'test/zipped/')]"
        + " return base-uri($x)";
    final QueryProcessor qp = new QueryProcessor(find, CTX);
    assertEquals(4, qp.execute().size());
    qp.close();
  }

  /**
   * Checks if the constructed base-uri matches the base-uri of added documents.
   * @throws Exception exception
   */
  @Test
  public void testBaseUri() throws Exception {
    final String find = "for $x in ."
      + " where $x[ends-with(document-uri(.), '" + FILES[1] + "')]"
      + " return base-uri($x)";
    final QueryProcessor qp = new QueryProcessor(find, CTX);
    final Item it = qp.iter().next();
    final String expath = '"' + CTX.data.meta.file.url().replace(DBNAME, "")
        + FILES[1] + '"';
    assertEquals(expath, it.toString());
    qp.close();
  }
}
