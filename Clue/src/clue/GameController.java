/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clue;

import clue.action.AccuseAction;
import clue.action.Action;
import clue.action.ActionType;
import clue.action.AvoidSuggestionAction;
import clue.action.EndTurnAction;
import clue.action.ExtraTurnAction;
import clue.action.MoveAction;
import clue.action.ShowCardAction;
import clue.action.ShowCardsAction;
import clue.action.StartAction;
import clue.action.StartTurnAction;
import clue.action.SuggestAction;
import clue.action.TeleportAction;
import clue.action.ThrowAgainAction;
import clue.action.UnknownActionException;
import clue.card.AvoidSuggestionIntrigue;
import clue.card.Card;
import clue.card.IntrigueCard;
import clue.card.PersonCard;
import clue.card.RoomCard;
import clue.card.WeaponCard;
import clue.player.Player;
import clue.tile.SpecialTile;
import clue.tile.Tile;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.SynchronousQueue;

/**
 * Keeps track of an instance of a Clue game state.
 *
 * @author slb35
 */
public final class GameController {

    public class MovementException extends Exception {
    }

    private GameState state;
    private List<IntrigueCard> cards;
    private PersonCard person;
    private RoomCard room;
    private WeaponCard weapon;
    private List<Player> players;
    private Player winner;
    private Player player;
    private final boolean working = false;
    private final SynchronousQueue queue;
    private final Random random;
    private List<Action> actionLog;
    private Queue<Action> actions;
    private int turns = 0;

    /**
     * Creates a new GameController.
     *
     * @param players
     * @throws java.lang.InterruptedException
     * @throws clue.action.UnknownActionException
     */
    public GameController(List<Player> players) throws InterruptedException, UnknownActionException {
        queue = new SynchronousQueue(true);
        this.players = players;
        random = new Random(Calendar.getInstance().getTimeInMillis());
        actionLog = new ArrayList();
        performAction(new StartAction());
    }

    /**
     * Takes an Action from a Player and adds it to the queue of Actions.
     *
     * @param action the Action to be performed
     * @throws UnknownActionException Action type could not be resolved
     * @throws InterruptedException Action was not performed at the correct
     * time.
     */
    public void performAction(Action action) throws UnknownActionException, InterruptedException {
        execute((Action) queue.poll());
        queue.offer(action);
    }

    /**
     * Executes an action from the queue. Waits for the current action to
     * complete before executing.
     *
     * @throws UnknownActionException
     * @throws InterruptedException
     */
    private void execute(Action action) throws UnknownActionException, InterruptedException {
        player = players.get(state.getPlayerTurn());
        action.execute();
        System.out.println(action.actionType + "executing");
        //Action specific logic
        switch (action.actionType) {
            default:
                throw new UnknownActionException();
            case DEFAULT:
                throw new UnknownActionException();
            case ACCUSATION:
                if (action.result) {
                    winner = state.endGame();
                    endGame();
                } else if (state.playersNumber == 0) {
                    state.endGame();
                    endGame();
                } else {
                    performAction(new EndTurnAction());
                }
                actionLog.add(turns,action);
                break;
            case AVOIDSUGGESTIONCARD:
                action.getPlayer().setActiveSuggestionBlock(true);//player will not be checked in next turns suggestion check
                //TODO: notify player they have a suggestion block
                returnCard((IntrigueCard) action.getPlayer().removeCard(((AvoidSuggestionAction) action).card));
                break;
            case ENDTURN:
                state.nextTurn(state.nextPlayer());

                int j = player.getId();

                for (int i = 0; i < state.playersNumber; i++) {

                    if (j != player.getId()) {
                        state.getPlayer(j).setActiveSuggestionBlock(false);//remove any suggestion blocks players may have 
                    }

                    j = state.getNextPointer(j);
                }
                moveActionLog();
                turns++;
                performAction(new StartTurnAction(player));
                break;
            case EXTRATURN:
                performAction(new StartTurnAction(action.getPlayer()));
                break;
            case MOVE:
                if (action.result && (state.getAction().actionType == ActionType.STARTTURN || state.getAction().actionType == ActionType.THROWAGAIN)) {
                    Tile loc = ((MoveAction) action).getTile();
                    player.setPosition(loc);
                    if (loc.special) {
                        IntrigueCard card = ((SpecialTile) loc).getIntrigue(player);
                        switch (card.cardType) {
                            case AVOIDSUGGESTION:
                                performAction(new AvoidSuggestionAction(player, (AvoidSuggestionIntrigue) card));
                                break;
                            case EXTRATURN:
                                //TODOplayer
                                performAction(new ExtraTurnAction(player));
                                break;
                            case TELEPORT:
                                //TODO
                                performAction(new TeleportAction(player));
                                break;
                            case THROWAGAIN:
                                //TODO
                                performAction(new ThrowAgainAction(player));
                                break;
                        }
                    }
                }
                break;
            case SHOWCARD:
                if (state.getAction().actionType == ActionType.SHOWCARDS) {
                    //TODO
                }
                break;
            case SHOWCARDS:
                if (state.getAction().actionType == ActionType.SUGGEST) {
                    //TODO
                }
                actionLog.add(turns,action);
                break;
            case START:
                state = new GameState(players);
                break;
            case STARTTURN:
                if (state.getAction().actionType == ActionType.ENDTURN || state.getAction().actionType == ActionType.EXTRATURN) {
                    state.nextTurn(player.getId());
                }
                break;
            case SUGGEST:
                if (action.result && state.getAction().actionType == ActionType.STARTTURN | state.getAction().actionType == ActionType.MOVE) {
                    performAction(new ShowCardsAction(((SuggestAction) action).show, ((SuggestAction) action).player, ((SuggestAction) action).foundCards));
                }
                actionLog.add(turns,action);
                break;
            case THROWAGAIN:
                //TODO: tell gui to roll again
                //TODO: allow players to roll again
                roll();
                break;

        }
        //update game state
        state.setAction(action);
        state.notifyAllObservers();
    }

    /**
     * Returns the last action executed on the state
     *
     * @return Action
     */
    public Action getLastAction() {
        return state.getAction();
    }

    /**
     * Returns the player whose turn it is
     *
     * @return Current player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Terminates the game instance and declares a winner.
     */
    private void endGame() {
        if (winner == null) {
        } else {

        }
    }

    /**
     * rolls the current player's moves
     *
     * @return new movement limit
     */
    public int roll() {
        player.setMoves(random.nextInt(10) + 2);
        return player.getMoves();
    }

    /**
     * Moves the current player
     *
     * @param tiles tiles to move to
     * @throws UnknownActionException
     * @throws InterruptedException
     * @throws clue.GameController.MovementException
     */
    public void move(Queue<Tile> tiles) throws UnknownActionException, InterruptedException, MovementException {
        if (tiles.size() <= player.getMoves()) {
            performAction(new MoveAction(tiles, player));
        } else {
            throw new MovementException();
        }
    }

    /**
     * Creates a new SuggestAction for a player
     *
     * @param person the person card to be suggested
     * @param room the room card to be suggested
     * @param weapon the weapon card to be suggested
     * @param player the suggesting Player
     * @return new SuggestAction
     */
    public void suggest(PersonCard person, RoomCard room, WeaponCard weapon, Player player) throws UnknownActionException, InterruptedException {
        performAction(new SuggestAction(person, room, weapon, player, state));
    }

    /**
     *
     * @param card
     * @throws UnknownActionException
     * @throws InterruptedException
     */
    public void showCard(Card card) throws UnknownActionException, InterruptedException {
        performAction(new ShowCardAction(player, card));
    }

    /**
     *
     * @param person
     * @param room
     * @param weapon
     * @throws UnknownActionException
     * @throws InterruptedException
     */
    public void accuse(PersonCard person, RoomCard room, WeaponCard weapon) throws UnknownActionException, InterruptedException {
        performAction(new AccuseAction(player, person, room, weapon, person == this.person && room == this.room && weapon == this.weapon));
    }

    /**
     *
     * @return
     */
    public IntrigueCard drawCard() {
        int nextCard = random.nextInt(cards.size());
        return cards.remove(nextCard);
    }

    private void moveActionLog() {
        //TODO
        int pointer = player.getLogPointer();
        while (pointer != turns) {
            actions.offer(actionLog.get(pointer));
            pointer++;
        }
        player.setLogPointer(pointer);
    }

    public Queue<Action> getActions() {
        return actions;
    }

    /**
     *
     * @param card
     */
    private void returnCard(IntrigueCard card) {
        cards.add(card);
    }
}
