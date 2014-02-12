package mc.alk.arena.objects.spawns;

import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Tameable;

import java.util.ArrayList;
import java.util.List;

public class EntitySpawn extends SpawnInstance{
	final private EntityType et;
	final List<LivingEntity> uids = new ArrayList<LivingEntity>();
    int number = 1;

	public EntitySpawn(EntityType et) {
		super(null);
		this.et = et;
	}

	public EntitySpawn(EntityType et,int number) {
		super(null);
		this.et = et;
		this.number =number;
	}

    @Override
    public void spawn() {
		for (LivingEntity id: uids){
			if (!id.isDead()){
				return;} /// The entities are already spawned
		}
		uids.clear();
		for (int i=0;i< number;i++){
			LivingEntity le = (LivingEntity)loc.getWorld().spawnEntity(loc, et);
			uids.add(le);
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

