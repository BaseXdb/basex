(:~
 : Files.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/files';

import module namespace config = 'dba/config' at '../lib/config.xqm';
import module namespace html = 'dba/html' at '../lib/html.xqm';
import module namespace utils = 'dba/utils' at '../lib/utils.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'files';

(:~
 : Files.
 : @param  $sort   table sort key
 : @param  $error  error message
 : @param  $info   info message
 : @param  $page   current page
 : @return page
 :)
declare
  %rest:GET
  %rest:path('/dba/files')
  %rest:query-param('sort',  '{$sort}', '')
  %rest:query-param('error', '{$error}')
  %rest:query-param('info',  '{$info}')
  %rest:query-param('page',  '{$page}', '1')
  %output:method('html')
function dba:files(
  $sort   as xs:string,
  $error  as xs:string?,
  $info   as xs:string?,
  $page   as xs:string
) as element(html) {
  let $dir := config:files-dir()
  return (
    <div class='panel'>
      <h2>Directory</h2>
      <form action='files/dir-change' method='get' autocomplete='off'>
        <button type='button' class='right' onclick='copy(this.form.dir.value)'>Copy path</button>
        <select name='dir' style='width: 350px;' onchange='this.form.submit();'>{
          let $dir-path := fn($path) {
            try {
              file:path-to-native($path)
            } catch file:* { }
          }
          let $webapp := $dir-path(db:option('webpath'))[.]
          let $options := (
            [ 'DBA'       , $config:DBA-DIR ],
            [ 'Webapp'    , $webapp ],
            [ 'RESTXQ'    , $dir-path($webapp ! file:resolve-path(db:option('restxqpath'), .)) ],
            [ 'Repository', $dir-path(db:option('repopath')) ],
            [ 'Home'      , Q{org.basex.util.Prop}HOMEDIR() ],
            [ 'Working'   , file:current-dir() ],
            [ 'Temporary' , file:temp-dir() ],
            file:list-roots() ! [ 'Root', string(.) ],
            [ 'Current'   , $dir ]
          )
          let $selected := head(
            for $option at $pos in $options
            where $option(2) = $dir
            return $pos
          )
          for $option at $pos in $options
          let $name := $option(1), $path := $option(2)
          where $path
          return element option {
            attribute value { $path },
            attribute selected { }[$pos = $selected],
            $path[.] ! (($name || ': ')[$name] || .)
          }
        }</select>
      </form>
      <p/>

      <form method='post' autocomplete='off'>{
        let $headers := (
          { 'key': 'name', 'label': 'Name', 'type': 'dynamic' },
          { 'key': 'date', 'label': 'Date', 'type': 'dateTime', 'order': 'desc' },
          { 'key': 'bytes', 'label': 'Bytes', 'type': 'bytes', 'order': 'desc' },
          { 'key': 'action', 'label': 'Action', 'type': 'dynamic' }
        )
        let $entries := (
          let $limit := config:get($config:MAXCHARS)
          let $jobs := job:list-details()
          let $parent := if (file:parent($dir)) { $dir || '..' }
          for $file in ($parent, file:children($dir))
          let $dir := file:is-dir($file)
          let $name := file:name($file)
          order by $dir descending, $name != '..', $name collation '?lang=en'

          (: skip files without access permissions :)
          for $modified in try { file:last-modified($file) } catch * { }
          let $size := file:size($file)
          return {
            'name': fn() {
              if ($dir) then html:link($name, 'files/dir-change', { 'dir': $name }) else $name
            },
            'date': $modified,
            'bytes': $size,
            'action': fn() {
              insert-separator(
                if (not($dir)) {
                  html:link('Download', 'file/' || encode-for-uri($name)),
                  if ($size <= $limit) {
                    html:link('Edit', 'editor', { 'name': $name })
                  },
                  if (matches($name, '\.xq$')) {
                    (: choose first running job :)
                    let $job := head(
                      let $uri := replace(file:path-to-uri($file), '^file:/*', '')
                      return $jobs[replace(., '^file:/*', '') = $uri]
                    )
                    let $id := string($job/@id)
                    return if (empty($job)) {
                      html:link('Start', 'files/start', { 'file': $name })
                    } else {
                      html:link('Job', 'jobs', { 'job': $id })
                    }
                  }
                }
              , ' · ')
            }
          }
        )
        let $buttons := html:button('files/delete', 'Delete', ('CHECK', 'CONFIRM'))
        let $options := { 'sort': $sort, 'page': xs:integer($page) }
        return html:table($headers, $entries, $buttons, {}, $options)
      }</form>

      <h3>Create Directory</h3>
      <form method='post' autocomplete='off'>{
        <input type='text' name='name'/>, ' ',
        html:button('files/dir-create', 'Create')
      }</form>

      <h3>Upload Files</h3>
      <form method='post' enctype='multipart/form-data' autocomplete='off'
            onsubmit='uploading(this);'>{
        <input type='file' name='files' multiple='multiple'/>,
        html:button('files/upload', 'Upload')
      }</form>
    <div class='note'>
      Ensure that your server has enough RAM to upload large files.
    </div>
  </div>
    => html:wrap({ 'header': $dba:CAT, 'info': $info, 'error': $error })
  )
};

(:~
 : Runs a file action. The directory is also changed via GET requests.
 : @param  $action  name of action
 : @return redirection
 :)
declare
  %updating
  %rest:GET
  %rest:POST
  %rest:path('/dba/files/{$action}')
function dba:action(
  $action  as xs:string
) {
  utils:dispatch($action, {
    'dir-create': fn($args) { {
      'page': $dba:CAT,
      'info': `Directory "{ $args?name }" was created.`,
      'run' : %updating fn() { file:create-dir(config:files-dir() || $args?name) }
    } },
    'dir-change': fn($args) { {
      'page': $dba:CAT,
      'run' : %updating fn() {
        let $sep := file:dir-separator()
        let $dir := string($args?dir)
        let $path := file:path-to-native(
          if (contains($dir, $sep)) then $dir else config:files-dir() || $dir || $sep
        )
        (: ensure that the directory can be accessed :)
        return (void(file:list($path)), config:set-files-dir($path))
      }
    } },
    'delete': fn($args) { {
      'page': $dba:CAT,
      'info': utils:info($args?name, 'file', 'deleted'),
      'run' : %updating fn() {
        (: delete all files, ignore reference to parent directory :)
        $args?name[. != '..'] ! file:delete(config:files-dir() || .)
      }
    } },
    'upload': fn($args) {
      let $dir := config:files-dir()
      let $files := $args?files[. instance of map(*)] otherwise {}
      return {
        'page': $dba:CAT,
        'info': if (map:size($files)) { utils:info(map:keys($files), 'file', 'uploaded') },
        'run' : %updating fn() {
          (: parse all XQuery files; reject files that cannot be parsed :)
          void(
            for key $name value $content in $files
            where matches($name, '\.xq(m|l|y|u|uery)?$')
            return utils:query-parse(convert:binary-to-string($content), $dir || $name)
          ),
          for key $name value $content in $files
          return file:write-binary(utils:safe-path($dir, $name), $content)
        }
      }
    },
    'start': fn($args) {
      let $file := replace(string($args?file), '\.\.+|/|\\', '')
      let $id := utils:job-id($file)
      return {
        'page'  : $dba:CAT,
        'params': { 'job': $id },
        'info'  : 'Job was started.',
        'run'   : %updating fn() {
          void(job:eval(xs:anyURI(config:files-dir() || $file), (),
            { 'cache': true(), 'id': $id, 'log': 'DBA file' }))
        }
      }
    }
  })
};
