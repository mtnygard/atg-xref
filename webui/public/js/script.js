$(document).ready(function() {
  $('#moduleplaceholder').load('/v1/modules');
  $('#classplaceholder').load('/v1/classes');
  $('#jspplaceholder').load('/v1/jsps');
});
