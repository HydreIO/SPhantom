var nav = $(".nav-hideable");
var button = $("#nav-button");

function changeNavState() {
    if(nav.hasClass("nav-hidden")){
        nav.removeClass("nav-hidden");
        button.text(">");
    }else{
        nav.addClass("nav-hidden");
        button.text("<");
    }
}
