package org.basex.gui.view.project;

import static org.basex.core.Text.*;

import java.io.*;
import java.util.*;
import java.util.List;

import org.basex.core.cmd.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * Project commands.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
interface ProjectCommands {
  /**
   * Returns the project view.
   * @return project view
   */
  ProjectView view();

  /**
   * Returns the current search string.
   * @return search string
   */
  String search();

  /**
   * Returns the selected file, or {@code null} if zero or more than one value is selected.
   * @return selected file
   */
  IOFile selectedFile();

  /**
   * Returns all selected files.
   * @return selected files
   */
  List<IOFile> selectedFiles();

  /**
   * Refreshes the current node and its children.
   */
  void refresh();

  /**
   * Returns the popup menu commands.
   * @param edit commands for editing files
   * @return commands
   */
  default GUIPopupCmd[] commands(final GUIPopupCmd... edit) {
    final ArrayList<GUIPopupCmd> commands = new ArrayList<>();

    commands.add(new GUIPopupCmd(OPEN, BaseXKeys.ENTER) {
      @Override public void execute() {
        for(final IOFile file : selectedFiles()) view().open(file, search());
      }
      @Override public boolean enabled(final GUI main) {
        return ((Checks<IOFile>) file -> !file.isDir()).all(selectedFiles());
      }
    });
    commands.add(new GUIPopupCmd(OPEN_EXTERNALLY, BaseXKeys.SHIFT_ENTER) {
      @Override public void execute() {
        for(final IOFile file : selectedFiles()) {
          try {
            file.open();
          } catch(final IOException ex) {
            Util.debug(ex);
            BaseXDialog.error(view().gui, Util.info(FILE_NOT_OPENED_X, file));
          }
        }
      }
      @Override public boolean enabled(final GUI main) {
        return selectedFile() != null;
      }
    });
    commands.add(new GUIPopupCmd(RUN_TESTS, BaseXKeys.UNIT) {
      @Override public void execute() {
        view().gui.execute(new Test(selectedFile().path()));
      }
      @Override public boolean enabled(final GUI main) {
        final IOFile file = selectedFile();
        return file != null && (file.isDir() || file.hasSuffix(IO.XQSUFFIXES));
      }
    });
    commands.add(new GUIPopupCmd(SET_CONTEXT) {
      @Override public void execute() {
        view().gui.editor.setContext(selectedFile());
      }
      @Override public boolean enabled(final GUI main) {
        final IOFile file = selectedFile();
        return file != null && !file.isDir() && file.hasSuffix(view().gui.gopts.xmlSuffixes());
      }
    });
    commands.add(null);

    commands.addAll(Arrays.asList(edit));

    commands.add(new GUIPopupCmd(REFRESH, BaseXKeys.REFRESH) {
      @Override public void execute() {
        refresh();
      }

      @Override public boolean enabled(final GUI main) {
        return selectedFile() != null;
      }
    });
    commands.add(null);

    commands.add(new GUIPopupCmd(COPY_PATH, BaseXKeys.COPYPATH) {
      @Override public void execute() {
        BaseXLayout.copyPath(selectedFile().path());
      }

      @Override public boolean enabled(final GUI main) {
        return selectedFile() != null;
      }
    });
    return commands.toArray(new GUIPopupCmd[0]);
  }
}
