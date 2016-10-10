package com.github.dakusui.geophile.mymodel.index;

import com.geophile.z.Cursor;
import com.geophile.z.DuplicateRecordException;
import com.github.dakusui.geophile.mymodel.Record;

import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

public class Index extends com.geophile.z.Index<Record> {
  private static final Comparator<Record> COMPARATOR =
          new Comparator<Record>() {
              @Override
              public int compare(Record r, Record s) {
                  return
                          r.z() < s.z() ?
                                  -1 :
                                  r.z() > s.z() ?
                                          1 :
                                          r.id() < s.id() ?
                                                  -1 :
                                                  r.id() > s.id() ?
                                                          1 :
                                                          0;
              }
          };

  public Index(boolean stableRecords) {
    this(COMPARATOR, stableRecords);
  }

  // Object interface

  @Override
  public String toString() {
    return name;
  }

  // Index interface

  @Override
  public void add(Record record) {
    Record copy;
    if (stableRecords) {
      copy = record;
    } else {
      copy = newRecord();
      record.copyTo(copy);
    }
    if (!tree.add(copy)) {
      throw new DuplicateRecordException(copy);
    }
  }


  @Override
  public boolean remove(long z, com.geophile.z.Record.Filter<Record> filter) {
    boolean foundRecord = false;
    boolean zMatch = true;
    Iterator<Record> iterator = tree.tailSet(key(z)).iterator();
    while (zMatch && iterator.hasNext() && !foundRecord) {
      Record record = iterator.next();
      if (record.z() == z) {
        foundRecord = filter.select(record);
      } else {
        zMatch = false;
      }
    }
    if (foundRecord) {
      iterator.remove();
    }
    return foundRecord;
  }

  @Override
  public Cursor<Record> cursor() {
    return new IndexCursor(this);
  }

  @Override
  public Record newRecord() {
    return new Record.Mutable();
  }

  @Override
  public boolean blindUpdates() {
    return false;
  }

  @Override
  public boolean stableRecords() {
    return stableRecords;
  }

  // TreeIndex

  public Index(Comparator<Record> recordComparator, boolean stableRecords) {
    this.tree = new TreeSet<>(recordComparator);
    this.stableRecords = stableRecords;
  }

  // For use by this package

  TreeSet<Record> tree() {
    return tree;
  }

  // For use by this class

  private Record key(long z) {
    Record keyRecord = newRecord();
    keyRecord.z(z);
    return keyRecord;
  }

  // Class state

  private static final AtomicInteger idGenerator = new AtomicInteger(0);

  // Object state

  private final String name = String.format("TreeIndex(%s)", idGenerator.getAndIncrement());
  private final TreeSet<Record> tree;
  private final boolean stableRecords;
}
