package com.eshsprogramming.dash_or_smash.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.eshsprogramming.dash_or_smash.DashOrSmash;
import com.eshsprogramming.dash_or_smash.model.entity.Entity;
import com.eshsprogramming.dash_or_smash.model.entity.pedestrian.PedestrianEntity;
import com.eshsprogramming.dash_or_smash.model.entity.pedestrian.baddy.BaddyPedestrianEntity;
import com.eshsprogramming.dash_or_smash.model.entity.pedestrian.baddy.BurglarBaddyPedestrianEntity;
import com.eshsprogramming.dash_or_smash.model.entity.vehicle.VehicleEntity;
import com.eshsprogramming.dash_or_smash.model.gui.Score;
import com.eshsprogramming.dash_or_smash.model.world.GameWorld;
import com.eshsprogramming.dash_or_smash.model.world.HighScoreWorld;
import com.eshsprogramming.dash_or_smash.processor.BaddyController;
import com.eshsprogramming.dash_or_smash.processor.PedestrianController;
import com.eshsprogramming.dash_or_smash.view.GameRenderer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

/**
 * The controller for the gameWorld. Manages sprite properties.
 *
 * @author Zachary Latta, Benjamin Landers
 */
public class GameController extends Controller
{
    /**
     * manages baddies
     */
    private BaddyController baddyController = null;
    /**
     * manages touches for the user
     */
    private PedestrianController touchManager = null;
    /**
     * Used to count how long since the player hit a train
     */
    private float respawnCounter = 0;
    /**
     * Timer to keep track of total delta time.
     */
    private float timer = 0;
    /**
     * Used to tell if player selected a pedestrian
     */
    private boolean selected = false;
    /**
     * The gameWorld.
     */
    private GameWorld gameWorld = null;
    /**
     * The pedestrianEntities in the gameWorld.
     */
    private Array<PedestrianEntity> pedestrianEntities = null;
    /**
     * The baddyEntities in the gameWorld.
     */
    private Array<BaddyPedestrianEntity> baddyPedestrianEntities = null;
    /**
     * The vehicleEntities in the gameWorld.
     */
    private Array<VehicleEntity> vehicleEntities = null;
    /**
     * The hurt sound. Played when a pedestrian dies.
     */
    private Sound hurtSound = null;
    /**
     * The powerup sound. Played when a pedestrian is spawned.
     */
    private Sound powerupSound = null;
    /**
     * The baddy death sound. Played when a baddy dies.
     */
    private Sound baddyDeathSound = null;

    /**
     * The Score object
     */
    private Score score = null;

    /**
     * Creates a new gameWorld controller.
     *
     * @param gameWorld The gameWorld to be used in the gameWorld controller.
     * @param game      A reference to the game.
     */
    public GameController(GameWorld gameWorld, DashOrSmash game)
    {
        super(game);
        this.gameWorld = gameWorld;
        this.pedestrianEntities = gameWorld.getPedestrianEntities();
        this.baddyPedestrianEntities = gameWorld.getBaddyPedestrianEntities();
        this.vehicleEntities = gameWorld.getVehicleEntities();
        this.score = gameWorld.getScore();
        this.hurtSound = Gdx.audio.newSound(Gdx.files.internal("sounds/effects/hurt.wav"));
        this.powerupSound = Gdx.audio.newSound(Gdx.files.internal("sounds/effects/powerup.wav"));
        this.baddyDeathSound = Gdx.audio.newSound(Gdx.files.internal("sounds/effects/baddy_death.wav"));
        this.touchManager = new PedestrianController(game, 3);
        this.baddyController = new BaddyController(game, 3);
        setTouchPosition(gameWorld.getPedestrianEntities().first().getPosition());
    }

    /**
     * The main update method.
     */
    public void update(float delta)
    {
        processInput();
        respawnCounter += delta;
        timer += delta;

        // Calls update methods of pedestrianEntities
        for(PedestrianEntity pedestrianEntity : pedestrianEntities)
        {
            pedestrianEntity.update(delta);
        }

        // Calls update methods of baddyPedestrianEntities
        for(BaddyPedestrianEntity baddyPedestrianEntity : baddyPedestrianEntities)
        {
            baddyPedestrianEntity.update(delta);
        }

        // Calls update methods of vehicleEntities
        for(VehicleEntity vehicleEntity : vehicleEntities)
        {
            vehicleEntity.update(delta);
        }

        handleCollision();
        checkTrainValidity();

        if(respawnCounter > 7)
        {
            boolean isLegit = true;

            for(VehicleEntity vehicle : vehicleEntities)
            {
                isLegit &= !(vehicle.getPosition().x + VehicleEntity.SIZEX > pedestrianEntities.first().getPosition().x + PedestrianEntity.SIZEX &&
                             vehicle.getPosition().x < pedestrianEntities.first().getPosition().x + 2 * PedestrianEntity.SIZEX);
            }

            if(isLegit)
            {
                powerupSound.play();
                respawnCounter -= 7;
                spawnPedestrian();
            }
        }

        updateBaddies(delta);
    }

    /**
     * Changes the pedestrianEntities' state and position based on input controls
     */
    private void processInput()
    {
        touchManager.updatePositions();
        touchManager.updatePedestrians(pedestrianEntities);
        baddyController.updatePositions();
        baddyController.updateBaddies(baddyPedestrianEntities, pedestrianEntities);
    }

    /**
     * Checks if vehicleEntities are allowed to be on the gameWorld. Removes them if they aren't and creates a new one.
     */
    private void checkTrainValidity()
    {
        for(VehicleEntity vehicleEntity : vehicleEntities)
        {
            if(vehicleEntity.getPosition().y + VehicleEntity.SIZEY < 0)
            {
                vehicleEntities.removeValue(vehicleEntity, true);
                score.increment(pedestrianEntities.size);
            }
        }

        VehicleEntity temp;

        while(vehicleEntities.size < VehicleEntity.NUMBER_OF_TRAINS)
        {
            temp = new VehicleEntity(new Vector2(MathUtils.random(0f, GameRenderer.CAMERA_WIDTH - VehicleEntity.SIZEX),
                                                 MathUtils.random(5f, 10f)), MathUtils.random(-2.2f) * timer * .01f - .8f, MathUtils.random(2));

            while(checkVehicle(temp))
            {
                temp.getPosition().x = MathUtils.random(0f, GameRenderer.CAMERA_WIDTH - VehicleEntity.SIZEX);
            }

            vehicleEntities.add(temp);
        }
    }

    /**
     * Handles collision between vehicleEntities and pedestrianEntities.
     */
    private void handleCollision()
    {
        // Collision between vehicles and pedestrians
        for(int index1 = 0; index1 < vehicleEntities.size; index1++)
        {
            for(int index2 = 0; index2 < pedestrianEntities.size; index2++)
            {
                if(checkCollision(vehicleEntities.get(index1), pedestrianEntities.get(index2)))
                {
                    hurtSound.play();
                    pedestrianEntities.removeIndex(index2);
                    respawnCounter = 0;
                    score.pedestrianDeath();

                    if(index2 == 0)
                    {
                        selected = false;
                    }

                    if(pedestrianEntities.size == 0)
                    {
                        if(updateHighScore(this.gameWorld.getScore().getScore()))
                        {
                            getGame().newHighScoreScreen.setScore(this.gameWorld.getScore().getScore());
                            getGame().setScreen(getGame().newHighScoreScreen);
                        }

                        else
                        {
                            getGame().gameOverScreen.setScore(this.gameWorld.getScore().getScore());
                            getGame().setScreen(getGame().gameOverScreen);
                        }
                    }
                }
            }
        }

        // Collision between vehicles and baddies
        for(int index1 = 0; index1 < vehicleEntities.size; index1++)
        {
            for(int index2 = 0; index2 < baddyPedestrianEntities.size; index2++)
            {
                if(checkCollision(vehicleEntities.get(index1), baddyPedestrianEntities.get(index2)))
                {
                    baddyDeathSound.play();
                    score.baddyDeath(baddyPedestrianEntities.get(index2).POINTS_ON_DEATH);
                    baddyPedestrianEntities.removeIndex(index2);
                }
            }
        }
    }

    /**
     * Checks for collision between two entities.
     *
     * @param entity1 The first entity used for the collision check.
     * @param entity2 The second entity used for the collision check.
     * @return Whether or not there is a collision between the entity1 and pedestrianEntity.
     */
    private boolean checkCollision(Entity entity1, Entity entity2)
    {
        return Intersector.intersectRectangles(entity1.getBounds(), entity2.getBounds());
    }

    /**
     * Sets whether or not the user has a selected pedestrian.
     *
     * @param isSelected Whether or not the user has selected a pedestrian.
     */
    public void setSelected(boolean isSelected)
    {
        selected = isSelected;
    }

    /**
     * Spawns a new pedestrian into the gameWorld.
     */
    private void spawnPedestrian()
    {
        PedestrianEntity temp = new PedestrianEntity(new Vector2(pedestrianEntities.first().getPosition().x +
                PedestrianEntity.SIZEX, pedestrianEntities.first().getPosition().y), MathUtils.random(2));

        if(temp.getPosition().x < 0 || temp.getPosition().x > GameRenderer.CAMERA_WIDTH - PedestrianEntity.SIZEX)
        {
            temp.getPosition().x = (temp.getPosition().x > 0) ? temp.getPosition().x : 0;
            temp.getPosition().x = (temp.getPosition().x < GameRenderer.CAMERA_WIDTH - PedestrianEntity.SIZEX)
                                   ? temp.getPosition().x : GameRenderer.CAMERA_WIDTH - 2 * PedestrianEntity.SIZEX;
        }

        pedestrianEntities.add(temp);
    }

    /**
     * Creates a new random number and checks if it matches spawn requirements of the different baddies. If it does,
     * then a new baddy is added to the baddyPedestrianEntities array.
     */
    private void updateBaddies(float delta)
    {
        float randomNumber = MathUtils.random(10000);

        if(randomNumber < BurglarBaddyPedestrianEntity.SPAWN_CHANCE)
        {
            BurglarBaddyPedestrianEntity temp = new BurglarBaddyPedestrianEntity(new Vector2(MathUtils.random(8f), 0), score);
            boolean isNotLegit = true;

            while(isNotLegit)
            {
                temp.getPosition().x =  MathUtils.random(8f);
                isNotLegit = true;

                for(VehicleEntity vehicle : vehicleEntities)
                {
                    isNotLegit &= !(vehicle.getPosition().x + VehicleEntity.SIZEX > temp.getPosition().x + BaddyPedestrianEntity.SIZEX &&
                                    vehicle.getPosition().x > temp.getPosition().x);
                }
            }

            temp.update(delta);
            baddyPedestrianEntities.add(temp);
        }
    }

    /**
     * Checks whether the vehicle is on another
     *
     * @param vehicle The vehicle in question
     * @return Whether or not the vehicle fails the check or not
     */
    private boolean checkVehicle(VehicleEntity vehicle)
    {
        for(VehicleEntity temp : vehicleEntities)
        {
            if(vehicle != temp)
            {
                if(vehicle.getPosition().x > temp.getPosition().x - VehicleEntity.SIZEX && vehicle.getPosition().x <
                        temp.getPosition().x + VehicleEntity.SIZEX)
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks if the user's score is a new high score and writes it to the file if it is.
     *
     * @return Whether or not the user's score is a high score.
     */
    private boolean updateHighScore(int score)
    {
        Array<Integer> highScoreValues = new Array<Integer>();
        FileHandle file = Gdx.files.local("save.dat");

        if(file.exists())
        {
            BufferedReader bufferedReader = file.reader(100);
            String line;

            try
            {
                while((line = bufferedReader.readLine()) != null)
                {
                    highScoreValues.add(Integer.parseInt(line));
                }
            }

            catch(IOException e)
            {
                System.out.println("Error reading save.dat!");
                e.printStackTrace();
            }
        }

        else
        {
            // Creates a new file
            file.write(false);
        }

        for(int index = 0; index < HighScoreWorld.HIGH_SCORE_COUNT; index++)
        {
            try
            {
                if(score > highScoreValues.get(index))
                {
                    Writer writer = file.writer(false);
                    highScoreValues.insert(index, score);

                    try
                    {
                        for(int index2 = 0; index2 < highScoreValues.size; index2++)
                        {
                            writer.write(highScoreValues.get(index2) + "\n");
                        }

                        writer.close();
                    }

                    catch(IOException e)
                    {
                        e.printStackTrace();
                    }

                    return true;
                }
            }

            catch(IndexOutOfBoundsException e)
            {
                Writer writer = file.writer(false);
                highScoreValues.insert(index, score);

                try
                {
                    for(int index2 = 0; index2 < highScoreValues.size; index2++)
                    {
                        writer.write(highScoreValues.get(index2) + "\n");
                    }

                    writer.close();
                }

                catch(IOException e2)
                {
                    e2.printStackTrace();
                }

                return true;
            }
        }

        return false;
    }
}
