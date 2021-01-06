package ginrummy.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import eaai.ginrummy.Card;

/**
 * Provides functionality to count cards in a game of Gin Rummy to provide
 * information that an agent can use to make decisons during the game.
 */
public class CardCounter {

    // private instance variables for tracking this agent's state
    private int myNumber;            // this agent's player number
    private Set<Card> myHand;        // this agent's current hand
    private Set<Card> myRejects;     // cards this agent has discarded or passed over
    
    // private instance variables for tracking opponent state
    private int opNumber;            // opponent's player number
    private Set<Card> opKnownHand;   // cards known to be in opponents hand
    private Set<Card> opRejects;     // cards the opponent has discarded or passed over
    
    // instance variable for tracking discard and draw piles
    private Stack<Card> discardPile;
    private int drawPileSize;        // number of cards remaining in draw pile
    private Set<Card> unseenCards;   // cards in draw pile and unknown cards in opponent's hand

    /**
     * Reset the state of this card counter to prepare for a new game.
     */
    public void reset(int playerNum, Set<Card> hand) {
        // initialize state for this agent
        this.myNumber = playerNum;
        this.myHand = new HashSet<Card>(hand);
        this.myRejects = new HashSet<Card>();

        // initialize state for opponent
        this.opNumber = (playerNum + 1) % 2;
        this.opKnownHand = new HashSet<Card>();
        this.opRejects = new HashSet<Card>();

        // initialize piles
        this.discardPile = new Stack<Card>();
        this.drawPileSize = Card.NUM_CARDS - 2*Utils.HAND_SIZE;
        this.unseenCards = new HashSet<Card>();
        for (int i = 0; i < Card.NUM_CARDS; i++) {
            if (!this.myHand.contains(Card.allCards[i])) {
                this.unseenCards.add(Card.allCards[i]);
            }
        }
    }

    /**
     * Record the first card that is added to the discard pile after the
     * agents have been delt their hands.
     */
    public void reportFirstFaceUpCard(Card faceUpCard) {
        if (this.discardPile.isEmpty()) {
            // this card was drawn from the draw pile
            // decrement draw card pile size
            this.drawPileSize--;

            // push card onto discard pile
            this.discardPile.push(faceUpCard);

            // remove card from unseen cards
            this.unseenCards.remove(faceUpCard);
        }
        else {
            throw new java.lang.UnsupportedOperationException("This isn't the first face up card: " + faceUpCard);
        }
    }

    /**
     * Record the fact that the specified card was drawn by the specified
     * player.
     */
    public void reportDraw(int playerNum, Card drawnCard) {
        // update discard pile, draw pile size and rejects
        this.updatePilesAndRejects(playerNum, drawnCard);
        // update "active" agent hand
        if (this.isMe(playerNum)) {
            this.myHand.add(drawnCard);
        }
        else if (drawnCard != null) { // opponent
            this.opKnownHand.add(drawnCard);
        }
    }

    /**
     * Record the fact that the specified card was discarded by the specified
     * player.
     */
    public void reportDiscard(int playerNum, Card discardedCard) {
        // push card onto discard pile
        this.discardPile.push(discardedCard);
        // update rejects and hands
        if (this.isMe(playerNum)) { // this agent discarded
            this.myRejects.add(discardedCard);
            this.myHand.remove(discardedCard);
        }
        else { // opponent discarded
            this.opRejects.add(discardedCard);
            this.opKnownHand.remove(discardedCard);
            this.unseenCards.remove(discardedCard);
        }
    }

    // accessors
    /**
     * Get an unmodifiable view of my hand.
     */
    public Set<Card> getMyHand() {
        return Collections.unmodifiableSet(this.myHand);
    }

    /**
     * Get an unmodifiable view of my opponent's known hand.
     */
    public Set<Card> getOpKnownHand() {
        return Collections.unmodifiableSet(this.opKnownHand);
    }

    /**
     * Get an unmodifiable view of my rejects.
     */
    public Set<Card> getMyRejects() {
        return Collections.unmodifiableSet(this.myRejects);
    }

    /**
     * Get an unmodifiable view of my opponent's rejects.
     */
    public Set<Card> getOpRejects() {
        return Collections.unmodifiableSet(this.opRejects);
    }

    /**
     * Get an unmodifiable view of the discard pile.
     */
    public List<Card> getDiscardPile() {
        return Collections.unmodifiableList(this.discardPile);
    }

    /**
     * Get the number of cards remaining in the draw pile.
     */
    public int getDrawPileSize() {
        return this.drawPileSize;
    }

    /**
     * Return a display string for the card counter.
     */
    public String toString() {
        return "<" + getClass().getName() + "\n" +
        "  myHand:       " + this.myHand + "\n" +
        "  myRejects:    " + this.myRejects + "\n" +
        "  opKnownHand:  " + this.opKnownHand + "\n" +
        "  opRejects:    " + this.opRejects + "\n" +
        "  discardPile:  " + this.discardPile + "\n" +
        "  drawPileSize: " + this.drawPileSize + "\n>";
    }

    /**
     * Get an unmodifiable view of the unseen cards.
     */
    public Set<Card> getUnseenCards() {
        return Collections.unmodifiableSet(this.unseenCards);
    }

    // private utility methods
    private boolean isMe(int playerNum) {
        return (this.myNumber == playerNum);
    }

    private boolean isFaceUpCard(Card card) {
        return (!this.discardPile.isEmpty() &&
                (card != null) && 
                card.equals(this.discardPile.peek()));
    }

    /**
     * If the drawn card is the face up card then update the discard pile.  Otherwise,
     * update the set of rejected cards for the appropriate agent, the number
     * of cards remaining in the draw pile and the unseen cards.
     */
    private void updatePilesAndRejects(int playerNum, Card drawnCard) {
        if (drawnCard != null) {
            // since the agent has seen the drawn card, remove it from the unseen cards
            this.unseenCards.remove(drawnCard);
        }
        if (this.isFaceUpCard(drawnCard)) {
            // card was drawn from face up discard pile
             this.discardPile.pop();
        }
        else {
            // card was drawn from face down draw pile

            // decrement draw card pile size
            this.drawPileSize--;

            // update rejects
            if (!this.discardPile.isEmpty()) {
                if (this.isMe(playerNum)) { // this agent rejected the face up card
                    this.myRejects.add(this.discardPile.peek());
                }
                else { // opponent rejected the face up card
                    this.opRejects.add(this.discardPile.peek());
                }
            }
        }
    }
}