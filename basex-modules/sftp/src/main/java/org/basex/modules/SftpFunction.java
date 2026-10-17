package org.basex.modules;

/**
 * Function that raises query exceptions.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
@FunctionalInterface
public interface SftpFunction {
  /**
   * Runs an operation.
   * @throws Exception exception
   */
  void apply() throws Exception;
}
