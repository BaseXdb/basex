package org.basex.test.examples;

import java.io.OutputStream;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.proc.Add;
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
  final Context ctx;

  /** The path you want to add to your collection. */
  private static final String PATH = "./etc/";

  /** The database name. **/
  protected static final String DBNAME = "MyFileCollection";

  /**
   * PrintOutput Context. Point the PrintOutput to whatever file you like to
   * store the serializing results in a file. You may as well point it to
   * System.out.
   */
  private final OutputStream out;

  /** Flag to show verbose output. */
  private boolean verbose;

  /**
   * Runs the examples.
   * @param args not used.
   * @throws BaseXException if a database command fails
   */
  public static void main(final String[] args) throws BaseXException {
    new CollectionExamples().run();
  }
  /**
   * Constructor initializes the Database Context.
   * Points 'out' to System.out.
   * Sets verbosity (on or off).
   */
  public CollectionExamples() {
    ctx = new Context();
    out  = System.out;
    verbose = false;
  }
  
  /**
   * Runs the examples.
   * @throws BaseXException on error.
   */
  private void run() throws BaseXException {
    System.out.println("=== Creating a collection.");
    createColl();

    System.out.println("=== Add an document to the collection.");
    addFile("input.xml");

    System.out.println("=== Remove an document from the collection.");
    deleteDocumentFromColl("input.xml");

    System.out.println("=== Closing & Dropping the collection.");
    cleanup();

  }
  /**
   * Cleans up afterwards.
   * Close is implicitly called when executing DropDB.
   * @throws BaseXException if a database command fails
   */
  protected void cleanup() throws BaseXException {
    new DropDB(DBNAME).execute(ctx);
  }

  /**
   * This method creates a database using a Collection of XML files. The method
   * {@link CreateColl#CreateColl(String)} is used to create an
   * <strong>empty</strong> collection. 
   * Once the database has been created it is optimized.
   * @throws BaseXException if a database command fails
   */
  protected void createColl() throws BaseXException {
    // -------------------------------------------------------------------------
    // To import XML files with suffixes other than XML, for example KML use
    // this Property:
    // ctx.prop.set("CREATEFILTER", "*.kml");
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // Create an empty collection:
    new CreateColl(DBNAME).execute(ctx);

    new Add(PATH).execute(ctx);

    // -------------------------------------------------------------------------
    // Optimize the database structures
    new Optimize().execute(ctx);
    
    /**
     * Output some information on your newly created database you may as well
     * run Queries on your collection, see the examples provided in
     * {@link QueryExample}.
     */
    if(verbose) new InfoDB().execute(ctx, out);
  }
  
  /**
   * Adds a single file to the collection.
   * You may add documents and folders via
   * {@link Add#Add(String)} to any database that is opened in the current
   * context.
   * @param xmlfile the file to add.
   * @throws BaseXException on fail.
   */
  private void addFile(final String xmlfile) throws BaseXException {
    // -------------------------------------------------------------------------
    // Adds a single file:
    new Add(xmlfile).execute(ctx);
  }
  
  /**
   * This command removes a single document from your collection.
   * @param xmlfile the file to delete.
   * @throws BaseXException if a database command fails
   */
  private void deleteDocumentFromColl(final String xmlfile) 
    throws BaseXException {
    // -------------------------------------------------------------------------
    // Delete a file:
    new Delete(xmlfile).execute(ctx);

    // -------------------------------------------------------------------------
    // Optimize the database structures
    new Optimize().execute(ctx);

    // -------------------------------------------------------------------------
    // Output some information on your altered collection
    if(verbose) new InfoDB().execute(ctx, out);
  }
}
