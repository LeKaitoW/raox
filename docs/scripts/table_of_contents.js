let isNavbarPresent;
const MIN_WINDOW_WIDTH = 300;

function fillContents(ul, contents) {
    for (var i = 0; i < contents.length; i++) {
        var li = $("<li>");
        var a = $("<a>");

        a.attr("href", contents[i].href);
        a.html(contents[i].title);

        a.appendTo(li);
        li.appendTo(ul);
    }
}

function generateSidebar(referenceContents, tutorialContents, userGuideContents, debugContents) {
    if (window.innerWidth < MIN_WINDOW_WIDTH) {
        isNavbarPresent = false;
        return;
    }

    isNavbarPresent = true;

    body = $("body");

    wrapper = $("<div>");
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

    var commonList = $("<ul>");

    var referenceList = $("<li>");
    referenceList.html("Грамматика");
    var referenceNestedList = $("<ul>");

    var tutorialList = $("<li>");
    tutorialList.html("Руководство по изучению языка");
    var tutorialNestedList = $("<ul>");

    var userGuideList = $("<li>");
    userGuideList.html("Руководство пользователя");
    var userGuideNestedList = $("<ul>");

    var debugList = $("<li>");
    debugList.html("Отладка моделей");
    var debugNestedList = $("<ul>");

    fillContents(referenceNestedList, referenceContents);
    referenceNestedList.appendTo(referenceList);
    referenceList.appendTo(commonList);

    fillContents(tutorialNestedList, tutorialContents);
    tutorialNestedList.appendTo(tutorialList);
    tutorialList.appendTo(commonList);

    fillContents(userGuideNestedList, userGuideContents);
    userGuideNestedList.appendTo(userGuideList);
    userGuideList.appendTo(commonList);

    fillContents(debugNestedList, debugContents);
    debugNestedList.appendTo(debugList);
    debugList.appendTo(commonList);

    header.appendTo(sidebarNav);
    commonList.appendTo(sidebarNav);

    body.wrapInner(pageContent);
    body.wrapInner(pageContentWrapper);
    sidebarWrapper.wrapInner(sidebarNav);
    sidebarWrapper.prependTo(body);
    body.wrapInner(wrapper);
}

function getReferenceContents() {
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
            "href":"../reference/logic.html",
            "title":"Группы активностей"
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
            "href":"../reference/result.html",
            "title":"Результаты моделирования"
        },
        {
            "href":"../reference/animation.html",
            "title":"Анимация"
        }
    ];

    return contents;
}

function getTutorialContents() {
    var contents = [
        {
            "href":"../tutorial/call_center_call_count.html",
            "title":"Модель подсчета количества звонков в службу технической поддержки"
        },
        {
            "href":"../tutorial/call_center_single_operator.html",
            "title":"Модель службы технической поддержки с одним оператором"
        }
    ];

    return contents;
}

function getUserGuideContents() {
    var contents = [
        {
            "href":"../user_guide/first_run.html",
            "title":"Первый запуск"
        },
        {
            "href":"../user_guide/model_create.html",
            "title":"Создание модели"
        },
        {
            "href":"../user_guide/model_run.html",
            "title":"Запуск модели"
        },
        {
            "href":"../user_guide/model_import.html",
            "title":"Импорт модели"
        },
        {
            "href":"../user_guide/show_animation.html",
            "title":"Включить анимацию"
        }
    ];

    return contents;
}

function getDebugContents() {
    var contents = [
        {
            "href":"../debug/trace.html",
            "title":"Трассировка"
        },
        {
            "href":"../debug/logger.html",
            "title":"Логирование"
        }
    ];

    return contents;
}

$(document).ready(function() {
    generateSidebar(getReferenceContents(), getTutorialContents(), getUserGuideContents(), getDebugContents());
    window.addEventListener('resize', () => {
        if (window.innerWidth < MIN_WINDOW_WIDTH && isNavbarPresent) {
        isNavbarPresent = false;
            $('#sidebar-wrapper').hide(500);
            $('.page-content').css('margin-left', '0%');
        }
        if (window.innerWidth > MIN_WINDOW_WIDTH && !isNavbarPresent) {
        isNavbarPresent = false;
            $('#sidebar-wrapper').show(500);
            isNavbarPresent = true;
            $('.page-content').css('margin-left', '33%');
        }
    });
});


