(:~
 : Settings, garbage collection.
 :
 : @author Christian Grün, BaseX Team 2005-23, BSD License
 :)
module namespace dba = 'dba/settings';

(:~ Top category :)
declare variable $dba:CAT := 'settings';

(:~
 : Triggers garbage collection.
 : @return redirection
 :)
declare
  %rest:POST
  %rest:path('/dba/settings-gc')
function dba:settings-gc(
) as element(rest:response) {
  Q{java.lang.System}gc(),
  web:redirect($dba:CAT, map { 'info': 'Garbage collection was triggered.' })
};
