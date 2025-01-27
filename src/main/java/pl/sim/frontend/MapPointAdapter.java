package pl.sim.frontend;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.gluonhq.maps.MapPoint;

import java.io.IOException;

public class MapPointAdapter extends TypeAdapter<MapPoint> {
    @Override
    public void write(JsonWriter out, MapPoint mapPoint) throws IOException {
        out.beginObject();
        out.name("latitude").value(mapPoint.getLatitude());
        out.name("longitude").value(mapPoint.getLongitude());
        out.endObject();
    }

    @Override
    public MapPoint read(JsonReader in) throws IOException {
        double latitude = 0;
        double longitude = 0;
        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();
            if (name.equals("latitude")) {
                latitude = in.nextDouble();
            } else if (name.equals("longitude")) {
                longitude = in.nextDouble();
            }
        }
        in.endObject();
        return new MapPoint(latitude, longitude);
    }
}
