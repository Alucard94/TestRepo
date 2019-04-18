package pocker;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BertoniPocker implements ExecuteProgram{
	public static Double typeOfHand;	
	
	public static void main(String[] args) {
		new BertoniPocker().run();
	}
		
	public void run() {
		int count1 = 0;	// games win by player1
		int count2 = 0;	// games win by player2
		int equal = 0;	// number of draws		
		Double hand1 = 0.0;
		Double hand2 = 0.0;
		int score1 = 0;
		int score2 = 0;
		String line = ""; // string to store the result
		
		try {
			line = line + "PLAYER1|PLAYER2\n";
			for (String hand : Files.readAllLines(Paths.get("data/pokerdata.txt"))) {
				// Parse cards and divide among players
				String[] cards = hand.split(" ");
				
				if (cards.length != 10)
					throw new AssertionError();
				
				Card[] player1 = new Card[5];
				Card[] player2 = new Card[5];
				
				for (int i = 0; i < 5; i++) {
					player1[i] = new Card(cards[i + 0]);
					player2[i] = new Card(cards[i + 5]);
				}
								
				// Compare hand scores	
				score1 = getScore(player1);
				hand1 = typeOfHand;
				score2 = getScore(player2);
				hand2 = typeOfHand;
				
				if (score1 > score2)
					count1 = count1 + 1;
				if (score1 < score2)
					count2 = count2 + 1;
				if (score1 == score2)
					equal = equal + 1;
								
				line = line + Double.toString(hand1) + "|" + Double.toString(hand2) + "\n";			    
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AssertionError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("p1 wins: " + Integer.toString(count1));
		System.out.println("p2 wins: " + Integer.toString(count2));
		System.out.println("draws: " + Integer.toString(equal));
		
		BufferedWriter writer;
		
		try {
			line = "p1 wins: " + Integer.toString(count1) + "\n" + "p2 wins: " + Integer.toString(count2) + "\n" + 
					"draws: " + Integer.toString(equal) + "\n" + line;
			writer = new BufferedWriter(new FileWriter("data/outputFile.txt"));
			writer.write(line);
		    writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	    
	}
		
	// Returns a score for the given hand.
	// If hand1 beats hand2 then hand1's score is greater than hand2's score.
	// If there is a draw between hand1 and hand2 then the scores for both are the same.
	// The probability of win is based on 100% minus the probability of get a given hand.
	public static int getScore(Card[] hand) {
		if (hand.length != 5)
			throw new IllegalArgumentException();
		
		int[] rankCounts = new int[13];  		// rankCounts[i] is the number of cards with the rank of i
		int flushSuit = hand[0].getSuit();    	// flushSuit is in the range [0,3] if all cards have that suit; otherwise -1
		
		for (Card card : hand) {
			rankCounts[card.getRank()]++;
			if (card.getSuit() != flushSuit)
				flushSuit = -1;
		}
		
		// rankCountHist[i] is the number of times a rank count of i occurs.
		// For example if there are four triplet, then rankCountHist[3] = 4.
		int[] rankCountHist = new int[6];
		for (int count : rankCounts)
			rankCountHist[count]++;
		
		int bestCards = get5FrequentHighestCards(rankCounts, rankCountHist);
		int straightHighRank = getStraightHighRank(rankCounts);
		
		// Encode the hand type in the top bits, then encode up to 5 cards in big endian (4 bits each).
		if (straightHighRank != -1 && flushSuit != -1) {			// Straight flush
			typeOfHand = 100 - HandProbability.StraightFlush;
			return 8 << 20 | straightHighRank;  			
		}
		else if (rankCountHist[4] == 1) {							// Four of a kind
			typeOfHand = 100 - HandProbability.FourOfKind;
			return 7 << 20 | bestCards;         						
		}
		else if (rankCountHist[3] == 1 && rankCountHist[2] == 1) { 	// Full house
			typeOfHand = 100 - HandProbability.Flush;
			return 6 << 20 | bestCards;
		}
		else if (flushSuit != -1) {									// Flush
			typeOfHand = 100 - HandProbability.FullHouse;
			return 5 << 20 | bestCards;         								
		}
		else if (straightHighRank != -1) {							// Straight
			typeOfHand = 100 - HandProbability.Straight;
			return 4 << 20 | straightHighRank;  						
		}
		else if (rankCountHist[3] == 1) {							// Three of a kind
			typeOfHand = 100 - HandProbability.ThreeOfKind;
			return 3 << 20 | bestCards;         						
		}
		else if (rankCountHist[2] == 2) {							// Two pairs
			typeOfHand = 100 - HandProbability.TwoPair;
			return 2 << 20 | bestCards;         						
		}
		else if (rankCountHist[2] == 1) {							// One pair
			typeOfHand = 100 - HandProbability.OnePair;
			return 1 << 20 | bestCards;         						
		}
		else {														// High card
			typeOfHand = 100 - HandProbability.HighCard;
			return 0 << 20 | bestCards;         													
		}
	}
		
	// Encodes 5 card ranks into 20 bits in big endian, starting with the most frequent cards.
	// The ties are breaking by highest rank. The set of {3,3,J,6,J} is encoded as [J,J,3,3,6]
	// Remember pairs come before singles and highest pairs come first.
	private static int get5FrequentHighestCards(int[] ranks, int[] ranksHist) {
		int result = 0;
		int count = 0;
		
		for (int i = ranksHist.length - 1; i >= 0; i--) {
			for (int j = ranks.length - 1; j >= 0; j--) {
				if (ranks[j] == i) {
					for (int k = 0; k < i && count < 5; k++, count++)
						result = result << 4 | j;
				}
			}
		}
		
		if (count != 5)
			throw new IllegalArgumentException();
		return result;
	}
		
	// Returns the rank of the highest card in the straight, or -1 if the set of cards does not form a straight.
	// This takes into account the fact that ace can be rank 0 (i.e. face value 1) or rank 13 (value immediately after king).
	private static int getStraightHighRank(int[] ranks) {
		loop:
		for (int i = ranks.length - 1; i >= 3; i--) {
			for (int j = 0; j < 5; j++) {
				if (ranks[(i - j + 13) % 13] == 0)
					continue loop;  // Current offset is not a straight
			}
			return i;  // Straight found
		}
		return -1;
	}			

}
