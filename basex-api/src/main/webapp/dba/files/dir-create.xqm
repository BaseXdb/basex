(:~
 : Create directory.
 :
 : @author Christian Grün, BaseX Team 2005-24, BSD License
 :)
module namespace dba = 'dba/files';

import module namespace config = 'dba/config' at '../lib/config.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'files';

(:~
 : Creates a directory.
 : @param  $name  name of directory to create
 : @return redirection
 :)
declare
  %rest:POST
  %rest:path('/dba/dir-create')
  %rest:query-param('name', '{$name}')
function dba:dir-create(
  $name  as xs:string
) as element(rest:response) {
  file:create-dir(config:files-dir() || $name),
  web:redirect($dba:CAT, { 'info': 'Directory "' || $name || '" was created.' })
};
