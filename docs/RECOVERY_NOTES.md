# Notas de recuperacion

## Origen

- Archivo analizado: `SpazioDuels-1.2.9.jar`
- Version declarada: `1.2.9`
- Clase principal: `network.minespazio.spazioduels.SpazioDuelsPlugin`
- API objetivo: Paper `1.21.10-R0.1-SNAPSHOT`
- Java de compilacion original: 21
- Decompilador: CFR 0.152
- Clases recuperadas: 52

## Que se preservo

- Las clases Java recuperadas conservan sus paquetes originales.
- Todos los recursos no compilados del JAR estan en `src/main/resources`.
- El POM original del artefacto se conserva en `docs/reference/original-pom.xml`.
- `pom.xml` es una version normalizada para esta estructura Maven estandar.

## Limitaciones

La decompilacion reconstruye Java a partir de bytecode. Aunque CFR produjo
fuentes legibles, pueden existir diferencias cosmeticas frente al fuente
original, por ejemplo nombres de variables locales, bloques sinteticos o
formato. No se debe asumir que este arbol recompilara sin las dependencias
privadas exactas ni que su salida binaria sera identica byte a byte al JAR base.

## Verificacion recomendada

1. Resolver las dependencias descritas en `libs/README.md`.
2. Compilar usando JDK 21.
3. Iniciar un servidor de prueba compatible con Paper/Pufferfish 1.21.10.
4. Verificar carga, comandos, kits, restauracion de inventario, KOTH,
   PlaceholderAPI y Survival Core.
5. Comparar el comportamiento con `SpazioDuels-1.2.9.jar` antes de desplegar.

