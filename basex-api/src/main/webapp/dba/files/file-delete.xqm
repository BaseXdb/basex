(:~
 : Delete files.
 :
 : @author Christian Grün, BaseX Team, 2014-17
 :)
module namespace dba = 'dba/files';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace util = 'dba/util' at '../modules/util.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'files';

(:~
 : Deletes files.
 : @param  $names  names of files
 : @return redirection
 :)
declare
  %rest:GET
  %rest:path("/dba/file-delete")
  %rest:query-param("name", "{$names}")
  %output:method("html")
function dba:file-delete(
  $names  as xs:string*
) as element(rest:response) {
  cons:check(),
  try {
    $names ! file:delete($cons:DBA-DIR || .),
    web:redirect($dba:CAT, map { 'info': util:info($names, 'file', 'deleted') })
  } catch * {
    web:redirect($dba:CAT, map { 'error': $err:description })
  }
};
