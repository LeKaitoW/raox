#Сборка через maven

##Общие положения
```
[export MAVEN_OPTS="-Xmx512M"]
[mvn initialize -N -Pset-git-version]
mvn [clean] package [-Declipse-path=<path-to-eclipse-root>] [-Dtarget-os=linux|win|mac] [-Dtarget-bitness=x64|x86]
[mvn deploy -Drepository-url=<url-to-repository-root>]
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

- ```mvn clean package [-Declipse-path=<path-to-eclipse-root>] [-Dtarget-os=linux|win|mac] [-Dtarget-bitness=x64|x86]``` - Запускает сборку, результатом которой является готовый к развертыванию архив, содержащий ```eclipse``` и плагины из пункта выше, подставленные в папку ```dropins```.<br>
Параметр ```eclipse-path``` указывает путь до корневой папки ```eclipse```. Значение ```<path-to-eclipse-root>``` задается абсолютным путем или относительно папки ```assembly```.
Если путь содержит пробелы, то кавычки ставятся вокруг параметра целиком, т.е.
 ```
mvn clean package "-Declipse-path=C:\path with spaces\eclipse"
```

 Параметры ```target-os``` и ```target-bitness``` задают операционную систему и битность целевой платформы соответсвенно. Влияют на название получаемого архива: ```rao-<version>-<target-os>-<target-platform>.zip``` Имеют смысл только для сборки с параметром ```-Declipse-path```, обязательны для сборки на Дженкинсе.


- `mvn deploy` - Запускает сборку и развертывает полученные артифакты в репозитории.

 ```
mvn deploy -Drepository-url=<url-to-repository-root>
```
Параметр `repository-url` задает адрес репозитория, который по умолчанию не задан, поэтому является обязательным при запуске сборки с развертыванием.

##Автосборка на Дженкинсе
### Только джарники
```
export MAVEN_OPTS="-Xmx512M"
mvn initialize -N -Pset-git-version
mvn clean deploy -Drepository-url=file:///home/rdo/nexus/rdo-work/nexus/storage/raox-m2/

```
### Архив с Эклипсом
```
export MAVEN_OPTS="-Xmx512M"
mvn initialize -N -Pset-git-version
mvn clean package -Declipse-path=../../../../eclipses/eclipse-dsl-mars-1-linux-gtk-x64 -Dtarget-os=linux -Dtarget-bitness=x64
```
##Продвижение версии
Продвижение версии можно посмотреть на [gitflow.md](/gitflow.md) в разделе ```release/version```
