(:~
 : Updates user information.
 :
 : @author Christian Grün, BaseX Team 2005-24, BSD License
 :)
module namespace dba = 'dba/users';

import module namespace utils = 'dba/utils' at '../lib/utils.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'users';
(:~ Sub category :)
declare variable $dba:SUB := 'user';

(:~
 : Updates users information.
 : @param  $info  users information
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/users-info')
  %rest:form-param('info', '{$info}')
function dba:users-info(
  $info  as xs:string
) as empty-sequence() {
  try {
    (: change user info :)
    let $xml := dba:user-info($info)
    where not(deep-equal(user:info(), $xml))
    return user:update-info($xml),

    utils:redirect($dba:CAT, { 'info': 'User information was updated.' })
  } catch * {
    utils:redirect($dba:CAT, { 'error': $err:description })
  }
};

(:~
 : Updates a user.
 : @param  $name     username
 : @param  $newname  new name
 : @param  $pw       password
 : @param  $perm     permission
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/user-update')
  %rest:form-param('name',    '{$name}')
  %rest:form-param('newname', '{$newname}')
  %rest:form-param('pw',      '{$pw}')
  %rest:form-param('perm',    '{$perm}')
  %rest:form-param('info',    '{$info}')
function dba:user-update(
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
      let $xml := dba:user-info($info)
      where not(deep-equal(user:info($name), $xml))
      return user:update-info($xml, $name)
    ),
    utils:redirect($dba:CAT, { 'name': $newname, 'info': 'User was updated.' })
  } catch * {
    utils:redirect($dba:SUB, {
      'name': $name, 'newname': $newname, 'pw': $pw, 'perm': $perm, 'error': $err:description
    })
  }
};

(:~
 : Converts a user info string to XML.
 : @param  $info  user info
 : @return info element
 :)
declare %private function dba:user-info(
  $info  as xs:string
) as element(info) {
  if($info) then (
    let $xml := parse-xml($info)/*
    return if($xml/self::info) then (
      $xml update {
        delete node .//text()[not(normalize-space())]
      }
    ) else (
      error((), 'XML with "info" root element expected.')
    )
  ) else (
    element info { }
  )
};
