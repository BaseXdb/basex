(:~
 : File panel of the Workspace view.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace panel = 'dba/lib/file-panel';

import module namespace config = 'dba/lib/config' at '../lib/config.xqm';
import module namespace form = 'dba/lib/form' at '../lib/form.xqm';
import module namespace html = 'dba/lib/html' at '../lib/html.xqm';
import module namespace table = 'dba/lib/table' at '../lib/table.xqm';

(:~ Page the deep links of the panel refer to. :)
declare %private variable $panel:CAT := 'workspace';

(:~
 : Creates the contents of the file panel.
 : @param  $sort  sort key of the file list
 : @param  $dir   directory to be shown
 : @return directory chooser and file list
 :)
declare function panel:files(
  $sort  as xs:string,
  $dir   as xs:string?
) as element()+ {
  let $dir := config:files-dir($dir)
  (: the parent directory is reached via a button, not via a table row :)
  let $parent := file:parent($dir)
  return (
    <form method='post' autocomplete='off' data-sort='{ $sort }'>{
    <input type='hidden' name='dir' value='{ $dir }'/>,
    let $headers := (
      { 'key': 'name', 'label': 'Name', 'type': 'dynamic', 'width': '45%' },
      (: the size never grows beyond four digits and a unit :)
      { 'key': 'size', 'label': 'Size', 'type': 'bytes', 'order': 'desc', 'width': '17%' },
      { 'key': 'date', 'label': 'Date', 'type': 'dateTime', 'order': 'desc', 'width': '38%' }
    )
    let $entries := (
      let $limit := config:get($config:MAXCHARS)
      for $file in file:children($dir)
      let $is-dir := file:is-dir($file)
      let $name := file:name($file)
      order by $is-dir descending, $name collation '?lang=en'

      (: skip files without access permissions :)
      for $modified in try { file:last-modified($file) } catch * { }
      let $size := file:size($file)
      return {
        (: directories are entered and files are opened in place; both references stay
           deep links, which name the directory in full :)
        'name': fn() {
          if ($is-dir) {
            html:action($name, 'enterDir', { 'name': $name },
              { 'href': web:create-url($panel:CAT, { 'dir': $dir || $name }) })
          } else if ($size <= $limit) {
            html:action($name, 'openFile', { 'name': $name },
              { 'href': web:create-url($panel:CAT, { 'dir': $dir, 'name': $name }) })
          } else {
            $name
          }
        },
        'date': $modified,
        'size': $size
      }
    )
    (: the chooser shares the row of the buttons, so that one block can be pinned; it is not
       wide enough to fill the line on its own, so the break keeps the buttons underneath :)
    let $buttons := (
      form:directory($dir),
      <div class='break'/>,
      <button type='button' onclick='enterDir("..")' title='Go to the parent directory'>{
        attribute disabled { }[not($parent)], '..'
      }</button>,
      <button type='button' onclick='createDir()'>New…</button>,
      form:button('workspace/delete', 'Delete', ('CHECK', 'CONFIRM')),
      form:button('files-download', 'Download', 'CHECK'),
      <button type='button' onclick='chooseUpload("upload")'>Upload…</button>
    )
    (: the entries are sorted before they are truncated, so the order covers every file :)
    let $options := {
      'sort': $sort, 'presort': 'name',
      (: a directory is listed as a whole; its files are not spread over pages :)
      'all': true(),
      (: the panel scrolls as a whole, so its actions are pinned to the top of it :)
      'sticky': ()
    }
    return table:create($headers, $entries, $buttons, {}, $options) update {
      (: sort links refresh the panel instead of reloading the page :)
      for $link in descendant::th/a
      return insert node attribute onclick {
        'refreshFiles(new URLSearchParams(this.search).get("sort")); return false;'
      } into $link
    }
    }</form>,

    (: the file chooser is opened by the Upload button and submits what it collects :)
    form:upload('workspace/upload', 'upload', true(),
      <input type='hidden' name='dir' value='{ $dir }'/>),

    (: the New Dir button asks for a name and submits it :)
    form:prompt('dir-name', 'name', 'workspace/dir-create',
      <input type='hidden' name='dir' value='{ $dir }'/>)
  )
};
