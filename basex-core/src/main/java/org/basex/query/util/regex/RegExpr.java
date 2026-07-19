package org.basex.query.util.regex;

import java.util.*;
import java.util.regex.*;

import org.basex.util.*;

/**
 * Compiled regular expression (pattern and lazily computed group metadata).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class RegExpr {
  /** Pattern. */
  public final Pattern pattern;
  /** Group info (volatile: lazily set once, may be read from concurrent child contexts). */
  private volatile GroupInfo groupInfo;

  /**
   * Constructor.
   * @param pattern pattern
   */
  public RegExpr(final Pattern pattern) {
    this.pattern = pattern;
    groupInfo = null;
  }

  /**
   * Returns the parent group IDs of capturing groups.
   * @return parent group IDs.
   */
  public int[] getParentGroups() {
    if(groupInfo == null) groupInfo = GroupScanner.groupInfo(pattern.pattern());
    return groupInfo.parentGroups;
  }

  /**
   * Returns the assertion flags of capturing groups.
   * @return assertion flags.
   */
  public boolean[] getAssertionFlags() {
    if(groupInfo == null) groupInfo = GroupScanner.groupInfo(pattern.pattern());
    return groupInfo.assertionFlags;
  }

  /**
   * Returns the names of capturing groups.
   * @return names: element i contains the name of capturing group i+1, or {@code null}.
   */
  public String[] getGroupNames() {
    if(groupInfo == null) groupInfo = GroupScanner.groupInfo(pattern.pattern());
    return groupInfo.groupNames;
  }

  /**
   * Information about capturing groups.
   */
  public static class GroupInfo {
    /** Parent group: element i contains the parent group ID of capturing group i+1. */
    public final int[] parentGroups;
    /** Assertion status: element i tells whether capturing group i+1 occurs in an assertion. */
    public final boolean[] assertionFlags;
    /** Group names: element i contains the name of capturing group i+1, or {@code null}. */
    public final String[] groupNames;

    /**
     * Constructor.
     * @param parentGroup parent group IDs
     * @param assertionFlags inside of assertion indicators
     * @param groupNames group names
     */
    GroupInfo(final int[] parentGroup, final boolean[] assertionFlags, final String[] groupNames) {
      parentGroups = parentGroup;
      this.assertionFlags = assertionFlags;
      this.groupNames = groupNames;
    }
  }

  /**
   * Analyzes the nesting of capturing groups in a Java regular expression.
   */
  public static final class GroupScanner {
    /** The pattern. */
    private final String pattern;
    /** Pattern length. */
    private final int len;
    /** Current position. */
    private int pos;
    /** Length of most recent code point. */
    private int chrCount;
    /** Name of the most recently scanned named capturing group ({@code null} otherwise). */
    private String name;

    /**
     * Constructor.
     * @param pattern a Java regular expression.
     */
    private GroupScanner(final String pattern) {
      this.pattern = pattern;
      len = pattern.length();
      pos = 0;
    }

    /**
     * Find the parent groups of capturing groups in a Java regular expression.
     * @param pattern the regular expression.
     * @return an array indicating the parent group ID for each capturing group, where element i
     * contains the parent group ID of capturing group i+1.
     */
    public static GroupInfo groupInfo(final String pattern) {
      final GroupScanner gnd = new GroupScanner(pattern);
      final Stack<Integer> open = new Stack<>();
      open.push(0);
      int[] parentGroups = { };
      boolean[] inAssertion = { };
      String[] groupNames = { };
      boolean quoted = false;
      int classLevel = 0;
      int assrtMark = 0;
      for(;;) {
        switch(gnd.nxtToken()) {
          case EOP -> {
            return new GroupInfo(parentGroups, inAssertion, groupNames);
          }
          case LBRACKET -> {
            if(!quoted) ++classLevel;
          }
          case RBRACKET -> {
            if(!quoted) --classLevel;
          }
          case LQUOTE -> {
            if(classLevel == 0) quoted = true;
          }
          case RQUOTE -> {
            if(classLevel == 0) quoted = false;
          }
          case CAPT_LPAREN -> {
            if(!quoted && classLevel == 0) {
              parentGroups = Array.add(parentGroups, open.peek());
              inAssertion = Array.add(inAssertion, assrtMark != 0);
              groupNames = Array.add(groupNames, gnd.name);
              open.push(parentGroups.length);
            }
          }
          case ASSRT_LPAREN -> {
            if(!quoted && classLevel == 0) {
              open.push(open.peek());
              if(assrtMark == 0) assrtMark = open.size();
            }
          }
          case LPAREN -> {
            if(!quoted && classLevel == 0) open.push(open.peek());
          }
          case RPAREN -> {
            if(!quoted && classLevel == 0) {
              if(open.size() == assrtMark) assrtMark = 0;
              open.pop();
            }
          }
          default -> { }
        }
      }
    }

    /**
     * Return the next token.
     * @return next token
     */
    private Token nxtToken() {
      name = null;
      return switch(nxtCp()) {
        case -1 -> Token.EOP;
        case ']' -> Token.RBRACKET;
        case '[' -> Token.LBRACKET;
        case ')' -> Token.RPAREN;
        case '\\' -> switch(nxtCp()) {
          case -1 -> Token.EOP;
          case 'Q' -> Token.LQUOTE;
          case 'E' -> Token.RQUOTE;
          default -> Token.OTHER;
        };
        case '(' -> switch(nxtCp()) {
          case '?' -> switch(nxtCp()) {
            case '=', '!' -> Token.ASSRT_LPAREN;
            case '<' -> {
              final int c = nxtCp();
              if(c == '=' || c == '!') yield Token.ASSRT_LPAREN;
              // named capturing group (?<name>...): record the name
              final StringBuilder nm = new StringBuilder();
              for(int n = c; n != '>' && n != -1; n = nxtCp()) nm.append((char) n);
              name = nm.toString();
              yield Token.CAPT_LPAREN;
            }
            default -> {
              reset();
              yield Token.LPAREN;
            }
          };
          default -> {
            reset();
            yield Token.CAPT_LPAREN;
          }
        };
        default -> Token.OTHER;
      };
    }

    /**
     * Fetch the next code point.
     * @return the next code point.
     */
    private int nxtCp() {
      final int cp;
      if(pos < len) {
        cp = pattern.codePointAt(pos);
        chrCount = Character.charCount(cp);
        pos += chrCount;
      }
      else {
        cp = -1;
        chrCount = 0;
      }
      return cp;
    }

    /**
     * Reset current position to before the most recent code point.
     */
    private void reset() {
      pos -= chrCount;
    }

    /** Relevant token types. */
    private enum Token {
      /** End of pattern.                         */ EOP,
      /** Capturing group's left parenthesis.     */ CAPT_LPAREN,
      /** Non-capturing group's left parenthesis. */ LPAREN,
      /** Assertion's left parenthesis.           */ ASSRT_LPAREN,
      /** Right parenthesis.                      */ RPAREN,
      /** Left square bracket.                    */ LBRACKET,
      /** Right square bracket.                   */ RBRACKET,
      /** Left quote.                             */ LQUOTE,
      /** Right quote.                            */ RQUOTE,
      /** Anything else.                          */ OTHER,
    }
  }
}
