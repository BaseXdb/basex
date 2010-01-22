package org.basex.test.examples;

import java.io.OutputStream;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.proc.Close;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.CreateIndex;
import org.basex.core.proc.DropDB;
import org.basex.core.proc.DropIndex;
import org.basex.core.proc.InfoDB;
import org.basex.core.proc.List;
import org.basex.core.proc.Open;
import org.basex.core.proc.Optimize;

/**
 * This class demonstrates database creation and dropping.
 * It then shows how to add indexes to the database and retrieve
 * some information on the database structures.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author BaseX Team
 */
public final class DatabaseExample {
  /** Database context. */
  static final Context CONTEXT = new Context();
  /** Output stream. */
  static final OutputStream OUT = System.out;

  /** Default constructor. */
  protected DatabaseExample() { }

  /**
   * Runs the example code.
   * @param args (ignored) command-line arguments
   * @throws BaseXException if a database command fails
   */
  public static void main(final String[] args) throws BaseXException {

    System.out.println("=== DBExample ===");

    // ------------------------------------------------------------------------
    // Create a database from a local or remote XML document or XML String.
    System.out.println("\n* Create a database.");

    new CreateDB("input.xml", "DBExample").execute(CONTEXT);

    // ------------------------------------------------------------------------
    // Close and reopen the database
    System.out.println("\n* Close and reopen database.");

    new Close().execute(CONTEXT);
    new Open("DBExample").execute(CONTEXT);

    // ------------------------------------------------------------------------
    // Additionally create a full-text index
    System.out.println("\n* Create a full-text index.");

    new CreateIndex("fulltext").execute(CONTEXT);

    // ------------------------------------------------------------------------
    // Show information on the currently opened database
    System.out.println("\n* Show database information:");

    new InfoDB().execute(CONTEXT, OUT);

    // ------------------------------------------------------------------------
    // Optimize the internal database structures.
    // This command is recommendable after all kinds of database updates
    System.out.println("\n* Optimize the database.");

    new Optimize().execute(CONTEXT);

    // ------------------------------------------------------------------------
    // Drop indexes to save disk space
    System.out.println("\n* Drop indexes.");

    new DropIndex("text").execute(CONTEXT);
    new DropIndex("attribute").execute(CONTEXT);
    new DropIndex("fulltext").execute(CONTEXT);
    new DropIndex("path").execute(CONTEXT);

    // ------------------------------------------------------------------------
    // Show all existing databases
    System.out.println("\n* Show existing databases:");

    new List().execute(CONTEXT, OUT);

    // ------------------------------------------------------------------------
    // Drop the database
    System.out.println("\n* Drop the database.");

    new DropDB("DBExample").execute(CONTEXT);
  }
}
