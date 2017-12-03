# Именование бранчей

|Имя ветки       | Описание |
|----------------|----------|
|master          | Ветка содержит только то, что выехало в прод. После мержа в ```master``` необходимо назначить ```tag``` с номером версии, чтобы она стала отображаться в [релизах](https://github.com/aurusov/raox/releases). Нумерация по правилам [deb-version](http://manpages.ubuntu.com/manpages/natty/man5/deb-version.5.html). Если кратко, то версия состоит из трёх чисел ```major.minor.fix```. Если в новой версии теряется обратная совместимость, то увеличивается ```major```. Если добавили новую фичу - ```minor```. Если исправили баг - ```fix```.
|release/current | Основная ветка. Как правило, совпадает с ```master```. Когда в мастер выехал новый релиз или hotfix, то его надо подмержить в ```release/current```. Подмержить именно ```master -> release/current```, а не релиз или hotfix. От этой ветки создаются бранчи для разработки фич и релизные ветки. Используется для рефакторинга кода - он заливается прямо сюда. Т.е. вновь созданные фичи будут включать отрефакторенный код даже до выхода нового релиза. А в текущие фичи-ветки рефакторинг подмерживается руками.|
|release/version | Создаётся от ```release/current```. Используется для подготовки нового номерного релиза. Имеет формат ```release/major.minor```, например, ```release/1.2```. Означает, что мы или выпускаем продукт с новой фичей под версией ```1.2.0``` или правим баг. Если баг, то окончательная версия может быть ```1.2.7```. В эту ветку подливаются все фичи-ветки, которые готовятся к релизу. Важно - это ветка может быть удалена в любой момент и пересоздана. Никаких коммитов с кодом в ней быть не должно. Только мержи фичи-веток. Когда ветка готова к выходу в прод, отдельным коммитом изменяются версии командой <pre>mvn org.eclipse.tycho:tycho-versions-plugin:set-version "-DnewVersion=&lt;New Version&gt;"</pre> или вручную в файлах<ul><li> [`ru.bmstu.rk9.rao.lib/META-INF/MANIFEST.MF`](/ru.bmstu.rk9.rao.lib/META-INF/MANIFEST.MF#L5)</li><li>[`ru.bmstu.rk9.rao.sdk/feature.xml`](/ru.bmstu.rk9.rao.sdk/feature.xml#L4)</li><li>[`ru.bmstu.rk9.rao.tests/META-INF/MANIFEST.MF`](/ru.bmstu.rk9.rao.tests/META-INF/MANIFEST.MF#L5)</li><li>[`ru.bmstu.rk9.rao.ui/META-INF/MANIFEST.MF`](ru.bmstu.rk9.rao.ui/META-INF/MANIFEST.MF#L5)</li><li>[`ru.bmstu.rk9.rao/META-INF/MANIFEST.MF`](/ru.bmstu.rk9.rao/META-INF/MANIFEST.MF#L5)</li><li>[`pom.xml`](/pom.xml#L21)</li><li>[`assembly/pom.xml`](assembly/pom.xml#L9)</li><li>[`ru.bmstu.rk9.rao/pom.xml`](ru.bmstu.rk9.rao/pom.xml#L9)</li><li>[`ru.bmstu.rk9.rao.lib/pom.xml`](ru.bmstu.rk9.rao.lib/pom.xml#L9)</li><li>[`ru.bmstu.rk9.rao.sdk/pom.xml`](ru.bmstu.rk9.rao.sdk/pom.xml#L9)</li><li>[`ru.bmstu.rk9.rao.tests/pom.xml`](ru.bmstu.rk9.rao.tests/pom.xml#L9)</li><li>[`ru.bmstu.rk9.rao.ui/pom.xml`](ru.bmstu.rk9.rao.ui/pom.xml#L9)</li><li>[`ru.bmstu.rk9.rao.querydsl/pom.xml`](ru.bmstu.rk9.rao.querydsl/pom.xml#L9)</li></ul>После этого ветка мержится в мастер. После мержа должна быть удалена вместе со всем выехавшими фича-ветками.|
feature/name    | Создаётся от ```release/current```. Используется для разработки нового функционала (фичи). В конце работы должна быть влита в ```release/version```. В неё в любой момент времени может быть подлит ```release/current```. С другими ветками она не должна мержиться, потому что в релиз поедет непонятно что.
bug/name        | Создаётся от ```release/current```. Используется для исправления ошибок, если фича-ветка уже удалена. На практике нужна редко, потому что разработка и отладка фичи довольно долго ведётся в её ветке.
refactoring/name | Создаётся от ```release/current```. Используется для рефакторинга существующего кода. В конце работы должна быть влита сразу в ```release/current```. Версия проекта в результате рефакторинга не изменяется.
hotfix/name     | Создаётся от ```master```. Используется для срочного исправления кода в мастере, в обход всех веток. Результат мержится в мастер и ветка удаляется. Как всегда, код из мастера подмерживается в ```release/current``` и далее по фичам. Даже не знаю, когда такое может понадобиться при разработке РДО. Написано для полноты картины.

# Что там с форками
Имя ветки                            | Описание
-------------------------------------|---------
master и resease/current             | Живут только в основном репозитории. Должны быть уникальными на все форки.
release/version                      | Живёт в форке отвественного за релиз. Ветка должна быть уникальной на все форки. По умолчанию в основном репозитории
feature/name, bug/name, refactoring/name, hotfix/name | Живут в том форке, где по ним ведётся разработка

# Как подлить результаты работы в корневой репозиторий
Скорее всего, ваша фича-ветка в конце разработки будет отставать от release/current корневого репозитория. Потому что ваши коллеги могли сделать курсач раньше и подлить туда наработки. Проверить это можно через [Network](https://github.com/aurusov/raox/network). Поэтому сначала надо подлить в фича-ветку release/current корневого репозитория:
- [Перед мержем](https://help.github.com/articles/configuring-a-remote-for-a-fork/), в нашем случае ```git remote add aurusov git@github.com:aurusov/raox.git```
- [Инструкция](https://help.github.com/articles/syncing-a-fork/), как мержиться с форком, в нашем случае ```git merge aurusov/release/current```

Во время мержа могут возникнуть конфликты. Их надо разрешить руками, вникая в изменения. В идеальном мире вы даже пообщаетесь голосом с людьми, код которых конфликтует с вашим. После разрешения всех конфликтов заливаете изменения к себе в фича-ветку. Рекомендуется **как можно чаще** мержиться с release/current корневого репозитория в процессе работы над курсовым. Это резко снижает вероятность конфликтов в конце полугодовой работы.

После мержа надо поставить пул-реквест на релиз с номером в корневом репозитории, например, на ветку ```release/1.2``` . Если такой ветки нет, то её должен создать владелец корневого репозитория, напишите ему. На пул-реквесте код обсуждается, по необходимости вносятся правки. В конце процесса нажимается кнопка ```Merge``` в гитхабе. Система предложит степерь фича-ветку - не надо этого делать. Фича-ветка удаляется руками тогда, когда релиз с номером замержится в ```master```.

# Тестирование

У нас имеются [регрессионные тесты](https://ru.wikipedia.org/wiki/Регрессионное_тестирование). Их можно запустить локально. Для этого
- Скачать [Test Runner](http://www.eclipse.org/rcptt/download/) (справа на странице, прямая ссылка на [rcptt.runner-2.1.0.zip](http://ftp-stud.fht-esslingen.de/pub/Mirrors/eclipse/rcptt/release/2.1.0/runner/rcptt.runner-2.1.0.zip)), например, в `~/bin/rcptt.runner`
- Скачать [Eclipse IDE for Java and DSL Developers](http://www.eclipse.org/downloads/packages/eclipse-ide-java-and-dsl-developers/neonr) или скопировать ваш существующий, например, в `~/bin/eclipse-test`
- Удалить старые плагины, переписать тестируемые в `~/bin/eclipse-test/plugins`. Пример со сборкой из исходников
```bash
git clone git@github.com:aurusov/raox.git
cd raox
mvn initialize -N -Pset-git-version
mvn -U clean package
rm -f ~/bin/eclipse-test/dropins/ru.bmstu.rk9.*
rm -f ~/bin/eclipse-test/plugins/ru.bmstu.rk9.*
cp ./assembly/target/plugins/* ~/bin/eclipse-test/plugins
```
Важно
  - переписать надо в папку `plugins`, а не `dropins`
  - в списоке плагинов не должно быть пятнашек
  
- Запустить тесты из корня с исходниками
```bash
./ru.bmstu.rk9.rao.rcptt/run_tests.sh ~/bin/rcptt.runner ~/bin/eclipse-test
```
Не двигать мышкой, пока они идут. Посмотреть отчет. Если в конце будет `Failed Tests:`, то надо разбираться. Должно быть что-то такое:
```
drobus@drobus-mac:~/git/raox$ ./ru.bmstu.rk9.rao.rcptt/run_tests.sh ~/bin/rcptt.runner ~/bin/eclipse-test
Started at Tue Oct 25 19:12:33 MSK 2016
RCPTT Runner version: 2.1
Initializing target platform...
Target platform is valid.
Looking for tests...
Existing projects in workspace:
    <none>
Importing projects to workspace:
Refreshing projects:
    /home/drobus/git/raox/ru.bmstu.rk9.rao.rcptt... OK
Refreshing projects:
    ru.bmtsu.rk9.rao.rcptt... OK
Searching for tests in projects:
    ru.bmtsu.rk9.rao.rcptt... 
Complete OK
Testcase Artifacts:35
AUT-0:Launching
AUT-0:Product: org.eclipse.epp.package.dsl.product
AUT-0:Application: org.eclipse.ui.ide.workbench
AUT-0:Architecture: x86_64
64bit arch is selected because AUT uses launcher library
	"plugins/org.eclipse.equinox.launcher.gtk.linux.x86_64_1.1.400.v20160518-1444" specified in config file: eclipse.ini
/home/drobus/bin/rcptt.runner/results/aut-workspace-0: AUT arguments: -os ${target.os} -arch ${target.arch} -consoleLog
/home/drobus/bin/rcptt.runner/results/aut-workspace-0: AUT VM arguments: -Dosgi.requiredJavaVersion=1.8 -XX:+UseG1GC -XX:+UseStringDeduplication -Dosgi.requiredJavaVersion=1.8 -Xms256m -Xmx1024m -XX:MaxPermSize=128m
AUT-0: Launch failed. Reason: Process was terminated while waiting for AUT startup data
AUT-0: For more information check AUT output at '/home/drobus/bin/rcptt.runner/results/aut-out-0_console.log'
Failed to launch AUT:AUT-0: Launch failed. Reason: Process was terminated while waiting for AUT startup data
AUT-0:Launching
AUT-0:Product: org.eclipse.epp.package.dsl.product
AUT-0:Application: org.eclipse.ui.ide.workbench
AUT-0:Architecture: x86_64
64bit arch is selected because AUT uses launcher library
	"plugins/org.eclipse.equinox.launcher.gtk.linux.x86_64_1.1.400.v20160518-1444" specified in config file: eclipse.ini
/home/drobus/bin/rcptt.runner/results/aut-workspace-0: AUT arguments: -os ${target.os} -arch ${target.arch} -consoleLog
/home/drobus/bin/rcptt.runner/results/aut-workspace-0: AUT VM arguments: -Dosgi.requiredJavaVersion=1.8 -XX:+UseG1GC -XX:+UseStringDeduplication -Dosgi.requiredJavaVersion=1.8 -Xms256m -Xmx1024m -XX:MaxPermSize=128m
Pass 1 (35) processed. 0 failed. spent: 0:21, 5:57 mins remaining. base_java_project. time: 20547ms 
Pass 2 (35) processed. 0 failed. spent: 0:40, 5:20 mins remaining. continuous_histogram_sequence. time: 18102ms 
Pass 3 (35) processed. 0 failed. spent: 0:51, 5:16 mins remaining. continuous_histogram_sequence_notrace. time: 10679ms 
Pass 4 (35) processed. 0 failed. spent: 0:59, 4:55 mins remaining. model_accessible_all_conflict. time: 8241ms 
Pass 5 (35) processed. 0 failed. spent: 1:07, 4:37 mins remaining. model_accessible_all_conflict_notrace. time: 7505ms 
Pass 6 (35) processed. 0 failed. spent: 1:14, 4:19 mins remaining. model_accessible_resources. time: 6522ms 
Pass 7 (35) processed. 0 failed. spent: 1:22, 4:06 mins remaining. model_accessible_resources_notrace. time: 7088ms 
Pass 8 (35) processed. 0 failed. spent: 1:28, 3:48 mins remaining. model_barber_clients. time: 5845ms 
Pass 9 (35) processed. 0 failed. spent: 1:35, 3:35 mins remaining. model_barber_clients_notrace. time: 6048ms 
Pass 10 (35) processed. 0 failed. spent: 1:39, 3:18 mins remaining. model_barber_events. time: 4456ms 
Pass 11 (35) processed. 0 failed. spent: 1:44, 3:04 mins remaining. model_barber_events_notrace. time: 4039ms 
Pass 12 (35) processed. 0 failed. spent: 1:49, 2:51 mins remaining. model_barber_simple. time: 4467ms 
Pass 13 (35) processed. 0 failed. spent: 1:54, 2:39 mins remaining. model_barber_simple_notrace. time: 5110ms 
Pass 14 (35) processed. 0 failed. spent: 2:01, 2:31 mins remaining. model_discrete_histogram_sequence. time: 6905ms 
Pass 15 (35) processed. 0 failed. spent: 2:08, 2:23 mins remaining. model_discrete_histogram_sequence_notrace. time: 6499ms 
Pass 16 (35) processed. 0 failed. spent: 2:12, 2:12 mins remaining. empty_model. time: 3543ms 
Pass 17 (35) processed. 0 failed. spent: 2:19, 2:04 mins remaining. model_exponential_sequence. time: 6841ms 
Pass 18 (35) processed. 0 failed. spent: 2:25, 1:56 mins remaining. model_exponential_sequence_notrace. time: 5413ms 
Pass 19 (35) processed. 0 failed. spent: 2:35, 1:50 mins remaining. model_game5. time: 10145ms 
Pass 20 (35) processed. 0 failed. spent: 2:44, 1:44 mins remaining. model_game5_notrace. time: 8428ms 
Pass 21 (35) processed. 0 failed. spent: 2:50, 1:36 mins remaining. model_generator_sequence. time: 6109ms 
Pass 22 (35) processed. 0 failed. spent: 2:57, 1:28 mins remaining. model_generator_sequence_notrace. time: 6930ms 
Pass 23 (35) processed. 0 failed. spent: 3:03, 1:20 mins remaining. model_list_sequence. time: 5902ms 
Pass 24 (35) processed. 0 failed. spent: 3:10, 1:13 mins remaining. model_list_sequence_notrace. time: 6280ms 
Pass 25 (35) processed. 0 failed. spent: 3:17, 1:05 mins remaining. model_normal_sequence. time: 6523ms 
Pass 26 (35) processed. 0 failed. spent: 3:23, 0:58 mins remaining. model_normal_sequence_notrace. time: 6234ms 
Pass 27 (35) processed. 0 failed. spent: 3:30, 0:50 mins remaining. model_relevants_combination. time: 6916ms 
Pass 28 (35) processed. 0 failed. spent: 3:36, 0:43 mins remaining. model_relevants_combination_notrace. time: 5685ms 
Pass 29 (35) processed. 0 failed. spent: 3:42, 0:35 mins remaining. model_relevants_no_combination. time: 5799ms 
Pass 30 (35) processed. 0 failed. spent: 3:49, 0:28 mins remaining. model_relevants_no_combination_notrace. time: 6898ms 
Pass 31 (35) processed. 0 failed. spent: 3:56, 0:21 mins remaining. model_triangular_sequence. time: 6098ms 
Pass 32 (35) processed. 0 failed. spent: 4:03, 0:14 mins remaining. model_triangular_sequence_notrace. time: 6368ms 
Pass 33 (35) processed. 0 failed. spent: 4:09, 0:07 mins remaining. model_uniform_sequence. time: 5658ms 
Pass 34 (35) processed. 0 failed. spent: 4:15, 0:00 mins remaining. model_uniform_sequence_notrace. time: 6410ms 
Pass 35 (35) processed. 0 failed. spent: 4:17, 0:00 mins remaining. rao_perspective. time: 1604ms 
Process terminated. Shut down AUTs
Finished at Tue Oct 25 19:17:55 MSK 2016
```

## Как добавить регрессионный тест

- Установить [RCPTT UI testing tool](https://marketplace.eclipse.org/content/rcptt-eclipse-ui-testing-tool)

Состав тестировочной среды
- Общий контекст **common_procedures.ctx**, в котором хранятся вспомогательные функции-скрипты
- Тесты
  - Подготовительный контекст ***_prepare.ctx**, в котором описано рабочее пространство теста
  - Непосредственно тесты ***.test**, в которых описаны вызовы функций-скриптов из **common_procedures.ctx**
  - Вспомогательные файлы, например,  шаблон модели или эталоны трассировки
  
  Чтобы добавить новый тест необхожимо:
  - Добавить в проекте папку для теста
  - Добавить все необходимые вспомогательные файлы
  - Добавить подготовительный контекст и в редакторе во вкладке **workspace** включить в него вспомогательные файлы
  - Добавить и описать тест
    - В редакторе во вкладке **context** включить базовый и подготовительные контексты
    - В редакторе во вкладке **script** вызвать или описать функцию-скрипт, или записать последовательность использую кнопку **Record**
    
  Список доступных функций-сриптов:
  Функция-скрипт              | Параметры        | Описание
------------------------------------------------------
open_perspective            | perspective_name | Открытие перспективы **perspective_name**
create_rao_project          | template_name    | Создание проекта Rao X из шаблона **template_name**
open_rao_perspective        |                  | Открытие перспективы Rao
open_java_perspective       |                  | Открытие перспективы Java
copy_model                  | model_name       | Включение трассировки для модели **model_name**
compare_trace               | model_name       | Копирование исходного текста модели **model_name** из файла **model_name**
compare_result              | model_name       | Копирование исходного текста модели **model_name** из файла **model_name**
enable_monitoring           | model_name       | Валидация совпадения трассировки для модели **model_name** с эталоном из **model_name**
execute_model               | model_name       | Запуск модели **model_name**
test_model_template_notrace | template_name    | Запуск тестового запуска модели из шаблона **template_name**
test_model_template_trace   | template_name    | Запуск тестового запуска модели из шаблона **template_name** с валидацией трассировки
test_model_source_notrace   | model_name       | Запуск тестового запуска модели с текстом исходного кода из файла **model_name**
test_model_source_trace     | model_name       | Запуск тестового запуска модели с текстом исходного кода из файла **model_name** с валидацией трассировки
test_model_source_result    | model_name       | Запуск тестового запуска модели с текстом исходного кода из файла **model_name** с валидацией результатов

Функции-скрипты можно описать [вручную](https://www.eclipse.org/rcptt/documentation/userguide/procedures/) или используя кнопку **Record**  
