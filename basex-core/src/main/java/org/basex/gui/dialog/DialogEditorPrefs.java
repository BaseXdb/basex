package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import org.basex.gui.*;
import org.basex.gui.layout.*;

/**
 * Editor preferences.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class DialogEditorPrefs extends BaseXBack {
  /** Values of {@link GUIOptions#MAXFILES}. */
  private static final int[] MAXFILES = { 1000, 10000, 100000, 1000000, Integer.MAX_VALUE };
  /** Values of {@link GUIOptions#MAXHITS}. */
  private static final int[] MAXHITS = { 10, 100, 1000, 10000, Integer.MAX_VALUE };

  /** GUI options. */
  private final GUIOptions gopts;

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
  /** Single-line editor tabs. */
  private final BaseXCheckBox scrollTabs;
  /** Save before executing file. */
  private final BaseXCheckBox saverun;
  /** Remove trailing whitespace. */
  private final BaseXCheckBox trimLines;
  /** Append final newline. */
  private final BaseXCheckBox finalNl;
  /** Code completion. */
  private final BaseXCombo completion;
  /** Parse project files. */
  private final BaseXCheckBox parseproj;
  /** Automatically add characters. */
  private final BaseXCheckBox auto;
  /** Default file filter. */
  private final BaseXTextField files;
  /** Show hidden files. */
  private final BaseXCheckBox showHidden;
  /** Maximum number of indexed project files. */
  private final BaseXSlider maxFiles;
  /** Label for the number of indexed project files. */
  private final BaseXLabel maxFilesLabel;
  /** Maximum number of search results. */
  private final BaseXSlider maxHits;
  /** Label for the number of search results. */
  private final BaseXLabel maxHitsLabel;

  /**
   * Default constructor.
   * @param dialog dialog reference
   */
  DialogEditorPrefs(final BaseXDialog dialog) {
    border(8).setLayout(new ColumnLayout(40));

    gopts = dialog.gui().gopts;
    showmargin = new BaseXCheckBox(dialog, SHOW_LINE_MARGIN + COL, GUIOptions.SHOWMARGIN, gopts);
    margin = new BaseXTextField(dialog, GUIOptions.MARGIN, gopts);
    invisible = new BaseXCheckBox(dialog, SHOW_INVISIBLE, GUIOptions.SHOWINVISIBLE, gopts);
    shownl = new BaseXCheckBox(dialog, SHOW_NEWLINES, GUIOptions.SHOWNL, gopts);
    numbers = new BaseXCheckBox(dialog, SHOW_LINE_NUMBERS, GUIOptions.SHOWLINES, gopts);
    markline = new BaseXCheckBox(dialog, MARK_EDITED_LINE, GUIOptions.MARKLINE, gopts);
    scrollTabs = new BaseXCheckBox(dialog, SCROLL_TABS, GUIOptions.SCROLLTABS, gopts);
    spaces = new BaseXCheckBox(dialog, TABS_AS_SPACES, GUIOptions.TABSPACES, gopts);
    indent = new BaseXTextField(dialog, GUIOptions.INDENT, gopts);
    auto = new BaseXCheckBox(dialog, AUTO_ADD_CHARS, GUIOptions.AUTO, gopts);
    saverun = new BaseXCheckBox(dialog, SAVE_BEFORE_EXECUTE, GUIOptions.SAVERUN, gopts);
    trimLines = new BaseXCheckBox(dialog, TRIM_LINES, GUIOptions.TRIMLINES, gopts);
    finalNl = new BaseXCheckBox(dialog, FINAL_NEWLINE, GUIOptions.FINALNL, gopts);
    completion = new BaseXCombo(dialog, GUIOptions.COMPLETION, gopts, COMPLETIONS);
    parseproj = new BaseXCheckBox(dialog, PARSE_PROJECT_FILES, GUIOptions.PARSEPROJ, gopts);
    files = new BaseXTextField(dialog, GUIOptions.FILES, gopts);
    showHidden = new BaseXCheckBox(dialog, SHOW_HIDDEN_FILES, GUIOptions.SHOWHIDDEN, gopts);
    maxFiles = new BaseXSlider(dialog, 0, MAXFILES.length - 1,
      BaseXSlider.index(gopts.get(GUIOptions.MAXFILES), MAXFILES));
    maxFiles.addActionListener(e -> action());
    maxFilesLabel = new BaseXLabel(" ");
    maxHits = new BaseXSlider(dialog, 0, MAXHITS.length - 1,
      BaseXSlider.index(gopts.get(GUIOptions.MAXHITS), MAXHITS));
    maxHits.addActionListener(e -> action());
    maxHitsLabel = new BaseXLabel(" ");
    margin.setColumns(4);
    indent.setColumns(3);
    files.setColumns(18);
    BaseXLayout.setWidth(maxFiles, 120);
    BaseXLayout.setWidth(maxHits, 120);

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
    p.add(scrollTabs);
    add(p);

    p = new BaseXBack().layout(new RowLayout());
    p.add(new BaseXLabel(EDIT + COL, true, true));
    pp = new BaseXBack().layout(new ColumnLayout(8));
    pp.add(new BaseXLabel(CODE_COMPLETION + COL));
    pp.add(completion);
    p.add(pp);
    pp = new BaseXBack().layout(new ColumnLayout(8));
    pp.add(new BaseXLabel(INDENTATION_SIZE + COL));
    pp.add(indent);
    p.add(pp);
    p.add(spaces);
    p.add(auto);
    p.add(saverun);
    p.add(trimLines);
    p.add(finalNl);
    add(p);

    p = new BaseXBack().layout(new RowLayout());
    p.add(new BaseXLabel(PROJECT + COL, true, true));
    p.add(parseproj);
    p.add(showHidden);
    p.add(new BaseXLabel(FILE_FILTER + COL).border(6, 0, 2, 0));
    p.add(files);
    p.add(new BaseXLabel(INDEXED_FILES + COL).border(6, 0, 2, 0));
    pp = new BaseXBack().layout(new ColumnLayout(12));
    pp.add(maxFiles);
    pp.add(maxFilesLabel);
    p.add(pp);
    p.add(new BaseXLabel(SEARCH_RESULTS + COL).border(6, 0, 2, 0));
    pp = new BaseXBack().layout(new ColumnLayout(12));
    pp.add(maxHits);
    pp.add(maxHitsLabel);
    p.add(pp);
    add(p);
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
    scrollTabs.assign();
    files.assign();
    spaces.assign();
    auto.assign();
    saverun.assign();
    trimLines.assign();
    finalNl.assign();
    completion.assign();
    parseproj.assign();
    showHidden.assign();

    final int mf = MAXFILES[maxFiles.getValue()];
    gopts.set(GUIOptions.MAXFILES, mf);
    maxFilesLabel.setText(mf == Integer.MAX_VALUE ? ALL : BaseXLayout.format(mf));

    final int mh = MAXHITS[maxHits.getValue()];
    gopts.set(GUIOptions.MAXHITS, mh);
    maxHitsLabel.setText(mh == Integer.MAX_VALUE ? ALL : BaseXLayout.format(mh));

    // no short-circuiting, do all checks...
    return margin.assign() & indent.assign();
  }
}
