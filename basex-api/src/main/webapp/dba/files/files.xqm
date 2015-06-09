(:~
 : Files page.
 :
 : @author Christian Grün, BaseX GmbH, 2014-15
 :)
module namespace _ = 'dba/files';

import module namespace Sessions = 'http://basex.org/modules/sessions';
import module namespace Session = 'http://basex.org/modules/session';
import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace html = 'dba/html' at '../modules/html.xqm';
import module namespace tmpl = 'dba/tmpl' at '../modules/tmpl.xqm';

(:~ Top category :)
declare variable $_:CAT := 'files';

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
function _:files(
  $sort   as xs:string,
  $error  as xs:string?,
  $info   as xs:string?
) as element() {
  cons:check(),

  (: request data in a single step :)
  let $files := file:children($cons:DBA-DIR)[file:is-file(.)]

  return tmpl:wrap(map { 'top': $_:CAT, 'info': $info, 'error': $error },
    <tr>
      <td>
        <form action="{ $_:CAT }" method="post" class="update">
        <h2>Files</h2>
        {
          let $entries :=
            for $file in $files
            let $date := file:last-modified($file)
            return <e name='{ file:name($file) }' date='{ $date }' size='{ file:size($file) }'/>
          let $headers := (
            <name>{ html:label($entries, ('File', 'Files')) }</name>,
            <date type='dateTime' order='desc'>Date</date>,
            <size type='bytes' order='desc'>Size</size>
          )
          let $buttons := html:button('delete-files', 'Delete', true())
          let $link := function($value) { 'file/' || $value }
          return html:table($entries, $headers, $buttons, map {}, $sort, $link)
        }
        </form>
        <h3>Upload Files</h3>
        <form action="upload-files" method="post" enctype="multipart/form-data">
          <input type="file" name="files" multiple="multiple"/>
          <input type="submit" value='Send'/>
        </form>
        <div class='note'>
          The files are located on the DBA system in the temporary directory
          <code>{ $cons:DBA-DIR }</code>.
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
function _:action(
  $action  as xs:string,
  $names   as xs:string*
) {
  web:redirect($action, map { 'name': $names, 'redirect': $_:CAT })
};

(:~
 : Downloads a file.
 : @param  $name  name of file
 : @return file
 :)
declare
  %rest:GET
  %rest:path("/dba/file/{$name}")
function _:files(
  $name  as xs:string
) as item()+ {
  web:response-header(),
  file:read-binary($cons:DBA-DIR || $name)
};
