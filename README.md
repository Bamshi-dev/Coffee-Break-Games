# Coffee Break Games

A collection of three two-player games built as our SEDA Masters project:

- **Memory** — flip cards on a shared board and find matching K-tuples
  (K cards sharing the same symbol). Most points when the board is empty
  wins.
- **Tetris** — competitive two-board Tetris with optional power-ups
  (portals, board/block swaps, speed and rotation effects), bombs, a
  shared-boundary mode where clearing lines shrinks the opponent's board,
  two-block mode, and a custom block editor.
- **Hexagonal Chess** — Glinski's variant on a hexagonal board, with a bot
  opponent, custom starting positions via a board designer, promotions,
  and draw/resign handling.

Every game can be played **locally** (two players sharing one machine) or
over **LAN** (two machines on the same network, one hosts and one joins).
The rules are the same in both modes.


## Team

| Person               | Area                                                  |
|----------------------|-------------------------------------------------------|
| Amirhossein (Amir)   | Game engines — Memory match logic, Tetris/Chess logic |
| Erik                 | UI / FXML controllers, animations, sound, easter eggs |
| Suleyman             | LAN networking — sockets, discovery, lobbies, sync    |

AI usage disclosures per member are in `docs/` (`Suleyman-AI-Usage.md`,
`Erik_AIUsage.md`, `Amirhossein_Mottie_AI_Parts.pdf`).

## Requirements

- **Java 24 or newer** (Java 24 language features + `module-info.java`).
- **Maven 3.9+** (or IntelliJ's bundled Maven).
- For LAN play: both machines on the same local network, with TCP port
  **8765** reachable and UDP port **8766** allowed for host discovery.

## Build

From the repo root:

```sh
mvn clean package
```

This compiles, runs the tests, and writes to `target/`:

- `GameBox.jar` — plain artifact (needs JavaFX on the module path).
- `GameBox-jar-with-dependencies.jar` — fat JAR for `java -jar`, with the
  JavaFX natives for the **current OS only**.

### Cross-platform release build

```sh
mvn clean package -Prelease
```

Same fat JAR, but with the JavaFX natives for **Windows, Linux, Intel mac
and Apple-silicon mac** bundled, so one file runs everywhere (a Java 24+
runtime is still required on the target machine):

```sh
java -jar target/GameBox-jar-with-dependencies.jar
```

## Run

During development:

```sh
mvn javafx:run
```

Or run the fat JAR directly (see above). From IntelliJ, use the green ▶
next to `Launcher.java` under
`src/main/java/seda_project/control_alt_defeat/gamebox/`.

## How to play

### Local games

Pick a game from the main menu, choose **Local Game**, configure it
(player names, plus per game: K/deck size for Memory, level and advanced
settings for Tetris, board setup for Chess) and start. Both players share
the keyboard/mouse.

Hexagonal Chess also offers a **Bot** opponent in local mode.

### LAN games

One player hosts, the other joins.

**Hosting** — choose **Host** in the game's LAN menu, enter your name and
the game configuration, and you land in a lobby that shows your local IP.
The host's settings (deck, board, Tetris advanced settings) apply to both
sides. macOS/Windows may show a firewall prompt the first time — allow
incoming connections, otherwise nobody can join.

**Joining** —
- **Tetris / Hexagonal Chess:** open **Join**, running hosts on the
  network appear automatically in a list (UDP discovery). Select one,
  enter your name, connect.
- **Memory:** enter the host's IP address (shown in their lobby) and your
  name, then connect.

When both players press **Ready**, a 3-second countdown starts the game on
both machines simultaneously.

If a joiner leaves the lobby, the host resets and is discoverable/joinable
again. Backing out of a lobby frees the port, so you can immediately host
another game.

**During a LAN game** — each game keeps both machines in sync over TCP.
Tetris runs a full engine on *each* machine (you simulate your own board,
so your input reacts instantly; cross-board effects like portals and swaps
travel as network messages). Memory and Chess validate moves and mirror
them to the peer.

**Disconnections** — if the connection drops mid-game (app closed, Wi-Fi
gone), the other player gets an alert within at most 5 seconds and returns
to the main menu. There is no automatic reconnect.

### After a game

The result screen shows names, scores and the outcome. **Play Again**
starts a rematch with the same settings (in LAN the host initiates and the
other side follows). **Exit** returns to the main menu.

## Project layout

```
src/main/java/seda_project/control_alt_defeat/gamebox/
├── GameBox.java              JavaFX Application entry
├── Launcher.java             main()
├── Configuration.java        scene loading/navigation helper
├── ViewStack.java            FXML scene navigation stack
├── SoundController.java      music + sound effects (and the Konami code)
├── Memory/
│   ├── Controller/           FXML controllers (menu, lobby, game, result)
│   └── engine/               match logic, scoring, deck setup
├── Tetris/
│   ├── Controller/           menus, lobbies, game screen, block editor
│   ├── Engine/               TetrisEngine, blocks, bombs, power-ups,
│   │                         KeyHandler, advanced settings
│   └── network/              TetrisMessage protocol, dual-engine flag
├── HexChess/
│   ├── Controller/           menus, lobbies, game screen, board designer
│   ├── Engine/               GameEngine, pieces, HexBot
│   └── Network/              ChessMessage protocol
├── network/                  shared LAN layer
│   ├── NetworkLayer/Listener  public interface + callbacks
│   ├── LanSession.java        socket reader + heartbeat + disconnect
│   ├── LanHost/LanClient      accept/connect (host side is cancellable)
│   ├── Discovery/Announcer…   UDP host discovery (port 8766)
│   ├── GameMessage.java       Memory protocol (sealed interface)
│   ├── Lan.java               port + IP utilities
│   └── Session.java           cross-screen state holder
└── ui/                       shared UI helpers

src/main/resources/Views/     FXML layouts (Memory, Tetris, HexChess)
src/main/resources/           Images, Sounds, css
src/test/java/                unit + integration tests
docs/                         PROJECT_DOCUMENTATION.md, requirements PDFs,
                              AI usage disclosures
```

## Tests

JUnit 5, run with:

```sh
mvn test
```

- `gamebox.network.LanSessionTest` — socket layer over loopback: message
  roundtrip, the 5-second peer-disconnect detection, and a regression test
  that cancelling a host lobby releases the port.
- `gamebox.ExampleTest` — placeholder smoke test from the template.

## Known limitations / non-goals

- No internet play, only LAN (exactly two players).
- No persistent accounts or cross-session leaderboard.
- No mobile or web client.
- Memory join is by IP address; automatic discovery is only in Tetris and
  Chess.

## Troubleshooting

**"Cannot resolve symbol" on JavaFX classes after pulling.** Run
`mvn clean compile` once. IntelliJ sometimes loses the JavaFX module path
after a Maven dependency change.

**"Connection refused" when joining.** Either the host hasn't reached the
lobby screen yet (the server socket only opens there), the firewall blocks
inbound TCP 8765, or the machines aren't on the same network.

**No hosts show up in the join list (Tetris/Chess).** UDP broadcast on
port 8766 is blocked by the firewall, or the network (e.g. eduroam-style
Wi-Fi) filters broadcast traffic.

**"Address already in use: bind" when hosting.** Shouldn't happen anymore
(backing out of a lobby now frees the port) — if it does, an old GameBox
process is probably still running in the background.

**Disconnect alert fires quickly.** Correct — a cleanly closed socket is
detected immediately via TCP FIN; the 5-second timeout is the fallback for
ungraceful disconnects (cable pulled, process killed).

## Credits

### Chess
* Pieces from: https://greenchess.net/info.php?item=downloads
* Mug from: https://www.cgtrader.com/items/3536740/download-page
* 3d pieces from: https://free3d.com/3d-model/chess-19650.html

### Images
* Application Icon: https://www.svgrepo.com/svg/211920/coffee-grain-seed?edit=true
* Unmute Button: https://www.svgrepo.com/svg/363075/speaker-simple-none-bold?edit=true
* Mute Button: https://www.svgrepo.com/svg/363074/speaker-simple-slash-bold?edit=true
* Close Icon: https://www.svgrepo.com/svg/522388/close?edit=true

### Sounds
* Chess Pieces: https://pixabay.com/sound-effects/film-special-effects-chess-pieces-60890/
* Chess Background: https://pixabay.com/music/video-games-music-for-puzzle-game-146738/
* Chess Duck(White): https://www.svgrepo.com/svg/479843/duck-toy-illustration-3?edit=true
* Chess Duck(Black): https://www.svgrepo.com/svg/479851/duck-toy-1

* Tetris Background: https://pixabay.com/music/classical-string-quartet-tetris-theme-korobeiniki-rearranged-arr-for-strings-185592/
* Tetris Lineclear: https://pixabay.com/sound-effects/film-special-effects-clear-notification-355970/
* Tetris Piecesnap: https://pixabay.com/sound-effects/film-special-effects-finger-snap-179180/
* Tetris Levelup: https://pixabay.com/sound-effects/film-special-effects-level-up-05-326133/
* Tetris Radialbomb: https://pixabay.com/sound-effects/bomb-explosion-469038/
* Tetris Columnbomb: https://pixabay.com/sound-effects/film-special-effects-drill-spinning-in-open-air-33575/

* Memory Background: https://pixabay.com/music/solo-piano-retro-arcade-game-music-491487/
* Memory Match: https://pixabay.com/sound-effects/film-special-effects-sample-confirm-success03-kofi-by-miraclei-360151/
* Memory Missmatch: https://pixabay.com/sound-effects/film-special-effects-fail-234710/

* Button press: https://pixabay.com/sound-effects/film-special-effects-button-press-382713/
* Lobby Background: https://pixabay.com/music/ambient-coffee-morning-coffee-shop-music-499186/
* Error Sound: https://pixabay.com/sound-effects/film-special-effects-windows-error-sound-effect-35894/

* Konami EasterEgg: https://www.vgmpf.com/Wiki/index.php?title=Nemesis_(ARC)

## Easter Eggs
* Konami Code -> Change Music
* "Upsidedown", "Grumm", "Dinnerbone" -> GameScreen is rotated by 180
* "jeb_" -> GameScreen background has Rainbowcolors
* "Duck" -> Pawns in Chess are replaced by Ducks
* Second color-scheme when performing a double click on the header

## AI-Usage

See the per-member disclosures in `docs/`:
`Suleyman-AI-Usage.md`, `Erik_AIUsage.md`,
`Amirhossein_Mottie_AI_Parts.pdf`.
