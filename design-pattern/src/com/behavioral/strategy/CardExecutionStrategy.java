package com.behavioral.strategy;

import java.util.ArrayList;
import java.util.List;

public class CardExecutionStrategy extends Strategy {

	@Override
	public Strategy build() {
		List<ParallelSquentialRule> parallelSquentialRules = new ArrayList<>();

		ParallelSquentialRule parallelSquentialRule0 = new ParallelSquentialRule();
		parallelSquentialRule0.setRules(Rules.RULE_10);
		parallelSquentialRule0.setExecuteStrategy(ExecutionStrategy.FIRE_AND_FORGET);
		parallelSquentialRule0.setName("rule-0");
		parallelSquentialRules.add(parallelSquentialRule0);

		ParallelSquentialRule parallelSquentialRule1 = new ParallelSquentialRule();
		parallelSquentialRule1.setRules(Rules.RULE_1, Rules.RULE_2, Rules.RULE_3);
		parallelSquentialRule1.setExecuteStrategy(ExecutionStrategy.PARALLEL);
		parallelSquentialRule1.setName("rule-1");
		parallelSquentialRules.add(parallelSquentialRule1);

		ParallelSquentialRule parallelSquentialRule2 = new ParallelSquentialRule();
		parallelSquentialRule2.setRules(Rules.RULE_4, Rules.RULE_5, Rules.RULE_6);
		parallelSquentialRule2.setExecuteStrategy(ExecutionStrategy.PARALLEL);
		parallelSquentialRule2.setName("rule-2");
		parallelSquentialRules.add(parallelSquentialRule2);

		ParallelSquentialRule parallelSquentialRule3 = new ParallelSquentialRule();
		parallelSquentialRule3.setRules(Rules.RULE_7, Rules.RULE_8, Rules.RULE_9);
		parallelSquentialRule3.setExecuteStrategy(ExecutionStrategy.PARALLEL);
		parallelSquentialRule3.setName("rule-3");
		parallelSquentialRules.add(parallelSquentialRule3);

		super.setRules(parallelSquentialRules);
		super.setExecutionStrategy(ExecutionStrategy.SEQUENTIAL);

		return this;
	}

}
