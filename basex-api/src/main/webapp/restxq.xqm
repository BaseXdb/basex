(:~
 : This module redirects to the database administration interface.
 : @author BaseX Team
 :)
module namespace page = 'http://basex.org/modules/web-page';

(:~
 : Redirects to the administration interface.
 : @return HTML page
 :)
declare %rest:path("") function page:redirect() {
  element rest:redirect { 'dba' }
};
