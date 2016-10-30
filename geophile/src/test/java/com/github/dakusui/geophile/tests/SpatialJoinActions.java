package com.github.dakusui.geophile.tests;

import com.geophile.z.SpatialIndex;
import com.geophile.z.SpatialJoin;
import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.Actions;
import com.github.dakusui.actionunit.Context;
import com.github.dakusui.actionunit.DataSource;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.connectors.Source;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.geophile.jcunit.Concretizer;
import com.github.dakusui.geophile.mymodel.Record;
import com.github.dakusui.geophile.mymodel.SpatialJoinFilter;
import com.github.dakusui.geophile.mymodel.SpatialObject;
import com.github.dakusui.geophile.mymodel.index.Index;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Set;

import static com.github.dakusui.actionunit.Actions.*;
import static com.github.dakusui.actionunit.actions.ForEach.Mode.SEQUENTIALLY;
import static com.github.dakusui.jcunit.runners.standard.TestCaseUtils.toTestCase;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;
import static org.junit.Assert.assertEquals;

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

  public static Action setUp(SpatialJoinTest testObject) {
    return named("setUp",
        sequential(
            loadObjects(testObject),
            createSpace(testObject),
            createIndex(testObject),
            createSpatialIndex(testObject),
            createRecordBuilder(testObject),
            foreach(
                spatialObjectsForIndex(testObject),
                SEQUENTIALLY,
                tag(0),
                addSpatialObjectToSpatialIndex(testObject)
            ),
            createSpatialJoinSession(testObject),
            computeQueryAndExpectation(testObject)
        ));
  }

  public static Action perform(final SpatialJoinTest testObject) {
    final SpatialJoinFixture fixture = testObject.fixture;
    return named("perform",
        sequential(
            printFixture(testObject),
            Actions.<SpatialJoinFixture, Iterable<SpatialObject>>test("run")
                .given(new Source<SpatialJoinFixture>() {
                  @Override
                  public SpatialJoinFixture apply(Context context) {
                    return fixture;
                  }
                }).when(new Function<SpatialJoinFixture, Iterable<SpatialObject>>() {
                          @Override
                          public Iterable<SpatialObject> apply(SpatialJoinFixture input) {
                            try {
                              SpatialJoinTest.WRITER.writeLine("When:");
                              Iterable<Concretizer<SpatialJoinTest, SpatialObject>> query =
                                  testObject.operation == SpatialJoinTest.OperationType.WITH_ITSELF ?
                                      testObject.indexedObjects :
                                      testObject.queryObjects;

                              SpatialJoinTest.WRITER.writeLine(format("  perform operation(%s)='%s' on '%s'",
                                  testObject.fixture.operation,
                                  transform(query, new Function<Concretizer<SpatialJoinTest, SpatialObject>, Object>() {
                                    @Override
                                    public Object apply(Concretizer<SpatialJoinTest, SpatialObject> input) {
                                      return input.concretize(testObject);
                                    }
                                  }),
                                  testObject.fixture.spatialIndex));
                              return input.operation.perform(testObject);
                            } catch (IOException | InterruptedException e) {
                              throw ActionException.wrap(e);
                            }
                          }
                        }
            ).then(new Sink<Iterable<SpatialObject>>() {
              @Override
              public void apply(Iterable<SpatialObject> input, Context context) {
                final Set<SpatialObject> expectation = fixture.expectationForQuery;
                Set<SpatialObject> actual = newHashSet(input);
                SpatialJoinTest.WRITER.writeLine("Then: ");
                SpatialJoinTest.WRITER.writeLine(format("  Expectation          : %s", expectation));
                SpatialJoinTest.WRITER.writeLine(format("  Actual (deduplicated): %s", actual));
                assertEquals(
                    expectation,
                    actual
                );
              }
            }).build()));
  }

  static DataSource.Factory.Base<SpatialObject> spatialObjectsForIndex(final SpatialJoinTest testObject) {
    return new DataSource.Factory.Base<SpatialObject>() {
      @Override
      protected Iterable<SpatialObject> iterable(Context context) {
        return testObject.fixture.spatialObjects;
      }
    };
  }

  static Sink<SpatialObject> addSpatialObjectToSpatialIndex(final SpatialJoinTest testObject) {
    return new Sink<SpatialObject>() {
      @Override
      public void apply(SpatialObject spatialObject, Context context) {
        try {
          if (SpatialIndex.Options.SINGLE_CELL == testObject.options) {
            testObject.fixture.spatialIndex.add(spatialObject, testObject.fixture.recordBuilder.with(spatialObject), 1);
          } else {
            testObject.fixture.spatialIndex.add(spatialObject, testObject.fixture.recordBuilder.with(spatialObject));
          }
        } catch (IOException | InterruptedException e) {
          throw ActionException.wrap(e);
        }
      }

      @Override
      public String toString() {
        return format("addSpatialObjectToSpatialIndex(%s)", testObject);
      }
    };
  }
}
