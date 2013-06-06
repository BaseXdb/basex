package org.basex.core.parse;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.cmd.List;
import org.basex.core.cmd.Set;
import org.basex.core.parse.Commands.Cmd;
import org.basex.core.parse.Commands.CmdAlter;
import org.basex.core.parse.Commands.CmdCreate;
import org.basex.core.parse.Commands.CmdDrop;
import org.basex.core.parse.Commands.CmdIndex;
import org.basex.core.parse.Commands.CmdIndexInfo;
import org.basex.core.parse.Commands.CmdInfo;
import org.basex.core.parse.Commands.CmdOptimize;
import org.basex.core.parse.Commands.CmdPerm;
import org.basex.core.parse.Commands.CmdRepo;
import org.basex.core.parse.Commands.CmdShow;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This is a parser for command strings, creating {@link Command} instances.
 * Several commands can be formulated in one string and separated by semicolons.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class StringParser extends CmdParser {
  /** Input lines. */
  private final StringList lines = new StringList();
  /** Context. */
  private final Context ctx;

  /** Current parser. */
  private InputParser parser;

  /**
   * Constructor.
   * @param input input
   * @param context database context
   */
  StringParser(final String input, final Context context) {
    ctx = context;
    final Scanner sc = new Scanner(input).useDelimiter(single ? "\0" : "\r\n?|\n");
    while(sc.hasNext()) {
      final String line = sc.next().trim();
      if(!line.isEmpty() && !line.startsWith("#")) lines.add(line);
    }
  }

  @Override
  void parse(final ArrayList<Command> cmds) throws QueryException {
    for(final String line : lines) {
      parser = new InputParser(line);
      while(parser.more()) {
        final Cmd cmd = consume(Cmd.class, null);
        if(cmd != null) cmds.add(parse(cmd));
        if(parser.more() && !parser.consume(';')) throw help(null, cmd);
      }
    }
  }

  /**
   * Parses a single command.
   * @param cmd command definition
   * @return resulting command
   * @throws QueryException query exception
   */
  private Command parse(final Cmd cmd) throws QueryException {
    switch(cmd) {
      case CREATE:
        switch(consume(CmdCreate.class, cmd)) {
          case BACKUP:
            return new CreateBackup(glob(cmd));
          case DATABASE: case DB:
            return new CreateDB(name(cmd), single ? remaining(null) : string(null));
          case INDEX:
            return new CreateIndex(consume(CmdIndex.class, cmd));
          case USER:
            return new CreateUser(name(cmd), password());
          case EVENT:
            return new CreateEvent(name(cmd));
        }
        break;
      case COPY:
        return new Copy(name(cmd), name(cmd));
      case ALTER:
        switch(consume(CmdAlter.class, cmd)) {
          case DATABASE: case DB:
            return new AlterDB(name(cmd), name(cmd));
          case USER:
            return new AlterUser(name(cmd), password());
        }
        break;
      case OPEN:
        return new Open(name(cmd));
      case CHECK:
        return new Check(string(cmd));
      case ADD:
        String arg = key(C_TO, null) ? string(cmd) : null;
        return new Add(arg, single ? remaining(cmd) : string(cmd));
      case STORE:
        arg = key(C_TO, null) ? string(cmd) : null;
        return new Store(arg, single ? remaining(cmd) : string(cmd));
      case RETRIEVE:
        return new Retrieve(string(cmd));
      case DELETE:
        return new Delete(string(cmd));
      case RENAME:
        return new Rename(string(cmd), string(cmd));
      case REPLACE:
        return new Replace(string(cmd), single ? remaining(cmd) : string(cmd));
      case INFO:
        switch(consume(CmdInfo.class, cmd)) {
          case NULL:
            return new Info();
          case DATABASE: case DB:
            return new InfoDB();
          case INDEX:
            return new InfoIndex(consume(CmdIndexInfo.class, null));
          case STORAGE:
            String arg1 = number(null);
            final String arg2 = arg1 != null ? number(null) : null;
            if(arg1 == null) arg1 = xquery(null);
            return new InfoStorage(arg1, arg2);
        }
        break;
      case INSPECT:
        return new Inspect();
      case CLOSE:
        return new Close();
      case LIST:
        return new List(string(null), string(null));
      case DROP:
        switch(consume(CmdDrop.class, cmd)) {
          case DATABASE: case DB:
            return new DropDB(glob(cmd));
          case INDEX:
            return new DropIndex(consume(CmdIndex.class, cmd));
          case USER:
            return new DropUser(glob(cmd), key(ON, null) ? glob(cmd) : null);
          case BACKUP:
            return new DropBackup(glob(cmd));
          case EVENT:
            return new DropEvent(name(cmd));
        }
        break;
      case OPTIMIZE:
        switch(consume(CmdOptimize.class, cmd)) {
          case NULL:
            return new Optimize();
          case ALL:
            return new OptimizeAll();
        }
        break;
      case EXPORT:
        return new Export(string(cmd));
      case XQUERY:
        return new XQuery(xquery(cmd));
      case RUN:
        return new Run(string(cmd));
      case EXECUTE:
        return new Execute(string(cmd));
      case FIND:
        return new Find(string(cmd));
      case CS:
        return new Cs(xquery(cmd));
      case GET:
        return new Get(name(null));
      case SET:
        return new Set(name(cmd), string(null));
      case PASSWORD:
        return new Password(password());
      case HELP:
        return new Help(name(null));
      case EXIT:
        return new Exit();
      case FLUSH:
        return new Flush();
      case KILL:
        return new Kill(string(cmd));
      case RESTORE:
        return new Restore(name(cmd));
      case SHOW:
        switch(consume(CmdShow.class, cmd)) {
          case SESSIONS:
            return new ShowSessions();
          case USERS:
            return new ShowUsers(key(ON, null) ? name(cmd) : null);
          case BACKUPS:
            return new ShowBackups();
          case EVENTS:
            return new ShowEvents();
          default:
        }
        break;
      case GRANT:
        final CmdPerm perm = consume(CmdPerm.class, cmd);
        if(perm == null) throw help(null, cmd);
        final String db = key(ON, null) ? glob(cmd) : null;
        key(C_TO, cmd);
        return new Grant(perm, glob(cmd), db);
      case REPO:
        switch(consume(CmdRepo.class, cmd)) {
          case INSTALL:
            return new RepoInstall(string(cmd), new InputInfo(parser));
          case DELETE:
            return new RepoDelete(string(cmd), new InputInfo(parser));
          case LIST:
            return new RepoList();
          default:
        }
        break;
      default:
    }
    throw Util.notexpected("command specified, but not implemented yet");
  }

  /**
   * Parses and returns a string, delimited by a space or semicolon.
   * Quotes can be used to include spaces.
   * @param cmd referring command; if specified, the result must not be empty
   * @return string
   * @throws QueryException query exception
   */
  private String string(final Cmd cmd) throws QueryException {
    final StringBuilder sb = new StringBuilder();
    consumeWS();
    boolean q = false;
    while(parser.more()) {
      final char c = parser.curr();
      if(!q && (c <= ' ' || eoc())) break;
      if(c == '"') q ^= true;
      else sb.append(c);
      parser.consume();
    }
    return finish(sb, cmd);
  }

  /**
   * Parses and returns the remaining string. Quotes at the beginning and end
   * of the argument will be stripped.
   * @param cmd referring command; if specified, the result must not be empty
   * @return remaining string
   * @throws QueryException query exception
   */
  private String remaining(final Cmd cmd) throws QueryException {
    final StringBuilder sb = new StringBuilder();
    consumeWS();
    while(parser.more()) sb.append(parser.consume());
    String arg = finish(sb, cmd);
    if(arg != null) {
      // chop quotes; substrings are faster than replaces...
      if(arg.startsWith("\"")) arg = arg.substring(1);
      if(arg.endsWith("\"")) arg = arg.substring(0, arg.length() - 1);
    }
    return arg;
  }

  /**
   * Parses and returns an xquery expression.
   * @param cmd referring command; if specified, the result must not be empty
   * @return path
   * @throws QueryException query exception
   */
  private String xquery(final Cmd cmd) throws QueryException {
    consumeWS();
    final StringBuilder sb = new StringBuilder();
    if(!eoc()) {
      final QueryContext qc = new QueryContext(ctx);
      try {
        final QueryParser p = new QueryParser(parser.input, null, qc);
        p.ip = parser.ip;
        p.parseMain();
        sb.append(parser.input.substring(parser.ip, p.ip));
        parser.ip = p.ip;
      } finally {
        qc.close();
      }
    }
    return finish(sb, cmd);
  }

  /**
   * Parses and returns a command. A command is limited to letters.
   * @param cmd referring command; if specified, the result must not be empty
   * @return name
   * @throws QueryException query exception
   */
  private String command(final Cmd cmd) throws QueryException {
    consumeWS();
    final StringBuilder sb = new StringBuilder();
    while(!eoc() && !ws(parser.curr())) {
      sb.append(parser.consume());
    }
    return finish(sb, cmd);
  }

  /**
   * Parses and returns a name. A name is limited to letters, digits,
   * underscores, dashes, and periods: {@code [A-Za-z0-9_-]+}.
   * @param cmd referring command; if specified, the result must not be empty
   * @return name
   * @throws QueryException query exception
   */
  private String name(final Cmd cmd) throws QueryException {
    consumeWS();
    final StringBuilder sb = new StringBuilder();
    while(Databases.validChar(parser.curr())) sb.append(parser.consume());
    return finish(eoc() || ws(parser.curr()) ? sb : null, cmd);
  }

  /**
   * Parses and returns a password string.
   * @return password string
   * @throws QueryException query exception
   */
  private String password() throws QueryException {
    final String pw = string(null);
    return pw != null ? pw : pwReader == null ? "" : pwReader.password();
  }

  /**
   * Parses and returns a glob expression, which extends the {@link #name}
   * with asterisks, question marks and commands.
   * @param cmd referring command; if specified, the result must not be empty
   * @return glob expression
   * @throws QueryException query exception
   */
  private String glob(final Cmd cmd) throws QueryException {
    consumeWS();
    final StringBuilder sb = new StringBuilder();
    while(true) {
      final char ch = parser.curr();
      if(!Databases.validChar(ch) && ch != '*' && ch != '?' && ch != ',') {
        return finish(eoc() || ws(ch) ? sb : null, cmd);
      }
      sb.append(parser.consume());
    }
  }

  /**
   * Parses and returns the specified keyword.
   * @param key token to be parsed
   * @param cmd referring command; if specified, the keyword is mandatory
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean key(final String key, final Cmd cmd) throws QueryException {
    consumeWS();
    final int p = parser.ip;
    final boolean ok = (parser.consume(key) || parser.consume(
        key.toLowerCase(Locale.ENGLISH))) && (parser.curr(0) || ws(parser.curr()));
    if(!ok) {
      parser.ip = p;
      if(cmd != null) throw help(null, cmd);
    }
    return ok;
  }

  /**
   * Parses and returns a string result.
   * @param s input string, or {@code null} if invalid
   * @param cmd referring command; if specified, the result must not be empty
   * @return string result, or {@code null}
   * @throws QueryException query exception
   */
  private String finish(final StringBuilder s, final Cmd cmd) throws QueryException {
    if(s != null && s.length() != 0) return s.toString();
    if(cmd != null) throw help(null, cmd);
    return null;
  }

  /**
   * Parses and returns a number.
   * @param cmd referring command; if specified, the result must not be empty
   * @return name
   * @throws QueryException query exception
   */
  private String number(final Cmd cmd) throws QueryException {
    consumeWS();
    final StringBuilder sb = new StringBuilder();
    if(parser.curr() == '-') sb.append(parser.consume());
    while(digit(parser.curr())) sb.append(parser.consume());
    return finish(eoc() || ws(parser.curr()) ? sb : null, cmd);
  }

  /**
   * Consumes all whitespace characters from the beginning of the remaining
   * query.
   */
  private void consumeWS() {
    while(parser.ip < parser.il && parser.input.charAt(parser.ip) <= ' ') ++parser.ip;
    parser.im = parser.ip - 1;
  }

  /**
   * Returns the found command or throws an error.
   * @param cmp possible completions
   * @param par parent command
   * @param <E> token type
   * @return index
   * @throws QueryException query exception
   */
  private <E extends Enum<E>> E consume(final Class<E> cmp, final Cmd par)
      throws QueryException {

    final String token = command(null);
    if(!suggest || token == null || !token.isEmpty()) {
      try {
        // return command reference; allow empty strings as input ("NULL")
        final String t = token == null ? "NULL" : token.toUpperCase(Locale.ENGLISH);
        return Enum.valueOf(cmp, t);
      } catch(final IllegalArgumentException ignore) { }
    }

    final Enum<?>[] alt = startWith(cmp, token);
    // handle empty input
    if(token == null) {
      if(par != null) throw help(alt, par);
      if(suggest) throw error(alt, EXPECTING_CMD);
      return null;
    }

    // output error for similar commands
    final byte[] name = uc(token(token));
    final Levenshtein ls = new Levenshtein();
    for(final Enum<?> s : startWith(cmp, null)) {
      final byte[] sm = uc(token(s.name()));
      if(ls.similar(name, sm) && Cmd.class.isInstance(s)) {
        throw error(alt, UNKNOWN_SIMILAR_X, name, sm);
      }
    }

    // show unknown command error or available command extensions
    throw par == null ? error(alt, UNKNOWN_TRY_X, token) : help(alt, par);
  }

  /**
   * Returns help output as query exception instance.
   * Prints some command info.
   * @param alt input alternatives
   * @param cmd input completions
   * @return QueryException query exception
   */
  private QueryException help(final Enum<?>[] alt, final Cmd cmd) {
    return error(alt, SYNTAX_X, cmd.help(true));
  }

  /**
   * Returns all commands that start with the specified user input.
   * @param <T> token type
   * @param en available commands
   * @param prefix user input
   * @return completions
   */
  private static <T extends Enum<T>> Enum<?>[] startWith(final Class<T> en,
      final String prefix) {
    Enum<?>[] list = new Enum<?>[0];
    final String t = prefix == null ? "" : prefix.toUpperCase(Locale.ENGLISH);
    for(final Enum<?> e : en.getEnumConstants()) {
      if(e.name().startsWith(t)) {
        final int s = list.length;
        list = Array.copy(list, new Enum<?>[s + 1]);
        list[s] = e;
      }
    }
    return list;
  }

  /**
   * Checks if the end of a command has been reached.
   * @return true if command has ended
   */
  private boolean eoc() {
    return !parser.more() || parser.curr() == ';';
  }

  /**
   * Returns a query exception instance.
   * @param comp input completions
   * @param m message
   * @param e extension
   * @return query exception
   */
  private QueryException error(final Enum<?>[] comp, final String m, final Object... e) {
    return new QueryException(parser.info(), new QNm(), m, e).suggest(parser, list(comp));
  }

  /**
   * Converts the specified commands into a string list.
   * @param comp input completions
   * @return string list
   */
  private static StringList list(final Enum<?>[] comp) {
    final StringList list = new StringList();
    if(comp != null) {
      for(final Enum<?> c : comp) list.add(c.name().toLowerCase(Locale.ENGLISH));
    }
    return list;
  }
}

