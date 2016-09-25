package com.github.dakusui.geophile.tests;

import com.geophile.z.Space;
import com.geophile.z.SpatialIndex;
import com.geophile.z.SpatialJoin;
import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.ActionUnit;
import com.github.dakusui.actionunit.Context;
import com.github.dakusui.actionunit.DataSource;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import com.github.dakusui.geophile.mymodel.Record;
import com.github.dakusui.geophile.mymodel.SpatialJoinFilter;
import com.github.dakusui.geophile.mymodel.SpatialObject;
import com.github.dakusui.geophile.mymodel.index.Index;
import com.github.dakusui.geophile.testbase.SpaceProvider;
import com.github.dakusui.geophile.testbase.SpatialObjectProvider;
import com.github.dakusui.geophile.testbase.SpatialObjectSet;
import com.github.dakusui.geophile.testbase.SpatialObjectSetProvider;
import com.github.dakusui.jcunit.plugins.constraints.SmartConstraintChecker;
import com.github.dakusui.jcunit.runners.standard.TestCaseUtils;
import com.github.dakusui.jcunit.runners.standard.annotations.Checker;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.runners.standard.annotations.GenerateCoveringArrayWith;
import com.google.common.base.Function;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
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
 * <p>
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
@GenerateCoveringArrayWith(checker = @Checker(SmartConstraintChecker.class))
public class SpatialJoinWithSpatialObjectTest {
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Print {
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface Execute {
  }

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
  public Action verify() {
    return sequential(
        createSpace(),
        createIndex(this.stableRecords),
        createSpatialIndex(this.space, this.index, this.options),
        createRecordBuilder(this.stableRecords),
        loadObjects(),
        foreach(
            spatialObjectsForIndex(),
            SEQUENTIALLY,
            tag(0),
            addSpatialObjectToSpatialIndex(this.spatialIndex, this.recordBuilder, this.options)
        ),
        createSpatialJoinSession(this.duplicates, SpatialJoinFilter.INSTANCE),
        createQuery()
    );
  }

  @Print
  public void print(Action action) {
    action.accept(new ActionRunner.Impl());
  }

  @Execute
  public void execute(Action action) {
    ActionRunner.WithResult runner = new ActionRunner.WithResult();
    try {
      action.accept(runner);
    } finally {
      action.accept(runner.createPrinter());
    }
  }

  private Action createQuery() {
    return simple("createQuery", new Runnable() {
      @Override
      public void run() {
        query = queryObjectProvider.get();
      }
    });
  }

  private Action createSpatialJoinSession(final SpatialJoin.Duplicates duplicates, final SpatialJoinFilter filter) {
    return simple("createSpatialJoinSession", new Runnable() {
      @Override
      public void run() {
        session = SpatialJoin.newSpatialJoin(duplicates, filter);
      }
    });
  }

  private Action createRecordBuilder(final boolean stableRecords) {
    return simple("createRecordBuilder", new Runnable() {
      @Override
      public void run() {
        recordBuilder = new Record.Builder(stableRecords);
      }
    });
  }

  private DataSource.Factory.Base<SpatialObject> spatialObjectsForIndex() {
    return new DataSource.Factory.Base<SpatialObject>() {
      @Override
      protected Iterable<SpatialObject> iterable(Context context) {
        return spatialObjects.forIndex();
      }
    };
  }

  private Action loadObjects() {
    return simple("loadObjects", new Runnable() {
      @Override
      public void run() {
        spatialObjects = spatialObjectSetProvider.get();
      }
    });
  }

  private Action createIndex(final boolean stableRecords) {
    return simple("createIndex", new Runnable() {
      @Override
      public void run() {
        index = new Index(stableRecords);
      }
    });
  }

  private Action createSpace() {
    return simple("createSpace", new Runnable() {
      @Override
      public void run() {
        space = spaceProvider.create();
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

  private Action createSpatialIndex(final Space space, final Index index, final SpatialIndex.Options options) {
    return simple("createSpatialIndex", new Runnable() {
      @Override
      public void run() {
        try {
          if (options == null) {
            spatialIndex = SpatialIndex.newSpatialIndex(space, index);
          } else {
            spatialIndex = SpatialIndex.newSpatialIndex(space, index, options);
          }
        } catch (IOException | InterruptedException e) {
          throw ActionException.wrap(e);
        }
      }
    });
  }

  private static Sink<SpatialObject> addSpatialObjectToSpatialIndex(final SpatialIndex<Record> spatialIndex, final Record.Builder recordBuilder, final SpatialIndex.Options options) {
    return new Sink<SpatialObject>() {
      @Override
      public void apply(SpatialObject spatialObject, Context context) {
        try {
          if (SpatialIndex.Options.SINGLE_CELL == options) {
            spatialIndex.add(spatialObject, recordBuilder.with(spatialObject), 1);
          } else {
            spatialIndex.add(spatialObject, recordBuilder.with(spatialObject));
          }
        } catch (IOException | InterruptedException e) {
          throw ActionException.wrap(e);
        }
      }

      @Override
      public String toString() {
        return format("addSpatialObjectToSpatialIndex(%s,%s)", spatialIndex, recordBuilder);
      }
    };
  }

  private static Iterable<SpatialObject> toIterable(Iterator<SpatialObject> iterator) {
    return asList(toArray(iterator, SpatialObject.class));
  }


  private static class Stash extends SpatialJoinWithSpatialObjectTest {
  /*
  @Before
  public void wireObjects() throws IOException, InterruptedException {
    ;
    this.spatialIndex = createSpatialIndex(this.space, this.index, this.options);
    this.spatialObjects = this.spatialObjectSetProvider.get();

    Record.Builder recordBuilder = new Record.Builder(stableRecords);
    for (SpatialObject each : this.spatialObjects.forIndex()) {
      addSpatialObjectToSpatialIndex(each, spatialIndex, recordBuilder, options);
    }

    this.session = SpatialJoin.newSpatialJoin(this.duplicates, SpatialJoinFilter.INSTANCE);
    this.query = this.queryObjectProvider.get();
  }
  */

    @Test
    public void printTestCase() {
      System.out.println(TestCaseUtils.toTestCase(this));
    }

    @Test
    public void printIndexedObjectSet() throws IOException, InterruptedException {
      int count = 0;
      for (SpatialObject each : this.spatialObjects.forIndex()) {
        System.out.println("  " + each);
        count++;
      }
      System.out.println(count + " objects are added to SpatialIndex");
    }

    @Test
    public void printExpectation() {
      System.out.println("Expectation");
      int count = 0;
      for (SpatialObject each : Sets.newHashSet(this.spatialObjectSetProvider.get().getObjectsMatchingWith(query))) {
        System.out.println("  " + each);
        count++;
      }
      System.out.println(count + " unique objects are expected to be returned.");
    }

    @Test
    public void printQueryResult() throws IOException, InterruptedException {
      System.out.println("query=" + this.query);
      int count = 0;
      Set<SpatialObject> uniqueObjects = Sets.newHashSet();
      for (SpatialObject each : performQuery(this.spatialIndex, this.session, this.query)) {
        System.out.println("  " + each);
        uniqueObjects.add(each);
        count++;
      }
      System.out.println(count + " objects hit " + this.query + "(" + uniqueObjects.size() + " are unique)");
    }

    @Test
    public void verifyQueryResult() throws IOException, InterruptedException {
      assertEquals(
          Sets.newHashSet(this.spatialObjectSetProvider.get().getObjectsMatchingWith(query)),
          Sets.newHashSet(performQuery(this.spatialIndex, this.session, this.query)));
    }
  }
}
