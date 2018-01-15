(:~
 : Create directory.
 :
 : @author Christian Grün, BaseX Team, 2014-18
 :)
module namespace dba = 'dba/files';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'files';

(:~
 : Creates a directory.
 : @param  $name  name of directory to create
 : @return redirection
 :)
declare
  %rest:POST
  %rest:path("/dba/dir-create")
  %rest:query-param("name", "{$name}")
function dba:file-delete(
  $name  as xs:string
) as element(rest:response) {
  cons:check(),
  file:create-dir(cons:current-dir() || $name),
  web:redirect($dba:CAT, map { 'info': 'Directory "' || $name || '" was created.' })
};
