(:~
 : Edit user.
 :
 : @author Christian Grün, BaseX Team 2005-20, BSD License
 :)
module namespace dba = 'dba/users';

import module namespace util = 'dba/util' at '../lib/util.xqm';

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
  %rest:path('/dba/user-edit')
  %rest:form-param('name',    '{$name}')
  %rest:form-param('newname', '{$newname}')
  %rest:form-param('pw',      '{$pw}')
  %rest:form-param('perm',    '{$perm}')
  %rest:form-param('info',    '{$info}')
function dba:user-edit(
  $name     as xs:string,
  $newname  as xs:string,
  $pw       as xs:string,
  $perm     as xs:string,
  $info     as xs:string
) as empty-sequence() {
  try {
    let $old := user:list-details($name)
    return (
      (: change name of user :)
      if($name = $newname) then () else (
        if(user:exists($newname)) then (
           error((), 'User already exists.')
         ) else (
           user:alter($name, $newname)
        )
      ),
      (: change password :)
      if($pw = '') then () else user:password($name, $pw),
      (: change permissions :)
      if($perm = $old/@permission) then () else user:grant($name, $perm),
      (: change user info :)
      let $xml := if($info) then (
        parse-xml($info)/*[self::info or error(xs:QName(err:FORC0006))]
      ) else (
        <info/>
      )
      where not(deep-equal(user:info($name), $xml))
      return user:update-info($xml, $name)
    ),
    util:redirect($dba:SUB, map { 'name': $newname, 'info': 'User was saved.' })
  } catch * {
    let $error := if ($err:code != xs:QName('err:FODC0006')) then $err:description else
      'Information must be XML with an info root element.'
    return util:redirect($dba:SUB, map {
      'name': $name, 'newname': $newname, 'pw': $pw, 'perm': $perm, 'error': $error
    })
  }
};
