# Arquitectura de SpazioDuels

## Arranque y ciclo de vida

`SpazioDuelsPlugin` es el punto de entrada. En `onEnable()` crea los managers,
registra listeners, conecta el addon de Survival Core y enlaza los comandos.
En `onDisable()` restaura duelos activos, detiene el KOTH, limpia scoreboards y
elimina el addon.

Orden de inicializacion:

1. Kits, arenas y proteccion de inventario.
2. Parties, duelos, eventos de duelos y KOTH.
3. Scoreboard, PvP 1.8 e integracion con Survival Core.
4. Listeners, comandos y PlaceholderAPI cuando esta instalado.

## Modulos principales

| Paquete | Responsabilidad | Clases de entrada |
| --- | --- | --- |
| `arena` | Estado, ubicaciones, disponibilidad y rollback de arenas. | `ArenaManager`, `Arena` |
| `duel` | Solicitudes, equipos, partidas y reglas de combate. | `DuelManager`, `DuelMatch` |
| `kit` | Carga, administracion y entrega de kits locales o PlayerKits2. | `KitManager`, `Kit` |
| `event` | Eventos automaticos, lobby, resumen y entrega diferida de premios. | `DuelEventManager`, `DuelEvent` |
| `koth` | Zonas, captura, horarios y premios de KOTH. | `KothManager`, `KothMatch` |
| `command` | Adaptadores Bukkit para `duel`, `duelevent`, `koth`, `party` y `spazioduels`. | Clases `*Command` |
| `listener` | Proteccion, inventarios GUI, daño, muertes y sopas. | `MatchListener`, `ProtectionListener` |
| `antidupe` | Marcado de items temporales y respaldo/restauracion de inventario. | `AntiDupeManager`, `InventoryBackupManager` |
| `party` | Equipos persistentes en memoria para modos grupales. | `PartyManager`, `Party` |
| `pvp` | Ajustes de cooldown, invulnerabilidad y knockback tipo 1.8. | `PvP18Manager`, `PvP18Listener` |
| `gui` | Menus de seleccion, administracion de arenas/kits y botin KOTH. | `KitSelectorGUI`, `KothLootGUI` |
| `placeholder` | Expansion de PlaceholderAPI para estado de duelos, KOTH y eventos. | `SpazioDuelsPlaceholderExpansion` |
| `scoreboard` | Integracion con el servicio de scoreboards de Survival Core. | `ScoreboardManager` |
| `integration`, `addon` | PlayerKits2 y API de Survival Core. | `PlayerKitsHook`, `SurvivalCoreAddonHook` |

## Flujos importantes

### Duelo directo

`DuelCommand` -> `DuelManager` -> `KitSelectorGUI` -> `DuelMatch`.

La partida guarda inventarios mediante `InventoryBackupManager`, entrega el kit,
marca sus items con `AntiDupeManager`, teletransporta a la arena y registra el
match. Al finalizar, `DuelMatch.cleanupAndRestore()` purga los items temporales,
restaura inventario/ubicacion y solicita la entrega pendiente de premios.

### Evento de duelos

`DuelEventCommand` y la programacion de `DuelEventManager` crean un
`DuelEvent`. El evento escoge modo, kit y recompensas, controla el lobby y crea
partidas `DuelMatch`. Las recompensas se difieren hasta que el ganador deja la
arena, para evitar que se pierdan durante la restauracion del inventario.

### KOTH

`KothCommand` administra definicion, zona, captura, loot y recompensas por
comando. `KothManager` carga `koths.yml`, inicia el evento y usa `KothMatch` para
evaluar jugadores dentro de la region, progreso, vencimiento y finalizacion.
`KothSchedule` describe horarios y `KothCommandReward` representa un premio por
comando con probabilidad.

## Persistencia

Los managers de arena, kits y KOTH trabajan contra archivos YAML dentro de la
carpeta de datos del plugin. Los objetos de partidas y parties viven en memoria.
Por ello, el apagado ordenado es crucial: el plugin restaura las partidas antes
de terminarse.

## Dependencias

Obligatoria: `survival_core`.

Opcionales declaradas en `plugin.yml`: PlayerKits2, WorldEdit o FAWE y
PlaceholderAPI. El codigo usa las APIs de PlayerKits2 y PlaceholderAPI cuando
estan disponibles; WorldEdit/FAWE se usan para seleccionar regiones KOTH.

