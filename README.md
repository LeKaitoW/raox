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

 * `Help` `>` `Eclipse Marketplace...` `>` `xtext` `>` `Install`
 * `Help` `>` `Install New Software...` `>` `Add` `>` `Name = emf` `>` `Location = http://download.eclipse.org/modeling/emf/updates/releases/` `>` `emf sdk` `>` `Next` ...
 * Download sources
```bash
ssh-add ~/.ssh/github.openssh.private.key
git clone git@github.com:aurusov/rdo-xtext.git
```

## Installing  
### Setting up the workspace for Eclipse
* `File` `>` `Import` `>` `General` `>` `Existing Projects into Workspace`
* Wait for the workspace to build and get tons of errors
* ru.bmstu.rk9.rdo/src/ru.bmstu.rk9.rdo/RDO.xtext `>` `Run As` `>` `Generate Xtext Artifacts`
``` 
1    [main] INFO  lipse.emf.mwe.utils.StandaloneSetup  - Registering platform uri '/home/drobus/git/rdo-xtext'
1028 [main] INFO  lipse.emf.mwe.utils.StandaloneSetup  - Adding generated EPackage 'org.eclipse.xtext.common.types.TypesPackage'
1035 [main] INFO  ipse.emf.mwe.utils.DirectoryCleaner  - Cleaning /home/drobus/git/rdo-xtext/ru.bmstu.rk9.rdo/../ru.bmstu.rk9.rdo/src-gen
1036 [main] INFO  ipse.emf.mwe.utils.DirectoryCleaner  - Cleaning /home/drobus/git/rdo-xtext/ru.bmstu.rk9.rdo/../ru.bmstu.rk9.rdo.ui/src-gen
1036 [main] INFO  ipse.emf.mwe.utils.DirectoryCleaner  - Cleaning /home/drobus/git/rdo-xtext/ru.bmstu.rk9.rdo/../ru.bmstu.rk9.rdo.tests/src-gen
5500 [main] INFO  clipse.emf.mwe.utils.GenModelHelper  - Registered GenModel 'http://www.bmstu.ru/rk9/rdo/RDO' from 'platform:/resource/ru.bmstu.rk9.rdo/model/generated/RDO.genmodel'
22337 [main] INFO  text.generator.junit.Junit4Fragment  - generating Junit4 Test support classes
22345 [main] INFO  text.generator.junit.Junit4Fragment  - generating Compare Framework infrastructure
22408 [main] INFO  .emf.mwe2.runtime.workflow.Workflow  - Done.
```
* `Run` `>` `Run Configurations...` `>` `Eclipse Application` > `New` `>` `Name = runtime-EclipseXtext` `>` `Location = ${workspace_loc}/../runtime-EclipseXtext` `>` `Run`
 * Eclipse Platform may freeze during its launch. This happens due to the unsufficient [permgen](http://wiki.eclipse.org/FAQ_How_do_I_increase_the_permgen_size_available_to_Eclipse%3F) size available to Eclipse. To prevent that, add `-XX:MaxPermSize=256M` to VM arguments in Run Configuration
* And that's it.
