$(document).ready(function(){
  var searchbox = $(".search");
  var placeholder = "Search...";
	
  searchbox.focus(function(e){
    $(this).addClass("active");
    if($(this).attr("value") == placeholder) $(this).attr("value", "");
  });
  searchbox.blur(function(e){
    $(this).removeClass("active");
    if($(this).attr("value") == "") $(this).attr("value", placeholder);
  });
	
  searchbox.focus();
});
