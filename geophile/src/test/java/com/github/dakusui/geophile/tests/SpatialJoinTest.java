package com.github.dakusui.geophile.tests;

import com.geophile.z.Pair;
import com.geophile.z.Space;
import com.geophile.z.SpatialIndex;
import com.geophile.z.SpatialJoin;
import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.ActionUnit;
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
import com.github.dakusui.jcunit.plugins.caengines.StandardCoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.constraints.SmartConstraintCheckerImpl;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.*;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;

import static com.github.dakusui.actionunit.Actions.named;
import static com.github.dakusui.actionunit.Actions.sequential;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Iterators.toArray;
import static com.google.common.collect.Iterators.transform;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;
import static java.util.Arrays.asList;

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
    engine = @Generator(StandardCoveringArrayEngine.class),
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
  public SpaceProvider spaceProvider;

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
          @Value("true") })
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
    if (this.operation == OperationType.WITH_SPATIAL_OBJECT_AFTER_REMOVAL) {
      return this.queryObjects != null && this.queryObjects.size() == 1;// && this.queryObjects.get(0).toString().contains("POINT");
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

  protected SpatialJoinFixture fixture = new SpatialJoinFixture();

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
            SpatialJoinActions.setUp(testObject),
            SpatialJoinActions.perform(testObject)
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

  static Function<Concretizer<SpatialJoinTest, SpatialObject>, SpatialObject> spatialObjectConcretizer(final SpatialJoinTest testObject) {
    return new Function<Concretizer<SpatialJoinTest, SpatialObject>, SpatialObject>() {
      @Override
      public SpatialObject apply(Concretizer<SpatialJoinTest, SpatialObject> input) {
        return input.concretize(testObject);
      }
    };
  }

  private static boolean performRemoval(
      SpatialIndex<Record> spatialIndex,
      SpatialObject removeRequest) throws IOException, InterruptedException {
    return spatialIndex.remove(removeRequest, Record.Filter.Factory.create(removeRequest));
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
        return (SpatialObject) ((Record) input.right()).spatialObject();
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

  private static Iterable<SpatialObject> toIterable(Iterator<SpatialObject> iterator) {
    return asList(toArray(iterator, SpatialObject.class));
  }

  @SuppressWarnings("unused")
  public enum OperationType {
    WITH_SPATIAL_OBJECT {
      @Override
      Set<SpatialObject> computeQuery(SpatialJoinTest testObject) {
        return Collections.singleton(testObject.queryObjects.get(0).concretize(testObject));
      }

      @Override
      Set<SpatialObject> computeExpectationForQuery(SpatialJoinTest testObject) {
        return newHashSet(
            filter(
                transform(testObject.indexedObjects, spatialObjectConcretizer(testObject)),
                isOverlapping(this.computeQuery(testObject))).iterator());
      }

      @Override
      Iterable<SpatialObject> perform(SpatialJoinTest testObject) throws IOException, InterruptedException {
        return toIterable(transform(
            testObject.fixture.session.iterator(
                testObject.fixture.query.iterator().next(),
                testObject.fixture.spatialIndex),
            recordToSpatialObject()));
      }
    },
    WITH_ANOTHER_INDEX {
      @Override
      Set<SpatialObject> computeQuery(SpatialJoinTest testObject) {
        return newHashSet(transform(testObject.queryObjects, spatialObjectConcretizer(testObject)));
      }

      @Override
      Set<SpatialObject> computeExpectationForQuery(SpatialJoinTest testObject) {
        return newHashSet(
            filter(
                transform(testObject.indexedObjects, spatialObjectConcretizer(testObject)),
                isOverlapping(this.computeQuery(testObject))).iterator());
      }

      @Override
      Iterable<SpatialObject> perform(SpatialJoinTest testObject) throws IOException, InterruptedException {
        return toIterable(Iterators.transform(
            testObject.fixture.session.iterator(
                buildSpatialIndexFromSpatialObjectSet(
                    testObject.fixture.spatialIndex.space(),
                    testObject.fixture.query),
                testObject.fixture.spatialIndex),
            rightSide()));
      }
    },
    WITH_ITSELF {
      @Override
      Set<SpatialObject> computeQuery(SpatialJoinTest testObject) {
        return newHashSet(transform(testObject.indexedObjects, spatialObjectConcretizer(testObject)));
      }

      @Override
      Set<SpatialObject> computeExpectationForQuery(SpatialJoinTest testObject) {
        return newHashSet(
            filter(
                transform(testObject.indexedObjects, spatialObjectConcretizer(testObject)),
                isOverlapping(this.computeQuery(testObject))).iterator());
      }

      @Override
      Iterable<SpatialObject> perform(SpatialJoinTest testObject) throws IOException, InterruptedException {
        return toIterable(transform(
            testObject.fixture.session.iterator(
                testObject.fixture.spatialIndex,
                testObject.fixture.spatialIndex),
            rightSide()));
      }

    },
    WITH_SPATIAL_OBJECT_AFTER_REMOVAL {
      @Override
      Set<SpatialObject> computeQuery(SpatialJoinTest testObject) {
        ////
        // Returns a spatial object that should match all the objects in the
        // spatial index. (i.e., testObject.indexedObjects)
        return Collections.singleton((SpatialObject) new SpatialObject.Box(0, 0, 50, 50));
      }

      @Override
      Set<SpatialObject> computeExpectationForQuery(SpatialJoinTest testObject) {
        Set<SpatialObject> ret = newHashSet(transform(testObject.indexedObjects, spatialObjectConcretizer(testObject)));
        ////
        // IssueFoundByJCUnit: remove operation removes only the exact object not all overlapping object.
        ret.remove(testObject.queryObjects.get(0).concretize(testObject));
        // ret.removeAll(WITH_SPATIAL_OBJECT.computeExpectationForQuery(testObject));
        return ret;
      }

      @Override
      Iterable<SpatialObject> perform(SpatialJoinTest testObject) throws IOException, InterruptedException {
        SpatialJoinTest.performRemoval(
            testObject.fixture.spatialIndex,
            testObject.queryObjects.get(0).concretize(testObject)
        );
        return WITH_SPATIAL_OBJECT.perform(testObject);
      }
    };

    abstract Iterable<SpatialObject> perform(SpatialJoinTest testObject) throws IOException, InterruptedException;

    abstract Set<SpatialObject> computeQuery(SpatialJoinTest spatialJoinTest);

    abstract Set<SpatialObject> computeExpectationForQuery(SpatialJoinTest spatialJoinTest);

    private static Predicate<SpatialObject> isOverlapping(final Iterable<SpatialObject> spatialObjects) {
      return new Predicate<SpatialObject>() {
        @Override
        public boolean apply(SpatialObject input) {
          for (SpatialObject each : spatialObjects) {
            if (FILTER.overlap(input, each))
              return true;
          }
          return false;
        }
      };
    }
  }
}
