package io.github.angrybirdsbox2d;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Box2DPhysicsWorld {
    private World physicsWorld;
    private static final float PPM = 10f;
    private static final float TIME_STEP = 1/60f;
    private static final int VELOCITY_ITERATIONS = 6;
    private static final int POSITION_ITERATIONS = 2;

    public Box2DPhysicsWorld() {
        physicsWorld = new World(new Vector2(0, -9.81f/4f), true);
    }

    // Helper method to convert game coordinates to Box2D coordinates
    public static float toBox2D(float pixels) {
        return pixels / PPM;
    }

    // Helper method to convert Box2D coordinates to game coordinates
    public static float toPixels(float box2DUnits) {
        return box2DUnits * PPM;
    }

    public Body createBird(float x, float y, float radius, Bird bird) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(toBox2D(x), toBox2D(y));

        Body body = physicsWorld.createBody(bodyDef);

        CircleShape shape = new CircleShape();
        shape.setRadius(toBox2D(radius));

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.3f;
        fixtureDef.restitution = 0.5f;

        body.createFixture(fixtureDef);
        body.setUserData(bird);
        shape.dispose();

        return body;
    }

    public Body createBlock(float x, float y, float width, float height, Block block) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(toBox2D(x + width/2), toBox2D(y + height/2));

        Body body = physicsWorld.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(toBox2D(width/2), toBox2D(height/2));

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;

        // Set different physical properties based on block material
        if (block instanceof WoodBlock) {
            fixtureDef.density = 1.0f;
            fixtureDef.friction = 0.5f;
            fixtureDef.restitution = 0.2f;
        } else if (block instanceof GlassBlock) {
            fixtureDef.density = 0.8f;
            fixtureDef.friction = 0.3f;
            fixtureDef.restitution = 0.1f;
        } else if (block instanceof SteelBlock) {
            fixtureDef.density = 2.0f;
            fixtureDef.friction = 0.7f;
            fixtureDef.restitution = 0.1f;
        }

        body.createFixture(fixtureDef);
        body.setUserData(block);
        shape.dispose();

        return body;
    }

    public Body createGround() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(toBox2D(0), toBox2D(0));

        Body ground = physicsWorld.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(toBox2D(1000), toBox2D(10)); // Wide ground

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.friction = 0.5f;

        ground.createFixture(fixtureDef);
        shape.dispose();

        return ground;
    }

    public void update() {
        physicsWorld.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
    }

    // Contact listener for collision handling
    public void setupCollisionHandling() {
        physicsWorld.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Body bodyA = contact.getFixtureA().getBody();
                Body bodyB = contact.getFixtureB().getBody();
                Object userDataA = bodyA.getUserData();
                Object userDataB = bodyB.getUserData();

                // Calculate collision force
                float impactForce = calculateImpactForce(bodyA, bodyB);

                // Handle bird collisions
                if (userDataA instanceof Bird && userDataB instanceof Block) {
                    handleBirdBlockCollision((Bird)userDataA, (Block)userDataB, impactForce);
                } else if (userDataB instanceof Bird && userDataA instanceof Block) {
                    handleBirdBlockCollision((Bird)userDataB, (Block)userDataA, impactForce);
                }
            }

            @Override
            public void endContact(Contact contact) {}

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {}

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {}
        });
    }

    private float calculateImpactForce(Body bodyA, Body bodyB) {
        Vector2 velA = bodyA.getLinearVelocity();
        Vector2 velB = bodyB.getLinearVelocity();
        Vector2 relativeVel = velA.sub(velB);
        return relativeVel.len();
    }

    private void handleBirdBlockCollision(Bird bird, Block block, float impactForce) {
        float damageMultiplier = impactForce / 10f;
        int damage = (int)(bird.getDamage() * damageMultiplier);
        block.takeDamage(damage);
    }

    public void dispose() {
        physicsWorld.dispose();
    }
}
