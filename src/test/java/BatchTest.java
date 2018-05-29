import batch.BatchApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @version :1.0.0
 * @author: lvJian
 * @time: 2018-05-28 10:31
 * @description :
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = BatchApplication.class)
public class BatchTest {

    @Autowired
    private JobExplorer jobExplorer;

    @Autowired
    private JobRepository jobRepository;


    @Test
    public void testJobExploer(){

        jobExplorer.getJobInstance(1L);

    }
}
