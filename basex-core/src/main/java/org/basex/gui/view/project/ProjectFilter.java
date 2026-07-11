package org.basex.gui.view.project;

import static org.basex.gui.GUIConstants.*;

import java.awt.*;
import java.awt.event.*;
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
  /** Project view. */
  private final ProjectView view;

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

    add(filesFilter, BorderLayout.NORTH);
    add(contentRow, BorderLayout.CENTER);

    addKeyListener(filesFilter);
    addKeyListener(contentsFilter);
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
    }
    view.showList(filter);
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
   * Derives a files filter from a file name, matching files with the same extension.
   * @param name file name
   * @return new files pattern, or {@code null} if the current filter should be kept
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
        // highlight an invalid regular expression
        final String error = content.error();
        contentsFilter.highlight(error != null ? lightRed : backColor);
        contentsFilter.setToolTipText(error);
        final String found = list.length >= ProjectFiles.MAXHITS ?
          ">" + ProjectFiles.MAXHITS : String.valueOf(list.length);
        view.gui.status.setText(contents.isEmpty() ? Util.info(Text.FILES_FOUND_X, found) :
          Util.info(Text.FILES_FOUND_STATS_X, found, content.searched(), content.tooLarge(),
              content.binary()), true);
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
    combo.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        if(combo.isPopupVisible()) return;
        if(BaseXKeys.NEXTLINE.is(e) || BaseXKeys.PREVLINE.is(e) ||
            BaseXKeys.NEXTPAGE.is(e) || BaseXKeys.PREVPAGE.is(e)) {
          view.list.dispatchEvent(e);
        } else if(BaseXKeys.MATCHCASE.is(e)) {
          toggleMode(mcase);
          e.consume();
        } else if(BaseXKeys.WHOLEWORD.is(e)) {
          toggleMode(word);
          e.consume();
        } else if(BaseXKeys.REGEX.is(e)) {
          toggleMode(regex);
          e.consume();
        } else if(BaseXKeys.DOTALL.is(e)) {
          if(dotall.isEnabled()) toggleMode(dotall);
          e.consume();
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
