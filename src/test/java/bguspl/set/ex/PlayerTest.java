package bguspl.set.ex;

import bguspl.set.Config;
import bguspl.set.Env;
import bguspl.set.UserInterface;
import bguspl.set.Util;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;

import java.util.Properties;

@ExtendWith(MockitoExtension.class)
class PlayerTest {

    Player player;
    @Mock
    Util util;
    @Mock
    private UserInterface ui;
    @Mock
    private Table table;
    @Mock
    private Dealer dealer;
    @Mock
    private Logger logger;

    Integer[] slotToCard;
    Integer[] cardToSlot;

    void assertInvariants() {
        assertTrue(player.id >= 0);
        assertTrue(player.score() >= 0);
    }

    @BeforeEach
    void setUp() {
        // // purposely do not find the configuration files (use defaults here).
        // slotToCard = new Integer[12];
        // cardToSlot = new Integer[999];
        // Env env = new Env(logger, new Config(logger, """), ui, util);
        // Table table = new Table(env, slotToCard, cardToSlot);
        // player = new Player(env, dealer, table, 0, false);
        // assertInvariants();
        Properties properties = new Properties();
        properties.put("Rows", "3");
        properties.put("Columns", "4");
        properties.put("FeatureSize", "3");
        properties.put("FeatureCount", "4");
        properties.put("TableDelaySeconds", "0");
        properties.put("PlayerKeys1", "81,87,69,82");
        properties.put("PlayerKeys2", "85,73,79,80");
        Config config = new Config(logger, properties);
        slotToCard = new Integer[config.tableSize];
        cardToSlot = new Integer[config.deckSize];

        Env env = new Env(logger, config, ui, util);
        table = new Table(env, slotToCard, cardToSlot);
        player = new Player(env, dealer, table, 0, false);
        assertInvariants();
    }

    @AfterEach
    void tearDown() {
        assertInvariants();
    }

    @Test
    void point() {

        // // force table.countCards to return 3
        // when(table.countCards()).thenReturn(3); // this part is just for demonstration

        // calculate the expected score for later
        int expectedScore = player.score() + 1;

        // Make it so player knows it should get a point
        player.shouldGetAPoint = true;

        // call the method we are testing
        player.point();

        // check that the score was increased correctly
        assertEquals(expectedScore, player.score());

        // check that ui.setScore was called with the player's id and the correct score
        verify(ui).setScore(eq(player.id), eq(expectedScore));
    }

    // Placing some cards ob the table
    private void placingSomeCardsOnTable(){
        // for (int i = 0; i < slotToCard.length; ++i) {
        //     table.slotToCard[i] = i;
        //     table.cardToSlot[i] = i;
        // }

        table.placeCard(9, 0);
        table.placeCard(8, 1);
        table.placeCard(7, 2);
        table.placeCard(6, 3);
        table.placeCard(5, 4);

        
        
    } 

    // Tests if the gard array we get from the player contains the same cards thats have the players tokens
    @Test
    void getCardArrayTest(){

        placingSomeCardsOnTable();

        // Make it so the dealer doesn't stop the player from placing tokens; 
        dealer.removingCards = false;

        // The Player placing tokens on 3 slots
        player.keyPressed(0);
        player.keyPressed(2);
        player.keyPressed(4);
        
        // Check if the players actions size is the right size after 3 key presses
        assertEquals(3, player.getCardArray().length);

        int[] expectedPlayersActions = {0, 2, 4};

        // The players array
        int[] playerArr = player.getCardArray(); 

        // Checks if the players cards are the same as the cards the players tokens are on
        for (int i = 0; i < playerArr.length; ++i){
            assertEquals(slotToCard[expectedPlayersActions[i]],playerArr[i]);
        }
    }

    // Tests when the player presses a difrent key after he pressed 3 keys (makes sure he doest plce another token)
    @Test
    void diffrentKeyPressedTest(){

        placingSomeCardsOnTable(); 
        
        // Make it so the dealer doesn't stop the player from placing tokens; 
        dealer.removingCards = false;

         // The Player placing tokens on 3 slots
         player.keyPressed(1);
         player.keyPressed(3);
         player.keyPressed(2);

         // Checks if the slots we pressed are in actions
         assertEquals(true,  player.actionsContainsSlot(1));
         assertEquals(true,  player.actionsContainsSlot(3));
         assertEquals(true,  player.actionsContainsSlot(2));

        // The Player placing tokens on 1 slot
        player.keyPressed(0);

        // Checks if the players actions contain the next slot pressed after he already placed 3 tokens
        assertEquals(false,  player.actionsContainsSlot(0));
    }

    // Tests when the same key is pressed a second time if it removes the slot from actions
    @Test
    void sameKeyPressedTest(){

        placingSomeCardsOnTable(); 
        
        // Make it so the dealer doesn't stop the player from placing tokens; 
        dealer.removingCards = false;

         // The Player placing tokens on 3 slots
         player.keyPressed(1);
         player.keyPressed(3);
         player.keyPressed(2);

         // Checks if the slots we pressed are in actions
         assertEquals(true,  player.actionsContainsSlot(1));
         assertEquals(true,  player.actionsContainsSlot(3));
         assertEquals(true,  player.actionsContainsSlot(2));

        // The Player pressig one of the slots again
        player.keyPressed(3);

        // Checks if the players actions contain the same slot pressed after he already placed 3 tokens
        assertEquals(true,  player.actionsContainsSlot(1));
         assertEquals(false,  player.actionsContainsSlot(3));
         assertEquals(true,  player.actionsContainsSlot(2));
    }
}