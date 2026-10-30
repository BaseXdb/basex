(:~
 : Workspace: file panel, editor and query results in a single view.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/workspace';

import module namespace config = 'dba/lib/config' at '../lib/config.xqm';
import module namespace html = 'dba/lib/html' at '../lib/html.xqm';
import module namespace utils = 'dba/lib/utils' at '../lib/utils.xqm';
import module namespace panels = 'dba/lib/panels' at '../activity/panels.xqm';

(:~ Top category. :)
declare variable $dba:CAT := 'workspace';

(:~
 : Workspace: file panel, editor and query results in a single view.
 : @param  $job       job whose details are shown
 : @param  $download  job whose result is to be downloaded
 : @return page
 :)
declare
  %rest:GET
  %rest:path('/dba/workspace')
  %rest:query-param('job',      '{$job}')
  %rest:query-param('download', '{$download}')
  %output:method('html')
function dba:workspace(
  $job       as xs:string?,
  $download  as xs:string?
) as element(html) {
  (: the directory of the file panel and the open documents are remembered by the client,
     which requests what it needs :)
  (
    (: the grid is placed explicitly: the file panel on the left, the toolbar above the editor
       and the result, and the result beside the editor. A narrow screen stacks the panels in
       source order :)
    (: the client knows the directory to be shown, and fills the panel :)
    <div class='panel' data-label='' style='grid-area: 1 / 1 / -1 / 2'>
      <h2 class='sr-only'>Files</h2>
      <div id='files-panel' class='pane'/>
      <div class='resizer' data-split='0'/>
    </div>,
    <div class='panel' style='grid-area: 1 / 2 / 2 / 4'>
      <form class='toolbar' autocomplete='off' action='javascript:void(0);'>
        <div>
          { (: files are opened in the file panel and named by their tab :) }
          <button type='button' onclick='newFile()' title='Open an empty tab (Alt+N)'>New</button>
          <button id='save' disabled='' onclick='saveFile()'
                  title='Save the file (Alt+S, Ctrl+S)'>Save</button>
          <button id='saveas' disabled='' onclick='saveFile(true)'
                  title='Save under another name (Alt+Shift+S, Ctrl+Shift+S)'>Save as…</button>
        </div>
        <div>
          <button id='run' onclick='runQuery()' title='Run the query (Alt+Enter, Ctrl+Enter)'>Run</button>
          <button id='stop' onclick='stopQuery()' disabled='' title='Stop the running query'>Stop</button>
          <button type='button' id='job' onclick='openJob(event)' disabled=''
                  title='Show the details of the running query (Ctrl: Activity view in a new tab)'>Job</button>
        </div>
        <label><input type='checkbox' id='indent' onchange='indentChanged()'/> Indent</label>
      </form>
    </div>,
    (: the open documents are known to the client, which draws the strip :)
    <div class='panel' data-label='' style='grid-area: 2 / 2 / -1 / 3'>
      <h2 class='sr-only'>Editor</h2>
      <div id='tabs' class='tabs' role='tablist' aria-label='Open documents'/>
      <textarea id='editor' autofocus='' spellcheck='false'/>
      <div class='resizer' data-split='1'/>
    </div>,
    (: two panels: the space between them can be dragged. The result is headed by a tab,
       so that its text area starts at the height of the editor :)
    <div class='panel' data-label='' style='grid-area: 2 / 3 / 3 / 4'>
      <div class='tabs'>
        <h2 class='tab active' id='result-label'>Result</h2>
        <label title='Show the information of the last query'><input type='checkbox'
          id='query-info' onchange='queryInfoChanged()'/> Query Info</label>
      </div>
      <textarea id='output' readonly='' spellcheck='false'/>
      <img id='output-preview' class='preview' alt='' hidden=''/>
      <div id='output-hint' class='hint'>Ctrl-Enter to run</div>
      <div class='resizer-row' id='info-resizer' data-split='0' hidden=''/>
    </div>,
    <div class='panel hidden' data-label='' id='info-view'
         style='grid-area: 3 / 3 / 4 / 4'>
      <div class='tabs'>
        <h2 class='tab active'>Query Info</h2>
        { (: the outcome of the last run: its time, or the position of its error :) }
        <output id='run-info'/>
      </div>
      <div id='info-panel' class='pane'/>
    </div>,
    (: the details of a job that is run here are opened over the workspace :)
    panels:details-dialog($job, $dba:CAT),
    panels:download-form($download)
  ) => html:wrap({
    'header' : $dba:CAT,
    'columns': ('25fr', '38fr', '37fr'),
    'rows'   : ('auto', '1fr', '1fr'),
    'scripts': ('cm6', 'editor', 'job', 'workspace'),
    'init'   : 'initWorkspace();'
  })
};

(:~
 : Downloads the selected files; several files are packed into an archive.
 : @param  $names  names of files
 : @param  $dir    directory of the file panel
 : @return rest response and file content, or redirection
 :)
declare
  %rest:POST
  %rest:path('/dba/files-download')
  %rest:form-param('name', '{$names}')
  %rest:form-param('dir',  '{$dir}')
function dba:files-download(
  $names  as xs:string*,
  $dir    as xs:string?
) as item()+ {
  let $dir := config:files-dir($dir)
  let $paths :=
    for $name in $names
    let $path := utils:safe-path($dir, $name)
    where file:is-file($path)
    return $path
  return try {
    if (empty($paths)) then (
      utils:outcome($dba:CAT, {}, { 'error': 'No file was selected.' })
    ) else (
      (: the archive is named after the current directory :)
      utils:download($paths, file:name(replace($dir, '[/\\]+$', ''))[.] otherwise 'files')
    )
  } catch * {
    utils:outcome($dba:CAT, {}, { 'error': 'Download failed: ' || $err:description })
  }
};

(:~
 : Runs a query file as job and shows its details.
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/workspace/run')
function dba:run() {
  utils:dispatch($dba:CAT, fn($args) {
    (: a file is run by one job at a time, which is named by its path :)
    let $id := utils:file-path($args?dir, $args?run)
    return {
      'params': { 'job': $id },
      (: a job that was not started is not shown :)
      'failed': {},
      'run'   : %updating fn() {
        (: the result of an earlier run is discarded :)
        if (job:list-details($id)/@state = 'cached') { job:remove($id) },
        (: the result is cached: it can be read in the details of the job :)
        void(job:eval(file:read-text($id), (), {
          config:limits(), 'cache': true(), 'base-uri': file:path-to-uri($id), 'id': $id
        }))
      }
    }
  })
};

(:~
 : Creates a directory.
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/workspace/dir-create')
function dba:dir-create() {
  utils:dispatch($dba:CAT, fn($args) { {
    'info': utils:info($args?name, 'directory', 'created'),
    'run' : %updating fn() {
      file:create-dir(utils:file-path($args?dir, $args?name))
    }
  } })
};

(:~
 : Renames a file or directory.
 : @param  $name  name of file or directory
 : @param  $to    new name
 : @param  $dir   directory of the file panel
 : @return empty response
 :)
declare
  %rest:POST
  %rest:path('/dba/workspace/rename')
  %rest:query-param('name', '{$name}')
  %rest:query-param('to',   '{$to}')
  %rest:query-param('dir',  '{$dir}')
function dba:rename(
  $name  as xs:string,
  $to    as xs:string,
  $dir   as xs:string?
) as xs:string {
  let $target := utils:file-path($dir, $to)
  return switch () {
    (: an entry stays in its directory :)
    case matches($to, '[/\\]') return web:error(400, `Invalid name: { $to }`)
    case file:exists($target) return web:error(409, `“{ $to }” already exists.`)
    default return (file:move(utils:file-path($dir, $name), $target), '')
  }
};

(:~
 : Deletes files.
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/workspace/delete')
function dba:delete() {
  utils:dispatch($dba:CAT, fn($args) {
    let $paths := $args?name ! utils:file-path($args?dir, .)
    (: the message names what was deleted: files, directories, or entries of both kinds :)
    let $dirs := count($paths[file:is-dir(.)])
    let $noun := switch () {
      case $dirs = 0 return 'file'
      case $dirs = count($paths) return 'directory'
      default return 'entry'
    }
    return {
      'info': utils:info($args?name, $noun, 'deleted'),
      'run' : %updating fn() { $paths ! file:delete(.) }
    }
  })
};

(:~
 : Uploads files.
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/workspace/upload')
function dba:upload() {
  utils:dispatch($dba:CAT, fn($args) {
    let $dir := config:files-dir($args?dir)
    let $files := utils:files($args?files)
    return {
      'info': if (map:size($files)) { utils:info(map:keys($files), 'file', 'uploaded') },
      'run' : %updating fn() {
        (: parse all XQuery files; reject files that cannot be parsed :)
        void(
          for key $name value $content in $files
          where matches($name, $utils:XQUERY-REGEX, 'i')
          return utils:query-parse(bin:decode-string($content), $dir || $name)
        ),
        for key $name value $content in $files
        return file:write-binary(utils:safe-path($dir, $name), $content)
      }
    }
  })
};
