(:~
 : Create directory.
 :
 : @author Christian Grün, BaseX Team, 2014-18
 :)
module namespace dba = 'dba/files';

import module namespace session = 'dba/session' at '../modules/session.xqm';

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
  file:create-dir(session:directory() || $name),
  web:redirect($dba:CAT, map { 'info': 'Directory "' || $name || '" was created.' })
};
