(:~
 : Delete files.
 :
 : @author Christian Grün, BaseX Team, 2014-16
 :)
module namespace dba = 'dba/files';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'files';

(:~
 : Deletes files.
 : @param  $names  names of files
 :)
declare
  %rest:GET
  %rest:path("/dba/delete-files")
  %rest:query-param("name", "{$names}")
  %output:method("html")
function dba:drop(
  $names  as xs:string*
) as element(rest:response) {
  cons:check(),
  try {
    $names ! file:delete($cons:DBA-DIR || .),
    web:redirect($dba:CAT, map { 'info': 'Deleted files: ' || count($names) })
  } catch * {
    web:redirect($dba:CAT, map { 'error': $err:description })
  }
};
