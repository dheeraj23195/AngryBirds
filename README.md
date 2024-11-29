# Angry Birds Game (CSE201 Project)

## Project Description
A recreation of the classic Angry Birds game using Java and libGDX framework. Players launch birds from a slingshot to destroy structures and eliminate pigs, with physics-based gameplay and multiple levels.

## Features
- 3 types of birds: Red, Yellow, and Black with unique damage properties
- 3 types of blocks: Wood, Glass, and Steel with varying durability
- 3 types of pigs: Small, Medium, and Large with different health values
- Physics-based collisions and destruction mechanics
- Save/Load game functionality using serialization
- Multiple levels with increasing difficulty
- Score system based on birds used (1-3 stars)
- Pause menu and settings controls

## Demo Video
- Open Demo Video.zip in the Folder

## UML Diagrams , Use case Diagram
- Present in ZIP File


## How to Run
1. Install JDK 17 or higher
2. Clone the repository
3. Open in IntelliJ IDEA
4. Run `gradlew desktop:run`

## Testing
Run JUnit tests: `gradlew test`

## Controls
- Drag bird backward to aim
- Release to launch
- Click pause button for menu
- ESC key also pauses game

## Design Patterns Used
1. Singleton Pattern
   - AssetManager
   - AudioManager

2. Factory Method Pattern
   - Level generation system

## External Sources
- libGDX Framework: https://libgdx.com/
- Box2D Physics Engine Documentation: https://box2d.org/documentation/
- Background music: https://www.dl-sounds.com/royalty-free/category/game-film/video-game/ (Royalty free)
- Game art assets: Created using Adobe Illustrator

## Project Structure
- `core/`: Main game logic and classes
- `desktop/`: Desktop launcher
- `assets/`: Game resources (images, audio)
- `test/`: JUnit test files

## Team Members
- Student 1
- Student 2

## Build Instructions
```bash
# Clean build
./gradlew clean

# Build project
./gradlew build

# Run desktop version
./gradlew desktop:run

# Run tests
./gradlew test

Project Structure
CopyAngryBirdsBox2D/
├── core/
│   ├── src/
│   │   ├── main/
│   │   │   └── java/io/github/angrybirdsbox2d/
│   │   │       ├── AngryBirdsGame.java
│   │   │       ├── AssetManager.java
│   │   │       ├── AudioManager.java
│   │   │       ├── Bird.java
│   │   │       ├── BirdType.java
│   │   │       ├── Block.java
│   │   │       ├── Box2DPhysicsWorld.java
│   │   │       ├── GameSaveData.java
│   │   │       ├── GameScreen.java
│   │   │       └── [other game classes]
│   │   └── test/
│   │       └── java/io/github/angrybirdsbox2d/
│   │           └── GameTest.java
│   └── build.gradle
├── desktop/
│   ├── src/
│   │   └── io/github/angrybirdsbox2d/desktop/
│   │       └── DesktopLauncher.java
│   └── build.gradle
├── assets/
│   ├── music/
│   │   └── background_music.mp3
│   ├── images/
│   │   ├── birds/
│   │   ├── blocks/
│   │   ├── pigs/
│   │   └── ui/
│   └── uiskin.json
├── build.gradle
└── settings.gradle

```

