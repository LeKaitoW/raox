# RDO-xtext
## Summary
<img src=https://raw.githubusercontent.com/k-alexandrovsky/k-alexandrovsky.github.io/master/rdo-xtext.png><img>
This project is an implementation of RDO modelling language in Eclipse, using xtext.
* [About RDO modelling language (rus)](http://rdo.rk9.bmstu.ru/help/help/rdo_lang_rus/html/rdo_intro.htm)

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
 * Download [JFreeChart](http://www.jfree.org/jfreechart/download.html)
 * Git clone `rdo-xtext` repository
```bash
ssh-add ~/.ssh/github.openssh.private.key
git clone git@github.com:aurusov/rdo-xtext.git
```
## Installing  
### Setting up the workspace for Eclipse
* `File` `>` `Import` `>` `General` `>` `Existing Projects into Workspace``>` `Select root directory` `>` `/home/USERNAME/git/rdo-xtext` `>` `Finish`
* Wait for the workspace to build and get tons of errors
* `ru.bmstu.rk9.rdo/src/ru.bmstu.rk9.rdo/RDO.xtext` `>` `Run As` `>` `Generate Xtext Artifacts` `>` `Proceed`
* `*ATTENTION* It is recommended to use the ANTLR 3...` press `y`
``` 
0    [main] INFO  lipse.emf.mwe.utils.StandaloneSetup  - Registering platform uri '/home/drobus/git/rdo-xtext'
637  [main] INFO  lipse.emf.mwe.utils.StandaloneSetup  - Adding generated EPackage 'org.eclipse.xtext.common.types.TypesPackage'
644  [main] INFO  ipse.emf.mwe.utils.DirectoryCleaner  - Cleaning /home/drobus/git/rdo-xtext/ru.bmstu.rk9.rdo/../ru.bmstu.rk9.rdo/src-gen
652  [main] INFO  ipse.emf.mwe.utils.DirectoryCleaner  - Cleaning /home/drobus/git/rdo-xtext/ru.bmstu.rk9.rdo/../ru.bmstu.rk9.rdo/model
652  [main] INFO  ipse.emf.mwe.utils.DirectoryCleaner  - Cleaning /home/drobus/git/rdo-xtext/ru.bmstu.rk9.rdo/../ru.bmstu.rk9.rdo.ui/src-gen
653  [main] INFO  ipse.emf.mwe.utils.DirectoryCleaner  - Cleaning /home/drobus/git/rdo-xtext/ru.bmstu.rk9.rdo/../ru.bmstu.rk9.rdo.tests/src-gen
5720 [main] INFO  clipse.emf.mwe.utils.GenModelHelper  - Registered GenModel 'http://www.bmstu.ru/rk9/rdo/RDO' from 'platform:/resource/ru.bmstu.rk9.rdo/model/generated/RDO.genmodel'
27489 [main] INFO  text.generator.junit.Junit4Fragment  - generating Junit4 Test support classes
27496 [main] INFO  text.generator.junit.Junit4Fragment  - generating Compare Framework infrastructure
27550 [main] INFO  .emf.mwe2.runtime.workflow.Workflow  - Done.
```
>**[!]** *If your output differs from the one above by a lot of errors mentioning* `RULE_ANY_OTHER`*, you should run the generation process again and again until the bulid is succesfull. This is Xtext/Antlr bug caused by complex rules supporting unicode identifiers in grammar, sorry for the inconvenience*

* `ru.bmstu.rk9.rdo.ui` `>` `New` `>` `Folder` `>` `Folder name:` `=` `thirdparty` `>` `Finish`
* Drag and drop files from `jfreechart-1.0.19\lib`
 * `jcommon-1.0.23.jar`
 * `jfreechart-1.0.19.jar`
 * `jfreechart-1.0.19-experimental.jar`
 * `jfreechart-1.0.19-swt.jar`
* `Select how files should be imported into the project:` `>` `Copy files` `>` `Ok`
* `Run` `>` `Run Configurations...` `>` `Eclipse Application` `>` `New` `>` `Name` `=` `runtime-EclipseXtext` `>` `Location` `=` `${workspace_loc}/../runtime-EclipseXtext` `>` `Run`
 * *Ignore this if you use Java version 8 or later.* Eclipse Platform may freeze during its launch. This happens due to the unsufficient [permgen](http://wiki.eclipse.org/FAQ_How_do_I_increase_the_permgen_size_available_to_Eclipse%3F) size available to Eclipse. To prevent that, add `-XX:MaxPermSize=256M` to VM arguments in Run Configuration.
* And that's it.

## Running  
* `Window` `>` `Open Perspective` `>` `Other...` `>` `RDO`
* `File` `>` `New` `>` `Project...` `>` `Java Project` `>` `Next>` `>` `Project name:` `>` set project name `>` `Project layout` `>` `Configure default` `>` `Source folder name` `=` `src-gen` `>` `OK` `>` `Finish`
 * `Do you want to open this perspective now?` `>` `No`
* Right-click on created project `>` `New` `>` `File` `>` `File name:` `>` filename.**rdo** (must be valid java identificator) `>` `Finish`
 * `Do you want to add the Xtext nature to the project?` `>` `Yes`
* Model example
```
$Resource_type Парикмахерские: permanent
$Parameters
    состояние_парикмахера : ( Свободен, Занят )
    количество_в_очереди  : integer
    количество_обслуженных: integer
$End

$Resources
    Парикмахерская = Парикмахерские(Свободен, 0, 0);
$End

$Pattern Образец_прихода_клиента : event
$Relevant_resources
    _Парикмахерская: Парикмахерская Keep
$Body
_Парикмахерская:
    Convert_event
        Образец_прихода_клиента.planning( time_now + Интервал_прихода( 30 ) );
        количество_в_очереди++;
$End

$Pattern Образец_обслуживания_клиента : operation
$Relevant_resources
    _Парикмахерская: Парикмахерская Keep Keep
$Time = Длительность_обслуживания( 20, 40 )
$Body
_Парикмахерская:
    Choice from _Парикмахерская.состояние_парикмахера == Свободен and _Парикмахерская.количество_в_очереди > 0
    Convert_begin
        количество_в_очереди--;
        состояние_парикмахера = Занят;
    Convert_end
        состояние_парикмахера  = Свободен;
        количество_обслуженных++;
$End

$Decision_point model: some
$Condition NoCheck
$Activities
    Обслуживание_клиента: Образец_обслуживания_клиента;
$End

$Sequence Интервал_прихода : real 
$Type = exponential 123456789 legacy
$End

$Sequence Длительность_обслуживания : real 
$Type = uniform 123456789 legacy
$End

$Simulation_run
    Образец_прихода_клиента.planning( time_now + Интервал_прихода( 30 ) );
    Terminate_if Time_now >= 12 * 7 * 70;
$End

$Results
    Занятость_парикмахера : watch_state Парикмахерская.состояние_парикмахера == Занят
    Длина_очереди         : watch_par   Парикмахерская.количество_в_очереди
    Всего_обслужено       : get_value   Парикмахерская.количество_обслуженных
    Пропускная_способность: get_value   Парикмахерская.количество_обслуженных / Time_now * 60
    Длительность_работы   : get_value   Time_now / 60
$End
```
