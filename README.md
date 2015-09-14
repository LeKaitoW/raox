# RAO-XT
## Summary
<img src=docs/rdo-xtext.png><img>
This project is an implementation of RDO modelling language in Eclipse, using xtext.
* [About RAO modelling language (rus)](http://rdo.rk9.bmstu.ru/help/help/rdo_lang_rus/html/rdo_intro.htm)

## Preparing
 * Install [Ubuntu Desktop](http://www.ubuntu.com/download/desktop/) or any other [linux distribution](http://www.linux.com/directory/Distributions/desktop)
 * Download [Eclipse IDE for Java and DSL Developers](http://www.eclipse.org/downloads/packages/eclipse-ide-java-and-dsl-developers/lunasr2)
```bash
sudo apt-get install openjdk-8-jdk # for debian-based distributions
cd ~/Downloads
gunzip -c eclipse-dsl-luna-SR2-linux-gtk-x86_64.tar.gz  | tar xvf -
cd eclipse
./eclipse
```
 * Git clone `rdo-xtext` repository
```bash
ssh-add ~/.ssh/github.openssh.private.key
git clone git@github.com:aurusov/rdo-xtext.git
```
## Installing
### Setting up the workspace for Eclipse
* `File` `>` `Import` `>` `General` `>` `Existing Projects into Workspace``>` `Select root directory` `>` `/home/USERNAME/git/rdo-xtext` `>` `Finish`
* Wait for the workspace to build and get tons of errors
* `ru.bmstu.rk9.rao/src/ru.bmstu.rk9.rao/Rao.xtext` `>` `Run As` `>` `Generate Xtext Artifacts` `>` `Proceed`
* `*ATTENTION* It is recommended to use the ANTLR 3...` press `y`
```
0    [main] INFO  lipse.emf.mwe.utils.StandaloneSetup  - Registering platform uri '/home/drobus/git/rdo-xtext'
637  [main] INFO  lipse.emf.mwe.utils.StandaloneSetup  - Adding generated EPackage 'org.eclipse.xtext.common.types.TypesPackage'
644  [main] INFO  ipse.emf.mwe.utils.DirectoryCleaner  - Cleaning /home/drobus/git/rdo-xtext/ru.bmstu.rk9.rao/../ru.bmstu.rk9.rao/src-gen
652  [main] INFO  ipse.emf.mwe.utils.DirectoryCleaner  - Cleaning /home/drobus/git/rdo-xtext/ru.bmstu.rk9.rao/../ru.bmstu.rk9.rao/model
652  [main] INFO  ipse.emf.mwe.utils.DirectoryCleaner  - Cleaning /home/drobus/git/rdo-xtext/ru.bmstu.rk9.rao/../ru.bmstu.rk9.rao.ui/src-gen
653  [main] INFO  ipse.emf.mwe.utils.DirectoryCleaner  - Cleaning /home/drobus/git/rdo-xtext/ru.bmstu.rk9.rao/../ru.bmstu.rk9.rao.tests/src-gen
5720 [main] INFO  clipse.emf.mwe.utils.GenModelHelper  - Registered GenModel 'http://www.bmstu.ru/rk9/rao/Rao' from 'platform:/resource/ru.bmstu.rk9.rao/model/generated/Rao.genmodel'
27489 [main] INFO  text.generator.junit.Junit4Fragment  - generating Junit4 Test support classes
27496 [main] INFO  text.generator.junit.Junit4Fragment  - generating Compare Framework infrastructure
27550 [main] INFO  .emf.mwe2.runtime.workflow.Workflow  - Done.
```
>**[!]** *If your output differs from the one above by a lot of errors mentioning* `RULE_ANY_OTHER`*, you should run the generation process again and again until the bulid is succesfull. This is Xtext/Antlr bug caused by complex rules supporting unicode identifiers in grammar, sorry for the inconvenience*

* `Run` `>` `Run Configurations...` `>` `Eclipse Application` `>` `New` `>` `Name` `=`
 * `runtime-EclipseXtext`<br>`>` `Location` `=`
 * `${workspace_loc}/../runtime-EclipseXtext`<br>`>` `Run`
 * *Ignore this if you use Java version 8 or later.* Eclipse Platform may freeze during its launch. This happens due to the unsufficient [permgen](http://wiki.eclipse.org/FAQ_How_do_I_increase_the_permgen_size_available_to_Eclipse%3F) size available to Eclipse. To prevent that, add `-XX:MaxPermSize=256M` to VM arguments in Run Configuration.
* And that's it.

## Running
* `Window` `>` `Open Perspective` `>` `Other...` `>` `Rao`
* `File` `>` `New` `>` `Project...` `>` `Java Project` `>` `Next>` `>` `Project name:` `>`
 * set project name<br>`>` `Project layout` `>` `Configure default` `>` `Source folder name` `=`
 * `src-gen`<br>`>` `OK` `>` `Preference Recorder` `>` `OK` `>` `Finish`
 * `Do you want to open this perspective now?` `>` `No`
* Right-click on created project `>` `New` `>` `File` `>` `File name:` `>`
 * filename.**rao** (must be valid java identificator)<br>`>` `Finish`
 * `Do you want to add the Xtext nature to the project?` `>` `Yes`
* [Models examples](https://github.com/aurusov/rdo-xtext-models)
