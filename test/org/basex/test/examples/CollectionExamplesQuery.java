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
      + CollectionExamples.DBNAME
      + "\")"
      + "return <file>{base-uri($doc)}</file>";

  /** XQuery filter file by file-name. */
  private static final String FIND_BY_NAME = "for $x in "
      + "collection(\"MyFileCollection\")"
        + "let $fileName := base-uri($x)"
        + "where ends-with($fileName,'" + XMLFILE + "')"
      + "return <file>{$fileName}</file>";


  public static void main(final String[] args) {
    final CollectionExamplesQuery queryExamples = new CollectionExamplesQuery();

    System.out.println("=== Silently create a collection.");
    queryExamples.createColl(false);
    try {
      queryExamples.findFiles();
    } catch(final BaseXException e) {
      e.printStackTrace();
    }
    queryExamples.cleanup();
  }
  /**
   * This method exexcutes an XQuery Process for the given database context. The
   * results are automatically serialized and printed to an arbitrary
   * OutputStream.
   * @throws BaseXException in case your query contains errors.
   */
  private void findFiles() throws BaseXException {
    System.out.println("\n=== II Evaluating queries.");
    System.out.println("===== Find all files in collection:");
    new XQuery(FIND_FILE_QUERY).exec(CONTEXT, System.out);

    // -------------------------------------------------------------------------
    System.out.println("\n\n===== Find all filenames containing 'text':");
    new XQuery(FIND_BY_NAME).exec(CONTEXT, System.out);

  }

}
