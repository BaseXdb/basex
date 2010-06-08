package org.basex.examples.create;

import org.basex.core.Context;
import org.basex.core.proc.ACreate;
import org.basex.core.proc.DropDB;
import org.basex.core.proc.XQuery;

/**
 * This example demonstrates a simple filesystem parser.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class SimpleFSExample {
  /** Private constructor. */
  private SimpleFSExample() { }

  /**
   * Main test method.
   * @param args command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {

    System.out.println("=== CreateLST ===");

    // create database context
    final Context ctx = new Context();

    System.out.println("\n* Import a directory.");

    // input path and name of database
    final String path = ".";
    final String name = "fsexample";

    new ACreate(new SimpleFSParser(path), name).execute(ctx);

    System.out.println("\n* Number of created elements:");

    new XQuery("count(//*)").execute(ctx, System.out);

    System.out.println("\n\n* Drop database.");

    new DropDB(name).execute(ctx);
    ctx.close();
  }
}
