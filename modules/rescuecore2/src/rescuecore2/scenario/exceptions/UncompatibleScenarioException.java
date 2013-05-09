package rescuecore2.scenario.exceptions;

public class UncompatibleScenarioException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UncompatibleScenarioException() {
		super(
				"Uncompatible Scenario Exception: Scenario is not implementing CollapseSimCompatibaleScenarioV1_1 interface");
	}

}
