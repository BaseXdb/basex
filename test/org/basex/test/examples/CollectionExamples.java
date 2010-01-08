package org.basex.test.examples;

import java.io.OutputStream;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.proc.Add;
import org.basex.core.proc.Close;
import org.basex.core.proc.CreateColl;
import org.basex.core.proc.Delete;
import org.basex.core.proc.DropDB;
import org.basex.core.proc.InfoDB;
import org.basex.core.proc.Optimize;

/**
 * This class shows the basic usage of the BaseX Collection functions.
 * Collections provide access to several XML documents inside one database.
 * Collections may be created by importing a folder or adding files.
 * For more XQuery examples in a collection context
 * see {@link CollectionExamplesQuery}
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author BaseXTeam
 */
public class CollectionExamples {
  /** The current database Context. */
  static final Context CONTEXT = new Context();

  /** The path you want to add to your collection. */
  private static final String PATH = "./etc/";

  /** The file you want to add to your collection. */
  protected static final String XMLFILE = "input.xml";

  /** The database name. **/
  protected static final String DBNAME = "MyFileCollection";

  /**
   * PrintOutput Context. Point the PrintOutput to whatever file you like to
   * store the serializing results in a file. You may as well point it to
   * System.out.
   */
  private final OutputStream out = System.out;

  /**
   * Sets up the example class.
   * @param args not used.
   * @throws BaseXException if a database command fails
   */
  public static void main(final String[] args) throws BaseXException {
    // -------------------------------------------------------------------------
    // To import XML files with suffixes other than XML, for example KML use
    // this Property:
    // CONTEXT.prop.set("CREATEFILTER", "*.kml");
    // -------------------------------------------------------------------------

    final CollectionExamples collectionExamples = new CollectionExamples();
    System.out.println("=== Creating a collection.");
    collectionExamples.createColl(true);

    System.out.println("=== Remove an document from the collection.");
    collectionExamples.deleteDocumentFromColl();

    System.out.println("=== Closing & Dropping the collection.");
    collectionExamples.cleanup();
  }

  /**
   * Cleans up afterwards.
   * @throws BaseXException if a database command fails
   */
  protected void cleanup() throws BaseXException {
    new Close().execute(CONTEXT);
    new DropDB(DBNAME).execute(CONTEXT);
  }

  /**
   * This method creates a database using a Collection of XML files. The method
   * {@link CreateColl#CreateColl(String)} is used to create an
   * <strong>empty</strong> collection. You may add documents via
   * {@link Add#Add(String)} to any database that is opened in the current
   * context. Once the database has been created
   * @param verbose output the InfoDB if true.
   * @throws BaseXException if a database command fails
   */
  protected void createColl(final boolean verbose) throws BaseXException {
    new CreateColl(DBNAME).execute(CONTEXT);

    // -------------------------------------------------------------------------
    // Add some files and folders:
    new Add(XMLFILE).execute(CONTEXT);
    new Add(PATH).execute(CONTEXT);

    // -------------------------------------------------------------------------
    // Optimize the database structures
    new Optimize().execute(CONTEXT);

    /**
     * Output some information on your newly created database you may as well
     * run Queries on your collection, see the examples provided in
     * {@link QueryExample}.
     */
    if(verbose) new InfoDB().execute(CONTEXT, out);
  }

  /**
   * This command removes a single document from your collection.
   * @throws BaseXException if a database command fails
   */
  private void deleteDocumentFromColl() throws BaseXException {
    // -------------------------------------------------------------------------
    // Delete a file:
    new Delete(XMLFILE).execute(CONTEXT);

    // -------------------------------------------------------------------------
    // Optimize the database structures
    new Optimize().execute(CONTEXT);

    // -------------------------------------------------------------------------
    // Output some information on your altered collection
     try {
      new InfoDB().execute(CONTEXT, out);
    } catch(final BaseXException e) {
      e.printStackTrace();
    }
  }
}
