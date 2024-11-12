package interfaces;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public interface TaskDeletionCallback {
    void deleteSelectedTasks(ArrayList<JSONObject> deletionTasks) throws JSONException;
}
