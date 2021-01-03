package ginrummy.util;

import java.util.ArrayList;

import eaai.ginrummy.Card;
import eaai.ginrummy.GinRummyUtil;

/**
 * This class provides utility functions for implementing Gin Rummy agents.
 * 
 * Much of the base functionality of this class was taken from:
 * https://github.com/cudy789/gin-rummy-SIGRA/blob/master/sigra/agents/util
 */
public class Utils {

    // constants
    public static final int HAND_SIZE = 10;

    /**
     * Return true if the specified hand is "gin" - the melds in
     * the hand include all the cards in the hand.
     */
    static public boolean isGin(ArrayList<Card> hand) {
        ArrayList<ArrayList<ArrayList<Card>>> bestMelds =
            GinRummyUtil.cardsToBestMeldSets(hand);
        if (bestMelds.isEmpty()) {
          return false;
        }
        return numCardsInMelds(bestMelds.get(0)) == Utils.HAND_SIZE;
      }
    
    /**
     * Count the number of cards in the specified melds.
     */
    static public int numCardsInMelds(ArrayList<ArrayList<Card>> melds) {
        return melds.stream()
            .mapToInt(
                (x) -> {
                  return x.size();
                })
            .reduce(
                (a, b) -> {
                  return a + b;
                })
            .orElse(0);
    }

    /**
     * Return one of the possible sets of melds that have minimal deadwood.
     */
    public static ArrayList<ArrayList<Card>> getBestMelds(ArrayList<Card> cards) {
        ArrayList<ArrayList<ArrayList<Card>>> rv = GinRummyUtil.cardsToBestMeldSets(cards);
        if (rv.isEmpty()) {
          return new ArrayList<ArrayList<Card>>();
        }
        return rv.get(0);
    }
}