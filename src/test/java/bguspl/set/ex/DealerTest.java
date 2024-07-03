package bguspl.set.ex;

import bguspl.set.Config;
import bguspl.set.Env;
import bguspl.set.UserInterface;
import bguspl.set.Util;
// import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertTrue;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;

import java.util.Properties;

@ExtendWith(MockitoExtension.class)
class DealerTest {

    Dealer dealer;
    @Mock
    Util util;
    @Mock
    private UserInterface ui;
    @Mock
    private Table table;
    // @Mock
    // private Player[] players;
    @Mock
    private Logger logger;
    @Mock
    private Player player0;
    @Mock
    private Player player1;

    Player[] players = new Player[2];
    // Player player0;
    // Player player1;
    Integer[] slotToCard;
    Integer[] cardToSlot;

    // void assertInvariants() {
    //     assertTrue(player0.id >= 0);
    //     assertTrue(player0.score() >= 0);
    //     assertTrue(player1.id >= 0);
    //     assertTrue(player1.score() >= 0);
    // }

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
        player0 = new Player(env, dealer, table, 0, false);
        player1 = new Player(env, dealer, table, 1, false);
        players[0] = player0;
        players[1] = player1;
        dealer = new Dealer(env, table, players);
        // assertInvariants();
    }

    // @AfterEach
    // void tearDown() {
    //     assertInvariants();
    // }

    // Tests if the gard array we get from the player contains the same cards thats have the players tokens
    @Test
    void addPlayersWithSetTest(){
        assertEquals(false, dealer.isInPlayersWithSets(player0));
        assertEquals(false, dealer.isInPlayersWithSets(player1));
        
        dealer.addPlayersWithSet(player0);
        assertEquals(true, dealer.isInPlayersWithSets(player0));
        assertEquals(false, dealer.isInPlayersWithSets(player1));

        dealer.addPlayersWithSet(player1);
        assertEquals(true, dealer.isInPlayersWithSets(player0));
        assertEquals(true, dealer.isInPlayersWithSets(player1));
    }

    // Tests if the dealer places cards and removes cards successfully
    @Test
    void placeAndRemoveAllCardsOnTableTest(){
        
        dealer.placeCardsTest();
        for (int i = 0; i < slotToCard.length; ++i){
            assertEquals(true, slotToCard[i] != null);
        }
        
        dealer.removeAllCardsTest();
        for (int i = 0; i < slotToCard.length; ++i){
            assertEquals(null, slotToCard[i]);
        }
    }
}