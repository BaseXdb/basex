package org.basex.core;

import static org.basex.core.Text.*;
import java.util.regex.Pattern;
import org.basex.core.cmd.Set;

/**
 * This class defines the available command-line commands.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
@SuppressWarnings("all")
public interface Commands {
  /** Create commands. */
  enum CmdCreate { DATABASE, DB, MAB, INDEX, USER, TRIGGER }
  /** Info commands. */
  enum CmdInfo { NULL, DATABASE, DB, INDEX, STORAGE }
  /** Drop commands. */
  enum CmdDrop { DATABASE, DB, INDEX, USER, BACKUP, TRIGGER }
  /** Show commands. */
  enum CmdShow { DATABASES, SESSIONS, USERS, BACKUPS, TRIGGERS}
  /** Permission commands. */
  enum CmdPerm { NONE, READ, WRITE, CREATE, ADMIN }
  /** Set commands. Should be synchronized with {@link Set#STRINGS}. */
  enum CmdSet { QUERYINFO, DEBUG, SERIALIZE, CHOP, ENTITY, TEXTINDEX, ATTRINDEX,
    FTINDEX, PATHINDEX }
  /** Index types. */
  enum CmdIndex { TEXT, ATTRIBUTE, FULLTEXT, PATH }
  /** Index types. */
  enum CmdIndexInfo { NULL, TEXT, ATTRIBUTE, FULLTEXT, PATH, TAG, ATTNAME }
  /** Alter types. */
  enum CmdAlter { DATABASE, DB, USER }

  /** Command definitions. */
  enum Cmd {
    ADD(HELPADD), ALTER(HELPALTER), BACKUP(HELPBACKUP), CHECK(HELPCHECK),
    CLOSE(HELPCLOSE), COPY(HELPCOPY), CREATE(HELPCREATE), CS(HELPCS),
    DELETE(HELPDELETE), DROP(HELPDROP), EXIT(HELPEXIT), EXPORT(HELPEXPORT),
    FIND(HELPFIND), GET(HELPGET), GRANT(HELPGRANT), HELP(HELPHELP),
    INFO(HELPINFO), KILL(HELPKILL), LIST(HELPLIST), OPEN(HELPOPEN),
    OPTIMIZE(HELPOPTIMIZE), PASSWORD(HELPPASSWORD), RESTORE(HELPRESTORE),
    RUN(HELPRUN), SET(HELPSET), SHOW(HELPSHOW), XQUERY(HELPXQUERY);

    /** Help texts. */
    private final String[] help;

    /**
     * Empty constructor.
     */
    private Cmd() {
      this(null);
    }

    /**
     * Default constructor.
     * @param h help texts, or {@code null} if command is hidden.
     */
    private Cmd(final String... h) {
      help = h;
    }

    /**
     * Returns a help string.
     * @param detail show details
     * @param wiki print wiki format
     * @return string
     */
    public final String help(final boolean detail, final boolean wiki) {
      final StringBuilder sb = new StringBuilder();
      if(wiki) {
        wiki(sb);
      } else {
        if(help == null) {
          if(detail) sb.append(NOHELP + NL);
        } else {
          sb.append(this + " " + help[0] + NL + "  " + help[1] + NL);
          if(detail) sb.append(NL + help[2] + NL);
        }
      }
      return sb.toString();
    }

    /**
     * Returns a help string in the Wiki format.
     * @param sb string builder
     */
    private void wiki(final StringBuilder sb) {
      if(help == null) return;

      sb.append("===" + this + "===" + NL + NL);
      sb.append("'''<code>" + this + " " + help[0] + "</code>'''" + NL + NL);

      for(String s : help[2].split(NL)) {
        if(s.startsWith("- ")) {
          s = s.replaceAll("^- (.*?)(:|$)", "* <code>$1</code>$2");
        } else {
          s = s.replaceAll("^ ", ":");
          s = s.replaceAll("\\[", "<code>[").replaceAll("\\]", "]</code>");
        }
        sb.append(s).append(NL);
      }
      sb.append(NL);
    }
  }
}
