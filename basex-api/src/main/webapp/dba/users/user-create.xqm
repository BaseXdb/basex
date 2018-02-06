(:~
 : Create new user.
 :
 : @author Christian Grün, BaseX Team, 2014-18
 :)
module namespace dba = 'dba/users';

import module namespace html = 'dba/html' at '../modules/html.xqm';
import module namespace options = 'dba/options' at '../modules/options.xqm';
import module namespace util = 'dba/util' at '../modules/util.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'users';

(:~
 : Form for creating a new user.
 : @param  $name   entered user name
 : @param  $pw     entered password
 : @param  $perm   chosen permission
 : @param  $error  error string
 : @return page
 :)
declare
  %rest:GET
  %rest:path("/dba/user-create")
  %rest:query-param("name",  "{$name}")
  %rest:query-param("pw",    "{$pw}")
  %rest:query-param("perm",  "{$perm}", "none")
  %rest:query-param("error", "{$error}")
  %output:method("html")
function dba:user-create(
  $name   as xs:string?,
  $pw     as xs:string?,
  $perm   as xs:string,
  $error  as xs:string?
) as element(html) {
  html:wrap(map { 'header': $dba:CAT, 'error': $error },
    <tr>
      <td>
        <form action='user-create' method='post' autocomplete='off'>
          <!--  force chrome not to autocomplete form -->
          <input style='display:none' type='text' name='fake1'/>
          <input style='display:none' type='password' name='fake2'/>
          <h2>{
            html:link('Users', $dba:CAT), ' » ',
            html:button('create', 'Create')
          }</h2>
          <!-- dummy value; prevents reset of options when nothing is selected -->
          <input type='hidden' name='opts' value='x'/>
          <table>
            <tr>
              <td>Name:</td>
              <td>
                <input type='text' name='name' value='{ $name }' id='name'/>
                { html:focus('name') }
                <div class='small'/>
              </td>
            </tr>
            <tr>
              <td>Passsword:</td>
              <td>
                <input type='password' name='pw' value='{ $pw }' id='pw'
                  autocomplete='new-password'/>
                <div class='small'/>
              </td>
            </tr>
            <tr>
              <td>Permission:</td>
              <td>
                <select name='perm' size='5'>{
                  for $p in $options:PERMISSIONS
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
 : @param  $name  user name
 : @param  $pw    password
 : @param  $perm  permission
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path("/dba/user-create")
  %rest:query-param("name", "{$name}")
  %rest:query-param("pw",   "{$pw}")
  %rest:query-param("perm", "{$perm}")
function dba:user-create(
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
    util:redirect($dba:CAT, map { 'info': 'User was created.' })
  } catch * {
    util:redirect('user-create', map {
      'name': $name, 'pw': $pw, 'perm': $perm, 'error': $err:description
    })
  }
};
