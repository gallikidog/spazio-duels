# Comandos y configuracion

## Comandos registrados

| Comando | Clase | Funcion |
| --- | --- | --- |
| `/duel` | `command/DuelCommand` | Solicitud, aceptacion, rechazo, rendicion y recarga de duelos. |
| `/duelevent` | `command/DuelEventCommand` | Inicio, cancelacion, union, resumen y control de eventos de duelos. |
| `/koth` | `command/KothCommand` | Creacion, zonas, movimiento, inicio, parada, loot y recompensas KOTH. |
| `/party` | `command/PartyCommand` | Parties para modos de equipo. |
| `/spazioduels` | `command/SpazioDuelsAdminCommand` | Administracion, setup, kits y recarga general. |

Los aliases, permisos y sintaxis visibles para Bukkit se encuentran en
`src/main/resources/plugin.yml`.

## Archivos de datos

| Archivo | Consumidor principal | Contenido |
| --- | --- | --- |
| `config.yml` | `SpazioDuelsPlugin`, `DuelEventManager`, `KothManager`, `PvP18Manager` | Ajustes generales, PvP 1.8, eventos, KOTH y pool de recompensas. |
| `arenas.yml` | `ArenaManager` | Spawns, estado y regiones de arenas. |
| `kits.yml` | `KitManager` | Kits locales y referencias a PlayerKits2. |
| `koths.yml` | `KothManager` | KOTHs, regiones, horarios, loot y premios por comando. |
| `rewards.yml` | `RewardUtil` | Recompensas disponibles para eventos. |
| `scoreboard.yml` | `ScoreboardManager` | Lineas y visibilidad de scoreboards. |

## Puntos de cambio habituales

- Reglas de un duelo y restauracion: `duel/DuelMatch.java`.
- Seleccion de arena y solicitudes: `duel/DuelManager.java`.
- Logica de eventos automaticos: `event/DuelEventManager.java`.
- Flujo de lobby, ganador y premios diferidos: `event/DuelEvent.java`.
- Captura, expiracion y barra KOTH: `koth/KothMatch.java`.
- Horarios y carga de KOTH: `koth/KothManager.java` y `koth/KothSchedule.java`.
- Placeholders: `placeholder/SpazioDuelsPlaceholderExpansion.java`.
- Inventarios GUI: `gui/` y eventos de clic en `listener/GUIListener.java`.
- Prevencion de duplicados: `antidupe/` y `listener/AntiDupeListener.java`.

