# RDO-xtext
## Summary
This project is an implementation of RDO modelling language in Eclipse, using xtext.
* [About RDO modelling language (rus)](http://rdo.rk9.bmstu.ru/help/help/rdo_lang_rus/html/rdo_intro.htm)

## Installing  
### Setting up the workspace for Eclipse
* File > Import existing projects into workspace (with repo as root directory)
* Wait for the workspace to build and get tons of errors
* ru.bmstu.rk9.rdo/src/ru.bmstu.rk9.rdo/RDO.xtext > Run As > Generate Xtext Artifacts
* Run > Run Configurations... > Eclipse Application > New  
    e.g. `runtime-EclipseXtext` with location `${workspace_loc}/../runtime-EclipseXtext`
* And that's it.