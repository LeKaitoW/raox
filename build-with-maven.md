#Сборка через maven
* Общие положения 
* Автосборка на Дженкинсе
  - только джарники
  - архив с Эклипсом
* Продвижение версии

##Общие положения
```
[export MAVEN_OPTS="-Xmx512M"]
[mvn initialize -N -Pset-git-version]
mvn [clean] package [-Declipse-path=<path-to-eclipse-root>] [-Dtarget-os=linux|win|mac] [-Dtarget-bitness=x64|x86]
```
```export MAVEN_OPTS="-Xmx512M"``` - Устанавливает максимальное количество выделяемой памяти.
Необходима, если сборка выдает ошибку ```java heap space```.<br>
Под Windows устанавливается командой ```set MAVEN_OPTS="-Xmx512M"``` или настройкой переменных среды.

```mvn initialize -N -Pset-git-version``` - Запускает фазу ```initialize``` с флагом ```-N (--non-recursive)``` и профилем ```set-git-version```.<br>
Необходима, если результаты сборки используются не только разрабочиком, например, в автосборке на Дженкинсе.<br>
В профиле ```set-git-version``` используются плагины, небходимые для установки версии проекта на основе 
```
git describe --tags
```

```-N (--non-recursive)``` Запускает только родительский pom.xml. Без этого флага версия поменяется, но сборка завершится с ошибкой, что критично для сборки на Дженкинсе.

```mvn [clean] package``` - Запускает фазу ```clean``` и следом за ней фазу ```package``` идентично<br>
```
[mvn clean]
mvn package
```
```mvn clean``` - Удаляет бинарники и содержимое папок ```target```.<br>
```mvn package``` - Запускает сборку.
Результатом сборки является архив с плагинами ```assembly\target\rao-<version>-plugins.zip```.
Например 
- ```rao-2.8.0.12-gea41589-plugins.zip``` для не релизной сборки
- ```rao-2.8.0-plugins.zip``` для релизной

А также папка с этими плагинами
- ```assembly\target\plugins\ru.bmstu.rk9.rao.lib-<version>.jar```
- ```assembly\target\plugins\ru.bmstu.rk9.rao.ui-<version>.jar```
- ```assembly\target\plugins\ru.bmstu.rk9.rao-<version>.jar```
```
mvn clean package [-Declipse-path=<path-to-eclipse-root>] [-Dtarget-os=linux|win|mac] [-Dtarget-bitness=x64|x86]
```
Запускает сборку, результатом которой является готовый к развертыванию архив, содержащий ```eclipse``` и плагины из пункта выше, подставленные в папку ```dropins```.<br>
Параметр ```eclipse-path``` указывает путь до корневой папки ```eclipse```. Значение ```<path-to-eclipse-root>``` задается абсолютным путем или относительно папки ```assembly```.
Если путь содержит пробелы, то кавычки ставятся вокруг параметра целиком, т.е.
```
mvn clean package "-Declipse-path=C:\path with spaces\eclipse"
```

Параметры ```target-os``` и ```target-bitness``` задают операционную систему и битность целевой платформы соответсвенно. Влияют на название получаемого архива: ```rao-<version>-<target-os>-<target-platform>.zip``` Имеют смысл только для сборки с параметром ```-Declipse-path```, обязательны для сборки на Дженкинсе.
##Автосборка на Дженкинсе
### Только джарники
```
export MAVEN_OPTS="-Xmx512M"
mvn initialize -N -Pset-git-version
mvn clean package
```
### Архив с Эклипсом
```
export MAVEN_OPTS="-Xmx512M"
mvn initialize -N -Pset-git-version
mvn clean package -Declipse-path=../../../../eclipses/eclipse-dsl-mars-1-linux-gtk-x64 -Dtarget-os=linux -Dtarget-bitness=x64
```
##Продвижение версии
Продвижение версии можно посмотреть на [gitflow.md](/gitflow.md) в разделе ```release/version```
