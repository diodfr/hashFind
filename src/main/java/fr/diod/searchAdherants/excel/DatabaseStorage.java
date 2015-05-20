package fr.diod.searchAdherants.excel;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.language.bm.BeiderMorseEncoder;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

import fr.diod.searchAdherants.hashFind.HashName;

/**
 * 
 * @author broca
 *
 */
public class DatabaseStorage {
	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseStorage.class);

	/**
	 * Liste des adhérants
	 */
	private List<Adherant> adherants = new ArrayList<Adherant>();
	/**
	 * correspondance entre les noms et les adhrérants
	 */
	private MultiMap nameMap = new MultiValueMap();

	public DatabaseStorage() {
	}

	/** Rempli la base à partir d'un nom de fichier et d'un numéro de feuille.
	 * "NOM", "Nom Jeune Fille", "Prenom", "Date Naissance" sont utilisés pour les noms de colonne.
	 * @param fileName
	 * @param sheetNumber
	 */
	public void populate(String fileName, int sheetNumber) {
		populate(fileName, sheetNumber, "NOM", "Nom Jeune Fille", "Prenom", "Date Naissance", "Portable", "Téléphone", "Email");
	}
	
	/**
	 * Rempli la base à partir d'un {@link File} et d'un numéro de feuille.
	 * "NOM", "Nom Jeune Fille", "Prenom", "Date Naissance" sont utilisés pour les noms de colonne.
	 * @param file
	 * @param sheetNumber
	 */
	public void populate(File file, int sheetNumber) {
		populate(file, sheetNumber, "NOM", "Nom Jeune Fille", "Prenom", "Date Naissance", "Portable", "Téléphone", "Email");
	}
	
	/**
	 * Rempli la base des Adhérants à partir d'un {@link File}.
	 * @param file
	 * @param sheetNumber
	 * @param name label colonne nom de famille
	 * @param maidenName label colonne nom de jeune fille
	 * @param firstName label colonne prénom
	 * @param birthDate label colonne date de naissance
	 * @param portable 
	 * @param telephone 
	 * @param email 
	 */
	private void populate(File file, int sheetNumber, String name, String maidenName, String firstName, String birthDate, String portable, String telephone, String email) {
		List<Adherant> adherantsList = ExcelTool.populate(file, sheetNumber, name, maidenName, firstName, birthDate, portable, telephone, email);
		populate(adherantsList);
	}
	
	/**
	 * Rempli la base des Adhérants à partir d'un fichier.
	 * @param file
	 * @param sheetNumber
	 * @param name label colonne nom de famille
	 * @param maidenName label colonne nom de jeune fille
	 * @param firstName label colonne prénom
	 * @param birthDate label colonne date de naissance
	 * @param mobile 
	 * @param telephone 
	 * @param email 
	 */
	private void populate(String fileName, int sheetNumber, String name, String maidenName, String firstName, String birthDate, String mobile, String telephone, String email) {
		List<Adherant> adherantsList = ExcelTool.populate(fileName, sheetNumber, name, maidenName, firstName, birthDate, mobile, telephone, email);
		populate(adherantsList);
	}

	/**
	 * Rempli la base avec les adhérants & initialise la map de recherche des noms
	 * @param adherantsList
	 */
	void populate(List<Adherant> adherantsList) {
		adherants = adherantsList;

		for (Adherant adherant : adherantsList) {
			List<String> keys = createKeys4NameMap(adherant);
			for (String key : keys) {
				nameMap.put(key, adherant);
			}
		}
	}

	/**
	 * Création de la liste des alias de noms pour un Adhérant.
	 * @param adherant
	 * @return
	 */
	private List<String> createKeys4NameMap(Adherant adherant) {
		List<String> keys = new ArrayList<String>();

		String name = HashName.cleanName(adherant.name);
		keys.add(name);
		keys.addAll(encode(name));
		if (!adherant.maidenName.isEmpty()) {
			String maidenName = HashName.cleanName(adherant.maidenName);
			keys.add(maidenName);
			keys.addAll(encode(maidenName));
		}

		return keys;
	}

	/**
	 * Encode un nom avec le {@link BeiderMorseEncoder}
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Collection<? extends String> encode(String name) {
		BeiderMorseEncoder encoder = new BeiderMorseEncoder();
		try {
			String encodedName = encoder.encode(name);
			return Arrays.asList(encodedName.split("\\|"));
		} catch (EncoderException e) {
			e.printStackTrace();
			return (List<String>) Collections.EMPTY_LIST;
		}
	}

	/**
	 * Cherche des adhérants en base et détermine le score
	 * @param values liste des valeurs concernant cet adhérant
	 * @return Adhérant et score max
	 */
	public Optional<AdherantScore> searchAdherant(String values[]) {
		int maxScore = 0;
		Adherant currentMax = null;

		for (String value : values) { // Pour chaque valeur
			LOGGER.debug("Search => {}", value);

			@SuppressWarnings("unchecked")
			Collection<Adherant> coll = (Collection<Adherant>) nameMap.get(HashName.cleanName(value)); // y a t'il un nom ?
			
			if (coll != null) {
				for (Adherant currentAdherant : coll) {
					if (currentAdherant != null) {
						LOGGER.debug("CurrentAdherant : {}", currentAdherant);
						int currentScore = (currentAdherant.name.equals(value) ? 50 : 25);
						LOGGER.debug("Name : {} %", currentScore);
						if (search(HashName.cleanName(currentAdherant.firstName), values)) {
							currentScore += 30;
						} else {
							currentScore -= 10;
						}

						LOGGER.debug("First Name : {} %", currentScore);
						
						if (currentAdherant.birth.isEmpty()) {
							currentScore += Math.min(20, currentScore * 0.5);
						} else if (search(HashName.cleanName(currentAdherant.birth), values)) {
							currentScore += 20;
						}
						
						LOGGER.debug("Birth : {} %", currentScore);

						if (currentScore == 100) {
							return Optional.of(new AdherantScore(currentAdherant, currentScore)); // perfect match
						} else if (maxScore < currentScore) {
							LOGGER.debug("CURRENT MAX {}", currentScore);
							currentMax = currentAdherant;
							maxScore = currentScore;
						}
					}
				}
			}
		}

		if (maxScore > 30) {
			return Optional.of(new AdherantScore(currentMax, maxScore));
		}
		return Optional.absent();
	}

	private boolean search(String value, String[] values) {
		if (value == null)
			return false;

		for (String elt : values) {
			if (value.equals(HashName.cleanName(elt))) 
				return true;
		}

		return false;
	}


	private void printAdherants() {
		System.out.println("====================================================================================");
		for (Adherant adherant : adherants) {
			System.out.println(adherant.toString());
		}
		System.out.println("====================================================================================");
	}

	public List<Adherant> getAdherants() {
		return adherants;
	}

	public static void main(String[] args) {
		DatabaseStorage st = new DatabaseStorage();

		st.populate(args[0], 1, "NOM", "Nom Jeune Fille", "Prenom", "Date Naissance", "Portable", "Téléphone", "Email");
		st.printAdherants();
	}
}
