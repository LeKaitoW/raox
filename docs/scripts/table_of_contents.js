function generateTableOfContents(contents) {
    var contentsList = $("ul#table_of_contents");

    for (var i = 0; i < contents.length; i++) {
        var li = $("<li>");
        var a = $("<a>");

        a.attr("href", contents[i].href);
        a.html(contents[i].title);

        a.appendTo(li);
        li.appendTo(contentsList);
    }
}

function getContents() {
    var contents = [
        {
            "href":"../reference/base.html",
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
    generateTableOfContents(getContents());
});
