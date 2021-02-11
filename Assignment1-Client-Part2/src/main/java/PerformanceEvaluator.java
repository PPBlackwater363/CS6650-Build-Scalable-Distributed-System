import java.util.concurrent.atomic.AtomicInteger;

public class PerformanceEvaluator {
    private AtomicInteger numSuccessfulRequest;
    private AtomicInteger numUnsuccessfulRequest;

    public PerformanceEvaluator(AtomicInteger numSuccessfulRequest, AtomicInteger numUnsuccessfulRequest) {
        this.numSuccessfulRequest = numSuccessfulRequest;
        this.numUnsuccessfulRequest = numUnsuccessfulRequest;
    }

    public AtomicInteger getNumSuccessfulRequest() {
        return numSuccessfulRequest;
    }

    public void setNumSuccessfulRequest(AtomicInteger numSuccessfulRequest) {
        this.numSuccessfulRequest = numSuccessfulRequest;
    }

    public AtomicInteger getNumUnsuccessfulRequest() {
        return numUnsuccessfulRequest;
    }

    public void setNumUnsuccessfulRequest(AtomicInteger numUnsuccessfulRequest) {
        this.numUnsuccessfulRequest = numUnsuccessfulRequest;
    }
}

