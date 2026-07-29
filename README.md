# ⚔️ SpazioDuels (v1.2.0)

**SpazioDuels** es un plugin modular, robusto y optimizado para entornos de producción de Minecraft (**Paper 1.21.1** / **Java 21**). Diseñado como un addon nativo para el core `survival_core`, ofrece un sistema completo de **Duelos**, **Eventos Automáticos de Duelos**, **Hosteo Automático de KOTHs**, **Scoreboard Dinámica (Practice & HCF)**, **Emulación PvP 1.8.9 (Hit Detection & Knockback)**, **Integración con PlayerKits2**, **Protección Anti-Dupe** y **WorldEdit / FAWE**.

---

## 🌟 Características Principales

### 📊 Sistema de Scoreboards Dinámicas (Practice & HCF) (`scoreboard.yml`)
- **Scoreboard Estilo Practice (Duelos)**:
  - **Modo 1v1**: Muestra el nombre del rival, vida restante (❤), golpes asestados (hits) y el tiempo transcurrido del duelo (`%duel_time%`).
  - **Modo Equipos / Party**: Lista dinámica de integrantes de tu equipo y del equipo rival mostrando su salud/hits o estado de `MUERTO`.
- **Scoreboard Estilo HCF (KOTH)**:
  - Se activa **únicamente** cuando los jugadores ingresan a la zona de un KOTH activo.
  - Muestra el nombre del KOTH, estado de captura, capper actual (`%koth_capper%`) y temporizador restante en segundos y milisegundos (`%koth_time_ms%`).
- **Anulación & Restauración Automática**:
  - Reemplaza temporalmente las scoreboards de otros plugins (ScoreboardWave, FeatherBoard, Tab, etc.) durante duelos o en zonas de KOTH activo.
  - Al salir de la zona del KOTH o finalizar el duelo, **restaura automáticamente** la scoreboard previa del jugador.
- **100% Configurable**: Títulos, líneas, colores, ritmos de actualización y placeholders editables en `scoreboard.yml`.

### 🤺 Módulo de Emulación PvP 1.8.9 (Hit Detection & Knockback)
- **Ataques Instantáneos sin Cooldown**: Desactiva el indicador y retardo de ataque de Minecraft 1.9+ (`GENERIC_ATTACK_SPEED: 16.0`) para emular el combate rápido de la 1.8.9.
- **Hit Detection & No-Damage-Ticks**: Ticks de invulnerabilidad reducidos (configurables, por defecto 10 ticks en lugar de 20 vanilla) para permitir combos fluidos estilo practice.
- **Knockback 1.8.9 Personalizado**: Cálculo físico de empuje horizontal, vertical y sprint-hit (W-Tap) configurable en `config.yml`.

### ⚔️ Sistema de Duelos (Solo, Equipos y Handicap)
- **Desafío Rápido e Interactivo**: `/duel <jugador>` abre una interfaz GUI (`KitSelectorGUI`) filtrando automáticamente los kits habilitados.
- **Invitaciones en Chat**: Los jugadores reciben mensajes interactivos con botones `[ACEPTAR DUELO]` y `[RECHAZAR DUELO]`.
- **Múltiples Modos de Juego**: `1v1`, `2v2`, `3v3`, `4v4` y `Handicap` (2v1, 3v2, 4v3).
- **Estadísticas Post-Duelo**: Mensaje final al terminar la partida con el desglose de **vida restante (❤)** y **golpes asestados (hits)** de todos los participantes.
- **Retorno Automático de Ubicación**: Al finalizar el duelo, todos los participantes (vivos y espectadores) son teletransportados automáticamente a su ubicación previa al duelo.

### 🏆 Eventos Automáticos de Duelos y Torneos Programados
- **Torneos Automáticos**: Anuncios interactivos en el chat con botón `- CLICK PARA INGRESAR AL EVENTO -` y desarrollo automático de rondas de torneo (brackets).
- **Resumen Post-Evento**: Botón `- CLICK PARA VER LA INFORMACION -` que abre la GUI `EventSummaryGUI` mostrando el ganador, duración, muertes y recompensas.
- **Programación Automática (`duel_event_autostart`)**: Configurable en `config.yml` para hostear eventos y torneos de duelos automáticamente durante el día.

### 🏰 Sistema KOTH (King Of The Hill)
- **Creación & Configuración**: Definición de tiempo de captura (`/koth setcapdelay`), botín interactivo con botón **GUARDAR LOOT** (`/koth setloot`).
- **Integración con WorldEdit / FAWE**: Configuración de zona perimetral (`/koth setzone`) y zona de captura (`/koth setcapzone`).
- **Notificaciones & Títulos**: Título en pantalla durante 2 segundos al entrar al área y al capturar el KOTH.
- **Hosteo Automático de KOTHs (`koth_autostart`)**: Programación automática de KOTHs aleatorios durante el día.

### 📦 Integración con PlayerKits (PlayerKits2)
- **Importación Directa & Exacta**: Importación de kits desde **PlayerKits2** respetando las posiciones exactas de inventario/hotbar (slots 0-35), armadura (slots 36-39) y mano secundaria (slot 40).
- **Gestión de Kits (`/sd adminkits`)**: Panel GUI interactivo para marcar qué kits están **HABILITADOS** o **DESHABILITADOS** para duelos.

### 🔒 Sistema Anti-Dupe de Grado de Producción
- **Etiquetado PDC**: Todos los ítems de kit están marcados con `spazioduels:kit_item`.
- **Protección de Contenedores y Dropeos**: Cancela la tirada de ítems y bloquea guardar ítems de duelos en cofres, shulkers, ender chests y marcos.
- **Restauración Asegurada (Disk-Backed)**: Respaldo automático del inventario original en `plugins/SpazioDuels/data/inventories/<uuid>.yml` con restauración garantizada al finalizar la partida, desconectarse o ante un reinicio del servidor.

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
| `/spazioduels reload` | - | Recarga la configuración, kits, KOTHs, scoreboards y arenas. | `spazioduels.admin` |

---

## 🔧 Requisitos e Instalación

- **Servidor**: Paper / Pufferfish / Spigot `1.21.1`
- **Java**: `21`
- **Dependencias**:
  - `survival_core` (Core obligatorio)
  - `WorldEdit` / `FastAsyncWorldEdit` (Para selección de zonas KOTH)
  - `PlayerKits2` (Opcional - Importación de kits)
  - `PlaceholderAPI` (Opcional)

---

*Desarrollado por y para MineSpazio Network*
