(:~
 : Workspace: file panel, editor and query results in a single view.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/workspace';

import module namespace config = 'dba/lib/config' at '../lib/config.xqm';
import module namespace html = 'dba/lib/html' at '../lib/html.xqm';
import module namespace utils = 'dba/lib/utils' at '../lib/utils.xqm';

(:~ Top category. :)
declare variable $dba:CAT := 'workspace';

(:~
 : Workspace: file panel, editor and query results in a single view. The directory of the file
 : panel and the open documents are remembered by the client, which requests what it needs.
 : @param  $error  error string
 : @param  $info   info string
 : @return page
 :)
declare
  %rest:GET
  %rest:path('/dba/workspace')
  %rest:query-param('error', '{$error}')
  %rest:query-param('info',  '{$info}')
  %output:method('html')
function dba:workspace(
  $error  as xs:string?,
  $info   as xs:string?
) as element(html) {
  (
    (: the grid is placed explicitly: the file panel on the left, the toolbar above the editor,
       and the result beside both of them :)
    <div class='panel no-divider' style='grid-area: 1 / 2 / 2 / 3'>
      <form autocomplete='off' action='javascript:void(0);'>{
        insert-separator((
          (: files are opened in the file panel and named by their tab :)
          <button type='button' onclick='newFile()' title='Open an empty tab'>New</button>,
          <button id='save' disabled='' onclick='saveFile()'>Save</button>,
          <button id='saveas' disabled='' onclick='saveFile(true)'
                  title='Save under another name'>Save as</button>,
          <span>&#xa0;&#xa0;</span>,
          <button id='run' onclick='runQuery()' title='Ctrl-Enter'>Run</button>,
          <button id='stop' onclick='stopQuery()' disabled=''>Stop</button>,
          <button type='button' id='job' onclick='openJob(event)' disabled=''
                  title='Show the running query in the job view (Ctrl: new tab)'>Job</button>,
          <span>&#xa0;</span>,
          <label><input type='checkbox' id='indent' onchange='indentChanged()'/> Indent</label>
        ), <span>&#xa0;</span>)
      }</form>
    </div>,
    (: the client knows the directory to be shown, and fills the panel :)
    <div class='panel no-divider' style='grid-area: 1 / 1 / 3 / 2'>
      <div id='files-panel' class='pane'/>
      <div class='resizer' data-split='0'/>
    </div>,
    (: the open documents are known to the client, which draws the strip :)
    <div class='panel no-divider' style='grid-area: 2 / 2 / 3 / 3'>
      <div id='tabs' class='tabs'/>
      <textarea id='editor' autofocus='' spellcheck='false'/>
      <div class='resizer' data-split='1'/>
    </div>,
    <div class='panel no-divider' style='grid-area: 1 / 3 / 3 / 4'>
      <textarea id='output' readonly='' spellcheck='false'/>
    </div>
  ) => html:wrap({
    'header' : $dba:CAT,
    'columns': ('25fr', '38fr', '37fr'),
    'rows'   : ('auto', '1fr'),
    'scripts': ('cm6', 'editor', 'workspace'),
    'init'   : 'initWorkspace();',
    'info'   : $info,
    'error'  : $error
  })
};

(:~
 : Downloads the selected files; several files are packed into an archive.
 : @param  $names  names of files
 : @param  $dir    directory of the file panel
 : @return binary data
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
    for $name in $names[. != '..']
    let $path := utils:safe-path($dir, $name)
    where file:is-file($path)
    return $path
  return try {
    if (empty($paths)) {
      web:redirect(utils:page($dba:CAT), { 'error': 'No file was selected.' })
    } else {
      (: the archive is named after the current directory :)
      utils:download($paths, file:name(replace($dir, '[/\\]+$', ''))[.] otherwise 'files')
    }
  } catch * {
    web:redirect(utils:page($dba:CAT), { 'error': 'Download failed: ' || $err:description })
  }
};

(:~
 : Runs a file action.
 : @param  $action  name of action
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/workspace/{$action}')
function dba:action(
  $action  as xs:string
) {
  utils:dispatch($dba:CAT, $action, {
    'dir-create': fn($args) { {
      'info': utils:info($args?name, 'directory', 'created'),
      'run' : %updating fn() {
        file:create-dir(utils:safe-path(config:files-dir($args?dir), $args?name))
      }
    } },
    'delete': fn($args) { {
      'info': utils:info($args?name, 'file', 'deleted'),
      'run' : %updating fn() {
        (: delete all files, ignore reference to parent directory :)
        let $dir := config:files-dir($args?dir)
        return $args?name[. != '..'] ! file:delete(utils:safe-path($dir, .))
      }
    } },
    'upload': fn($args) {
      let $dir := config:files-dir($args?dir)
      let $files := $args?files[. instance of map(*)] otherwise {}
      return {
        'info': if (map:size($files)) { utils:info(map:keys($files), 'file', 'uploaded') },
        'run' : %updating fn() {
          (: parse all XQuery files; reject files that cannot be parsed :)
          void(
            for key $name value $content in $files
            where matches($name, $utils:XQUERY-REGEX, 'i')
            return utils:query-parse(convert:binary-to-string($content), $dir || $name)
          ),
          for key $name value $content in $files
          return file:write-binary(utils:safe-path($dir, $name), $content)
        }
      }
    }
  })
};
