package mc.alk.arena.objects.meta;


import mc.alk.arena.objects.options.JoinOptions;

public class PlayerMetaData {
	private boolean joining = false;
    private JoinOptions joinOptions;
    private int livesLeft = -1;

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
}
