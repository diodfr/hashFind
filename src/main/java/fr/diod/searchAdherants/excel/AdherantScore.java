package fr.diod.searchAdherants.excel;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

public class AdherantScore {
	public Adherant adherant;
	public int score;

	public AdherantScore(Adherant adherant, int score) {
		this.adherant = adherant;
		this.score = score;
	}

	@Override
	public String toString() {
		ToStringHelper toString = Objects.toStringHelper(adherant)
				.addValue(adherant.name)
				.addValue(adherant.maidenName)
				.addValue(adherant.firstName)
				.addValue(adherant.birth);

		if (score >= 75) {
			toString.addValue(adherant.portable)
			.addValue(adherant.telephone)
			.addValue(adherant.email);
		}
		
		return toString.toString();
	}
}
