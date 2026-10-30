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
import module namespace utils = 'dba/lib/utils' at '../lib/utils.xqm';

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
  (: a remembered directory that is gone falls back to the default; the client adopts it :)
  let $dir := try { config:files-dir($dir) } catch file:* { config:files-dir(()) }
  (: the parent directory is reached via a button, not via a table row :)
  let $parent := file:parent($dir)
  return (
    <form method='post' autocomplete='off' data-sort='{ $sort }'>{
    <input type='hidden' name='dir' value='{ $dir }'/>,
    (: a job that is stopped returns to the workspace :)
    <input type='hidden' name='page' value='{ $panel:CAT }'/>,
    let $headers := (
      { 'key': 'name', 'label': 'Name' },
      (: the size never grows beyond four digits and a unit, the short date beyond eight digits
         and three narrow separators; the name takes the rest :)
      { 'key': 'size', 'label': 'Size', 'type': 'bytes', 'order': 'desc',
        'width': 'calc(9ch + 0.5rem)' },
      { 'key': 'date', 'label': 'Date', 'type': 'dateTime', 'order': 'desc',
        'width': 'calc(9.5ch + 0.5rem)' },
      { 'key': 'run', 'label': '', 'width': 'calc(2ch + 1rem)' }
    )
    let $entries := (
      let $limit := config:get($config:MAXCHARS)
      (: the jobs that have not ended yet; the job of a file is named by its path :)
      let $running := job:list()[not(job:finished(.))]
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
          if ($is-dir) then (
            html:action($name, 'enterDir', { 'name': $name },
              { 'href': web:create-url($panel:CAT, { 'dir': $dir || $name }) })
          ) else if ($size <= $limit) then (
            html:action($name, 'openFile', { 'name': $name },
              { 'href': web:create-url($panel:CAT, { 'dir': $dir, 'name': $name }) })
          ) else (
            <span class='too-large' title='Too large to be opened; download it instead'>{
              $name
            }</span>
          )
        },
        'date': $modified,
        'size': $size[not($is-dir)],
        (: a query file is run as job, whose details are shown then; a running job is stopped :)
        'run': fn() {
          switch () {
            case $is-dir or not(matches($name, $utils:XQUERY-REGEX, 'i')) return ()
            case $file = $running return
              <button class='run stop' formaction='jobs/remove' name='id' value='{ $file }'
                      title='Stop the job' aria-label='Stop { $name }'>□</button>
            default return
              <button class='run' formaction='workspace/run' name='run' value='{ $name }'
                      title='Run the query as job and show its details'
                      aria-label='Run { $name }'>▷</button>
          }
        }
      }
    )
    (: the chooser shares the block of the buttons, so that one block can be pinned; the
       buttons get a line of their own, which does not wrap, like the toolbar beside it :)
    let $buttons := (
      form:directory($dir),
      <div class='row'>{
        <button type='button' onclick='enterDir("..")' title='Go to the parent directory'>{
          attribute disabled { }[not($parent)], '..'
        }</button>,
        <button type='button' onclick='showDialog("dir")' title='Create a new directory'>New…</button>,
        <button type='button' onclick='renameEntry()' data-check='check'
                title='Rename the ticked file or directory (F2)'>Rename</button>,
        form:button('workspace/delete', 'Delete', ('CHECK', 'CONFIRM'),
          title := 'Delete the selected files and empty directories'),
        form:button('files-download', 'Download', 'CHECK',
          title := 'Download the selected files'),
        <button type='button' onclick='chooseUpload("upload")'
                title='Upload files to this directory'>Upload…</button>
      }</div>
    )
    (: the entries are sorted before they are truncated, so the order covers every file :)
    let $options := {
      'sort': $sort, 'presort': 'name', 'noun': 'file',
      (: a directory is listed as a whole; its files are not spread over pages :)
      'all': true(),
      (: the panel scrolls as a whole, so its actions are pinned to the top of it :)
      'pinned': true(),
      'empty': 'The directory is empty.'
    }
    return table:create($headers, $entries, $buttons, options := $options)
    }</form>,

    (: the file chooser is opened by the Upload button and submits what it collects :)
    form:upload('workspace/upload', 'upload', true(),
      <input type='hidden' name='dir' value='{ $dir }'/>),

    (: the New button asks for a directory name and submits it :)
    form:dialog('dir', 'New Directory', 'Create', 'workspace/dir-create', false(), (
      <input type='hidden' name='dir' value='{ $dir }'/>,
      form:field('Name:', <input type='text' name='name' autofocus='' required=''/>)
    ))
  )
};
