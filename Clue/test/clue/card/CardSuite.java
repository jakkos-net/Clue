/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clue.card;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author steve
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({CardTest.class, TeleportIntrigueTest.class, ExtraTurnIntrigueTest.class, ThrowAgainIntrigueTest.class, AvoidSuggestionIntrigueTest.class, CardTypeTest.class, RoomCardTest.class, WeaponCardTest.class, IntrigueCardTest.class, PersonCardTest.class})
public class CardSuite {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    
}
