package org.basex.examples.create;

import org.basex.core.Context;
import org.basex.core.proc.ACreate;
import org.basex.core.proc.DropDB;
import org.basex.core.proc.XQuery;

/**
 * This example demonstrates how to import data in the LST format.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class LSTExample {
  /** Private constructor. */
  private LSTExample() { }

  /**
   * Main test method.
   * @param args command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {

    System.out.println("=== CreateLST ===");

    // create database context
    final Context ctx = new Context();

    System.out.println("\n* Import an LST file.");

    // input file and name of database
    final String file = "etc/basex.lst";
    final String name = "lstexample";

    new ACreate(new LSTParser(file), name).execute(ctx);

    System.out.println("\n* Number of created elements:");

    new XQuery("count(//*)").execute(ctx, System.out);

    System.out.println("\n\n* Drop database.");

    new DropDB(name).execute(ctx);
    ctx.close();
  }
}