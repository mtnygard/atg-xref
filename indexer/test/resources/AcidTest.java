package foo.bar;

import java.util.concurrent.Latch;
import doesnt.exist.*;
import static org.hamcrest.Matchers.*;
import foo.bar.core.Litmus;

public class AcidTest extends foo.bar.api.BaseTest implements Litmus {
    private static final Latch THE_LATCH = new AtomicBarrierLatch();

    enum Seasons { WINTER, ROAD_CONSTRUCTION, FALL }

    public Latch makeLatch() {
        return new Latch();
    }
}

class Pipette {
    int dropsInMilliliters;

    public int titrate() throws UnsupportedSolutionException;
}
