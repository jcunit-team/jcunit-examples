package com.github.dakusui.geophile.tests;

import com.geophile.z.SpatialIndex;
import com.geophile.z.SpatialJoin;
import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.geophile.mymodel.Record;
import com.github.dakusui.geophile.mymodel.SpatialJoinFilter;
import com.github.dakusui.geophile.mymodel.index.Index;
import com.google.common.collect.Iterables;

import java.io.IOException;
import java.util.LinkedList;

import static com.github.dakusui.actionunit.Actions.simple;
import static com.github.dakusui.jcunit.runners.standard.TestCaseUtils.toTestCase;
import static com.google.common.collect.Iterables.transform;
import static java.lang.String.format;

public enum SpatialJoinActions {
  ;

  static Action computeQueryAndExpectation(final SpatialJoinTest testObject) {
    return simple("computeQueryAndExpectation", new Runnable() {
      @Override
      public void run() {
        testObject.fixture.operation = testObject.operation;
        testObject.fixture.query = testObject.operation.computeQuery(testObject);
        testObject.fixture.expectationForQuery = testObject.operation.computeExpectationForQuery(testObject);
      }
    });
  }

  static Action createSpatialJoinSession(final SpatialJoinTest testObject) {
    return simple("createSpatialJoinSession", new Runnable() {
      @Override
      public void run() {
        testObject.fixture.session = SpatialJoin.newSpatialJoin(testObject.duplicates, SpatialJoinFilter.INSTANCE);
      }
    });
  }

  static Action createRecordBuilder(final SpatialJoinTest testObject) {
    return simple("createRecordBuilder", new Runnable() {
      @Override
      public void run() {
        testObject.fixture.recordBuilder = new Record.Builder(testObject.stableRecords);
      }
    });
  }

  static Action printFixture(final SpatialJoinTest testObject) {
    return simple("printFixture", new Runnable() {
      @Override
      public void run() {
        SpatialJoinTest.WRITER.writeLine("Given:");
        SpatialJoinTest.WRITER.writeLine(format("  testcase: '%s'", toTestCase(testObject)));
        SpatialJoinTest.WRITER.writeLine(format("  fixture : '%s'", testObject.fixture));
      }
    });
  }

  static Action loadObjects(final SpatialJoinTest testObject) {
    return simple("loadObjects", new Runnable() {
      @Override
      public void run() {
        testObject.fixture.spatialObjects = new LinkedList<>();
        Iterables.addAll(testObject.fixture.spatialObjects, transform(testObject.indexedObjects,
            SpatialJoinTest.spatialObjectConcretizer(testObject)));
      }
    });
  }

  static Action createIndex(final SpatialJoinTest testObject) {
    return simple("createIndex", new Runnable() {
      @Override
      public void run() {
        testObject.fixture.index = new Index(testObject.stableRecords);
      }
    });
  }

  static Action createSpace(final SpatialJoinTest testObject) {
    return simple("createSpace", new Runnable() {
      @Override
      public void run() {
        testObject.fixture.space = testObject.spaceProvider.create();
      }
    });
  }

  static Action createSpatialIndex(final SpatialJoinTest testObject) {
    return simple("createSpatialIndex", new Runnable() {
      @Override
      public void run() {
        try {
          if (testObject.options == null) {
            testObject.fixture.spatialIndex = SpatialIndex.newSpatialIndex(testObject.fixture.space, testObject.fixture.index);
          } else {
            testObject.fixture.spatialIndex = SpatialIndex.newSpatialIndex(testObject.fixture.space, testObject.fixture.index, testObject.options);
          }
        } catch (IOException | InterruptedException e) {
          throw ActionException.wrap(e);
        }
      }
    });
  }
}
