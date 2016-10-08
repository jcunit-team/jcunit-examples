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

  public abstract int id();

  @Override
  public void copyTo(com.geophile.z.Record record) {
    if (record instanceof Mutable) {
      ((Mutable) record).id = this.id();
      super.copyTo(record);
      return;
    }
    throw new UnsupportedOperationException("Non mutable recode '" + record.getClass() + ":" + record + "' was given.");
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
}
