(:~
 : Drop backups.
 :
 : @author Christian Grün, BaseX Team 2005-23, BSD License
 :)
module namespace dba = 'dba/databases';

import module namespace util = 'dba/util' at '../../lib/util.xqm';

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
  %rest:GET
  %rest:path('/dba/backup-drop')
  %rest:query-param('name',   '{$name}', '')
  %rest:query-param('backup', '{$backups}')
function dba:backup-drop(
  $name     as xs:string,
  $backups  as xs:string*
) as empty-sequence() {
  let $target := $name ?? $dba:SUB !! $dba:CAT
  return try {
    $backups ! db:drop-backup($name || '-' || .),
    util:redirect($target, map { 'name': $name, 'info': util:info($backups, 'backup', 'dropped') })
  } catch * {
    util:redirect($target, map { 'name': $name, 'error': $err:description })
  }
};
