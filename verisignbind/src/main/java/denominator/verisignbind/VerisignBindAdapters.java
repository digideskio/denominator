package denominator.verisignbind;

import static com.google.gson.stream.JsonToken.NULL;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import denominator.model.Zone;

final class VerisignBindAdapters {

  private static final Comparator<Object> TO_STRING_COMPARATOR = new Comparator<Object>() {
    @Override
    public int compare(Object left, Object right) {
      return left.toString().compareTo(right.toString());
    }
  };


  @SuppressWarnings({"unchecked", "hiding"})
  private static <X> Comparator<X> toStringComparator() {
    return Comparator.class.cast(TO_STRING_COMPARATOR);
  }

  static class ZoneListAdapter extends ListAdapter<Zone> {

    @Override
    protected String jsonKey() {
      return "zones";
    }

    protected Zone build(JsonReader reader) throws IOException {
      String name = null, id = null, email = null;
      int ttl = -1;
      while (reader.hasNext()) {
        String nextName = reader.nextName();
        if (nextName.equals("id")) {
          id = reader.nextString();
        } else if (nextName.equals("name")) {
          name = reader.nextString();
        } else if (nextName.equals("ttl")) {
          ttl = reader.nextInt();
        } else if (nextName.equals("email")) {
          email = reader.nextString();
        } else {
          reader.skipValue();
        }
      }

      if (name == null) {
        return null;
      }
      return Zone.create(id, name, ttl, email);
    }
  }

  static class ResourceRecordListAdapter extends ListAdapter<ResourceRecord> {

    @Override
    protected String jsonKey() {
      return "records";
    }

    protected ResourceRecord build(JsonReader reader) throws IOException {
      ResourceRecord record = new ResourceRecord();
      while (reader.hasNext()) {
        String key = reader.nextName();
        if (key.equals("id")) {
          record.id = reader.nextString();
        } else if (key.equals("name")) {
          record.name = reader.nextString();
        } else if (key.equals("type")) {
          record.type = reader.nextString();
        } else if (key.equals("ttl") && reader.peek() != NULL) {
          record.ttl = reader.nextInt();
        } else if (key.equals("rdata")) {
          record.rdata = reader.nextString();
        } else {
          reader.skipValue();
        }
      }

      if (record.name == null) {
        return null;
      }
      return record;
    }
  }

  @SuppressWarnings("hiding")
  static abstract class ListAdapter<X> extends TypeAdapter<List<X>> {
    protected abstract String jsonKey();

    protected abstract X build(JsonReader reader) throws IOException;

    @Override
    public List<X> read(JsonReader reader) throws IOException {
      List<X> elements = new LinkedList<X>();
      reader.beginArray();
      while (reader.hasNext()) {
        reader.beginObject();
        X x = build(reader);
        if (x != null) {
          elements.add(x);
        }
        reader.endObject();
      }
      reader.endArray();

      Collections.sort(elements, toStringComparator());
      return elements;
    }

    @Override
    public void write(JsonWriter out, List<X> value) throws IOException {
      throw new UnsupportedOperationException();
    }
  }

  static class ResourceRecord {
    private String id;
    private String name;
    private String type;
    private String rdata;
    private Integer ttl;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public String getRdata() {
      return rdata;
    }

    public void setRdata(String rdata) {
      this.rdata = rdata;
    }

    public Integer getTtl() {
      return ttl;
    }

    public void setTtl(Integer ttl) {
      this.ttl = ttl;
    }

    @Override
    public String toString() {
      return String.format("rr[id=%s name=%s type=%s rdata=\"%s\" ttl=%d]", id, name, type, rdata,
          ttl);
    }
  }
}
