package net.doodcraft.oshcon.bukkit.doodcore.badges;

public enum BadgeType {
    // Played during the testing phase of any DoodCraft server/map
    // PERMANENT
    BETA_TESTER,
    // Slain 50 different players on a single DoodCraft survival server/map
    // PERMANENT
    PLAYER_SLAYER,
    // Was every element at least once on DoodCraft Bending
    // PERMANENT
    AVATAR,
    // Supported the server with a donation
    // CONDITIONAL, usually kept for about two months, or until supporter status expires
    SUPPORTER,
    // Played on one of the original DoodCraft server/maps between 2013 and 2015
    // PERMANENT
    OLDIE,
    // Defeated the EnderDragon first.
    // CONDITIONAL, does not persist to other maps/servers.
    DRAGON_SLAYER,
    // Invalid Badge
    NULL_BADGE;

    public static Boolean isBadgeType(String string) {
        switch (string) {
            case "BETA_TESTER":
                return true;
            case "PLAYER_SLAYER":
                return true;
            case "AVATAR":
                return true;
            case "SUPPORTER":
                return true;
            case "OLDIE":
                return true;
            case "DRAGON_SLAYER":
                return true;
        }

        return false;
    }
}