function generateSidebar(contents) {
    var body = $("body");

    var wrapper = $("<div>");
    wrapper.attr("id", "wrapper");

    var sidebarWrapper = $("<div>");
    sidebarWrapper.attr("id", "sidebar-wrapper");

    var pageContentWrapper = $("<div>");
    pageContentWrapper.attr("id", "page-content-wrapper");

    var pageContent = $("<div>");
    pageContent.attr("class", "page-content");

    var sidebarNav = $("<div>");
    sidebarNav.attr("class", "sidebar-nav");

    var header = $("<h2>");
    header.attr("align", "center");
    header.html("Содержание");

    var ul = $("<ul>");

    for (var i = 0; i < contents.length; i++) {
        var li = $("<li>");
        var a = $("<a>");

        a.attr("href", contents[i].href);
        a.html(contents[i].title);

        a.appendTo(li);
        li.appendTo(ul);
    }

    header.appendTo(sidebarNav);
    ul.appendTo(sidebarNav);

    body.wrapInner(pageContent);
    body.wrapInner(pageContentWrapper);
    sidebarWrapper.wrapInner(sidebarNav);
    sidebarWrapper.prependTo(body);
    body.wrapInner(wrapper);
}

function getContents() {
    var contents = [
        {
            "href":"../reference/base_types_and_functions.html",
            "title":"Базовые возможности языка"
        },
        {
            "href":"../reference/resource.html",
            "title":"Ресурсы"
        },
        {
            "href":"../reference/event.html",
            "title":"События"
        },
        {
            "href":"../reference/pattern.html",
            "title":"Образцы"
        },
        {
            "href":"../reference/dpt.html",
            "title":"Точки принятия решений"
        },
        {
            "href":"../reference/search.html",
            "title":"Поиск решения на графе"
        },
        {
            "href":"../reference/sequence.html",
            "title":"Последовательности"
        },
        {
            "href":"../reference/constant.html",
            "title":"Константы"
        },
        {
            "href":"../reference/function.html",
            "title":"Функции"
        },
        {
            "href":"../reference/table_list.html",
            "title":"Таблицы и списки"
        },
        {
            "href":"../reference/result.html",
            "title":"Результаты моделирования"
        }
    ];

    return contents;
}

$(document).ready(function() {
    generateSidebar(getContents());
});
