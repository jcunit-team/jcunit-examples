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
import com.github.dakusui.geophile.mymodel.Record;
import com.github.dakusui.geophile.mymodel.SpatialJoinFilter;
import com.github.dakusui.geophile.mymodel.SpatialObject;
import com.github.dakusui.geophile.mymodel.index.Index;
import com.github.dakusui.geophile.testbase.SpaceProvider;
import com.github.dakusui.geophile.testbase.SpatialObjectProvider;
import com.github.dakusui.geophile.testbase.SpatialObjectSet;
import com.github.dakusui.geophile.testbase.SpatialObjectSetProvider;
import com.github.dakusui.jcunit.framework.TestSuite;
import com.github.dakusui.jcunit.plugins.caengines.Ipo2CoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.constraints.SmartConstraintCheckerImpl;
import com.github.dakusui.jcunit.runners.standard.annotations.Checker;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.runners.standard.annotations.GenerateCoveringArrayWith;
import com.github.dakusui.jcunit.runners.standard.annotations.Generator;
import com.google.common.base.Function;
import com.google.common.collect.Sets;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.Set;

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
public class SpatialJoinWithSpatialObjectTest {
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Print {
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface Execute {
  }

  public static final ActionPrinter.Writer WRITER = ActionPrinter.Writer.Std.OUT;

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

  @FactorField
  public SpatialObjectProvider.Standard queryObjectProvider;

  protected Space space;

  protected Index index;

  protected SpatialIndex<Record> spatialIndex;

  protected SpatialObjectSet spatialObjects;

  protected Record.Builder recordBuilder;

  protected SpatialJoin session;

  protected SpatialObject query;

  @ActionUnit.PerformWith({ Print.class, Execute.class })
  public Iterable<Action> testCases() {
    final TestSuite.Typed<SpatialJoinWithSpatialObjectTest> testSuite = TestSuite.Typed.generate(SpatialJoinWithSpatialObjectTest.class);
    return new AbstractList<Action>() {
      @Override
      public Action get(int index) {
        SpatialJoinWithSpatialObjectTest testObject = testSuite.inject(index);
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

  public static Action setUp(SpatialJoinWithSpatialObjectTest testObject) {
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
            createQuery(testObject)
        ));
  }

  public static Action runTest(final SpatialJoinWithSpatialObjectTest testObject) {
    return named("runTest",
        Actions.<SpatialObject, Iterable<SpatialObject>>test("runQuery")
            .given(new Source<SpatialObject>() {
              @Override
              public SpatialObject apply(Context context) {
                WRITER.writeLine("Given:");
                WRITER.writeLine(format("  query='%s'", testObject.query));
                return testObject.query;
              }
            }).when(new Function<SpatialObject, Iterable<SpatialObject>>() {
                      @Override
                      public Iterable<SpatialObject> apply(SpatialObject input) {
                        try {
                          WRITER.writeLine("When:");
                          WRITER.writeLine(format("  perform query='%s' on '%s'", testObject.query, testObject.spatialIndex));
                          return performQuery(testObject.spatialIndex, testObject.session, input);
                        } catch (IOException | InterruptedException e) {
                          throw ActionException.wrap(e);
                        }
                      }
                    }
        ).then(new Sink<Iterable<SpatialObject>>() {
          @Override
          public void apply(Iterable<SpatialObject> spatialObjects, Context context) {
            Set<SpatialObject> expectation = Sets.newHashSet(testObject.spatialObjectSetProvider.get().getObjectsMatchingWith(testObject.query));
            Set<SpatialObject> actual = Sets.newHashSet(spatialObjects);
            WRITER.writeLine("Then: ");
            WRITER.writeLine(format("  Expectation          : %s", expectation));
            WRITER.writeLine(format("  Actual (deduplicated): %s", expectation));
            assertEquals(
                expectation,
                actual
            );
          }
        }).build());
  }

  private static Action createQuery(final SpatialJoinWithSpatialObjectTest testObject) {
    return simple("createQuery", new Runnable() {
      @Override
      public void run() {
        testObject.query = testObject.queryObjectProvider.get();
      }
    });
  }

  private static Action createSpatialJoinSession(final SpatialJoinWithSpatialObjectTest testObject) {
    return simple("createSpatialJoinSession", new Runnable() {
      @Override
      public void run() {
        testObject.session = SpatialJoin.newSpatialJoin(testObject.duplicates, SpatialJoinFilter.INSTANCE);
      }
    });
  }

  private static Action createRecordBuilder(final SpatialJoinWithSpatialObjectTest testObject) {
    return simple("createRecordBuilder", new Runnable() {
      @Override
      public void run() {
        testObject.recordBuilder = new Record.Builder(testObject.stableRecords);
      }
    });
  }

  private static DataSource.Factory.Base<SpatialObject> spatialObjectsForIndex(final SpatialJoinWithSpatialObjectTest testObject) {
    return new DataSource.Factory.Base<SpatialObject>() {
      @Override
      protected Iterable<SpatialObject> iterable(Context context) {
        return testObject.spatialObjects.forIndex();
      }
    };
  }

  private static Action loadObjects(final SpatialJoinWithSpatialObjectTest testObject) {
    return simple("loadObjects", new Runnable() {
      @Override
      public void run() {
        testObject.spatialObjects = testObject.spatialObjectSetProvider.get();
      }
    });
  }

  private static Action createIndex(final SpatialJoinWithSpatialObjectTest testObject) {
    return simple("createIndex", new Runnable() {
      @Override
      public void run() {
        testObject.index = new Index(testObject.stableRecords);
      }
    });
  }

  private static Action createSpace(final SpatialJoinWithSpatialObjectTest testObject) {
    return simple("createSpace", new Runnable() {
      @Override
      public void run() {
        testObject.space = testObject.spaceProvider.create();
      }
    });
  }

  private static Iterable<SpatialObject> performQuery(
      SpatialIndex<Record> spatialIndex,
      SpatialJoin session,
      SpatialObject query) throws IOException, InterruptedException {
    return toIterable(transform(
        session.iterator(query, spatialIndex),
        new Function<Record, SpatialObject>() {
          @Override
          public SpatialObject apply(Record input) {
            return (SpatialObject) input.spatialObject();
          }
        }));
  }

  private static Action createSpatialIndex(final SpatialJoinWithSpatialObjectTest testObject) {
    return simple("createSpatialIndex", new Runnable() {
      @Override
      public void run() {
        try {
          if (testObject.options == null) {
            testObject.spatialIndex = SpatialIndex.newSpatialIndex(testObject.space, testObject.index);
          } else {
            testObject.spatialIndex = SpatialIndex.newSpatialIndex(testObject.space, testObject.index, testObject.options);
          }
        } catch (IOException | InterruptedException e) {
          throw ActionException.wrap(e);
        }
      }
    });
  }

  private static Sink<SpatialObject> addSpatialObjectToSpatialIndex(final SpatialJoinWithSpatialObjectTest testObject) {
    return new Sink<SpatialObject>() {
      @Override
      public void apply(SpatialObject spatialObject, Context context) {
        try {
          if (SpatialIndex.Options.SINGLE_CELL == testObject.options) {
            testObject.spatialIndex.add(spatialObject, testObject.recordBuilder.with(spatialObject), 1);
          } else {
            testObject.spatialIndex.add(spatialObject, testObject.recordBuilder.with(spatialObject));
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
