package org.basex.examples.create;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.core.cmd.*;

/**
 * This example demonstrates a simple filesystem parser.
 *
 * @author BaseX Team 2005-13, BSD License
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
    System.out.println("=== SimpleFSExample ===");

    // create database context
    final Context ctx = new Context();

    System.out.println("\n* Import a directory.");

    // input path and name of database
    final String path = ".";
    final String name = "fsexample";

    final Parser parser = new SimpleFSParser(path, ctx.options);
    final CreateDB create = new CreateDB(name);
    create.setParser(parser);
    create.execute(ctx);

    System.out.println("\n* Number of created elements:");

    new Open(name).execute(ctx);
    new XQuery(".").execute(ctx, System.out);

    System.out.println("\n\n* Drop database.");

    new DropDB(name).execute(ctx);
    ctx.close();
  }
}
