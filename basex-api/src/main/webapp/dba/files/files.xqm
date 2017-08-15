(:~
 : Files page.
 :
 : @author Christian Grün, BaseX Team, 2014-17
 :)
module namespace dba = 'dba/files';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace html = 'dba/html' at '../modules/html.xqm';
import module namespace tmpl = 'dba/tmpl' at '../modules/tmpl.xqm';
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
) as element() {
  cons:check(),

  let $dir := $cons:DBA-DIR ! (file:create-dir(.), .)
  return tmpl:wrap(map { 'top': $dba:CAT, 'info': $info, 'error': $error },
    <tr>
      <td>
        <form action="{ $dba:CAT }" method="post" class="update">
        <h2>Files</h2>
        {
          let $files := file:children($dir)[file:is-file(.)]
          let $jobs := jobs:list-details()
          let $headers := (
            <name>Name</name>,
            <date type='dateTime' order='desc'>Date</date>,
            <size type='bytes' order='desc'>Size</size>,
            <action type='xml'>Action</action>
          )
          let $rows := (
            for $file in $files
            let $name := file:name($file)
            let $date := file:last-modified($file)
            let $xquery := ends-with($name, $cons:SUFFIX)
            let $actions := util:item-join(if($xquery) then (
              html:link('Edit', 'queries',
                map { 'file': replace($name, $cons:SUFFIX || '$', '') }
              ),
              let $job := (
                let $uri := file:path-to-uri($file)
                return $jobs[. = $uri]
              )
              let $id := string($job/@id)
              return if($job/@state = 'cached') then (
                html:link('Result', 'file-result/' || web:encode-url($name || '.txt'),
                  map { 'id': $id }
                )
              ) else if($job) then (
                html:link('Stop', 'file-stop', map { 'id': $id })
              ) else (
                html:link('Start', 'file-eval', map { 'file': $name })
              )
            ) else (), ' | ')
            return <row name='{ $name }' date='{ $date }' size='{ file:size($file) }'
              action='{ serialize($actions) }'/>
          )
          let $buttons := html:button('delete-files', 'Delete', true())
          let $link := function($value) { 'file/' || $value }
          return html:table($headers, $rows, $buttons, map {},
            map { 'sort': $sort, 'link': $link }
          )
        }
        </form>
        <h3>Upload Files</h3>
        <form action="upload-files" method="post" enctype="multipart/form-data">
          <input type="file" name="files" multiple="multiple"/>
          <input type="submit" value='Send'/>
        </form>
        <div class='note'>
          The files are located on the DBA system in the temporary directory
          <code>{ $dir }</code>.
        </div>
        <div>&#xa0;</div>
      </td>
    </tr>
  )
};

(:~
 : Redirects to the specified action.
 : @param  $action  action to perform
 : @param  $names   names of files
 :)
declare
  %rest:POST
  %rest:path("/dba/files")
  %rest:query-param("action", "{$action}")
  %rest:query-param("name",   "{$names}")
  %output:method("html")
function dba:files-redirect(
  $action  as xs:string,
  $names   as xs:string*
) as element(rest:response) {
  cons:check(),
  web:redirect($action, map { 'name': $names, 'redirect': $dba:CAT })
};

(:~
 : Downloads a file.
 : @param  $name  name of file
 : @return file
 :)
declare
  %rest:GET
  %rest:path("/dba/file/{$name}")
function dba:files(
  $name  as xs:string
) as item()+ {
  cons:check(),
  web:response-header(map { }, map { 'Cache-Control': '' }),
  file:read-binary($cons:DBA-DIR || $name)
};
