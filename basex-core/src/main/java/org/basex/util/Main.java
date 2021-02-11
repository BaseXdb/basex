package org.basex.util;

import java.io.*;
import java.util.*;

import org.basex.io.*;

/**
 * This is an interface for classes with main methods and command-line arguments.
 *
 * @author BaseX Team 2005-21, BSD License
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
  final String[] args() {
    return args;
  }

  /**
   * Generates a stop file for the specified class and port.
   * @param clazz class name
   * @param port port
   * @return stop file
   */
  protected static IOFile stopFile(final Class<?> clazz, final int port) {
    final String file = Util.className(clazz).toLowerCase(Locale.ENGLISH) + "stop-" + port + ".tmp";
    return new IOFile(new IOFile(Prop.TEMPDIR, Prop.PROJECT), file);
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
