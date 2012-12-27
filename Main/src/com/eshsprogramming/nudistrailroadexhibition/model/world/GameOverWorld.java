package com.eshsprogramming.nudistrailroadexhibition.model.world;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.eshsprogramming.nudistrailroadexhibition.model.gui.Text;
import com.eshsprogramming.nudistrailroadexhibition.model.entity.BlockEntity;
import com.eshsprogramming.nudistrailroadexhibition.screens.GameOverScreen;


/**
 * @author Benjamin Landers
 */
public class GameOverWorld
{
    /**
     * The game over world. Used for relative sizing of text.
     */
    private GameOverScreen gameOverScreen = null;
    /**
     * The title text.
     */
    Text messageText = null;
    /**
     * The play text.
     */
    Text scoreText = null;
	/**
	 * text to return
	 */
	Text returnText = null;
    /**
     * An array of the blocks which make up the background.
     */
    private Array<BlockEntity> blocks = new Array<BlockEntity>();

    /**
     * Creates a new main menu
     */
    public GameOverWorld(GameOverScreen gameOverScreen, int score)
    {
        this.gameOverScreen = gameOverScreen;

        messageText = new Text("fonts/arial/font.fnt", false, gameOverScreen.getWidth() * 0.0035f,
                new Vector2(1f, 4.5f), "Your score was ...");
        scoreText = new Text("fonts/arial/font.fnt", false, gameOverScreen.getWidth() * 0.0035f, new Vector2(5f, 4.5f),
                "" + score);
		returnText = new Text("fonts/arial/font.fnt", false, gameOverScreen.getWidth() * 0.0035f,
				new Vector2(3f, 2.5f), "Return to MainMenu");

        fillBlockArray();
    }

    /**
     * Fills the blocks array with blocks.
     */
    private void fillBlockArray()
    {
        for(int index1 = 0; index1 < 8; index1++)
        {
            for(int index2 = 0; index2 < 5; index2++)
            {
                blocks.add(new BlockEntity(new Vector2(index1, index2)));
            }
        }
    }

    /**
     * Returns the array of blocks which make up the background.
     *
     * @return The array of blocks which make up the background.
     */
    public Array<BlockEntity> getBlocks()
    {
        return blocks;
    }

    /**
     * Returns the title text object.
     *
     * @return The title text object.
     */
    public Text getMessageText()
    {
        return messageText;
    }

    /**
     * Returns the play text object.
     *
     * @return The play text object.
     */
    public Text getScoreText()
    {
        return scoreText;
    }
	/**
	 *
	 */
	public Text getReturnText()
	{
	   return returnText;
	}
}
