package org.basex;

/**
 * This class contains a main method for starting the GUI from the API package.
 * This way, all API dependencies will be available in the visual frontend.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
final class StartGUI {
  /** Private constructor. */
  private StartGUI() { }

  /**
   * Main method.
   * @param args command-line arguments
   * @throws Exception exception
   */
  public static void main(final String... args) throws Exception {
    new BaseXGUI(args);
  }
}
