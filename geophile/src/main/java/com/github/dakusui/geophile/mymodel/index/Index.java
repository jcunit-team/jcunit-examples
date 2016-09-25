package com.github.dakusui.geophile.mymodel.index;

import com.github.dakusui.geophile.mymodel.Record;

import java.util.Comparator;

public class Index extends TreeIndex<Record> {
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
    super(COMPARATOR, stableRecords);
  }

  @Override
  public Record newRecord() {
    return new Record.Mutable();
  }
}
