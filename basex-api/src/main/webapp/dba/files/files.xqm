(:~
 : Files page.
 :
 : @author Christian Grün, BaseX Team 2005-24, BSD License
 :)
module namespace dba = 'dba/files';

import module namespace config = 'dba/config' at '../lib/config.xqm';
import module namespace html = 'dba/html' at '../lib/html.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'files';

(:~
 : Files page.
 : @param  $sort   table sort key
 : @param  $error  error message
 : @param  $info   info message
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
  %output:html-version('5')
function dba:files(
  $sort   as xs:string,
  $error  as xs:string?,
  $info   as xs:string?,
  $page   as xs:string
) as element(html) {
  let $dir := config:files-dir()
  return html:wrap({ 'header': $dba:CAT, 'info': $info, 'error': $error },
    <tr>
      <td>
        <h2>Directory</h2>
        <form action='dir-change' method='post'>
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

        <form method='post'>{
          let $headers := (
            { 'key': 'name', 'label': 'Name', 'type': 'dynamic' },
            { 'key': 'date', 'label': 'Date', 'type': 'dateTime', 'order': 'desc' },
            { 'key': 'bytes', 'label': 'Bytes', 'type': 'bytes', 'order': 'desc' },
            { 'key': 'action', 'label': 'Action', 'type': 'dynamic' }
          )
          let $entries := (
            let $limit := config:get($config:MAXCHARS)
            let $jobs := job:list-details()
            let $parent := if(file:parent($dir)) then ($dir || '..') else ()
            for $file in ($parent, file:children($dir))
            let $dir := file:is-dir($file)
            let $name := file:name($file)
            order by $dir descending, $name != '..', $name collation '?lang=en'

            (: skip files without access permissions :)
            for $modified in try { file:last-modified($file) } catch * { }
            let $size := file:size($file)
            return {
              'name': fn() {
                if($dir) then html:link($name, 'dir-change', { 'dir': $name }) else $name
              },
              'date': $modified,
              'bytes': $size,
              'action': fn() {
                intersperse(
                  if($dir) then () else (
                    html:link('Download', 'file/' || encode-for-uri($name)),
                    if($size <= $limit) then (
                      html:link('Edit', 'editor', { 'name': $name })
                    ) else (),
                    if(matches($name, '\.xq$')) then (
                      (: choose first running job :)
                      let $job := head(
                        let $uri := replace(file:path-to-uri($file), '^file:/*', '')
                        return $jobs[replace(., '^file:/*', '') = $uri]
                      )
                      let $id := string($job/@id)
                      return if(empty($job)) then (
                        html:link('Start', 'file-start', { 'file': $name })
                      ) else (
                        html:link('Job', 'jobs', { 'job': $id })
                      )
                    ) else ()
                  )
                , ' · ')
              }
            }
          )
          let $buttons := html:button('file-delete', 'Delete', ('CHECK', 'CONFIRM'))
          let $options := { 'sort': $sort, 'page': xs:integer($page) }
          return html:table($headers, $entries, $buttons, {}, $options)
        }</form>

        <h3>Create Directory</h3>
        <form method='post'>{
          <input type='text' name='name'/>, ' ',
          html:button('dir-create', 'Create')
        }</form>

        <h3>Upload Files</h3>
        <form method='post' enctype='multipart/form-data'>{
          <input type='file' name='files' multiple='multiple'/>,
          html:button('file-upload', 'Upload')
        }</form>
        <div class='note'>
          Ensure that your server has enough RAM to upload large files.
        </div>
      </td>
    </tr>
  )
};
