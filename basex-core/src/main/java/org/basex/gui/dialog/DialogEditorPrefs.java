package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import org.basex.gui.*;
import org.basex.gui.layout.*;

/**
 * Editor preferences.
 *
 * @author BaseX Team 2005-21, BSD License
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
   * @param dialog dialog reference
   */
  DialogEditorPrefs(final BaseXDialog dialog) {
    border(8).setLayout(new ColumnLayout(40));

    final GUIOptions gopts = dialog.gui.gopts;
    showmargin = new BaseXCheckBox(dialog, SHOW_LINE_MARGIN + COL, GUIOptions.SHOWMARGIN, gopts);
    margin = new BaseXTextField(dialog, GUIOptions.MARGIN, gopts);
    invisible = new BaseXCheckBox(dialog, SHOW_INVISIBLE, GUIOptions.SHOWINVISIBLE, gopts);
    shownl = new BaseXCheckBox(dialog, SHOW_NEWLINES, GUIOptions.SHOWNL, gopts);
    numbers = new BaseXCheckBox(dialog, SHOW_LINE_NUMBERS, GUIOptions.SHOWLINES, gopts);
    markline = new BaseXCheckBox(dialog, MARK_EDITED_LINE, GUIOptions.MARKLINE, gopts);
    spaces = new BaseXCheckBox(dialog, TABS_AS_SPACES, GUIOptions.TABSPACES, gopts);
    indent = new BaseXTextField(dialog, GUIOptions.INDENT, gopts);
    auto = new BaseXCheckBox(dialog, AUTO_ADD_CHARS, GUIOptions.AUTO, gopts);
    saverun = new BaseXCheckBox(dialog, SAVE_BEFORE_EXECUTE, GUIOptions.SAVERUN, gopts);
    parseproj = new BaseXCheckBox(dialog, PARSE_PROJECT_FILES, GUIOptions.PARSEPROJ, gopts);
    files = new BaseXTextField(dialog, GUIOptions.FILES, gopts);
    showHidden = new BaseXCheckBox(dialog, SHOW_HIDDEN_FILES, GUIOptions.SHOWHIDDEN, gopts);
    margin.setColumns(4);
    indent.setColumns(3);
    files.setColumns(18);

    BaseXBack p = new BaseXBack().layout(new RowLayout());
    p.add(new BaseXLabel(VIEW + COL, true, true));
    BaseXBack pp = new BaseXBack().layout(new ColumnLayout(8));
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

    final BaseXBack pv = new BaseXBack().layout(new RowLayout(8));
    p = new BaseXBack().layout(new RowLayout());
    p.add(new BaseXLabel(EDIT + COL, true, true));
    pp = new BaseXBack().layout(new ColumnLayout(8));
    pp.add(new BaseXLabel(INDENTATION_SIZE + COL));
    pp.add(indent);
    p.add(pp);
    p.add(spaces);
    p.add(auto);
    pv.add(p);

    p = new BaseXBack().layout(new RowLayout());
    p.add(new BaseXLabel(EVALUATING + COL, true, true));
    p.add(saverun);
    p.add(parseproj);
    pv.add(p);

    p = new BaseXBack().layout(new RowLayout());
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
