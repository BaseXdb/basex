(:~
 : Drop backups.
 :
 : @author Christian Grün, BaseX Team 2005-24, BSD License
 :)
module namespace dba = 'dba/databases';

import module namespace utils = 'dba/utils' at '../../lib/utils.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'databases';
(:~ Sub category :)
declare variable $dba:SUB := 'database';

(:~
 : Drops database backups.
 : @param  $name     name of database
 : @param  $backups  timestamps of backups
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/backup-drop')
  %rest:query-param('name',   '{$name}', '')
  %rest:query-param('backup', '{$backups}')
function dba:backup-drop(
  $name     as xs:string,
  $backups  as xs:string*
) as empty-sequence() {
  let $target := if($name) then $dba:SUB else $dba:CAT
  return try {
    $backups ! db:drop-backup($name || '-' || .),
    utils:redirect($target, { 'name': $name, 'info': utils:info($backups, 'backup', 'dropped') })
  } catch * {
    utils:redirect($target, { 'name': $name, 'error': $err:description })
  }
};
