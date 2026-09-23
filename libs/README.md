# Dependencias privadas

Para compilar, coloca los JAR privados con estos nombres:

```text
libs/
  survival_core.jar
  PlaceholderAPI.jar
  PlayerKits2.jar
```

Tambien puedes apuntar Maven a otras rutas sin editar el POM:

```powershell
mvn clean package `
  -Dsurvival.core.jar=C:\ruta\survival_core.jar `
  -Dplaceholderapi.jar=C:\ruta\PlaceholderAPI.jar `
  -Dplayerkits2.jar=C:\ruta\PlayerKits2.jar
```

Las dependencias Paper, WorldEdit y BungeeChat se resuelven desde repositorios
Maven. Las tres dependencias anteriores eran `system` en el POM original porque
no se distribuyen desde los repositorios declarados.

