# ⚔️ SpazioDuels (v1.1.0)

**SpazioDuels** es un plugin de duelos y **KOTH (King Of The Hill)** altamente modular, extremadamente sólido y optimizado para entornos de producción de Minecraft (**Paper 1.21.10** / **Java 21**). Diseñado como addon nativo para el core `survival_core` e integrado con **PlayerKits2** y **WorldEdit / FAWE**.

---

## 🌟 Nuevas Características (v1.1.0): Sistema Completo KOTH

### 🏰 Rey de la Colina (KOTH)
- **Creación & Configuración de KOTHs**:
  - `/koth create <nombre>`: Crea un nuevo KOTH.
  - `/koth setcapdelay <koth> <segundos>`: Configura el tiempo necesario para la captura (en segundos).
  - `/koth setloot <koth>`: Abre una interfaz GUI depositario para configurar los ítems de botín del KOTH con botón de **GUARDAR LOOT**.
- **Integración con WorldEdit / FAWE**:
  - `/koth setzone <koth>`: Configura el área perimetral a partir de la selección de WorldEdit (`//wand`). Al entrar a esta zona, el jugador ve un título en pantalla durante 2 segundos (`"Ingresaste a la zona del Koth <Koth>"`).
  - `/koth setcapzone <koth>`: Configura la zona interna de captura. Al permanecer en ella se descuenta el tiempo de control.
- **Scoreboard & Placeholders en Tiempo Real**:
  - Integrado dinámicamente con la Scoreboard de `survival_core` mediante PlaceholderAPI:
    - `%spazioduels_koth_active%`: Estado activo del KOTH (`true`/`false`).
    - `%spazioduels_koth_name%`: Nombre del KOTH en ejecución.
    - `%spazioduels_koth_capper%`: Nombre del jugador que está capturando en vivo.
    - `%spazioduels_koth_time%`: Tiempo restante formateado (`MM:SS`).
- **Anuncios & Pantalla de Victoria**:
  - Al capturar el KOTH, el ganador ve un título de felicitaciones en su pantalla por 2 segundos.
  - Se transmite un mensaje de felicitaciones y victoria en el chat global del servidor.
  - El botín configurado se entrega automáticamente al inventario del ganador.
- **Hosteo Automático Programado**:
  - Configurable en `config.yml` (`koth_autostart.enabled: true`, `interval_minutes: 120`) para automatizar eventos KOTH aleatorios a lo largo del día.

---

## 📜 Lista Completa de Comandos

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
| `/koth list` | Muestra la lista de KOTHs y sus estadísticas. | `spazioduels.admin.koth` |
| `/koth info <koth>` | Muestra información detallada de un KOTH. | `spazioduels.admin.koth` |

### ⚔️ Comandos de Duelos y Administración (`/duel`, `/sd`, `/party`, `/duelevent`)
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
| `/spazioduels setup setspawn1 <arena>` | - | Establece el Spawn 1 de la arena. | `spazioduels.admin` |
| `/spazioduels setup setspawn2 <arena>` | - | Establece el Spawn 2 de la arena. | `spazioduels.admin` |
| `/spazioduels setup setspectator <arena>`| - | Establece la ubicación de espectador de la arena. | `spazioduels.admin` |
| `/spazioduels setup setlobby` | - | Establece el spawn global del lobby de duelos. | `spazioduels.admin` |
| `/spazioduels reload` | - | Recarga la configuración, kits, KOTHs y arenas. | `spazioduels.admin` |

---

## 🔑 Permisos

| Permiso | Descripción | Por defecto |
| :--- | :--- | :--- |
| `spazioduels.admin.koth` | Acceso a todos los comandos de administración de KOTHs (`/koth`). | OP |
| `spazioduels.admin` | Acceso a comandos de administración, setup de duelos y panel `/sd adminkits`. | OP |
| `spazioduels.admin.event` | Acceso para iniciar eventos automáticos de duelos. | OP |
| `spazioduels.admin.bypass` | Permite ejecutar cualquier comando durante un duelo. | OP |

---

## ⚙️ Guía de Creación de un KOTH Paso a Paso

1. **Crear el KOTH**:
   - `/koth create ArenaKoth`
2. **Establecer el tiempo de captura**:
   - `/koth setcapdelay ArenaKoth 300` (5 minutos)
3. **Seleccionar las zonas con WorldEdit**:
   - Equípate el hacha (`//wand`) y selecciona dos esquinas para el área perimetral.
   - Ejecuta: `/koth setzone ArenaKoth`
   - Selecciona las esquinas para la zona interna de captura (donde deben pararse los jugadores para descontar tiempo).
   - Ejecuta: `/koth setcapzone ArenaKoth`
4. **Configurar el Botín**:
   - Ejecuta: `/koth setloot ArenaKoth`
   - Deposita todos los objetos deseados en la interfaz GUI y haz clic abajo en **`[ GUARDAR LOOT ]`**.
5. **Iniciar el KOTH**:
   - `/koth start ArenaKoth`

---

## 🔧 Requisitos e Instalación

- **Servidor**: Paper / Pufferfish / Spigot `1.21.10`
- **Java**: `21` o superior
- **Dependencias**:
  - `survival_core` (Core principal)
  - `WorldEdit` / `FastAsyncWorldEdit` (Para selección de zonas KOTH)
  - `PlayerKits2` (Opcional - Importación de kits)
  - `PlaceholderAPI` (Opcional)

---

*Desarrollado con ❤️ para el core survival_core.*
