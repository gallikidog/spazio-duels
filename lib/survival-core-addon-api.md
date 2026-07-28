# Survival Core Addon API

Version inicial: `0.9.14-SNAPSHOT`.

## Dependencia Maven

```xml
<dependency>
  <groupId>network.minespazio</groupId>
  <artifactId>survival-core-addon-api</artifactId>
  <version>0.9.14-SNAPSHOT</version>
  <scope>provided</scope>
</dependency>
```

El addon tambien debe compilar contra Paper `1.21.10` y declarar:

```yaml
depend:
  - survival_core
```

## Obtener la API

```java
RegisteredServiceProvider<SurvivalCoreApi> registration =
        Bukkit.getServicesManager().getRegistration(SurvivalCoreApi.class);

if (registration == null) {
    getLogger().warning("survival_core API no esta disponible.");
    return;
}

SurvivalCoreApi api = registration.getProvider();
```

## Registro de addon

```java
api.addons().register(SurvivalCoreAddonDescriptor.of(
        "combat-addon",
        "Combat Addon",
        getPluginMeta().getVersion(),
        "combat.hooks",
        "spaziogears.items"
));
```

En `onDisable`:

```java
api.addons().unregister("combat-addon");
```

## Teleport

```java
api.teleport().request(player.getUniqueId(), "survival", RequestSource.API)
        .thenAccept(result -> getLogger().info("Teleport: " + result.status()));
```

## SpazioGears

```java
if (api.gears().available()) {
    List<String> weapons = api.gears().weaponIds();
    Optional<ItemStack> sword = api.gears().createWeapon("exodia_sword", 10);
}
```

La fachada de gears esta pensada para lectura y creacion segura de items. No expone
el runtime interno para evitar que cambios de optimizacion rompan addons.

## Modulos

```java
boolean gearsRunning = api.modules().running("spaziogears");
Map<String, SurvivalModuleStatus> statuses = api.modules().statuses();
```

IDs actuales:

- `core`
- `teleport`
- `portals`
- `menus`
- `storage`
- `jump-pads`
- `double-jump`
- `spaziogears`
