package hack.suggesttag;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.telecom.Call;

import com.clarifai.api.ClarifaiClient;
import com.clarifai.api.RecognitionRequest;
import com.clarifai.api.RecognitionResult;
import com.clarifai.api.Tag;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class ImageTags {
    private static final String APP_ID = "UW1PLB5CxBUfKHyYkLuQTfFO0LQAIDkwpfO-g7Sy";
    private static final String APP_SECRET = "_HtY3kiLnQfWroINxbiAbca7d-4j4UxR9WCBTXtz";

    public static void getTags(final String path, final Callback cb){
        new AsyncTask<Object, Object, List<RecognitionResult>>() {
            @Override
            protected List<RecognitionResult> doInBackground(Object[] objects) {
                ClarifaiClient clarifai = new ClarifaiClient(APP_ID, APP_SECRET);
                List<RecognitionResult> results =
                        clarifai.recognize(new RecognitionRequest(new File(path)));

                return results;
            }

            @Override
            protected void onPostExecute(List<RecognitionResult> results) {
                for (Tag tag : results.get(0).getTags()) {
                    System.out.println(tag.getName() + ": " + tag.getProbability());
                }
                cb.onResult(results);
            }
        }.execute();
    }

    public interface Callback {
        void onResult(List<RecognitionResult> results);
    }

}
