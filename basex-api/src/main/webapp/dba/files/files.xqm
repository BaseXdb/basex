(:~
 : Files page.
 :
 : @author Christian Grün, BaseX Team, 2014-16
 :)
module namespace dba = 'dba/files';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace html = 'dba/html' at '../modules/html.xqm';
import module namespace tmpl = 'dba/tmpl' at '../modules/tmpl.xqm';

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
  %rest:query-param("sort", "{$sort}", "")
  %rest:query-param("error", "{$error}")
  %rest:query-param("info",  "{$info}")
  %output:method("html")
function dba:files(
  $sort   as xs:string,
  $error  as xs:string?,
  $info   as xs:string?
) as element() {
  cons:check(),

  (: request data in a single step :)
  let $dir := $cons:DBA-DIR
  let $files := if(not(file:exists($dir))) then () else
    file:children($dir)[file:is-file(.)]

  return tmpl:wrap(map { 'top': $dba:CAT, 'info': $info, 'error': $error },
    <tr>
      <td>
        <form action="{ $dba:CAT }" method="post" class="update">
        <h2>Files</h2>
        {
          let $rows :=
            for $file in $files
            let $date := file:last-modified($file)
            return <row name='{ file:name($file) }' date='{ $date }' size='{ file:size($file) }'/>
          let $headers := (
            <name>Name</name>,
            <date type='dateTime' order='desc'>Date</date>,
            <size type='bytes' order='desc'>Size</size>
          )
          let $buttons := html:button('delete-files', 'Delete', true())
          let $link := function($value) { 'file/' || $value }
          return html:table($headers, $rows, $buttons, map {}, map { 'sort': $sort, 'link': $link })
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
  web:response-header(),
  file:read-binary($cons:DBA-DIR || $name)
};
