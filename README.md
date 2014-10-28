# RDO-xtext
## Summary
This project is an implementation of RDO modelling language in Eclipse, using xtext.
* [About RDO modelling language (rus)](http://rdo.rk9.bmstu.ru/help/help/rdo_lang_rus/html/rdo_intro.htm)

## Preparing
 * Install [Ubuntu Desktop](http://www.ubuntu.com/download/desktop/)
 * Download [Eclipse IDE for Java Developers](https://www.eclipse.org/downloads/)
```bash
cd Загрузки
gunzip eclipse-java-luna-SR1-linux-gtk-x86_64.tar.gz
tar -xf eclipse-java-luna-SR1-linux-gtk-x86_64.tar
cd eclipse
sudo apt-get install openjdk-8-jdk
./eclipse
```

 * Help `>` Eclipse Marketplace... `>` xtext `>` Install
 * Download sources
```bash
ssh-add ~/.ssh/github.openssh.private.key
git clone git@github.com:aurusov/rdo-xtext.git
```

## Installing  
### Setting up the workspace for Eclipse
* File `>` Import existing projects into workspace (with repo as root directory)
* Wait for the workspace to build and get tons of errors
* ru.bmstu.rk9.rdo/src/ru.bmstu.rk9.rdo/RDO.xtext `>` Run As `>` Generate Xtext Artifacts
* Run `>` Run Configurations... `>` Eclipse Application > New  
    e.g. `runtime-EclipseXtext` with location `${workspace_loc}/../runtime-EclipseXtext`
 * Eclipse Platform may freeze during its launch. This happens due to the unsufficient [permgen](http://wiki.eclipse.org/FAQ_How_do_I_increase_the_permgen_size_available_to_Eclipse%3F) size available to Eclipse. To prevent that, add `-XX:MaxPermSize=256M` to VM arguments in Run Configuration
* And that's it.
