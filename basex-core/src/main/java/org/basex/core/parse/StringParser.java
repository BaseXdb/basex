package org.basex.core.parse;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.cmd.Check;
import org.basex.core.cmd.List;
import org.basex.core.cmd.Set;
import org.basex.core.parse.Commands.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.basex.util.similarity.*;

/**
 * This is a parser for command strings, creating {@link Command} instances.
 * Several commands can be formulated in one string and separated by semicolons.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class StringParser extends CommandParser {
  /** Current parser. */
  private InputParser parser;

  /**
   * Constructor.
   * @param input input
   * @param context database context
   */
  StringParser(final String input, final Context context) {
    super(input, context);
  }

  @Override
  protected void parse(final ArrayList<Command> cmds) throws QueryException {
    final Scanner sc = new Scanner(input).useDelimiter(single ? "\0" : "\r\n?|\n");
    while(sc.hasNext()) {
      final String line = sc.next().trim();
      if(line.isEmpty() || Strings.startsWith(line, '#')) continue;
      parser = new InputParser(line);
      parser.file = uri;
      while(parser.more()) {
        final Cmd cmd = consume(Cmd.class, null);
        if(cmd != null) cmds.add(parse(cmd).baseURI(uri));
        if(parser.more() && !parser.consume(';')) throw help(null, cmd);
      }
    }
  }

  /**
   * Parses a command.
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
            return new CreateDB(name(cmd), remaining(null, true));
          case INDEX:
            return new CreateIndex(consume(CmdIndex.class, cmd));
          case USER:
            return new CreateUser(name(cmd), password());
        }
        break;
      case COPY:
        return new Copy(name(cmd), name(cmd));
      case ALTER:
        switch(consume(CmdAlter.class, cmd)) {
          case BACKUP:
            return new AlterBackup(name(cmd), name(cmd));
          case DATABASE: case DB:
            return new AlterDB(name(cmd), name(cmd));
          case PASSWORD:
            return new AlterPassword(name(cmd), password());
          case USER:
            return new AlterUser(name(cmd), name(cmd));
        }
        break;
      case OPEN:
        return new Open(name(cmd), string(null));
      case CHECK:
        return new Check(string(cmd));
      case ADD:
        final String aa = key(S_TO, null) ? string(cmd) : null;
        return new Add(aa, remaining(cmd, true));
      case STORE:
        final String sa = key(S_TO, null) ? string(cmd) : null;
        return new Store(sa, remaining(cmd, true));
      case RETRIEVE:
        return new Retrieve(string(cmd));
      case DELETE:
        return new Delete(string(cmd));
      case RENAME:
        return new Rename(string(cmd), string(cmd));
      case REPLACE:
        return new Replace(string(cmd), remaining(cmd, true));
      case INFO:
        switch(consume(CmdInfo.class, cmd)) {
          case NULL:
            return new Info();
          case DATABASE: case DB:
            return new InfoDB();
          case INDEX:
            return new InfoIndex(consume(CmdIndexInfo.class, null));
          case STORAGE:
            final String arg1 = number();
            final String arg2 = arg1 != null ? number() : null;
            return new InfoStorage(arg1, arg2);
        }
        break;
      case INSPECT:
        return new Inspect();
      case CLOSE:
        return new Close();
      case LIST:
        return new List(name(null), string(null));
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
        return new XQuery(remaining(cmd, false));
      case RUN:
        return new Run(string(cmd));
      case TEST:
        return new Test(string(cmd));
      case EXECUTE:
        return new Execute(remaining(cmd, true));
      case FIND:
        return new Find(remaining(cmd, true));
      case GET:
        return new Get(name(null));
      case SET:
        return new Set(name(cmd), remaining(null, true));
      case PASSWORD:
        return new Password(password());
      case HELP:
        return new Help(name(null));
      case EXIT:
      case QUIT:
        return new Exit();
      case FLUSH:
        return new Flush();
      case KILL:
        return new Kill(string(cmd));
      case RESTORE:
        return new Restore(name(cmd));
      case JOBS:
        switch(consume(CmdJobs.class, cmd)) {
          case LIST:
            return new JobsList();
          case STOP:
            return new JobsStop(name(cmd));
          case RESULT:
            return new JobsResult(name(cmd));
        }
        break;
      case SHOW:
        switch(consume(CmdShow.class, cmd)) {
          case SESSIONS:
            return new ShowSessions();
          case USERS:
            return new ShowUsers(key(ON, null) ? name(cmd) : null);
          case BACKUPS:
            return new ShowBackups();
        }
        break;
      case GRANT:
        final CmdPerm perm = consume(CmdPerm.class, cmd);
        if(perm == null) throw help(null, cmd);
        final String db = key(ON, null) ? glob(cmd) : null;
        key(S_TO, cmd);
        return new Grant(perm, glob(cmd), db);
      case REPO:
        switch(consume(CmdRepo.class, cmd)) {
          case INSTALL:
            return new RepoInstall(string(cmd), new InputInfo(parser));
          case DELETE:
            return new RepoDelete(string(cmd), new InputInfo(parser));
          case LIST:
            return new RepoList();
        }
        break;
    }
    throw Util.notExpected("Command specified, but not implemented yet");
  }

  /**
   * Parses and returns a string, delimited by a semicolon or a space.
   * The input can be wrapped with quotes.
   * @param cmd referring command; if specified, the result must not be empty
   * @return string or {@code null}
   * @throws QueryException query exception
   */
  private String string(final Cmd cmd) throws QueryException {
    final StringBuilder sb = new StringBuilder();
    consumeWS();
    boolean more = true, quoted = false;
    while(more && parser.more()) {
      final char ch = parser.curr();
      if(quoted) {
        if(ch == '"') more = false;
      } else if(ch <= ' ' || eoc()) {
        break;
      } else if(ch == '"' && sb.length() == 0) {
        quoted = true;
      }
      sb.append(ch);
      parser.consume();
    }
    return finish(sb.toString().replaceAll("^\"|\"$", ""), cmd);
  }

  /**
   * Parses and returns the remaining string.
   * @param cmd referring command; if specified, the result must not be empty
   * @param quotes strip leading and trailing quotes
   * @return remaining string or {@code null}
   * @throws QueryException query exception
   */
  private String remaining(final Cmd cmd, final boolean quotes) throws QueryException {
    consumeWS();
    final StringBuilder sb = new StringBuilder();
    while(parser.more()) sb.append(parser.consume());
    final String str = sb.toString();
    return finish(quotes ? str.replaceAll("^\"|\"$", "") : str, cmd);
  }

  /**
   * Parses and returns a command. A command is limited to letters.
   * @return command or {@code null}
   * @throws QueryException query exception
   */
  private String command() throws QueryException {
    consumeWS();
    final StringBuilder sb = new StringBuilder();
    while(!eoc() && !ws(parser.curr())) sb.append(parser.consume());
    return finish(sb, null);
  }

  /**
   * Parses and returns a name. A name may contain letters, numbers and some special
   * characters (see {@link Databases#DBCHARS}).
   * @param cmd referring command; if specified, the result must not be empty
   * @return name
   * @throws QueryException query exception
   */
  private String name(final Cmd cmd) throws QueryException {
    return name(cmd, false);
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
   * Parses and returns a glob expression, which extends {@link #name(Cmd)} function
   * with asterisks, question marks and commands.
   * @param cmd referring command; if specified, the result must not be empty
   * @return glob expression
   * @throws QueryException query exception
   */
  private String glob(final Cmd cmd) throws QueryException {
    return name(cmd, true);
  }

  /**
   * Parses and returns a name, or a glob string that extends a {@link #name(Cmd)}
   * with asterisks, question marks and commands.
   * @param cmd referring command; if specified, the result must not be empty
   * @param glob allow glob syntax
   * @return glob expression
   * @throws QueryException query exception
   */
  private String name(final Cmd cmd, final boolean glob) throws QueryException {
    consumeWS();
    final StringBuilder sb = new StringBuilder();
    char last = 0;
    while(true) {
      final char ch = parser.curr();
      if(!Databases.validChar(ch, sb.length() == 0) &&
          (!glob || ch != '*' && ch != '?' && ch != ',')) {
        return finish((eoc() || ws(ch)) && last != '.' ? sb : null, cmd);
      }
      sb.append(parser.consume());
      last = ch;
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
    final int p = parser.pos;
    final boolean ok = (parser.consume(key) || parser.consume(
        key.toLowerCase(Locale.ENGLISH))) && (parser.curr(0) || ws(parser.curr()));
    if(!ok) {
      parser.pos = p;
      if(cmd != null) throw help(null, cmd);
    }
    return ok;
  }

  /**
   * Parses and returns a string result.
   * @param string input string or {@code null} if invalid
   * @param cmd referring command; if specified, the result must not be empty
   * @return string result or {@code null}
   * @throws QueryException query exception
   */
  private String finish(final CharSequence string, final Cmd cmd) throws QueryException {
    if(string != null && string.length() != 0) return string.toString();
    if(cmd != null) throw help(null, cmd);
    return null;
  }

  /**
   * Parses and returns a number.
   * @return name
   * @throws QueryException query exception
   */
  private String number() throws QueryException {
    consumeWS();
    final StringBuilder sb = new StringBuilder();
    if(parser.curr() == '-') sb.append(parser.consume());
    while(digit(parser.curr())) sb.append(parser.consume());
    return finish(eoc() || ws(parser.curr()) ? sb : null, null);
  }

  /**
   * Consumes all whitespace characters from the beginning of the remaining
   * query.
   */
  private void consumeWS() {
    final int pl = parser.length;
    while(parser.pos < pl && parser.input.charAt(parser.pos) <= ' ') ++parser.pos;
    parser.mark = parser.pos - 1;
  }

  /**
   * Returns the found command or throws an exception.
   * @param complete possible completions
   * @param parent parent command
   * @param <E> token type
   * @return command, or {@code null} if no nothing can be consumed and if no parent was specified
   * @throws QueryException query exception
   */
  private <E extends Enum<E>> E consume(final Class<E> complete, final Cmd parent)
      throws QueryException {

    final String token = command();
    if(!suggest || token == null || !token.isEmpty()) {
      try {
        // return command reference; allow empty strings as input ("NULL")
        return Enum.valueOf(complete, token == null ? "NULL" : token.toUpperCase(Locale.ENGLISH));
      } catch(final IllegalArgumentException ignore) { }
    }

    final Enum<?>[] alt = startWith(complete, token);
    // handle empty input
    if(token == null) {
      if(parent != null) throw help(alt, parent);
      if(suggest) throw error(alt, EXPECTING_CMD);
      return null;
    }

    // output error for similar commands
    final byte[] name = uc(token(token));
    final Object similar = Levenshtein.similar(name, startWith(complete, null),
        o -> ((Enum<?>) o).name());
    if(similar != null) throw error(alt, UNKNOWN_SIMILAR_X_X, name, similar);

    // show unknown command error or available command extensions
    throw parent == null ? error(alt, UNKNOWN_TRY_X, token) : help(alt, parent);
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
  private static <T extends Enum<T>> Enum<?>[] startWith(final Class<T> en, final String prefix) {
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
   * @param complete input completions
   * @param msg message
   * @param ext extension
   * @return query exception
   */
  private QueryException error(final Enum<?>[] complete, final String msg, final Object... ext) {
    return new QueryException(parser.info(), QNm.EMPTY, msg, ext).suggest(parser, list(complete));
  }

  /**
   * Converts the specified commands into a string list.
   * @param complete input completions
   * @return string list
   */
  private static StringList list(final Enum<?>[] complete) {
    final StringList list = new StringList();
    if(complete != null) {
      for(final Enum<?> c : complete) list.add(c.name().toLowerCase(Locale.ENGLISH));
    }
    return list;
  }
}

