package org.basex.core;

import static org.basex.core.Command.*;
import static org.basex.core.Prop.NL;
import static org.basex.Text.*;
import org.basex.BaseX;
import org.basex.Text;
import org.basex.core.proc.Cd;
import org.basex.core.proc.Check;
import org.basex.core.proc.Close;
import org.basex.core.proc.Copy;
import org.basex.core.proc.Create;
import org.basex.core.proc.CreateFS;
import org.basex.core.proc.CreateIndex;
import org.basex.core.proc.CreateXML;
import org.basex.core.proc.Delete;
import org.basex.core.proc.Drop;
import org.basex.core.proc.DropIndex;
import org.basex.core.proc.Export;
import org.basex.core.proc.Find;
import org.basex.core.proc.Fs;
import org.basex.core.proc.Help;
import org.basex.core.proc.Info;
import org.basex.core.proc.Insert;
import org.basex.core.proc.Link;
import org.basex.core.proc.List;
import org.basex.core.proc.Open;
import org.basex.core.proc.Optimize;
import org.basex.core.proc.Ping;
import org.basex.core.proc.Proc;
import org.basex.core.proc.Prompt;
import org.basex.core.proc.Set;
import org.basex.core.proc.Update;
import org.basex.core.proc.XMark;
import org.basex.core.proc.XPath;
import org.basex.core.proc.XPathMV;
import org.basex.core.proc.XQEnv;
import org.basex.core.proc.XQuery;
import org.basex.util.Levenshtein;
import org.basex.util.StringList;
import org.basex.util.Token;

/**
 * This enumeration contains definitions of all available internal commands.
 * The {@link #find} method maps a command string to the respective command.
 * Adding a command should be done here; a how-to is included in the source.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Stefan Klinger
 * @author Christian Gruen
 */
public enum Commands {
  /*
   * HOWTO add a command:
   *
   * 1. Decide which string the user should type to run the command, and
   *    add an enum constant.
   *    Example: XQUERY
   *
   * 2. Create a new class for the command and add its class reference.
   *    Example: XQuery.class
   *
   * 3. Set some properties that are specific to the command:
   *    STANDARD  The command has no specific properties or prerequisites.
   *    LOCAL     The command is only evaluated locally; it won't be sent
   *              to the server instance.
   *    SERVER    The command is a pure server command.
   *    DATAREF   The command needs a data instance for processing.
   *    PRINTING  The command creates textual output.
   *    UPDATING  The command modifies the data.
   *
   * 4. Define the minimum and maximum number of allowed arguments.
   *    Example: 1 1
   *
   * 5. Decide if the command should be visible to everyone.
   *    Example: true
   *
   * 6. Describe the arguments of the command in the {@link Text} class.
   *    Example: XQUERY0 = "[query]";
   *
   * 6. Add a one-line description in the  {@link Text} class.
   *    Example: XQUERY1 = "Evaluate XQuery [query].";
   *
   * 7. Add an exhaustive description in the  {@link Text} class.
   *    Example: XQUERY2 = "Evaluate the specified XQuery and show its result.";
   *
   * The DUMMY constants serve as separators; they are evaluated when the
   * help is shown.
   */

  // DATABASE COMMANDS ========================================================
  
  /** Database separator. */
  DUMMYDATABASE(),

  /** Create main-memory mapping for specified XML document. */
  CREATE(Create.class, STANDARD, 1, 3, false),
  /** Convenience alias for creating a filesystem database. */
  CREATEFS(CreateFS.class, STANDARD, 2, 2, true),
  /** Convenience alias for creating an index. */
  CREATEINDEX(CreateIndex.class, STANDARD, 1, 1, true),
  /** Convenience alias for creating an xml database. */
  CREATEXML(CreateXML.class, STANDARD, 1, 1, true),
  /** Open a database. */
  OPEN(Open.class, STANDARD, 1, 1, false),
  /** Show some information on the XML and memory structure. */
  INFO(Info.class, PRINTING, 0, -1, false),
  /** Make sure the specified document is in memory. */
  CHECK(Check.class, STANDARD, 1, 1, true),
  /** Close database. */
  CLOSE(Close.class, STANDARD, 0, 0, false),
  /** List available databases. */
  LIST(List.class, PRINTING, 0, 0, false),
  /** Deletes database. */
  DROP(Drop.class, STANDARD, 1, 2, false),
  /** Convenience alias for dropping an index. */
  DROPINDEX(DropIndex.class, STANDARD, 1, 1, true),
  /** Open a database. */
  OPTIMIZE(Optimize.class, DATAREF | UPDATING, 0, 0, true),
  /** Export XML file. */
  EXPORT(Export.class, DATAREF, 0, 1, false),

  // QUERY COMMANDS ===========================================================
      
  /** Query separator. */
  DUMMYQUERY(),

  /** Evaluate XPath query and show result. */
  XPATH(XPath.class, DATAREF | PRINTING, 0, -1, false),
  /** Evaluate a MedioVis query. */
  XPATHMV(XPathMV.class, DATAREF | PRINTING, 3, 3, true),
  /** Evaluate XQuery and show result. */
  XQUERY(XQuery.class, PRINTING, 0, -1, false),
  /** Launch a simplified query. */
  FIND(Find.class, DATAREF | PRINTING, 0, -1, false),
  /** Evaluate XQuery and show result. */
  XQENV(XQEnv.class, PRINTING, 0, -1, true),
  /** launches XMark query. */
  XMARK(XMark.class, DATAREF | PRINTING, 1, 1, true),
  /** Switch context set. */
  CD(Cd.class, DATAREF, 0, -1, false),
  /** Evaluate XPath query, show result and create links. */
  LINK(Link.class, PRINTING, 0, -1, true),
  /** Filesystem command. */
  FS(Fs.class, DATAREF | PRINTING, 1, 30, true),

  // UPDATE COMMANDS ==========================================================
          
  /** Update separator. */
  DUMMYUPDATE(),
      
  /** Copy nodes. */
  COPY(Copy.class, DATAREF | UPDATING, 1, 3, false),
  /** Delete nodes. */
  DELETE(Delete.class, DATAREF | UPDATING, 0, 1, false),
  /** Insert nodes. */
  INSERT(Insert.class, DATAREF | UPDATING, 2, 5, false),
  /** Update nodes. */
  UPDATE(Update.class, DATAREF | UPDATING, 2, 4, false),

  // GENERAL COMMANDS =========================================================
      
  /** General separator. */
  DUMMYGENERAL(),
      
  /** Toggle chopping of whitespaces. */
  SET(Set.class, STANDARD, 1, 2, false),
  /** Get command overview or detailed information on one command. */
  HELP(Help.class, PRINTING, 0, 1, false),
  /** Test database connection. */
  PING(Ping.class, PRINTING, 0, 0, true),
  /** Show Prompt. */
  PROMPT(Prompt.class, PRINTING, 0, 0, true),
  /** Terminate client. */
  EXIT(null, LOCAL, 0, 0, false),
  /** Alias for 'exit'. */
  QUIT(null, LOCAL, 0, 0, true),

  // SERVER COMMANDS ==========================================================
  
  /** Internal control commands. */
  GETRESULT(null, SERVER, 0, 0, false),
  /** Internal 'get info' command. */
  GETINFO(null, SERVER, 0, 0, false),
  /** Stop server instance. */
  STOP(null, SERVER, 0, 0, false);
      
  // =========================================================================

  /** Command class. */
  private Class<? extends Proc> cmd;
  /** Command properties. */
  private int props;
  /** Minimum number of arguments. */
  private int minargs;
  /** Maximum number of arguments. */
  private int maxargs;
  /** Hidden command. */
  private boolean hidden;

  /**
   * Constructor, initializing the enum constants.
   * @param c command class
   * @param p command properties
   * @param m minimum number of arguments
   * @param x maximum number of arguments
   * @param d hidden flag
   */
  Commands(final Class<? extends Proc> c, final int p, final int m,
      final int x, final boolean d) {
    cmd = c;
    props = p;
    minargs = m;
    maxargs = x;
    hidden = d;
  }

  /**
   * Constructor, initializing a dummy entry.
   */
  Commands() { }

  /**
   * Returns if the current command is supposed to be processed locally.
   * @return result of check
   */
  public boolean local() {
    return check(LOCAL);
  }

  /**
   * Returns if the current command yields some output.
   * @return result of check
   */
  public boolean printing() {
    return check(PRINTING);
  }

  /**
   * Returns if the current command needs a data reference for processing.
   * @return result of check
   */
  public boolean data() {
    return check(DATAREF);
  }

  /**
   * Returns if the current command is a pure server command.
   * @return result of check
   */
  public boolean server() {
    return check(SERVER);
  }

  /**
   * Returns if the current command generates updates in the data structure.
   * @return result of check
   */
  public boolean updating() {
    return check(UPDATING);
  }

  /**
   * Checks the specified command property.
   * @param prop property to be checked
   * @return result of check
   */
  private boolean check(final int prop) {
    return (props & prop) != 0;
  }

  /**
   * Checks the number of arguments.
   * @param n number of arguments
   * @return true if number is correct
   */
  public boolean args(final int n) {
    return n >= minargs && (maxargs == -1 || n <= maxargs);
  }

  /**
   * Returns a help string.
   * @param detail show details
   * @param all show all commands (also hidden ones)
   * @return string
   */
  public String help(final boolean detail, final boolean all) {
    final StringBuilder sb = new StringBuilder();

    final Object args = help(0);
    if(name().startsWith("DUMMY")) {
      sb.append(NL + args + NL + NL);
    } else if(args != null && (!hidden || detail || all)) {
      sb.append(name().toLowerCase() + " " + args + NL + "  " + help(1) + NL);
      if(detail) sb.append(NL + help(2) + NL);
    } else {
      if(detail) sb.append(NOHELP + NL);
    }
    return sb.toString();
  }
  
  /**
   * Returns the specified help text for the current command.
   * @param n help offset
   * @return text or null
   */
  private String help(final int n) {
    try {
      return Text.class.getField(name() + n).get(null).toString();
    } catch(Exception e) {
      return null;
    }
  }

  /**
   * Returns a new process instance.
   * @param ctx command context
   * @param comm command instance
   * @return new process instance
   */
  Proc newInstance(final Command comm, final Context ctx) {
    try {
      final Proc proc = cmd.newInstance();
      proc.init(ctx, comm);
      return proc;
    } catch(final Exception ex) {
      BaseX.debug(ex);
      return null;
    }
  }

  /**
   * Returns the a reference to the specified command or an
   * error if the command was not found.
   * @param c command to be found
   * @return command
   */
  static Commands find(final String c) {
    try {
      if(!c.startsWith("DUMMY")) return valueOf(c.toUpperCase());
    } catch(final IllegalArgumentException e) { }

    final byte[] nm = Token.lc(Token.token(c));
    for(Commands cmd : values()) {
      final byte[] sim = Token.lc(Token.token(cmd.name()));
      if(Levenshtein.similar(nm, sim)) {
        throw new IllegalArgumentException(BaseX.info(CMDSIMILAR, c, sim));
      }
    }
    throw new IllegalArgumentException(BaseX.info(CMDWHICH, c));
  }

  /**
   * Returns an array with all commands.
   * @return command array
   */
  public static String[] list() {
    final StringList list = new StringList();
    for(Commands cmd : values()) {
      String name = cmd.name();
      if(name.startsWith("DUMMY") || cmd.hidden || cmd.server()) continue;
      list.add(name.toLowerCase());
    }
    return list.finish();
  }
}
