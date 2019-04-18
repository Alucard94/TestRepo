package pocker;

public class Card {
	
	private final int rank;
	private final int suit;
		
	public Card(int rank, int suit) {
		if (rank < 0 || rank >= 13 || suit < 0 || suit >= 4)
			throw new IllegalArgumentException();
		this.rank = rank;
		this.suit = suit;
	}
		
	public Card(String str) {
		this("23456789TJQKA".indexOf(str.charAt(0)), "SHCD".indexOf(str.charAt(1)));
	}
		
	public boolean equals(Object obj) {
		if (!(obj instanceof Card))
			return false;
		Card other = (Card)obj;
		return rank == other.rank && suit == other.suit;
	}
		
	public int hashCode() {
		return rank * 4 + suit;
	}

	public int getRank() {
		return rank;
	}

	public int getSuit() {
		return suit;
	}	
}
