package org.basex.core.parse;

import java.util.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * This is a parser for command strings, creating {@link Command} instances.
 * Several commands can be formulated in one string and separated by semicolons.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class CommandParser {
  /** Input string. */
  final String input;
  /** Context. */
  final Context ctx;

  /** Password reader. */
  PasswordReader pwReader;
  /** Base URI. */
  String uri = "";
  /** Parse single command. */
  boolean single;
  /** XQuery suggestions. */
  boolean suggest;

  /**
   * Constructor.
   * @param input input string
   * @param ctx database context
   */
  CommandParser(final String input, final Context ctx) {
    this.ctx = ctx;
    this.input = input;
  }

  /**
   * Constructor.
   * @param input input
   * @param ctx context
   * @return command parser
   */
  public static CommandParser get(final String input, final Context ctx) {
    return Strings.startsWith(input, '<') ? new XMLParser(input, ctx) :
      new StringParser(input, ctx);
  }

  /**
   * Attaches a password reader.
   * @param pr password reader
   * @return self reference
   */
  public final CommandParser pwReader(final PasswordReader pr) {
    pwReader = pr;
    return this;
  }

  /**
   * Attaches a base URI.
   * @param base base URI
   * @return self reference
   */
  public final CommandParser baseURI(final String base) {
    uri = base;
    return this;
  }

  /**
   * XQuery suggestions.
   * @return self reference
   */
  public final CommandParser suggest() {
    suggest = true;
    return this;
  }

  /**
   * Restricts parsing to a single command.
   * @return self reference
   * @throws QueryException query exception
   */
  public final Command parseSingle() throws QueryException {
    single = true;
    return parse()[0];
  }

  /**
   * Parses the input and returns a command list.
   * @return commands
   * @throws QueryException query exception
   */
  public final Command[] parse() throws QueryException {
    final ArrayList<Command> cmds = new ArrayList<>();
    parse(cmds);
    if(!single || cmds.size() == 1) return cmds.toArray(new Command[0]);
    throw new QueryException(null, QNm.EMPTY, Text.SINGLE_CMD);
  }

  /**
   * Parses the input and fills the command list.
   * @param cmds container for created commands
   * @throws QueryException query exception
   */
  protected abstract void parse(ArrayList<Command> cmds) throws QueryException;
}
