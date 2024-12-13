package hooks;
import br.com.dimensa.report.CucumberReport;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventHandler;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestRunFinished;

public class TestEventHandlerPlugin implements ConcurrentEventListener{

	@Override
	public void setEventPublisher(EventPublisher publisher) {
		publisher.registerHandlerFor(TestRunFinished.class, teardown);
	}
	
	private EventHandler<TestRunFinished> teardown = event -> {
		CucumberReport.generate();
	};
}
