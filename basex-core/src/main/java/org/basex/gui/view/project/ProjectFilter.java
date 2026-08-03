package org.basex.gui.view.project;

import static org.basex.gui.GUIConstants.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.regex.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.dialog.*;
import org.basex.gui.layout.*;
import org.basex.gui.text.*;
import org.basex.gui.view.editor.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Project filter.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class ProjectFilter extends BaseXBack {
  /** Number of hit counts that are sent to the project list at a time. */
  private static final int BATCH = 25;

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
  /** ID of the most recent hit count (invalidates older ones). */
  private volatile int countId;

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
    contentsFilter.hint(Text.FIND_CONTENTS + Text.ELLIPSIS);
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

    // restore the search modes of the last session
    final GUIOptions gopts = view.gui.gopts;
    mcase.setSelected(gopts.get(GUIOptions.MATCHCASE));
    word.setSelected(gopts.get(GUIOptions.WHOLEWORD));
    regex.setSelected(gopts.get(GUIOptions.REGEX));
    dotall.setSelected(gopts.get(GUIOptions.DOTALL));
    dotall.setEnabled(regex.isSelected());

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
    replace.hint(Text.REPLACE_WITH + Text.ELLIPSIS);
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

    addListeners(filesFilter);
    addListeners(contentsFilter);
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
      ++countId;
    }
    view.showList(filter || replaceRow.isVisible());
  }

  /**
   * Finds files with the text selected in the specified editor area.
   * @param ea calling editor
   */
  void find(final EditorArea ea) {
    final SearchBar bar = ea.getSearch();
    final String selection = ea.searchString();
    // a running search is continued, unless another text was selected afterwards
    final boolean adopt = bar != null && bar.adopts(selection);

    final String string = adopt ? bar.searchString() : selection;
    if(!string.isEmpty()) {
      if(adopt) {
        // adopt the search flags of the editor
        final SearchFlags flags = bar.flags();
        mcase.setSelected(flags.mcase());
        word.setSelected(flags.word());
        regex.setSelected(flags.regex());
        dotall.setSelected(flags.dotall());
      } else {
        // reset the search flags: the selected text is searched literally
        mcase.setSelected(false);
        word.setSelected(false);
        regex.setSelected(false);
        dotall.setSelected(false);
      }
      dotall.setEnabled(regex.isSelected());
      modes();
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
   * Returns the effective files filter.
   * @param files files filter (empty string for the default filter)
   * @return files filter
   */
  private String filesQuery(final String files) {
    return files.isEmpty() ? view.gui.gopts.get(GUIOptions.FILES) : files;
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
    final String pattern = filesQuery(filesFilter.getText());
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

    // a selection of multiple files restricts the replacement; otherwise, the displayed list
    // can be adopted if it is complete
    final List<IOFile> selected = view.list.selectedFiles();
    final List<IOFile> known = selected.size() > 1 ? selected :
      view.list.complete() ? view.list.allFiles() : null;

    // an uncounted or too large file invalidates the hits of the list
    final int hits = known != null ? view.list.hits(known) : -1;

    final List<IOFile> targets;
    final int strings;
    if(hits >= 0) {
      // all files to be changed and all their hits are known
      targets = known;
      strings = hits;
    } else {
      final FilterJob filterJob = known != null ? new FilterJob(known, pattern) :
        new FilterJob(view.files, filesQuery(filesFilter.getText()),
          view.files.contentFilter(contents, mcase.isSelected(), word.isSelected(), rgx,
              dotall.isSelected()), view.root.file, pattern);
      DialogProgress.execute(view.gui, filterJob);
      targets = filterJob.result();
      strings = filterJob.strings();
    }

    if(targets.isEmpty()) return;
    final DialogReplace dialog = new DialogReplace(view.gui, strings, targets.size(), backupDir);
    if(!dialog.ok()) return;

    final String in = replace.getText();
    final String replacement = rgx ? SearchContext.normalize(in) : Matcher.quoteReplacement(in);
    replace.updateHistory();

    // save open editors so the on-disk replacement sees their current content
    view.gui.editor.saveAll();
    discardBackups();

    final boolean backup = dialog.backup();
    final ReplaceJob job = new ReplaceJob(targets, view.root.file, pattern, replacement,
        backup ? backupDir : null);
    DialogProgress.execute(view.gui, job);

    for(final IOFile file : job.changed()) {
      final EditorArea ea = view.gui.editor.editor(file);
      if(ea != null) ea.reopen(true);
    }
    final RuntimeException error = job.error();
    replace.highlight(error != null ? lightRed : backColor);
    replace.setToolTipText(error != null ? error.getLocalizedMessage() : null);
    // backups of a canceled or failed replacement are kept: they are the only way back
    undo.setEnabled(backup && !job.changed().isEmpty());
    report(Util.info(Text.STRINGS_REPLACED_X, BaseXLayout.format(job.count())), job.skipped());
    view.refresh();
  }

  /**
   * Undoes the last replacement.
   */
  private void undoReplace() {
    final StringList backups = backupDir.descendants();
    if(!new DialogRevert(view.gui, backups.size()).ok()) return;

    final IOFile root = view.root.file;
    int reverted = 0, skipped = 0;
    for(final String rel : backups) {
      final IOFile file = new IOFile(root, rel);
      try {
        new IOFile(backupDir, rel).moveTo(file);
        reverted++;
      } catch(final IOException ex) {
        // file may not be writable
        Util.debug(ex);
        skipped++;
      }
      final EditorArea ea = view.gui.editor.editor(file);
      if(ea != null) ea.reopen(true);
    }
    discardBackups();
    report(Util.info(Text.FILES_REVERTED_X, BaseXLayout.format(reverted)), skipped);
    view.refresh();
  }

  /**
   * Reports files that could not be processed. A successful run is reflected by the refreshed
   * file list and needs no dialog.
   * @param message result message
   * @param skipped number of skipped files
   */
  private void report(final String message, final int skipped) {
    if(skipped > 0) BaseXDialog.info(view.gui, message + ", " +
        Util.info(Text.FILES_SKIPPED_X, BaseXLayout.format(skipped)));
  }

  /**
   * Discards the pending replacement backup.
   */
  void discardBackups() {
    backupDir.delete();
    undo.setEnabled(false);
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
    modes();
    refresh(true);
  }

  /**
   * Adopts the current search modes and remembers them for the next session.
   */
  private void modes() {
    final GUIOptions gopts = view.gui.gopts;
    gopts.set(GUIOptions.MATCHCASE, mcase.isSelected());
    gopts.set(GUIOptions.WHOLEWORD, word.isSelected());
    gopts.set(GUIOptions.REGEX, regex.isSelected());
    gopts.set(GUIOptions.DOTALL, dotall.isSelected());
  }

  /**
   * Called when the GUI design has changed.
   */
  void refreshLayout() {
    final String filter = view.gui.gopts.get(GUIOptions.FILES).trim();
    filesFilter.hint(filter.isEmpty() ? Text.FIND_FILES + Text.ELLIPSIS : filter);
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
        return view.files.filter(filesQuery(files), content, view.root.file);
      }

      @Override
      protected void done(final String[] list) {
        final int max = view.gui.gopts.get(GUIOptions.MAXHITS);
        view.list.setElements(list, contents, list.length < max);
        countHits(list, contents);
        final String error = content.error();
        contentsFilter.highlight(error != null ? lightRed : backColor);
        contentsFilter.setToolTipText(error);
        final String found = list.length >= max ? ">" + max : String.valueOf(list.length);
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
   * Counts the content hits of all listed files in the background and shows them in the list.
   * @param list listed file paths
   * @param contents contents search string
   */
  private void countHits(final String[] list, final String contents) {
    // a new count invalidates the results of the previous one
    final int id = ++countId;
    if(contents.isEmpty() || list.length == 0) return;

    final Pattern pattern;
    try {
      pattern = SearchContext.pattern(contents, mcase.isSelected(), word.isSelected(),
          regex.isSelected(), dotall.isSelected());
    } catch(final PatternSyntaxException ex) {
      Util.debug(ex);
      return;
    }

    final Thread thread = new Thread(() -> {
      // wait until the search has settled: do not count the hits of every keystroke
      Performance.sleep(300);
      final Map<String, Integer> batch = new HashMap<>();
      for(final String path : list) {
        if(id != countId) return;
        // unreadable and large files are counted as unknown: the count is complete either way
        // (a streaming search also lists files that are too large to be scanned after each key)
        final IOFile file = new IOFile(path);
        batch.put(file.path(), file.length() > ProjectFiles.MAXBYTES ? -1 :
          ProjectFiles.count(file, pattern));
        if(batch.size() == BATCH) publish(id, batch);
      }
      publish(id, batch);
    });
    // the count must not keep the JVM alive
    thread.setDaemon(true);
    thread.start();
  }

  /**
   * Sends counted hits to the project list. Results are batched: a single repaint is
   * requested for many files.
   * @param id ID of the count
   * @param batch counted hits (will be cleared)
   */
  private void publish(final int id, final Map<String, Integer> batch) {
    if(batch.isEmpty()) return;
    final Map<String, Integer> hits = new HashMap<>(batch);
    batch.clear();
    EventQueue.invokeLater(() -> {
      if(id == countId) view.list.count(hits);
    });
  }

  /**
   * Adds the filter listeners to the specified combo box.
   * @param combo combo box
   */
  private void addListeners(final BaseXCombo combo) {
    combo.addKeyListener(modeKeys);
    // catch all changes of the filter string, including those caused by mouse interactions
    combo.onChange(() -> refresh(false));
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
    });
  }
}
