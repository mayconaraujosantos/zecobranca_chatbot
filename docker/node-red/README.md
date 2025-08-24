# Node-RED Setup

This directory contains the Docker configuration for running Node-RED.

## Prerequisites

- Docker
- Docker Compose

## Quick Start

1. Create the data directory for persistent storage:
   ```bash
   mkdir data
   ```

2. Start Node-RED:
   ```bash
   docker-compose up -d
   ```

3. Access Node-RED in your browser at http://localhost:1880

## Configuration

The Node-RED container is configured with:

- Port mapping: 1880 (host) -> 1880 (container)
- Persistent data storage in the `data` directory
- Automatic restart policy
- Timezone set to America/Sao_Paulo

## Stopping Node-RED

To stop Node-RED:
```bash
docker-compose down
```

## Data Persistence

All flows and configurations are stored in the `data` directory, which is mounted as a volume in the container. This ensures that your work is preserved between container restarts.
