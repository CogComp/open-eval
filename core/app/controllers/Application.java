package controllers;

import play.*;
import play.mvc.*;

import views.html.*;

import java.util.*;

import models.*;

public class Application extends Controller {

    /**
     * Connect to db here to get the datasets
     */
    private List<String> getDatasets() {
        List<String> datasets = new ArrayList<>();
        datasets.add("dataset 1");
        datasets.add("dataset 2");
        datasets.add("dataset 3");
        return datasets;
    }

    /**
     * Connect to db here to get the solvers for a particular dataset
     */
    private List<String> getSolvers(String dataset) {
        List<String> solvers = new ArrayList<>();
        solvers.add("solver 1 for " + dataset);
        solvers.add("solver 2 for " + dataset);
        solvers.add("solver 3 for " + dataset);
        return solvers;
    }

    public Result index() {
        List<String> datasets = getDatasets();
        return ok(index.render(datasets));
    }

    public Result dataset(String dataset_name) {
        DatasetViewModel viewModel = new DatasetViewModel();
        viewModel.dataset = dataset_name;
        viewModel.solvers = getSolvers(dataset_name);;
        return ok(dataset.render(viewModel));
    }

    public Result recipe(String dataset_name, String solver_name) {
        RecipeViewModel viewModel = new RecipeViewModel();
        viewModel.dataset = dataset_name;
        viewModel.solver = solver_name;
        return ok(recipe.render(viewModel));
    }

}
