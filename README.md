# RDO-xtext
## Summary
<img src=https://raw.githubusercontent.com/k-alexandrovsky/k-alexandrovsky.github.io/master/rdo-xtext.png><img>
This project is an implementation of RDO modelling language in Eclipse, using xtext.
* [About RDO modelling language (rus)](http://rdo.rk9.bmstu.ru/help/help/rdo_lang_rus/html/rdo_intro.htm)

## Preparing
 * Install [Ubuntu Desktop](http://www.ubuntu.com/download/desktop/) or any other [linux distribution](http://www.linux.com/directory/Distributions/desktop)
 * Download [Eclipse IDE for Java Developers](https://www.eclipse.org/downloads/)
```bash
cd ~/Downloads
gunzip eclipse-java-luna-SR1-linux-gtk-x86_64.tar.gz  | tar xvf -
tar -xf eclipse-java-luna-SR1-linux-gtk-x86_64.tar
cd eclipse
sudo apt-get install openjdk-8-jdk # for debian-based distributions
./eclipse
```
 * Download EMF runtime from (http://www.eclipse.org/modeling/emf/downloads/?). Lataest working version is [2.10.0](http://www.eclipse.org/downloads/download.php?file=/modeling/emf/emf/downloads/drops/2.10.0/R201405190339/emf-runtime-2.10.0.zip). *Note that `2.10.1` fails to build the project*
 * `Help` `>` `Install New Software...` `>` `Work with:` : `jar:file:/home/USERNAME/Downloads/emf-xsd-Update-2.10.0.zip!/` > Hit `Enter` > Select everything from the first item to `EMF Examples` **and** also `EMF SDK` > Install
 * `Help` `>` `Eclipse Marketplace...` `>` `Xtext` `>` `Install`
 * Git clone `rdo-xtext` repository
```bash
ssh-add ~/.ssh/github.openssh.private.key
git clone git@github.com:k-alexandrovsky/rdo-xtext.git
```
## Installing  
### Setting up the workspace for Eclipse
* `File` `>` `Import` `>` `General` `>` `Existing Projects into Workspace``>` `Select root directory` `>` `/home/USERNAME/git/rdo-xtext`
* Wait for the workspace to build and get tons of errors
* ru.bmstu.rk9.rdo/src/ru.bmstu.rk9.rdo/RDO.xtext `>` `Run As` `>` `Generate Xtext Artifacts`
``` 
0    [main] INFO  lipse.emf.mwe.utils.StandaloneSetup  - Registering platform uri '/home/kirill/dev/rdo-xtext'
1101 [main] INFO  lipse.emf.mwe.utils.StandaloneSetup  - Adding generated EPackage 'org.eclipse.xtext.common.types.TypesPackage'
1110 [main] INFO  ipse.emf.mwe.utils.DirectoryCleaner  - Cleaning /home/kirill/dev/rdo-xtext/ru.bmstu.rk9.rdo/../ru.bmstu.rk9.rdo/src-gen
1140 [main] INFO  ipse.emf.mwe.utils.DirectoryCleaner  - Cleaning /home/kirill/dev/rdo-xtext/ru.bmstu.rk9.rdo/../ru.bmstu.rk9.rdo/model
1140 [main] INFO  ipse.emf.mwe.utils.DirectoryCleaner  - Cleaning /home/kirill/dev/rdo-xtext/ru.bmstu.rk9.rdo/../ru.bmstu.rk9.rdo.ui/src-gen
1143 [main] INFO  ipse.emf.mwe.utils.DirectoryCleaner  - Cleaning /home/kirill/dev/rdo-xtext/ru.bmstu.rk9.rdo/../ru.bmstu.rk9.rdo.tests/src-gen
6771 [main] INFO  clipse.emf.mwe.utils.GenModelHelper  - Registered GenModel 'http://www.bmstu.ru/rk9/rdo/RDO' from 'platform:/resource/ru.bmstu.rk9.rdo/model/generated/RDO.genmodel'
31175 [main] INFO  text.generator.junit.Junit4Fragment  - generating Junit4 Test support classes
31186 [main] INFO  text.generator.junit.Junit4Fragment  - generating Compare Framework infrastructure
31267 [main] INFO  .emf.mwe2.runtime.workflow.Workflow  - Done.
```
>**[!]** *If your output differs from the one above by a lot of errors mentioning* `RULE_ANY_OTHER`*, you should run the generation process again and again until the bulid is succesfull. This is Xtext/Antlr bug caused by complex rules supporting unicode identifiers in grammar, sorry for the inconvenience*

* `Run` `>` `Run Configurations...` `>` `Eclipse Application` `>` `New` `>` `Name = runtime-EclipseXtext` `>` `Location = ${workspace_loc}/../runtime-EclipseXtext` `>` `Run`
 * *Ignore this if you use Java version 8 or later.* Eclipse Platform may freeze during its launch. This happens due to the unsufficient [permgen](http://wiki.eclipse.org/FAQ_How_do_I_increase_the_permgen_size_available_to_Eclipse%3F) size available to Eclipse. To prevent that, add `-XX:MaxPermSize=256M` to VM arguments in Run Configuration.
* And that's it.

## Running  
* `Window` `>` `Open Perspective` `>` `Other...` `>` `RDO`
* `File` `>` `New` `>` `Project...` `>` `Java Project` `>` `Next>` `>` `Project name:` `>` set project name `>` `Project layout` `>` `Configure default` `>` `Source folder name = src-gen` `>` `OK` `>` `Finish`
 * `Do you want to open this perspective now?` `>` `No`
* Right-click on created project `>` `New` `>` `File` `>` `File name:` `>` filename.rdo (must be valid java identificator) `>` `Finish`
 * `Do you want to add the Xtext nature to the project?` `>` `Yes`
