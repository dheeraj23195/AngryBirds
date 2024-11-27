package io.github.angrybirdsbox2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class GameScreen implements Screen, InputProcessor {
    private final AngryBirdsGame game;
    private LevelSingle currentLevel;
    private Texture backgroundImg;
    private Texture slingBackImg;
    private Texture slingFrontImg;
    private float slingX, slingY, slingW, slingH;
    private static final float PIG_SIZE = 50f;
    private static final float SLING_SCALE = 0.07f;
    private static final float BIRD_SCALE = 0.8f;
    private static final float LAUNCH_SPEED_MULTIPLIER = 5f;
    private static final float PPM = 32f;
    private static final float MAX_DRAG_DISTANCE = 100f;
    private static final float RUBBER_BAND_TENSION = 0.8f;
    private static final float SLINGSHOT_DAMPING = 0.85f;
    private static final int TRAJECTORY_POINTS = 20;
    private static final float TIME_STEP = 1/60f;
    private static final int VELOCITY_ITERATIONS = 6;
    private static final int POSITION_ITERATIONS = 2;

    private World physicsWorld;
    private Box2DDebugRenderer debugRenderer;
    private Map<GameObject, Body> bodyMap;
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
        physicsWorld = new World(new Vector2(0, -9.81f), true);
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
        groundDef.position.set(0, toBox2D(slingY+50f));

        Body ground = physicsWorld.createBody(groundDef);
        PolygonShape groundShape = new PolygonShape();
        groundShape.setAsBox(toBox2D(Gdx.graphics.getWidth()), toBox2D(55));

        FixtureDef groundFixture = new FixtureDef();
        groundFixture.shape = groundShape;
        groundFixture.friction = 0.5f;

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

        // Position the body at the center of the sprite
        bodyDef.position.set(
            toBox2D(block.getX() + Block.WIDTH/2),
            toBox2D(block.getY() + Block.HEIGHT/2)
        );

        Body body = physicsWorld.createBody(bodyDef);
        PolygonShape shape = new PolygonShape();

        // Create the box around the center point
        shape.setAsBox(toBox2D(Block.WIDTH/2), toBox2D(Block.HEIGHT/2));

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.4f;
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
        //shape.setPosition(center);
        shape.setRadius(toBox2D(PIG_SIZE/2));

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.3f;
        fixtureDef.restitution = 0.3f;

        body.createFixture(fixtureDef);
        body.setUserData(pig);
        bodyMap.put(pig, body);
        shape.dispose();
    }

    private void drawDebugDots() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);
        for (Body body : bodyMap.values()) {
            Vector2 center = body.getPosition();
            shapeRenderer.circle(toPixels(center.x), toPixels(center.y), 3);
        }
        shapeRenderer.setColor(Color.BLACK);
        for (Block block : currentLevel.getBlocks()) {
            shapeRenderer.circle(block.getX(), block.getY(), 3);
        }
        for (Pig pig : currentLevel.getPigs()) {
            shapeRenderer.circle(pig.getX(), pig.getY(), 3);
        }

        shapeRenderer.end();
    }

    private void updateGameObjects() {
        for (Map.Entry<GameObject, Body> entry : bodyMap.entrySet()) {
            GameObject obj = entry.getKey();
            Body body = entry.getValue();
            Vector2 position = body.getPosition();
            obj.setX(toPixels(position.x) - Block.WIDTH/2);
            obj.setY(toPixels(position.y) - Block.HEIGHT/2);
        }
    }
    private void createBirdBody(Bird bird, Vector2 position) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(toBox2D(position.x), toBox2D(position.y));

        Body body = physicsWorld.createBody(bodyDef);
        CircleShape shape = new CircleShape();
        if(bird.getBirdType()==BirdType.RED){
            shape.setRadius(toBox2D(bird.getRadius()-5));
        } else if (bird.getBirdType()==BirdType.BLACK) {
            shape.setRadius(toBox2D(bird.getRadius()));
        } else{
            shape.setRadius(toBox2D(bird.getRadius()-10));
        }
        FixtureDef fixture = new FixtureDef();
        fixture.shape = shape;
        fixture.density = 4.0f;
        fixture.friction = 0.3f;
        fixture.restitution = 0.4f;

        body.createFixture(fixture);
        body.setUserData(bird);
        bodyMap.put(bird, body);
        shape.dispose();
        body.setAngularDamping(0.8f);
        body.setLinearDamping(0.2f);
    }

    private void launchBird() {
        if (!currentLevel.getBirds().isEmpty() && !birdStopped) {
            gameStarted = true;
            Bird currentBird = currentLevel.getBirds().get(0);

            Vector2 dragVector = slingAnchor.cpy().sub(currentDrag);
            float distance = dragVector.len();
            float launchPower = (distance / MAX_DRAG_DISTANCE) * 25f;
            Vector2 impulse = dragVector.nor().scl(launchPower);
            createBirdBody(currentBird, currentDrag);
            Body birdBody = bodyMap.get(currentBird);
            birdBody.setLinearVelocity(toBox2D(impulse.x), toBox2D(impulse.y));
            birdBody.setBullet(true);
            birdLaunched = true;
        }
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

    private void handleCollision(Body bodyA, Body bodyB) {
        Object userDataA = bodyA.getUserData();
        Object userDataB = bodyB.getUserData();
        float impactForce = calculateImpactForce(bodyA, bodyB);

        if (userDataA instanceof Bird && userDataB instanceof Block) {
            handleBirdBlockCollision((Bird)userDataA, (Block)userDataB, impactForce);
        } else if (userDataB instanceof Bird && userDataA instanceof Block) {
            handleBirdBlockCollision((Bird)userDataB, (Block)userDataA, impactForce);
        }
    }

    private float calculateImpactForce(Body bodyA, Body bodyB) {
        Vector2 velA = bodyA.getLinearVelocity();
        Vector2 velB = bodyB.getLinearVelocity();
        return velA.sub(velB).len();
    }

    private void handleBirdBlockCollision(Bird bird, Block block, float impactForce) {
        float damageMultiplier = impactForce / 500f;
        int damage = (int)(bird.getDamage() * damageMultiplier);
        block.takeDamage(damage);

        if (block.isDestroyed()) {
            Body blockBody = bodyMap.get(block);
            if (blockBody != null) {
                physicsWorld.destroyBody(blockBody);
                bodyMap.remove(block);
            }
            currentLevel.getBlocks().remove(block);
        }
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector2 touchPos = new Vector2(screenX, Gdx.graphics.getHeight() - screenY);

        if (!gameStarted && !currentLevel.getBirds().isEmpty() && isNearSlingshot(touchPos)) {
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
        for (Body body : bodyMap.values()) {
            physicsWorld.destroyBody(body);
        }
        bodyMap.clear();

        currentLevel = new LevelSingle(currentLevel.getNumber(), currentLevel.getRating(), currentLevel.isUnlocked());
        setupPhysicsWorld();

        gameStarted = false;
        birdLaunched = false;
        birdStopped = false;
        isDragging = false;

        if (gamePaused) {
            togglePause();
        }
    }

    public void quitToMainMenu() {
        if (gamePaused) {
            togglePause();
        }
        game.setScreen(new LevelsScreen(game));
    }

    public boolean isPaused() {
        return gamePaused;
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

    private void drawTrajectoryPreview() {
        if (isDragging && currentDrag != null) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(0.8f, 0.8f, 0.8f, 0.5f);

            Vector2 dragVector = slingAnchor.cpy().sub(currentDrag);
            float distance = dragVector.len();
            float launchPower = (distance / MAX_DRAG_DISTANCE) * LAUNCH_SPEED_MULTIPLIER;
            Vector2 velocity = dragVector.nor().scl(launchPower);

            float prevX = currentDrag.x;
            float prevY = currentDrag.y;

            for (int i = 1; i <= TRAJECTORY_POINTS; i++) {
                float t = i * TIME_STEP * 2;
                float x = currentDrag.x + velocity.x * t;
                float y = currentDrag.y + velocity.y * t + 0.5f * -9.81f * t * t;

                if (i % 2 == 0) {
                    shapeRenderer.line(prevX, prevY, x, y);
                }

                prevX = x;
                prevY = y;

                if (y < slingY) break;
            }

            shapeRenderer.end();
        }
    }

    @Override
    public void render(float delta) {
        clearBackground();

        if (!gamePaused) {
            doPhysicsStep(delta);
            updateGameObjects();
        }

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
        drawDebugDots();  // Add this line after debug rendering

        mainStage.act(delta);
        mainStage.draw();

        if (gamePaused) {
            pauseScreen.render(delta);
        }
    }

    public void togglePause() {
        gamePaused = !gamePaused;
        if (gamePaused) {
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
        game.gameBatch.draw(birdImg, x, y, size, size / ratio);
    }

    private void drawBlocks() {
        for (Block block : currentLevel.getBlocks()) {
            Texture blockImg = AssetManager.getInstance().getTexture(block.getMaterial() + "_block.png");
            float alpha = 1f - (block.getDestructionProgress() * 0.5f);
            game.gameBatch.setColor(1, 1, 1, alpha);
            game.gameBatch.draw(blockImg, block.getX(), block.getY(), Block.WIDTH, Block.HEIGHT);
        }
        game.gameBatch.setColor(1, 1, 1, 1);
    }

    private void drawPigs() {
        for (Pig pig : currentLevel.getPigs()) {
            Texture pigImg = pig.getTexture();
            game.gameBatch.draw(pigImg, pig.getX(), pig.getY(), PIG_SIZE, PIG_SIZE);
        }
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
