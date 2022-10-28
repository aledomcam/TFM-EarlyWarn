# TFM-EarlyWarn
Este repositorio contiene el proyecto principal de código de mi Trabajo de Fin de Máster, que se puede consultar [en el Archivo Digital de la UPM](https://oa.upm.es/71403/).

El código es una versión modificada del [proyecto original](https://github.com/Luis-gd/Plugins_TFM) en el que colaboraron múltiples personas. El único cambio con respecto a dicha versión ha sido la eliminación de todo código que no fue escrito por mí, para así poder mantener mis contribuciones con fines expositivos.

El proyecto es un plugin para Neo4J desarrollado en Java y se organiza de la siguiente forma:

- definiciones: Clases especiales uasadas en diferentes partes del código. Normalmente excepciones, interfaces, enums y clases de datos.
- etl: Clases que se encarguen de cargar los nuevos datos o limpiar los existentes.
- funciones: Contiene funciones pueden ser llamadas desde Neo4J.
- main: Clases generales que se vayan a usar en el código de diferentes personas.
- mh/vnsrs: Clases que implementan la metaheurística desarrollada por mí como parte del TFM.

## Véase también
- [Herramienta para obtención de pesos de un decisor](https://github.com/aledomcam/TFM-HerramientaPesos)
- [Scripts para procesado de datos usados en el trabajo](https://github.com/aledomcam/TFM-Scripts)