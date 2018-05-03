package batch.advance;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;

public class SetDataContextTasklet implements Tasklet {

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        ExecutionContext jobExecutionContext = chunkContext.getStepContext().
                getStepExecution().getExecutionContext();
//                getJobExecution().
//                getExecutionContext();
        jobExecutionContext.put("username","huhx");
        return RepeatStatus.FINISHED;
    }
}