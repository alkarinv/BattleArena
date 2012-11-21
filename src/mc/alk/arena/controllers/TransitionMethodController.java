package mc.alk.arena.controllers;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mc.alk.arena.events.BAEvent;
import mc.alk.arena.listeners.ArenaListener;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.events.MatchEventHandler;
import mc.alk.arena.objects.events.MatchEventMethod;


public class TransitionMethodController {

	/** Our registered events and the methods to call when they happen*/
	HashMap<ArenaListener,HashMap<Class<? extends BAEvent>,List<MatchEventMethod>>> MatchEventMethods =
			new HashMap<ArenaListener,HashMap<Class<? extends BAEvent>,List<MatchEventMethod>>>();

	public TransitionMethodController(){}

	public List<MatchEventMethod> getMethods(ArenaListener ael, BAEvent event) {
		HashMap<Class<? extends BAEvent>,List<MatchEventMethod>> typeMap = MatchEventMethods.get(ael);
		if (typeMap == null)
			return null;
		return typeMap.get(event.getClass());
	}

	public Map<Class<? extends BAEvent>,List<MatchEventMethod>> getMethods(ArenaListener ael) {
		return MatchEventMethods.get(ael);
	}

	@SuppressWarnings("unchecked")
	public void addListener(ArenaListener transitionListener){
		HashMap<Class<? extends BAEvent>,List<MatchEventMethod>> typeMap =
				new HashMap<Class<? extends BAEvent>,List<MatchEventMethod>>();
		Method[] methodArray = transitionListener.getClass().getMethods();
		for (Method method : methodArray){
			MatchEventHandler teh = method.getAnnotation(MatchEventHandler.class);
			if (teh == null)
				continue;
			/// Make sure there is some sort of BAEvent here
			Class<?>[] classes = method.getParameterTypes();
			if (classes.length == 0 || !(BAEvent.class.isAssignableFrom(classes[0]))){
//				System.err.println("BAEvent was not found for method " + method);
				continue;
			}
			Class<? extends BAEvent> baEvent = (Class<? extends BAEvent>)classes[0];

			List<MatchEventMethod> mths = typeMap.get(baEvent);
			if (mths == null){
				mths = new ArrayList<MatchEventMethod>();
				typeMap.put(baEvent, mths);
			}

			mths.add(new MatchEventMethod(method, baEvent,MatchState.NONE,MatchState.NONE,MatchState.NONE,teh.priority()));
			Collections.sort(mths);
		}
		MatchEventMethods.put(transitionListener, typeMap);
	}

	public void removeListener(ArenaListener transitionListener){
		synchronized(MatchEventMethods){
			MatchEventMethods.remove(transitionListener);
		}
	}

	public void callListeners(BAEvent event) {
		Set<ArenaListener> mtls = MatchEventMethods.keySet();
		if (mtls == null){
			return;}
		/// For each ArenaListener class that is listening
		for (ArenaListener tl: mtls){
			List<MatchEventMethod> methods = getMethods(tl,event);
			if (methods == null){
				continue;}

			/// For each of the splisteners methods that deal with this Event
			for(MatchEventMethod method: methods){
				try {
					method.getMethod().invoke(tl, event); /// Invoke the listening transitionlisteners method
				} catch (Exception e){
					e.printStackTrace();
				}
			}
		}
	}
}