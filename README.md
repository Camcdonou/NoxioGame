# NoxioGame

**Real-time Game Server for Noxio Multiplayer Game**

## Overview

NoxioGame is the real-time game server that handles live gameplay, WebSocket communication, game state management, and player interactions for the Noxio multiplayer game platform.

## Credits

**Original Creator:** [InfernoPlus](https://github.com/InfernoPlus)

This is a community-maintained fork of the original Noxio game server. All credit for the original implementation goes to InfernoPlus. This fork is maintained by the community to continue development and support.

## Features

- Real-time multiplayer game logic
- WebSocket-based client communication
- Game state synchronization
- Player collision detection and physics
- WebGL-compatible 3D rendering support
- CORS-enabled for cross-origin requests
- Health check endpoints for monitoring

## Tech Stack

- **Backend:** Java 8, Spring Framework 4.3.5, Spring Boot 2.0.0
- **Server:** Apache Tomcat 9
- **Real-time:** Spring WebSocket
- **Client:** WebGL, JavaScript

## Project Structure

```
noxiogame-core/          # Core game server application
├── src/main/java/       # Java configuration classes
├── src/main/webapp/     # Minimal frontend (game client entry point)
└── src/main/resources/  # Configuration files

noxiogame-module/        # Game logic modules
└── game/                # Game controllers and WebSocket handlers
```

## Setup

### Prerequisites

- Java 8 JDK
- Maven 3.x
- NoxioAuth server running (for authentication)

### Configuration

1. Copy `noxio.properties.example` to `noxio.properties`:
   ```bash
   cp noxiogame-core/src/main/resources/noxio.properties.example \
      noxiogame-core/src/main/resources/noxio.properties
   ```

2. Edit `noxio.properties` with your settings:
   - Server name and location
   - Max player count
   - Auth server connection details
   - Asset file paths

### Build

```bash
mvn clean package -DskipTests
```

This generates `noxiogame-core/target/NoxioGame-1.0.war`

### Run (Development)

```bash
cd noxiogame-core
mvn spring-boot:run
```

Access at: `http://localhost:8080/nxg/`

## Deployment

The WAR file can be deployed to:
- Standalone Tomcat 9 server
- Docker container (see deployment docs)
- Cloud platforms with WebSocket support

### Port Configuration

- **HTTP:** Port 8080 (default) or configured port
- **WebSocket:** Port 7001 (default game communication port)

## Architecture

```
Game Client (Browser)
      ↓ WebSocket
Game Server (NoxioGame)
      ↓ HTTP
Auth Server (NoxioAuth)
      ↓ MySQL
Database
```

## API Endpoints

- `/nxg/` - Game client entry point
- `/nxg/status` - Health check endpoint
- WebSocket endpoint for real-time game communication

## Performance

- Supports 50-100 concurrent players per server instance
- Recommended: 1-2GB RAM allocation
- CPU: Multi-core recommended for physics calculations

## Security Notes

**IMPORTANT:** Never commit these files:
- `noxio.properties` - Contains auth server credentials
- `application.properties` - Contains configuration
- `*.war` files - Contains embedded configuration

See `.gitignore` for complete list.

## Development

### Running with NoxioAuth

The game server requires NoxioAuth to be running for player authentication:

1. Start NoxioAuth server first
2. Configure `noxio.properties` to point to NoxioAuth
3. Start NoxioGame server
4. Players authenticate via NoxioAuth, then connect to NoxioGame for gameplay

## Contributing

1. Create a feature branch: `git checkout -b feat/your-feature`
2. Make your changes
3. Test with multiple connected clients
4. Create a pull request to `main`

## License

[Your License Here]

## Related Repositories

- [NoxioAuth](../NoxioAuth) - Authentication and hub server
- [NoxioAsset](../NoxioAsset) - Asset conversion tools
