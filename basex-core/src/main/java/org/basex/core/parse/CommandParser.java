package org.basex.core.parse;

import java.util.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * This is a parser for command strings, creating {@link Command} instances.
 * Several commands can be formulated in one string and separated by semicolons.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class CommandParser {
  /** Context. */
  private final CmdParser parser;

  /**
   * Constructor.
   * @param input input
   * @param ctx context
   */
  public CommandParser(final String input, final Context ctx) {
    parser = input.startsWith("<") ? new XMLParser(input, ctx) : new StringParser(input, ctx);
  }

  /**
   * Attaches a password reader.
   * @param pr password reader
   * @return self reference
   */
  public CommandParser pwReader(final PasswordReader pr) {
    parser.pwReader(pr);
    return this;
  }

  /**
   * Parses the input as single command and returns the resulting command.
   * @return command
   * @throws QueryException query exception
   */
  public Command parseSingle() throws QueryException {
    return parse(true, false)[0];
  }

  /**
   * Parses the input and returns a command list.
   * @return commands
   * @throws QueryException query exception
   */
  public Command[] parse() throws QueryException {
    return parse(false, false);
  }

  /**
   * Parses the input and creates command completions on the way.
   * @return commands
   * @throws QueryException query exception
   */
  public Command[] suggest() throws QueryException {
    return parse(false, true);
  }

  /**
   * Parses the input and returns a command list.
   * @param single input must only contain a single command
   * @param suggest suggest flag
   * @return commands
   * @throws QueryException query exception
   */
  private Command[] parse(final boolean single, final boolean suggest) throws QueryException {
    final ArrayList<Command> cmds = new ArrayList<>();
    parser.parse(cmds, single, suggest);
    if(!single || cmds.size() == 1) return cmds.toArray(new Command[cmds.size()]);
    throw new QueryException(null, new QNm(), Text.SINGLE_CMD);
  }
}
