# Game 5 plugin
## Preparing
 * Download [Eclipse IDE for Java and DSL Developers](http://www.eclipse.org/downloads/packages/eclipse-ide-java-and-dsl-developers/lunasr2)
```bash
sudo apt-get install openjdk-8-jdk # for debian-based distributions
cd ~/Downloads
gunzip -c eclipse-dsl-luna-SR2-linux-gtk-x86_64.tar.gz  | tar xvf -
cd eclipse
./eclipse
```
 * Download `rdo-xtext` [libraries] (https://www.dropbox.com/sh/g41180l0ffkq88z/AACqDeOiqBz7tNK_xRCBeTbba?dl=0)
 * Put `ru.bmstu.rk9.rao*.jar` into `.../eclipse/dropins`
 * Git clone `rdo-game5` repository
```bash
ssh-add ~/.ssh/github.openssh.private.key
git clone git@github.com:lekaitow/rdo-game5.git
```
## Installing  
### Setting up the workspace for Eclipse
* `File` `>` `Import` `>` `General` `>` `Existing Projects into Workspace``>` `Select root directory` `>` `/home/USERNAME/git/rdo-game5` `>` `Finish`
* `Run` `>` `Run As` `>` `Eclipse Application`

>**[!]** Fix [bug](https://bugs.eclipse.org/bugs/show_bug.cgi?id=463733) in Eclipse Mars under Linux:
`Run` > `Run Configurations` > `Eclipse Application` > Tab `Environment` > `New...` >
`Name:` = ` SWT_GTK3 ` > `Value:` = `0` > `OK` > `Apply` > `Run`

## Running  
* `Window` `>` `Open Perspective` `>` `Other...` `>` `Rao`
* `Plugins` > `Game 5` > `Model name:` > `Ok`
