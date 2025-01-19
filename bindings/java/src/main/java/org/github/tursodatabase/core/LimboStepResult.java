package org.github.tursodatabase.core;

import java.util.Arrays;

import org.github.tursodatabase.annotations.NativeInvocation;

/**
 * Represents the step result of limbo's statement's step function.
 */
public class LimboStepResult {
    private static final int STEP_RESULT_ID_ROW = 10;
    private static final int STEP_RESULT_ID_IO = 20;
    private static final int STEP_RESULT_ID_DONE = 30;
    private static final int STEP_RESULT_ID_INTERRUPT = 40;
    private static final int STEP_RESULT_ID_BUSY = 50;
    private static final int STEP_RESULT_ID_ERROR = 60;

    // Identifier for limbo's StepResult
    private final int stepResultId;
    private final Object[] result;

    @NativeInvocation(invokedFrom = "limbo_statement.rs")
    public LimboStepResult(int stepResultId, Object[] result) {
        this.stepResultId = stepResultId;
        this.result = result;
    }

    public boolean isRow() {
        return stepResultId == STEP_RESULT_ID_ROW;
    }

    public boolean isDone() {
        return stepResultId == STEP_RESULT_ID_DONE;
    }

    @Override
    public String toString() {
        return "LimboStepResult{" +
               "stepResultId=" + stepResultId +
               ", result=" + Arrays.toString(result) +
               '}';
    }
}
