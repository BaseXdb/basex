package org.basex.examples.create;

import org.basex.build.Parser;
import org.basex.core.Context;
import org.basex.core.cmd.Create;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.XQuery;

/**
 * This example demonstrates a simple filesystem parser.
 *
 * @author BaseX Team 2005-12, BSD License
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

    final Parser parser = new SimpleFSParser(path);
    new Create(parser, name).execute(ctx);

    System.out.println("\n* Number of created elements:");

    new Open(name).execute(ctx);
    new XQuery(".").execute(ctx, System.out);

    System.out.println("\n\n* Drop database.");

    new DropDB(name).execute(ctx);
    ctx.close();
  }
}
