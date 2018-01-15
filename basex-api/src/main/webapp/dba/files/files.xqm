(:~
 : Files page.
 :
 : @author Christian Grün, BaseX Team, 2014-18
 :)
module namespace dba = 'dba/files';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace html = 'dba/html' at '../modules/html.xqm';
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
  cons:check(),

  let $dir := cons:current-dir()
  return html:wrap(map { 'header': $dba:CAT, 'info': $info, 'error': $error },
    <tr>
      <td>
        <h2>Directory</h2>
        <form action="dir-change" method="post">
          <select name="dir" style="width: 350px;" onchange="this.form.submit();">{
            let $system := db:system()
            let $webapp := dba:dir($system//webpath)[.]
            let $options := (
              ['DBA'       , $cons:DBA-DIR],
              ['Webapp'    , $webapp],
              ['RESTXQ'    , dba:dir($webapp ! file:resolve-path($system//restxqpath, .))],
              ['Repository', dba:dir($system//repopath)],
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
        <form action="{ $dba:CAT }" method="post" class="update">
        {
          let $headers := (
            <name type='xml'>Name</name>,
            <date type='dateTime' order='desc'>Date</date>,
            <bytes type='bytes' order='desc'>Bytes</bytes>,
            <action type='xml'>Action</action>
          )
          let $rows := (
            let $jobs := jobs:list-details()
            let $parent := if(file:parent($dir)) then ($dir || '..') else ()
            for $file in ($parent, file:children($dir))
            let $dir := file:is-dir($file)
            let $name := file:name($file)
            order by $dir descending, $name != '..', $name collation '?lang=en'
            let $actions := util:item-join((
              if($dir) then () else html:link('Download', 'file/' || encode-for-uri($name)),
              if($dir or not(ends-with($name, '.xq') or ends-with($name, '.xqm'))) then () else (
                html:link('Edit', 'queries', map { 'file': $name })
              ),
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

            return <row name='{ serialize(
              if($dir) then html:link($name, 'dir-change', map { 'dir': $name }) else $name
            )}' date='{
              file:last-modified($file)
            }' bytes='{
              file:size($file)
            }' action='{ serialize(
              $actions
            )}'/>
          )
          let $buttons := html:button('file-delete', 'Delete', true())
          return html:table($headers, $rows, $buttons, map { }, map { })
        }
        </form>
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
  cons:check(),
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
