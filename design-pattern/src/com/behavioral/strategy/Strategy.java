package com.behavioral.strategy;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

public abstract class Strategy {
	private List<ParallelSquentialRule> parallelSquentialRules;
	private ExecutionStrategy executionStrategy;

	public abstract Strategy build();

	public Strategy() {
		this.executionStrategy = ExecutionStrategy.SEQUENTIAL;
	}

	protected Strategy setRules(List<ParallelSquentialRule> parallelSquentialRules) {
		this.parallelSquentialRules = Collections.synchronizedList(parallelSquentialRules);
		return this;
	}

	protected Strategy setExecutionStrategy(ExecutionStrategy executionStrategy) {
		this.executionStrategy = executionStrategy;
		return this;
	}

	public void executeStrategy(DomainModel domainModel) {
		if (null != parallelSquentialRules && parallelSquentialRules.size() > 0) {
			if (null == executionStrategy || executionStrategy == ExecutionStrategy.SEQUENTIAL) {
				parallelSquentialRules.stream().forEach(rules -> execute(rules, domainModel));
			} else {
				parallelSquentialRules.parallelStream().forEach(rules -> execute(rules, domainModel));
			}
		}
	}

	private void execute(ParallelSquentialRule rules, DomainModel domainModel) {
		System.out.println("Rule execution started for " + rules.getName());
		if (null != rules && null != rules.getRules() && rules.getRules().size() > 0) {
			if (null == rules.getExecuteStrategy() || rules.getExecuteStrategy() == ExecutionStrategy.SEQUENTIAL) {
				rules.getRules().stream().forEach(rule -> rule.process(domainModel));
			} else if (rules.getExecuteStrategy() == ExecutionStrategy.PARALLEL) {
				try {
					new ForkJoinPool(Runtime.getRuntime().availableProcessors()).submit(() -> {
						rules.getRules().parallelStream().forEach(rule -> rule.process(domainModel));
					}).get();
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			} else if (rules.getExecuteStrategy() == ExecutionStrategy.FIRE_AND_FORGET) {
				CompletableFuture.runAsync(() -> {
					rules.getRules().parallelStream().forEach(rule -> rule.process(domainModel));
				});
			}
		}
		System.out.println("Rule execution finished for " + rules.getName() + "\n");
	}
}
