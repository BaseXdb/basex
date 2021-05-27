package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.layout.BaseXFileChooser.*;
import org.basex.io.*;
import org.basex.query.util.pkg.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Open database dialog.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DialogPackages extends BaseXDialog {
  /** List of available packages. */
  private final BaseXList packages;
  /** Title of package. */
  private final BaseXLabel title;
  /** Install from button. */
  private final BaseXButton installURL;
  /** Install button. */
  private final BaseXButton install;
  /** Delete button. */
  private final BaseXButton delete;

  /** Name label. */
  private final BaseXLabel name;
  /** Version label. */
  private final BaseXLabel version;
  /** Type label. */
  private final BaseXLabel type;
  /** Path label. */
  private final BaseXLabel path;

  /** Refresh list of databases. */
  private boolean refresh;

  /**
   * Default constructor.
   * @param gui reference to the main window
   */
  public DialogPackages(final GUI gui) {
    super(gui, PACKAGES);
    panel.setLayout(new BorderLayout(8, 0));

    // create package chooser
    packages = new BaseXList(this, false);
    packages.setSize(400, 280);

    title = new BaseXLabel(" ").large().border(0, 5, 5, 0);
    name = new BaseXLabel(" ");
    version = new BaseXLabel(" ");
    type = new BaseXLabel(" ");
    path = new BaseXLabel(" ");

    final BaseXBack table = new BaseXBack(new TableLayout(4, 2, 16, 0)).border(5);
    table.add(new BaseXLabel(NAME + COL, false, true));
    table.add(name);
    table.add(new BaseXLabel(VERSINFO + COL, false, true));
    table.add(version);
    table.add(new BaseXLabel(TYPE + COL, false, true));
    table.add(type);
    table.add(new BaseXLabel(PATH + COL, false, true));
    table.add(path);

    // database buttons
    installURL = new BaseXButton(this, INSTALL_FROM_URL + DOTS);
    install = new BaseXButton(this, INSTALL + DOTS);
    delete = new BaseXButton(this, DELETE + DOTS);

    BaseXBack p = new BaseXBack(new BorderLayout());
    p.add(packages, BorderLayout.CENTER);
    final BaseXBack ss = new BaseXBack(new ColumnLayout(8)).border(8, 0, 0, 0);
    ss.add(new BaseXLabel(PATH + COL, true, true), BorderLayout.NORTH);
    ss.add(new BaseXLabel(gui.context.soptions.get(StaticOptions.REPOPATH)));
    p.add(ss, BorderLayout.SOUTH);
    set(p, BorderLayout.CENTER);

    p = new BaseXBack(new BorderLayout());
    p.add(title, BorderLayout.NORTH);
    p.add(table, BorderLayout.CENTER);
    p.add(newButtons(installURL, install, delete), BorderLayout.SOUTH);
    BaseXLayout.setWidth(p, 430);
    set(p, BorderLayout.EAST);

    refresh = true;
    action(null);
    finish();
  }

  @Override
  public void action(final Object cmp) {
    final Context ctx = gui.context;
    if(refresh) {
      // rebuild databases and focus list chooser
      packages.setData(new RepoManager(ctx).ids().finish());
      packages.requestFocusInWindow();
      refresh = false;
    }

    final StringList pkgs = packages.getValues();
    final ArrayList<Command> cmds = new ArrayList<>();

    if(cmp == installURL) {
      final DialogInstallURL dialog = new DialogInstallURL(this);
      if(!dialog.ok()) return;
      refresh = true;
      cmds.add(new RepoInstall(dialog.url(), null));

    } else if(cmp == install) {
      final BaseXFileChooser fc = new BaseXFileChooser(this, FILE_OR_DIR,
          gui.gopts.get(GUIOptions.WORKPATH));
      fc.filter(XML_ARCHIVES, false, IO.XARSUFFIX);
      fc.filter(JAVA_ARCHIVES, false, IO.JARSUFFIX);
      fc.filter(XQUERY_FILES, false, IO.XQSUFFIXES);
      final IOFile file = fc.select(Mode.FDOPEN);
      if(file == null) return;
      gui.gopts.setFile(GUIOptions.WORKPATH, file);
      refresh = true;
      cmds.add(new RepoInstall(file.path(), null));

    } else if(cmp == delete) {
      if(!BaseXDialog.confirm(gui, Util.info(DELETE_PACKAGES_X, pkgs.size()))) return;
      refresh = true;
      for(final String pkg : pkgs) cmds.add(new RepoDelete(pkg, null));

    } else {
      final String key = packages.getValue();
      for(final Pkg pkg : new RepoManager(ctx).packages()) {
        if(pkg.id().equals(key)) {
          title.setText(key);
          name.setText(pkg.name());
          version.setText(pkg.version());
          type.setText(pkg.type().toString());
          path.setText(pkg.path());
          break;
        }
      }
      // enable or disable buttons
      delete.setEnabled(!pkgs.isEmpty());
    }

    // run all commands
    if(!cmds.isEmpty()) {
      DialogProgress.execute(this, cmds.toArray(new Command[0]));
    }
  }
}