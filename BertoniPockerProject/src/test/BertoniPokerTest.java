package test;

import static org.junit.Assert.*;

import org.junit.Test;

import pocker.BertoniPocker;
import pocker.Card;

public class BertoniPokerTest {

	@Test
	public void test() {
		Double hand1 = 0.0;
		Double hand2 = 0.0;
		int score1 = 0;
		int score2 = 0;
		
		String[] hands = {"5C AD 5D AC 9C 7C 5H 8D TD KS", "TH 8H 5C QS TC 9H 4D JC KS JS", "JC 6S 5H 2H 2D KD 9D 7C AS JS"};
				
		for(String hand: hands) {
			String[] cards = hand.split(" ");
			Card[] player1 = new Card[5];
			Card[] player2 = new Card[5];
		
			for (int i = 0; i < 5; i++) {
				player1[i] = new Card(cards[i + 0]);
				player2[i] = new Card(cards[i + 5]);
			}
			
			score1 = BertoniPocker.getScore(player1);
			hand1 = BertoniPocker.typeOfHand;
			score2 = BertoniPocker.getScore(player2);
			hand2 = BertoniPocker.typeOfHand;
			
			if (score1 > score2)
				System.out.println("player 1 wins");
			if (score1 < score2)
				System.out.println("player 2 wins");
			if (score1 == score2)
				System.out.println("draw");
			
			System.out.println("player 1 probability to win: " + Double.toString(hand1));
			System.out.println("player 2 probability to win: " + Double.toString(hand2));
		}
	}

}
