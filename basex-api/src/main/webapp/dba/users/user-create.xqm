(:~
 : Create new user.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/users';

import module namespace config = 'dba/config' at '../lib/config.xqm';
import module namespace html = 'dba/html' at '../lib/html.xqm';
import module namespace utils = 'dba/utils' at '../lib/utils.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'users';

(:~
 : Create new user.
 : @param  $name  entered username
 : @param  $pw    entered password
 : @param  $perm  chosen permission
 : @param  $do    perform update
 : @return form or redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/user-create')
  %rest:form-param('name', '{$name}')
  %rest:form-param('pw',   '{$pw}')
  %rest:form-param('perm', '{$perm}', 'none')
  %rest:form-param('do',   '{$do}')
  %output:method('html')
  %output:html-version('5')
function dba:user-create(
  $name  as xs:string?,
  $pw    as xs:string?,
  $perm  as xs:string,
  $do    as xs:string?
) {
  html:update($do, { 'header': $dba:CAT }, fn() {
    <tr>
      <td>
        <form method='post' autocomplete='off'>
          <input type='hidden' name='do' value='do'/>
          <!-- force chrome not to autocomplete form -->
          <h2>{
            html:link('Users', $dba:CAT), ' » ',
            html:button('user-create', 'Create')
          }</h2>
          <table>
            <tr>
              <td>Name:</td>
              <td>
                <input type='text' name='name' value='{ $name }' autofocus=''/>
                <div class='small'/>
              </td>
            </tr>
            <tr>
              <td>Password:</td>
              <td>
                <input type='password' name='pw' value='{ $pw }' autocomplete='new-password'/>
                <div class='small'/>
              </td>
            </tr>
            <tr>
              <td>Permission:</td>
              <td>
                <select name='perm' size='5'>{
                  for $p in $config:PERMISSIONS
                  return element option { attribute selected { }[$p = $perm], $p }
                }</select>
                <div class='small'/>
              </td>
            </tr>
          </table>
        </form>
      </td>
    </tr>
  }, fn() {
    if (user:exists($name)) {
      error((), 'User already exists.')
    } else {
      user:create($name, $pw, $perm),
      utils:redirect($dba:CAT, { 'info': 'User was created.' })
    }
  })
};
