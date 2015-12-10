package fr.diod.searchAdherants.excel;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

public class AdherentScore {
	public Adherent adherant;
	public int score;

	public AdherentScore(Adherent adherant, int score) {
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


		toString.addValue(adherant.portable)
		.addValue(adherant.telephone)
		.addValue(adherant.email);

		return toString.toString();
	}
}
