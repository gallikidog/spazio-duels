# SpazioDuels v1.2.9 - Duel & KOTH Engine Core

**SpazioDuels** es el plugin avanzado de duelos PvP, eventos automáticos, combate por equipos y rey de la colina (**KOTH**) desarrollado para la infraestructura de **survival_core** en **Paper 1.21+**.

---

## 🌟 Características Principales (v1.2.9)

- ⚔️ **Duelos 1v1 y Party vs Party**: Sistema de desafíos PvP en arenas aisladas con selecciones de kits, tiempo de cuenta regresiva, prevención de deserciones y visualización de salud final del ganador.
- 👑 **King Of The Hill (KOTH)**: Sistema completo de KOTH con selección de áreas y zonas de captura mediante WorldEdit, tiempos personalizables, mensajes de emisión globales, GUI de recompensas de ítems (`KothLootGUI`) y comandos de recompensa ejecutados por consola con probabilidades porcentuales.
- 🏆 **Eventos y Torneos Automáticos (`/duelevent`)**: Organiza torneos masivos por llaves o eliminatorias de duelos en el servidor con resúmenes interactivos en GUI (`EventSummaryGUI`).
- 👥 **Sistema de Parties (`/party`)**: Permite a los jugadores formar equipos para combatir en duelos grupales.
- 🛡️ **AntiDupe y Guardado de Inventarios**: Preserva el inventario real del jugador antes del duelo, previene la clonación de ítems etiquetando objetos de kits con `PersistentDataContainer` y garantiza el rollback automático de bloques en arenas desarmables (`ArenaRollback`).
- ⚡ **PvP 1.8 Mechanics**: Opción de deshabilitar el cooldown de ataques para simular las mecánicas de combate PvP de Minecraft 1.8.
- 📊 **Scoreboard Dinámica e Integración PlaceholderAPI**: Scoreboards animadas para duelos y KOTHs, más expansión nativa de PlaceholderAPI (`%spazioduels_*%`).
- 🎒 **Integración con PlayerKits2**: Importación automática e integración con kits de `PlayerKits2`.

---

## 📜 Lista Completa de Comandos y Alias

| Comando | Alias | Descripción | Permiso |
| :--- | :--- | :--- | :--- |
| `/duel <jugador>` | `/duelo`, `/duels` | Desafía a un jugador a un duelo o gestiona respuestas (`accept`, `deny`, `forfeit`). | Usuario |
| `/party <subcomando>` | `/p`, `/equipo`, `/team` | Sistema de parties (`create`, `invite`, `accept`, `leave`, `disband`, `kick`, `info`). | Usuario |
| `/duelevent <subcomando>` | `/eventoduel`, `/devent` | Administra y participa en torneos automáticos de duelos (`start`, `cancel`, `join`, `autowin`, `summary`). | `spazioduels.admin.event` |
| `/koth <subcomando>` | `/kingofthehill` | Administración total de KOTHs (`create`, `delete`, `setzone`, `setcapzone`, `setcapdelay`, `setloot`, `addcommandreward`, `removecommandreward`, `listcommandrewards`, `start`, `stop`, `list`, `info`, `reload`). | `spazioduels.admin.koth` |
| `/spazioduels <subcomando>` | `/sd`, `/sduels` | Administración general del plugin, setup de arenas, gestión de kits y recarga (`setup`, `adminkits`, `kit`, `reload`). | `spazioduels.admin` |

---

## 🔑 Permisos del Plugin

| Permiso | Descripción | Por Defecto |
| :--- | :--- | :--- |
| `spazioduels.admin` | Acceso completo a la configuración, arenas, kits y administración (`/spazioduels`). | `OP` |
| `spazioduels.admin.event` | Permite iniciar, cancelar y administrar eventos de duelos (`/duelevent`). | `OP` |
| `spazioduels.admin.koth` | Permite crear, editar zonas WorldEdit, loot y controlar KOTHs (`/koth`). | `OP` |
| `spazioduels.admin.bypass` | Permite ejecutar comandos de consola prohibidos durante un duelo activo. | `OP` |

---

## 🖥️ Menús Interactivos (GUIs)

1. **KitSelectorGUI**: Menú visual para seleccionar el Kit de combate al desafiar a un jugador.
2. **AdminKitsGUI (`/sd adminkits`)**: Interfaz para crear, editar, guardar y borrar kits de combate desde el juego.
3. **ArenaAdminGUI (`/sd setup`)**: Gestión visual de arenas, puntos de aparición (Pos1, Pos2) y lobbies.
4. **KothLootGUI (`/koth setloot <nombre>`)**: GUI de cofre para depositar los ítems de recompensa que recibirá el ganador del KOTH.
5. **EventSummaryGUI (`/duelevent summary`)**: Resumen del torneo en curso con clasificaciones y llaves.

---

## 🧩 Variables de PlaceholderAPI (`%spazioduels_*%`)

- `%spazioduels_in_duel%`: Muestra si el jugador está en combate (`true`/`false`).
- `%spazioduels_opponent%`: Nombre del oponente actual en el duelo.
- `%spazioduels_arena%`: Nombre de la arena activa.
- `%spazioduels_kit%`: Nombre del Kit equipado en el duelo.
- `%spazioduels_koth_active%`: Nombre del KOTH en ejecución o `Ninguno`.
- `%spazioduels_koth_capping%`: Nombre del jugador que domina la zona del KOTH.
- `%spazioduels_koth_time%`: Tiempo restante en formato `mm:ss` para capturar el KOTH.
- `%spazioduels_event_players%`: Cantidad de jugadores registrados en el torneo.

---

## 📂 Archivos de Configuración (`src/main/resources`)

- `config.yml`: Ajustes generales del sistema, prevención AntiDupe, tiempos de cuenta regresiva y mecánica PvP 1.8.
- `arenas.yml`: Registro de coordenadas de spawns, lobbies y esquemas de arenas.
- `kits.yml`: Configuración de kits con ítems, armaduras y efectos de pociones.
- `koths.yml`: Configuración de áreas cuboides (WorldEdit), tiempos de captura y recompensas de comandos de los KOTHs.
- `rewards.yml`: Tabla de botines e ítems recompensa.
- `scoreboard.yml`: Plantillas de Scoreboard dinámicas para duelos, lobbies y eventos KOTH.

---

## 🛠️ Compilación e Instalación

1. Requisitos: JDK 21 y Maven 3.9+.
2. Colocar las librerías en la carpeta `libs/`:
   - `survival_core.jar`
   - `PlaceholderAPI.jar`
   - `PlayerKits2.jar`
3. Ejecutar comando de compilación:
   ```bash
   mvn clean package
   ```
4. El archivo final generado se encontrará en: `target/spazioduels-1.2.9.jar`.
