package org.basex.util;

import java.io.*;

/**
 * This is an interface for classes with main methods and command-line arguments.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class Main {
  /** Command-line arguments. */
  private final String[] args;

  /**
   * Constructor.
   * @param args command-line arguments
   */
  protected Main(final String[] args) {
    this.args = args;
  }

  /**
   * Returns the command-line arguments.
   * @return arguments
   */
  String[] args() {
    return args;
  }

  /**
   * Returns a header string for command-line information.
   * @return header string
   */
  public abstract String header();

  /**
   * Returns a usage string for command-line information.
   * @return usage string
   */
  public abstract String usage();

  /**
   * Parses the command-line arguments, specified by the user.
   * @throws IOException I/O exception
   */
  protected abstract void parseArgs() throws IOException;
}
