package org.basex.build;

import java.io.IOException;

/**
 * This is class defines methods for parser implementations.
 * A parser is applied to create database instances.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class Parser {
  /** Input file. */
  public String file;

  /**
   * Constructor.
   * @param fn input filename.
   */
  public Parser(final String fn) {
    file = fn;
  }

  /**
   * Parses all nodes and sends events to the specified builder.
   * @param build event listener.
   * @throws IOException I/O exception
   */
  public abstract void parse(Builder build) throws IOException;

  /**
   * Returns a compact description of the current progress.
   * @return progress information
   */
  public abstract String head();

  /**
   * Returns detailed progress information.
   * @return position info
   */
  public abstract String det();

  /**
   * Returns a value from 0 to 1, representing the current progress.
   * @return progress information
   */
  public abstract double percent();
}
