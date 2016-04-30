var button = $("#nav-collapse");
var nav;

function changeNavState() {
    if(nav.hasClass("nav-collapsed")){
        nav.removeClass("nav-collapsed");
        button.children().removeClass("fa-angle-right");
        button.children().addClass("fa-angle-left");
    }else{
        nav.addClass("nav-collapsed");
        button.children().removeClass("fa-angle-left");
        button.children().addClass("fa-angle-right");
    }
}

function setActive(){
    var url = window.location.pathname,
    urlRegExp = new RegExp(url.replace(/\/$/,'') + "$");
    $('nav ul li a').each(function(){
        if(urlRegExp.test(this.href.replace(/\/$/,''))){
            $(this).addClass('active');
        }
    });
}

$( document ).ready(function (){
    setActive();
    nav = $(".nav-hideable");
    button.click(function () {
        changeNavState();
    });
});
