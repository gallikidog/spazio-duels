# ⚔️ SpazioDuels (v1.0.2)

**SpazioDuels** es un plugin de duelos altamente modular, extremadamente sólido y optimizado para entornos de producción de Minecraft (**Paper 1.21.10** / **Java 21**). Diseñado como addon nativo para el core `survival_core` e integrado con **PlayerKits2**.

---

## 🌟 Características Principales

### 🐛 Correcciones y Mejoras (v1.0.2)
- **Corrección de N性的 en Selección de Kits**: Implementación de `InventoryHolder` en todas las GUIs para almacenar de forma persistente y segura el jugador desafiante (`sender`) y el objetivo (`target`), solucionando el error al enviar retos de duelo.

### 📦 Integración Automática con PlayerKits (PlayerKits2)
- **Importación Directa**: Detecta automáticamente el plugin **PlayerKits2** e importa todos los kits definidos en el servidor sin necesidad de re-crearlos.
- **Gestión de Kits para Duelos (`/sd adminkits`)**:
  - Abre un panel administrativo interactivo en GUI para habilitar o deshabilitar qué kits (nativos o de PlayerKits2) estarán disponibles para el sistema de duelos.
  - En la selección de kit para los jugadores (`/duel`), únicamente se mostrarán los kits marcados como **HABILITADOS**.

### ⚔️ Sistema de Duelos Flexible
- **Sistema de desafío rápido**: `/duel <jugador>` abre la GUI (`KitSelectorGUI`) filtrando solo los kits habilitados.
- **Invitaciones Interactivas**: Mensajes con botones interactivos `[ACEPTAR DUELO]` y `[RECHAZAR DUELO]` en el chat.
- **Múltiples Modos de Juego**:
  - **1v1** (Duelo individual).
  - **2v2, 3v3, 4v4** (Duelos por equipos/party).
  - **Handicap** (2v1, 3v2, 4v3 con reglas especiales para el equipo mayoritario).
- **Límite de tiempo**: Duración máxima de **10 minutos** (configurable) con Actionbar.

### 🔒 Sistema Anti-Dupe de Grado de Producción
- **Etiquetado PDC**: Ítems etiquetados con `spazioduels:kit_item`.
- **Protección de Contenedores y Dropeos**: Cancela dropeos y bloquea guardar en cofres, shulker boxes, ender chests y marcos.
- **Restauración Asegurada de Inventario (Disk-Backed)**: Respaldo guardado en `plugins/SpazioDuels/data/inventories/<uuid>.yml` con restauración automática en cualquier circunstancia.

### 🛡️ Setup In-Game & Protección
- **Gestión de Arenas**: Configuración in-game de spawns (`spawn1`, `spawn2`, `spectator`, `lobby`) y panel GUI (`/sd setup`).
- **Protección de Comandos y Rollback**: Bloqueo de comandos no autorizados durante duelos y rollback de bloques colocados en la arena (ej. UHC).

### 🏆 Eventos Automáticos & Resúmenes Interactivos
- **Eventos Automáticos**: Torneos automáticos (`/duelevent start <modo> <kit>`) con botones de ingreso y finalización interactivos.
- **Resumen Final Interactivo**: Clic en el mensaje de finalización abre `EventSummaryGUI` mostrando ganadores, duración, rondas y recompensas.

---

## 📜 Lista Completa de Comandos

| Comando | Alias | Descripción | Permiso |
| :--- | :--- | :--- | :--- |
| `/duel <jugador>` | `/duelo`, `/duels` | Envía una solicitud de duelo abriendo la GUI de kits habilitados. | `Ninguno` |
| `/duel accept <jugador>` | `/duel aceptar` | Acepta una solicitud de duelo pendiente. | `Ninguno` |
| `/duel deny <jugador>` | `/duel rechazar` | Rechaza una solicitud de duelo pendiente. | `Ninguno` |
| `/duel forfeit` | `/duel rendirse` | Se rinde en el duelo actual. | `Ninguno` |
| `/party create` | `/p`, `/equipo`, `/team` | Crea una party para duelos de equipo o handicap. | `Ninguno` |
| `/party invite <jugador>` | - | Invita a un jugador a tu party. | `Ninguno` |
| `/party accept <líder>` | - | Acepta una invitación de party. | `Ninguno` |
| `/party leave` | - | Abandona la party actual. | `Ninguno` |
| `/party disband` | - | Disuelve la party (solo el líder). | `Ninguno` |
| `/party info` | - | Muestra los miembros de la party. | `Ninguno` |
| `/duelevent start <modo> <kit>` | `/eventoduel`, `/devent` | Inicia un evento automático de duelos. | `spazioduels.admin.event` |
| `/duelevent join` | - | Se une al evento de duelos activo. | `Ninguno` |
| `/duelevent summary [uuid]` | - | Abre la GUI con el resumen del evento. | `Ninguno` |
| `/spazioduels adminkits` | `/sd adminkits` | Abre la GUI para habilitar/deshabilitar kits para duelos. | `spazioduels.admin` |
| `/spazioduels setup` | `/sd setup` | Abre la GUI de gestión de arenas. | `spazioduels.admin` |
| `/spazioduels setup create <nombre>` | - | Crea una nueva arena. | `spazioduels.admin` |
| `/spazioduels setup setspawn1 <arena>` | - | Establece el Spawn 1 de la arena. | `spazioduels.admin` |
| `/spazioduels setup setspawn2 <arena>` | - | Establece el Spawn 2 de la arena. | `spazioduels.admin` |
| `/spazioduels setup setspectator <arena>`| - | Establece la ubicación de espectador de la arena. | `spazioduels.admin` |
| `/spazioduels setup setlobby` | - | Establece el spawn global del lobby de duelos. | `spazioduels.admin` |
| `/spazioduels kit create <nombre>` | - | Crea un kit nativo con tu inventario actual. | `spazioduels.admin` |
| `/spazioduels kit toggleduel <nombre>`| - | Alterna la activación del kit para el sistema de duelos. | `spazioduels.admin` |
| `/spazioduels kit togglebuild <nombre>`| - | Alterna si el kit permite colocar/romper bloques. | `spazioduels.admin` |
| `/spazioduels kit list` | - | Lista todos los kits cargados (nativos y de PlayerKits2). | `spazioduels.admin` |
| `/spazioduels reload` | - | Recarga la configuración, kits y arenas. | `spazioduels.admin` |

---

## 🔑 Permisos

| Permiso | Descripción | Por defecto |
| :--- | :--- | :--- |
| `spazioduels.admin` | Acceso a comandos de administración, setup y panel `/sd adminkits`. | OP |
| `spazioduels.admin.event` | Acceso para iniciar eventos automáticos de duelos. | OP |
| `spazioduels.admin.bypass` | Permite ejecutar cualquier comando durante un duelo. | OP |

---

## 🔧 Requisitos e Instalación

- **Servidor**: Paper / Pufferfish / Spigot `1.21.10`
- **Java**: `21` o superior
- **Dependencias**:
  - `survival_core` (Core principal)
  - `PlayerKits2` (Opcional - Importación de kits)
  - `PlaceholderAPI` (Opcional)

---

*Desarrollado con ❤️ para el core survival_core.*
