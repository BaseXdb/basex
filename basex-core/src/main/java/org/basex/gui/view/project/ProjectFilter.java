package org.basex.gui.view.project;

import static org.basex.gui.GUIConstants.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;
import java.util.regex.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.text.*;
import org.basex.gui.view.editor.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * Project filter.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class ProjectFilter extends BaseXBack {
  /** Directory for replacement backups. */
  private static final IOFile REPLACE_TEMP =
      new IOFile(new IOFile(Prop.TEMPDIR, Prop.PROJECT), "replace");

  /** Backup directory. */
  private final IOFile backupDir = new IOFile(REPLACE_TEMP, String.valueOf(Prop.PID));
  /** Files. */
  private final BaseXCombo filesFilter;
  /** Contents. */
  private final BaseXCombo contentsFilter;
  /** Mode: match case. */
  private final AbstractButton mcase;
  /** Mode: whole word. */
  private final AbstractButton word;
  /** Mode: regular expression. */
  private final AbstractButton regex;
  /** Mode: dot matches all. */
  private final AbstractButton dotall;
  /** Replacement. */
  private final BaseXCombo replace;
  /** Replace-all button. */
  private final AbstractButton replaceButton;
  /** Replacement row. */
  private final BaseXBack replaceRow;
  /** Undo last replacement. */
  private final AbstractButton undo;
  /** Project view. */
  private final ProjectView view;
  /** Common text field shortcuts. */
  private final KeyListener modeKeys = new KeyAdapter() {
    @Override
    public void keyPressed(final KeyEvent e) {
      if(BaseXKeys.META_ENTER.is(e)) {
        replace();
      } else if(BaseXKeys.MATCHCASE.is(e)) {
        toggleMode(mcase);
      } else if(BaseXKeys.WHOLEWORD.is(e)) {
        toggleMode(word);
      } else if(BaseXKeys.REGEX.is(e)) {
        toggleMode(regex);
      } else if(BaseXKeys.DOTALL.is(e)) {
        if(dotall.isEnabled()) toggleMode(dotall);
      } else {
        return;
      }
      e.consume();
    }
  };

  /** Last files filter. */
  private String lastFiles = "";
  /** Last contents filter. */
  private String lastContents = "";

  /**
   * Constructor.
   * @param view project view
   */
  ProjectFilter(final ProjectView view) {
    this.view = view;

    cleanupBackups();

    layout(new BorderLayout(0, 2));
    filesFilter = new BaseXCombo(view.gui, true).history(GUIOptions.PROJFILES, view.gui.gopts);
    filesFilter.addFocusListener(view.lastfocus);

    contentsFilter = new BaseXCombo(view.gui, true).history(GUIOptions.PROJCONTS, view.gui.gopts);
    contentsFilter.hint(Text.FIND_CONTENTS + Text.DOTS);
    contentsFilter.addFocusListener(view.lastfocus);

    // content search modes
    mcase = toggle("f_case",
        BaseXLayout.addShortcut(Text.MATCH_CASE, BaseXKeys.MATCHCASE.toString()));
    word = toggle("f_word",
        BaseXLayout.addShortcut(Text.WHOLE_WORD, BaseXKeys.WHOLEWORD.toString()));
    regex = toggle("f_regex",
        BaseXLayout.addShortcut(Text.REGULAR_EXPR, BaseXKeys.REGEX.toString()));
    dotall = toggle("f_dotall",
        BaseXLayout.addShortcut(Text.DOT_ALL, BaseXKeys.DOTALL.toString()));
    dotall.setEnabled(false);

    final BaseXToolBar modes = new BaseXToolBar();
    modes.add(mcase);
    modes.add(word);
    modes.add(regex);
    modes.add(dotall);
    final BaseXBack contentRow = new BaseXBack(false).layout(new BorderLayout(2, 0));
    contentRow.add(contentsFilter, BorderLayout.CENTER);
    contentRow.add(modes, BorderLayout.EAST);

    // content replacement
    replace = new BaseXCombo(view.gui, true).history(GUIOptions.PROJREPLACE, view.gui.gopts);
    replace.hint(Text.REPLACE_WITH + Text.DOTS);
    replace.addFocusListener(view.lastfocus);
    replaceButton = BaseXButton.get("f_replaceall", BaseXLayout.addShortcut(
        Text.REPLACE_ALL, BaseXKeys.META_ENTER.toString()), false, view.gui);
    replaceButton.addActionListener(e -> replace());
    replaceButton.setEnabled(false);
    undo = BaseXButton.get("c_go_back", Text.UNDO_REPLACE, false, view.gui);
    undo.addActionListener(e -> undoReplace());
    undo.setEnabled(false);
    final BaseXToolBar actions = new BaseXToolBar();
    actions.add(undo);
    actions.add(replaceButton);
    replaceRow = new BaseXBack(false).layout(new BorderLayout(2, 0));
    replaceRow.add(replace, BorderLayout.CENTER);
    replaceRow.add(actions, BorderLayout.EAST);
    replaceRow.setVisible(false);

    add(filesFilter, BorderLayout.NORTH);
    add(contentRow, BorderLayout.CENTER);

    addKeyListener(filesFilter);
    addKeyListener(contentsFilter);
    replace.addKeyListener(modeKeys);
    refreshLayout();
  }

  /**
   * Refreshes the filter view.
   * @param enforce enforce refresh
   */
  void refresh(final boolean enforce) {
    final String files = filesFilter.getText();
    final String contents = contentsFilter.getText();
    if(!enforce && lastFiles.equals(files) && lastContents.equals(contents)) return;
    lastFiles = files;
    lastContents = contents;

    final boolean filter = !files.isEmpty() || !contents.isEmpty();
    if(filter) {
      filter(files, contents);
    } else {
      // clear the feedback of a previous search
      contentsFilter.highlight(backColor);
      contentsFilter.setToolTipText(null);
      showReplace(false);
    }
    view.showList(filter || replaceRow.isVisible());
  }

  /**
   * Finds files with the text selected in the specified editor area.
   * @param ea calling editor
   */
  void find(final EditorArea ea) {
    final String string = ea.searchString();
    if(!string.isEmpty()) {
      // reset the search flags: the selected text is searched literally
      mcase.setSelected(false);
      word.setSelected(false);
      regex.setSelected(false);
      dotall.setSelected(false);
      dotall.setEnabled(false);
      contentsFilter.setText(string);
      if(ea.opened()) {
        final String pattern = filePattern(ea.file().name());
        if(pattern != null) filesFilter.setText(pattern);
      }
      refresh(true);
    }
    contentsFilter.requestFocusInWindow();
  }

  /**
   * Derives a files filter from a file name.
   * @param name file name
   * @return new files pattern or {@code null}
   */
  private String filePattern(final String name) {
    // extension-less files have no meaningful extension pattern
    final int dot = name.lastIndexOf('.');
    if(dot == -1) return null;
    final String files = filesFilter.getText();
    final String pattern = files.isEmpty() ? view.gui.gopts.get(GUIOptions.FILES) : files;
    // keep the current pattern if it has wildcards and already matches the file
    final boolean wildcards = pattern.contains("*") || pattern.contains("?");
    return wildcards && Pattern.compile(IOFile.regex(pattern)).matcher(name).matches() ?
        null : '*' + name.substring(dot);
  }

  /**
   * Returns the current content search flags.
   * @return search flags
   */
  SearchFlags flags() {
    return new SearchFlags(mcase.isSelected(), word.isSelected(),
        regex.isSelected(), dotall.isSelected());
  }

  /**
   * Returns the replacement row.
   * @return replacement row
   */
  BaseXBack replaceRow() {
    return replaceRow;
  }

  /**
   * Shows or hides the replacement row.
   * @param contentSearch content search is active
   */
  private void showReplace(final boolean contentSearch) {
    final boolean show = contentSearch || undo.isEnabled();
    if(replaceRow.isVisible() != show) {
      replaceRow.setVisible(show);
      view.revalidate();
    }
  }

  /**
   * Replaces the content search string in all listed files.
   */
  private void replace() {
    if(!replaceButton.isEnabled()) return;

    final String contents = view.list.search();
    final boolean rgx = regex.isSelected();
    final Pattern pattern;
    try {
      pattern = SearchContext.pattern(contents, mcase.isSelected(), word.isSelected(), rgx,
          dotall.isSelected());
    } catch(final PatternSyntaxException ex) {
      Util.debug(ex);
      return;
    }

    final List<IOFile> targets = view.list.allFiles();
    if(targets.isEmpty() || !BaseXDialog.confirm(view.gui,
        Util.info(Text.REPLACE_FILES_X, targets.size()))) return;

    final String in = replace.getText();
    final String replacement = rgx ? SearchContext.normalize(in) : Matcher.quoteReplacement(in);
    replace.updateHistory();

    // save open editors so the on-disk replacement sees their current content
    view.gui.editor.saveAll();
    discardBackups();

    final IOFile root = view.root.file;
    boolean changed = false;
    try {
      for(final IOFile file : targets) {
        final String rel = relative(root, file);
        if(rel != null && ProjectFiles.replace(file, pattern, replacement,
            new IOFile(backupDir, rel))) {
          changed = true;
          final EditorArea ea = view.gui.editor.editor(file);
          if(ea != null) ea.reopen(true);
        }
      }
    } catch(final RuntimeException ex) {
      Util.debug(ex);
      backupDir.delete();
      replace.highlight(lightRed);
      replace.setToolTipText(ex.getLocalizedMessage());
      return;
    }
    replace.highlight(backColor);
    replace.setToolTipText(null);
    undo.setEnabled(changed);
    view.refresh();
  }

  /**
   * Undoes the last replacement.
   */
  private void undoReplace() {
    final IOFile root = view.root.file;
    for(final String rel : backupDir.descendants()) {
      final IOFile file = new IOFile(root, rel);
      try {
        new IOFile(backupDir, rel).copyTo(file);
      } catch(final IOException ex) {
        // file may not be writable
        Util.debug(ex);
      }
      final EditorArea ea = view.gui.editor.editor(file);
      if(ea != null) ea.reopen(true);
    }
    discardBackups();
    view.refresh();
  }

  /**
   * Discards the pending replacement backup.
   */
  void discardBackups() {
    backupDir.delete();
    undo.setEnabled(false);
  }

  /**
   * Returns the path of a file relative to the project root.
   * @param root project root directory
   * @param file file
   * @return relative path, or {@code null} if the file is not inside the root
   */
  private static String relative(final IOFile root, final IOFile file) {
    final String base = Strings.endsWith(root.path(), '/') ? root.path() : root.path() + '/';
    final String path = file.path();
    return path.startsWith(base) ? path.substring(base.length()) : null;
  }

  /**
   * Removes this instance's backups and those of dead instances, never a live instance's.
   */
  private static void cleanupBackups() {
    for(final IOFile child : REPLACE_TEMP.children()) {
      final long owner = Strings.toLong(child.name());
      if(owner == Prop.PID || owner > 0 && ProcessHandle.of(owner).isEmpty()) child.delete();
    }
  }

  /**
   * Creates a content search-mode toggle button that refreshes the filter when clicked.
   * @param icon icon name
   * @param tooltip tooltip text
   * @return button
   */
  private AbstractButton toggle(final String icon, final String tooltip) {
    final AbstractButton button = BaseXButton.get(icon, tooltip, true, view.gui);
    button.addActionListener(e -> modeChanged());
    return button;
  }

  /**
   * Flips a search-mode button (invoked via keyboard shortcut) and refreshes the filter.
   * @param button mode button
   */
  private void toggleMode(final AbstractButton button) {
    button.setSelected(!button.isSelected());
    modeChanged();
  }

  /**
   * Adopts a changed search mode: enables dot-all only for regular expressions, and refreshes.
   */
  private void modeChanged() {
    dotall.setEnabled(regex.isSelected());
    refresh(true);
  }

  /**
   * Called when the GUI design has changed.
   */
  void refreshLayout() {
    final String filter = view.gui.gopts.get(GUIOptions.FILES).trim();
    filesFilter.hint(filter.isEmpty() ? Text.FIND_FILES + Text.DOTS : filter);
  }

  /**
   * Filters the entries.
   * @param files files search string
   * @param contents contents search string
   */
  private void filter(final String files, final String contents) {
    filesFilter.setCursor(CURSORWAIT);
    contentsFilter.setCursor(CURSORWAIT);
    view.list.setCursor(CURSORWAIT);

    final ProjectFiles.ContentFilter content = view.files.contentFilter(contents,
        mcase.isSelected(), word.isSelected(), regex.isSelected(), dotall.isSelected());
    new GUIWorker<String[]>() {
      @Override
      protected String[] doInBackground() throws Exception {
        final String pattern = files.isEmpty() ? view.gui.gopts.get(GUIOptions.FILES) : files;
        return view.files.filter(pattern, content, view.root.file);
      }

      @Override
      protected void done(final String[] list) {
        view.list.setElements(list, contents);
        final String error = content.error();
        contentsFilter.highlight(error != null ? lightRed : backColor);
        contentsFilter.setToolTipText(error);
        final String found = list.length >= ProjectFiles.MAXHITS ?
          ">" + ProjectFiles.MAXHITS : String.valueOf(list.length);
        view.gui.status.setText(contents.isEmpty() ? Util.info(Text.FILES_FOUND_X, found) :
          Util.info(Text.FILES_FOUND_STATS_X, found, content.searched(), content.tooLarge(),
              content.binary()), true);
        final boolean contentSearch = !contents.isEmpty();
        replaceButton.setEnabled(contentSearch && list.length > 0);
        showReplace(contentSearch);
        filesFilter.setCursor(CURSORTEXT);
        contentsFilter.setCursor(CURSORTEXT);
        view.list.setCursor(CURSORARROW);
      }
    }.execute();
  }

  /**
   * Adds a key listener to the specified combo box.
   * @param combo combo box
   */
  private void addKeyListener(final BaseXCombo combo) {
    combo.addKeyListener(modeKeys);
    combo.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        if(combo.isPopupVisible()) return;
        if(BaseXKeys.NEXTLINE.is(e) || BaseXKeys.PREVLINE.is(e) ||
            BaseXKeys.NEXTPAGE.is(e) || BaseXKeys.PREVPAGE.is(e)) {
          view.list.dispatchEvent(e);
        } else {
          for(final GUIPopupCmd cmd : view.list.commands) {
            if(cmd == null) continue;
            for(final BaseXKeys sc : cmd.shortcuts()) {
              if(sc.is(e)) {
                cmd.execute(view.gui);
                e.consume();
                return;
              }
            }
          }
        }
      }
      @Override
      public void keyReleased(final KeyEvent e) {
        refresh(false);
      }
    });
  }
}
