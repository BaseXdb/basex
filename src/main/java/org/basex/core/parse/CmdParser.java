package org.basex.core.parse;

import java.util.*;

import org.basex.core.*;
import org.basex.query.*;

/**
 * This is an interface for parsing database commands.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
abstract class CmdParser {
  /** Suggest possible completions. */
  protected boolean suggest;
  /** Single command flag. */
  protected boolean single;
  /** Password reader. */
  protected PasswordReader passwords;

  /**
   * Attaches a password reader.
   * @param pr password reader
   */
  final void password(final PasswordReader pr) {
    passwords = pr;
  }

  /**
   * Parses the input and returns a command list.
   * @param list command list
   * @param sngl single command flag
   * @param sggst suggest flag
   * @throws QueryException query exception
   */
  final void parse(final ArrayList<Command> list, final boolean sngl, final boolean sggst)
      throws QueryException {

    single = sngl;
    suggest = sggst;
    parse(list);
  }

  /**
   * Parses the input and fills the command list.
   * @param cmds command list
   * @throws QueryException query exception
   */
  abstract void parse(final ArrayList<Command> cmds) throws QueryException;
}
