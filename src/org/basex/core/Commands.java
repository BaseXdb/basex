package org.basex.core;

import static org.basex.core.Text.*;
import java.util.regex.Pattern;

/**
 * This class defines the available command-line commands.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
@SuppressWarnings("all")
public interface Commands {
  /** Command flag: command which will not be shown in the help. */
  int HID = 1;
  /** Command flag: command which cannot be run by the user. */
  int INT = 2;
  /** Command flag: dummy command for formatting the help output. */
  int HLP = 4;
  /** Command flag: command which can only be executed by the server. */
  int SRV = 8;

  /** Command definitions. */
  enum Cmd {
    // Database commands
    HELPDATABASE(HLP), C(HID), CREATE(), O(HID), OPEN(), I(HID), INFO(),
    CLOSE(), LIST(), DROP(), EXPORT(), OPTIMIZE(),
    // Query commands
    HELPQUERY(HLP), X(HID), XQUERY(), XQUERYMV(HID), RUN(), FIND(), CS(),
    // Update commands
    HELPUPDATE(HLP), COPY(), DELETE(), INSERT(), UPDATE(),
    // Server commands
    HELPSERVER(HLP | SRV), KILL(SRV), SHOW(SRV),
    // General commands
    HELPGENERAL(HLP), SET(), HELP(), EXIT(), Q(HID), QUIT(HID),
    // Internal commands
    INTPROMPT(INT), INTOUTPUT(INT), INTINFO(INT), INTSTOP(INT);

    /** Flags for controlling command parsing. */
    private final int flags;

    /**
     * Default constructor.
     */
    private Cmd() {
      this(0);
    }

    /**
     * Constructor with additional flags.
     * @param f command flags
     */
    private Cmd(final int f) {
      flags = f;
    }

    /**
     * Returns if this is a command which will not be shown in the help.
     * @return result of check
     */
    boolean hidden() {
      return (flags & HID) != 0;
    }

    /**
     * Returns if this is an internal command which cannot be run by the user.
     * @return result of check
     */
    boolean internal() {
      return (flags & INT) != 0;
    }

    /**
     * Returns if this is a dummy command for formatting the help.
     * @return result of check
     */
    boolean help() {
      return (flags & HLP) != 0;
    }

    /**
     * Returns if this is a command which can only be executed by the server.
     * @return result of check
     */
    boolean server() {
      return (flags & SRV) != 0;
    }

    /**
     * Returns a help string.
     * @param detail show details
     * @param server show server commands
     * @return string
     */
    public final String help(final boolean detail, final boolean server) {
      final StringBuilder sb = new StringBuilder();

      final Object args = help(0);
      if(help()) {
        if(!server() || server) sb.append(NL + args + NL + NL);
      } else if(args != null && !help() && !internal() &&
          (!hidden() || detail) && (!server() || server)) {
        sb.append(this + " " + args + NL + "  " + help(1) + NL);
        if(detail) sb.append(NL + help(2) + NL);
      } else {
        if(detail) sb.append(NOHELP + NL);
      }
      return sb.toString();
    }

    /*
     * Returns a help string as html.
     * @return string
    public final String html() {
      final StringBuilder sb = new StringBuilder();
      final Object args = help(0);
      if(dummy()) {
        sb.append("<br/>" + NL + "<h2>" + args + "</h2>" + NL);
      } else {
        if(args != null && !dummy() && !internal()) {
          String name = name().toLowerCase();
          sb.append("<a name=\"" + name + "\"></a>");
          sb.append("<h3>" + name.substring(0, 1).toUpperCase() +
              name.substring(1) + "</h3>" + NL);
          sb.append("<p>" + NL);
          sb.append("<code>" + name() + " " + args + "</code><br/><br/>" + NL);
          final String help2 = help(2);

          final String help1 = Pattern.compile("\n\r?\n.*", Pattern.DOTALL).
            matcher(help2).replaceAll("");
          sb.append(help1.replaceAll("(\\[.*?\\]\\??)", "<code>$1</code>"));
          sb.append(NL + "</p>" + NL);

          boolean first = true;
          for(String s : help2.split(NL)) {
            if(s.length() == 0) continue;
            if(s.startsWith("- ")) {
              sb.append((first ? "<ul>" : "</li>") + NL);
              sb.append(s.replaceAll(
                  "- (.*?):(.*)", "<li><code>$1</code>: $2<br/>") + NL);
              first = false;
            } else if(!first) {
              sb.append(s.replaceAll("(\\[.*?\\]\\??)", "<code>$1</code>"));
              sb.append(NL);
            }
          }
          if(!first) sb.append("</li>" + NL + "</ul>" + NL);
          sb.append(NL);
        }
      }
      return sb.toString();
    }
     */

    /**
     * Returns the specified help text for the current command.
     * @param n help offset
     * @return text or null
     */
    private String help(final int n) {
      try {
        return Text.class.getField(name() + n).get(null).toString();
      } catch(final Exception ex) {
        return null;
      }
    }
  }

  /** Create command definitions. */
  enum CmdCreate { DATABASE, DB, MAB, FS, INDEX }
  /** Info command definitions. */
  enum CmdInfo { NULL, DATABASE, DB, INDEX, TABLE }
  /** Drop command definitions. */
  enum CmdDrop { DATABASE, DB, INDEX }
  /** Show command definitions. */
  enum CmdShow { DATABASES, SESSIONS }
  /** Set command definitions. */
  enum CmdSet {
    INFO, DEBUG, SERIALIZE, XMLOUTPUT, MAINMEM, CHOP,
    ENTITY, TEXTINDEX, ATTRINDEX, FTINDEX
  }

  /** Index types. */
  enum CmdIndex { TEXT, ATTRIBUTE, FULLTEXT, SUMMARY }
  /** Node types for updates. Order equals the data kind definitions. */
  enum CmdUpdate { FRAGMENT, ELEMENT, TEXT, ATTRIBUTE, COMMENT, PI }
}
