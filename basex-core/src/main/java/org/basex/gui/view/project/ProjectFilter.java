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
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
final class ProjectFilter extends BaseXBack {
  /** Files. */
  private final BaseXTextField filesFilter;
  /** Contents. */
  private final BaseXTextField contentsFilter;
  /** Project view. */
  private final ProjectView project;

  /** Last file search. */
  private String fileFilter = "";
  /** Last content search. */
  private String contentFilter = "";

  /**
   * Constructor.
   * @param project project view
   */
  ProjectFilter(final ProjectView project) {
    this.project = project;

    layout(new BorderLayout(0, 2));
    filesFilter = new BaseXTextField(project.gui);
    filesFilter.addFocusListener(project.lastfocus);

    contentsFilter = new BaseXTextField(project.gui);
    contentsFilter.hint(Text.FIND_CONTENTS + Text.DOTS);
    contentsFilter.addFocusListener(project.lastfocus);

    add(filesFilter, BorderLayout.NORTH);
    add(contentsFilter, BorderLayout.CENTER);

    final KeyAdapter refreshKeys = new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        if(BaseXKeys.NEXTLINE.is(e) || BaseXKeys.PREVLINE.is(e) ||
           BaseXKeys.NEXTPAGE.is(e) || BaseXKeys.PREVPAGE.is(e)) {
          project.list.dispatchEvent(e);
        } else {
          for(final GUIPopupCmd cmd : project.list.commands) {
            if(cmd == null) continue;
            for(final BaseXKeys sc : cmd.shortcuts()) {
              if(sc.is(e)) {
                cmd.execute(project.gui);
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
    };
    filesFilter.addKeyListener(refreshKeys);
    contentsFilter.addKeyListener(refreshKeys);
    refreshLayout();
  }

  /**
   * Refreshes the filter view.
   * @param force force refresh
   */
  void refresh(final boolean force) {
    final String file = filesFilter.getText();
    final String content = contentsFilter.getText();
    if(!force && fileFilter.equals(file) && contentFilter.equals(content)) return;
    fileFilter = file;
    contentFilter = content;

    final boolean filter = !file.isEmpty() || !content.isEmpty();
    if(filter) filter(file, content);
    project.showList(filter);
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
        final String pattern = file.isEmpty() ? project.gui.gopts.get(GUIOptions.FILES) : file;
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
    final String filter = project.gui.gopts.get(GUIOptions.FILES).trim();
    filesFilter.hint(filter.isEmpty() ? Text.FIND_FILES + Text.DOTS : filter);
  }

  /**
   * Filters the entries.
   * @param file file search string
   * @param content content search string
   */
  private void filter(final String file, final String content) {
    filesFilter.setCursor(CURSORWAIT);
    contentsFilter.setCursor(CURSORWAIT);
    project.list.setCursor(CURSORWAIT);

    new GUIWorker<String[]>() {
      @Override
      protected String[] doInBackground() throws Exception {
        final String pattern = file.isEmpty() ? project.gui.gopts.get(GUIOptions.FILES) : file;
        return project.files.filter(pattern, content, project.root.file);
      }
      @Override
      protected void done(final String[] files) {
        project.list.setElements(files, content);
        filesFilter.setCursor(CURSORTEXT);
        contentsFilter.setCursor(CURSORTEXT);
        project.list.setCursor(CURSORARROW);
      }
    }.execute();
  }
}
