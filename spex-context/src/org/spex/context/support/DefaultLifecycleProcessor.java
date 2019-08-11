package org.spex.context.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.spex.beans.factory.BeanFactory;
import org.spex.beans.factory.BeanFactoryAware;
import org.spex.beans.factory.BeanFactoryUtils;
import org.spex.beans.factory.ListableBeanFactory;
import org.spex.context.Lifecycle;
import org.spex.context.LifecycleProcessor;
import org.spex.context.Phased;
import org.spex.context.SmartLifecycle;
import org.spex.util.LoggerUtil;

public class DefaultLifecycleProcessor implements LifecycleProcessor, BeanFactoryAware {

	private volatile long timeoutPerShutdown = 30000;
	
	private volatile boolean running;
	
	private volatile ListableBeanFactory beanFactory;
	
	public void setTimeoutPerShutdown(long timeoutPerShutdown) {
		this.timeoutPerShutdown = timeoutPerShutdown;
	}

	@Override
	public void start() {
		startBeans(false);
		this.running = true;
	}

	@Override
	public void stop() {
		stopBeans();
		this.running = false;
	}

	@Override
	public boolean isRunning() {
		return this.running;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = (ListableBeanFactory) beanFactory;
	}

	@Override
	public void onRefresh() {
		startBeans(true);
		this.running = true;
	}

	@Override
	public void onClose() {
		stopBeans();
		this.running = false;
	}

	protected Map<String, Lifecycle> getLifecycleBeans() {
		Map<String, Lifecycle> beans = new LinkedHashMap<String, Lifecycle>();
		
		String[] beanNames = this.beanFactory.getBeanNamesForType(Lifecycle.class, false, false);
		for (String beanName : beanNames) {
			String beanNameToRegister = BeanFactoryUtils.transformedBeanName(beanName);
			boolean isFactoryBean = this.beanFactory.isFactoryBean(beanNameToRegister);
			String beanNameToCheck = isFactoryBean ? BeanFactory.FACTORY_BEAN_PREFIX + beanName : beanName;
			if (this.beanFactory.containsSingleton(beanNameToRegister) &&
					(!isFactoryBean || Lifecycle.class.isAssignableFrom(this.beanFactory.getType(beanNameToCheck))) ||
					SmartLifecycle.class.isAssignableFrom(this.beanFactory.getType(beanNameToCheck))) {
				
				Lifecycle bean = this.beanFactory.getBean(beanNameToCheck, Lifecycle.class);
				if (bean != this) {
					beans.put(beanNameToRegister, bean);
				}
			}
		}
		
		return beans;
	}
	
	protected int getPhase(Lifecycle bean) {
		return (bean instanceof Phased ? ((Phased) bean).getPhase() : 0);
	}
	
	private void startBeans(boolean autoStartupOnly) {
		Map<String, Lifecycle> lifecycleBeans = getLifecycleBeans();
		Map<Integer, LifecycleGroup> phases = new HashMap<Integer, LifecycleGroup>();
		
		for (Map.Entry<String, Lifecycle> entry : lifecycleBeans.entrySet()) {
			Lifecycle lifecycle = entry.getValue();
			
			if (!autoStartupOnly || (lifecycle instanceof SmartLifecycle && ((SmartLifecycle) lifecycle).isAutoStartup())) {
				int phase = getPhase(lifecycle);
				LifecycleGroup group = phases.get(phase);
				
				if (group == null) {
					group = new LifecycleGroup(phase, timeoutPerShutdown, lifecycleBeans);
					phases.put(phase, group);
				}
				group.add(entry.getKey(), lifecycle);
			}
		}
		
		if (phases.size() > 0) {
			List<Integer> keys = new ArrayList<Integer>(phases.keySet());
			Collections.sort(keys);
			for (Integer key : keys) {
				phases.get(key).start();
			}
		}
	}
	
	private void stopBeans() {
		Map<String, Lifecycle> lifecycleBeans = getLifecycleBeans();
		Map<Integer, LifecycleGroup> phases = new HashMap<Integer, LifecycleGroup>();
		
		for (Map.Entry<String, Lifecycle> entry : lifecycleBeans.entrySet()) {
			Lifecycle lifecycle = entry.getValue();
			
			int phase = getPhase(lifecycle);
			LifecycleGroup group = phases.get(phase);
			
			if (group == null) {
				group = new LifecycleGroup(phase, timeoutPerShutdown, lifecycleBeans);
				phases.put(phase, group);
			}
			group.add(entry.getKey(), lifecycle);
		}
		
		if (phases.size() > 0) {
			List<Integer> keys = new ArrayList<Integer>(phases.keySet());
			Collections.sort(keys, Collections.reverseOrder());
			for (Integer key : keys) {
				phases.get(key).stop();
			}
		}
	}
	
	private void doStart(Map<String, ? extends Lifecycle> lifecycleBeans, String beanName) {
		Lifecycle bean = lifecycleBeans.get(beanName);
		if (bean != null && !this.equals(bean)) {
			String[] dependenciesForBean = this.beanFactory.getDependenciesForBean(beanName);
			for (String dependency : dependenciesForBean) {
				doStart(lifecycleBeans, dependency);
			}
			
			if (!bean.isRunning()) {
				bean.start();
			}
			lifecycleBeans.remove(beanName);
		}
	}
	
	
	private void doStop(Map<String, ? extends Lifecycle> lifecycleBeans, final String beanName, 
			final CountDownLatch latch, final Set<String> countDownBeanNames) {
		
		Lifecycle bean = lifecycleBeans.get(beanName);
		if (bean != null) {
			String[] dependenciesForBean = this.beanFactory.getDependenciesForBean(beanName);
			for (String dependency : dependenciesForBean) {
				doStop(lifecycleBeans, dependency, latch, countDownBeanNames);
			}
			
			try {
				if (bean.isRunning()) {
					if (bean instanceof SmartLifecycle) {
						countDownBeanNames.add(beanName);
						((SmartLifecycle) bean).stop(new Runnable() {
	
							@Override
							public void run() {
								latch.countDown();
								countDownBeanNames.remove(beanName);
							}
							
						});
					} else {
						bean.stop();
					}
				} else {
					if (bean instanceof SmartLifecycle) {
						latch.countDown();
					}
				}
			} catch (Throwable e) {
				LoggerUtil.error("¹Ø±Õ'" + beanName + "'Ê§°Ü", e);
			}
			lifecycleBeans.remove(beanName);
		}
	}
	
	private class LifecycleGroupMember implements Comparable<LifecycleGroupMember> {

		private final String name;
		
		private final Lifecycle bean;
		
		public LifecycleGroupMember(String name, Lifecycle bean) {
			this.name = name;
			this.bean = bean;
		}
		
		@Override
		public int compareTo(LifecycleGroupMember o) {

			int thisOrder = getPhase(this.bean);
			int otherOrder = getPhase(o.bean);
			return thisOrder == otherOrder ? 0 : thisOrder < otherOrder ? -1 : 1;
		}
	}
	
	private class LifecycleGroup {
		
		private final List<LifecycleGroupMember> members = new ArrayList<LifecycleGroupMember>();
		
		private Map<String, ? extends Lifecycle> lifecycleBeans = getLifecycleBeans();
		
		private volatile int smartMemberCount;
		
		private final int phase;
		
		private final long timeout;
		
		public LifecycleGroup(int phase, long timeout, Map<String, ? extends Lifecycle> lifecycleBeans) {
			this.phase = phase;
			this.timeout = timeout;
			this.lifecycleBeans = lifecycleBeans;
		}
		
		public void add(String name, Lifecycle bean) {
			if (bean instanceof SmartLifecycle) {
				this.smartMemberCount++;
			}
			this.members.add(new LifecycleGroupMember(name, bean));
		}
		
		public void start() {
			if (this.members.isEmpty()) {
				return;
			}
			
			LoggerUtil.info("Starting beans in phase " + this.phase);
			
			Collections.sort(this.members);
			
			for (LifecycleGroupMember member : this.members) {
				if (this.lifecycleBeans.containsKey(member.name)) {
					doStart(this.lifecycleBeans, member.name);
				}
			}
		}
		
		public void stop() {
			if (this.members.isEmpty()) {
				return;
			}
			
			LoggerUtil.info("Stoping beans in phase " + this.phase);
			
			Collections.sort(this.members, Collections.reverseOrder());
			
			CountDownLatch latch = new CountDownLatch(this.members.size());
			
			Set<String> countDownBeanNames = Collections.synchronizedSet(new LinkedHashSet<String>());
			
			for (LifecycleGroupMember member : this.members) {
				if (this.lifecycleBeans.containsKey(member.name)) {
					doStop(this.lifecycleBeans, member.name, latch, countDownBeanNames);
				}
				else if (member.bean instanceof SmartLifecycle) {
					latch.countDown();
				}
			}
			
			try{
				latch.await(timeout, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
}
