package vfx;

/** Checks the UI icons and rank badges are well-formed. */
public final class IconTest {

	private IconTest() {
	}

	private static void check(final boolean condition, final String message) {
		if (!condition)
			throw new AssertionError(message);
	}

	public static void main(final String[] args) {
		check(Icon.values().length == 17,
				"HUD, menu, settings and title screen icons exist");

		for (Icon icon : Icon.values()) {
			check(icon.getWidth() > 0 && icon.getHeight() > 0,
					icon + " has a size");

			int lit = 0;
			for (boolean[] column : icon.getPixels()) {
				check(column.length == icon.getHeight(),
						icon + " columns all have the same height");
				for (boolean pixel : column)
					if (pixel)
						lit++;
			}
			check(lit > 0, icon + " has at least one lit pixel");
		}

		check(Icon.LIFE.getPixels()[0][1], "Heart left lobe is lit");
		check(!Icon.LIFE.getPixels()[0][5], "Heart tip is narrow");
		check(!Icon.PAUSE.getPixels()[3][3], "Pause icon has a gap");
		check(!Icon.LIFE_LOST.getPixels()[3][1], "Broken heart has a crack");
		check(Icon.PLAY.getColor(4, 2) != Icon.PLAY.getColor(4, 0),
				"Rocket window differs from its body");

		for (int rank = 1; rank <= 7; rank++) {
			java.awt.Color[][] badge = RankBadge.forRank(rank);
			check(badge.length == RankBadge.WIDTH
					&& badge[0].length == RankBadge.HEIGHT,
					"Rank " + rank + " badge has the badge size");
		}
		check(RankBadge.forRank(1)[1][0] != null,
				"Top three medals have a ribbon");
		check(RankBadge.forRank(4)[1][0] == null,
				"Other ranks have no ribbon");
		check(!RankBadge.forRank(1)[5][6].equals(RankBadge.forRank(2)[5][6]),
				"Gold and silver medals differ");

		System.out.println("Icon checks passed.");
	}
}
