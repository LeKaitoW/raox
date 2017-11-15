# Game 5 plugin
## Компиляция

```bash
git clone git@github.com:aurusov/raox.git
git clone git@github.com:aurusov/raox-game5.git
cd raox
mvn initialize -N -Pset-git-version
mvn deploy -Drepository-url=file:///home/"$USER"/__repository__
cd ../raox-game5
mvn package
rm -rf /home/"$USER"/__repository__
```

## Запуск
- Скачать [Eclipse IDE for Java and DSL Developers](http://www.eclipse.org/downloads/packages/eclipse-ide-java-and-dsl-developers/neonr)
- Скопировать артефакты в папаку `eclipse_path/dropins`
  - `raox/assembly/target/plugins/ru.bmstu.rk9.rao-<version>.jar`
  - `raox/assembly/target/plugins/ru.bmstu.rk9.rao.lib-<version>.jar`
  - `raox/assembly/target/plugins/ru.bmstu.rk9.rao.ui-<version>.jar`
  - `raox/assembly/target/raox-copy-rename/ru.bmstu.rk9.rao.lib.jar`
  - `raox-game5/assembly/target/plugins/ru.bmstu.rk9.raox.plugins.game5-<version>.jar`
- Запустить Эклипс и создать модель через визард `Rao X Game5 Project`
