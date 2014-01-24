package mc.alk.arena.util;

import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.MatchParams;

public class ParamUtil {
	MatchParams params;
	ArenaParams parent;
	public ParamUtil(MatchParams params){
		this.params = params;
	}
	public void removeParent() {
		parent = params.getParent();
		params.setParent(null);
	}

	public void restoreParent() {
		params.setParent(parent);
	}

}
