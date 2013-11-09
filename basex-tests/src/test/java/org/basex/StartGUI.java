package org.basex;
/**
 * GUI start class, which includes all module libraries for convenient testing.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class StartGUI {
  /** Hidden constructor. */
  private StartGUI() { }

  /**
   * Main method.
   * @param args command-line arguments
   * @throws Exception exception
   */
  public static void main(final String... args) throws Exception {
    new org.basex.BaseXGUI(args);
  }
}
