package com.behavioral.strategy;

import java.util.ArrayList;
import java.util.List;

public class ParallelSquentialRule {
	private String name;
	private List<Rule> rules;
	private ExecutionStrategy executeStrategy;

	public ParallelSquentialRule(List<Rule> rules, ExecutionStrategy executeStrategy, String name) {
		this.rules = rules;
		this.executeStrategy = executeStrategy;
		this.name = name;
	}

	public ParallelSquentialRule() {
		rules = new ArrayList<>();
		executeStrategy = ExecutionStrategy.SEQUENTIAL;
		this.name = "default";
	}

	public List<Rule> getRules() {
		return rules;
	}

	public void setRules(List<Rule> rules) {
		if (null != this.rules && !this.rules.isEmpty())
			this.rules.addAll(rules);
		else
			this.rules = rules;
	}

	public void setRules(Rule... rules) {
		setRules(List.of(rules));
	}

	public ExecutionStrategy getExecuteStrategy() {
		return executeStrategy;
	}

	public void setExecuteStrategy(ExecutionStrategy executeStrategy) {
		this.executeStrategy = executeStrategy;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
