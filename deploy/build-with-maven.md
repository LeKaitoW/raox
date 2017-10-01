## Автосборка архива с эклипсом на Дженкинсе
``` bash
export MAVEN_OPTS="-Xmx512M"
cd assembly
mvn initialize -N -Pset-raox-version
mvn clean package -Declipse-path=../../../../eclipses/eclipse-dsl-mars-1-linux-gtk-x64 -Dtarget-os=linux -Dtarget-bitness=x64
```

`mvn initialize -N -Pset-raox-version` - Запускает вспомогательную команду, которая конфигурирует правильное название архива в последующей сборке.

`mvn clean package [-Declipse-path=<path-to-eclipse-root>] [-Dtarget-os=linux|win|mac] [-Dtarget-bitness=x64|x86]` - Запускает сборку, результатом которой является готовый к развертыванию архив, содержащий ```eclipse``` и подставленные в папку `dropins` плагины
- [aurusov/rdo-xtext](https://github.com/aurusov/rdo-xtext)
- [aurusov/raox-game5](https://github.com/aurusov/raox-game5)

Параметр `eclipse-path` указывает путь до корневой папки `eclipse`. Значение `<path-to-eclipse-root>` задается абсолютным путем или относительно папки `assembly`.
Если путь содержит пробелы, то кавычки ставятся вокруг параметра целиком, т.е.
```
mvn clean package "-Declipse-path=C:\path with spaces\eclipse"
```

Параметры ```target-os``` и ```target-bitness``` задают операционную систему и битность целевой платформы соответсвенно. Влияют на название получаемого архива: ```rao-<version>-<target-os>-<target-platform>.zip```
