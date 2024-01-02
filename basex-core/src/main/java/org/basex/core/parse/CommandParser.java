package org.basex.core.parse;

import java.util.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This is a parser for command strings, creating {@link Command} instances.
 * Several commands can be formulated in one string and separated by semicolons.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public abstract class CommandParser {
  /** Input string. */
  final String input;
  /** Context. */
  final Context ctx;

  /** Password reader. */
  PasswordReader pwReader;
  /** Possible completions. */
  Enum<?>[] completions;
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
   * Command suggestions.
   * @return list of suggestions, followed by valid input string
   */
  public final StringList suggest() {
    suggest = true;
    final StringList list = new StringList();
    String valid = "";
    try {
      parse();
    } catch(final QueryException ex) {
      if(completions != null) {
        for(final Enum<?> cmp : completions) list.add(cmp.name().toLowerCase(Locale.ENGLISH));
      }
      final int marked = ex.markedColumn() + 1;
      if(marked <= input.length()) valid = input.substring(0, marked);
    }
    return list.add(valid);
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
    if(!single || cmds.size() == 1) return cmds.toArray(Command[]::new);
    throw new QueryException(null, QNm.EMPTY, Text.SINGLE_CMD);
  }

  /**
   * Parses the input and fills the command list.
   * @param cmds container for created commands
   * @throws QueryException query exception
   */
  protected abstract void parse(ArrayList<Command> cmds) throws QueryException;
}
