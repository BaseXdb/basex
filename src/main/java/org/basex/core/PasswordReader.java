package org.basex.core;

/**
   * Reads a password from a specified source (eg. command line or GUI).
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class PasswordReader {
  /**
   * Parses and returns a password.
   * In command line and server mode, read from stdin, on GUI command line
   * prompt using a password box.
   * @return password or empty string
   */
  public abstract String password();
}
