package org.basex.core.parse;

import java.util.*;

import org.basex.core.*;
import org.basex.query.*;

/**
 * This is an interface for parsing database commands.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
abstract class CmdParser {
  /** Input string. */
  final String string;
  /** Context. */
  final Context ctx;

  /** Suggest possible completions. */
  boolean suggest;
  /** Single command flag. */
  boolean single;
  /** Password reader. */
  PasswordReader pwReader;

  /**
   * Constructor.
   * @param input input
   * @param context context
   */
  CmdParser(final String input, final Context context) {
    ctx = context;
    string = input;
  }

  /**
   * Attaches a password reader.
   * @param pr password reader
   */
  final void pwReader(final PasswordReader pr) {
    pwReader = pr;
  }

  /**
   * Parses the input and returns a command list.
   * @param cmds container for created commands
   * @param sngl single command flag
   * @param sggst suggest flag
   * @throws QueryException query exception
   */
  final void parse(final ArrayList<Command> cmds, final boolean sngl, final boolean sggst)
      throws QueryException {

    single = sngl;
    suggest = sggst;
    parse(cmds);
  }

  /**
   * Parses the input and fills the command list.
   * @param cmds container for created commands
   * @throws QueryException query exception
   */
  protected abstract void parse(final ArrayList<Command> cmds) throws QueryException;
}
