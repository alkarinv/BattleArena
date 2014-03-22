package test.mc.alk.arena;

import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.util.MinMax;


public class TestParams extends BATest {

	public void testFlatten() throws Exception {
        MatchParams mp = loadParams("Arena", "/ArenaConfig.yml");
        mp.setNTeams(new MinMax(2,2));
        mp.setTeamSize(new MinMax(2, 2));
        mp.setNLives(3);
        mp.setMatchTime(37);
        assert(mp.getMinTeams()==2);
        assert(mp.getMaxTeams()==2);
        assert(mp.getNLives()==3);

        Arena arena = bac.getArena("a2");
        assertNotNull(arena);
        MatchParams ap = arena.getParams();
        ap.setNTeams(new MinMax(3, 5));
        ap.setTeamSize(new MinMax(3, 5));
        ap.setParent(mp);
        ap.setNLives(null);
        ap.setMatchTime(null);

        assert(ap.getNLives()==3);
        assert(ap.getMinTeams()==3);
        assert(ap.getMaxTeams()==5);
        assert(ap.getMatchTime().equals(mp.getMatchTime()));

        MatchParams apc = ParamController.copyParams(ap); /// ap copy
        assert (apc.getParent() == mp);
        assert(apc.getNLives()==3);
        assert(apc.getMinTeams()==3);
        assert(apc.getMaxTeams()==5);

        apc.flatten();
        /// verify we haven't changed old values
        ap = arena.getParams();
        mp = ParamController.getMatchParams(mp);
        assert(ap.getNLives()==3);
        assert(ap.getMinTeams()==3);
        assert(ap.getMaxTeams()==5);
        assert(mp.getMinTeams()==2);
        assert(mp.getMaxTeams()==2);
        assert(mp.getNLives()==3);

        MatchParams a = arena.getParams();
        MatchParams b = new MatchParams(ParamController.getMatchParamCopy(arena.getArenaType()));
        apc = ParamController.copyParams(a);
        apc.setName(this.getName());
        apc.flatten();
//        this.tops = params.getThisTransitionOptions();
        MatchParams a2 = arena.getParams();
        MatchParams b2 = ParamController.getMatchParamCopy(arena.getArenaType());

        assert(a.getNLives().equals(a2.getNLives()));
        assert(a.getMinTeams().equals(a2.getMinTeams()));
        assert (a.getParent() == a2.getParent());
        assert(b.getMaxTeams().equals(b2.getMaxTeams()));
        assert(b.getMinTeams().equals(b2.getMinTeams()));
        assert (b.getParent() == b2.getParent());
    }
}
