package denominator.verisignbind;

import static denominator.common.Util.peekingIterator;
import static denominator.verisignbind.VerisignBindResourceRecordSetApi.getRRTypeAndRdata;

import java.util.Iterator;
import java.util.Map;

import denominator.common.PeekingIterator;
import denominator.model.ResourceRecordSet;
import denominator.model.ResourceRecordSet.Builder;
import denominator.verisignbind.VerisignBindAdapters.ResourceRecord;

class RecordIterator implements Iterator<ResourceRecordSet<?>> {

  private final PeekingIterator<ResourceRecord> peekingIterator;

  public RecordIterator(Iterator<ResourceRecord> iterator) {
    this.peekingIterator = peekingIterator(iterator);
  }

  @Override
  public boolean hasNext() {
    return peekingIterator.hasNext();
  }

  @Override
  public ResourceRecordSet<?> next() {
    ResourceRecord record = peekingIterator.next();
    Builder<Map<String, Object>> builder = ResourceRecordSet.builder()
        .name(noFinalDot(record.getName())).type(record.getType()).ttl(record.getTtl());

    builder.add(getRRTypeAndRdata(record.getType(), record.getRdata()));

    while (hasNextWithNameAndType(record)) {
      ResourceRecord next = peekingIterator.next();
      builder.add(getRRTypeAndRdata(record.getType(), next.getRdata()));
    }

    return builder.build();
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  private boolean hasNextWithNameAndType(ResourceRecord prev) {
    ResourceRecord next = peekingIterator.hasNext() ? peekingIterator.peek() : null;

    return next != null && nameAndTypeEquals(prev, next);
  }

  private static boolean nameAndTypeEquals(ResourceRecord actual, ResourceRecord expected) {
    return actual.getName().equals(expected.getName())
        && actual.getType().equals(expected.getType());
  }

  private static String noFinalDot(String qname) {
    return qname.endsWith(".") ? qname.substring(0, qname.length() - 1) : qname;
  }
}
