package Modules;

import java.util.List;

/**
 * Created by WINDOWS 8.1 on 12/5/2016.
 */

public interface DirectionFinderListener {
    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<Route> route);
}
