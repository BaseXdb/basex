package org.basex.test.examples;

import org.basex.core.BaseXException;
import org.basex.core.proc.XQuery;

/**
 * This class provides some examples for navigating and querying collections.
 * Querying is done via the {@link XQuery} class.
 * You may as well use the other query methods described in
 * {@link QueryExample}.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author BaseX Team
 */
public final class CollectionExamplesQuery extends CollectionExamples {
  
  /** The base-uri() function returns a filename. */
  private static final String FIND_FILE_QUERY = " for $doc in collection(\""
      + "MyFileCollection"
      + "\")"
      + "return <file>{base-uri($doc)}</file>";

  /** XQuery filter file by file-name. */
  private static final String FIND_BY_NAME = "for $x in "
      + "collection(\"MyFileCollection\")"
        + "let $fileName := base-uri($x)"
        + "where ends-with($fileName,'" + "%s" + "')"
      + "return <file>{$fileName}</file>";


  public static void main(final String[] args) {
    new CollectionExamplesQuery().run();
  }

  /**
   * Initializes the database context for the superclass.
   */
  public CollectionExamplesQuery() {
    super();
  }
  /**
   * Runs the examples.
   */
  private  void run() {
    try {
      // Superclass creates the collection.
      System.out.println("=== Silently create a collection.");
      createColl();

      // ----------------------------------------------------------------------
      System.out.println("=== II Evaluating queries.");
      System.out.println("===== Find all files...:");
      findFiles();
      
      // ----------------------------------------------------------------------
      System.out.println("\n===== Find specific file...:");
      findFileByName("factbook.xml");
      
   // -------------------------------------------------------------------------
   // Superclass Closes & Drops the Database.
      cleanup();
    } catch(final BaseXException e) {
      e.printStackTrace();
    }
  }

  /**
   * This method executes an XQuery Process for the given database context. 
   * The results are automatically serialized and printed to System.out.
   * @throws BaseXException in case the query contains errors.
   */
  private void findFiles() throws BaseXException {
    new XQuery(FIND_FILE_QUERY).execute(ctx, System.out);
  }
  /**
   * Searches for file in a collection by filename.
   * @param filename file to search.
   * @throws BaseXException on error.
   */
  private void findFileByName(final String filename) throws BaseXException {
    new XQuery(String.format(FIND_BY_NAME, filename)).
                                            execute(ctx, System.out);
  }
 

}
