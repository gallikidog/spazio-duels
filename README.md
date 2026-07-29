# ⚔️ SpazioDuels (v1.1.1)

**SpazioDuels** es un plugin modular, robusto y optimizado para entornos de producción de Minecraft (**Paper 1.21.10** / **Java 21**). Diseñado como un addon nativo para el core `survival_core`, ofrece un sistema completo de **Duelos**, **Eventos Automáticos de Duelos**, **Hosteo Automático de KOTHs**, **Integración con PlayerKits2**, **Protección Anti-Dupe** y **WorldEdit / FAWE**.

---

## 🌟 Características Principales

### ⚔️ Sistema de Duelos (Solo, Equipos y Handicap)
- **Desafío Rápido e Interactivo**: `/duel <jugador>` abre una interfaz GUI (`KitSelectorGUI`) filtrando automáticamente los kits habilitados.
- **Invitaciones en Chat**: Los jugadores reciben mensajes interactivos con botones `[ACEPTAR DUELO]` y `[RECHAZAR DUELO]`.
- **Múltiples Modos de Juego**:
  - **1v1**: Duelos individuales clásicos.
  - **2v2, 3v3, 4v4**: Duelos por equipos/party.
  - **Handicap (2v1, 3v2, 4v3)**: Modalidades con desventajas automáticas configurables para el equipo mayoritario (ej. sin casco o vida reducida).
- **Límite de Tiempo**: Duración máxima de 10 minutos (configurable) con temporizador en la Actionbar.

### 🏆 Eventos Automáticos de Duelos y Torneos Programados
- **Torneos Automáticos**: Anuncios interactivos en el chat con botón `- CLICK PARA INGRESAR AL EVENTO -` y desarrollo automático de rondas de torneo (brackets).
- **Resumen Post-Evento**: Botón `- CLICK PARA VER LA INFORMACION -` que abre la GUI `EventSummaryGUI` mostrando el ganador, duración, muertes y recompensas.
- **Programación Automática (`duel_event_autostart`)**: Configurable en `config.yml` para hostear eventos y torneos de duelos automáticamente durante el día en todos los modos (`1v1`, `2v2`, `3v3`, `4v4`, `handicap`) y con kits aleatorios.

### 🏰 Sistema KOTH (King Of The Hill)
- **Creación & Configuración**: Definición de tiempo de captura (`/koth setcapdelay`), botín interactivo con botón **GUARDAR LOOT** (`/koth setloot`).
- **Integración con WorldEdit / FAWE**: Configuración de zona perimetral (`/koth setzone`) y zona de captura (`/koth setcapzone`).
- **Notificaciones & Títulos**: Título en pantalla durante 2 segundos al entrar al área y al capturar el KOTH.
- **Scoreboard en Tiempo Real**: Placeholders en vivo para mostrar el KOTH activo, capper y tiempo restante.
- **Hosteo Automático de KOTHs (`koth_autostart`)**: Programación automática de KOTHs aleatorios durante el día.

### 📦 Integración con PlayerKits (PlayerKits2)
- **Importación Directa**: Detecta automáticamente el plugin **PlayerKits2** e importa todos los kits configurados en el servidor.
- **Gestión de Kits (`/sd adminkits`)**: Panel GUI interactivo para marcar qué kits están **HABILITADOS** o **DESHABILITADOS** para duelos.

### 🔒 Sistema Anti-Dupe de Grado de Producción
- **Etiquetado PDC**: Todos los ítems de kit están marcados con `spazioduels:kit_item`.
- **Protección de Contenedores y Dropeos**: Cancela la tirada de ítems y bloquea guardar ítems de duelos en cofres, shulkers, ender chests y marcos.
- **Restauración Asegurada (Disk-Backed)**: Respaldo automático del inventario original en `plugins/SpazioDuels/data/inventories/<uuid>.yml` con restauración garantizada al finalizar la partida, desconectarse o ante un reinicio del servidor.

### 🛡️ Setup In-Game & Protección de Arenas
- **Configuración In-Game**: Definición de `spawn1`, `spawn2`, `spectator` y `lobby` global.
- **Protección de Comandos & Rollback**: Bloqueo de comandos no autorizados durante duelos y rollback automático de bloques colocados en la arena (ej. cobwebs/agua/lava en kits UHC).

---

## 📜 Todos los Comandos

### 🏰 Comandos de KOTH (`/koth`)
| Comando | Descripción | Permiso |
| :--- | :--- | :--- |
| `/koth create <nombre>` | Crea un nuevo KOTH. | `spazioduels.admin.koth` |
| `/koth setcapdelay <koth> <segundos>` | Establece el tiempo de captura (en segundos). | `spazioduels.admin.koth` |
| `/koth setloot <koth>` | Abre la GUI para depositar y guardar el botín del KOTH. | `spazioduels.admin.koth` |
| `/koth setzone <koth>` | Establece la zona perimetral del KOTH con la selección de WorldEdit. | `spazioduels.admin.koth` |
| `/koth setcapzone <koth>` | Establece la zona de captura del KOTH con la selección de WorldEdit. | `spazioduels.admin.koth` |
| `/koth start <koth>` | Inicia manualmente un KOTH. | `spazioduels.admin.koth` |
| `/koth stop` | Detiene el KOTH activo actual. | `spazioduels.admin.koth` |
| `/koth list` | Muestra la lista de KOTHs registrados. | `spazioduels.admin.koth` |
| `/koth info <koth>` | Muestra información detallada de un KOTH. | `spazioduels.admin.koth` |

### ⚔️ Comandos de Duelos (`/duel`)
| Comando | Alias | Descripción | Permiso |
| :--- | :--- | :--- | :--- |
| `/duel <jugador>` | `/duelo`, `/duels` | Envía una solicitud de duelo abriendo el menú GUI de kits habilitados. | `Ninguno` |
| `/duel accept <jugador>` | `/duel aceptar` | Acepta una solicitud de duelo pendiente. | `Ninguno` |
| `/duel deny <jugador>` | `/duel rechazar` | Rechaza una solicitud de duelo pendiente. | `Ninguno` |
| `/duel forfeit` | `/duel rendirse` | Se rinde en el duelo actual. | `Ninguno` |

### 👥 Comandos de Party / Equipo (`/party`)
| Comando | Alias | Descripción | Permiso |
| :--- | :--- | :--- | :--- |
| `/party create` | `/p`, `/equipo`, `/team` | Crea una party para duelos de equipo o handicap. | `Ninguno` |
| `/party invite <jugador>` | - | Invita a un jugador a tu party. | `Ninguno` |
| `/party accept <líder>` | - | Acepta la invitación de party de un líder. | `Ninguno` |
| `/party leave` | - | Abandona la party actual. | `Ninguno` |
| `/party disband` | - | Disuelve la party (solo el líder). | `Ninguno` |
| `/party info` | - | Muestra los miembros de la party. | `Ninguno` |

### 🏆 Comandos de Eventos Automáticos (`/duelevent`)
| Comando | Alias | Descripción | Permiso |
| :--- | :--- | :--- | :--- |
| `/duelevent start <modo> <kit>` | `/eventoduel`, `/devent` | Inicia un evento automático de duelos. | `spazioduels.admin.event` |
| `/duelevent join` | - | Se une al evento de duelos activo. | `Ninguno` |
| `/duelevent summary [uuid]` | - | Abre la GUI con el resumen del evento. | `Ninguno` |

### ⚙️ Comandos de Administración (`/spazioduels`)
| Comando | Alias | Descripción | Permiso |
| :--- | :--- | :--- | :--- |
| `/spazioduels adminkits` | `/sd adminkits` | Abre la GUI para habilitar/deshabilitar kits para duelos. | `spazioduels.admin` |
| `/spazioduels setup` | `/sd setup` | Abre la GUI de gestión de arenas. | `spazioduels.admin` |
| `/spazioduels setup create <nombre>` | - | Crea una nueva arena. | `spazioduels.admin` |
| `/spazioduels setup setspawn1 <arena>` | - | Establece el Spawn 1 de la arena. | `spazioduels.admin` |
| `/spazioduels setup setspawn2 <arena>` | - | Establece el Spawn 2 de la arena. | `spazioduels.admin` |
| `/spazioduels setup setspectator <arena>`| - | Establece la ubicación de espectador de la arena. | `spazioduels.admin` |
| `/spazioduels setup setlobby` | - | Establece el spawn global del lobby de duelos. | `spazioduels.admin` |
| `/spazioduels kit create <nombre>` | - | Crea un kit nativo con tu inventario actual. | `spazioduels.admin` |
| `/spazioduels kit toggleduel <nombre>`| - | Alterna la activación del kit para duelos. | `spazioduels.admin` |
| `/spazioduels kit togglebuild <nombre>`| - | Alterna si el kit permite colocar/romper bloques. | `spazioduels.admin` |
| `/spazioduels kit list` | - | Lista todos los kits cargados (nativos y de PlayerKits2). | `spazioduels.admin` |
| `/spazioduels reload` | - | Recarga la configuración, kits, KOTHs y arenas. | `spazioduels.admin` |

---

## 🔑 Permisos

| Permiso | Descripción | Por defecto |
| :--- | :--- | :--- |
| `spazioduels.admin.koth` | Acceso completo a la gestión de KOTHs (`/koth`). | OP |
| `spazioduels.admin` | Acceso a administración de arenas, kits y `/sd adminkits`. | OP |
| `spazioduels.admin.event` | Permiso para iniciar eventos automáticos de duelos. | OP |
| `spazioduels.admin.bypass` | Permite ejecutar cualquier comando durante un duelo. | OP |

---

## 📊 Placeholders (PlaceholderAPI & Survival Core)

| Placeholder | Descripción | Ejemplo de Salida |
| :--- | :--- | :--- |
| `%spazioduels_in_duel%` | Indica si el jugador está en un duelo. | `true` / `false` |
| `%spazioduels_mode%` | Modo del duelo actual. | `1v1`, `2v2`, `2v1 Handicap` |
| `%spazioduels_kit%` | Kit seleccionado en el duelo. | `Nodebuff`, `UHC` |
| `%spazioduels_arena%` | Arena donde se disputa el duelo. | `Arena1` |
| `%spazioduels_koth_active%` | Estado activo del evento KOTH. | `true` / `false` |
| `%spazioduels_koth_name%` | Nombre del KOTH activo. | `KothCentral` |
| `%spazioduels_koth_capper%` | Jugador capturando el KOTH en tiempo real. | `Valen` / `Nadie` |
| `%spazioduels_koth_time%` | Tiempo restante para capturar el KOTH. | `04:35` |

---

## 🔧 Requisitos e Instalación

- **Servidor**: Paper / Pufferfish / Spigot `1.21.10`
- **Java**: `21`
- **Dependencias**:
  - `survival_core` (Core obligatorio)
  - `WorldEdit` / `FastAsyncWorldEdit` (Para selección de zonas KOTH)
  - `PlayerKits2` (Opcional - Importación de kits)
  - `PlaceholderAPI` (Opcional)

---

*Desarrollado por y para MineSpazio Network*
