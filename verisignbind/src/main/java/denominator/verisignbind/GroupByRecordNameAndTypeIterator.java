package denominator.verisignbind;

import static denominator.common.Util.peekingIterator;
import static denominator.verisignbind.VerisignBindResourceRecordSetApi.getRRTypeAndRdata;

import java.util.Iterator;
import java.util.Map;

import denominator.common.PeekingIterator;
import denominator.model.ResourceRecordSet;
import denominator.model.ResourceRecordSet.Builder;
import denominator.verisignbind.VerisignBindAdapters.ResourceRecord;

class GroupByRecordNameAndTypeIterator implements Iterator<ResourceRecordSet<?>> {

  private final PeekingIterator<ResourceRecord> peekingIterator;

  public GroupByRecordNameAndTypeIterator(Iterator<ResourceRecord> sortedIterator) {
    this.peekingIterator = peekingIterator(sortedIterator);
  }

  private static boolean nameAndTypeEquals(ResourceRecord actual, ResourceRecord expected) {
    return actual.getName().equals(expected.getName())
        && actual.getType().equals(expected.getType());
  }

  @Override
  public boolean hasNext() {
    return peekingIterator.hasNext();
  }

  @Override
  public ResourceRecordSet<?> next() {
    ResourceRecord record = peekingIterator.next();
    Builder<Map<String, Object>> builder =
        ResourceRecordSet.builder().name(record.getName()).type(record.getType())
            .ttl(record.getTtl());

    builder.add(getRRTypeAndRdata(record.getType(), record.getRdata()));

    while (hasNext()) {
      ResourceRecord next = peekingIterator.peek();
      if (next == null) {
        continue;
      }
      if (nameAndTypeEquals(next, record)) {
        builder.add(getRRTypeAndRdata(record.getType(), next.getRdata()));
      } else {
        break;
      }
    }
    return builder.build();
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }



}
