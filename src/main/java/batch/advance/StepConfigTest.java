package batch.advance;

import org.junit.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

/**
 * @version :1.0.0
 * @author: term
 * @time: 2018-05-02 23:07
 * @description :
 */
public class StepConfigTest {
private JobBuilderFactory jobBuilderFactory;
    private  StepBuilderFactory stepBuilderFactory;
    @Bean
    public Job footballJob() {
        return this.jobBuilderFactory.get("footballJob")
                .start((Step) null)
//                .next(gameLoad())
//                .next(playerSummarization())
//                .end()
                .build();
    }

    @Test
    public void method(){
        JobExecution jobExecution=null;

        JobLauncher launcher=null;

//        launcher.run()
    Step step=null;


        StepExecution stepExecution=null;
        stepExecution.getJobExecution().getExecutionContext();
//        stepExecution.getReadCount();
//        stepExecution.getSkipCount();
//        stepExecution.setReadCount(2);
//        Step  step=null;
        CompositeItemProcessor<Foo,Foobar> compositeProcessor =
                new CompositeItemProcessor<Foo,Foobar>();
        List itemProcessors = new ArrayList();
        itemProcessors.add(new FooProcessor());
//        itemProcessors.add(new BarProcessor());
        compositeProcessor.setDelegates(itemProcessors);    }
    public class Foo {}

    public class Bar {
        public Bar(Foo foo) {}
    }

    public class Foobar {
        public Foobar(Bar bar) {}
    }

    public class FooProcessor implements ItemProcessor<Foo,Bar>{
        public Bar process(Foo foo) throws Exception {
            //Perform simple transformation, convert a Foo to a Bar
            return new Bar(foo);
        }
    }


//    public class BarProcessor implements ItemProcessor<Bar,Foobar> {
//        public Foobar process(Bar bar) throws Exception {
//            return new Foobar(bar);
//        }
//    }
//
//    public class FoobarWriter implements ItemWriter<Foobar> {
//        public void write(List<? extends Foobar> items) throws Exception {
//            //write items
//
//            stepBuilderFactory.get("").chunk(10).writer(new CompositeItemWriter<>())
//        }
//    }
}
