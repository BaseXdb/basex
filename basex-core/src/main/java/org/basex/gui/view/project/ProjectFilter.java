package org.basex.gui.view.project;

import static org.basex.gui.GUIConstants.*;

import java.awt.*;
import java.awt.event.*;
import java.util.regex.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.view.editor.*;
import org.basex.io.*;

/**
 * Project filter.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class ProjectFilter extends BaseXBack {
  /** Files. */
  private final BaseXCombo filesFilter;
  /** Contents. */
  private final BaseXCombo contentsFilter;
  /** Project view. */
  private final ProjectView view;

  /** Last file search. */
  private String fileFilter = "";
  /** Last content search. */
  private String contentFilter = "";

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

    add(filesFilter, BorderLayout.NORTH);
    add(contentsFilter, BorderLayout.CENTER);

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
    if(!enforce && fileFilter.equals(files) && contentFilter.equals(contents)) return;
    fileFilter = files;
    contentFilter = contents;

    final boolean filter = !files.isEmpty() || !contents.isEmpty();
    if(filter) filter(files, contents);
    view.showList(filter);
  }

  /**
   * Finds files with the text selected in the specified editor area.
   * @param ea calling editor
   */
  void find(final EditorArea ea) {
    final String string = ea.searchString();
    if(!string.isEmpty()) {
      contentsFilter.setText(string);
      if(ea.opened()) {
        final String name = ea.file().name();
        final int i = name.lastIndexOf('.');
        final String file = filesFilter.getText();
        final String pattern = file.isEmpty() ? view.gui.gopts.get(GUIOptions.FILES) : file;
        if(i != -1 && !pattern.contains("*") && !pattern.contains("?") ||
            !Pattern.compile(IOFile.regex(pattern)).matcher(name).matches()) {
          filesFilter.setText('*' + name.substring(i));
        }
      }
      refresh(false);
    }
    contentsFilter.requestFocusInWindow();
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

    new GUIWorker<String[]>() {
      @Override
      protected String[] doInBackground() throws Exception {
        final String pattern = files.isEmpty() ? view.gui.gopts.get(GUIOptions.FILES) : files;
        return view.files.filter(pattern, contents, view.root.file);
      }
      @Override
      protected void done(final String[] list) {
        view.list.setElements(list, contents);
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
