(:~
 : Edit database.
 :
 : @author Christian Grün, BaseX Team, 2014-17
 :)
module namespace dba = 'dba/databases';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';

(:~ Sub category :)
declare variable $dba:SUB := 'user';

(:~
 : Edits a user.
 : @param  $name     user name
 : @param  $newname  new name
 : @param  $pw       password
 : @param  $perm     permission
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path("/dba/user-edit")
  %rest:query-param("name",    "{$name}")
  %rest:query-param("newname", "{$newname}")
  %rest:query-param("pw",      "{$pw}")
  %rest:query-param("perm",    "{$perm}")
function dba:user-edit(
  $name     as xs:string,
  $newname  as xs:string,
  $pw       as xs:string,
  $perm     as xs:string
) as empty-sequence() {
  cons:check(),
  try {
    let $old := user:list-details($name) return (
      if($name = $newname) then () else if(user:exists($newname)) then (
         error((), 'User already exists.')
       ) else (
         user:alter($name, $newname)
      ),
      if($pw = '') then () else user:password($name, $pw),
      if($perm = $old/@permission) then () else user:grant($name, $perm)
    ),
    cons:redirect($dba:SUB, map { 'name': $newname, 'info': 'User was saved.' })
  } catch * {
    cons:redirect($dba:SUB, map {
      'name': $name, 'newname': $newname, 'pw': $pw, 'perm': $perm, 'error': $err:description
    })
  }
};
