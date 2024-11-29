package io.github.angrybirdsbox2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.*;

public class GameScreen implements Screen, InputProcessor {
    private final AngryBirdsGame game;
    private LevelSingle currentLevel;
    private Texture backgroundImg;
    private Texture slingBackImg;
    private Texture slingFrontImg;
    private float slingX, slingY, slingW, slingH;
    private static final float PIG_SIZE = 35f;
    private static final float SLING_SCALE = 0.07f;
    private static final float BIRD_SCALE = 0.8f;
    private static final float LAUNCH_SPEED_MULTIPLIER = 7f;
    private static final float PPM = 100f;
    private static final float MAX_DRAG_DISTANCE = 100f;
    private static final float RUBBER_BAND_TENSION = 0.8f;
    private static final int TRAJECTORY_POINTS = 20;
    private static final float TIME_STEP = 1/60f;
    private static final int VELOCITY_ITERATIONS = 6;
    private static final int POSITION_ITERATIONS = 2;
    private static final float GRAVITY=-9.81f/6f;
    private static final float GROUND_HEIGHT = Gdx.graphics.getHeight() * 0.132f;


    private World physicsWorld;
    private Box2DDebugRenderer debugRenderer;
    private final Map<GameObject, Body> bodyMap;
    private float accumulator = 0;
    private Body tempBirdBody;

    private Vector2 slingAnchor;
    private Vector2 dragStart;
    private Vector2 currentDrag;
    private boolean isDragging;
    private boolean birdLaunched;
    private boolean birdStopped;
    private boolean gameStarted;
    private boolean gamePaused;
    private final Set<Body> bodiesToDestroy = new HashSet<>();
    private final boolean levelCompleted = false;
    private final boolean levelWon = false;
    private boolean showingWinLossPopup = false;


    private Stage mainStage;
    private PauseMenuScreen pauseScreen;
    private ShapeRenderer shapeRenderer;

    public GameScreen(AngryBirdsGame game, LevelSingle level) {
        this.game = game;
        this.currentLevel = level;
        this.gamePaused = false;
        this.gameStarted = false;
        this.bodyMap = new HashMap<>();

        initializeBox2D();
        loadAssets();
        setupGameElements();
        setupInput();
    }

    private void initializeBox2D() {
        physicsWorld = new World(new Vector2(0, GRAVITY), true);
        debugRenderer = new Box2DDebugRenderer();
        setupPhysicsWorld();
        initializeCollisionListener();
    }

    private void loadAssets() {
        backgroundImg = AssetManager.getInstance().getTexture("backgroundlevel.png");
        slingBackImg = AssetManager.getInstance().getTexture("catapult_back.png");
        slingFrontImg = AssetManager.getInstance().getTexture("catapult_front.png");
    }

    private void setupGameElements() {
        setupSling();
        createGameUI();
        shapeRenderer = new ShapeRenderer();
    }

    private void setupSling() {
        slingW = Gdx.graphics.getHeight() * SLING_SCALE;
        slingH = slingW * slingBackImg.getHeight() / slingBackImg.getWidth();
        slingX = Gdx.graphics.getWidth() * 0.1f;
        slingY = Gdx.graphics.getHeight() * 0.132f;
        slingAnchor = new Vector2(slingX + slingW * 0.6f, slingY + slingH * 0.6f);
    }

    private void setupInput() {
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(this);
        inputMultiplexer.addProcessor(mainStage);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    private void setupPhysicsWorld() {
        BodyDef groundDef = new BodyDef();
        groundDef.type = BodyDef.BodyType.StaticBody;
        groundDef.position.set(0, toBox2D(GROUND_HEIGHT));

        Body ground = physicsWorld.createBody(groundDef);
        PolygonShape groundShape = new PolygonShape();
        groundShape.setAsBox(toBox2D(Gdx.graphics.getWidth()), toBox2D(5));

        FixtureDef groundFixture = new FixtureDef();
        groundFixture.shape = groundShape;
        groundFixture.friction = 0.7f;
        groundFixture.restitution = 0.1f;

        ground.createFixture(groundFixture);
        groundShape.dispose();

        createInitialBodies();
    }

    private void createInitialBodies() {
        for (Block block : currentLevel.getBlocks()) {
            createBlockBody(block);
        }

        for (Pig pig : currentLevel.getPigs()) {
            createPigBody(pig);
        }
    }

    private void createBlockBody(Block block) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;

        bodyDef.position.set(
            toBox2D(block.getX() + Block.WIDTH/2),
            toBox2D(block.getY() + Block.HEIGHT/2)
        );

        Body body = physicsWorld.createBody(bodyDef);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(toBox2D(Block.WIDTH/2), toBox2D(Block.HEIGHT/2));

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.9f;
        fixtureDef.restitution = 0.2f;

        body.createFixture(fixtureDef);
        body.setUserData(block);
        bodyMap.put(block, body);
        shape.dispose();
    }

    private void createPigBody(Pig pig) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(
            toBox2D(pig.getX() + PIG_SIZE/2),
            toBox2D(pig.getY() + PIG_SIZE/2)
        );

        Body body = physicsWorld.createBody(bodyDef);
        CircleShape shape = new CircleShape();
        Vector2 center = new Vector2(-toBox2D(PIG_SIZE/4), -toBox2D(PIG_SIZE/4));
        shape.setRadius(toBox2D(PIG_SIZE/2));

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.7f;
        fixtureDef.restitution = 0.3f;

        body.createFixture(fixtureDef);
        body.setUserData(pig);
        bodyMap.put(pig, body);
        shape.dispose();
    }

    private void launchBird() {
        if (!currentLevel.getBirds().isEmpty() && !birdStopped) {
            gameStarted = true;
            Bird currentBird = currentLevel.getBirds().get(0);
            Vector2 dragVector = currentDrag.cpy().sub(slingAnchor);

            if (dragVector.len() > 0) {
                float distance = Math.min(dragVector.len(), MAX_DRAG_DISTANCE);
                dragVector.nor().scl(distance);

                createBirdBody(currentBird, currentDrag);
                Body birdBody = bodyMap.get(currentBird);

                float launchPower = (distance / MAX_DRAG_DISTANCE) * LAUNCH_SPEED_MULTIPLIER;
                Vector2 launchVelocity = dragVector.scl(-launchPower);

                birdBody.setLinearVelocity(launchVelocity.x / PPM, launchVelocity.y / PPM);
                birdBody.setBullet(true);

                birdLaunched = true;
            }
        }
    }

    private void createBirdBody(Bird bird, Vector2 position) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(toBox2D(position.x), toBox2D(position.y));
        bodyDef.bullet = true;

        Body body = physicsWorld.createBody(bodyDef);
        CircleShape shape = new CircleShape();
        shape.setRadius(toBox2D(bird.getRadius()));

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 3.0f;
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0.3f;

        body.createFixture(fixtureDef);
        body.setUserData(bird);
        bodyMap.put(bird, body);
        shape.dispose();
    }



    private void initializeCollisionListener() {
        physicsWorld.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Body bodyA = contact.getFixtureA().getBody();
                Body bodyB = contact.getFixtureB().getBody();
                handleCollision(bodyA, bodyB);
            }

            @Override
            public void endContact(Contact contact) {}

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {}

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {}
        });
    }

    private void handleBirdBlockCollision(Object objectA, Object objectB, float impactForce) {
        if ((objectA instanceof Bird && objectB instanceof Block) ||
            (objectB instanceof Bird && objectA instanceof Block)) {
            Bird bird = (Bird)(objectA instanceof Bird ? objectA : objectB);
            Block block = (Block)(objectA instanceof Block ? objectA : objectB);

            float damageMultiplier = impactForce / 20f;  // Changed from 50f
            int damage = (int)(bird.getDamage() * damageMultiplier);
            block.takeDamage(damage);

            if (block.isDestroyed()) {
                Body blockBody = bodyMap.get(block);
                if (blockBody != null) {
                    bodiesToDestroy.add(blockBody);
                    bodyMap.remove(block);
                    currentLevel.getBlocks().remove(block);
                }
            }
        }
    }

    private void resetForNextBird() {
        birdLaunched = false;
        birdStopped = false;
        isDragging = false;
        gameStarted = false;
    }

    private void checkLevelCompletion() {
        if (!showingWinLossPopup) {
            boolean noMorePigs = currentLevel.getPigs().isEmpty();
            if (noMorePigs) {
                int stars = calculateStars();
                currentLevel.setRating(stars);
                if (stars >= 2) {
                    LevelsScreen.unlockNextLevel(currentLevel.getNumber());
                }

                showingWinLossPopup = true;
                gamePaused = true;
                pauseScreen.showWinLoss(true, stars);
            } else if (!birdLaunched && currentLevel.getBirds().isEmpty()) {
                // Only show loss if we have no more birds to launch AND pigs still exist
                showingWinLossPopup = true;
                gamePaused = true;
                pauseScreen.showWinLoss(false, 0);
            }
        }
    }

    private int calculateStars() {
        int totalBirdsAtStart = LevelsScreen.getAvailableBirdsForLevel(currentLevel.getNumber()).size();
        int birdsUsed = totalBirdsAtStart - currentLevel.getBirds().size();

        // Base score on birds used
        int stars = 3;

        // Deduct stars based on birds used
        if (birdsUsed > 1) stars--;
        if (birdsUsed > 2) stars--;

        return Math.max(1, stars);  // Ensure at least 1 star for completing level
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector2 touchPos = new Vector2(screenX, Gdx.graphics.getHeight() - screenY);

        if (!birdLaunched && !currentLevel.getBirds().isEmpty() && isNearSlingshot(touchPos)) {
            isDragging = true;
            dragStart = touchPos;
            currentDrag = touchPos;
            createTempBirdBody();
            return true;
        }
        return false;
    }

    private boolean isNearSlingshot(Vector2 position) {
        return position.dst(slingAnchor) < slingW;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (isDragging) {
            currentDrag = new Vector2(screenX, Gdx.graphics.getHeight() - screenY);
            Vector2 dragVector = currentDrag.cpy().sub(slingAnchor);

            float dragDistance = dragVector.len();
            if (dragDistance > MAX_DRAG_DISTANCE) {
                dragVector.nor().scl(MAX_DRAG_DISTANCE);
                currentDrag = slingAnchor.cpy().add(dragVector);
            }

            float tensionFactor = (dragDistance / MAX_DRAG_DISTANCE) * RUBBER_BAND_TENSION;
            dragVector.scl(1 - tensionFactor);
            updateTempBirdPosition();
            return true;
        }
        return false;
    }

    private void createTempBirdBody() {
        if (!currentLevel.getBirds().isEmpty()) {
            Bird currentBird = currentLevel.getBirds().get(0);
            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.DynamicBody;
            bodyDef.position.set(toBox2D(slingAnchor.x), toBox2D(slingAnchor.y));

            if (tempBirdBody != null) {
                physicsWorld.destroyBody(tempBirdBody);
            }

            tempBirdBody = physicsWorld.createBody(bodyDef);
            CircleShape shape = new CircleShape();
            shape.setRadius(toBox2D(currentBird.getRadius()));

            FixtureDef fixture = new FixtureDef();
            fixture.shape = shape;
            fixture.density = 1.0f;

            tempBirdBody.createFixture(fixture);
            shape.dispose();
        }
    }

    private void updateTempBirdPosition() {
        if (tempBirdBody != null) {
            tempBirdBody.setTransform(toBox2D(currentDrag.x), toBox2D(currentDrag.y), 0);
        }
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (isDragging) {
            isDragging = false;
            if (tempBirdBody != null) {
                physicsWorld.destroyBody(tempBirdBody);
                tempBirdBody = null;
            }
            launchBird();
            return true;
        }
        return false;
    }

    public void restartLevel() {
        // Clear all bodies
        for (Body body : bodyMap.values()) {
            physicsWorld.destroyBody(body);
        }
        bodyMap.clear();

        // Dispose of old world
        physicsWorld.dispose();

        // Create new world with same gravity
        physicsWorld = new World(new Vector2(0, GRAVITY), true);

        // Reset level
        currentLevel = new LevelSingle(currentLevel.getNumber(), currentLevel.getRating(), currentLevel.isUnlocked());

        // Setup physics again
        setupPhysicsWorld();
        initializeCollisionListener();

        // Reset game states
        gameStarted = false;
        birdLaunched = false;
        birdStopped = false;
        isDragging = false;
        showingWinLossPopup = false;

        if (gamePaused) {
            togglePause();
        }
    }

    private void drawSlingBand() {
        if (!currentLevel.getBirds().isEmpty() && isDragging) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.4f, 0.2f, 0.1f, 1);

            Vector2 leftAnchor = new Vector2(slingX + slingW * 0.25f, slingY + slingH * 0.75f);
            Vector2 rightAnchor = new Vector2(slingX + slingW * 0.75f, slingY + slingH * 0.75f);
            Vector2 controlPoint = currentDrag.cpy().add(0, 10);

            for (int i = -1; i <= 1; i++) {
                float offset = i * 1.5f;
                shapeRenderer.rectLine(
                    leftAnchor.x + offset, leftAnchor.y,
                    currentDrag.x, currentDrag.y,
                    3
                );
                shapeRenderer.rectLine(
                    rightAnchor.x + offset, rightAnchor.y,
                    currentDrag.x, currentDrag.y,
                    3
                );
            }

            shapeRenderer.end();
        }
    }

    public void quitToMainMenu() {
        showingWinLossPopup = false;
        if (gamePaused) {
            togglePause();
        }
        game.setScreen(new LevelsScreen(game));
    }

    public boolean isPaused() {
        return gamePaused;
    }

    private void drawTrajectoryPreview() {
        if (isDragging && currentDrag != null) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(0f, 0f, 0f, 1f);

            Vector2 dragVector = currentDrag.cpy().sub(slingAnchor);
            float distance = Math.min(dragVector.len(), MAX_DRAG_DISTANCE);
            dragVector.nor().scl(-distance);

            float launchPower = (distance / MAX_DRAG_DISTANCE) * LAUNCH_SPEED_MULTIPLIER;
            Vector2 velocity = dragVector.scl(launchPower / PPM);

            float x = currentDrag.x;
            float y = currentDrag.y;
            float dt = 1/60.0f;
            float gravity =GRAVITY;

            for (int i = 0; i < 100; i++) {
                float nextX = x + velocity.x * PPM * dt;
                float nextY = y + velocity.y * PPM * dt;

                shapeRenderer.line(x, y, nextX, nextY);

                x = nextX;
                y = nextY;
                velocity.y += gravity * dt;

                if (y < 0) break;
            }
            shapeRenderer.end();
        }
    }

    private void handleBirdStop() {
        if (birdLaunched && !currentLevel.getBirds().isEmpty()) {
            Bird currentBird = currentLevel.getBirds().get(0);
            Body birdBody = bodyMap.get(currentBird);

            if (birdBody != null) {
                Vector2 pos = birdBody.getPosition();
                Vector2 vel = birdBody.getLinearVelocity();
                boolean offScreen = toPixels(pos.x) < 0 || toPixels(pos.x) > Gdx.graphics.getWidth() ||
                    toPixels(pos.y) < 0 || toPixels(pos.y) > Gdx.graphics.getHeight();
                boolean stopped = vel.len() < 0.1f;

                if (offScreen || stopped) {
                    physicsWorld.destroyBody(birdBody);
                    bodyMap.remove(currentBird);
                    currentLevel.getBirds().remove(0);
                    resetForNextBird();
                    checkLevelCompletion();
                    if (currentLevel.getBirds().isEmpty() && currentLevel.getPigs().isEmpty()) {
                        int stars = calculateStars();
                        currentLevel.setRating(stars);
                        if (stars >= 2) {
                            LevelsScreen.unlockNextLevel(currentLevel.getNumber());
                        }
                        showingWinLossPopup = true;
                        gamePaused = true;
                        pauseScreen.showWinLoss(true, stars);
                    } else if (currentLevel.getBirds().isEmpty() && !currentLevel.getPigs().isEmpty()) {
                        // Lost condition - no more birds but pigs remain
                        showingWinLossPopup = true;
                        gamePaused = true;
                        pauseScreen.showWinLoss(false, 0);
                    }
                }
            }
        }
    }

    // In GameScreen.java, update/add these methods:

    private void handleCollision(Body bodyA, Body bodyB) {
        if (!gameStarted) return;

        Object userDataA = bodyA.getUserData();
        Object userDataB = bodyB.getUserData();

        float relativeVelocity = bodyA.getLinearVelocity().sub(bodyB.getLinearVelocity()).len();
        float impactForce = relativeVelocity * Math.max(bodyA.getMass(), bodyB.getMass());

        // Check for ground collisions first
        if (handleGroundCollision(userDataA, bodyA, impactForce) ||
            handleGroundCollision(userDataB, bodyB, impactForce)) {
            return;
        }

        handleBirdPigCollision(userDataA, userDataB, impactForce);
        handleBirdBlockCollision(userDataA, userDataB, impactForce);
        handleBlockPigCollision(userDataA, userDataB, impactForce);
    }


    private boolean handleGroundCollision(Object userData, Body body, float impactForce) {
        if (userData instanceof Pig && body.getPosition().y < toBox2D(GROUND_HEIGHT + 10)) {
            Pig pig = (Pig) userData;
            // Ground impact damage increases with velocity
            float damageMultiplier = impactForce / 10f;
            int groundDamage = (int)(300 * damageMultiplier); // Base ground damage
            pig.takeDamage(groundDamage);

            checkPigDestruction(pig);
            return true;
        }
        return false;
    }
    private void handleBirdPigCollision(Object objectA, Object objectB, float impactForce) {
        if ((objectA instanceof Bird && objectB instanceof Pig) ||
            (objectB instanceof Bird && objectA instanceof Pig)) {
            Bird bird = (Bird)(objectA instanceof Bird ? objectA : objectB);
            Pig pig = (Pig)(objectA instanceof Pig ? objectA : objectB);

            float damageMultiplier = impactForce / 15f;  // Reduced from previous value
            int damage = (int)(bird.getDamage() * damageMultiplier);
            pig.takeDamage(damage);

            if (pig.getHp() <= 0) {
                Body pigBody = bodyMap.get(pig);
                if (pigBody != null) {
                    bodiesToDestroy.add(pigBody);
                    bodyMap.remove(pig);
                    currentLevel.getPigs().remove(pig);
                }
            }
        }
    }

    private void handleBlockPigCollision(Object objectA, Object objectB, float impactForce) {
        if ((objectA instanceof Block && objectB instanceof Pig) ||
            (objectB instanceof Block && objectA instanceof Pig)) {

            Block block = (Block)(objectA instanceof Block ? objectA : objectB);
            Pig pig = (Pig)(objectA instanceof Pig ? objectA : objectB);

            // Block collision damage based on impact force and block material
            float damageMultiplier = impactForce / 15f;
            int baseDamage = 200; // Base damage for block collisions

            // Adjust damage based on block type
            if (block instanceof SteelBlock) {
                baseDamage *= 1.5f; // Steel blocks deal more damage
            } else if (block instanceof GlassBlock) {
                baseDamage *= 0.8f; // Glass blocks deal less damage
            }

            int damage = (int)(baseDamage * damageMultiplier);
            pig.takeDamage(damage);

            checkPigDestruction(pig);
        }
    }

    private void checkPigDestruction(Pig pig) {
        if (pig.getHp() <= 0) {
            Body pigBody = bodyMap.get(pig);
            if (pigBody != null) {
                bodiesToDestroy.add(pigBody);
                bodyMap.remove(pig);
                currentLevel.getPigs().remove(pig);
                checkLevelCompletion();
            }
        }
    }

    // Update drawPigs method to include fading based on health
    private void drawPigs() {
        for (Pig pig : currentLevel.getPigs()) {
            Texture pigImg = pig.getTexture();

            // Calculate alpha based on health percentage
            float healthPercentage = (float)pig.getHp() / pig.getMaxHealth();
            float alpha = Math.max(0.3f, healthPercentage); // Minimum alpha of 0.3

            game.gameBatch.setColor(1, 1, 1, alpha);
            game.gameBatch.draw(pigImg,
                pig.getX(), pig.getY(),            // Position
                PIG_SIZE/2, PIG_SIZE/2,            // Origin
                PIG_SIZE, PIG_SIZE,                // Size
                1, 1,                              // Scale
                pig.getRotation(),                 // Rotation angle
                0, 0,                              // Source rectangle position
                pigImg.getWidth(),                 // Source rectangle width
                pigImg.getHeight(),                // Source rectangle height
                false, false);                     // Flip horizontally/vertically
        }
        game.gameBatch.setColor(1, 1, 1, 1); // Reset color
    }

    // Add new method to check for ground collision
    private void checkPigGroundCollision(Pig pig, float velocityY) {
        if (velocityY < -5f) {  // If pig hits ground with significant force
            pig.takeDamage(100);  // Direct damage from ground impact

            if (pig.getHp() <= 0) {
                Body pigBody = bodyMap.get(pig);
                if (pigBody != null) {
                    bodiesToDestroy.add(pigBody);
                    bodyMap.remove(pig);
                    currentLevel.getPigs().remove(pig);
                }
            }
        }
    }

    private void updateGameObjects() {
        handleBirdStop();
        Map<GameObject, Body> bodyMapCopy = new HashMap<>(bodyMap);

        for (Map.Entry<GameObject, Body> entry : bodyMapCopy.entrySet()) {
            GameObject obj = entry.getKey();
            Body body = entry.getValue();

            if (body.isActive()) {
                Vector2 position = body.getPosition();
                float angle = body.getAngle() * MathUtils.radiansToDegrees; // Convert radians to degrees

                // Update rotation for all game objects
                obj.setRotation(angle);

                if (obj instanceof Pig) {
                    boolean offScreen = toPixels(position.x) < -50 ||
                        toPixels(position.x) > Gdx.graphics.getWidth() + 50 ||
                        toPixels(position.y) < -50 ||
                        toPixels(position.y) > Gdx.graphics.getHeight() + 50;

                    if (offScreen) {
                        physicsWorld.destroyBody(body);
                        bodyMap.remove(obj);
                        currentLevel.getPigs().remove(obj);
                        checkLevelCompletion();
                        continue;
                    }
                    obj.setX(toPixels(position.x) - PIG_SIZE/2);
                    obj.setY(toPixels(position.y) - PIG_SIZE/2);
                }
                else if (obj instanceof Block) {
                    obj.setX(toPixels(position.x) - Block.WIDTH/2);
                    obj.setY(toPixels(position.y) - Block.HEIGHT/2);
                } else if (obj instanceof Bird) {
                    obj.setX(toPixels(position.x) - ((Bird) obj).getRadius());
                    obj.setY(toPixels(position.y) - ((Bird) obj).getRadius());
                }
            }
        }
    }

    public LevelSingle getCurrentLevel() {
        return currentLevel;
    }

    private void destroyQueuedBodies() {
        for (Body body : bodiesToDestroy) {
            physicsWorld.destroyBody(body);
        }
        bodiesToDestroy.clear();
    }

    @Override
    public void render(float delta) {
        clearBackground();

        if (!gamePaused) {
            doPhysicsStep(delta);
            updateGameObjects();
            destroyQueuedBodies();
        }

        // Always render game state
        game.gameBatch.begin();
        drawBackground();
        drawSlingBack();
        drawBirds();
        drawBlocks();
        drawPigs();
        drawSlingFront();
        game.gameBatch.end();

        if (isDragging) {
            drawSlingBand();
            drawTrajectoryPreview();
        }

        debugRenderer.render(physicsWorld, game.gameBatch.getProjectionMatrix().cpy().scale(PPM, PPM, 1));

        mainStage.act(delta);
        mainStage.draw();

        // Draw pause screen or win/loss popup on top
        if (gamePaused) {
            pauseScreen.render(delta);
        }
    }

    public void togglePause() {
        gamePaused = !gamePaused;
        if (gamePaused) {
            pauseScreen.togglePauseMenu();  // This will create and show the pause menu
            Gdx.input.setInputProcessor(pauseScreen.getStage());
        }
        else {
            InputMultiplexer inputMultiplexer = new InputMultiplexer();
            inputMultiplexer.addProcessor(this);
            inputMultiplexer.addProcessor(mainStage);
            Gdx.input.setInputProcessor(inputMultiplexer);
        }
    }

    public PauseMenuScreen getPauseMenu() {
        return pauseScreen;
    }

    private void createGameUI() {
        mainStage = new Stage(new ScreenViewport());
        addPauseButton();
        pauseScreen = new PauseMenuScreen(game, this);
    }

    private void addPauseButton() {
        Texture pauseImg = AssetManager.getInstance().getTexture("pause.png");
        ImageButton pauseBtn = new ImageButton(new TextureRegionDrawable(new TextureRegion(pauseImg)));
        pauseBtn.setSize(50, 50);
        pauseBtn.setPosition(Gdx.graphics.getWidth() - 60, Gdx.graphics.getHeight() - 60);
        pauseBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                togglePause();
            }
        });
        mainStage.addActor(pauseBtn);
    }
    private void drawBackground() {
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        game.gameBatch.draw(backgroundImg, 0, 0, width, height);
    }

    private void drawSlingBack() {
        game.gameBatch.draw(slingBackImg, slingX, slingY, slingW, slingH);
    }

    private void drawSlingFront() {
        game.gameBatch.draw(slingFrontImg, slingX, slingY, slingW, slingH);
    }

    private void drawBirds() {
        List<Bird> birds = currentLevel.getBirds();
        if (!birds.isEmpty()) {
            Bird mainBird = birds.get(0);
            float birdSize = slingW * BIRD_SCALE;

            if (birdLaunched) {
                drawBird(mainBird, mainBird.getX(), mainBird.getY(), birdSize);
            } else if (isDragging && currentDrag != null) {
                drawBird(mainBird, currentDrag.x - birdSize * 0.5f, currentDrag.y - birdSize * 0.5f, birdSize);
            } else {
                drawBird(mainBird, slingX + slingW * 0.6f - birdSize * 0.5f, slingY + slingH * 0.6f, birdSize);
            }

            for (int i = 1; i < birds.size(); i++) {
                Bird waitingBird = birds.get(i);
                float waitX = slingX - (i * (birdSize * 1.2f));
                drawBird(waitingBird, waitX, slingY, birdSize);
            }
        }
    }

    private void drawBird(Bird bird, float x, float y, float size) {
        Texture birdImg = bird.getTexture();
        float ratio = (float) birdImg.getWidth() / birdImg.getHeight();

        game.gameBatch.draw(birdImg,
            x, y,
            size/2, size/(2*ratio),
            size, size/ratio,
            1, 1,
            bird.getRotation(),
            0, 0,
            birdImg.getWidth(),
            birdImg.getHeight(),
            false, false);
    }

    private void drawBlocks() {
        for (Block block : currentLevel.getBlocks()) {
            Texture blockImg = AssetManager.getInstance().getTexture(block.getMaterial() + "_block.png");
            float alpha = 1f - (block.getDestructionProgress() * 0.5f);
            game.gameBatch.setColor(1, 1, 1, alpha);

            game.gameBatch.draw(blockImg,
                block.getX(), block.getY(),
                Block.WIDTH/2, Block.HEIGHT/2,
                Block.WIDTH, Block.HEIGHT,
                1, 1,
                block.getRotation(),
                0, 0,
                blockImg.getWidth(),
                blockImg.getHeight(),
                false, false);
        }
        game.gameBatch.setColor(1, 1, 1, 1);
    }


    private void clearBackground() {
        Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void doPhysicsStep(float deltaTime) {
        float frameTime = Math.min(deltaTime, 0.25f);
        accumulator += frameTime;
        while (accumulator >= TIME_STEP) {
            physicsWorld.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
            accumulator -= TIME_STEP;
        }
    }


    private float toBox2D(float pixels) {
        return pixels / PPM;
    }

    private float toPixels(float box2DUnits) {
        return box2DUnits * PPM;
    }

    @Override
    public void dispose() {
        mainStage.dispose();
        pauseScreen.dispose();
        shapeRenderer.dispose();
        physicsWorld.dispose();
        debugRenderer.dispose();
    }

    @Override
    public void resize(int width, int height) {
        mainStage.getViewport().update(width, height, true);
        pauseScreen.resize(width, height);
    }

    @Override
    public void show() {
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(this);
        inputMultiplexer.addProcessor(mainStage);
        Gdx.input.setInputProcessor(inputMultiplexer);
        AudioManager.getInstance().playBackgroundMusic();
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void pause() {
        if (!gamePaused) {
            togglePause();
        }
    }

    @Override
    public void resume() {
        if (gamePaused) {
            togglePause();
        }
    }

    @Override
    public boolean keyDown(int keycode) { return false; }
    @Override
    public boolean keyUp(int keycode) { return false; }
    @Override
    public boolean keyTyped(char character) { return false; }
    @Override
    public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override
    public boolean scrolled(float amountX, float amountY) { return false; }
    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
}
