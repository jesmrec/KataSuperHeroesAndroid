/*
 * Copyright (C) 2015 Karumi.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.karumi.katasuperheroes;

import com.karumi.katasuperheroes.ui.view.SuperHeroDetailActivity;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import com.karumi.katasuperheroes.di.MainComponent;
import com.karumi.katasuperheroes.di.MainModule;
import com.karumi.katasuperheroes.model.SuperHero;
import com.karumi.katasuperheroes.model.SuperHeroesRepository;
import com.karumi.katasuperheroes.recyclerview.RecyclerViewInteraction;
import com.karumi.katasuperheroes.ui.presenter.Presenter;
import com.karumi.katasuperheroes.ui.view.MainActivity;
import it.cosenonjaviste.daggermock.DaggerMockRule;
import static android.support.test.espresso.action.ViewActions.click;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.RecursiveAction;

import android.view.View;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.AllOf.allOf;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;



@RunWith(AndroidJUnit4.class) @LargeTest public class MainActivityTest {

  @Rule public DaggerMockRule<MainComponent> daggerRule =
      new DaggerMockRule<>(MainComponent.class, new MainModule()).set(
          new DaggerMockRule.ComponentSetter<MainComponent>() {
            @Override public void setComponent(MainComponent component) {
              SuperHeroesApplication app =
                  (SuperHeroesApplication) InstrumentationRegistry.getInstrumentation()
                      .getTargetContext()
                      .getApplicationContext();
              app.setComponent(component);
            }
          });

  @Rule public IntentsTestRule<MainActivity> activityRule =
      new IntentsTestRule<>(MainActivity.class, true, false);

  @Mock SuperHeroesRepository repository;

  @Test
  public void showsEmptyCaseIfThereAreNoSuperHeroes() {
    givenThereAreNoSuperHeroes();
    startActivity();
    onView(withText("¯\\_(ツ)_/¯")).check(matches(isDisplayed()));
  }

  @Test
  public void showsNoEmptyCaseIfThereAreNoSuperHeroes() {
    givenThereAreSuperHeroes();
    startActivity();
    onView(withText("¯\\_(ツ)_/¯")).check(matches(not(isDisplayed())));
  }

    @Test
    public void showsNoProgressBarIfThereAreSuperHeroes() {
        givenThereAreSuperHeroes();
        startActivity();
        onView(withId(R.id.progress_bar)).check(matches(not(isDisplayed())));
    }

    @Test
    public void showsProgressBarIfThereAreNoSuperHeroes() {
        givenThereAreNoSuperHeroes();
        startActivity();
        onView(withId(R.id.progress_bar)).check(matches(isDisplayed()));
    }


    @Test
    public void checkIfEachSuperHeroHasHisName() {

        List<SuperHero> superHeros = givenThereAreSuperHeroes2();

        List<String> names = new ArrayList<>();
        for (SuperHero superHero: superHeros) {
            names.add(superHero.getName());
        }
        startActivity();
        RecyclerViewInteraction.<String>onRecyclerView(withId(R.id.recycler_view))
        .withItems(names).check(new RecyclerViewInteraction.ItemViewAssertion<String>(){
            @Override public void check(String name, View view, NoMatchingViewException e){
                matches(hasDescendant(withText(name))).check(view, e);
            }
        });

    }

    @Test
    public void checkIfHeroIsAvenger() {
        List<SuperHero> superHeros = givenThereAreSuperHeroes2();
        startActivity();
        RecyclerViewInteraction.<SuperHero>onRecyclerView(withId(R.id.recycler_view))
                .withItems(superHeros).check(new RecyclerViewInteraction.ItemViewAssertion<SuperHero>(){
                @Override public void check(SuperHero hero, View view, NoMatchingViewException e){
                        matches(hasDescendant(allOf(withId(R.id.iv_avengers_badge), isDisplayed()))).check(view, e);
            }
        });
    }

    @Test
    public void checkIfIntentIsSetCorrectly() {
        List<SuperHero> superHeros = givenThereAreSuperHeroes2();
        startActivity();
        onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        //check that goes to the correct activity
        intended(hasComponent(SuperHeroDetailActivity.class.getCanonicalName()));
        //check intent has the parameter
        SuperHero selectedHero = superHeros.get(0);
        intended(hasExtra("super_hero_name_key", selectedHero.getName()));
    }



    private void givenThereAreNoSuperHeroes() {
        when(repository.getAll()).thenReturn(Collections.<SuperHero>emptyList());
    }


    private List<SuperHero> givenThereAreSuperHeroes2() {
        List<SuperHero> heroesList = new ArrayList<SuperHero>();
          for (int i = 0; i < 10; i++) {
                  heroesList.add(new SuperHero("name" + i, "a", true, "a"));
                    //Added to avoid the problems in the intended
                    when(repository.getByName(heroesList.get(i).getName())).thenReturn(heroesList.get(i));
              }

        when(repository.getAll()).thenReturn(heroesList);
        return heroesList;
      }

    private void givenThereAreSuperHeroes() {
        ArrayList<SuperHero> emptyList = new ArrayList<SuperHero>();
        for (int i = 0; i < 10; i++) {
            emptyList.add(new SuperHero("name"+i,"a",true,"a"));
        }

        when(repository.getAll()).thenReturn(emptyList);

    }

    private MainActivity startActivity() {
    return activityRule.launchActivity(null);
  }

}