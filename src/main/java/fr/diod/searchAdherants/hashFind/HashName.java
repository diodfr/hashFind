package fr.diod.searchAdherants.hashFind;

import java.util.regex.Pattern;

/**
 * Hash Names Utils
 */
public class HashName {
	
	private static final Pattern PATTERN_NOT_LETTER = Pattern.compile("\\W");
	private static final Pattern PATTERN_E = Pattern.compile("[éèêë]");
	private static final Pattern PATTERN_I = Pattern.compile("ï");
	public static String hashName(String name) {
		String cleanName = name.trim().toLowerCase();
		
		cleanName = PATTERN_E.matcher(cleanName).replaceAll("e");
		cleanName = PATTERN_I.matcher(cleanName).replaceAll("i");
		cleanName = PATTERN_NOT_LETTER.matcher(cleanName).replaceAll("");
		
		if (cleanName.endsWith("s")) {
			cleanName = cleanName.substring(0, Math.max(0, cleanName.length() - 1));
		}
		
		return cleanName;
	}
}
