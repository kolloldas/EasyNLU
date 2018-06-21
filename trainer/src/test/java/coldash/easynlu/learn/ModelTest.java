package coldash.easynlu.learn;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.File;

import static coldash.easynlu.learn.demo.ReminderLearn.makeReminderModel;
import static org.junit.jupiter.api.Assertions.*;

class ModelTest {
    @AfterAll
    static void cleanUp(){
        File file = new File("test-weights");
        file.delete();
    }

    @Test
    void saveLoad() {

        HParams hparams = HParams.hparams()
                .withLearnRate(0.08f)
                .withL2Penalty(0.01f)
                .set(SVMOptimizer.CORRECT_PROB, 0.4f);

        Dataset d = Dataset.fromText("data/examples-reminders.txt");
        Model m = makeReminderModel();
        Optimizer optimizer = new SVMOptimizer(m, hparams);

        m.train(d, optimizer,30);

        float acc = m.evaluate(d, 0);
        m.saveWeights("test-weights");

        m = makeReminderModel();
        m.loadWeights("test-weights");
        float savedAcc = m.evaluate(d, 0);

        assertEquals(acc, savedAcc, 1e-7);
    }
}