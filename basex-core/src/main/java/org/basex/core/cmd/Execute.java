package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.util.*;
import java.util.stream.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.parse.*;
import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * Evaluates the 'execute' command and runs a command script.
 * This command can be used to run multiple commands as a single transaction.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class Execute extends Command {
  /** Commands to execute. */
  final java.util.List<Command> commands;
  /** Error message. */
  String error;

  /**
   * Constructor for string input.
   * @param input user input
   */
  public Execute(final String input) {
    super(Perm.NONE, false, input);
    commands = new ArrayList<>();
  }

  /**
   * Constructor for command input.
   * @param commands commands to execute
   */
  public Execute(final Command... commands) {
    super(Perm.NONE, false);
    this.commands = Arrays.asList(commands);
  }

  @Override
  public final boolean newData(final Context ctx) {
    return Close.close(ctx);
  }

  @Override
  public final void addLocks() {
    final Locks locks = jc().locks;
    for(final Command cmd : commands) {
      final Locks cmdLocks = cmd.jc().locks;
      (cmd.updating ? cmdLocks.writes : cmdLocks.reads).addGlobal();
    }
    (updating ? locks.writes : locks.reads).addGlobal();
  }

  @Override
  protected boolean run() {
    if(!init(context)) return error(error);

    final StringBuilder sb = new StringBuilder();
    for(final Command cmd : commands) {
      if(cmd.openDB && context.data() == null) return error(NO_DB_OPENED);
      try {
        final boolean ok = pushJob(cmd).run(context, out);
        sb.append(cmd.info());
        if(!ok) {
          exception = cmd.exception();
          return error(sb.toString());
        }
      } finally {
        popJob();
      }
    }
    return info(sb.toString().replaceAll("\r?\n?$", ""));
  }

  @Override
  public final boolean updating(final Context ctx) {
    if(!init(ctx)) return false;
    for(final Command cmd : commands) updating |= cmd.updating(ctx);
    return updating;
  }

  /**
   * Initializes command execution.
   * @param ctx database context
   * @return success flag
   */
  boolean init(final Context ctx) {
    return args.length == 0 || init(args[0], uri, ctx);
  }

  /**
   * Initializes the specified input.
   * @param input command input
   * @param base base URI
   * @param ctx database context
   * @return success flag
   */
  final boolean init(final String input, final String base, final Context ctx) {
    if(commands.isEmpty() && error == null) {
      try {
        Collections.addAll(commands, CommandParser.get(input, ctx).baseURI(base).parse());
      } catch(final QueryException ex) {
        error = Util.message(ex);
        return false;
      }
    }
    return error == null;
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init();
    if(args.length == 0) {
      cb.arg(null, commands.stream().map(Command::toString).collect(Collectors.joining(";")));
    } else {
      cb.arg(0);
    }
  }
}
