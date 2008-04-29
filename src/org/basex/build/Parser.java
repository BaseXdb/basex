package org.basex.build;

import java.io.IOException;
import org.basex.io.IO;

/**
 * This is class defines methods for parser implementations.
 * A parser is applied to create database instances.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class Parser {
  /** Input file. */
  public IO file;

  /**
   * Constructor.
   * @param f file reference.
   */
  public Parser(final IO f) {
    file = f;
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
