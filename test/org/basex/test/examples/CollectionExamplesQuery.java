package org.basex.test.examples;

import org.basex.core.BaseXException;
import org.basex.core.proc.XQuery;

/**
 * @author BaseXTeam
 * This class provides you with some examples to navigate and query collections.
 */
public class CollectionExamplesQuery extends CollectionExamples {
  /** The base-uri() function returns a filename. */
  private static final String FIND_FILE_QUERY = " for $doc in collection(\""
      + CollectionExamples.DBNAME 
      + "\")"
      + "return base-uri($doc)"; 

  public static void main(final String[] args) {
    CollectionExamplesQuery queryExamples = new CollectionExamplesQuery();
    
    System.out.println("=== Silently create a collection.");
    queryExamples.createColl(false);
    try {
      queryExamples.findFiles();
    } catch(BaseXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
  }
  /**
   * This method exexcutes an XQuery Process for the given database context. The
   * results are automatically serialized and printed to an arbitrary
   * OutputStream.
   * @throws BaseXException in case your query contains errors.
   */
  private void findFiles() throws BaseXException {
    System.out.println("\n=== II Evaluating queries.");
    System.out.println("===== XQuery Proc: direct output.");
    new XQuery(FIND_FILE_QUERY).exec(CONTEXT, System.out);

  }

}
