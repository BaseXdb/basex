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
  /** Create command definitions. */
  enum CmdCreate { DATABASE, DB, MAB, FS, INDEX, USER }
  /** Info command definitions. */
  enum CmdInfo { NULL, DATABASE, DB, INDEX, TABLE, USERS }
  /** Drop command definitions. */
  enum CmdDrop { DATABASE, DB, INDEX, USER }
  /** Show command definitions. */
  enum CmdShow { DATABASES, SESSIONS, USERS }
  /** Show command definitions. */
  enum CmdPerm { READ, WRITE, CREATE, ADMIN, ALL }
  /** Set command definitions. */
  enum CmdSet {
    INFO, DEBUG, SERIALIZE, XMLOUTPUT, MAINMEM, CHOP,
    ENTITY, TEXTINDEX, ATTRINDEX, FTINDEX
  }
  /** Node types for updates. Order equals the data kind definitions. */
  enum CmdUpdate { FRAGMENT, ELEMENT, TEXT, ATTRIBUTE, COMMENT, PI }
  /** Index types. */
  enum CmdIndex { TEXT, ATTRIBUTE, FULLTEXT, SUMMARY }

  /** Command flag: command which will not be shown in the help. */
  int HID = 1;
  /** Command flag: command which cannot be run by the user. */
  int INT = 2;

  /** Command definitions. */
  enum Cmd {
    // Database commands
    HD(HID, HELPDB), CREATE(HELPCREATE), C(HID), OPEN(HELPOPEN), O(HID),
    INFO(HELPINFO), I(HID), CLOSE(HELPCLOSE), LIST(HELPLIST),
    DROP(HELPDROP), EXPORT(HELPEXPORT), OPTIMIZE(HELPOPTIMIZE),
    // Query commands
    HQ(HID, HELPQ), XQUERY(HELPXQUERY), X(HID), XQUERYMV(HID),
    FIND(HELPFIND), RUN(HELPRUN), CS(HELPCS),
    // Admin commands
    HA(HID, HELPA), KILL(HELPKILL), SHOW(HELPSHOW),
    GRANT(HELPGRANT), REVOKE(HELPREVOKE), ALTER(HELPALTER),
    // General commands
    HG(HID, HELPG), SET(HELPSET), PASSWORD(HELPPASSWORD),
    HELP(HELPHELP), EXIT(HELPEXIT), Q(HID), QUIT(HID),
    // Internal commands
    INTINFO(INT), INTSTOP(INT);

    /** Flags for controlling command parsing. */
    private final int flags;
    /** Help texts. */
    private final String[] help;

    /**
     * Default constructor.
     * @param h help texts
     */
    private Cmd(final String... h) {
      this(0, h);
    }

    /**
     * Constructor with additional flags.
     * @param f command flags
     * @param h help texts
     */
    private Cmd(final int f, final String... h) {
      flags = f;
      help = h;
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
      return help.length == 1;
    }

    /**
     * Returns a help string.
     * @param detail show details
     * @return string
     */
    public final String help(final boolean detail) {
      final StringBuilder sb = new StringBuilder();
      if(help.length == 0) {
        if(detail) sb.append(NOHELP + NL);
      } else if(help.length == 1) {
        sb.append(NL + help[0] + NL + NL);
      } else {
        sb.append(this + " " + help[0] + NL + "  " + help[1] + NL);
        if(detail) sb.append(NL + help[2] + NL);
      }
      return sb.toString();
    }

    /*
     * Returns a help string as html.
     * @return string
  public final String html() {
    final StringBuilder sb = new StringBuilder();
    if(dummy()) {
      sb.append("<br/>" + NL + "<h2>" + help[0] + "</h2>" + NL);
    } else {
      if(args != null && !dummy() && !internal()) {
        String name = name().toLowerCase();
        sb.append("<a name=\"" + name + "\"></a>");
        sb.append("<h3>" + name.substring(0, 1).toUpperCase() +
            name.substring(1) + "</h3>" + NL);
        sb.append("<p>" + NL);
        sb.append("<code>" + name() + " " + help[0] + "</code><br/><br/>" + NL);
        final String help2 = help[2];

        final String help1 = Pattern.compile("\n\r?\n.*", Pattern.DOTALL).
          matcher(help2).replaceAll("");
        sb.append(help1.replaceAll("(\\[.*?\\]\\??)", "<code>$1</code>"));
        sb.append(NL + "</p>" + NL);

        boolean first = true;
        for(String s : help2.split(NL)) {
          if(s.isEmpty()) continue;
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
  }
}
