(:~
 : Delete files.
 :
 : @author Christian Grün, BaseX Team 2005-24, BSD License
 :)
module namespace dba = 'dba/files';

import module namespace config = 'dba/config' at '../lib/config.xqm';
import module namespace utils = 'dba/utils' at '../lib/utils.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'files';

(:~
 : Deletes files.
 : @param  $names  names of files
 : @return redirection
 :)
declare
  %rest:POST
  %rest:path('/dba/file-delete')
  %rest:query-param('name', '{$names}')
function dba:file-delete(
  $names  as xs:string*
) as element(rest:response) {
  try {
    (: delete all files, ignore reference to parent directory :)
    for $name in $names
    where $name != '..'
    return file:delete(config:files-dir() || $name),
    web:redirect($dba:CAT, { 'info': utils:info($names, 'file', 'deleted') })
  } catch * {
    web:redirect($dba:CAT, { 'error': $err:description })
  }
};
