package com.github.dakusui.geophile.tests;

import com.geophile.z.Pair;
import com.geophile.z.Space;
import com.geophile.z.SpatialIndex;
import com.geophile.z.SpatialJoin;
import com.github.dakusui.actionunit.*;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.connectors.Source;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import com.github.dakusui.geophile.jcunit.Concretizer;
import com.github.dakusui.geophile.jcunit.SubsetLevels;
import com.github.dakusui.geophile.mymodel.Record;
import com.github.dakusui.geophile.mymodel.SpatialJoinFilter;
import com.github.dakusui.geophile.mymodel.SpatialObject;
import com.github.dakusui.geophile.mymodel.index.Index;
import com.github.dakusui.geophile.testbase.SpaceProvider;
import com.github.dakusui.jcunit.coverage.CombinatorialMetrics;
import com.github.dakusui.jcunit.framework.TestSuite;
import com.github.dakusui.jcunit.plugins.caengines.IpoGcCoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.constraints.SmartConstraintCheckerImpl;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.*;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;

import static com.github.dakusui.actionunit.Actions.*;
import static com.github.dakusui.actionunit.actions.ForEach.Mode.SEQUENTIALLY;
import static com.github.dakusui.jcunit.runners.standard.TestCaseUtils.toTestCase;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Iterators.toArray;
import static com.google.common.collect.Iterators.transform;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

/**
 * This test verifies geophile's simple query use case.
 * The scenario is as follows.
 * <p/>
 * <ol>
 * <li>Prepare space and index.</li>
 * <li>Perform a spatial join.
 * <ul>
 * <li>Add objects to spatial index.</li>
 * </li>Create a spatial join session.</li>
 * </ul>
 * </li>
 * <li>Perform a spatial join with a spatial object.</li>
 * </ol>
 */
@RunWith(ActionUnit.class)
@GenerateCoveringArrayWith(
    engine = @Generator(IpoGcCoveringArrayEngine.class),
    checker = @Checker(value = SmartConstraintCheckerImpl.class),
    reporters = @Reporter(value = CombinatorialMetrics.class))
public class SpatialJoinTest {
  public static final SpatialJoinFilter FILTER = new SpatialJoinFilter();
  @SuppressWarnings("unused")
  public static final SpatialObject     BOX1   = new SpatialObject.Box(10, 10, 20, 20);
  @SuppressWarnings("unused")
  public static final SpatialObject     BOX2   = new SpatialObject.Box(15, 15, 25, 25);
  @SuppressWarnings("unused")
  public static final SpatialObject     BOX3   = new SpatialObject.Box(25, 10, 35, 20);
  @SuppressWarnings("unused")
  public static final SpatialObject     POINT1 = new SpatialObject.Point(18, 13);
  @SuppressWarnings("unused")
  public static final SpatialObject     POINT2 = new SpatialObject.Point(18, 18);
  @SuppressWarnings("unused")
  public static final SpatialObject     POINT3 = new SpatialObject.Point(40, 45);

  @FactorField
  public OperationType operation;

  @FactorField
  public SpaceProvider spaceProvider;// = SpaceProvider.NORMAL;

  @FactorField(includeNull = true)
  public SpatialIndex.Options options;

  @FactorField
  public boolean stableRecords;

  @FactorField
  public SpatialJoin.Duplicates duplicates;

  @FactorField(
      levelsProvider = SubsetLevels.FromFields.class,
      args = {
          @Value({ "BOX1", "BOX2", "POINT1", "POINT2" }) })
  public List<Concretizer<SpatialJoinTest, SpatialObject>> indexedObjects;

  @FactorField(
      levelsProvider = SubsetLevels.FromFields.class,
      args = {
          @Value({ "BOX1", "BOX2", "POINT1", "POINT2", "BOX3", "POINT3" }),
          @Value("0"),
          @Value("2"),
          @Value("NOT_ORDERED"),
          @Value("false") })
  public List<Concretizer<SpatialJoinTest, SpatialObject>> queryObjects;

  @Condition(constraint = true)
  @Uses({ "operation", "queryObjects" })
  public boolean checkQueryObjects() {
    if (operation == OperationType.WITH_ITSELF) {
      return queryObjects == null;
    }
    if (this.operation == OperationType.WITH_SPATIAL_OBJECT) {
      return this.queryObjects != null && this.queryObjects.size() == 1;
    }
    return operation != null && queryObjects != null;
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface Print {
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface Execute {
  }

  public static final ActionPrinter.Writer WRITER = ActionPrinter.Writer.Std.OUT;

  public static class Fixture {
    protected OperationType operation;

    protected Space space;

    protected Index index;

    protected SpatialIndex<Record> spatialIndex;

    protected List<SpatialObject> spatialObjects;

    protected Record.Builder recordBuilder;

    protected SpatialJoin session;

    protected Set<SpatialObject> query;

    protected Set<SpatialObject> expectationForQuery;

    public String toString() {
      return format("(%s,%s,%s,%s)", space, index, spatialIndex, spatialObjects);
    }
  }

  protected Fixture fixture = new Fixture();

  @ActionUnit.PerformWith({ Print.class, Execute.class })
  public Iterable<Action> testCases() {
    final TestSuite.Typed<SpatialJoinTest> testSuite = TestSuite.Typed.generate(
        SpatialJoinTest.class,
        new JCUnit.Engine.Config(true, true, false));
    return new AbstractList<Action>() {
      @Override
      public Action get(int index) {
        SpatialJoinTest testObject = testSuite.inject(index);
        return named(format("%s[%s]", testSuite.get(index).getCategory(), index), sequential(
            setUp(testObject),
            runTest(testObject)
        ));
      }

      @Override
      public int size() {
        return testSuite.size();
      }
    };
  }

  @Print
  public void printTestCase(Action action) {
    WRITER.writeLine("==== Test plan ====");
    action.accept(ActionPrinter.Factory.create(ActionPrinter.Writer.Std.OUT));
  }

  @Execute
  public void execute(Action action) {
    ActionRunner.WithResult runner = new ActionRunner.WithResult();
    try {
      WRITER.writeLine("==== Test execution phase ====");
      action.accept(runner);
    } finally {
      WRITER.writeLine("==== Test closing phase  ====");
      action.accept(runner.createPrinter());
    }
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
            computeQueryAndExpectation(testObject),
            printFixture(testObject)
        ));
  }

  public static Action runTest(final SpatialJoinTest testObject) {
    final Fixture fixture = testObject.fixture;
    return named("runTest",
        Actions.<Fixture, Iterable<SpatialObject>>test("runQuery")
            .given(new Source<Fixture>() {
              @Override
              public Fixture apply(Context context) {
                return fixture;
              }
            }).when(new Function<Fixture, Iterable<SpatialObject>>() {
                      @Override
                      public Iterable<SpatialObject> apply(Fixture input) {
                        try {
                          WRITER.writeLine("When:");
                          WRITER.writeLine(format("  perform query='%s' on '%s'", testObject.fixture.query, testObject.fixture.spatialIndex));
                          return performQuery(
                              input.operation,
                              input.spatialIndex,
                              input.session,
                              input.query);
                        } catch (IOException | InterruptedException e) {
                          throw ActionException.wrap(e);
                        }
                      }
                    }
        ).then(new Sink<Iterable<SpatialObject>>() {
          @Override
          public void apply(Iterable<SpatialObject> input, Context context) {
            final Set<SpatialObject> expectation = fixture.expectationForQuery;
            Set<SpatialObject> actual = Sets.newHashSet(input);
            WRITER.writeLine("Then: ");
            WRITER.writeLine(format("  Expectation          : %s", expectation));
            WRITER.writeLine(format("  Actual (deduplicated): %s", actual));
            assertEquals(
                expectation,
                actual
            );
          }
        }).build());
  }

  private static Action computeQueryAndExpectation(final SpatialJoinTest testObject) {
    return simple("computeQueryAndExpectation", new Runnable() {
      @Override
      public void run() {
        testObject.fixture.operation = testObject.operation;
        if (testObject.operation == OperationType.WITH_SPATIAL_OBJECT) {
          //// FIXME: 9/29/16
          testObject.fixture.query = Collections.singleton(testObject.queryObjects.get(0).concretize(testObject));
          testObject.fixture.expectationForQuery = Sets.newHashSet(
              filter(
                  transform(
                      testObject.indexedObjects,
                      spatialObjectConcretizer(testObject)
                  ), new Predicate<SpatialObject>() {
                    @Override
                    public boolean apply(SpatialObject input) {
                      return FILTER.overlap(input, testObject.fixture.query.iterator().next());
                    }
                  }).iterator());
        } else if (testObject.operation == OperationType.WITH_ANOTHER_INDEX) {
          //// FIXME: 9/29/16
          testObject.fixture.query = Sets.newHashSet(transform(testObject.queryObjects, spatialObjectConcretizer(testObject)));
          testObject.fixture.expectationForQuery = Sets.newHashSet(
              filter(
                  transform(
                      testObject.indexedObjects,
                      spatialObjectConcretizer(testObject)
                  ), new Predicate<SpatialObject>() {
                    @Override
                    public boolean apply(SpatialObject input) {
                      for (SpatialObject each : testObject.fixture.query) {
                        if (FILTER.overlap(input, each))
                          return true;
                      }
                      return false;
                    }
                  }).iterator());
        } else if (testObject.operation == OperationType.WITH_ITSELF) {
          //// FIXME: 9/29/16
          final Set<SpatialObject> indexedObjects = Sets.newHashSet(transform(
              testObject.indexedObjects,
              spatialObjectConcretizer(testObject)));
          testObject.fixture.expectationForQuery = Sets.newHashSet(
              filter(
                  transform(
                      testObject.indexedObjects,
                      spatialObjectConcretizer(testObject)
                  ), new Predicate<SpatialObject>() {
                    @Override
                    public boolean apply(SpatialObject input) {
                      for (SpatialObject each : indexedObjects) {
                        if (FILTER.overlap(input, each))
                          return true;
                      }
                      return false;
                    }
                  }).iterator());
        } else {
          throw new UnsupportedOperationException(format("Unsupported operation '%s'", testObject.fixture.operation));
        }
      }
    });
  }

  private static Function<Concretizer<SpatialJoinTest, SpatialObject>, SpatialObject> spatialObjectConcretizer(final SpatialJoinTest testObject) {
    return new Function<Concretizer<SpatialJoinTest, SpatialObject>, SpatialObject>() {
      @Override
      public SpatialObject apply(Concretizer<SpatialJoinTest, SpatialObject> input) {
        return input.concretize(testObject);
      }
    };
  }

  private static Action createSpatialJoinSession(final SpatialJoinTest testObject) {
    return simple("createSpatialJoinSession", new Runnable() {
      @Override
      public void run() {
        testObject.fixture.session = SpatialJoin.newSpatialJoin(testObject.duplicates, SpatialJoinFilter.INSTANCE);
      }
    });
  }

  private static Action createRecordBuilder(final SpatialJoinTest testObject) {
    return simple("createRecordBuilder", new Runnable() {
      @Override
      public void run() {
        testObject.fixture.recordBuilder = new Record.Builder(testObject.stableRecords);
      }
    });
  }

  private static DataSource.Factory.Base<SpatialObject> spatialObjectsForIndex(final SpatialJoinTest testObject) {
    return new DataSource.Factory.Base<SpatialObject>() {
      @Override
      protected Iterable<SpatialObject> iterable(Context context) {
        return testObject.fixture.spatialObjects;
      }
    };
  }

  private static Action printFixture(final SpatialJoinTest testObject) {
    return simple("printFixture", new Runnable() {
      @Override
      public void run() {
        WRITER.writeLine("Given:");
        WRITER.writeLine(format("  testcase: '%s'", toTestCase(testObject)));
        WRITER.writeLine(format("  fixture : '%s'", testObject.fixture));
      }
    });
  }

  private static Action loadObjects(final SpatialJoinTest testObject) {
    return simple("loadObjects", new Runnable() {
      @Override
      public void run() {
        testObject.fixture.spatialObjects = new LinkedList<>();
        Iterables.addAll(testObject.fixture.spatialObjects, transform(testObject.indexedObjects,
            spatialObjectConcretizer(testObject)));
      }
    });
  }

  private static Action createIndex(final SpatialJoinTest testObject) {
    return simple("createIndex", new Runnable() {
      @Override
      public void run() {
        testObject.fixture.index = new Index(testObject.stableRecords);
      }
    });
  }

  private static Action createSpace(final SpatialJoinTest testObject) {
    return simple("createSpace", new Runnable() {
      @Override
      public void run() {
        testObject.fixture.space = testObject.spaceProvider.create();
      }
    });
  }

  private static Iterable<SpatialObject> performQuery(
      OperationType queryType, SpatialIndex<Record> spatialIndex,
      SpatialJoin session,
      Set<SpatialObject> query) throws IOException, InterruptedException {
    if (queryType == OperationType.WITH_SPATIAL_OBJECT) {
      return toIterable(transform(
          session.iterator(query.iterator().next(), spatialIndex),
          recordToSpatialObject()));
    } else if (queryType == OperationType.WITH_ANOTHER_INDEX) {
      return toIterable(Iterators.transform(
          session.iterator(buildSpatialIndexFromSpatialObjectSet(spatialIndex.space(), query), spatialIndex), rightSide()));
    } else if (queryType == OperationType.WITH_ITSELF) {
      return toIterable(transform(
          session.iterator(spatialIndex, spatialIndex),
          rightSide()));
    } else {
      throw new UnsupportedOperationException(Objects.toString(queryType));
    }
  }

  private static SpatialIndex<? extends com.geophile.z.Record> buildSpatialIndexFromSpatialObjectSet(Space space, Set<SpatialObject> query) throws IOException, InterruptedException {
    Index index = new Index(true);
    int i = 0;
    for (SpatialObject each : query) {
      index.add(new Record.Immutable(each, i++));
    }
    return SpatialIndex.newSpatialIndex(space, index);
  }

  private static Function<? super Pair<? extends com.geophile.z.Record, ? extends com.geophile.z.Record>, SpatialObject> rightSide() {
    return new Function<Pair<? extends com.geophile.z.Record, ? extends com.geophile.z.Record>, SpatialObject>() {
      @Override
      public SpatialObject apply(Pair<? extends com.geophile.z.Record, ? extends com.geophile.z.Record> input) {
        return (SpatialObject) ((Record)input.right()).spatialObject();
      }
    };
  }

  private static Function<Record, SpatialObject> recordToSpatialObject() {
    return new Function<Record, SpatialObject>() {
      @Override
      public SpatialObject apply(Record input) {
        return (SpatialObject) input.spatialObject();
      }
    };
  }

  private static Action createSpatialIndex(final SpatialJoinTest testObject) {
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

  private static Sink<SpatialObject> addSpatialObjectToSpatialIndex(final SpatialJoinTest testObject) {
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

  private static Iterable<SpatialObject> toIterable(Iterator<SpatialObject> iterator) {
    return asList(toArray(iterator, SpatialObject.class));
  }

  public enum OperationType {
    WITH_SPATIAL_OBJECT,
    WITH_ANOTHER_INDEX,
    WITH_ITSELF
  }
}
