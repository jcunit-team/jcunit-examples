package com.github.dakusui.geophile.tests;

import com.geophile.z.Space;
import com.geophile.z.SpatialIndex;
import com.geophile.z.SpatialJoin;
import com.github.dakusui.actionunit.*;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.connectors.Source;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import com.github.dakusui.geophile.jcunit.SubsetLevelsProvider;
import com.github.dakusui.geophile.mymodel.Record;
import com.github.dakusui.geophile.mymodel.SpatialJoinFilter;
import com.github.dakusui.geophile.mymodel.SpatialObject;
import com.github.dakusui.geophile.mymodel.index.Index;
import com.github.dakusui.geophile.testbase.*;
import com.github.dakusui.jcunit.framework.TestSuite;
import com.github.dakusui.jcunit.plugins.caengines.Ipo2CoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.constraints.SmartConstraintCheckerImpl;
import com.github.dakusui.jcunit.runners.standard.TestCaseUtils;
import com.github.dakusui.jcunit.runners.standard.annotations.*;
import com.google.common.base.Function;
import com.google.common.collect.Sets;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;

import static com.github.dakusui.actionunit.Actions.*;
import static com.github.dakusui.actionunit.actions.ForEach.Mode.SEQUENTIALLY;
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
    engine = @Generator(Ipo2CoveringArrayEngine.class),
    checker = @Checker(
        value = SmartConstraintCheckerImpl.class))
public class SpatialJoinTest {
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Print {
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface Execute {
  }

  public static final ActionPrinter.Writer WRITER = ActionPrinter.Writer.Std.OUT;

  @FactorField
  public SpatialJoinOperation operation;

  @FactorField
  public SpaceProvider spaceProvider;

  @FactorField(includeNull = true)
  public SpatialIndex.Options options;

  @FactorField
  public boolean stableRecords;

  @FactorField
  public SpatialJoin.Duplicates duplicates;

  @FactorField
  public SpatialObjectSetProvider.Standard spatialObjectSetProvider;

  @FactorField(
      levelsProvider = SubsetLevelsProvider.PassThrough.class,
      args = { @Value("-1"), @Value("-1"), @Value({ "BOXb", "BOX2", "POINT1", "POINT2" }) })
  public List<String> indexedObjects;

  @FactorField(
      levelsProvider = SubsetLevelsProvider.PassThrough.class,
      args = { @Value("0"), @Value("2"), @Value({ "BOXa", "BOX2", "POINT1", "POINT2", "BOX3", "POINT3" }) })
  public List<String> queryObjects;

  @FactorField
  public SpatialObjectProvider.Standard queryObjectProvider;

  public static class Fixture {
    protected SpatialJoinOperation queryType = SpatialJoinOperation.WITH_SPATIAL_OBJECT;

    protected Space space;

    protected Index index;

    protected SpatialIndex<Record> spatialIndex;

    protected SpatialObjectSet spatialObjects;

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
    final TestSuite.Typed<SpatialJoinTest> testSuite = TestSuite.Typed.generate(SpatialJoinTest.class);
    return new AbstractList<Action>() {
      @Override
      public Action get(int index) {
        SpatialJoinTest testObject = testSuite.inject(index);
        return sequential(
            setUp(testObject),
            runTest(testObject)
        );
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
      WRITER.writeLine("==== Test execution log ====");
      action.accept(runner);
    } finally {
      WRITER.writeLine("==== Test result ====");
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
            computeQueryAndExpectation(testObject)
        ));
  }

  public static Action runTest(final SpatialJoinTest testObject) {
    final Fixture fixture = testObject.fixture;
    return named("runTest",
        Actions.<Fixture, Iterable<SpatialObject>>test("runQuery")
            .given(new Source<Fixture>() {
              @Override
              public Fixture apply(Context context) {
                WRITER.writeLine("Given:");
                WRITER.writeLine(format("  testcase: '%s'", TestCaseUtils.toTestCase(testObject)));
                WRITER.writeLine(format("  fixture : '%s'", fixture));
                return fixture;
              }
            }).when(new Function<Fixture, Iterable<SpatialObject>>() {
                      @Override
                      public Iterable<SpatialObject> apply(Fixture input) {
                        try {
                          WRITER.writeLine("When:");
                          WRITER.writeLine(format("  perform query='%s' on '%s'", testObject.fixture.query, testObject.fixture.spatialIndex));
                          return performQuery(
                              input.queryType,
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
        testObject.fixture.query = Collections.singleton(testObject.queryObjectProvider.get());
        testObject.fixture.expectationForQuery = Sets.newHashSet(
            testObject.spatialObjectSetProvider.get().getObjectsMatchingWith(testObject.fixture.query.iterator().next()));
      }
    });
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
        return testObject.fixture.spatialObjects.forIndex();
      }
    };
  }

  private static Action loadObjects(final SpatialJoinTest testObject) {
    return simple("loadObjects", new Runnable() {
      @Override
      public void run() {
        testObject.fixture.spatialObjects = testObject.spatialObjectSetProvider.get();
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
      SpatialJoinOperation queryType, SpatialIndex<Record> spatialIndex,
      SpatialJoin session,
      Set<SpatialObject> query) throws IOException, InterruptedException {
    if (queryType == SpatialJoinOperation.WITH_SPATIAL_OBJECT) {
      return toIterable(transform(
          session.iterator(query.iterator().next(), spatialIndex),
          new Function<Record, SpatialObject>() {
            @Override
            public SpatialObject apply(Record input) {
              return (SpatialObject) input.spatialObject();
            }
          }));
    } else {
      throw new UnsupportedOperationException(Objects.toString(queryType));
    }
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
}
