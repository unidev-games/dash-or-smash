package com.eshsprogramming.nudistrailroadexhibition.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.eshsprogramming.nudistrailroadexhibition.model.entity.Block;
import com.eshsprogramming.nudistrailroadexhibition.model.world.MainMenuWorld;

/**
 * Renders the main menu.
 *
 * @author Zachary Latta
 */
public class MainMenuRenderer
{
    /**
     * The width of the world in relative units.
     */
    public static final float CAMERA_WIDTH = 8f;
    /**
     * The height of the world in relative units.
     */
    public static final float CAMERA_HEIGHT = 5f;

    /**
     * The main menu instance.
     */
    private MainMenuWorld mainMenuWorld;

    /**
     * The sprite batch. Used for rendering sprites.
     */
    private SpriteBatch spriteBatch;

    /**
     * The texture for the blocks.
     */
    private Texture blockTexture;

    /**
     * The width of the world in pixels.
     */
    private int width;
    /**
     * The height of the world in pixels.
     */
    private int height;
    /**
     * Pixels per unit on the X axis
     */
    private float ppuX;
    /**
     * Pixels per unit on the Y axis
     */
    private float ppuY;

    /**
     * Creates a new MainMenuRenderer
     */
    public MainMenuRenderer(MainMenuWorld mainMenuWorld)
    {
        this.mainMenuWorld = mainMenuWorld;
        this.spriteBatch = new SpriteBatch();

        this.loadTextures();
    }

    /**
     * Renders the contents of main menu renderer.
     *
     * @param delta The time in milliseconds between frames.
     */
    public void render(float delta)
    {
        spriteBatch.begin();
        drawBlocks();
        drawText();
        spriteBatch.end();
    }

    /**
     * Draws blocks on the world.
     */
    private void drawBlocks()
    {
        for(Block block : mainMenuWorld.getBlocks())
        {
            spriteBatch.draw(blockTexture, block.getPosition().x * ppuX, block.getPosition().y * ppuY,
                    Block.SIZE * ppuX, Block.SIZE * ppuY);
        }
    }

    /**
     * Draws the text onto the world.
     */
    private void drawText()
    {
        mainMenuWorld.getTitleText().render(spriteBatch, ppuX, ppuY);
        mainMenuWorld.getPlayText().render(spriteBatch, ppuX, ppuY);
    }

    /**
     * Sets the size of the window in pixels.
     *
     * @param width  The width of the window in pixels.
     * @param height The height of the window in pixels.
     */
    public void setSize(int width, int height)
    {
        this.width = width;
        this.height = height;
        ppuX = (float) width / CAMERA_WIDTH;
        ppuY = (float) height / CAMERA_HEIGHT;
    }

    /**
     * Loads the textures from files.
     */
    private void loadTextures()
    {
        blockTexture = new Texture(Gdx.files.internal("images/block.png"));
    }
}
