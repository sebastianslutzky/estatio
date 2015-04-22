/*
 * Copyright 2015 Yodo Int. Projects and Consultancy
 *
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.estatio.fixture.budget;

import javax.inject.Inject;

import org.joda.time.LocalDate;

import org.estatio.dom.asset.Properties;
import org.estatio.dom.asset.Property;
import org.estatio.dom.budget.Budget;
import org.estatio.dom.budget.Budgets;
import org.estatio.fixture.EstatioFixtureScript;

/**
 * Created by jodo on 22/04/15.
 */
public abstract class BudgetAbstact extends EstatioFixtureScript {

    protected Budget createBudget(
            final Property property,
            final LocalDate startDate,
            final LocalDate endDate,
            final ExecutionContext fixtureResults){
        Budget budget = budgets.newBudget(property, startDate, endDate);
        return fixtureResults.addResult(this, budget);
    }

    @Inject
    protected Budgets budgets;

    @Inject
    protected Properties properties;
}
