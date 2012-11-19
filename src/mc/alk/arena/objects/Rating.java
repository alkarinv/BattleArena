package mc.alk.arena.objects;


public enum Rating {
	RATED,UNRATED, UNKNOWN, ANY;

	public static Rating fromString(String rating) {
		try {
			return Rating.valueOf(rating.toUpperCase());
		} catch (Exception e){
			return Rating.UNKNOWN;
		}
	}

	public static Rating fromBoolean(Boolean b) {
		return b? Rating.RATED : Rating.UNRATED;
	}

	public static String getValidList() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Rating r: Rating.values()){
			if (!first) sb.append(", ");
			first = false;
			sb.append(r);
		}
		return sb.toString();
	}
}
