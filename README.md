# ⚔️ SpazioDuels

**SpazioDuels** es un plugin de duelos altamente modular, extremadamente sólido y optimizado para entornos de producción de Minecraft (**Paper 1.21.10** / **Java 21**). Diseñado como addon nativo para el core `survival_core`.

---

## 🌟 Características Principales

### ⚔️ Sistema de Duelos Flexible
- **Sistema de desafío rápido**: `/duel <jugador>` abre una interfaz GUI (`KitSelectorGUI`) para elegir el kit antes de enviar el reto.
- **Invitaciones Interactivas**: Los contrincantes reciben un mensaje formateado en el chat con botones interactivos `[ACEPTAR DUELO]` y `[RECHAZAR DUELO]`.
- **Múltiples Modos de Juego**:
  - **1v1** (Duelo individual clásico).
  - **2v2, 3v3, 4v4** (Duelos por equipos/party).
  - **Handicap** (2v1, 3v2, 4v3 con reglas especiales para el equipo mayoritario, ej. sin casco o vida reducida).
- **Límite de tiempo**: Cada duelo tiene una duración máxima de **10 minutos** (configurable) con visualización de tiempo restante en la Actionbar. En caso de tiempo agotado, el ganador se decide por mayor número de miembros vivos o daño infligido.

### 🔒 Sistema Anti-Dupe de Grado de Producción
- **Etiquetado de Items PDC**: Los ítems de kit están marcados con `PersistentDataContainer` (`spazioduels:kit_item`).
- **Protección de Contenedores y Dropeos**:
  - Cancela el tiro de ítems durante duelos o para ítems del kit.
  - Bloquea la interacción con cofres, shulker boxes, ender chests y marcos de ítems.
  - Limpia ítems de kit en caso de muerte sin dropear nada al suelo.
- **Restauración Asegurada de Inventario (Disk-Backed)**:
  - Antes de cada duelo, el inventario original, armadura, mano secundaria, experiencia, nivel, vida, comida y estado de vuelo se guardan en disco (`plugins/SpazioDuels/data/inventories/<uuid>.yml`).
  - En caso de victoria, derrota, rendición, desconexión del jugador o reinicio del servidor, el inventario original se restaura automáticamente.

### 🛡️ Sistema de Setup & Protección In-Game
- **Administración de Arenas**:
  - Configuración fácil in-game de `spawn1`, `spawn2`, `spectator` y `lobby`.
  - Panel administrativo GUI (`/sd setup`).
- **Protección de Comandos y Grifeos**:
  - Bloqueo de ejecución de comandos no autorizados durante duelos (con whitelist configurable).
  - Cancelación de daño entre compañeros de equipo (Friendly Fire).
  - **Rollback de Bloques**: Para kits con construcción activada (ej. UHC con agua, lava, telarañas), los bloques colocados durante el duelo se revierten automáticamente al finalizar la partida.

### 🏆 Eventos Automáticos & Resúmenes Interactivos
- **Eventos Automáticos**:
  - Inicio de torneo por comando o temporizador automático (`/duelevent start <modo> <kit>`).
  - Anuncios formateados en el chat global con recompensas aleatorias.
  - Botón interactivo `- CLICK PARA INGRESAR AL EVENTO -`.
- **Fase de Torneo (Brackets)**:
  - Generación automática de llaves de eliminación directa entre los jugadores/equipos anotados a través de las arenas disponibles.
- **Resumen Final Interactivo**:
  - Al terminar el evento se transmite el mensaje de ganadores con el botón `- CLICK PARA VER LA INFORMACION -`.
  - Al hacer clic se abre la GUI `EventSummaryGUI` mostrando el ganador/equipo, duración del evento, encuentros jugados, kills totales y recompensas entregadas.

---

## 📜 Lista Completa de Comandos

| Comando | Alias | Descripción | Permiso |
| :--- | :--- | :--- | :--- |
| `/duel <jugador>` | `/duelo`, `/duels` | Envia una solicitud de duelo a un jugador mediante la interfaz GUI de kits. | `Ninguno` |
| `/duel accept <jugador>` | `/duel aceptar` | Acepta una solicitud de duelo pendiente. | `Ninguno` |
| `/duel deny <jugador>` | `/duel rechazar` | Rechaza una solicitud de duelo pendiente. | `Ninguno` |
| `/duel forfeit` | `/duel rendirse` | Abandona y se rinde en el duelo actual. | `Ninguno` |
| `/party create` | `/p`, `/equipo`, `/team` | Crea una party para duelos de equipo o handicap. | `Ninguno` |
| `/party invite <jugador>` | - | Invita a un jugador a tu party. | `Ninguno` |
| `/party accept <líder>` | - | Acepta la invitación de party de un líder. | `Ninguno` |
| `/party leave` | - | Abandona la party actual. | `Ninguno` |
| `/party disband` | - | Disuelve la party (solo el líder). | `Ninguno` |
| `/party info` | - | Muestra los miembros y el líder de la party. | `Ninguno` |
| `/duelevent start <modo> <kit>` | `/eventoduel`, `/devent` | Inicia un evento automático de duelos. | `spazioduels.admin.event` |
| `/duelevent join` | - | Se une al evento de duelos activo. | `Ninguno` |
| `/duelevent summary [uuid]` | - | Abre el panel GUI con el resumen del evento. | `Ninguno` |
| `/spazioduels setup` | `/sd`, `/sduels` | Abre el panel GUI de gestión de arenas. | `spazioduels.admin` |
| `/spazioduels setup create <nombre>` | - | Crea una nueva arena. | `spazioduels.admin` |
| `/spazioduels setup setspawn1 <arena>` | - | Estabilidad el Spawn 1 de la arena en tu ubicación. | `spazioduels.admin` |
| `/spazioduels setup setspawn2 <arena>` | - | Establece el Spawn 2 de la arena en tu ubicación. | `spazioduels.admin` |
| `/spazioduels setup setspectator <arena>`| - | Establece la ubicación del espectador de la arena. | `spazioduels.admin` |
| `/spazioduels setup setlobby` | - | Establece el spawn global del lobby de duelos. | `spazioduels.admin` |
| `/spazioduels kit create <nombre>` | - | Crea un kit con los ítems y armadura de tu inventario. | `spazioduels.admin` |
| `/spazioduels kit delete <nombre>` | - | Elimina un kit existente. | `spazioduels.admin` |
| `/spazioduels kit togglebuild <nombre>`| - | Alterna si el kit permite colocar/romper bloques. | `spazioduels.admin` |
| `/spazioduels kit list` | - | Lista todos los kits registrados. | `spazioduels.admin` |
| `/spazioduels reload` | - | Recarga la configuración, kits y arenas. | `spazioduels.admin` |

---

## 🔑 Permisos

| Permiso | Descripción | Por defecto |
| :--- | :--- | :--- |
| `spazioduels.admin` | Acceso completo a los comandos de administración y setup de arenas/kits. | OP |
| `spazioduels.admin.event` | Acceso para iniciar eventos automáticos de duelos. | OP |
| `spazioduels.admin.bypass` | Permite ejecutar cualquier comando durante un duelo. | OP |

---

## ⚙️ Guía de Setup de la Primera Arena y Kit

1. **Crear una Arena**:
   - Ve a la ubicación del primer jugador y ejecuta: `/sd setup setspawn1 Arena1`
   - Ve a la ubicación del segundo jugador y ejecuta: `/sd setup setspawn2 Arena1`
   - Configura la zona de espectadores: `/sd setup setspectator Arena1`
   - Configura el lobby de salida: `/sd setup setlobby`

2. **Crear un Kit**:
   - Equípate con la armadura, objetos en la mano primaria/secundaria y efectos de poción deseados.
   - Ejecuta: `/sd kit create Normal`
   - (Opcional) Si quieres un kit UHC donde se puedan poner bloques: `/sd kit togglebuild Normal`

3. **Probar un Duelo**:
   - Ejecuta `/duel <Jugador>`
   - Selecciona el kit en el menú GUI.
   - El otro jugador recibirá la invitación en el chat con el botón clickeable `[ACEPTAR DUELO]`.

4. **Iniciar un Evento de Duelos**:
   - Ejecuta `/duelevent start 1v1 Normal`
   - Los jugadores en el servidor verán el anuncio y podrán hacer clic en `- CLICK PARA INGRESAR AL EVENTO -`.

---

## 🔧 Requisitos e Instalación

- **Servidor**: Paper / Pufferfish / Spigot `1.21.10`
- **Java**: `21` o superior
- **Dependencias**:
  - `survival_core` (Core principal)
  - `PlaceholderAPI` (Opcional)

Simplemente coloca `SpazioDuels-1.0.0-SNAPSHOT.jar` en la carpeta `plugins/` de tu servidor y reinicia.

---

*Desarrollado con ❤️ para el core survival_core.*
