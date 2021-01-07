package ginrummy.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import eaai.ginrummy.Card;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Tests for the CardCounter class.
 */
class CardCounterTests {

    private static final Random RANDOM = new Random();    

    /**
     * A private class to hold expected values for test cases.
     */
    private class ExpectedValues {
        public final Stack<Card> deck = Card.getShuffle(RANDOM.nextInt());
        public final Stack<Card> discardPile = new Stack<Card>();
        public final Set<Card> unseenCards = new HashSet<Card>(deck);
        public final Set<Card> myHand = getHand(deck);
        public final Set<Card> myRejects = new HashSet<Card>();
        public final Set<Card> myKnownHand = new HashSet<Card>();
        public final Set<Card> opHand = getHand(deck);
        public final Set<Card> opKnownHand = new HashSet<Card>();
        public final Set<Card> opRejects = new HashSet<Card>();
        public final int myNum = (RANDOM.nextInt() % 2);
        public final int opNum = (myNum + 1) % 2;

        ExpectedValues() {
            // remove my hand from the unseen cards
            for (Card c : this.myHand) {
                this.unseenCards.remove(c);
            }
        }
    }

    /**
     * Get a random Gin Rummy hand.
     */
    private static Set<Card> getHand(Stack<Card> deck) {
        Set<Card> hand = new HashSet<Card>();
        for (int i = 0; i < Utils.HAND_SIZE; i++) {
            hand.add(deck.pop());
        }

        return hand;
    }

    /**
     * Compare the state of the specified card counter to the supplied
     * expected values.
     */
    private static void compare(ExpectedValues ev, CardCounter counter) {
        assertEquals(ev.deck.size(), counter.getDrawPileSize(), "Draw Pile: different sizes");
        compare(ev.myHand, counter.getMyHand(), "My Hand");
        compare(ev.myRejects, counter.getMyRejects(), "My Rejects");
        compare(ev.myKnownHand, counter.getMyKnownHand(), "My Known Hand");
        compare(ev.opKnownHand, counter.getOpKnownHand(), "Op Known Hand");
        compare(ev.opRejects, counter.getOpRejects(), "Op Rejects");
        compare(ev.discardPile, counter.getDiscardPile(), "Discard Pile");
        compare(ev.unseenCards, counter.getUnseenCards(), "Unseen Cards");
    }

    /**
     * Compare the two lists to deteremine whether they contain the same
     * cards in the same order.
     */
    private static void compare(List<Card> list1, List<Card> list2, String listName) {
        assertEquals(list1.size(), list2.size(), listName + ": different sizes");
        Iterator<Card> list1Iter = list1.iterator();
        Iterator<Card> list2Iter = list2.iterator();
        while (list1Iter.hasNext() && list2Iter.hasNext()) {
            assertEquals(list1Iter.next(), list2Iter.next(), listName + ": cards don't match");
        }
        assertEquals(list1Iter.hasNext(), list2Iter.hasNext(), listName + ": different sizes");
    }

    /**
     * Compare the two sets to deteremine whether they contain the same
     * cards.
     */
    private static void compare(Set<Card> set1, Set<Card> set2, String setName) {
        assertEquals(set1.size(), set2.size(), setName + ": different sizes");
        Iterator<Card> set1Iter = set1.iterator();
        while (set1Iter.hasNext()) {
            assertTrue(set2.contains(set1Iter.next()), setName + ": card not found");
        }
    }

    /**
     * Execute an end-to-end test of the card counter functionality.
     */
    @Test
    void test() {
        // create counter to be tested
        CardCounter counter = new CardCounter();

        // reset counter to start new hand
        ExpectedValues ev = new ExpectedValues();
        counter.reset(ev.myNum, ev.myHand);
        compare(ev, counter);

        // add first face up card to discard pile
        Card drawnCard = ev.deck.pop();
        ev.discardPile.push(drawnCard);
        ev.unseenCards.remove(drawnCard);
        counter.reportFirstFaceUpCard(drawnCard);
        compare(ev, counter);

        // agent draws a card from draw pile
        drawnCard = ev.deck.pop();
        ev.myHand.add(drawnCard);
        ev.myRejects.add(ev.discardPile.peek());
        ev.unseenCards.remove(drawnCard);
        counter.reportDraw(ev.myNum, drawnCard);
        compare(ev, counter);

        // agent discards a card
        Card discard = ev.myHand.iterator().next();
        ev.myHand.remove(discard);
        ev.myRejects.add(discard);
        ev.discardPile.push(discard);
        counter.reportDiscard(ev.myNum, discard);
        compare(ev, counter);

        // opponent draws a card from draw pile
        drawnCard = ev.deck.pop();
        ev.opHand.add(drawnCard);
        ev.opRejects.add(ev.discardPile.peek());
        counter.reportDraw(ev.opNum, null); // agent doesn't sees opponent's card
        compare(ev, counter);

        // opponent discards a card
        discard = ev.opHand.iterator().next();
        ev.opHand.remove(discard);
        ev.opKnownHand.remove(discard);
        ev.opRejects.add(discard);
        ev.unseenCards.remove(discard);
        ev.discardPile.push(discard);
        counter.reportDiscard(ev.opNum, discard);
        compare(ev, counter);

        // agent draws a card from discard pile
        drawnCard = ev.discardPile.pop();
        ev.myHand.add(drawnCard);
        ev.myKnownHand.add(drawnCard);
        ev.unseenCards.remove(drawnCard);
        counter.reportDraw(ev.myNum, drawnCard);
        compare(ev, counter);

        // agent discards a card
        discard = ev.myHand.iterator().next();
        ev.myHand.remove(discard);
        ev.myKnownHand.remove(discard);
        ev.myRejects.add(discard);
        ev.discardPile.push(discard);
        counter.reportDiscard(ev.myNum, discard);
        compare(ev, counter);

        // opponent draws a card from discard pile
        drawnCard = ev.discardPile.pop();
        ev.opHand.add(drawnCard);
        ev.opKnownHand.add(drawnCard);
        counter.reportDraw(ev.opNum, drawnCard);  // agent sees opponent's card
        compare(ev, counter);

        // opponent discards a card
        discard = ev.opHand.iterator().next();
        ev.opHand.remove(discard);
        ev.opKnownHand.remove(discard);
        ev.opRejects.add(discard);
        ev.discardPile.push(discard);
        ev.unseenCards.remove(discard);
        counter.reportDiscard(ev.opNum, discard);
        compare(ev, counter);

        // reset counter to start new hand
        ev = new ExpectedValues();
        counter.reset(ev.myNum, ev.myHand);
        compare(ev, counter);
    }
}