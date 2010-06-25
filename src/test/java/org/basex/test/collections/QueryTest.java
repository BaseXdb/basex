package org.basex.test.collections;

import static org.junit.Assert.*;
import org.basex.core.BaseXException;
import org.basex.core.Commands;
import org.basex.core.Context;
import org.basex.core.proc.Add;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.CreateIndex;
import org.basex.core.proc.DropDB;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.item.Item;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests some queries on collections.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Michael Seiferle
 */
public class QueryTest {
  /** Database context. */
  private static final Context CTX = new Context();

  /** Test files. */
  private static final String[] FILES = { "etc/xml/input.xml",
      "etc/xml/factbook.xml", "etc/xml/test.xml"};
  /** Test ZIP. */
  private static final String ZIP = "etc/xml/xml.zip";
  /** Test DB name. */
  private static final String NAME = "CollectionQueryUnitTest";

  /**
   * Creates an initial database.
   * @throws BaseXException exception
   */
  @BeforeClass
  public static void before() throws BaseXException {
    new CreateDB(NAME).execute(CTX);
    for(String file : FILES) new Add(file, null, "etc/xml").execute(CTX);
    new Add(ZIP, null, "test/zipped").execute(CTX);
    new CreateIndex(Commands.CmdIndex.FULLTEXT);
  }

  /**
   * Drops the initial collection.
   * @throws BaseXException exception
   */
  @AfterClass
  public static void after() throws BaseXException {
    new DropDB(NAME).execute(CTX);
  }

  /**
   * Finds single doc.
   * @throws QueryException query exception
   */
  @Test
  public void testFindDoc() throws QueryException {
    final String find = "for $x in ."
        + " where $x[ends-with(document-uri(.), '" + FILES[1] + "')]"
        + " and $x//religions/text() contains text 'Catholic' "
        + " return base-uri($x)";
    QueryProcessor qp = new QueryProcessor(find, CTX);
    assertEquals(1, qp.query().size());

  }

  /**
   * Finds documents in path.
   * @throws QueryException query exception
   */
  @Test
  public void testFindDocs() throws QueryException {
    final String find = "for $x in ."
        + " where $x[matches(document-uri(.), 'test/zipped/')]"
        + " return base-uri($x)";
    QueryProcessor qp = new QueryProcessor(find, CTX);
    assertEquals(4, qp.query().size());

  }

  /**
   * Checks if the constructed base-uri matches the base-uri of added documents.
   * @throws QueryException query exception
   */
  @Test
  public void testBaseUri() throws QueryException {
    final String find = "for $x in ."
        + " where $x[ends-with(document-uri(.), '" + FILES[1] + "')]"
        + " return base-uri($x)";
    final QueryProcessor qp = new QueryProcessor(find, CTX);
    final Item it = qp.iter().next();
    final String expath = '"' + CTX.data.meta.file.url().replace(NAME, "")
        + FILES[1] + '"';
    assertEquals(expath, it.toString());

  }
}
