package org.basex.modules;

/**
 * This is an example for a module that can be added to the BaseX repository.
 * @author BaseX Team 2005-14, BSD License
 */
public class Hello {
  /**
   * Say hello to someone.
   * @param world the one to be greeted
   * @return welcome string
   */
  public String hello(final String world) {
    return "Hello " + world;
  }
}
