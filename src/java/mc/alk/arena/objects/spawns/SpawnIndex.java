package mc.alk.arena.objects.spawns;

/**
 * @author alkarin
 */
public class SpawnIndex {
    public final int teamIndex;
    public final int spawnIndex;

    public SpawnIndex(int teamIndex) {
        this(teamIndex, 0);
    }
    public SpawnIndex(int teamIndex, int spawnIndex) {
        this.teamIndex = teamIndex;
        this.spawnIndex = spawnIndex;
    }
}
