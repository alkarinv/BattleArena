package mc.alk.arena.objects.spawns;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class EntitySpawn extends SpawnInstance{
	private EntityType et;
	int number = 1;
	List<LivingEntity> uids = new ArrayList<LivingEntity>();

	public EntitySpawn(EntityType et) {
		super(null);
		this.et = et;
	}

	public EntitySpawn(EntityType et,int number) {
		super(null);
		this.et = et;
		this.number =number;
	}

	public int spawn() {
		for (LivingEntity id: uids){
			if (!id.isDead()){
				return spawnId;} /// The entities are already spawned
		}
		uids.clear();
		for (int i=0;i< number;i++){
			uids.add((LivingEntity)loc.getWorld().spawnEntity(loc, et));}
		return spawnId;
	}

	public void despawn() {
		for (LivingEntity id: uids){
			if (!id.isDead()){
				id.remove();}
		}
		uids.clear();
	}

	public String getEntityString() {
		return et.getName();
	}

	public int getNumber() {
		return number;
	}

	public String toString(){
		return "[EntitySpawn "+et +":" + number+"]";
	}
}

