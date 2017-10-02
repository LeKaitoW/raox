# Сборка через maven

## Общие положения
```
[export MAVEN_OPTS="-Xmx512M"]
[mvn initialize -N -Pset-git-version]
mvn [clean] package|deploy -Drepository-url=<url-to-repository-root> [-Pdebug]
```
- `export MAVEN_OPTS="-Xmx512M"` - Устанавливает максимальное количество выделяемой памяти.
Команда необходима, если сборка выдает ошибку `java heap space`. Под Windows `set MAVEN_OPTS="-Xmx512M"` или через настройки переменных среды.

- `mvn initialize -N -Pset-git-version` - Устанавливает версию бинарников на основе гита. Обязательная команда, если бинарники передаются пользователям.
 - `-N` или `--non-recursive` - Запускает только родительский pom.xml. Без этого флага версия поменяется, но сборка завершится с ошибкой, что критично для сборки на Дженкинсе.
 - `-Pset-git-version` - Определает и устанавливает версию на основе `git describe --tags`

- `mvn [clean] package` - Запускает очистку (`clean`) и следом за ней сборку (`package`) проекта, как если бы они запускались отдельно<br>
 ```
[mvn clean]
mvn package
```
`mvn clean` - Удаляет бинарники и содержимое папок `target`.<br>
`mvn package` - Запускает сборку.
Результаты сборки
 - архив с плагинами `assembly\target\rao-<version>-plugins.zip`. Например
    - `rao-2.8.0.12-gea41589-plugins.zip` для не релизной сборки
    - `rao-2.8.0-plugins.zip` для релизной

 - папка с плагинами
    - ```assembly\target\plugins\ru.bmstu.rk9.rao.lib-<version>.jar```
    - ```assembly\target\plugins\ru.bmstu.rk9.rao.ui-<version>.jar```
    - ```assembly\target\plugins\ru.bmstu.rk9.rao-<version>.jar```

- `mvn deploy` - Запускает сборку и развертывает полученные артифакты в репозитории.

 ```
mvn deploy -Drepository-url=<url-to-repository-root>
```
Параметр `repository-url` задает адрес репозитория, который по умолчанию не задан, поэтому является обязательным при запуске сборки с развертыванием.

- `-Pdebug` - Добавляет в сборку jar'ники с исходниками, которые при необходимости отладки можно использовать в `eclipse` через `attach source` 
    - ```assembly\target\plugins\ru.bmstu.rk9.rao.lib-<version>-sources.jar```
    - ```assembly\target\plugins\ru.bmstu.rk9.rao.ui-<version>-sources.jar```
    - ```assembly\target\plugins\ru.bmstu.rk9.rao-<version>-sources.jar```

## Автосборка на Дженкинсе
### Только джарники
```
export MAVEN_OPTS="-Xmx512M"
mvn initialize -N -Pset-git-version
mvn clean deploy -Drepository-url=file:///home/rdo/nexus/rdo-work/nexus/storage/raox-m2/

```
### Архив с Эклипсом
Cобирается в [aurusov/raox-deploy](https://github.com/aurusov/raox-deploy)
```
export MAVEN_OPTS="-Xmx512M"
cd assembly
mvn initialize -N -Pset-raox-version
mvn clean package -Declipse-path=../../../../eclipses/eclipse-dsl-mars-1-linux-gtk-x64 -Dtarget-os=linux -Dtarget-bitness=x64
```
## Продвижение версии
Продвижение версии можно посмотреть на [gitflow.md](/gitflow.md) в разделе ```release/version```
