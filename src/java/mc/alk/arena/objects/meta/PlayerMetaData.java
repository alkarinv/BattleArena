package mc.alk.arena.objects.meta;


import mc.alk.arena.objects.PlayerSave;
import mc.alk.arena.objects.options.JoinOptions;

public class PlayerMetaData {
	private boolean joining;
    private JoinOptions joinOptions;
    private int livesLeft = -1;

    PlayerSave joinRequirements;

    public boolean isJoining() {
		return joining;
	}

	public void setJoining(boolean joining) {
		this.joining = joining;
	}

    public JoinOptions getJoinOptions() {return joinOptions;}

    public void setJoinOptions(JoinOptions jo) {this.joinOptions = jo;}

    public int getLivesLeft() {
        return livesLeft;
    }

    public void setLivesLeft(int livesLeft) {
        this.livesLeft = livesLeft;
    }

    public PlayerSave getJoinRequirements() {
        return joinRequirements;
    }

    public void setJoinRequirements(PlayerSave joinRequirements) {
        this.joinRequirements = joinRequirements;
    }

}
