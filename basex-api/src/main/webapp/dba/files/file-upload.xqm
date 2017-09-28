(:~
 : Upload files.
 :
 : @author Christian Grün, BaseX Team, 2014-17
 :)
module namespace dba = 'dba/files';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace util = 'dba/util' at '../modules/util.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'files';

(:~
 : Upploads files.
 : @param  $files  map with uploaded files
 : @return redirection
 :)
declare
  %rest:POST
  %rest:path("/dba/file-upload")
  %rest:form-param("files", "{$files}")
  %output:method("html")
function dba:file-upload(
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
  web:redirect($dba:CAT, map { 'info': util:info(map:keys($files), 'file', 'uploaded') })
};
