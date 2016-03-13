package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import org.basex.gui.*;
import org.basex.gui.layout.*;

/**
 * Editor preferences.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
final class DialogEditorPrefs extends BaseXBack {
  /** Show line margin. */
  private final BaseXCheckBox showmargin;
  /** Line margin. */
  private final BaseXTextField margin;
  /** Indent tabs as spaced. */
  private final BaseXCheckBox spaces;
  /** Indentation. */
  private final BaseXTextField indent;
  /** Show special characters. */
  private final BaseXCheckBox invisible;
  /** Show newlines. */
  private final BaseXCheckBox shownl;
  /** Show line numbers. */
  private final BaseXCheckBox numbers;
  /** Mark current line. */
  private final BaseXCheckBox markline;
  /** Save before executing file. */
  private final BaseXCheckBox saverun;
  /** Parse project files. */
  private final BaseXCheckBox parseproj;
  /** Automatically add characters. */
  private final BaseXCheckBox auto;
  /** Default file filter. */
  private final BaseXTextField files;
  /** Show hidden files. */
  private final BaseXCheckBox showHidden;

  /**
   * Default constructor.
   * @param d dialog reference
   */
  DialogEditorPrefs(final BaseXDialog d) {
    border(8).setLayout(new TableLayout(1, 2, 40, 0));
    final GUIOptions gopts = d.gui.gopts;
    showmargin = new BaseXCheckBox(SHOW_LINE_MARGIN + COL, GUIOptions.SHOWMARGIN, gopts, d);
    margin = new BaseXTextField(GUIOptions.MARGIN, gopts, d);
    invisible = new BaseXCheckBox(SHOW_INVISIBLE, GUIOptions.SHOWINVISIBLE, gopts, d);
    shownl = new BaseXCheckBox(SHOW_NEWLINES, GUIOptions.SHOWNL, gopts, d);
    numbers = new BaseXCheckBox(SHOW_LINE_NUMBERS, GUIOptions.SHOWLINES, gopts, d);
    markline = new BaseXCheckBox(MARK_EDITED_LINE, GUIOptions.MARKLINE, gopts, d);
    spaces = new BaseXCheckBox(TABS_AS_SPACES, GUIOptions.TABSPACES, gopts, d);
    indent = new BaseXTextField(GUIOptions.INDENT, gopts, d);
    auto = new BaseXCheckBox(AUTO_ADD_CHARS, GUIOptions.AUTO, gopts, d);
    saverun = new BaseXCheckBox(SAVE_BEFORE_EXECUTE, GUIOptions.SAVERUN, gopts, d);
    parseproj = new BaseXCheckBox(PARSE_PROJECT_FILES, GUIOptions.PARSEPROJ, gopts, d);
    files = new BaseXTextField(GUIOptions.FILES, gopts, d);
    showHidden = new BaseXCheckBox(SHOW_HIDDEN_FILES, GUIOptions.HIDDENFILES, gopts, d);
    margin.setColumns(4);
    indent.setColumns(3);
    files.setColumns(18);

    BaseXBack p = new BaseXBack().layout(new TableLayout(8, 1));
    p.add(new BaseXLabel(VIEW + COL, true, true));
    BaseXBack pp = new BaseXBack().layout(new TableLayout(1, 2, 8, 0));
    pp.add(showmargin);
    pp.add(margin);
    p.add(pp);
    p.add(invisible);
    p.add(shownl);
    p.add(numbers);
    p.add(markline);
    p.add(new BaseXLabel(FILE_FILTER + COL, true, true).border(6,  0,  6,  0));
    p.add(files);
    add(p);

    final BaseXBack pv = new BaseXBack().layout(new TableLayout(3, 1, 0, 8));
    p = new BaseXBack().layout(new TableLayout(4, 1));
    p.add(new BaseXLabel(EDIT + COL, true, true));
    pp = new BaseXBack().layout(new TableLayout(1, 2, 8, 0));
    pp.add(new BaseXLabel(INDENTATION_SIZE + COL));
    pp.add(indent);
    p.add(pp);
    p.add(spaces);
    p.add(auto);
    pv.add(p);

    p = new BaseXBack().layout(new TableLayout(3, 1));
    p.add(new BaseXLabel(EVALUATING + COL, true, true));
    p.add(saverun);
    p.add(parseproj);
    pv.add(p);

    p = new BaseXBack().layout(new TableLayout(2, 1));
    p.add(new BaseXLabel(PROJECT + COL, true, true));
    p.add(showHidden);
    pv.add(p);
    add(pv);
  }

  /**
   * Reacts on user input.
   * @return success flag
   */
  boolean action() {
    margin.setEnabled(showmargin.isSelected());
    indent.setEnabled(spaces.isSelected());
    showmargin.assign();
    invisible.assign();
    shownl.assign();
    numbers.assign();
    markline.assign();
    files.assign();
    spaces.assign();
    auto.assign();
    saverun.assign();
    parseproj.assign();
    showHidden.assign();
    // no short-circuiting, do all checks...
    return margin.assign() & indent.assign();
  }
}
