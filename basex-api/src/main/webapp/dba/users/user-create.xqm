(:~
 : Create new user.
 :
 : @author Christian Grün, BaseX Team 2005-24, BSD License
 :)
module namespace dba = 'dba/users';

import module namespace config = 'dba/config' at '../lib/config.xqm';
import module namespace html = 'dba/html' at '../lib/html.xqm';
import module namespace utils = 'dba/utils' at '../lib/utils.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'users';

(:~
 : Form for creating a new user.
 : @param  $name   entered username
 : @param  $pw     entered password
 : @param  $perm   chosen permission
 : @param  $error  error string
 : @return page
 :)
declare
  %rest:GET
  %rest:POST
  %rest:path('/dba/user-create')
  %rest:query-param('name',  '{$name}')
  %rest:query-param('pw',    '{$pw}')
  %rest:query-param('perm',  '{$perm}', 'none')
  %rest:query-param('error', '{$error}')
  %output:method('html')
  %output:html-version('5')
function dba:user-create(
  $name   as xs:string?,
  $pw     as xs:string?,
  $perm   as xs:string,
  $error  as xs:string?
) as element(html) {
  html:wrap({ 'header': $dba:CAT, 'error': $error },
    <tr>
      <td>
        <form method='post' autocomplete='off'>
          <!--  force chrome not to autocomplete form -->
          <input style='display:none' type='text' name='fake1'/>
          <input style='display:none' type='password' name='fake2'/>
          <h2>{
            html:link('Users', $dba:CAT), ' » ',
            html:button('user-create-do', 'Create')
          }</h2>
          <!-- dummy value; prevents reset of options when nothing is selected -->
          <input type='hidden' name='opts' value='x'/>
          <table>
            <tr>
              <td>Name:</td>
              <td>
                <input type='text' name='name' value='{ $name }' autofocus='autofocus'/>
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
  )
};

(:~
 : Creates a user.
 : @param  $name  username
 : @param  $pw    password
 : @param  $perm  permission
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/user-create-do')
  %rest:query-param('name', '{$name}')
  %rest:query-param('pw',   '{$pw}')
  %rest:query-param('perm', '{$perm}')
function dba:user-create-do(
  $name  as xs:string,
  $pw    as xs:string,
  $perm  as xs:string
) as empty-sequence() {
  try {
    if(user:exists($name)) then (
      error((), 'User already exists.')
    ) else (
      user:create($name, $pw, $perm)
    ),
    utils:redirect($dba:CAT, { 'info': 'User was created.' })
  } catch * {
    utils:redirect('user-create', {
      'name': $name, 'pw': $pw, 'perm': $perm, 'error': $err:description
    })
  }
};
