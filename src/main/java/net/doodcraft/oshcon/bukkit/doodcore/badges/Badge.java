package net.doodcraft.oshcon.bukkit.doodcore.badges;

public class Badge {

    private BadgeType type;

    public Badge(String type) {
        if (BadgeType.isBadgeType(type.toUpperCase())) {
            this.type = BadgeType.valueOf(type.toUpperCase());
        } else {
            this.type = BadgeType.NULL_BADGE;
        }
    }

    public Badge(BadgeType type) {
        this.type = type;
    }

    public String getName() {
        if (this.type != null) {
            return this.type.toString().toUpperCase();
        } else {
            return "Invalid_Badge";
        }
    }

    public String getFriendlyName() {

        String friendlyName;

        switch (getType()) {
            case AVATAR:
                friendlyName = "§5Avatar";
                return friendlyName;
            case SUPPORTER:
                friendlyName = "§6§lSupporter";
                return friendlyName;
            case BETA_TESTER:
                friendlyName = "§bBeta Tester";
                return friendlyName;
            case PLAYER_SLAYER:
                friendlyName = "§4Player Slayer";
                return friendlyName;
            case OLDIE:
                friendlyName = "§3Oldie";
                return friendlyName;
            case DRAGON_SLAYER:
                friendlyName = "§c§lDragon Slayer";
                return friendlyName;
            default:
                friendlyName = "null_badge";
                return friendlyName;
        }
    }

    public String getDescription() {

        String description;

        switch (getType()) {
            case AVATAR:
                description = "Be every element, including chiblocking, at least once on a DoodCraft Bending server.\nDoes not include servers prior to 2017.\n*Permanent*";
                break;
            case SUPPORTER:
                description = "Become a DoodCraft supporter! [/donate]\n*Two month expiration*";
                break;
            case BETA_TESTER:
                description = "Play during the testing phase of any DoodCraft Minecraft server.\n*Permanent*";
                break;
            case PLAYER_SLAYER:
                description = "Slay at least 50 different players on a single DoodCraft Minecraft PvP server.\n*Permanent*";
                break;
            case OLDIE:
                description = "Participants on DoodCraft Minecraft servers from two years ago or older.\n*Permanent*";
                break;
            case DRAGON_SLAYER:
                description = "The first to slay the Ender Dragon!\n*Does not carry over to other/future maps/servers*";
                break;
            default:
                description = "Invalid badge name.";
                break;
        }

        return description;
    }

    public BadgeType getType() {
        return this.type;
    }
}