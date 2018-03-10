(:~
 : Files page.
 :
 : @author Christian Grün, BaseX Team, 2014-18
 :)
module namespace dba = 'dba/files';

import module namespace html = 'dba/html' at '../modules/html.xqm';
import module namespace options = 'dba/options' at '../modules/options.xqm';
import module namespace session = 'dba/session' at '../modules/session.xqm';
import module namespace util = 'dba/util' at '../modules/util.xqm';

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
  %rest:path("/dba/files")
  %rest:query-param("sort",  "{$sort}", "")
  %rest:query-param("error", "{$error}")
  %rest:query-param("info",  "{$info}")
  %output:method("html")
function dba:files(
  $sort   as xs:string,
  $error  as xs:string?,
  $info   as xs:string?
) as element(html) {
  let $dir := session:directory()
  return html:wrap(map { 'header': $dba:CAT, 'info': $info, 'error': $error },
    <tr>
      <td>
        <h2>Directory</h2>
        <form action="dir-change" method="post">
          <select name="dir" style="width: 350px;" onchange="this.form.submit();">{
            let $webapp := dba:dir(db:option('webpath'))[.]
            let $options := (
              ['DBA'       , $options:DBA-DIRECTORY],
              ['Webapp'    , $webapp],
              ['RESTXQ'    , dba:dir($webapp ! file:resolve-path(db:option('restxqpath'), .))],
              ['Repository', dba:dir(db:option('repopath'))],
              Q{java:java.io.File}listRoots() ! ['Root', string(.)],
              ['Current'   , $dir]
            )
            let $selected := (
              for $option at $pos in $options
              where $option(2) = $dir
              return $pos
            )[1]
            for $option at $pos in $options
            let $name := $option(1), $path := $option(2)
            where $path
            return element option {
              attribute value { $path },
              attribute selected { }[$pos = $selected],
              $path[.] ! (($name || ': ')[$name] || .)
            }
          }</select><![CDATA[ ]]>
        </form>

        <form action="{ $dba:CAT }" method="post" class="update">{
          let $headers := (
            map { 'key': 'name', 'label': 'Name', 'type': 'xml' },
            map { 'key': 'date', 'label': 'Date', 'type': 'dateTime', 'order': 'desc' },
            map { 'key': 'bytes', 'label': 'Bytes', 'type': 'bytes', 'order': 'desc' },
            map { 'key': 'action', 'label': 'Action', 'type': 'xml' }
          )
          let $entries :=
            let $jobs := jobs:list-details()
            let $parent := if(file:parent($dir)) then ($dir || '..') else ()
            for $file in ($parent, file:children($dir))
            let $dir := file:is-dir($file)
            let $name := file:name($file)
            order by $dir descending, $name != '..', $name collation '?lang=en'
            return map {
              'name': function() {
                if($dir) then html:link($name, 'dir-change', map { 'dir': $name }) else $name
              },
              'date': file:last-modified($file),
              'bytes': file:size($file),
              'action': function() {
                util:item-join((
                  if($dir) then () else html:link('Download', 'file/' || encode-for-uri($name)),
                  if($dir or not(ends-with($name, '.xq') or ends-with($name, '.xqm'))) then () else
                    html:link('Edit', 'queries', map { 'file': $name }),
                  if($dir or not(ends-with($name, '.xq'))) then () else (
                    let $job := (
                      let $uri := replace(file:path-to-uri($file), '^file:/*', '')
                      return $jobs[replace(., '^file:/*', '') = $uri]
                    )
                    let $id := string($job/@id)
                    return if(empty($job)) then (
                      html:link('Start', 'file-start', map { 'file': $name })
                    ) else (
                      html:link('Job', 'jobs', map { 'job': $id })
                    )
                  )
                ), ' · ')
              }
            }
          let $buttons := html:button('file-delete', 'Delete', true())
          let $options := map { 'sort': $sort }
          return html:table($headers, $entries, $buttons, map { }, $options)
        }</form>

        <h3>Upload Files</h3>
        <form action="file-upload" method="post" enctype="multipart/form-data">
          <input type="file" name="files" multiple="multiple"/>
          <input type="submit" value='Send'/>
        </form>

        <h3>Create Directory</h3>
        <form action="dir-create" method="post">
          <input type="text" name="name"/><![CDATA[ ]]>
          <input type="submit" value='Create'/>
        </form>
      </td>
    </tr>
  )
};

(:~
 : Redirects to the specified action.
 : @param  $action  action to perform
 : @param  $names   names of files
 : @param  $ids     ids
 : @return redirection
 :)
declare
  %rest:POST
  %rest:path("/dba/files")
  %rest:query-param("action", "{$action}")
  %rest:query-param("name",   "{$names}")
function dba:files-redirect(
  $action  as xs:string,
  $names   as xs:string*
) as element(rest:response) {
  web:redirect($action, map { 'name': $names, 'redirect': $dba:CAT })
};

(:~
 : Returns a native directory representation of the specified file.
 : @param  $dir  directory
 : @return native path (or empty sequence)
 :)
declare function dba:dir(
  $dir  as xs:string
) as xs:string? {
  try {
    file:path-to-native($dir)
  } catch file:* { }
};
