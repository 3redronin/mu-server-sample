package ronin.muserver.sample;

import ronin.muserver.MuHandler;
import ronin.muserver.MuRequest;
import ronin.muserver.MuResponse;

// ok, so this could do with a better name.. decorator?
public class Decoration implements MuHandler {
    public interface Munge { // also needs a better name
        void handle(MuRequest req, MuResponse res);
    }

    final Munge munge;

    private Decoration(Munge munge) {
        this.munge = munge;
    }

    public boolean handle(MuRequest request, MuResponse response) {
        munge.handle(request, response);
        return false;
    }

    public static Decoration decorate(Munge munge) {
        return new Decoration(munge);
    }
}
