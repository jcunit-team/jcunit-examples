package com.github.dakusui.geophile.mymodel;

import com.geophile.z.index.RecordWithSpatialObject;

public abstract class Record extends RecordWithSpatialObject {
  public static class Immutable extends Record {
    private final int id;

    public Immutable(SpatialObject spatialObject, int id) {
      spatialObject(spatialObject);
      this.id = id;
    }

    @Override
    public int id() {
      return this.id;
    }
  }

  public static class Mutable extends Record {
    private int id;

    public Mutable() {
    }

    public Mutable(SpatialObject spatialObject, int id) {
      spatialObject(spatialObject);
      this.id = id;
    }

    @Override
    public int id() {
      return this.id;
    }
  }

  public static class Builder implements com.geophile.z.Record.Factory<Record> {
    private final boolean stable;
    private SpatialObject spatialObject;
    private int id;

    public Builder(boolean stable) {
      this.stable = stable;
    }

    public Builder with(SpatialObject spatialObject) {
      this.spatialObject = spatialObject;
      return this;
    }

    @Override
    public Record newRecord() {
      if (stable) {
        return new Immutable(this.spatialObject, this.id++);
      } else {
        return new Mutable(this.spatialObject, this.id++);
      }
    }
  }

  @Override
  public void copyTo(com.geophile.z.Record record) {
    if (record instanceof Mutable) {
      ((Mutable) record).id = this.id();
      super.copyTo(record);
      return;
    }
    throw new UnsupportedOperationException("Non mutable recode '" + record.getClass() + ":" + record + "' was given.");
  }

  public abstract int id();

  public interface Filter extends com.geophile.z.Record.Filter<Record> {
    class Factory {
      public static Filter create(final SpatialObject spatialObject) {
        return new Filter() {
          @Override
          public boolean select(Record record) {
            return SpatialJoinFilter.INSTANCE.overlap(record.spatialObject(), spatialObject);
          }
        };
      }
    }
  }
}
