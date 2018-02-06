(:~
 : Delete files.
 :
 : @author Christian Grün, BaseX Team, 2014-18
 :)
module namespace dba = 'dba/files';

import module namespace session = 'dba/session' at '../modules/session.xqm';
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
function dba:file-delete(
  $names  as xs:string*
) as element(rest:response) {
  try {
    (: delete all files, ignore reference to parent directory :)
    for $name in $names
    where $name != '..'
    return file:delete(session:directory() || $name),
    web:redirect($dba:CAT, map { 'info': util:info($names, 'file', 'deleted') })
  } catch * {
    web:redirect($dba:CAT, map { 'error': $err:description })
  }
};
