package wbs.extras.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;

public class WbsEnums {

	private static Map<Class<? extends Enum>, String[]> stringArrays = new HashMap<>();
	
	public static String[] toStringArray(Class<? extends Enum> type) {
		if (stringArrays.containsKey(type)) {
			return stringArrays.get(type);
		}

		String[] typeArray = Arrays.stream(type.getEnumConstants())
				.map(Enum::toString)
				.map(String::toLowerCase)
				.toArray(String[]::new);
		stringArrays.put(type, typeArray);
		return typeArray;
	}
	
	public static List<String> toStringList(Class<? extends Enum> type) {
		return Arrays.asList(toStringArray(type));
	}
	
	public static Sound soundFromString(String from) {
		from = from.replaceAll("\\.", "_");
		for (String soundString : toStringArray(Sound.class)) {
			if (soundString.equalsIgnoreCase(from)) {
				return Sound.valueOf(soundString.toUpperCase());
			}
		}
		return null;
	}

	public static GameMode gameModeFromString(String from) {
		for (String modeString : toStringArray(GameMode.class)) {
			if (modeString.equalsIgnoreCase(from)) {
				return GameMode.valueOf(modeString.toUpperCase());
			}
		}
		return null;
	}

	public static Particle particleFromString(String from) {
		for (String particleString : toStringArray(Particle.class)) {
			if (particleString.equalsIgnoreCase(from)) {
				return Particle.valueOf(particleString.toUpperCase());
			}
		}
		return null;
	}

	public static Material materialFromString(String from) {
		return materialFromString(from, null);
	}
	
	public static Material materialFromString(String from, Material defaultMaterial) {
		if (from == null) {
			return defaultMaterial;
		}
		for (String materialString : toStringArray(Material.class)) {
			if (materialString.equalsIgnoreCase(from)) {
				return Material.valueOf(materialString.toUpperCase());
			}
		}
		return defaultMaterial;
	}
	
}
