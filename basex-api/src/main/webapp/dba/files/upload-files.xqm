(:~
 : Upload files.
 :
 : @author Christian Grün, BaseX Team, 2014-16
 :)
module namespace dba = 'dba/files';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'files';

(:~
 : Deletes files.
 : @param  $files  map with uploaded files
 :)
declare
  %rest:POST
  %rest:path("/dba/upload-files")
  %rest:form-param("files", "{$files}")
  %output:method("html")
function dba:drop(
  $files  as map(*)
) as element(rest:response) {
  cons:check(),

  let $dir := $cons:DBA-DIR
  return (
    file:create-dir($dir),
    map:for-each($files, function($name, $content) {
      file:write-binary($dir || file:name($name), $content)
    })
  ),
  web:redirect($dba:CAT, map { 'info': 'Uploaded files: ' || map:size($files) })
};
