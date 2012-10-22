package mc.alk.arena.controllers;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mc.alk.arena.Defaults;
import mc.alk.arena.events.BAEvent;
import mc.alk.arena.listeners.TransitionListener;
import mc.alk.arena.objects.events.TransitionEventHandler;
import mc.alk.arena.objects.events.TransitionMethod;


public class TransitionMethodController {

	/** Our registered events and the methods to call when they happen*/
	HashMap<TransitionListener,HashMap<Class<? extends BAEvent>,List<TransitionMethod>>> transitionMethods = 
			new HashMap<TransitionListener,HashMap<Class<? extends BAEvent>,List<TransitionMethod>>>();

	public TransitionMethodController(){}

	public List<TransitionMethod> getMethods(TransitionListener ael, BAEvent event) {
		HashMap<Class<? extends BAEvent>,List<TransitionMethod>> typeMap = transitionMethods.get(ael);
		if (Defaults.DEBUG_TEVENTS) System.out.println("!! getEvent "+ael.getClass()+ "   methods="+(typeMap==null?"null" :typeMap.size()));
		if (typeMap == null)
			return null;
		return typeMap.get(event.getClass());
	}

	public Map<Class<? extends BAEvent>,List<TransitionMethod>> getMethods(TransitionListener ael) {
		if (Defaults.DEBUG_TEVENTS) System.out.println("!!!! getEvent "+ael.getClass()+" contains=" + transitionMethods.containsKey(ael));
		return transitionMethods.get(ael);
	}

	@SuppressWarnings("unchecked")
	public void addListener(TransitionListener transitionListener){
		HashMap<Class<? extends BAEvent>,List<TransitionMethod>> typeMap = 
				new HashMap<Class<? extends BAEvent>,List<TransitionMethod>>();
		Method[] methodArray = transitionListener.getClass().getMethods();
		for (Method method : methodArray){
			TransitionEventHandler teh = method.getAnnotation(TransitionEventHandler.class);
			if (teh == null)
				continue;
			/// Make sure there is some sort of BAEvent here
			Class<?>[] classes = method.getParameterTypes();
			if (classes.length == 0 || !(BAEvent.class.isAssignableFrom(classes[0]))){
				System.err.println("BAEvent was not found for method " + method);
				continue;				
			}
			Class<? extends BAEvent> baEvent = (Class<? extends BAEvent>)classes[0];

			List<TransitionMethod> mths = typeMap.get(baEvent);
			if (mths == null){
				//				System.out.println("bukkitEvent !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! " + bukkitEvent + "  " + getPlayerMethod);
				mths = new ArrayList<TransitionMethod>();
				typeMap.put(baEvent, mths);
			}

			mths.add(new TransitionMethod(method, baEvent,teh.priority()));
			Collections.sort(mths);
		}
		transitionMethods.put(transitionListener, typeMap);
	}
	
	public void removeListener(TransitionListener transitionListener){
		synchronized(transitionMethods){
			transitionMethods.remove(transitionListener);
		}
	}
	
	public void callListeners(BAEvent event) {
		Set<TransitionListener> mtls = transitionMethods.keySet();
		if (mtls == null){
			if (Defaults.DEBUG_TEVENTS) System.out.println("   NO MTLS listening ");			
			return;
		}
		/// For each ArenaListener class that is listening
		if (Defaults.DEBUG_TEVENTS) System.out.println("   TLS splisteners .get " + mtls);
		for (TransitionListener tl: mtls){
			List<TransitionMethod> methods = getMethods(tl,event);
			if (Defaults.DEBUG_TEVENTS) System.out.println("    TL = " + tl.getClass() +"    getting methods "+methods);
			if (methods == null){
				continue;}

			/// For each of the splisteners methods that deal with this Event
			for(TransitionMethod method: methods){
				try {
					method.getMethod().invoke(tl, event); /// Invoke the listening transitionlisteners method
				} catch (Exception e){
					e.printStackTrace();
				}
			}			
		}
	}
}
