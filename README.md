# MCOPT - Minecraft Performance Optimization Mod

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.1-green.svg)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.1.77-orange.svg)](https://neoforged.net/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

MCOPT is a performance optimization mod for Minecraft designed to improve client-side performance in singleplayer and multiplayer environments. It focuses on reducing lag through intelligent chunk rendering, entity culling, and particle system optimizations while maintaining full compatibility with other mods.

## Features

### 🎮 Client-Side Optimizations

#### Chunk Rendering Optimization
- **Smart Chunk Update Limiting**: Prevents frame drops by limiting the number of chunk updates per frame
- **Aggressive Chunk Culling**: Optional advanced culling for maximum FPS (may cause slight pop-in)
- **Frustum Calculation Caching**: Reduces CPU overhead from redundant calculations
- **Elliptical Render Distance** ⭐ NEW: Renders chunks in a 3D ellipsoid instead of square/cylinder
  - Reduces rendered chunk sections by 10-35% for significant FPS improvement
  - Configurable vertical and horizontal stretch factors
  - Maintains visual quality while improving performance
  - Optional debug overlay showing culled chunk count

#### Entity Rendering Optimization
- **Distance-Based Entity Culling**: Automatically skips rendering distant entities
- **Behind-Wall Culling**: Optionally culls entities that are completely behind walls
- **Smart Importance Detection**: Never culls important entities like vehicles or passengers

#### Particle System Optimization
- **Per-Frame Particle Limiting**: Prevents FPS drops from particle explosions
- **Probabilistic Spawn Reduction**: Maintains visual quality while reducing particle count
- **Distance-Based Particle Culling**: Skips particles that are too far from the camera

#### Experience Orb Merging Optimization ⭐ NEW
- **Automatic Orb Merging**: Combines nearby experience orbs into single entities
- **Configurable Merge Radius**: Adjust how aggressively orbs merge
- **Performance Boost**: Significantly reduces lag during mob farming or mining
- **Fully Compatible**: Works seamlessly with vanilla gameplay mechanics

#### Dynamic Memory Management ⭐ NEW
- **GC Spike Prevention**: Object pooling for Vec3 and BlockPos to reduce garbage collection pressure
- **Smart Resource Cleanup**: Automatic cleanup of unused assets on world unload/disconnect
- **Memory HUD**: Real-time RAM usage display in top-left corner
- **Panic Button (F8)**: Emergency memory cleanup with instant feedback
- **FerriteCore Compatible**: Designed to complement static memory optimizations

### ⚙️ Highly Configurable

All optimizations can be toggled and fine-tuned through the mod's configuration file located at `.minecraft/config/mcopt-client.toml`.

## Installation

1. Download and install [NeoForge](https://neoforged.net/) for Minecraft 1.21.1
2. Download the latest MCOPT release from the [Releases](https://github.com/randomstrangerpassenger/MCOPT/releases) page
3. Place the downloaded `.jar` file in your `.minecraft/mods` folder
4. Launch Minecraft with the NeoForge profile

## Configuration

After the first launch, a configuration file will be created at `.minecraft/config/mcopt-client.toml`. You can edit this file to customize the optimization settings.

### Configuration Options

#### Chunk Rendering
```toml
[general.chunk_rendering]
# Enable chunk rendering optimizations
enableChunkOptimizations = true
# Maximum number of chunk updates per frame (1-20, default: 6)
chunkUpdateLimit = 6
# Enable aggressive chunk culling (may cause pop-in)
aggressiveChunkCulling = false
```

#### Elliptical Render Distance
```toml
[general.render_distance]
# Enable elliptical render distance optimization
enableEllipticalRenderDistance = true
# Vertical stretch factor (0.1-3.0, default: 0.75)
# Lower = better performance, higher = see more vertically
verticalRenderStretch = 0.75
# Horizontal stretch factor (0.5-2.0, default: 1.0)
# Values > 1.0 extend render distance horizontally
horizontalRenderStretch = 1.0
# Show debug overlay with culled chunk count
showCulledChunksDebug = false
```

#### Entity Culling
```toml
[general.entity_culling]
# Enable entity culling optimizations
enableEntityCulling = true
# Distance at which entities are culled (16-256 blocks, default: 64)
entityCullingDistance = 64
# Skip rendering entities behind walls
cullEntitiesBehindWalls = true
```

#### Particle System
```toml
[general.particles]
# Enable particle system optimizations
enableParticleOptimizations = true
# Maximum particles per frame (100-4000, default: 500)
maxParticlesPerFrame = 500
# Reduce particle spawn rate (0.0-0.9, default: 0.25 = 25% reduction)
particleSpawnReduction = 0.25
```

#### Memory Management
```toml
[general.memory]
# Enable memory management optimizations
enableMemoryOptimizations = true
# Prevent garbage collection during rendering
aggressiveGCPrevention = true
# Enable object pooling for Vec3 and BlockPos (reduces GC pressure)
enableObjectPooling = true
# Enable aggressive resource cleanup on world unload/disconnect
enableResourceCleanup = true
# Show memory usage HUD in top-left corner
showMemoryHud = true
```

#### Experience Orb Merging
```toml
[general.xp_orb_merging]
# Enable experience orb merging
enableXpOrbMerging = true
# Merge radius in blocks (0.5-5.0, default: 1.5)
# Larger radius = more aggressive merging
xpOrbMergeRadius = 1.5
# Merge check delay in ticks (1-40, default: 10)
# Lower = more frequent merging, higher = less CPU usage
xpOrbMergeDelay = 10
```

## Performance Tips

For best performance in singleplayer:
1. Enable all optimizations in the config
2. Enable `enableEllipticalRenderDistance` for 10-35% FPS boost
3. Enable `enableXpOrbMerging` to reduce lag during mob farming/mining
4. Enable `enableObjectPooling` and `showMemoryHud` to monitor and reduce GC pressure
5. Use **F8 (Panic Button)** when experiencing sudden lag to free memory
6. Set `chunkUpdateLimit` to 4-6 for smooth FPS
7. Set `verticalRenderStretch` to 0.5-0.75 for better performance
8. Set `entityCullingDistance` based on your render distance (32-64 for normal, 64-128 for high)
9. Set `particleSpawnReduction` to 0.25-0.5 depending on your preferences

For high-end systems:
- Increase `chunkUpdateLimit` to 10-15 for faster world updates
- Set `verticalRenderStretch` to 1.0-1.5 to see more vertically
- Set `horizontalRenderStretch` to 1.2-1.5 to extend horizontal view
- Increase `maxParticlesPerFrame` to 1000-2000 for more particles
- Set `xpOrbMergeRadius` to 2.0-3.0 for more aggressive merging
- Set `xpOrbMergeDelay` to 5-10 for more frequent checks
- Disable `aggressiveChunkCulling` if you notice pop-in

For low-end systems:
- Decrease `chunkUpdateLimit` to 2-4
- Set `verticalRenderStretch` to 0.3-0.5 for maximum FPS
- Set `horizontalRenderStretch` to 0.8-0.9 to reduce chunk load
- Set `entityCullingDistance` to 32-48
- Increase `particleSpawnReduction` to 0.5-0.75
- Set `xpOrbMergeRadius` to 2.5-5.0 for maximum orb reduction
- Set `xpOrbMergeDelay` to 15-20 to reduce CPU overhead
- Enable `aggressiveChunkCulling`

## Key Bindings

MCOPT adds the following key bindings (configurable in Minecraft's Controls menu):

| Key | Function | Description |
|-----|----------|-------------|
| **F8** | Memory Panic Button | Triggers emergency memory cleanup (GC + pool clearing) with 5-second cooldown |

The panic button provides instant feedback via chat message showing how much memory was freed.

## Compatibility

MCOPT is designed with mod compatibility as the highest priority:
- Uses non-invasive Mixin injections
- Preserves vanilla behavior and API contracts
- Compatible with most other performance mods
- Safe to use with content mods (no gameplay changes)

### Known Compatible Mods
- **FerriteCore**: Perfect compatibility - MCOPT handles dynamic memory while FerriteCore handles static memory
- Shader mods (OptiFine alternatives)
- World generation mods
- Content and gameplay mods
- Other performance mods (test before using together)

### Potential Conflicts
If you experience issues with other mods, try:
1. Disabling specific optimizations in the config
2. Reporting the issue on our [GitHub Issues](https://github.com/randomstrangerpassenger/MCOPT/issues) page

## Building from Source

### Prerequisites
- Java Development Kit (JDK) 21 or higher
- Git

### Build Steps
```bash
git clone https://github.com/randomstrangerpassenger/MCOPT.git
cd MCOPT
./gradlew build
```

The compiled mod will be located in `build/libs/mcopt-1.0.0.jar`

### Development Setup
```bash
# For IntelliJ IDEA
./gradlew genIntellijRuns

# For Eclipse
./gradlew eclipse

# For VSCode
./gradlew genVSCodeRuns
```

## Technical Details

### Architecture
- **Platform**: NeoForge 21.1.77+
- **Minecraft Version**: 1.21.1
- **Language**: Java 21
- **Injection Method**: Mixin

### Optimization Techniques
1. **Frame-based throttling**: Prevents overwhelming the render thread
2. **Spatial culling**: Skips rendering objects outside the view frustum
3. **Elliptical render distance**: Renders chunks in 3D ellipsoid instead of square/cylinder
4. **Distance-based LOD**: Reduces detail for distant objects
5. **Probabilistic reduction**: Maintains visual quality while reducing load
6. **Calculation caching**: Avoids redundant expensive operations
7. **Configurable stretch factors**: Fine-tune vertical/horizontal render shapes
8. **Entity merging**: Combines nearby experience orbs to reduce entity count
9. **Object pooling**: Reuses Vec3 and BlockPos instances to prevent GC spikes
10. **Smart resource cleanup**: Aggressive cleanup on world unload/disconnect
11. **Memory monitoring**: Real-time HUD and emergency cleanup button

## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues for bugs and feature requests.

### Guidelines
- Follow the existing code style
- Test your changes thoroughly
- Ensure compatibility with other mods
- Document new features and configuration options

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Inspired by optimization strategies from Sodium, Lithium, and Embeddium
- All implementations are original and independent
- Thanks to the NeoForge team for the excellent modding platform

## Support

- **Issues**: [GitHub Issues](https://github.com/randomstrangerpassenger/MCOPT/issues)
- **Discussions**: [GitHub Discussions](https://github.com/randomstrangerpassenger/MCOPT/discussions)

---

**Note**: This mod is focused on client-side performance improvements. While it works on servers, the primary benefits are seen in singleplayer or as a client-side mod in multiplayer.
