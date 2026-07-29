package org.basex.gui.view.editor;

/**
 * Editor action.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public enum Action {
  /** Check for changes; do nothing if input has not changed. */
  CHECK,
  /** Enforce parsing of input. */
  PARSE,
  /** Enforce execution of input. */
  EXECUTE,
  /** Enforce testing of input. */
  TEST
}
