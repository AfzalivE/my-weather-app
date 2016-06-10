package rx.plugins;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import rx.Scheduler;
import rx.schedulers.TestScheduler;


public class RxJavaResetRule implements TestRule {
    TestScheduler scheduler;

    public RxJavaResetRule(TestScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                //before: plugins reset, execution and schedulers hook defined
                RxJavaPlugins.getInstance().reset();
                RxJavaPlugins.getInstance().registerSchedulersHook(new SchedulerHook(scheduler));

                base.evaluate();

                //after: clean up
                RxJavaPlugins.getInstance().reset();
            }
        };
    }

    private static class SchedulerHook extends RxJavaSchedulersHook {
        private final TestScheduler scheduler;

        public SchedulerHook(TestScheduler scheduler) {
            this.scheduler = scheduler;
        }

        @Override
        public Scheduler getIOScheduler() {
            return scheduler;
        }

        @Override
        public Scheduler getNewThreadScheduler() {
            return scheduler;
        }

        @Override
        public Scheduler getComputationScheduler() {
            return scheduler;
        }
    }
}
