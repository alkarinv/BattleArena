package mc.alk.arena.objects.spawns;

import mc.alk.arena.util.Log;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Tameable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class EntitySpawn extends SpawnInstance{
	final private EntityType et;
	final List<LivingEntity> uids = new ArrayList<LivingEntity>();
    int number = 1;
    static Method spawnEntityMethod;
    static {
        try {
            spawnEntityMethod = World.class.getMethod("spawnEntity");
        } catch (Exception e) {
            try {
                spawnEntityMethod = World.class.getMethod("spawnCreature", Location.class, EntityType.class);
            } catch (NoSuchMethodException e1) {
                Log.printStackTrace(e1);
            }
        }
    }

	public EntitySpawn(EntityType et) {
		super(null);
		this.et = et;
	}

	public EntitySpawn(EntityType et,int number) {
		super(null);
		this.et = et;
		this.number =number;
	}

    public EntitySpawn(EntitySpawn entitySpawn) {
        super(null);
        this.et = entitySpawn.et;
        this.number = entitySpawn.number;
    }

    @Override
    public void spawn() {
        if (spawnEntityMethod==null)
            return;
        for (LivingEntity id: uids){
			if (!id.isDead()){
				return;} /// The entities are already spawned
		}
		uids.clear();
		for (int i=0;i< number;i++) {
            try {
                LivingEntity le = (LivingEntity) spawnEntityMethod.invoke(loc.getWorld(), loc, et);
                uids.add(le);
            } catch (Exception e) {
                Log.printStackTrace(e);
            }
        }
    }

    @Override
	public void despawn() {
		for (LivingEntity id: uids){
			if (!id.isDead()){
				id.remove();}
		}
		uids.clear();
	}

	public void setOwner(AnimalTamer tamer){
		for (LivingEntity id: uids){
			if (!id.isDead() && id instanceof Tameable){
				((Tameable)id).setTamed(true);
				((Tameable)id).setOwner(tamer);
			}
		}
	}

	public String getEntityString() {
		return et.getName();
	}

	public int getNumber() {
		return number;
	}

	@Override
	public String toString(){
		return "[EntitySpawn "+et +":" + number+"]";
	}
}

